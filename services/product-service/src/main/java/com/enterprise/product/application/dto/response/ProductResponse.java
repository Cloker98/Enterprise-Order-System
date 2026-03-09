package com.enterprise.product.application.dto.response;

import com.enterprise.product.domain.model.ProductCategory;
import com.enterprise.product.domain.model.ProductStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for Product API responses.
 *
 * @param id product ID
 * @param name product name
 * @param description product description
 * @param price product price
 * @param stockQuantity current stock quantity
 * @param sku stock keeping unit
 * @param category product category
 * @param status product status
 * @param createdAt creation timestamp
 * @param updatedAt last update timestamp
 */
public record ProductResponse(
    String id,
    String name,
    String description,
    BigDecimal price,
    Integer stockQuantity,
    String sku,
    ProductCategory category,
    ProductStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
