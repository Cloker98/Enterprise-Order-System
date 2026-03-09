package com.enterprise.product.infrastructure.rest.controller;

import com.enterprise.product.application.dto.request.CreateProductRequest;
import com.enterprise.product.application.dto.request.StockOperationRequest;
import com.enterprise.product.application.dto.request.UpdateProductRequest;
import com.enterprise.product.application.dto.response.ProductResponse;
import com.enterprise.product.application.usecase.CreateProductUseCase;
import com.enterprise.product.application.usecase.DecreaseStockUseCase;
import com.enterprise.product.application.usecase.DeleteProductUseCase;
import com.enterprise.product.application.usecase.GetProductUseCase;
import com.enterprise.product.application.usecase.UpdateProductUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for Product management.
 *
 * <p>Base path: /api/v1/products
 *
 * <p>Endpoints:
 * <ul>
 *   <li>POST / - Create product</li>
 *   <li>GET /{id} - Get product by ID</li>
 *   <li>PUT /{id} - Update product</li>
 *   <li>DELETE /{id} - Delete product (soft delete)</li>
 *   <li>POST /{id}/decrease-stock - Decrease stock</li>
 *   <li>POST /{id}/increase-stock - Increase stock</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Products", description = "Product management APIs")
public class ProductController {

  private final CreateProductUseCase createProductUseCase;
  private final GetProductUseCase getProductUseCase;
  private final UpdateProductUseCase updateProductUseCase;
  private final DeleteProductUseCase deleteProductUseCase;
  private final DecreaseStockUseCase decreaseStockUseCase;

  /**
   * Creates a new product.
   *
   * @param request the create product request
   * @return 201 Created with product data
   */
  @PostMapping
  @Operation(summary = "Create a new product", description = "Creates a new product in the catalog")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Product created successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid input data"),
      @ApiResponse(responseCode = "409", description = "SKU already exists")
  })
  public ResponseEntity<ProductResponse> create(@Valid @RequestBody CreateProductRequest request) {
    log.info("REST: Creating product with SKU: {}", request.sku());

    ProductResponse response = createProductUseCase.execute(request);

    log.info("REST: Product created with id: {}", response.id());

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * Retrieves a product by ID.
   *
   * @param id the product ID
   * @return 200 OK with product data
   */
  @GetMapping("/{id}")
  @Operation(summary = "Get product by ID", description = "Retrieves product details by ID")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Product found"),
      @ApiResponse(responseCode = "404", description = "Product not found")
  })
  public ResponseEntity<ProductResponse> getById(@PathVariable String id) {
    log.debug("REST: Getting product: {}", id);

    ProductResponse response = getProductUseCase.execute(id);

    return ResponseEntity.ok(response);
  }

  /**
   * Updates an existing product.
   *
   * @param id the product ID
   * @param request the update request
   * @return 200 OK with updated product data
   */
  @PutMapping("/{id}")
  @Operation(summary = "Update product", description = "Updates an existing product")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Product updated successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid input data"),
      @ApiResponse(responseCode = "404", description = "Product not found")
  })
  public ResponseEntity<ProductResponse> update(
      @PathVariable String id,
      @Valid @RequestBody UpdateProductRequest request) {

    log.info("REST: Updating product: {}", id);

    ProductResponse response = updateProductUseCase.execute(id, request);

    log.info("REST: Product updated: {}", id);

    return ResponseEntity.ok(response);
  }

  /**
   * Deletes a product (soft delete).
   *
   * @param id the product ID
   */
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(
      summary = "Delete product",
      description = "Soft deletes a product (sets status to INACTIVE)"
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Product deleted successfully"),
      @ApiResponse(responseCode = "404", description = "Product not found")
  })
  public void delete(@PathVariable String id) {
    log.info("REST: Deleting product: {}", id);

    deleteProductUseCase.execute(id);

    log.info("REST: Product deleted: {}", id);
  }

  /**
   * Decreases product stock.
   *
   * @param id the product ID
   * @param request the stock operation request
   * @return 200 OK with updated product
   */
  @PostMapping("/{id}/decrease-stock")
  @Operation(
      summary = "Decrease product stock",
      description = "Decreases stock quantity (e.g., when order is placed)"
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Stock decreased successfully"),
      @ApiResponse(responseCode = "404", description = "Product not found"),
      @ApiResponse(responseCode = "409", description = "Insufficient stock")
  })
  public ResponseEntity<ProductResponse> decreaseStock(
      @PathVariable String id,
      @Valid @RequestBody StockOperationRequest request) {

    log.info("REST: Decreasing stock for product: {} by quantity: {}", id, request.quantity());

    ProductResponse response = decreaseStockUseCase.execute(id, request);

    log.info("REST: Stock decreased for product: {}, new stock: {}",
        id, response.stockQuantity());

    return ResponseEntity.ok(response);
  }
}
