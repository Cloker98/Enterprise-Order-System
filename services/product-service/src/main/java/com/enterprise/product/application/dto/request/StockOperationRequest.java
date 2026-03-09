package com.enterprise.product.application.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for stock operations (increase/decrease).
 *
 * @param quantity amount to increase or decrease (required, > 0)
 */
public record StockOperationRequest(

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    Integer quantity
) {
}
