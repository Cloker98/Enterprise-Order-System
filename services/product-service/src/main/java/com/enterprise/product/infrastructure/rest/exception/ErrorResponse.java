package com.enterprise.product.infrastructure.rest.exception;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Standard error response DTO for API errors.
 *
 * @param timestamp when the error occurred
 * @param status HTTP status code
 * @param error HTTP status reason phrase
 * @param message error message
 * @param errors list of detailed errors (for validation failures)
 * @param path request path
 */
public record ErrorResponse(
    LocalDateTime timestamp,
    int status,
    String error,
    String message,
    List<String> errors,
    String path
) {

  /**
   * Creates simple error response without detailed errors.
   *
   * @param status HTTP status code
   * @param error HTTP status reason
   * @param message error message
   * @param path request path
   * @return ErrorResponse
   */
  public static ErrorResponse of(int status, String error, String message, String path) {
    return new ErrorResponse(
        LocalDateTime.now(),
        status,
        error,
        message,
        null,
        path
    );
  }

  /**
   * Creates error response with detailed validation errors.
   *
   * @param status HTTP status code
   * @param error HTTP status reason
   * @param message main error message
   * @param errors list of detailed errors
   * @param path request path
   * @return ErrorResponse
   */
  public static ErrorResponse of(
      int status,
      String error,
      String message,
      List<String> errors,
      String path) {

    return new ErrorResponse(
        LocalDateTime.now(),
        status,
        error,
        message,
        errors,
        path
    );
  }
}
