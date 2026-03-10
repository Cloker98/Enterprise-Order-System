package com.enterprise.product.integration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import com.enterprise.product.application.dto.request.CreateProductRequest;
import com.enterprise.product.application.dto.request.StockOperationRequest;
import com.enterprise.product.application.dto.request.UpdateProductRequest;
import com.enterprise.product.domain.model.ProductCategory;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;

/**
 * Integration tests for Product Service.
 *
 * <p>Tests the entire stack: REST → Application → Domain → Infrastructure → Database.
 * Uses Testcontainers for real PostgreSQL and Redis instances.
 */
@DisplayName("Product Service Integration Tests")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ProductIT extends AbstractIntegrationTest {

  @BeforeEach
  void setUp() {
    RestAssured.port = port;
    RestAssured.baseURI = "http://localhost";
    RestAssured.basePath = "/api/v1/products";
  }

  @Test
  @DisplayName("Should create and retrieve product end-to-end")
  void shouldCreateAndRetrieveProduct_EndToEnd() {
    // Given - Create product request
    CreateProductRequest request = new CreateProductRequest(
        "Notebook Dell",
        "Notebook Dell Inspiron 15",
        new BigDecimal("3500.00"),
        10,
        "NB-DELL-INT-001",
        ProductCategory.ELECTRONICS
    );

    // When - Create product
    String productId = given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .post()
        .then()
        .statusCode(201)
        .body("id", notNullValue())
        .body("name", equalTo("Notebook Dell"))
        .body("price", equalTo(3500.00f))
        .body("stockQuantity", equalTo(10))
        .body("sku", equalTo("NB-DELL-INT-001"))
        .body("category", equalTo("ELECTRONICS"))
        .body("status", equalTo("ACTIVE"))
        .extract()
        .path("id");

    // Then - Retrieve product (cache miss)
    given()
        .when()
        .get("/{id}", productId)
        .then()
        .statusCode(200)
        .body("id", equalTo(productId))
        .body("name", equalTo("Notebook Dell"))
        .body("stockQuantity", equalTo(10));

    // And - Retrieve again (cache hit - should be faster)
    given()
        .when()
        .get("/{id}", productId)
        .then()
        .statusCode(200)
        .body("id", equalTo(productId))
        .body("name", equalTo("Notebook Dell"));
  }

  @Test
  @DisplayName("Should update product end-to-end")
  void shouldUpdateProduct_EndToEnd() {
    // Given - Create product first
    CreateProductRequest createRequest = new CreateProductRequest(
        "Mouse Logitech",
        "Mouse básico",
        new BigDecimal("150.00"),
        20,
        "MS-LOG-INT-002",
        ProductCategory.ELECTRONICS
    );

    String productId = given()
        .contentType(ContentType.JSON)
        .body(createRequest)
        .post()
        .then()
        .statusCode(201)
        .extract()
        .path("id");

    // When - Update product
    UpdateProductRequest updateRequest = new UpdateProductRequest(
        "Mouse Logitech MX Master 3",
        "Mouse ergonômico premium",
        new BigDecimal("350.00"),
        ProductCategory.ELECTRONICS
    );

    given()
        .contentType(ContentType.JSON)
        .body(updateRequest)
        .when()
        .put("/{id}", productId)
        .then()
        .statusCode(200)
        .body("id", equalTo(productId))
        .body("name", equalTo("Mouse Logitech MX Master 3"))
        .body("price", equalTo(350.00f))
        .body("stockQuantity", equalTo(20)); // Stock não muda no update

    // Then - Verify update persisted
    given()
        .when()
        .get("/{id}", productId)
        .then()
        .statusCode(200)
        .body("name", equalTo("Mouse Logitech MX Master 3"))
        .body("price", equalTo(350.00f));
  }

  @Test
  @DisplayName("Should soft delete product end-to-end")
  void shouldDeleteProduct_EndToEnd() {
    // Given - Create product
    CreateProductRequest request = new CreateProductRequest(
        "Teclado Mecânico",
        "Teclado RGB",
        new BigDecimal("450.00"),
        15,
        "KB-MEC-INT-003",
        ProductCategory.ELECTRONICS
    );

    String productId = given()
        .contentType(ContentType.JSON)
        .body(request)
        .post()
        .then()
        .statusCode(201)
        .extract()
        .path("id");

    // When - Delete product (soft delete)
    given()
        .when()
        .delete("/{id}", productId)
        .then()
        .statusCode(204);

    // Then - Product should still exist but with INACTIVE status
    given()
        .when()
        .get("/{id}", productId)
        .then()
        .statusCode(200)
        .body("id", equalTo(productId))
        .body("status", equalTo("INACTIVE"));
  }

  @Test
  @DisplayName("Should decrease stock successfully")
  void shouldDecreaseStock_EndToEnd() {
    // Given - Create product with stock
    CreateProductRequest request = new CreateProductRequest(
        "Monitor Samsung",
        "Monitor 24 pol",
        new BigDecimal("800.00"),
        50,
        "MON-SAM-INT-004",
        ProductCategory.ELECTRONICS
    );

    String productId = given()
        .contentType(ContentType.JSON)
        .body(request)
        .post()
        .then()
        .statusCode(201)
        .extract()
        .path("id");

    // When - Decrease stock
    StockOperationRequest stockRequest = new StockOperationRequest(10);

    given()
        .contentType(ContentType.JSON)
        .body(stockRequest)
        .when()
        .post("/{id}/decrease-stock", productId)
        .then()
        .statusCode(200)
        .body("stockQuantity", equalTo(40)); // 50 - 10 = 40

    // Then - Verify stock persisted
    given()
        .when()
        .get("/{id}", productId)
        .then()
        .statusCode(200)
        .body("stockQuantity", equalTo(40));
  }

  @Test
  @DisplayName("Should return 404 when product not found")
  void shouldReturn404_WhenProductNotFound() {
    given()
        .when()
        .get("/{id}", "00000000-0000-0000-0000-000000000000")
        .then()
        .statusCode(404)
        .body("status", equalTo(404))
        .body("error", equalTo("Not Found"))
        .body("message", equalTo("Product not found with id: 00000000-0000-0000-0000-000000000000"));
  }

  @Test
  @DisplayName("Should return 409 when duplicate SKU")
  void shouldReturn409_WhenDuplicateSku() {
    // Given - Create first product
    CreateProductRequest firstRequest = new CreateProductRequest(
        "Product A",
        "Description A",
        new BigDecimal("100.00"),
        10,
        "DUPLICATE-SKU-001",
        ProductCategory.ELECTRONICS
    );

    given()
        .contentType(ContentType.JSON)
        .body(firstRequest)
        .post()
        .then()
        .statusCode(201);

    // When/Then - Try to create product with same SKU
    CreateProductRequest duplicateRequest = new CreateProductRequest(
        "Product B",
        "Description B",
        new BigDecimal("200.00"),
        20,
        "DUPLICATE-SKU-001", // Same SKU!
        ProductCategory.ELECTRONICS
    );

    given()
        .contentType(ContentType.JSON)
        .body(duplicateRequest)
        .when()
        .post()
        .then()
        .statusCode(409)
        .body("status", equalTo(409))
        .body("error", equalTo("Conflict"))
        .body("message", equalTo("SKU already exists: DUPLICATE-SKU-001"));
  }

  @Test
  @DisplayName("Should return 409 when insufficient stock")
  void shouldReturn409_WhenInsufficientStock() {
    // Given - Create product with limited stock
    CreateProductRequest request = new CreateProductRequest(
        "Limited Stock Product",
        "Test product",
        new BigDecimal("50.00"),
        5, // Only 5 units
        "LIMITED-INT-005",
        ProductCategory.ELECTRONICS
    );

    String productId = given()
        .contentType(ContentType.JSON)
        .body(request)
        .post()
        .then()
        .statusCode(201)
        .extract()
        .path("id");

    // When/Then - Try to decrease more than available
    StockOperationRequest stockRequest = new StockOperationRequest(10); // Trying 10, only 5 available

    given()
        .contentType(ContentType.JSON)
        .body(stockRequest)
        .when()
        .post("/{id}/decrease-stock", productId)
        .then()
        .statusCode(409)
        .body("status", equalTo(409))
        .body("error", equalTo("Conflict"))
        .body("message", equalTo("Insufficient stock. Available: 5, Requested: 10"));
  }

  @Test
  @DisplayName("Should return 400 when invalid UUID format")
  void shouldReturn400_WhenInvalidUuid() {
    given()
        .when()
        .get("/{id}", "invalid-uuid-format")
        .then()
        .statusCode(400)
        .body("status", equalTo(400))
        .body("error", equalTo("Bad Request"))
        .body("message", equalTo("Invalid UUID format: invalid-uuid-format"));
  }
}
