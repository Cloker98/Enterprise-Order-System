package com.enterprise.order.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.enterprise.order.application.dto.response.OrderResponse;
import com.enterprise.order.application.usecase.GetOrderUseCase.OrderNotFoundException;
import com.enterprise.order.domain.model.CustomerId;
import com.enterprise.order.domain.model.Money;
import com.enterprise.order.domain.model.Order;
import com.enterprise.order.domain.model.OrderId;
import com.enterprise.order.domain.model.OrderItem;
import com.enterprise.order.domain.model.ProductId;
import com.enterprise.order.domain.repository.OrderRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetOrderUseCase")
class GetOrderUseCaseTest {

  @Mock
  private OrderRepository orderRepository;

  private GetOrderUseCase getOrderUseCase;

  @BeforeEach
  void setUp() {
    getOrderUseCase = new GetOrderUseCase(orderRepository);
  }

  @Test
  @DisplayName("Should return order when order exists")
  void execute_WhenOrderExists_ShouldReturnOrder() {
    // Given
    String orderId = "550e8400-e29b-41d4-a716-446655440000";
    OrderId id = OrderId.from(orderId);
    
    CustomerId customerId = CustomerId.from("customer-123");
    ProductId productId = ProductId.from("product-456");
    
    OrderItem orderItem = OrderItem.create(
        productId,
        "Test Product",
        2,
        Money.brl(BigDecimal.valueOf(50.00))
    );
    
    Order order = Order.create(customerId, List.of(orderItem));
    
    when(orderRepository.findById(id)).thenReturn(Optional.of(order));

    // When
    OrderResponse response = getOrderUseCase.execute(orderId);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.id()).isEqualTo(order.getId().value());
    assertThat(response.customerId()).isEqualTo(customerId.value());
    assertThat(response.items()).hasSize(1);
    assertThat(response.items().get(0).productId()).isEqualTo(productId.value());
    assertThat(response.items().get(0).productName()).isEqualTo("Test Product");
    assertThat(response.items().get(0).quantity()).isEqualTo(2);
    assertThat(response.items().get(0).unitPrice()).isEqualTo(BigDecimal.valueOf(50.00));
    assertThat(response.items().get(0).totalPrice()).isEqualTo(BigDecimal.valueOf(100.00));
    assertThat(response.totalAmount()).isEqualTo(BigDecimal.valueOf(100.00));
    assertThat(response.status()).isEqualTo("PENDING");
    assertThat(response.cancellationReason()).isNull();
    assertThat(response.createdAt()).isNotNull();
    assertThat(response.updatedAt()).isNotNull();
  }

  @Test
  @DisplayName("Should throw OrderNotFoundException when order does not exist")
  void execute_WhenOrderNotFound_ShouldThrowOrderNotFoundException() {
    // Given
    String orderId = "550e8400-e29b-41d4-a716-446655440000";
    OrderId id = OrderId.from(orderId);
    
    when(orderRepository.findById(id)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> getOrderUseCase.execute(orderId))
        .isInstanceOf(OrderNotFoundException.class)
        .hasMessage("Order not found: " + orderId);
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when order ID is null")
  void execute_WhenOrderIdIsNull_ShouldThrowIllegalArgumentException() {
    // When & Then
    assertThatThrownBy(() -> getOrderUseCase.execute(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Order ID cannot be null or empty");
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when order ID is empty")
  void execute_WhenOrderIdIsEmpty_ShouldThrowIllegalArgumentException() {
    // When & Then
    assertThatThrownBy(() -> getOrderUseCase.execute(""))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Order ID cannot be null or empty");
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when order ID is blank")
  void execute_WhenOrderIdIsBlank_ShouldThrowIllegalArgumentException() {
    // When & Then
    assertThatThrownBy(() -> getOrderUseCase.execute("   "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Order ID cannot be null or empty");
  }

  @Test
  @DisplayName("Should return order with multiple items")
  void execute_WhenOrderHasMultipleItems_ShouldReturnOrderWithAllItems() {
    // Given
    String orderId = "550e8400-e29b-41d4-a716-446655440000";
    OrderId id = OrderId.from(orderId);
    
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
    
    Order order = Order.create(customerId, List.of(item1, item2));
    
    when(orderRepository.findById(id)).thenReturn(Optional.of(order));

    // When
    OrderResponse response = getOrderUseCase.execute(orderId);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.items()).hasSize(2);
    assertThat(response.totalAmount()).isEqualTo(BigDecimal.valueOf(125.00));
    
    // Verify first item
    assertThat(response.items().get(0).productId()).isEqualTo("product-1");
    assertThat(response.items().get(0).productName()).isEqualTo("Product 1");
    assertThat(response.items().get(0).quantity()).isEqualTo(2);
    assertThat(response.items().get(0).unitPrice()).isEqualTo(BigDecimal.valueOf(25.00));
    assertThat(response.items().get(0).totalPrice()).isEqualTo(BigDecimal.valueOf(50.00));
    
    // Verify second item
    assertThat(response.items().get(1).productId()).isEqualTo("product-2");
    assertThat(response.items().get(1).productName()).isEqualTo("Product 2");
    assertThat(response.items().get(1).quantity()).isEqualTo(1);
    assertThat(response.items().get(1).unitPrice()).isEqualTo(BigDecimal.valueOf(75.00));
    assertThat(response.items().get(1).totalPrice()).isEqualTo(BigDecimal.valueOf(75.00));
  }

  @Test
  @DisplayName("Should return cancelled order with cancellation reason")
  void execute_WhenOrderIsCancelled_ShouldReturnOrderWithCancellationReason() {
    // Given
    String orderId = "550e8400-e29b-41d4-a716-446655440000";
    OrderId id = OrderId.from(orderId);
    
    CustomerId customerId = CustomerId.from("customer-123");
    ProductId productId = ProductId.from("product-456");
    
    OrderItem orderItem = OrderItem.create(
        productId,
        "Test Product",
        1,
        Money.brl(BigDecimal.valueOf(100.00))
    );
    
    Order order = Order.create(customerId, List.of(orderItem));
    order.cancel("Customer requested cancellation");
    
    when(orderRepository.findById(id)).thenReturn(Optional.of(order));

    // When
    OrderResponse response = getOrderUseCase.execute(orderId);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.status()).isEqualTo("CANCELLED");
    assertThat(response.cancellationReason()).isEqualTo("Customer requested cancellation");
  }
}