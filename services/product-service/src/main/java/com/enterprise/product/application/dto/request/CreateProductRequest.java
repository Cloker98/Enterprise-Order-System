package com.enterprise.product.application.dto.request;

import com.enterprise.product.domain.model.ProductCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * DTO for creating a new product.
 *
 * @param name product name (required, max 200 chars)
 * @param description product description (max 1000 chars)
 * @param price product price (required, > 0)
 * @param stockQuantity initial stock (required, >= 0)
 * @param sku stock keeping unit (required, unique, alphanumeric)
 * @param category product category (required)
 */
public record CreateProductRequest(

    @NotBlank(message = "Name is required")
    @Size(max = 200, message = "Name cannot exceed 200 characters")
    String name,

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    String description,

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    BigDecimal price,

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    Integer stockQuantity,

    @NotBlank(message = "SKU is required")
    @Size(max = 50, message = "SKU cannot exceed 50 characters")
    @Pattern(
        regexp = "^[a-zA-Z0-9-]+$",
        message = "SKU must be alphanumeric with optional hyphens"
    )
    String sku,

    @NotNull(message = "Category is required")
    ProductCategory category
) {
}
