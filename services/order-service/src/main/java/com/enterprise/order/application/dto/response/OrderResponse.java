package com.enterprise.order.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for order.
 *
 * @param id the order identifier
 * @param customerId the customer identifier
 * @param items the list of order items
 * @param totalAmount the total order amount
 * @param status the order status
 * @param cancellationReason the cancellation reason (if applicable)
 * @param createdAt the creation timestamp
 * @param updatedAt the last update timestamp
 */
public record OrderResponse(
    String id,
    String customerId,
    List<OrderItemResponse> items,
    BigDecimal totalAmount,
    String status,
    String cancellationReason,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}