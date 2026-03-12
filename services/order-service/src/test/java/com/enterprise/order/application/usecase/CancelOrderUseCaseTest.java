package com.enterprise.order.application.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.enterprise.order.application.dto.request.CancelOrderRequest;
import com.enterprise.order.application.port.EventPublisherPort;
import com.enterprise.order.application.port.ProductServicePort;
import com.enterprise.order.application.usecase.CancelOrderUseCase.EventPublishingException;
import com.enterprise.order.application.usecase.CancelOrderUseCase.InvalidOrderStateException;
import com.enterprise.order.application.usecase.CancelOrderUseCase.OrderNotFoundException;
import com.enterprise.order.application.usecase.CancelOrderUseCase.StockCompensationException;
import com.enterprise.order.domain.model.CustomerId;
import com.enterprise.order.domain.model.Money;
import com.enterprise.order.domain.model.Order;
import com.enterprise.order.domain.model.OrderId;
import com.enterprise.order.domain.model.OrderItem;
import com.enterprise.order.domain.model.OrderStatus;
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
@DisplayName("CancelOrderUseCase")
class CancelOrderUseCaseTest {

  @Mock
  private OrderRepository orderRepository;

  @Mock
  private ProductServicePort productServicePort;

  @Mock
  private EventPublisherPort eventPublisherPort;

  private CancelOrderUseCase cancelOrderUseCase;

  @BeforeEach
  void setUp() {
    cancelOrderUseCase = new CancelOrderUseCase(
        orderRepository, productServicePort, eventPublisherPort);
  }

  @Test
  @DisplayName("Should cancel order successfully when valid request")
  void execute_WhenValidRequest_ShouldCancelOrderSuccessfully() {
    // Given
    String orderId = "550e8400-e29b-41d4-a716-446655440000";
    String reason = "Customer requested cancellation";
    CancelOrderRequest request = CancelOrderRequest.of(orderId, reason);
    
    Order order = createTestOrder();
    Order cancelledOrder = createTestOrder();
    cancelledOrder.cancel(reason);
    
    when(orderRepository.findById(OrderId.from(orderId))).thenReturn(Optional.of(order));
    when(orderRepository.save(any(Order.class))).thenReturn(cancelledOrder);
    doNothing().when(productServicePort).increaseStock(any(ProductId.class), any(Integer.class));
    doNothing().when(eventPublisherPort).publishOrderCancelled(any());

    // When
    cancelOrderUseCase.execute(request);

    // Then
    verify(productServicePort).increaseStock(ProductId.from("product-1"), 2);
    verify(orderRepository).save(any(Order.class));
    verify(eventPublisherPort).publishOrderCancelled(any(EventPublisherPort.OrderCancelledEvent.class));
  }

  @Test
  @DisplayName("Should throw OrderNotFoundException when order does not exist")
  void execute_WhenOrderNotFound_ShouldThrowOrderNotFoundException() {
    // Given
    String orderId = "550e8400-e29b-41d4-a716-446655440000";
    CancelOrderRequest request = CancelOrderRequest.of(orderId, "Customer requested cancellation");
    
    when(orderRepository.findById(OrderId.from(orderId))).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> cancelOrderUseCase.execute(request))
        .isInstanceOf(OrderNotFoundException.class)
        .hasMessage("Order not found: " + orderId);
  }

  @Test
  @DisplayName("Should throw InvalidOrderStateException when order cannot be cancelled")
  void execute_WhenOrderCannotBeCancelled_ShouldThrowInvalidOrderStateException() {
    // Given
    String orderId = "550e8400-e29b-41d4-a716-446655440000";
    CancelOrderRequest request = CancelOrderRequest.of(orderId, "Customer requested cancellation");
    
    Order order = createTestOrder();
    order.confirm();
    order.updateStatus(OrderStatus.DELIVERED);
    
    when(orderRepository.findById(OrderId.from(orderId))).thenReturn(Optional.of(order));

    // When & Then
    assertThatThrownBy(() -> cancelOrderUseCase.execute(request))
        .isInstanceOf(InvalidOrderStateException.class)
        .hasMessage("Order cannot be cancelled in current state: DELIVERED");
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when request is null")
  void execute_WhenRequestIsNull_ShouldThrowIllegalArgumentException() {
    // When & Then
    assertThatThrownBy(() -> cancelOrderUseCase.execute(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("CancelOrderRequest cannot be null");
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when order ID is null")
  void execute_WhenOrderIdIsNull_ShouldThrowIllegalArgumentException() {
    // Given
    CancelOrderRequest request = CancelOrderRequest.of(null, "Customer requested cancellation");

    // When & Then
    assertThatThrownBy(() -> cancelOrderUseCase.execute(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Order ID cannot be null or empty");
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when order ID is empty")
  void execute_WhenOrderIdIsEmpty_ShouldThrowIllegalArgumentException() {
    // Given
    CancelOrderRequest request = CancelOrderRequest.of("", "Customer requested cancellation");

    // When & Then
    assertThatThrownBy(() -> cancelOrderUseCase.execute(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Order ID cannot be null or empty");
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when reason is null")
  void execute_WhenReasonIsNull_ShouldThrowIllegalArgumentException() {
    // Given
    CancelOrderRequest request = CancelOrderRequest.of("550e8400-e29b-41d4-a716-446655440000", null);

    // When & Then
    assertThatThrownBy(() -> cancelOrderUseCase.execute(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Cancellation reason cannot be null or empty");
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when reason is empty")
  void execute_WhenReasonIsEmpty_ShouldThrowIllegalArgumentException() {
    // Given
    CancelOrderRequest request = CancelOrderRequest.of("550e8400-e29b-41d4-a716-446655440000", "");

    // When & Then
    assertThatThrownBy(() -> cancelOrderUseCase.execute(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Cancellation reason cannot be null or empty");
  }

  @Test
  @DisplayName("Should continue cancellation when product not found during stock compensation")
  void execute_WhenProductNotFoundDuringCompensation_ShouldContinueCancellation() {
    // Given
    String orderId = "550e8400-e29b-41d4-a716-446655440000";
    String reason = "Customer requested cancellation";
    CancelOrderRequest request = CancelOrderRequest.of(orderId, reason);
    
    Order order = createTestOrder();
    Order cancelledOrder = createTestOrder();
    cancelledOrder.cancel(reason);
    
    when(orderRepository.findById(OrderId.from(orderId))).thenReturn(Optional.of(order));
    when(orderRepository.save(any(Order.class))).thenReturn(cancelledOrder);
    doThrow(new ProductServicePort.ProductNotFoundException("Product not found"))
        .when(productServicePort).increaseStock(any(ProductId.class), any(Integer.class));
    doNothing().when(eventPublisherPort).publishOrderCancelled(any());

    // When
    cancelOrderUseCase.execute(request);

    // Then
    verify(orderRepository).save(any(Order.class));
    verify(eventPublisherPort).publishOrderCancelled(any(EventPublisherPort.OrderCancelledEvent.class));
  }

  @Test
  @DisplayName("Should throw StockCompensationException when product service unavailable")
  void execute_WhenProductServiceUnavailable_ShouldThrowStockCompensationException() {
    // Given
    String orderId = "550e8400-e29b-41d4-a716-446655440000";
    CancelOrderRequest request = CancelOrderRequest.of(orderId, "Customer requested cancellation");
    
    Order order = createTestOrder();
    
    when(orderRepository.findById(OrderId.from(orderId))).thenReturn(Optional.of(order));
    doThrow(new ProductServicePort.ProductServiceUnavailableException("Service unavailable", 
                                                                      new RuntimeException()))
        .when(productServicePort).increaseStock(any(ProductId.class), any(Integer.class));

    // When & Then
    assertThatThrownBy(() -> cancelOrderUseCase.execute(request))
        .isInstanceOf(StockCompensationException.class)
        .hasMessageContaining("Product service unavailable during stock compensation");
  }

  @Test
  @DisplayName("Should throw StockCompensationException when unexpected error during compensation")
  void execute_WhenUnexpectedErrorDuringCompensation_ShouldThrowStockCompensationException() {
    // Given
    String orderId = "550e8400-e29b-41d4-a716-446655440000";
    CancelOrderRequest request = CancelOrderRequest.of(orderId, "Customer requested cancellation");
    
    Order order = createTestOrder();
    
    when(orderRepository.findById(OrderId.from(orderId))).thenReturn(Optional.of(order));
    doThrow(new RuntimeException("Unexpected error"))
        .when(productServicePort).increaseStock(any(ProductId.class), any(Integer.class));

    // When & Then
    assertThatThrownBy(() -> cancelOrderUseCase.execute(request))
        .isInstanceOf(StockCompensationException.class)
        .hasMessageContaining("Critical failure during stock compensation");
  }

  @Test
  @DisplayName("Should throw EventPublishingException when event publishing fails")
  void execute_WhenEventPublishingFails_ShouldThrowEventPublishingException() {
    // Given
    String orderId = "550e8400-e29b-41d4-a716-446655440000";
    String reason = "Customer requested cancellation";
    CancelOrderRequest request = CancelOrderRequest.of(orderId, reason);
    
    Order order = createTestOrder();
    Order cancelledOrder = createTestOrder();
    cancelledOrder.cancel(reason);
    
    when(orderRepository.findById(OrderId.from(orderId))).thenReturn(Optional.of(order));
    when(orderRepository.save(any(Order.class))).thenReturn(cancelledOrder);
    doNothing().when(productServicePort).increaseStock(any(ProductId.class), any(Integer.class));
    doThrow(new RuntimeException("Event publishing failed"))
        .when(eventPublisherPort).publishOrderCancelled(any());

    // When & Then
    assertThatThrownBy(() -> cancelOrderUseCase.execute(request))
        .isInstanceOf(EventPublishingException.class)
        .hasMessageContaining("Failed to publish OrderCancelled event");
  }

  @Test
  @DisplayName("Should cancel order with multiple items and compensate all stock")
  void execute_WhenOrderHasMultipleItems_ShouldCompensateAllStock() {
    // Given
    String orderId = "550e8400-e29b-41d4-a716-446655440000";
    String reason = "Customer requested cancellation";
    CancelOrderRequest request = CancelOrderRequest.of(orderId, reason);
    
    Order order = createTestOrderWithMultipleItems();
    Order cancelledOrder = createTestOrderWithMultipleItems();
    cancelledOrder.cancel(reason);
    
    when(orderRepository.findById(OrderId.from(orderId))).thenReturn(Optional.of(order));
    when(orderRepository.save(any(Order.class))).thenReturn(cancelledOrder);
    doNothing().when(productServicePort).increaseStock(any(ProductId.class), any(Integer.class));
    doNothing().when(eventPublisherPort).publishOrderCancelled(any());

    // When
    cancelOrderUseCase.execute(request);

    // Then
    verify(productServicePort).increaseStock(ProductId.from("product-1"), 2);
    verify(productServicePort).increaseStock(ProductId.from("product-2"), 1);
    verify(orderRepository).save(any(Order.class));
    verify(eventPublisherPort).publishOrderCancelled(any(EventPublisherPort.OrderCancelledEvent.class));
  }

  private Order createTestOrder() {
    CustomerId customerId = CustomerId.from("customer-123");
    OrderItem orderItem = OrderItem.create(
        ProductId.from("product-1"),
        "Test Product",
        2,
        Money.brl(BigDecimal.valueOf(50.00))
    );
    
    return Order.create(customerId, List.of(orderItem));
  }

  private Order createTestOrderWithMultipleItems() {
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