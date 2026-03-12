package com.enterprise.order.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * Request DTO for order creation.
 *
 * @param customerId the customer identifier
 * @param items the list of items to order
 */
public record CreateOrderRequest(
    @NotBlank(message = "Customer ID cannot be blank")
    @Size(min = 3, max = 100, message = "Customer ID must be between 3 and 100 characters")
    String customerId,

    @NotEmpty(message = "Items list cannot be empty")
    @Valid
    List<OrderItemRequest> items
) {
}