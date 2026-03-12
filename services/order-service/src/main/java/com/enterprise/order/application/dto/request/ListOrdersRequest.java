package com.enterprise.order.application.dto.request;

import java.time.LocalDateTime;

/**
 * Request DTO for listing orders with pagination and filtering.
 *
 * @param customerId the customer ID to filter orders
 * @param status the order status filter (optional)
 * @param startDate the start date filter (optional)
 * @param endDate the end date filter (optional)
 * @param page the page number (0-based)
 * @param size the page size (1-100)
 */
public record ListOrdersRequest(
    String customerId,
    String status,
    LocalDateTime startDate,
    LocalDateTime endDate,
    int page,
    int size
) {
  
  /**
   * Creates a ListOrdersRequest with default pagination.
   *
   * @param customerId the customer ID
   * @return a request with page=0, size=20
   */
  public static ListOrdersRequest withDefaults(String customerId) {
    return new ListOrdersRequest(customerId, null, null, null, 0, 20);
  }

  /**
   * Creates a ListOrdersRequest with custom pagination.
   *
   * @param customerId the customer ID
   * @param page the page number
   * @param size the page size
   * @return a request with specified pagination
   */
  public static ListOrdersRequest withPagination(String customerId, int page, int size) {
    return new ListOrdersRequest(customerId, null, null, null, page, size);
  }

  /**
   * Creates a ListOrdersRequest with status filter.
   *
   * @param customerId the customer ID
   * @param status the order status
   * @param page the page number
   * @param size the page size
   * @return a request with status filter
   */
  public static ListOrdersRequest withStatusFilter(String customerId, String status, 
                                                   int page, int size) {
    return new ListOrdersRequest(customerId, status, null, null, page, size);
  }

  /**
   * Creates a ListOrdersRequest with date range filter.
   *
   * @param customerId the customer ID
   * @param startDate the start date
   * @param endDate the end date
   * @param page the page number
   * @param size the page size
   * @return a request with date range filter
   */
  public static ListOrdersRequest withDateRangeFilter(String customerId, 
                                                      LocalDateTime startDate,
                                                      LocalDateTime endDate,
                                                      int page, int size) {
    return new ListOrdersRequest(customerId, null, startDate, endDate, page, size);
  }
}