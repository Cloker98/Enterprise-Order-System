package com.enterprise.order.infrastructure.security;

import com.enterprise.order.application.usecase.GetOrderUseCase;
import com.enterprise.order.application.usecase.GetOrderUseCase.OrderNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Security service for order-related authorization checks.
 *
 * <p>This service provides methods to check if a user has permission
 * to perform specific operations on orders. It implements business
 * rules for order access control.
 */
@Service
public class OrderSecurityService {

  private static final Logger log = LoggerFactory.getLogger(OrderSecurityService.class);

  private final GetOrderUseCase getOrderUseCase;

  /**
   * Constructor for OrderSecurityService.
   *
   * @param getOrderUseCase the get order use case
   */
  public OrderSecurityService(GetOrderUseCase getOrderUseCase) {
    this.getOrderUseCase = getOrderUseCase;
  }

  /**
   * Checks if a user can access a specific order.
   * Users can only access their own orders unless they are admins.
   *
   * @param orderId the order ID
   * @param username the username of the requesting user
   * @return true if the user can access the order
   */
  public boolean canAccessOrder(String orderId, String username) {
    log.debug("Checking order access for user: {} on order: {}", username, orderId);
    
    try {
      var order = getOrderUseCase.execute(orderId);
      boolean canAccess = order.customerId().equals(username);
      
      log.debug("Order access check result for user: {} on order: {} = {}", 
                username, orderId, canAccess);
      return canAccess;
      
    } catch (OrderNotFoundException e) {
      log.debug("Order not found during access check: {}", orderId);
      return false; // If order doesn't exist, deny access
    } catch (Exception e) {
      log.error("Error checking order access for user: {} on order: {}", 
                username, orderId, e);
      return false; // Deny access on any error for security
    }
  }

  /**
   * Checks if a user can access orders for a specific customer.
   * Users can only access their own orders unless they are admins.
   *
   * @param customerId the customer ID
   * @param username the username of the requesting user
   * @return true if the user can access the customer's orders
   */
  public boolean canAccessCustomerOrders(String customerId, String username) {
    log.debug("Checking customer orders access for user: {} on customer: {}", 
              username, customerId);
    
    // Simple rule: users can only access their own orders
    // In a real system, this might involve more complex logic
    boolean canAccess = customerId.equals(username);
    
    log.debug("Customer orders access check result for user: {} on customer: {} = {}", 
              username, customerId, canAccess);
    return canAccess;
  }

  /**
   * Checks if a user can cancel a specific order.
   * Users can only cancel their own orders and only if the order is in a cancellable state.
   *
   * @param orderId the order ID
   * @param username the username of the requesting user
   * @return true if the user can cancel the order
   */
  public boolean canCancelOrder(String orderId, String username) {
    log.debug("Checking order cancellation permission for user: {} on order: {}", 
              username, orderId);
    
    try {
      var order = getOrderUseCase.execute(orderId);
      
      // Check if user owns the order
      if (!order.customerId().equals(username)) {
        log.debug("User {} does not own order {}", username, orderId);
        return false;
      }
      
      // Check if order can be cancelled (business rule)
      // This is a simplified check - in reality, you might need to check the actual domain object
      boolean canCancel = "PENDING".equals(order.status()) || "CONFIRMED".equals(order.status());
      
      log.debug("Order cancellation check result for user: {} on order: {} = {}", 
                username, orderId, canCancel);
      return canCancel;
      
    } catch (OrderNotFoundException e) {
      log.debug("Order not found during cancellation check: {}", orderId);
      return false; // If order doesn't exist, deny cancellation
    } catch (Exception e) {
      log.error("Error checking order cancellation permission for user: {} on order: {}", 
                username, orderId, e);
      return false; // Deny cancellation on any error for security
    }
  }

  /**
   * Checks if a user can create orders for a specific customer.
   * Users can only create orders for themselves unless they are admins.
   *
   * @param customerId the customer ID
   * @param username the username of the requesting user
   * @return true if the user can create orders for the customer
   */
  public boolean canCreateOrderForCustomer(String customerId, String username) {
    log.debug("Checking order creation permission for user: {} for customer: {}", 
              username, customerId);
    
    // Simple rule: users can only create orders for themselves
    boolean canCreate = customerId.equals(username);
    
    log.debug("Order creation check result for user: {} for customer: {} = {}", 
              username, customerId, canCreate);
    return canCreate;
  }
}