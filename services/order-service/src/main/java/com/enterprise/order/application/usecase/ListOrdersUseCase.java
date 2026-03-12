package com.enterprise.order.application.usecase;

import com.enterprise.order.application.dto.request.ListOrdersRequest;
import com.enterprise.order.application.dto.response.OrderSummaryResponse;
import com.enterprise.order.domain.model.CustomerId;
import com.enterprise.order.domain.model.Order;
import com.enterprise.order.domain.model.OrderStatus;
import com.enterprise.order.domain.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for listing orders with pagination and filtering.
 *
 * <p>This use case handles:
 * - Paginated order listing
 * - Filtering by status and date range
 * - Sorting by creation date
 * - Authorization validation (customer can only see their own orders)
 */
@Service
@Transactional(readOnly = true)
public class ListOrdersUseCase {

  private static final Logger log = LoggerFactory.getLogger(ListOrdersUseCase.class);

  private final OrderRepository orderRepository;

  /**
   * Constructor for ListOrdersUseCase.
   *
   * @param orderRepository the order repository
   */
  public ListOrdersUseCase(OrderRepository orderRepository) {
    this.orderRepository = orderRepository;
  }

  /**
   * Executes the list orders use case.
   *
   * @param request the list orders request with filters and pagination
   * @return paginated list of order summaries
   */
  public Page<OrderSummaryResponse> execute(ListOrdersRequest request) {
    log.info("Listing orders for customer: {} with filters: {}", 
             request.customerId(), request);

    validateRequest(request);

    CustomerId customerId = CustomerId.from(request.customerId());
    Pageable pageable = createPageable(request);

    Page<Order> orders;
    
    if (hasFilters(request)) {
      orders = orderRepository.findByCustomerIdWithFilters(
          customerId,
          request.status() != null ? OrderStatus.valueOf(request.status()) : null,
          request.startDate(),
          request.endDate(),
          pageable
      );
    } else {
      orders = orderRepository.findByCustomerId(customerId, pageable);
    }

    log.debug("Found {} orders for customer: {}", orders.getTotalElements(), customerId);

    return orders.map(this::mapToSummaryResponse);
  }

  private void validateRequest(ListOrdersRequest request) {
    if (request == null) {
      throw new IllegalArgumentException("ListOrdersRequest cannot be null");
    }
    
    if (request.customerId() == null || request.customerId().trim().isEmpty()) {
      throw new IllegalArgumentException("Customer ID cannot be null or empty");
    }
    
    if (request.page() < 0) {
      throw new IllegalArgumentException("Page number cannot be negative");
    }
    
    if (request.size() <= 0 || request.size() > 100) {
      throw new IllegalArgumentException("Page size must be between 1 and 100");
    }
    
    if (request.startDate() != null && request.endDate() != null 
        && request.startDate().isAfter(request.endDate())) {
      throw new IllegalArgumentException("Start date cannot be after end date");
    }
    
    if (request.status() != null) {
      try {
        OrderStatus.valueOf(request.status());
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("Invalid order status: " + request.status());
      }
    }
  }

  private Pageable createPageable(ListOrdersRequest request) {
    Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
    return PageRequest.of(request.page(), request.size(), sort);
  }

  private boolean hasFilters(ListOrdersRequest request) {
    return request.status() != null 
           || request.startDate() != null 
           || request.endDate() != null;
  }

  private OrderSummaryResponse mapToSummaryResponse(Order order) {
    return new OrderSummaryResponse(
        order.getId().value(),
        order.getCustomerId().value(),
        order.getItems().size(),
        order.getTotalAmount().amount(),
        order.getStatus().name(),
        order.getCreatedAt()
    );
  }
}