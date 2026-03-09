package com.enterprise.product.domain.exception;

/**
 * Exception thrown when attempting to decrease stock below zero.
 *
 * <p>HTTP Status: 409 Conflict
 */
public class InsufficientStockException extends DomainException {

  /**
   * Constructor with message.
   *
   * @param message the error message
   */
  public InsufficientStockException(String message) {
    super(message);
  }
}
