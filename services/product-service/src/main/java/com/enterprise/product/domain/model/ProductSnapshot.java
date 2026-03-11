package com.enterprise.product.domain.model;

import java.time.LocalDateTime;

/**
 * Creates a ProductSnapshot.
 *
 * @param id product ID
 * @param name product name
 * @param description product description
 * @param price product price (using Money value object for consistency)
 * @param stockQuantity stock quantity
 * @param sku stock keeping unit
 * @param category product category
 * @param status product status
 * @param createdAt creation timestamp
 * @param updatedAt last update timestamp
 */
public record ProductSnapshot(
    ProductId id,
    String name,
    String description,
    Money price,
    int stockQuantity,
    String sku,
    ProductCategory category,
    ProductStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

}