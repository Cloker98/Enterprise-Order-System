package com.enterprise.order.infrastructure.persistence.mapper;

import com.enterprise.order.domain.model.CustomerId;
import com.enterprise.order.domain.model.Money;
import com.enterprise.order.domain.model.Order;
import com.enterprise.order.domain.model.OrderId;
import com.enterprise.order.domain.model.OrderItem;
import com.enterprise.order.domain.model.OrderSnapshot;
import com.enterprise.order.domain.model.OrderStatus;
import com.enterprise.order.domain.model.ProductId;
import com.enterprise.order.infrastructure.persistence.entity.OrderItemJpaEntity;
import com.enterprise.order.infrastructure.persistence.entity.OrderJpaEntity;
import com.enterprise.order.infrastructure.persistence.entity.OrderJpaEntity.OrderStatusJpa;
import java.util.Collections;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * MapStruct mapper for Order JPA entities and domain objects.
 *
 * <p>Handles the mapping between domain models and JPA entities,
 * maintaining the separation between domain and infrastructure layers.
 */
@Mapper(componentModel = "spring")
public interface OrderJpaMapper {

  /**
   * Maps Order domain object to OrderJpaEntity.
   *
   * @param order the order domain object
   * @return the order JPA entity
   */
  @Mapping(source = "id.value", target = "id")
  @Mapping(source = "customerId.value", target = "customerId")
  @Mapping(source = "items", target = "items", qualifiedByName = "mapOrderItemsToJpa")
  @Mapping(source = "totalAmount.amount", target = "totalAmount")
  @Mapping(source = "status", target = "status", qualifiedByName = "mapOrderStatusToJpa")
  @Mapping(source = "cancellationReason", target = "cancellationReason")
  @Mapping(source = "createdAt", target = "createdAt")
  @Mapping(source = "updatedAt", target = "updatedAt")
  @Mapping(target = "version", ignore = true) // Managed by JPA
  OrderJpaEntity toJpaEntity(Order order);

  /**
   * Maps OrderItem domain object to OrderItemJpaEntity.
   *
   * @param orderItem the order item domain object
   * @return the order item JPA entity
   */
  @Mapping(source = "productId.value", target = "productId")
  @Mapping(source = "productName", target = "productName")
  @Mapping(source = "quantity", target = "quantity")
  @Mapping(source = "unitPrice.amount", target = "unitPrice")
  @Mapping(source = "totalPrice.amount", target = "totalPrice")
  @Mapping(target = "id", ignore = true) // Auto-generated
  @Mapping(target = "order", ignore = true) // Set by parent
  OrderItemJpaEntity toJpaEntity(OrderItem orderItem);

  /**
   * Maps OrderJpaEntity to Order domain object.
   *
   * @param jpaEntity the order JPA entity
   * @return the order domain object
   */
  default Order toDomainObject(OrderJpaEntity jpaEntity) {
    if (jpaEntity == null) {
      return null;
    }

    OrderSnapshot snapshot = new OrderSnapshot(
        OrderId.from(jpaEntity.getId()),
        CustomerId.from(jpaEntity.getCustomerId()),
        mapOrderItemsToDomain(jpaEntity.getItems()),
        Money.brl(jpaEntity.getTotalAmount()),
        mapOrderStatusToDomain(jpaEntity.getStatus()),
        jpaEntity.getCancellationReason(),
        jpaEntity.getCreatedAt(),
        jpaEntity.getUpdatedAt()
    );

    return Order.reconstitute(snapshot);
  }

  /**
   * Maps OrderItemJpaEntity to OrderItem domain object.
   *
   * @param jpaEntity the order item JPA entity
   * @return the order item domain object
   */
  default OrderItem toDomainObject(OrderItemJpaEntity jpaEntity) {
    if (jpaEntity == null) {
      return null;
    }

    return OrderItem.create(
        ProductId.from(jpaEntity.getProductId()),
        jpaEntity.getProductName(),
        jpaEntity.getQuantity(),
        Money.brl(jpaEntity.getUnitPrice())
    );
  }

  /**
   * Maps list of OrderItem domain objects to list of OrderItemJpaEntity.
   *
   * @param orderItems the list of order item domain objects
   * @return the list of order item JPA entities
   */
  @Named("mapOrderItemsToJpa")
  default List<OrderItemJpaEntity> mapOrderItemsToJpa(List<OrderItem> orderItems) {
    if (orderItems == null) {
      return Collections.emptyList();
    }
    return orderItems.stream()
        .map(this::toJpaEntity)
        .toList();
  }

  /**
   * Maps list of OrderItemJpaEntity to list of OrderItem domain objects.
   *
   * @param jpaEntities the list of order item JPA entities
   * @return the list of order item domain objects
   */
  default List<OrderItem> mapOrderItemsToDomain(List<OrderItemJpaEntity> jpaEntities) {
    if (jpaEntities == null) {
      return Collections.emptyList();
    }
    return jpaEntities.stream()
        .map(this::toDomainObject)
        .toList();
  }

  /**
   * Maps OrderStatus domain enum to OrderStatusJpa.
   *
   * @param status the domain order status
   * @return the JPA order status
   */
  @Named("mapOrderStatusToJpa")
  default OrderStatusJpa mapOrderStatusToJpa(OrderStatus status) {
    if (status == null) {
      return null;
    }
    return switch (status) {
      case PENDING -> OrderStatusJpa.PENDING;
      case CONFIRMED -> OrderStatusJpa.CONFIRMED;
      case DELIVERED -> OrderStatusJpa.DELIVERED;
      case CANCELLED -> OrderStatusJpa.CANCELLED;
    };
  }

  /**
   * Maps OrderStatusJpa to OrderStatus domain enum.
   *
   * @param status the JPA order status
   * @return the domain order status
   */
  default OrderStatus mapOrderStatusToDomain(OrderStatusJpa status) {
    if (status == null) {
      return null;
    }
    return switch (status) {
      case PENDING -> OrderStatus.PENDING;
      case CONFIRMED -> OrderStatus.CONFIRMED;
      case DELIVERED -> OrderStatus.DELIVERED;
      case CANCELLED -> OrderStatus.CANCELLED;
      case SHIPPED -> OrderStatus.DELIVERED; // Map SHIPPED to DELIVERED for compatibility
    };
  }
}