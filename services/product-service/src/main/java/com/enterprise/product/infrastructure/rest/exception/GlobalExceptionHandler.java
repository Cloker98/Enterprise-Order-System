package com.enterprise.product.infrastructure.rest.exception;

import com.enterprise.product.domain.exception.DomainException;
import com.enterprise.product.domain.exception.DuplicateSkuException;
import com.enterprise.product.domain.exception.InsufficientStockException;
import com.enterprise.product.domain.exception.ProductNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler for REST API.
 *
 * <p>Maps domain exceptions to appropriate HTTP responses.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  /**
   * Handles ProductNotFoundException.
   *
   * @param ex the exception
   * @param request the HTTP request
   * @return 404 Not Found response
   */
  @ExceptionHandler(ProductNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleProductNotFound(
      ProductNotFoundException ex,
      HttpServletRequest request) {

    log.warn("Product not found: {}", ex.getMessage());

    ErrorResponse error = ErrorResponse.of(
        HttpStatus.NOT_FOUND.value(),
        HttpStatus.NOT_FOUND.getReasonPhrase(),
        ex.getMessage(),
        request.getRequestURI()
    );

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
  }

  /**
   * Handles InsufficientStockException.
   *
   * @param ex the exception
   * @param request the HTTP request
   * @return 409 Conflict response
   */
  @ExceptionHandler(InsufficientStockException.class)
  public ResponseEntity<ErrorResponse> handleInsufficientStock(
      InsufficientStockException ex,
      HttpServletRequest request) {

    log.warn("Insufficient stock: {}", ex.getMessage());

    ErrorResponse error = ErrorResponse.of(
        HttpStatus.CONFLICT.value(),
        HttpStatus.CONFLICT.getReasonPhrase(),
        ex.getMessage(),
        request.getRequestURI()
    );

    return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
  }

  /**
   * Handles DuplicateSkuException.
   *
   * @param ex the exception
   * @param request the HTTP request
   * @return 409 Conflict response
   */
  @ExceptionHandler(DuplicateSkuException.class)
  public ResponseEntity<ErrorResponse> handleDuplicateSku(
      DuplicateSkuException ex,
      HttpServletRequest request) {

    log.warn("Duplicate SKU: {}", ex.getMessage());

    ErrorResponse error = ErrorResponse.of(
        HttpStatus.CONFLICT.value(),
        HttpStatus.CONFLICT.getReasonPhrase(),
        ex.getMessage(),
        request.getRequestURI()
    );

    return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
  }

  /**
   * Handles validation errors (Jakarta Bean Validation).
   *
   * @param ex the exception
   * @param request the HTTP request
   * @return 400 Bad Request response with field errors
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationErrors(
      MethodArgumentNotValidException ex,
      HttpServletRequest request) {

    List<String> errors = ex.getBindingResult()
        .getFieldErrors()
        .stream()
        .map(error -> error.getField() + ": " + error.getDefaultMessage())
        .toList();

    log.warn("Validation failed: {}", errors);

    ErrorResponse error = ErrorResponse.of(
        HttpStatus.BAD_REQUEST.value(),
        HttpStatus.BAD_REQUEST.getReasonPhrase(),
        "Validation failed",
        errors,
        request.getRequestURI()
    );

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  /**
   * Handles IllegalArgumentException (domain validation errors).
   *
   * @param ex the exception
   * @param request the HTTP request
   * @return 400 Bad Request response
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgument(
      IllegalArgumentException ex,
      HttpServletRequest request) {

    log.warn("Illegal argument: {}", ex.getMessage());

    ErrorResponse error = ErrorResponse.of(
        HttpStatus.BAD_REQUEST.value(),
        HttpStatus.BAD_REQUEST.getReasonPhrase(),
        ex.getMessage(),
        request.getRequestURI()
    );

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  /**
   * Handles generic domain exceptions.
   *
   * @param ex the exception
   * @param request the HTTP request
   * @return 500 Internal Server Error response
   */
  @ExceptionHandler(DomainException.class)
  public ResponseEntity<ErrorResponse> handleDomainException(
      DomainException ex,
      HttpServletRequest request) {

    log.error("Domain exception: {}", ex.getMessage(), ex);

    ErrorResponse error = ErrorResponse.of(
        HttpStatus.INTERNAL_SERVER_ERROR.value(),
        HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
        "An internal error occurred",
        request.getRequestURI()
    );

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
  }

  /**
   * Handles all other unexpected exceptions.
   *
   * @param ex the exception
   * @param request the HTTP request
   * @return 500 Internal Server Error response
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGenericException(
      Exception ex,
      HttpServletRequest request) {

    log.error("Unexpected error: {}", ex.getMessage(), ex);

    ErrorResponse error = ErrorResponse.of(
        HttpStatus.INTERNAL_SERVER_ERROR.value(),
        HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
        "An unexpected error occurred",
        request.getRequestURI()
    );

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
  }
}
