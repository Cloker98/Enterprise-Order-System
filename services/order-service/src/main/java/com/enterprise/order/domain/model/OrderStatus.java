package com.enterprise.order.domain.model;

/**
 * Enum representing the status of an order.
 *
 * <p>Defines valid order states and allowed transitions.
 */
public enum OrderStatus {
  PENDING,
  CONFIRMED,
  CANCELLED,
  DELIVERED;

  /**
   * Checks if transition to new status is allowed.
   *
   * @param newStatus the target status
   * @return true if transition is allowed
   */
  public boolean canTransitionTo(OrderStatus newStatus) {
    return switch (this) {
      case PENDING -> newStatus == CONFIRMED || newStatus == CANCELLED;
      case CONFIRMED -> newStatus == CANCELLED || newStatus == DELIVERED;
      case CANCELLED, DELIVERED -> false; // Final states
    };
  }
}