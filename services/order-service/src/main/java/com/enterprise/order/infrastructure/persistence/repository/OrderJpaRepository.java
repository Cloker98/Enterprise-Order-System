package com.enterprise.order.infrastructure.persistence.repository;

import com.enterprise.order.infrastructure.persistence.entity.OrderJpaEntity;
import com.enterprise.order.infrastructure.persistence.entity.OrderJpaEntity.OrderStatusJpa;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * JPA Repository for Order persistence operations.
 *
 * <p>This repository provides data access methods for Order entities
 * using Spring Data JPA. It includes custom queries for complex
 * filtering and pagination scenarios.
 */
@Repository
public interface OrderJpaRepository extends JpaRepository<OrderJpaEntity, String> {

  /**
   * Finds orders by customer ID with pagination.
   *
   * @param customerId the customer ID
   * @param pageable the pagination information
   * @return page of orders for the customer
   */
  Page<OrderJpaEntity> findByCustomerId(String customerId, Pageable pageable);

  /**
   * Finds orders by customer ID and status with pagination.
   *
   * @param customerId the customer ID
   * @param status the order status
   * @param pageable the pagination information
   * @return page of orders matching the criteria
   */
  Page<OrderJpaEntity> findByCustomerIdAndStatus(String customerId, 
                                                 OrderStatusJpa status, 
                                                 Pageable pageable);

  /**
   * Finds orders by customer ID and creation date range with pagination.
   *
   * @param customerId the customer ID
   * @param startDate the start date (inclusive)
   * @param endDate the end date (inclusive)
   * @param pageable the pagination information
   * @return page of orders matching the criteria
   */
  Page<OrderJpaEntity> findByCustomerIdAndCreatedAtBetween(String customerId,
                                                          LocalDateTime startDate,
                                                          LocalDateTime endDate,
                                                          Pageable pageable);

  /**
   * Finds orders by customer ID with multiple filters using custom query.
   * This method handles complex filtering scenarios where multiple optional
   * filters need to be applied dynamically.
   *
   * @param customerId the customer ID (required)
   * @param status the order status (optional)
   * @param startDate the start date (optional)
   * @param endDate the end date (optional)
   * @param pageable the pagination information
   * @return page of orders matching the criteria
   */
  @Query("""
      SELECT o FROM OrderJpaEntity o 
      WHERE o.customerId = :customerId
      AND (:status IS NULL OR o.status = :status)
      AND (:startDate IS NULL OR o.createdAt >= :startDate)
      AND (:endDate IS NULL OR o.createdAt <= :endDate)
      ORDER BY o.createdAt DESC
      """)
  Page<OrderJpaEntity> findByCustomerIdWithFilters(@Param("customerId") String customerId,
                                                   @Param("status") OrderStatusJpa status,
                                                   @Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate,
                                                   Pageable pageable);

  /**
   * Counts orders by customer ID.
   *
   * @param customerId the customer ID
   * @return the number of orders for the customer
   */
  long countByCustomerId(String customerId);

  /**
   * Counts orders by customer ID and status.
   *
   * @param customerId the customer ID
   * @param status the order status
   * @return the number of orders matching the criteria
   */
  long countByCustomerIdAndStatus(String customerId, OrderStatusJpa status);

  /**
   * Finds orders by status with pagination.
   * Useful for administrative queries across all customers.
   *
   * @param status the order status
   * @param pageable the pagination information
   * @return page of orders with the specified status
   */
  Page<OrderJpaEntity> findByStatus(OrderStatusJpa status, Pageable pageable);

  /**
   * Finds orders created within a date range with pagination.
   * Useful for reporting and analytics.
   *
   * @param startDate the start date (inclusive)
   * @param endDate the end date (inclusive)
   * @param pageable the pagination information
   * @return page of orders created within the date range
   */
  Page<OrderJpaEntity> findByCreatedAtBetween(LocalDateTime startDate,
                                             LocalDateTime endDate,
                                             Pageable pageable);

  /**
   * Finds orders that need attention (e.g., pending for too long).
   * This query helps identify orders that might need manual intervention.
   *
   * @param status the order status to check
   * @param cutoffDate the cutoff date for "too long"
   * @param pageable the pagination information
   * @return page of orders that need attention
   */
  @Query("""
      SELECT o FROM OrderJpaEntity o 
      WHERE o.status = :status 
      AND o.createdAt < :cutoffDate
      ORDER BY o.createdAt ASC
      """)
  Page<OrderJpaEntity> findOrdersNeedingAttention(@Param("status") OrderStatusJpa status,
                                                  @Param("cutoffDate") LocalDateTime cutoffDate,
                                                  Pageable pageable);

  /**
   * Gets order statistics for a customer.
   * Returns aggregated data about orders for reporting purposes.
   *
   * @param customerId the customer ID
   * @return order statistics
   */
  @Query("""
      SELECT new com.enterprise.order.infrastructure.persistence.repository.OrderStatistics(
          COUNT(o),
          COALESCE(SUM(o.totalAmount), 0),
          COALESCE(AVG(o.totalAmount), 0)
      )
      FROM OrderJpaEntity o 
      WHERE o.customerId = :customerId
      """)
  OrderStatistics getOrderStatistics(@Param("customerId") String customerId);

  /**
   * Record for order statistics.
   */
  record OrderStatistics(
      long totalOrders,
      java.math.BigDecimal totalAmount,
      java.math.BigDecimal averageAmount
  ) {
  }
}