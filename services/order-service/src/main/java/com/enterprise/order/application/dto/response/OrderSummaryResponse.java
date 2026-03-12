package com.enterprise.order.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for order summary information.
 * Used in paginated order listings to provide essential order information
 * without the full details of order items.
 *
 * @param id the order ID
 * @param customerId the customer ID
 * @param itemCount the number of items in the order
 * @param totalAmount the total amount of the order
 * @param status the order status
 * @param createdAt the order creation timestamp
 */
public record OrderSummaryResponse(
    String id,
    String customerId,
    int itemCount,
    BigDecimal totalAmount,
    String status,
    LocalDateTime createdAt
) {
}