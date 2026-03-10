package com.enterprise.product.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for ProductId value object.
 */
@DisplayName("ProductId Value Object Tests")
class ProductIdTest {

  @Test
  @DisplayName("constructor_WhenValidUuid_ShouldCreateProductId")
  void constructor_WhenValidUuid_ShouldCreateProductId() {
    // Given
    UUID uuid = UUID.randomUUID();

    // When
    ProductId productId = new ProductId(uuid);

    // Then
    assertNotNull(productId);
    assertEquals(uuid, productId.value());
  }

  @Test
  @DisplayName("constructor_WhenNullUuid_ShouldThrowException")
  void constructor_WhenNullUuid_ShouldThrowException() {
    assertThrows(IllegalArgumentException.class, () -> new ProductId(null));
  }

  @Test
  @DisplayName("from_WhenValidUuidString_ShouldCreateProductId")
  void from_WhenValidUuidString_ShouldCreateProductId() {
    // Given
    String uuidStr = "550e8400-e29b-41d4-a716-446655440000";

    // When
    ProductId productId = ProductId.from(uuidStr);

    // Then
    assertNotNull(productId);
    assertEquals(UUID.fromString(uuidStr), productId.value());
  }

  @Test
  @DisplayName("from_WhenNullString_ShouldThrowException")
  void from_WhenNullString_ShouldThrowException() {
    assertThrows(IllegalArgumentException.class, () -> ProductId.from(null));
  }

  @Test
  @DisplayName("from_WhenBlankString_ShouldThrowException")
  void from_WhenBlankString_ShouldThrowException() {
    assertThrows(IllegalArgumentException.class, () -> ProductId.from("   "));
  }

  @Test
  @DisplayName("from_WhenInvalidUuidFormat_ShouldThrowException")
  void from_WhenInvalidUuidFormat_ShouldThrowException() {
    assertThrows(IllegalArgumentException.class, () -> ProductId.from("not-a-uuid"));
  }

  @Test
  @DisplayName("generate_ShouldCreateNewRandomProductId")
  void generate_ShouldCreateNewRandomProductId() {
    // When
    ProductId productId = ProductId.generate();

    // Then
    assertNotNull(productId);
    assertNotNull(productId.value());
  }
}
