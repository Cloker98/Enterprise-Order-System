package com.enterprise.order.application.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.enterprise.order.application.dto.response.OrderItemResponse;
import com.enterprise.order.application.dto.response.OrderResponse;
import com.enterprise.order.application.dto.response.OrderSummaryResponse;
import com.enterprise.order.domain.model.CustomerId;
import com.enterprise.order.domain.model.Money;
import com.enterprise.order.domain.model.Order;
import com.enterprise.order.domain.model.OrderItem;
import com.enterprise.order.domain.model.ProductId;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

@DisplayName("OrderMapper")
class OrderMapperTest {

  private OrderMapper orderMapper;

  @BeforeEach
  void setUp() {
    orderMapper = Mappers.getMapper(OrderMapper.class);
  }

  @Test
  @DisplayName("Should map Order to OrderResponse correctly")
  void toOrderResponse_WhenValidOrder_ShouldMapCorrectly() {
    // Given
    Order order = createTestOrder();

    // When
    OrderResponse response = orderMapper.toOrderResponse(order);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.id()).isEqualTo(order.getId().value());
    assertThat(response.customerId()).isEqualTo(order.getCustomerId().value());
    assertThat(response.items()).hasSize(2);
    assertThat(response.totalAmount()).isEqualTo(order.getTotalAmount().amount());
    assertThat(response.status()).isEqualTo(order.getStatus().name());
    assertThat(response.cancellationReason()).isEqualTo(order.getCancellationReason());
    assertThat(response.createdAt()).isEqualTo(order.getCreatedAt());
    assertThat(response.updatedAt()).isEqualTo(order.getUpdatedAt());

    // Verify first item mapping
    OrderItemResponse item1 = response.items().get(0);
    OrderItem domainItem1 = order.getItems().get(0);
    assertThat(item1.productId()).isEqualTo(domainItem1.getProductId().value());
    assertThat(item1.productName()).isEqualTo(domainItem1.getProductName());
    assertThat(item1.quantity()).isEqualTo(domainItem1.getQuantity());
    assertThat(item1.unitPrice()).isEqualTo(domainItem1.getUnitPrice().amount());
    assertThat(item1.totalPrice()).isEqualTo(domainItem1.getTotalPrice().amount());
  }

  @Test
  @DisplayName("Should map Order to OrderSummaryResponse correctly")
  void toOrderSummaryResponse_WhenValidOrder_ShouldMapCorrectly() {
    // Given
    Order order = createTestOrder();

    // When
    OrderSummaryResponse response = orderMapper.toOrderSummaryResponse(order);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.id()).isEqualTo(order.getId().value());
    assertThat(response.customerId()).isEqualTo(order.getCustomerId().value());
    assertThat(response.itemCount()).isEqualTo(order.getItems().size());
    assertThat(response.totalAmount()).isEqualTo(order.getTotalAmount().amount());
    assertThat(response.status()).isEqualTo(order.getStatus().name());
    assertThat(response.createdAt()).isEqualTo(order.getCreatedAt());
  }

  @Test
  @DisplayName("Should map OrderItem to OrderItemResponse correctly")
  void toOrderItemResponse_WhenValidOrderItem_ShouldMapCorrectly() {
    // Given
    OrderItem orderItem = OrderItem.create(
        ProductId.from("product-123"),
        "Test Product",
        3,
        Money.brl(BigDecimal.valueOf(25.50))
    );

    // When
    OrderItemResponse response = orderMapper.toOrderItemResponse(orderItem);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.productId()).isEqualTo("product-123");
    assertThat(response.productName()).isEqualTo("Test Product");
    assertThat(response.quantity()).isEqualTo(3);
    assertThat(response.unitPrice()).isEqualTo(BigDecimal.valueOf(25.50));
    assertThat(response.totalPrice()).isEqualTo(BigDecimal.valueOf(76.50));
  }

  @Test
  @DisplayName("Should map list of Orders to list of OrderResponses correctly")
  void toOrderResponseList_WhenValidOrderList_ShouldMapCorrectly() {
    // Given
    Order order1 = createTestOrder();
    Order order2 = createTestOrder();
    List<Order> orders = List.of(order1, order2);

    // When
    List<OrderResponse> responses = orderMapper.toOrderResponseList(orders);

    // Then
    assertThat(responses)
      .isNotNull()
      .hasSize(2);
    assertThat(responses.get(0).id()).isEqualTo(order1.getId().value());
    assertThat(responses.get(1).id()).isEqualTo(order2.getId().value());
  }

  @Test
  @DisplayName("Should map list of Orders to list of OrderSummaryResponses correctly")
  void toOrderSummaryResponseList_WhenValidOrderList_ShouldMapCorrectly() {
    // Given
    Order order1 = createTestOrder();
    Order order2 = createTestOrder();
    List<Order> orders = List.of(order1, order2);

    // When
    List<OrderSummaryResponse> responses = orderMapper.toOrderSummaryResponseList(orders);

    // Then
    assertThat(responses)
      .isNotNull()
      .hasSize(2);
    assertThat(responses.get(0).id()).isEqualTo(order1.getId().value());
    assertThat(responses.get(1).id()).isEqualTo(order2.getId().value());
  }

  @Test
  @DisplayName("Should handle null order gracefully")
  void toOrderResponse_WhenNullOrder_ShouldReturnNull() {
    // When
    OrderResponse response = orderMapper.toOrderResponse(null);

    // Then
    assertThat(response).isNull();
  }

  @Test
  @DisplayName("Should handle null order list gracefully")
  void toOrderResponseList_WhenNullOrderList_ShouldReturnNull() {
    // When
    List<OrderResponse> responses = orderMapper.toOrderResponseList(null);

    // Then
    assertThat(responses).isNull();
  }

  @Test
  @DisplayName("Should handle empty order list correctly")
  void toOrderResponseList_WhenEmptyOrderList_ShouldReturnEmptyList() {
    // Given
    List<Order> orders = List.of();

    // When
    List<OrderResponse> responses = orderMapper.toOrderResponseList(orders);

    // Then
    assertThat(responses)
      .isNotNull()
      .isEmpty();
  }

  @Test
  @DisplayName("Should map cancelled order with cancellation reason")
  void toOrderResponse_WhenCancelledOrder_ShouldIncludeCancellationReason() {
    // Given
    Order order = createTestOrder();
    String cancellationReason = "Customer requested cancellation";
    order.cancel(cancellationReason);

    // When
    OrderResponse response = orderMapper.toOrderResponse(order);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.status()).isEqualTo("CANCELLED");
    assertThat(response.cancellationReason()).isEqualTo(cancellationReason);
  }

  @Test
  @DisplayName("Should map confirmed order correctly")
  void toOrderResponse_WhenConfirmedOrder_ShouldMapStatusCorrectly() {
    // Given
    Order order = createTestOrder();
    order.confirm();

    // When
    OrderResponse response = orderMapper.toOrderResponse(order);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.status()).isEqualTo("CONFIRMED");
    assertThat(response.cancellationReason()).isNull();
  }

  @Test
  @DisplayName("Should map order with single item correctly")
  void toOrderResponse_WhenSingleItemOrder_ShouldMapCorrectly() {
    // Given
    CustomerId customerId = CustomerId.from("customer-123");
    OrderItem orderItem = OrderItem.create(
        ProductId.from("product-1"),
        "Single Product",
        1,
        Money.brl(BigDecimal.valueOf(100.00))
    );
    Order order = Order.create(customerId, List.of(orderItem));

    // When
    OrderResponse response = orderMapper.toOrderResponse(order);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.items()).hasSize(1);
    assertThat(response.totalAmount()).isEqualTo(BigDecimal.valueOf(100.00));
  }

  @Test
  @DisplayName("Should map order summary with correct item count")
  void toOrderSummaryResponse_WhenMultipleItems_ShouldCountCorrectly() {
    // Given
    Order order = createTestOrder(); // Has 2 items

    // When
    OrderSummaryResponse response = orderMapper.toOrderSummaryResponse(order);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.itemCount()).isEqualTo(2);
  }

  private Order createTestOrder() {
    CustomerId customerId = CustomerId.from("customer-123");
    
    OrderItem item1 = OrderItem.create(
        ProductId.from("product-1"),
        "Product 1",
        2,
        Money.brl(BigDecimal.valueOf(25.00))
    );
    
    OrderItem item2 = OrderItem.create(
        ProductId.from("product-2"),
        "Product 2",
        1,
        Money.brl(BigDecimal.valueOf(75.00))
    );
    
    return Order.create(customerId, List.of(item1, item2));
  }
}