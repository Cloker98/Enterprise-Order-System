package com.enterprise.order.application.mapper;

import com.enterprise.order.application.dto.request.CreateOrderRequest;
import com.enterprise.order.application.dto.response.OrderItemResponse;
import com.enterprise.order.application.dto.response.OrderResponse;
import com.enterprise.order.application.dto.response.OrderSummaryResponse;
import com.enterprise.order.domain.model.Order;
import com.enterprise.order.domain.model.OrderItem;
import java.util.Collections;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * MapStruct mapper for Order domain objects and DTOs.
 *
 * <p>Handles the mapping between domain models and application DTOs,
 * following the hexagonal architecture principles by keeping the
 * domain layer free of framework dependencies.
 */
@Mapper(componentModel = "spring")
public interface OrderMapper {

  /**
   * Maps Order domain object to OrderResponse DTO.
   *
   * @param order the order domain object
   * @return the order response DTO
   */
  @Mapping(source = "id.value", target = "id")
  @Mapping(source = "customerId.value", target = "customerId")
  @Mapping(source = "items", target = "items", qualifiedByName = "mapOrderItems")
  @Mapping(source = "totalAmount.amount", target = "totalAmount")
  @Mapping(source = "status", target = "status", qualifiedByName = "mapOrderStatus")
  @Mapping(source = "cancellationReason", target = "cancellationReason")
  @Mapping(source = "createdAt", target = "createdAt")
  @Mapping(source = "updatedAt", target = "updatedAt")
  OrderResponse toOrderResponse(Order order);

  /**
   * Maps Order domain object to OrderSummaryResponse DTO.
   *
   * @param order the order domain object
   * @return the order summary response DTO
   */
  @Mapping(source = "id.value", target = "id")
  @Mapping(source = "customerId.value", target = "customerId")
  @Mapping(source = "items", target = "itemCount", qualifiedByName = "mapItemCount")
  @Mapping(source = "totalAmount.amount", target = "totalAmount")
  @Mapping(source = "status", target = "status", qualifiedByName = "mapOrderStatus")
  @Mapping(source = "createdAt", target = "createdAt")
  OrderSummaryResponse toOrderSummaryResponse(Order order);

  /**
   * Maps list of Order domain objects to list of OrderResponse DTOs.
   *
   * @param orders the list of order domain objects
   * @return the list of order response DTOs
   */
  List<OrderResponse> toOrderResponseList(List<Order> orders);

  /**
   * Maps list of Order domain objects to list of OrderSummaryResponse DTOs.
   *
   * @param orders the list of order domain objects
   * @return the list of order summary response DTOs
   */
  List<OrderSummaryResponse> toOrderSummaryResponseList(List<Order> orders);

  /**
   * Maps OrderItem domain object to OrderItemResponse DTO.
   *
   * @param orderItem the order item domain object
   * @return the order item response DTO
   */
  @Mapping(source = "productId.value", target = "productId")
  @Mapping(source = "productName", target = "productName")
  @Mapping(source = "quantity", target = "quantity")
  @Mapping(source = "unitPrice.amount", target = "unitPrice")
  @Mapping(source = "totalPrice.amount", target = "totalPrice")
  OrderItemResponse toOrderItemResponse(OrderItem orderItem);

  /**
   * Maps list of OrderItem domain objects to list of OrderItemResponse DTOs.
   *
   * @param orderItems the list of order item domain objects
   * @return the list of order item response DTOs
   */
  @Named("mapOrderItems")
  default List<OrderItemResponse> mapOrderItems(List<OrderItem> orderItems) {
    if (orderItems == null) {
      return Collections.emptyList();
    }
    return orderItems.stream()
        .map(this::toOrderItemResponse)
        .toList();
  }

  /**
   * Maps OrderStatus enum to string.
   *
   * @param status the order status enum
   * @return the status as string
   */
  @Named("mapOrderStatus")
  default String mapOrderStatus(com.enterprise.order.domain.model.OrderStatus status) {
    return status != null ? status.name() : null;
  }

  /**
   * Maps list of OrderItems to item count.
   *
   * @param orderItems the list of order items
   * @return the count of items
   */
  @Named("mapItemCount")
  default int mapItemCount(List<OrderItem> orderItems) {
    return orderItems != null ? orderItems.size() : 0;
  }
}