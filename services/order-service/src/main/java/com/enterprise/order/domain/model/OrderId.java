package com.enterprise.order.domain.model;

import java.util.UUID;

/**
 * Value Object representing an Order identifier.
 *
 * <p>Ensures the ID is a valid UUID format and provides factory methods
 * for generation and creation from existing values.
 *
 * @param value the UUID string value
 */
public record OrderId(String value) {

  /**
   * Constructor with validation.
   *
   * @param value the UUID string
   * @throws IllegalArgumentException if value is null, empty, or invalid UUID format
   */
  public OrderId {
    if (value == null || value.trim().isEmpty()) {
      throw new IllegalArgumentException("OrderId cannot be null or empty");
    }

    // Validate UUID format
    try {
      UUID.fromString(value);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid UUID format: " + value);
    }
  }

  /**
   * Generates a new random OrderId.
   *
   * @return new OrderId with random UUID
   */
  public static OrderId generate() {
    return new OrderId(UUID.randomUUID().toString());
  }

  /**
   * Creates OrderId from existing string value.
   *
   * @param value the UUID string
   * @return OrderId instance
   * @throws IllegalArgumentException if value is invalid
   */
  public static OrderId from(String value) {
    return new OrderId(value);
  }

  @Override
  public String toString() {
    return value;
  }
}