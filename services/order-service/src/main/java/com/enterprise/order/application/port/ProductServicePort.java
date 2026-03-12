package com.enterprise.order.application.port;

import com.enterprise.order.domain.model.ProductId;
import java.math.BigDecimal;

/**
 * Port for Product Service integration.
 *
 * <p>Defines the contract for product-related operations
 * needed by the Order Service.
 */
public interface ProductServicePort {

  /**
   * Gets product information by ID.
   *
   * @param productId the product ID
   * @return the product information
   * @throws ProductNotFoundException if product is not found
   * @throws ProductServiceUnavailableException if service is unavailable
   */
  ProductInfo getProduct(ProductId productId);

  /**
   * Decreases product stock.
   *
   * @param productId the product ID
   * @param quantity the quantity to decrease
   * @throws ProductNotFoundException if product is not found
   * @throws InsufficientStockException if insufficient stock
   * @throws ProductServiceUnavailableException if service is unavailable
   */
  void decreaseStock(ProductId productId, int quantity);

  /**
   * Increases product stock (for compensation).
   *
   * @param productId the product ID
   * @param quantity the quantity to increase
   * @throws ProductNotFoundException if product is not found
   * @throws ProductServiceUnavailableException if service is unavailable
   */
  void increaseStock(ProductId productId, int quantity);

  /**
   * Product information DTO.
   *
   * @param id the product ID
   * @param name the product name
   * @param price the product price
   * @param stockQuantity the available stock quantity
   */
  record ProductInfo(
      String id,
      String name,
      BigDecimal price,
      Integer stockQuantity
  ) {
  }

  /**
   * Exception thrown when product is not found.
   */
  class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String message) {
      super(message);
    }
  }

  /**
   * Exception thrown when there's insufficient stock.
   */
  class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String message) {
      super(message);
    }
  }

  /**
   * Exception thrown when Product Service is unavailable.
   */
  class ProductServiceUnavailableException extends RuntimeException {
    public ProductServiceUnavailableException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}