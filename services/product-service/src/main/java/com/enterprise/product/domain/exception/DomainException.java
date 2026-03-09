package com.enterprise.product.domain.exception;

/**
 * Base exception for all domain-related errors.
 *
 * <p>All domain exceptions should extend this class.
 */
public abstract class DomainException extends RuntimeException {

  /**
   * Constructor with message.
   *
   * @param message the error message
   */
  protected DomainException(String message) {
    super(message);
  }

  /**
   * Constructor with message and cause.
   *
   * @param message the error message
   * @param cause the cause
   */
  protected DomainException(String message, Throwable cause) {
    super(message, cause);
  }
}
