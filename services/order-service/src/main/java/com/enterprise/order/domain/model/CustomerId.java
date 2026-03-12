package com.enterprise.order.domain.model;

/**
 * Value Object representing a Customer identifier.
 *
 * <p>Ensures the ID is valid with proper length constraints.
 *
 * @param value the customer ID string
 */
public record CustomerId(String value) {

  private static final int MIN_LENGTH = 3;
  private static final int MAX_LENGTH = 100;

  /**
   * Constructor with validation.
   *
   * @param value the customer ID string
   * @throws IllegalArgumentException if value is null, empty, or invalid length
   */
  public CustomerId {
    if (value == null || value.trim().isEmpty()) {
      throw new IllegalArgumentException("CustomerId cannot be null or empty");
    }

    String trimmedValue = value.trim();
    if (trimmedValue.length() < MIN_LENGTH || trimmedValue.length() > MAX_LENGTH) {
      throw new IllegalArgumentException(
          "CustomerId must be between " + MIN_LENGTH + " and " + MAX_LENGTH + " characters"
      );
    }

    // Use trimmed value
    value = trimmedValue;
  }

  /**
   * Creates CustomerId from string value.
   *
   * @param value the customer ID string
   * @return CustomerId instance
   * @throws IllegalArgumentException if value is invalid
   */
  public static CustomerId from(String value) {
    return new CustomerId(value);
  }

  @Override
  public String toString() {
    return value;
  }
}