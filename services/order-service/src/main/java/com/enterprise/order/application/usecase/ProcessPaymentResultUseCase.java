package com.enterprise.order.application.usecase;

import com.enterprise.order.application.port.EventPublisherPort;
import com.enterprise.order.application.port.ProductServicePort;
import com.enterprise.order.domain.model.Order;
import com.enterprise.order.domain.model.OrderId;
import com.enterprise.order.domain.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for processing payment results from Payment Service.
 *
 * <p>This use case handles SAGA orchestration by processing payment
 * success and failure events, updating order status accordingly,
 * and triggering compensation actions when needed.
 */
@Service
@Transactional
public class ProcessPaymentResultUseCase {

  private static final Logger log = LoggerFactory.getLogger(ProcessPaymentResultUseCase.class);

  private final OrderRepository orderRepository;
  private final ProductServicePort productServicePort;
  private final EventPublisherPort eventPublisherPort;

  /**
   * Constructor for ProcessPaymentResultUseCase.
   *
   * @param orderRepository the order repository
   * @param productServicePort the product service port
   * @param eventPublisherPort the event publisher port
   */
  public ProcessPaymentResultUseCase(OrderRepository orderRepository,
                                    ProductServicePort productServicePort,
                                    EventPublisherPort eventPublisherPort) {
    this.orderRepository = orderRepository;
    this.productServicePort = productServicePort;
    this.eventPublisherPort = eventPublisherPort;
  }

  /**
   * Handles successful payment processing.
   *
   * @param request the payment processed request
   */
  public void handlePaymentProcessed(PaymentProcessedRequest request) {
    log.info("Handling payment success for order: {}, paymentId: {}", 
             request.orderId(), request.paymentId());

    try {
      OrderId orderId = OrderId.from(request.orderId());
      Order order = orderRepository.findById(orderId)
          .orElseThrow(() -> new OrderNotFoundException("Order not found: " + request.orderId()));

      // Update order status to CONFIRMED
      order.confirm();
      Order savedOrder = orderRepository.save(order);

      log.info("Order confirmed successfully: {}", savedOrder.getId());

      // Publish order status changed event
      publishOrderStatusChangedEvent(savedOrder, "PENDING", "CONFIRMED", 
                                   "Payment processed successfully");

    } catch (Exception e) {
      log.error("Failed to process payment success for order: {}", request.orderId(), e);
      throw new PaymentProcessingException(
          "Failed to process payment success for order: " + request.orderId(), e);
    }
  }

  /**
   * Handles failed payment processing with SAGA compensation.
   *
   * @param request the payment failed request
   */
  public void handlePaymentFailed(PaymentFailedRequest request) {
    log.info("Handling payment failure for order: {}, reason: {}", 
             request.orderId(), request.reason());

    try {
      OrderId orderId = OrderId.from(request.orderId());
      Order order = orderRepository.findById(orderId)
          .orElseThrow(() -> new OrderNotFoundException("Order not found: " + request.orderId()));

      // Step 1: Cancel the order
      String cancellationReason = "Payment failed: " + request.reason();
      order.cancel(cancellationReason);

      // Step 2: Compensate stock (return reserved stock)
      compensateStock(order);

      // Step 3: Save the cancelled order
      Order savedOrder = orderRepository.save(order);
      log.info("Order cancelled due to payment failure: {}", savedOrder.getId());

      // Step 4: Publish order cancellation event
      publishOrderCancelledEvent(savedOrder);

    } catch (Exception e) {
      log.error("Failed to process payment failure for order: {}", request.orderId(), e);
      throw new PaymentProcessingException(
          "Failed to process payment failure for order: " + request.orderId(), e);
    }
  }

  /**
   * Compensates stock by returning reserved quantities.
   *
   * @param order the cancelled order
   */
  private void compensateStock(Order order) {
    log.info("Compensating stock for cancelled order: {}", order.getId());

    int compensationFailures = 0;
    
    for (var item : order.getItems()) {
      try {
        productServicePort.increaseStock(item.getProductId(), item.getQuantity());
        log.debug("Compensated stock: {} units of product {}", 
                 item.getQuantity(), item.getProductId());
      } catch (ProductServicePort.ProductNotFoundException e) {
        compensationFailures++;
        log.warn("Cannot compensate stock for non-existent product: {}. " 
                + "This may indicate data inconsistency.", item.getProductId(), e);
        // Continue with other items - product might have been deleted
      } catch (ProductServicePort.ProductServiceUnavailableException e) {
        compensationFailures++;
        log.error("Product service unavailable during stock compensation for product: {}. " 
                 + "Manual intervention may be required.", item.getProductId(), e);
        // Continue with other items - service might recover
      } catch (Exception e) {
        compensationFailures++;
        log.error("Unexpected error during stock compensation for product: {}. " 
                 + "Manual intervention may be required.", item.getProductId(), e);
        // Continue with other items - don't fail entire compensation
      }
    }

    if (compensationFailures > 0) {
      log.warn("Stock compensation completed with {} failures for order: {}. " 
               + "Manual review recommended.", compensationFailures, order.getId());
    } else {
      log.info("All stock compensated successfully for order: {}", order.getId());
    }
  }

  /**
   * Publishes order status changed event.
   *
   * @param order the order
   * @param previousStatus the previous status
   * @param newStatus the new status
   * @param reason the reason for change
   */
  private void publishOrderStatusChangedEvent(Order order, String previousStatus, 
                                            String newStatus, String reason) {
    try {
      EventPublisherPort.OrderStatusChangedEvent event = 
          new EventPublisherPort.OrderStatusChangedEvent(
              order.getId().value(),
              previousStatus,
              newStatus,
              reason
          );

      eventPublisherPort.publishOrderStatusChanged(event);
      log.info("Successfully published OrderStatusChanged event for order: {}", order.getId());
    } catch (Exception e) {
      log.error("Failed to publish OrderStatusChanged event for order: {}. " 
               + "Event will be lost but order status was updated.", order.getId(), e);
      // Don't rethrow - order status change is more important than event publishing
    }
  }

  /**
   * Publishes order cancelled event.
   *
   * @param order the cancelled order
   */
  private void publishOrderCancelledEvent(Order order) {
    try {
      var itemEvents = order.getItems().stream()
          .map(item -> new EventPublisherPort.OrderItemEvent(
              item.getProductId().value(),
              item.getQuantity(),
              item.getUnitPrice().amount().toString()
          ))
          .toList();

      EventPublisherPort.OrderCancelledEvent event = 
          new EventPublisherPort.OrderCancelledEvent(
              order.getId().value(),
              order.getCustomerId().value(),
              order.getCancellationReason(),
              itemEvents
          );

      eventPublisherPort.publishOrderCancelled(event);
      log.info("Successfully published OrderCancelled event for order: {}", order.getId());
    } catch (Exception e) {
      log.error("Failed to publish OrderCancelled event for order: {}. " 
               + "Event will be lost but order was cancelled.", order.getId(), e);
      // Don't rethrow - order cancellation is more important than event publishing
    }
  }

  /**
   * Request for payment processed event.
   */
  public record PaymentProcessedRequest(
      String orderId,
      String paymentId,
      String amount,
      String paymentMethod,
      String transactionId
  ) {
  }

  /**
   * Request for payment failed event.
   */
  public record PaymentFailedRequest(
      String orderId,
      String paymentId,
      String reason,
      String errorCode
  ) {
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
   * Exception thrown when payment processing fails.
   */
  public static class PaymentProcessingException extends RuntimeException {
    public PaymentProcessingException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}