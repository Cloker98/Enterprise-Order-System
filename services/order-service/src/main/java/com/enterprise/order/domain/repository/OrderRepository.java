package com.enterprise.order.domain.repository;

import com.enterprise.order.domain.model.CustomerId;
import com.enterprise.order.domain.model.Order;
import com.enterprise.order.domain.model.OrderId;
import com.enterprise.order.domain.model.OrderStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Repository port for Order aggregate.
 *
 * <p>Defines the contract for order persistence operations.
 * This is a port in the hexagonal architecture.
 */
public interface OrderRepository {

  /**
   * Saves an order.
   *
   * @param order the order to save
   * @return the saved order
   */
  Order save(Order order);

  /**
   * Finds an order by its ID.
   *
   * @param orderId the order ID
   * @return the order if found
   */
  Optional<Order> findById(OrderId orderId);

  /**
   * Finds orders by customer ID.
   *
   * @param customerId the customer ID
   * @return list of orders for the customer
   */
  List<Order> findByCustomerId(CustomerId customerId);

  /**
   * Finds orders by customer ID with pagination (legacy method).
   *
   * @param customerId the customer ID
   * @param page the page number (0-based)
   * @param size the page size
   * @return list of orders for the customer
   */
  List<Order> findByCustomerId(CustomerId customerId, int page, int size);

  /**
   * Finds orders by customer ID with pagination.
   *
   * @param customerId the customer ID
   * @param pageable the pagination information
   * @return page of orders for the customer
   */
  Page<Order> findByCustomerId(CustomerId customerId, Pageable pageable);

  /**
   * Finds orders by customer ID with filters and pagination.
   *
   * @param customerId the customer ID
   * @param status the order status filter (optional)
   * @param startDate the start date filter (optional)
   * @param endDate the end date filter (optional)
   * @param pageable the pagination information
   * @return page of orders matching the criteria
   */
  Page<Order> findByCustomerIdWithFilters(CustomerId customerId, OrderStatus status,
                                         LocalDateTime startDate, LocalDateTime endDate,
                                         Pageable pageable);

  /**
   * Finds orders by customer ID and status.
   *
   * @param customerId the customer ID
   * @param status the order status
   * @param page the page number (0-based)
   * @param size the page size
   * @return list of orders matching the criteria
   */
  List<Order> findByCustomerIdAndStatus(CustomerId customerId, OrderStatus status, 
                                       int page, int size);

  /**
   * Finds orders by customer ID and date range.
   *
   * @param customerId the customer ID
   * @param startDate the start date
   * @param endDate the end date
   * @param page the page number (0-based)
   * @param size the page size
   * @return list of orders matching the criteria
   */
  List<Order> findByCustomerIdAndDateRange(CustomerId customerId, LocalDateTime startDate, 
                                          LocalDateTime endDate, int page, int size);

  /**
   * Counts orders by customer ID.
   *
   * @param customerId the customer ID
   * @return the number of orders for the customer
   */
  long countByCustomerId(CustomerId customerId);

  /**
   * Deletes an order by its ID.
   *
   * @param orderId the order ID
   */
  void deleteById(OrderId orderId);

  /**
   * Checks if an order exists by its ID.
   *
   * @param orderId the order ID
   * @return true if the order exists
   */
  boolean existsById(OrderId orderId);
}