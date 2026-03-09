package com.enterprise.product.domain.model;

import java.util.UUID;

/**
 * Value Object representing a Product's unique identifier.
 *
 * <p>Immutable UUID-based identifier.
 *
 * @param value the UUID value
 */
public record ProductId(UUID value) {

  /**
   * Constructor with validation.
   *
   * @param value the UUID value (must not be null)
   * @throws IllegalArgumentException if value is null
   */
  public ProductId {
    if (value == null) {
      throw new IllegalArgumentException("ProductId cannot be null");
    }
  }

  /**
   * Creates a ProductId from a string representation of UUID.
   *
   * @param value the UUID string
   * @return ProductId instance
   * @throws IllegalArgumentException if string is invalid UUID format
   */
  public static ProductId from(String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("ProductId string cannot be null or blank");
    }
    try {
      return new ProductId(UUID.fromString(value));
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid UUID format: " + value, e);
    }
  }

  /**
   * Generates a new random ProductId.
   *
   * @return new ProductId with random UUID
   */
  public static ProductId generate() {
    return new ProductId(UUID.randomUUID());
  }
}
