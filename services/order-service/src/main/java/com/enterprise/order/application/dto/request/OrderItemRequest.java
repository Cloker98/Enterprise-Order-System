package com.enterprise.order.application.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for order item creation.
 *
 * @param productId the product identifier
 * @param quantity the quantity to order
 */
public record OrderItemRequest(
    @NotBlank(message = "Product ID cannot be blank")
    @Size(min = 3, max = 100, message = "Product ID must be between 3 and 100 characters")
    String productId,

    @NotNull(message = "Quantity cannot be null")
    @Min(value = 1, message = "Quantity must be at least 1")
    Integer quantity
) {
}