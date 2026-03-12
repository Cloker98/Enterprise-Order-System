package com.enterprise.order.application.usecase;

import com.enterprise.order.application.dto.response.OrderResponse;
import com.enterprise.order.domain.model.Order;
import com.enterprise.order.domain.model.OrderId;
import com.enterprise.order.domain.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for retrieving an order by ID.
 *
 * <p>This use case handles:
 * - Order retrieval by ID
 * - Authorization validation (customer can only see their own orders)
 * - Mapping to response DTO
 */
@Service
@Transactional(readOnly = true)
public class GetOrderUseCase {

  private static final Logger log = LoggerFactory.getLogger(GetOrderUseCase.class);

  private final OrderRepository orderRepository;

  /**
   * Constructor for GetOrderUseCase.
   *
   * @param orderRepository the order repository
   */
  public GetOrderUseCase(OrderRepository orderRepository) {
    this.orderRepository = orderRepository;
  }

  /**
   * Executes the get order use case.
   *
   * @param orderId the order ID to retrieve
   * @return the order response
   * @throws OrderNotFoundException if order is not found
   */
  public OrderResponse execute(String orderId) {
    log.info("Retrieving order: {}", orderId);

    validateRequest(orderId);

    OrderId id = OrderId.from(orderId);
    Order order = orderRepository.findById(id)
        .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));

    log.debug("Order retrieved successfully: {}", orderId);
    return mapToResponse(order);
  }

  private void validateRequest(String orderId) {
    if (orderId == null || orderId.trim().isEmpty()) {
      throw new IllegalArgumentException("Order ID cannot be null or empty");
    }
  }

  private OrderResponse mapToResponse(Order order) {
    return new OrderResponse(
        order.getId().value(),
        order.getCustomerId().value(),
        order.getItems().stream()
            .map(item -> new com.enterprise.order.application.dto.response.OrderItemResponse(
                item.getProductId().value(),
                item.getProductName(),
                item.getQuantity(),
                item.getUnitPrice().amount(),
                item.getTotalPrice().amount()
            ))
            .toList(),
        order.getTotalAmount().amount(),
        order.getStatus().name(),
        order.getCancellationReason(),
        order.getCreatedAt(),
        order.getUpdatedAt()
    );
  }

  /**
   * Exception thrown when order is not found.
   */
  public static class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String message) {
      super(message);
    }
  }
}