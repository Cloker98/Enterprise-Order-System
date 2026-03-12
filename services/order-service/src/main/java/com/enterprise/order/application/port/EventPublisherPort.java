package com.enterprise.order.application.port;

import java.util.List;

/**
 * Port for event publishing.
 *
 * <p>Defines the contract for publishing domain events
 * to external systems via messaging.
 */
public interface EventPublisherPort {

  /**
   * Publishes an OrderCreated event.
   *
   * @param event the order created event
   */
  void publishOrderCreated(OrderCreatedEvent event);

  /**
   * Publishes an OrderStatusChanged event.
   *
   * @param event the order status changed event
   */
  void publishOrderStatusChanged(OrderStatusChangedEvent event);

  /**
   * Publishes an OrderCancelled event.
   *
   * @param event the order cancelled event
   */
  void publishOrderCancelled(OrderCancelledEvent event);

  /**
   * Order created event.
   *
   * @param orderId the order ID
   * @param customerId the customer ID
   * @param totalAmount the total amount
   * @param items the order items
   */
  record OrderCreatedEvent(
      String orderId,
      String customerId,
      String totalAmount,
      List<OrderItemEvent> items
  ) {
  }

  /**
   * Order status changed event.
   *
   * @param orderId the order ID
   * @param previousStatus the previous status
   * @param newStatus the new status
   * @param reason the reason for change
   */
  record OrderStatusChangedEvent(
      String orderId,
      String previousStatus,
      String newStatus,
      String reason
  ) {
  }

  /**
   * Order cancelled event.
   *
   * @param orderId the order ID
   * @param customerId the customer ID
   * @param reason the cancellation reason
   * @param items the order items for stock compensation
   */
  record OrderCancelledEvent(
      String orderId,
      String customerId,
      String reason,
      List<OrderItemEvent> items
  ) {
  }

  /**
   * Order item event.
   *
   * @param productId the product ID
   * @param quantity the quantity
   * @param unitPrice the unit price
   */
  record OrderItemEvent(
      String productId,
      Integer quantity,
      String unitPrice
  ) {
  }
}