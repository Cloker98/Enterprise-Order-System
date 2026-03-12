package com.enterprise.order.domain.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Snapshot of Order state for reconstitution (Memento pattern).
 *
 * <p>Used to recreate Order aggregates from persistence layer.
 *
 * @param id the order identifier
 * @param customerId the customer identifier
 * @param items the list of order items
 * @param totalAmount the total order amount
 * @param status the order status
 * @param cancellationReason the reason for cancellation (if applicable)
 * @param createdAt the creation timestamp
 * @param updatedAt the last update timestamp
 */
public record OrderSnapshot(
    OrderId id,
    CustomerId customerId,
    List<OrderItem> items,
    Money totalAmount,
    OrderStatus status,
    String cancellationReason,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}