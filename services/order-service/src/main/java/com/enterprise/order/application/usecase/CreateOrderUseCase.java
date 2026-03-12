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
import java.math.BigDecimal;
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

    } catch (Exception e) {
      log.error("Failed to create order for customer: {}. Compensating stock reservations.", 
                request.customerId(), e);
      
      // Compensate stock reservations
      compensateStockReservations(stockReservations);
      
      throw e;
    }
  }

  private void validateRequest(CreateOrderRequest request) {
    if (request == null) {
      throw new IllegalArgumentException("CreateOrderRequest cannot be null");
    }
  }

  private void compensateStockReservations(List<StockReservation> stockReservations) {
    for (StockReservation reservation : stockReservations) {
      try {
        productServicePort.increaseStock(reservation.productId(), reservation.quantity());
        log.debug("Compensated stock: {} units of product {}", 
                 reservation.quantity(), reservation.productId());
      } catch (Exception e) {
        log.error("Failed to compensate stock for product: {}", reservation.productId(), e);
        // Continue with other compensations
      }
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
      log.debug("Published OrderCreated event for order: {}", order.getId());
    } catch (Exception e) {
      log.error("Failed to publish OrderCreated event for order: {}", order.getId(), e);
      // Don't fail the entire operation for event publishing failures
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
}