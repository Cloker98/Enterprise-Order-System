package com.enterprise.order.domain.model;

/**
 * Value Object representing a Product identifier.
 *
 * <p>Ensures the ID is valid with proper length constraints.
 *
 * @param value the product ID string
 */
public record ProductId(String value) {

  private static final int MIN_LENGTH = 3;
  private static final int MAX_LENGTH = 100;

  /**
   * Constructor with validation.
   *
   * @param value the product ID string
   * @throws IllegalArgumentException if value is null, empty, or invalid length
   */
  public ProductId {
    if (value == null || value.trim().isEmpty()) {
      throw new IllegalArgumentException("ProductId cannot be null or empty");
    }

    String trimmedValue = value.trim();
    if (trimmedValue.length() < MIN_LENGTH || trimmedValue.length() > MAX_LENGTH) {
      throw new IllegalArgumentException(
          "ProductId must be between " + MIN_LENGTH + " and " + MAX_LENGTH + " characters"
      );
    }

    // Use trimmed value
    value = trimmedValue;
  }

  /**
   * Creates ProductId from string value.
   *
   * @param value the product ID string
   * @return ProductId instance
   * @throws IllegalArgumentException if value is invalid
   */
  public static ProductId from(String value) {
    return new ProductId(value);
  }

  @Override
  public String toString() {
    return value;
  }
}