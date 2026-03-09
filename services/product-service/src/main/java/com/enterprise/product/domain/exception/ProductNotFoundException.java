package com.enterprise.product.domain.exception;

/**
 * Exception thrown when a product is not found.
 *
 * <p>HTTP Status: 404 Not Found
 */
public class ProductNotFoundException extends DomainException {

  /**
   * Constructor with message.
   *
   * @param message the error message
   */
  public ProductNotFoundException(String message) {
    super(message);
  }
}
