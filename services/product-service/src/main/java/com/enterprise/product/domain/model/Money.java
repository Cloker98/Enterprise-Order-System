package com.enterprise.product.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

/**
 * Value Object representing monetary value.
 *
 * <p>Ensures amount is positive and rounded to 2 decimal places.
 *
 * @param amount the monetary amount (must be > 0)
 * @param currency the currency (e.g., BRL, USD)
 */
public record Money(BigDecimal amount, Currency currency) {

  /**
   * Constructor with validation and normalization.
   *
   * @param amount the monetary amount
   * @param currency the currency
   * @throws IllegalArgumentException if amount <= 0 or currency is null
   */
  public Money {
    if (amount == null) {
      throw new IllegalArgumentException("Amount cannot be null");
    }
    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException(
          "Amount must be greater than 0, but was: " + amount
      );
    }
    if (currency == null) {
      throw new IllegalArgumentException("Currency cannot be null");
    }

    // Normalize to 2 decimal places
    amount = amount.setScale(2, RoundingMode.HALF_UP);
  }

  /**
   * Creates Money in BRL (Brazilian Real).
   *
   * @param amount the amount in BRL
   * @return Money instance with BRL currency
   */
  public static Money brl(BigDecimal amount) {
    return new Money(amount, Currency.getInstance("BRL"));
  }

  /**
   * Creates Money in BRL from double (convenience method).
   *
   * @param amount the amount as double
   * @return Money instance with BRL currency
   */
  public static Money brl(double amount) {
    return brl(BigDecimal.valueOf(amount));
  }

  /**
   * Adds another Money value.
   *
   * @param other the money to add
   * @return new Money with summed amount
   * @throws IllegalArgumentException if currencies don't match
   */
  public Money add(Money other) {
    if (!this.currency.equals(other.currency)) {
      throw new IllegalArgumentException(
          "Cannot add different currencies: " + this.currency + " and " + other.currency
      );
    }
    return new Money(this.amount.add(other.amount), this.currency);
  }

  /**
   * Subtracts another Money value.
   *
   * @param other the money to subtract
   * @return new Money with subtracted amount
   * @throws IllegalArgumentException if currencies don't match or result would be negative
   */
  public Money subtract(Money other) {
    if (!this.currency.equals(other.currency)) {
      throw new IllegalArgumentException(
          "Cannot subtract different currencies: " + this.currency + " and " + other.currency
      );
    }
    BigDecimal result = this.amount.subtract(other.amount);
    if (result.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Result of subtraction must be positive");
    }
    return new Money(result, this.currency);
  }

  /**
   * Multiplies by a quantity.
   *
   * @param multiplier the multiplier
   * @return new Money with multiplied amount
   */
  public Money multiply(int multiplier) {
    if (multiplier <= 0) {
      throw new IllegalArgumentException("Multiplier must be greater than 0");
    }
    return new Money(this.amount.multiply(BigDecimal.valueOf(multiplier)), this.currency);
  }
}
