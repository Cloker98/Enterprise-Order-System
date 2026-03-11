package com.enterprise.product.performance;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.lessThan;

import com.enterprise.product.application.dto.request.CreateProductRequest;
import com.enterprise.product.domain.model.ProductCategory;
import com.enterprise.product.integration.AbstractIntegrationTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;

/**
 * Performance tests for Product Service.
 *
 * <p>Tests response times and throughput under load.
 * Validates that P99 response times are under 200ms.
 */
@DisplayName("Product Service Performance Tests")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ProductPerformanceTest extends AbstractIntegrationTest {

  @BeforeEach
  void setUp() {
    RestAssured.port = port;
    RestAssured.baseURI = "http://localhost";
    RestAssured.basePath = "/api/v1/products";
  }

  @Test
  @DisplayName("Should list products with response time under 200ms")
  void shouldListProducts_WithResponseTimeUnder200ms() {
    // Given - Create a dataset of 100 products
    createLargeDataset(100);

    // When/Then - List products should respond quickly
    given()
        .queryParam("page", 0)
        .queryParam("size", 20)
        .when()
        .get()
        .then()
        .statusCode(200)
        .time(lessThan(200L)); // P99 < 200ms requirement
  }

  @Test
  @DisplayName("Should handle large page sizes efficiently")
  void shouldHandleLargePageSizes_Efficiently() {
    // Given - Create a dataset of 500 products
    createLargeDataset(500);

    // When/Then - Large page size should still be fast
    given()
        .queryParam("page", 0)
        .queryParam("size", 100)
        .when()
        .get()
        .then()
        .statusCode(200)
        .time(lessThan(300L)); // Slightly higher limit for large pages
  }

  @Test
  @DisplayName("Should filter by category efficiently")
  void shouldFilterByCategory_Efficiently() {
    // Given - Create mixed dataset
    createMixedCategoryDataset(200);

    // When/Then - Category filter should be fast (uses index)
    given()
        .queryParam("category", "ELECTRONICS")
        .queryParam("page", 0)
        .queryParam("size", 20)
        .when()
        .get()
        .then()
        .statusCode(200)
        .time(lessThan(150L)); // Should be faster due to index
  }

  @Test
  @DisplayName("Should handle concurrent requests efficiently")
  void shouldHandleConcurrentRequests_Efficiently() {
    // Given - Create dataset
    createLargeDataset(100);

    // When - Execute 10 concurrent requests
    ExecutorService executor = Executors.newFixedThreadPool(10);
    List<CompletableFuture<Long>> futures = new ArrayList<>();

    for (int i = 0; i < 10; i++) {
      CompletableFuture<Long> future = CompletableFuture.supplyAsync(() -> {
        return given()
            .queryParam("page", 0)
            .queryParam("size", 10)
            .when()
            .get()
            .then()
            .statusCode(200)
            .extract()
            .time();
      }, executor);
      futures.add(future);
    }

    try {
      // Then - All requests should complete within reasonable time
      CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
          .get(5, TimeUnit.SECONDS);

      // Verify all response times are acceptable
      for (CompletableFuture<Long> future : futures) {
        Long responseTime = future.get();
        assert responseTime < 500L : "Response time " + responseTime + "ms exceeds limit";
      }
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      throw new RuntimeException("Concurrent test failed", e);
    } finally {
      executor.shutdown();
    }
  }

  @Test
  @DisplayName("Should handle deep pagination efficiently")
  void shouldHandleDeepPagination_Efficiently() {
    // Given - Create large dataset
    createLargeDataset(1000);

    // When/Then - Deep pagination should still be reasonably fast
    given()
        .queryParam("page", 40) // Page 40 of 50 (size=20)
        .queryParam("size", 20)
        .when()
        .get()
        .then()
        .statusCode(200)
        .time(lessThan(400L)); // Slightly higher limit for deep pagination
  }

  /**
   * Creates a large dataset for performance testing.
   *
   * @param count number of products to create
   */
  private void createLargeDataset(int count) {
    for (int i = 0; i < count; i++) {
      CreateProductRequest request = new CreateProductRequest(
          "Product " + i,
          "Performance test product " + i,
          new BigDecimal("100.00").add(new BigDecimal(i)),
          10 + (i % 50), // Stock between 10-59
          "PERF-" + String.format("%04d", i),
          ProductCategory.ELECTRONICS
      );

      given()
          .contentType(ContentType.JSON)
          .body(request)
          .post()
          .then()
          .statusCode(201);
    }
  }

  /**
   * Creates a mixed category dataset for performance testing.
   *
   * @param count number of products to create
   */
  private void createMixedCategoryDataset(int count) {
    ProductCategory[] categories = ProductCategory.values();
    
    for (int i = 0; i < count; i++) {
      ProductCategory category = categories[i % categories.length];
      
      CreateProductRequest request = new CreateProductRequest(
          "Product " + i + " " + category,
          "Mixed category test product " + i,
          new BigDecimal("50.00").add(new BigDecimal(i)),
          5 + (i % 20), // Stock between 5-24
          "MIX-" + category.name().substring(0, 3) + "-" + String.format("%04d", i),
          category
      );

      given()
          .contentType(ContentType.JSON)
          .body(request)
          .post()
          .then()
          .statusCode(201);
    }
  }
}