package com.enterprise.order.infrastructure.rest.exception;

import com.enterprise.order.application.port.ProductServicePort;
import com.enterprise.order.application.usecase.CancelOrderUseCase;
import com.enterprise.order.application.usecase.CreateOrderUseCase;
import com.enterprise.order.application.usecase.GetOrderUseCase;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

/**
 * Global exception handler for Order REST API.
 *
 * <p>Handles exceptions thrown by order-related operations and
 * converts them to appropriate HTTP responses with structured
 * error information.
 */
@RestControllerAdvice
public class OrderExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(OrderExceptionHandler.class);

  /**
   * Handles validation errors from request body validation.
   *
   * @param ex the validation exception
   * @param request the web request
   * @return error response with validation details
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(
      MethodArgumentNotValidException ex, WebRequest request) {
    
    log.debug("Validation error: {}", ex.getMessage());
    
    List<String> errors = ex.getBindingResult()
        .getFieldErrors()
        .stream()
        .map(this::formatFieldError)
        .toList();
    
    ErrorResponse errorResponse = ErrorResponse.builder()
        .timestamp(LocalDateTime.now())
        .status(HttpStatus.BAD_REQUEST.value())
        .error("Validation Failed")
        .message("Request validation failed")
        .details(errors)
        .path(request.getDescription(false).replace("uri=", ""))
        .build();
    
    return ResponseEntity.badRequest().body(errorResponse);
  }

  /**
   * Handles illegal argument exceptions.
   *
   * @param ex the illegal argument exception
   * @param request the web request
   * @return error response
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
      IllegalArgumentException ex, WebRequest request) {
    
    log.debug("Illegal argument: {}", ex.getMessage());
    
    ErrorResponse errorResponse = ErrorResponse.builder()
        .timestamp(LocalDateTime.now())
        .status(HttpStatus.BAD_REQUEST.value())
        .error("Bad Request")
        .message(ex.getMessage())
        .path(request.getDescription(false).replace("uri=", ""))
        .build();
    
    return ResponseEntity.badRequest().body(errorResponse);
  }

  /**
   * Handles order not found exceptions.
   *
   * @param ex the order not found exception
   * @param request the web request
   * @return error response
   */
  @ExceptionHandler({
      GetOrderUseCase.OrderNotFoundException.class,
      CancelOrderUseCase.OrderNotFoundException.class
  })
  public ResponseEntity<ErrorResponse> handleOrderNotFoundException(
      RuntimeException ex, WebRequest request) {
    
    log.debug("Order not found: {}", ex.getMessage());
    
    ErrorResponse.builder()
        .timestamp(LocalDateTime.now())
        .status(HttpStatus.NOT_FOUND.value())
        .error("Order Not Found")
        .message(ex.getMessage())
        .path(request.getDescription(false).replace("uri=", ""))
        .build();
    
    return ResponseEntity.notFound().build();
  }

  /**
   * Handles product service exceptions.
   *
   * @param ex the product service exception
   * @param request the web request
   * @return error response
   */
  @ExceptionHandler(ProductServicePort.ProductNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleProductNotFoundException(
      ProductServicePort.ProductNotFoundException ex, WebRequest request) {
    
    log.debug("Product not found: {}", ex.getMessage());
    
    ErrorResponse.builder()
        .timestamp(LocalDateTime.now())
        .status(HttpStatus.NOT_FOUND.value())
        .error("Product Not Found")
        .message(ex.getMessage())
        .path(request.getDescription(false).replace("uri=", ""))
        .build();
    
    return ResponseEntity.notFound().build();
  }

  /**
   * Handles insufficient stock exceptions.
   *
   * @param ex the insufficient stock exception
   * @param request the web request
   * @return error response
   */
  @ExceptionHandler(ProductServicePort.InsufficientStockException.class)
  public ResponseEntity<ErrorResponse> handleInsufficientStockException(
      ProductServicePort.InsufficientStockException ex, WebRequest request) {
    
    log.debug("Insufficient stock: {}", ex.getMessage());
    
    ErrorResponse errorResponse = ErrorResponse.builder()
        .timestamp(LocalDateTime.now())
        .status(HttpStatus.CONFLICT.value())
        .error("Insufficient Stock")
        .message(ex.getMessage())
        .path(request.getDescription(false).replace("uri=", ""))
        .build();
    
    return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
  }

  /**
   * Handles product service unavailable exceptions.
   *
   * @param ex the product service unavailable exception
   * @param request the web request
   * @return error response
   */
  @ExceptionHandler(ProductServicePort.ProductServiceUnavailableException.class)
  public ResponseEntity<ErrorResponse> handleProductServiceUnavailableException(
      ProductServicePort.ProductServiceUnavailableException ex, WebRequest request) {
    
    log.error("Product service unavailable: {}", ex.getMessage());
    
    ErrorResponse errorResponse = ErrorResponse.builder()
        .timestamp(LocalDateTime.now())
        .status(HttpStatus.SERVICE_UNAVAILABLE.value())
        .error("Service Unavailable")
        .message("Product service is temporarily unavailable. Please try again later.")
        .path(request.getDescription(false).replace("uri=", ""))
        .build();
    
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
  }

  /**
   * Handles invalid order state exceptions.
   *
   * @param ex the invalid order state exception
   * @param request the web request
   * @return error response
   */
  @ExceptionHandler(CancelOrderUseCase.InvalidOrderStateException.class)
  public ResponseEntity<ErrorResponse> handleInvalidOrderStateException(
      CancelOrderUseCase.InvalidOrderStateException ex, WebRequest request) {
    
    log.debug("Invalid order state: {}", ex.getMessage());
    
    ErrorResponse errorResponse = ErrorResponse.builder()
        .timestamp(LocalDateTime.now())
        .status(HttpStatus.BAD_REQUEST.value())
        .error("Invalid Order State")
        .message(ex.getMessage())
        .path(request.getDescription(false).replace("uri=", ""))
        .build();
    
    return ResponseEntity.badRequest().body(errorResponse);
  }

  /**
   * Handles access denied exceptions.
   *
   * @param ex the access denied exception
   * @param request the web request
   * @return error response
   */
  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ErrorResponse> handleAccessDeniedException(
      AccessDeniedException ex, WebRequest request) {
    
    log.debug("Access denied: {}", ex.getMessage());
    
    ErrorResponse errorResponse = ErrorResponse.builder()
        .timestamp(LocalDateTime.now())
        .status(HttpStatus.FORBIDDEN.value())
        .error("Access Denied")
        .message("You don't have permission to access this resource")
        .path(request.getDescription(false).replace("uri=", ""))
        .build();
    
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
  }

  /**
   * Handles order creation exceptions.
   *
   * @param ex the order creation exception
   * @param request the web request
   * @return error response
   */
  @ExceptionHandler(CreateOrderUseCase.OrderCreationException.class)
  public ResponseEntity<ErrorResponse> handleOrderCreationException(
      CreateOrderUseCase.OrderCreationException ex, WebRequest request) {
    
    log.error("Order creation failed: {}", ex.getMessage(), ex);
    
    ErrorResponse errorResponse = ErrorResponse.builder()
        .timestamp(LocalDateTime.now())
        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
        .error("Order Creation Failed")
        .message("Failed to create order. Please try again later.")
        .path(request.getDescription(false).replace("uri=", ""))
        .build();
    
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
  }

  /**
   * Handles stock compensation exceptions.
   *
   * @param ex the stock compensation exception
   * @param request the web request
   * @return error response
   */
  @ExceptionHandler(CancelOrderUseCase.StockCompensationException.class)
  public ResponseEntity<ErrorResponse> handleStockCompensationException(
      CancelOrderUseCase.StockCompensationException ex, WebRequest request) {
    
    log.error("Stock compensation failed: {}", ex.getMessage(), ex);
    
    ErrorResponse errorResponse = ErrorResponse.builder()
        .timestamp(LocalDateTime.now())
        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
        .error("Stock Compensation Failed")
        .message("Failed to compensate stock during order cancellation. "
                + "Manual intervention required.")
        .path(request.getDescription(false).replace("uri=", ""))
        .build();
    
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
  }

  /**
   * Handles event publishing exceptions.
   *
   * @param ex the event publishing exception
   * @param request the web request
   * @return error response
   */
  @ExceptionHandler({
      CreateOrderUseCase.EventPublishingException.class,
      CancelOrderUseCase.EventPublishingException.class
  })
  public ResponseEntity<ErrorResponse> handleEventPublishingException(
      RuntimeException ex, WebRequest request) {
    
    log.error("Event publishing failed: {}", ex.getMessage(), ex);
    
    ErrorResponse errorResponse = ErrorResponse.builder()
        .timestamp(LocalDateTime.now())
        .status(HttpStatus.ACCEPTED.value())
        .error("Event Publishing Failed")
        .message("Operation completed but event notification failed. "
                + "Some downstream services may not be updated.")
        .path(request.getDescription(false).replace("uri=", ""))
        .build();
    
    return ResponseEntity.status(HttpStatus.ACCEPTED).body(errorResponse);
  }

  /**
   * Handles all other unexpected exceptions.
   *
   * @param ex the unexpected exception
   * @param request the web request
   * @return error response
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGenericException(
      Exception ex, WebRequest request) {
    
    log.error("Unexpected error: {}", ex.getMessage(), ex);
    
    ErrorResponse errorResponse = ErrorResponse.builder()
        .timestamp(LocalDateTime.now())
        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
        .error("Internal Server Error")
        .message("An unexpected error occurred. Please try again later.")
        .path(request.getDescription(false).replace("uri=", ""))
        .build();
    
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
  }

  /**
   * Formats a field error for display.
   *
   * @param fieldError the field error
   * @return formatted error message
   */
  private String formatFieldError(FieldError fieldError) {
    return String.format("Field '%s': %s (rejected value: %s)", 
                        fieldError.getField(), 
                        fieldError.getDefaultMessage(), 
                        fieldError.getRejectedValue());
  }
}