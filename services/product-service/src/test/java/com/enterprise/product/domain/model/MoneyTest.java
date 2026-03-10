package com.enterprise.product.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.Currency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for Money value object.
 */
@DisplayName("Money Value Object Tests")
class MoneyTest {

  @Test
  @DisplayName("brl_WhenValidAmount_ShouldCreateMoney")
  void brl_WhenValidAmount_ShouldCreateMoney() {
    // When
    Money money = Money.brl(new BigDecimal("100.00"));

    // Then
    assertNotNull(money);
    assertEquals(new BigDecimal("100.00"), money.amount());
    assertEquals(Currency.getInstance("BRL"), money.currency());
  }

  @Test
  @DisplayName("brl_WhenDecimalsMoreThanTwo_ShouldRound")
  void brl_WhenDecimalsMoreThanTwo_ShouldRound() {
    // When
    Money money = Money.brl(new BigDecimal("100.556"));

    // Then
    assertEquals(new BigDecimal("100.56"), money.amount());
  }

  @Test
  @DisplayName("constructor_WhenNullAmount_ShouldThrowException")
  void constructor_WhenNullAmount_ShouldThrowException() {
    assertThrows(IllegalArgumentException.class, () ->
        new Money(null, Currency.getInstance("BRL"))
    );
  }

  @Test
  @DisplayName("constructor_WhenZeroAmount_ShouldThrowException")
  void constructor_WhenZeroAmount_ShouldThrowException() {
    assertThrows(IllegalArgumentException.class, () ->
        new Money(BigDecimal.ZERO, Currency.getInstance("BRL"))
    );
  }

  @Test
  @DisplayName("constructor_WhenNegativeAmount_ShouldThrowException")
  void constructor_WhenNegativeAmount_ShouldThrowException() {
    assertThrows(IllegalArgumentException.class, () ->
        new Money(new BigDecimal("-10.00"), Currency.getInstance("BRL"))
    );
  }

  @Test
  @DisplayName("constructor_WhenNullCurrency_ShouldThrowException")
  void constructor_WhenNullCurrency_ShouldThrowException() {
    assertThrows(IllegalArgumentException.class, () ->
        new Money(new BigDecimal("100.00"), null)
    );
  }

  @Test
  @DisplayName("add_WhenSameCurrency_ShouldReturnSum")
  void add_WhenSameCurrency_ShouldReturnSum() {
    // Given
    Money money1 = Money.brl(new BigDecimal("100.00"));
    Money money2 = Money.brl(new BigDecimal("50.00"));

    // When
    Money result = money1.add(money2);

    // Then
    assertEquals(new BigDecimal("150.00"), result.amount());
    assertEquals(Currency.getInstance("BRL"), result.currency());
  }

  @Test
  @DisplayName("add_WhenDifferentCurrencies_ShouldThrowException")
  void add_WhenDifferentCurrencies_ShouldThrowException() {
    // Given
    Money brl = new Money(new BigDecimal("100.00"), Currency.getInstance("BRL"));
    Money usd = new Money(new BigDecimal("50.00"), Currency.getInstance("USD"));

    // When/Then
    assertThrows(IllegalArgumentException.class, () -> brl.add(usd));
  }

  @Test
  @DisplayName("subtract_WhenSameCurrencyAndResultPositive_ShouldReturnDifference")
  void subtract_WhenSameCurrencyAndResultPositive_ShouldReturnDifference() {
    // Given
    Money money1 = Money.brl(new BigDecimal("100.00"));
    Money money2 = Money.brl(new BigDecimal("30.00"));

    // When
    Money result = money1.subtract(money2);

    // Then
    assertEquals(new BigDecimal("70.00"), result.amount());
  }

  @Test
  @DisplayName("subtract_WhenResultWouldBeNegative_ShouldThrowException")
  void subtract_WhenResultWouldBeNegative_ShouldThrowException() {
    // Given
    Money money1 = Money.brl(new BigDecimal("50.00"));
    Money money2 = Money.brl(new BigDecimal("100.00"));

    // When/Then
    assertThrows(IllegalArgumentException.class, () -> money1.subtract(money2));
  }

  @Test
  @DisplayName("subtract_WhenDifferentCurrencies_ShouldThrowException")
  void subtract_WhenDifferentCurrencies_ShouldThrowException() {
    // Given
    Money brl = new Money(new BigDecimal("100.00"), Currency.getInstance("BRL"));
    Money usd = new Money(new BigDecimal("50.00"), Currency.getInstance("USD"));

    // When/Then
    assertThrows(IllegalArgumentException.class, () -> brl.subtract(usd));
  }

  @Test
  @DisplayName("multiply_WhenValidMultiplier_ShouldReturnProduct")
  void multiply_WhenValidMultiplier_ShouldReturnProduct() {
    // Given
    Money money = Money.brl(new BigDecimal("100.00"));

    // When
    Money result = money.multiply(3);

    // Then
    assertEquals(new BigDecimal("300.00"), result.amount());
    assertEquals(Currency.getInstance("BRL"), result.currency());
  }

  @Test
  @DisplayName("multiply_WhenZeroMultiplier_ShouldThrowException")
  void multiply_WhenZeroMultiplier_ShouldThrowException() {
    // Given
    Money money = Money.brl(new BigDecimal("100.00"));

    // When/Then
    assertThrows(IllegalArgumentException.class, () -> money.multiply(0));
  }

  @Test
  @DisplayName("multiply_WhenNegativeMultiplier_ShouldThrowException")
  void multiply_WhenNegativeMultiplier_ShouldThrowException() {
    // Given
    Money money = Money.brl(new BigDecimal("100.00"));

    // When/Then
    assertThrows(IllegalArgumentException.class, () -> money.multiply(-2));
  }
}
