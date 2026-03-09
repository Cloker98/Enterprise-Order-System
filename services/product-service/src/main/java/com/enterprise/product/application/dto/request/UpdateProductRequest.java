package com.enterprise.product.application.dto.request;

import com.enterprise.product.domain.model.ProductCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * DTO for updating an existing product.
 *
 * <p>Note: Cannot update id, sku, or createdAt (immutable).
 *
 * @param name product name (required, max 200 chars)
 * @param description product description (max 1000 chars)
 * @param price product price (required, > 0)
 * @param category product category (required)
 */
public record UpdateProductRequest(

    @NotBlank(message = "Name is required")
    @Size(max = 200, message = "Name cannot exceed 200 characters")
    String name,

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    String description,

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    BigDecimal price,

    @NotNull(message = "Category is required")
    ProductCategory category
) {
}
