package com.enterprise.order.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for cancelling an order.
 *
 * @param orderId the ID of the order to cancel
 * @param reason the reason for cancellation
 */
public record CancelOrderRequest(
    @NotBlank(message = "Order ID cannot be null or empty")
    String orderId,
    
    @NotBlank(message = "Cancellation reason cannot be null or empty")
    @Size(min = 5, max = 500, message = "Cancellation reason must be between 5 and 500 characters")
    String reason
) {
  
  /**
   * Creates a CancelOrderRequest with the specified order ID and reason.
   *
   * @param orderId the order ID
   * @param reason the cancellation reason
   * @return a new CancelOrderRequest
   */
  public static CancelOrderRequest of(String orderId, String reason) {
    return new CancelOrderRequest(orderId, reason);
  }
}