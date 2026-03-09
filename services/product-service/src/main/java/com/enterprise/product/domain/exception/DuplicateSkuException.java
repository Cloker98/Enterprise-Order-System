package com.enterprise.product.domain.exception;

/**
 * Exception thrown when attempting to create a product with duplicate SKU.
 *
 * <p>HTTP Status: 409 Conflict
 */
public class DuplicateSkuException extends DomainException {

  /**
   * Constructor with message.
   *
   * @param message the error message
   */
  public DuplicateSkuException(String message) {
    super(message);
  }
}
