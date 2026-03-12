package com.enterprise.order.application.usecase;

import com.enterprise.order.application.dto.request.CreateOrderRequest;
import com.enterprise.order.application.dto.request.OrderItemRequest;
import com.enterprise.order.application.dto.response.OrderResponse;
import com.enterprise.order.application.port.EventPublisherPort;
import com.enterprise.order.application.port.ProductServicePort;
import com.enterprise.order.domain.model.CustomerId;
import com.enterprise.order.domain.model.Money;
import com.enterprise.order.domain.model.Order;
import com.enterprise.order.domain.model.OrderItem;
import com.enterprise.order.domain.model.ProductId;
import com.enterprise.order.domain.repository.OrderRepository;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for creating orders.
 *
 * <p>Orchestrates the order creation process including:
 * - Product validation and stock checking
 * - Stock reservation
 * - Order creation
 * - Event publishing
 * - Compensation in case of failures
 */
@Service
@Transactional
public class CreateOrderUseCase {

  private static final Logger log = LoggerFactory.getLogger(CreateOrderUseCase.class);

  private final OrderRepository orderRepository;
  private final ProductServicePort productServicePort;
  private final EventPublisherPort eventPublisherPort;

  /**
   * Constructor for CreateOrderUseCase.
   *
   * @param orderRepository the order repository
   * @param productServicePort the product service port
   * @param eventPublisherPort the event publisher port
   */
  public CreateOrderUseCase(OrderRepository orderRepository,
                           ProductServicePort productServicePort,
                           EventPublisherPort eventPublisherPort) {
    this.orderRepository = orderRepository;
    this.productServicePort = productServicePort;
    this.eventPublisherPort = eventPublisherPort;
  }

  /**
   * Executes the order creation use case.
   *
   * @param request the order creation request
   * @return the created order response
   * @throws IllegalArgumentException if request is invalid
   * @throws ProductServicePort.ProductNotFoundException if product is not found
   * @throws ProductServicePort.InsufficientStockException if insufficient stock
   * @throws ProductServicePort.ProductServiceUnavailableException if service is unavailable
   * @throws OrderCreationException if order creation fails after stock reservation
   */
  public OrderResponse execute(CreateOrderRequest request) {
    log.info("Creating order for customer: {}", request.customerId());

    validateRequest(request);

    CustomerId customerId = CustomerId.from(request.customerId());
    List<OrderItem> orderItems = new ArrayList<>();
    List<StockReservation> stockReservations = new ArrayList<>();

    try {
      // Step 1: Validate products and reserve stock
      for (OrderItemRequest itemRequest : request.items()) {
        ProductId productId = ProductId.from(itemRequest.productId());
        
        // Get product information
        ProductServicePort.ProductInfo productInfo = productServicePort.getProduct(productId);
        log.debug("Retrieved product info: {} - {}", productInfo.id(), productInfo.name());

        // Reserve stock
        productServicePort.decreaseStock(productId, itemRequest.quantity());
        stockReservations.add(new StockReservation(productId, itemRequest.quantity()));
        log.debug("Reserved stock: {} units of product {}", itemRequest.quantity(), productId);

        // Create order item
        Money unitPrice = Money.brl(productInfo.price());
        OrderItem orderItem = OrderItem.create(
            productId,
            productInfo.name(),
            itemRequest.quantity(),
            unitPrice
        );
        orderItems.add(orderItem);
      }

      // Step 2: Create and save order
      Order order = Order.create(customerId, orderItems);
      Order savedOrder = orderRepository.save(order);
      log.info("Order created successfully: {}", savedOrder.getId());

      // Step 3: Publish event
      publishOrderCreatedEvent(savedOrder);

      // Step 4: Convert to response
      return mapToResponse(savedOrder);

    } catch (ProductServicePort.ProductNotFoundException 
           | ProductServicePort.InsufficientStockException 
           | ProductServicePort.ProductServiceUnavailableException e) {
      // Compensate stock reservations and rethrow original exception
      compensateStockReservations(stockReservations);
      throw e;
    } catch (Exception e) {
      // Compensate stock reservations and rethrow with contextual information
      compensateStockReservations(stockReservations);
      throw new OrderCreationException("Failed to create order for customer: " 
                                      + request.customerId(), e);
    }
  }

  private void validateRequest(CreateOrderRequest request) {
    if (request == null) {
      throw new IllegalArgumentException("CreateOrderRequest cannot be null");
    }
  }

  private void compensateStockReservations(List<StockReservation> stockReservations) {
    int compensationFailures = 0;
    
    for (StockReservation reservation : stockReservations) {
      try {
        productServicePort.increaseStock(reservation.productId(), reservation.quantity());
        log.debug("Successfully compensated stock: {} units of product {}", 
                 reservation.quantity(), reservation.productId());
      } catch (ProductServicePort.ProductNotFoundException e) {
        compensationFailures++;
        log.warn("Cannot compensate stock for non-existent product: {}. " 
                + "This may indicate data inconsistency.", reservation.productId(), e);
        // Handle: Product was deleted between reservation and compensation
        // This is logged as warning since it's a recoverable inconsistency
      } catch (ProductServicePort.ProductServiceUnavailableException e) {
        compensationFailures++;
        log.error("Product service unavailable during stock compensation for product: {}. " 
                 + "Manual intervention may be required.", reservation.productId(), e);
        // Handle: Service is down, manual intervention needed
        // This is logged as error since it requires attention
      } catch (Exception e) {
        // Rethrow with contextual information for critical compensation failures
        throw new StockCompensationException(
            "Critical failure during stock compensation for product: " + reservation.productId() 
            + ". This may result in stock inconsistency and requires immediate attention.", e);
      }
    }
    
    if (compensationFailures > 0) {
      log.warn("Stock compensation completed with {} failures out of {} reservations. " 
               + "Manual review recommended.", compensationFailures, stockReservations.size());
    } else {
      log.info("All stock reservations compensated successfully. Total: {}", 
               stockReservations.size());
    }
  }

  private void publishOrderCreatedEvent(Order order) {
    try {
      List<EventPublisherPort.OrderItemEvent> itemEvents = order.getItems().stream()
          .map(item -> new EventPublisherPort.OrderItemEvent(
              item.getProductId().value(),
              item.getQuantity(),
              item.getUnitPrice().amount().toString()
          ))
          .toList();

      EventPublisherPort.OrderCreatedEvent event = new EventPublisherPort.OrderCreatedEvent(
          order.getId().value(),
          order.getCustomerId().value(),
          order.getTotalAmount().amount().toString(),
          itemEvents
      );

      eventPublisherPort.publishOrderCreated(event);
      log.info("Successfully published OrderCreated event for order: {}", order.getId());
    } catch (Exception e) {
      // Rethrow with contextual information for event publishing failures
      throw new EventPublishingException(
          "Failed to publish OrderCreated event for order: " + order.getId() 
          + ". Order was created successfully but downstream systems may not be notified.", e);
    }
  }

  private OrderResponse mapToResponse(Order order) {
    List<com.enterprise.order.application.dto.response.OrderItemResponse> itemResponses = 
        order.getItems().stream()
            .map(item -> new com.enterprise.order.application.dto.response.OrderItemResponse(
                item.getProductId().value(),
                item.getProductName(),
                item.getQuantity(),
                item.getUnitPrice().amount(),
                item.getTotalPrice().amount()
            ))
            .toList();

    return new OrderResponse(
        order.getId().value(),
        order.getCustomerId().value(),
        itemResponses,
        order.getTotalAmount().amount(),
        order.getStatus().name(),
        order.getCancellationReason(),
        order.getCreatedAt(),
        order.getUpdatedAt()
    );
  }

  /**
   * Record to track stock reservations for compensation.
   */
  private record StockReservation(ProductId productId, int quantity) {
  }

  /**
   * Exception thrown when order creation fails after stock has been reserved.
   */
  public static class OrderCreationException extends RuntimeException {
    public OrderCreationException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  /**
   * Exception thrown when stock compensation fails critically.
   * This indicates a potential data inconsistency that requires immediate attention.
   */
  public static class StockCompensationException extends RuntimeException {
    public StockCompensationException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  /**
   * Exception thrown when event publishing fails.
   * This is typically non-critical as the main operation (order creation) succeeded.
   */
  public static class EventPublishingException extends RuntimeException {
    public EventPublishingException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}