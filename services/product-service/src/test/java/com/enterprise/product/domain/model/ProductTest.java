package com.enterprise.product.domain.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enterprise.product.domain.exception.InsufficientStockException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for Product aggregate.
 *
 * <p>Tests domain business logic and validations.
 */
@DisplayName("Product Domain Tests")
class ProductTest {

  @Test
  @DisplayName("create_WhenValidData_ShouldReturnActiveProduct")
  void create_WhenValidData_ShouldReturnActiveProduct() {
    // When
    Product product = Product.create(
        "Notebook Dell",
        "Notebook Dell Inspiron 15",
        new BigDecimal("3500.00"),
        10,
        "NB-DELL-001",
        ProductCategory.ELECTRONICS
    );

    // Then
    assertNotNull(product);
    assertNotNull(product.getId());
    assertEquals("Notebook Dell", product.getName());
    assertEquals("Notebook Dell Inspiron 15", product.getDescription());
    assertEquals(new BigDecimal("3500.00"), product.getPrice().amount());
    assertEquals(10, product.getStockQuantity());
    assertEquals("NB-DELL-001", product.getSku());
    assertEquals(ProductCategory.ELECTRONICS, product.getCategory());
    assertEquals(ProductStatus.ACTIVE, product.getStatus());
    assertNotNull(product.getCreatedAt());
    assertNotNull(product.getUpdatedAt());
  }

  @Test
  @DisplayName("create_WhenNullName_ShouldThrowException")
  void create_WhenNullName_ShouldThrowException() {
    assertThrows(IllegalArgumentException.class, () ->
        Product.create(
            null, // Null name
            "Description",
            new BigDecimal("100.00"),
            10,
            "SKU-001",
            ProductCategory.ELECTRONICS
        )
    );
  }

  @Test
  @DisplayName("create_WhenBlankName_ShouldThrowException")
  void create_WhenBlankName_ShouldThrowException() {
    assertThrows(IllegalArgumentException.class, () ->
        Product.create(
            "   ", // Blank name
            "Description",
            new BigDecimal("100.00"),
            10,
            "SKU-002",
            ProductCategory.ELECTRONICS
        )
    );
  }

  @Test
  @DisplayName("create_WhenNameTooLong_ShouldThrowException")
  void create_WhenNameTooLong_ShouldThrowException() {
    String longName = "A".repeat(201); // Max is 200

    assertThrows(IllegalArgumentException.class, () ->
        Product.create(
            longName,
            "Description",
            new BigDecimal("100.00"),
            10,
            "SKU-003",
            ProductCategory.ELECTRONICS
        )
    );
  }

  @Test
  @DisplayName("create_WhenNegativePrice_ShouldThrowException")
  void create_WhenNegativePrice_ShouldThrowException() {
    assertThrows(IllegalArgumentException.class, () ->
        Product.create(
            "Product",
            "Description",
            new BigDecimal("-10.00"), // Negative price
            10,
            "SKU-004",
            ProductCategory.ELECTRONICS
        )
    );
  }

  @Test
  @DisplayName("create_WhenZeroPrice_ShouldThrowException")
  void create_WhenZeroPrice_ShouldThrowException() {
    assertThrows(IllegalArgumentException.class, () ->
        Product.create(
            "Product",
            "Description",
            BigDecimal.ZERO, // Zero price
            10,
            "SKU-005",
            ProductCategory.ELECTRONICS
        )
    );
  }

  @Test
  @DisplayName("create_WhenNegativeStock_ShouldThrowException")
  void create_WhenNegativeStock_ShouldThrowException() {
    assertThrows(IllegalArgumentException.class, () ->
        Product.create(
            "Product",
            "Description",
            new BigDecimal("100.00"),
            -5, // Negative stock
            "SKU-006",
            ProductCategory.ELECTRONICS
        )
    );
  }

  @Test
  @DisplayName("create_WhenNullSku_ShouldThrowException")
  void create_WhenNullSku_ShouldThrowException() {
    assertThrows(IllegalArgumentException.class, () ->
        Product.create(
            "Product",
            "Description",
            new BigDecimal("100.00"),
            10,
            null, // Null SKU
            ProductCategory.ELECTRONICS
        )
    );
  }

  @Test
  @DisplayName("decreaseStock_WhenSufficientQuantity_ShouldDecreaseSuccessfully")
  void decreaseStock_WhenSufficientQuantity_ShouldDecreaseSuccessfully() {
    // Given
    Product product = Product.create(
        "Product",
        "Description",
        new BigDecimal("100.00"),
        50,
        "SKU-STOCK-001",
        ProductCategory.ELECTRONICS
    );

    // When
    product.decreaseStock(10);

    // Then
    assertEquals(40, product.getStockQuantity());
  }

  @Test
  @DisplayName("decreaseStock_WhenInsufficientQuantity_ShouldThrowException")
  void decreaseStock_WhenInsufficientQuantity_ShouldThrowException() {
    // Given
    Product product = Product.create(
        "Product",
        "Description",
        new BigDecimal("100.00"),
        5, // Only 5 units
        "SKU-STOCK-002",
        ProductCategory.ELECTRONICS
    );

    // When/Then - Trying to decrease 10 when only 5 available
    InsufficientStockException exception = assertThrows(
        InsufficientStockException.class,
        () -> product.decreaseStock(10)
    );

    assertEquals("Insufficient stock. Available: 5, Requested: 10", exception.getMessage());
  }

  @Test
  @DisplayName("decreaseStock_WhenZeroQuantity_ShouldThrowException")
  void decreaseStock_WhenZeroQuantity_ShouldThrowException() {
    // Given
    Product product = Product.create(
        "Product",
        "Description",
        new BigDecimal("100.00"),
        50,
        "SKU-STOCK-003",
        ProductCategory.ELECTRONICS
    );

    // When/Then
    assertThrows(IllegalArgumentException.class, () -> product.decreaseStock(0));
  }

  @Test
  @DisplayName("decreaseStock_WhenNegativeQuantity_ShouldThrowException")
  void decreaseStock_WhenNegativeQuantity_ShouldThrowException() {
    // Given
    Product product = Product.create(
        "Product",
        "Description",
        new BigDecimal("100.00"),
        50,
        "SKU-STOCK-004",
        ProductCategory.ELECTRONICS
    );

    // When/Then
    assertThrows(IllegalArgumentException.class, () -> product.decreaseStock(-5));
  }

  @Test
  @DisplayName("increaseStock_WhenValidQuantity_ShouldIncreaseSuccessfully")
  void increaseStock_WhenValidQuantity_ShouldIncreaseSuccessfully() {
    // Given
    Product product = Product.create(
        "Product",
        "Description",
        new BigDecimal("100.00"),
        20,
        "SKU-STOCK-005",
        ProductCategory.ELECTRONICS
    );

    // When
    product.increaseStock(10);

    // Then
    assertEquals(30, product.getStockQuantity());
  }

  @Test
  @DisplayName("increaseStock_WhenZeroQuantity_ShouldThrowException")
  void increaseStock_WhenZeroQuantity_ShouldThrowException() {
    // Given
    Product product = Product.create(
        "Product",
        "Description",
        new BigDecimal("100.00"),
        20,
        "SKU-STOCK-006",
        ProductCategory.ELECTRONICS
    );

    // When/Then
    assertThrows(IllegalArgumentException.class, () -> product.increaseStock(0));
  }

  @Test
  @DisplayName("update_WhenValidData_ShouldUpdateSuccessfully")
  void update_WhenValidData_ShouldUpdateSuccessfully() {
    // Given
    Product product = Product.create(
        "Original Name",
        "Original Description",
        new BigDecimal("100.00"),
        10,
        "SKU-UPDATE-001",
        ProductCategory.ELECTRONICS
    );

    LocalDateTime originalUpdatedAt = product.getUpdatedAt();

    // When
    product.update(
        "Updated Name",
        "Updated Description",
        Money.brl(new BigDecimal("200.00")),
        ProductCategory.BOOKS
    );

    // Then
    assertEquals("Updated Name", product.getName());
    assertEquals("Updated Description", product.getDescription());
    assertEquals(new BigDecimal("200.00"), product.getPrice().amount());
    assertEquals(ProductCategory.BOOKS, product.getCategory());
    assertEquals("SKU-UPDATE-001", product.getSku()); // SKU is immutable
    assertEquals(10, product.getStockQuantity()); // Stock not affected
  }

  @Test
  @DisplayName("deactivate_ShouldSetStatusToInactive")
  void deactivate_ShouldSetStatusToInactive() {
    // Given
    Product product = Product.create(
        "Product",
        "Description",
        new BigDecimal("100.00"),
        10,
        "SKU-STATUS-001",
        ProductCategory.ELECTRONICS
    );

    assertEquals(ProductStatus.ACTIVE, product.getStatus());

    // When
    product.deactivate();

    // Then
    assertEquals(ProductStatus.INACTIVE, product.getStatus());
  }

  @Test
  @DisplayName("reconstitute_WhenValidData_ShouldReconstituteProduct")
  void reconstitute_WhenValidData_ShouldReconstituteProduct() {
    // Given
    ProductId id = ProductId.generate();
    LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
    LocalDateTime updatedAt = LocalDateTime.now();

    // When
    Product product = Product.reconstitute(
        id,
        "Reconstituted Product",
        "Description",
        new BigDecimal("500.00"),
        25,
        "SKU-RECON-001",
        ProductCategory.ELECTRONICS,
        ProductStatus.INACTIVE,
        createdAt,
        updatedAt
    );

    // Then
    assertNotNull(product);
    assertEquals(id, product.getId());
    assertEquals("Reconstituted Product", product.getName());
    assertEquals(new BigDecimal("500.00"), product.getPrice().amount());
    assertEquals(25, product.getStockQuantity());
    assertEquals("SKU-RECON-001", product.getSku());
    assertEquals(ProductCategory.ELECTRONICS, product.getCategory());
    assertEquals(ProductStatus.INACTIVE, product.getStatus());
    assertEquals(createdAt, product.getCreatedAt());
    assertEquals(updatedAt, product.getUpdatedAt());
  }
}
