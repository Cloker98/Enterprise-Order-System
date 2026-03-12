package com.enterprise.order.application.dto.response;

import java.math.BigDecimal;

/**
 * Response DTO for order item.
 *
 * @param productId the product identifier
 * @param productName the product name
 * @param quantity the quantity ordered
 * @param unitPrice the unit price
 * @param totalPrice the total price for this item
 */
public record OrderItemResponse(
    String productId,
    String productName,
    Integer quantity,
    BigDecimal unitPrice,
    BigDecimal totalPrice
) {
}