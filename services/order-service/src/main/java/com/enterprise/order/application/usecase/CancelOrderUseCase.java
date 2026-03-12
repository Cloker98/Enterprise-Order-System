package com.enterprise.order.application.usecase;

import com.enterprise.order.application.dto.request.CancelOrderRequest;
import com.enterprise.order.application.port.EventPublisherPort;
import com.enterprise.order.application.port.ProductServicePort;
import com.enterprise.order.domain.model.Order;
import com.enterprise.order.domain.model.OrderId;
import com.enterprise.order.domain.repository.OrderRepository;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for cancelling orders.
 *
 * <p>This use case handles:
 * - Order cancellation validation
 * - Stock compensation (returning reserved stock)
 * - Order status update
 * - Event publishing for downstream services
 */
@Service
@Transactional
public class CancelOrderUseCase {

  private static final Logger log = LoggerFactory.getLogger(CancelOrderUseCase.class);

  private final OrderRepository orderRepository;
  private final ProductServicePort productServicePort;
  private final EventPublisherPort eventPublisherPort;

  /**
   * Constructor for CancelOrderUseCase.
   *
   * @param orderRepository the order repository
   * @param productServicePort the product service port
   * @param eventPublisherPort the event publisher port
   */
  public CancelOrderUseCase(OrderRepository orderRepository,
                           ProductServicePort productServicePort,
                           EventPublisherPort eventPublisherPort) {
    this.orderRepository = orderRepository;
    this.productServicePort = productServicePort;
    this.eventPublisherPort = eventPublisherPort;
  }

  /**
   * Executes the cancel order use case.
   *
   * @param request the cancel order request
   * @throws OrderNotFoundException if order is not found
   * @throws InvalidOrderStateException if order cannot be cancelled
   * @throws StockCompensationException if stock compensation fails
   */
  public void execute(CancelOrderRequest request) {
    log.info("Cancelling order: {} with reason: {}", request.orderId(), request.reason());

    validateRequest(request);

    OrderId orderId = OrderId.from(request.orderId());
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new OrderNotFoundException("Order not found: " + request.orderId()));

    if (!order.canBeCancelled()) {
      throw new InvalidOrderStateException(
          "Order cannot be cancelled in current state: " + order.getStatus());
    }

    try {
      // Step 1: Compensate stock for all order items
      compensateStock(order);

      // Step 2: Cancel the order
      order.cancel(request.reason());

      // Step 3: Save the updated order
      Order savedOrder = orderRepository.save(order);
      log.info("Order cancelled successfully: {}", savedOrder.getId());

      // Step 4: Publish cancellation event
      publishOrderCancelledEvent(savedOrder);

    } catch (StockCompensationException | EventPublishingException e) {
      // Rethrow specific exceptions without additional logging to avoid duplication
      throw e;
    } catch (Exception e) {
      throw new OrderCancellationException(
          "Failed to cancel order: " + request.orderId() + ". Reason: " + e.getMessage(), e);
    }
  }

  private void validateRequest(CancelOrderRequest request) {
    if (request == null) {
      throw new IllegalArgumentException("CancelOrderRequest cannot be null");
    }
    
    if (request.orderId() == null || request.orderId().trim().isEmpty()) {
      throw new IllegalArgumentException("Order ID cannot be null or empty");
    }
    
    if (request.reason() == null || request.reason().trim().isEmpty()) {
      throw new IllegalArgumentException("Cancellation reason cannot be null or empty");
    }
  }

  private void compensateStock(Order order) {
    List<StockCompensationFailure> failures = order.getItems().stream()
        .map(item -> {
          try {
            productServicePort.increaseStock(item.getProductId(), item.getQuantity());
            log.debug("Compensated stock: {} units of product {}", 
                     item.getQuantity(), item.getProductId());
            return null;
          } catch (ProductServicePort.ProductNotFoundException e) {
            log.warn("Cannot compensate stock for non-existent product: {}. " 
                    + "This may indicate data inconsistency.", item.getProductId(), e);
            return new StockCompensationFailure(item.getProductId().value(), 
                                              item.getQuantity(), e.getMessage());
          } catch (ProductServicePort.ProductServiceUnavailableException e) {
            throw new StockCompensationException(
                "Product service unavailable during stock compensation for product: " 
                + item.getProductId() + ". Manual intervention required.", e);
          } catch (Exception e) {
            throw new StockCompensationException(
                "Critical failure during stock compensation for product: " + item.getProductId() 
                + ". This may result in stock inconsistency and requires immediate attention.", e);
          }
        })
        .filter(Objects::nonNull)
        .toList();

    if (!failures.isEmpty()) {
      log.warn("Stock compensation completed with {} failures for order: {}", 
               failures.size(), order.getId());
      // Note: We continue with cancellation even if some products are not found
      // This handles the case where products were deleted after order creation
    } else {
      log.info("All stock compensated successfully for order: {}", order.getId());
    }
  }

  private void publishOrderCancelledEvent(Order order) {
    try {
      List<EventPublisherPort.OrderItemEvent> itemEvents = order.getItems().stream()
          .map(item -> new EventPublisherPort.OrderItemEvent(
              item.getProductId().value(),
              item.getQuantity(),
              item.getUnitPrice().amount().toString()
          ))
          .toList();

      EventPublisherPort.OrderCancelledEvent event = new EventPublisherPort.OrderCancelledEvent(
          order.getId().value(),
          order.getCustomerId().value(),
          order.getCancellationReason(),
          itemEvents
      );

      eventPublisherPort.publishOrderCancelled(event);
      log.info("Successfully published OrderCancelled event for order: {}", order.getId());
    } catch (Exception e) {
      throw new EventPublishingException(
          "Failed to publish OrderCancelled event for order: " + order.getId() 
          + ". Order was cancelled successfully but downstream systems may not be notified.", e);
    }
  }

  /**
   * Record to track stock compensation failures.
   */
  private record StockCompensationFailure(String productId, int quantity, String reason) {
  }

  /**
   * Exception thrown when order is not found.
   */
  public static class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String message) {
      super(message);
    }
  }

  /**
   * Exception thrown when order cannot be cancelled due to its current state.
   */
  public static class InvalidOrderStateException extends RuntimeException {
    public InvalidOrderStateException(String message) {
      super(message);
    }
  }

  /**
   * Exception thrown when order cancellation fails.
   */
  public static class OrderCancellationException extends RuntimeException {
    public OrderCancellationException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  /**
   * Exception thrown when stock compensation fails critically.
   */
  public static class StockCompensationException extends RuntimeException {
    public StockCompensationException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  /**
   * Exception thrown when event publishing fails.
   */
  public static class EventPublishingException extends RuntimeException {
    public EventPublishingException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}