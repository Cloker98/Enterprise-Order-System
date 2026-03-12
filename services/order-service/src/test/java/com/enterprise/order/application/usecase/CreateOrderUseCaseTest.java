package com.enterprise.order.application.usecase;

import com.enterprise.order.application.dto.request.CreateOrderRequest;
import com.enterprise.order.application.dto.request.OrderItemRequest;
import com.enterprise.order.application.dto.response.OrderResponse;
import com.enterprise.order.application.port.EventPublisherPort;
import com.enterprise.order.application.port.ProductServicePort;
import com.enterprise.order.domain.model.CustomerId;
import com.enterprise.order.domain.model.Money;
import com.enterprise.order.domain.model.Order;
import com.enterprise.order.domain.model.OrderId;
import com.enterprise.order.domain.model.OrderItem;
import com.enterprise.order.domain.model.OrderSnapshot;
import com.enterprise.order.domain.model.OrderStatus;
import com.enterprise.order.domain.model.ProductId;
import com.enterprise.order.domain.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for CreateOrderUseCase.
 * 
 * Following TDD approach - tests first, implementation second.
 * Task: T006 - Implement CreateOrderUseCase
 */
@ExtendWith(MockitoExtension.class)
class CreateOrderUseCaseTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductServicePort productServicePort;

    @Mock
    private EventPublisherPort eventPublisherPort;

    private CreateOrderUseCase createOrderUseCase;

    @BeforeEach
    void setUp() {
        createOrderUseCase = new CreateOrderUseCase(
            orderRepository, 
            productServicePort, 
            eventPublisherPort
        );
    }

    @Test
    void execute_WhenValidRequest_ShouldCreateOrder() {
        // Given
        OrderItemRequest itemRequest = new OrderItemRequest("product-123", 2);
        CreateOrderRequest request = new CreateOrderRequest("customer-456", List.of(itemRequest));
        
        ProductServicePort.ProductInfo productInfo = new ProductServicePort.ProductInfo(
            "product-123", "iPhone 15", new BigDecimal("999.99"), 10
        );
        
        Order savedOrder = createMockOrder();
        
        when(productServicePort.getProduct(any())).thenReturn(productInfo);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        
        // When
        OrderResponse response = createOrderUseCase.execute(request);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo("order-789");
        assertThat(response.customerId()).isEqualTo("customer-456");
        assertThat(response.status()).isEqualTo("PENDING");
        assertThat(response.items()).hasSize(1);
        assertThat(response.totalAmount()).isEqualTo(new BigDecimal("1999.98"));
        
        verify(productServicePort).getProduct(any());
        verify(productServicePort).decreaseStock(any(), eq(2));
        verify(orderRepository).save(any(Order.class));
        verify(eventPublisherPort).publishOrderCreated(any());
    }

    @Test
    void execute_WhenNullRequest_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> createOrderUseCase.execute(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("CreateOrderRequest cannot be null");
    }

    @Test
    void execute_WhenProductNotFound_ShouldThrowException() {
        // Given
        OrderItemRequest itemRequest = new OrderItemRequest("product-999", 1);
        CreateOrderRequest request = new CreateOrderRequest("customer-456", List.of(itemRequest));
        
        when(productServicePort.getProduct(any()))
            .thenThrow(new ProductServicePort.ProductNotFoundException("Product not found"));
        
        // When & Then
        assertThatThrownBy(() -> createOrderUseCase.execute(request))
            .isInstanceOf(ProductServicePort.ProductNotFoundException.class)
            .hasMessage("Product not found");
    }

    @Test
    void execute_WhenInsufficientStock_ShouldThrowException() {
        // Given
        OrderItemRequest itemRequest = new OrderItemRequest("product-123", 20);
        CreateOrderRequest request = new CreateOrderRequest("customer-456", List.of(itemRequest));
        
        ProductServicePort.ProductInfo productInfo = new ProductServicePort.ProductInfo(
            "product-123", "iPhone 15", new BigDecimal("999.99"), 10
        );
        
        when(productServicePort.getProduct(any())).thenReturn(productInfo);
        doThrow(new ProductServicePort.InsufficientStockException("Insufficient stock"))
            .when(productServicePort).decreaseStock(any(), eq(20));
        
        // When & Then
        assertThatThrownBy(() -> createOrderUseCase.execute(request))
            .isInstanceOf(ProductServicePort.InsufficientStockException.class)
            .hasMessage("Insufficient stock");
    }

    @Test
    void execute_WhenMultipleItems_ShouldCreateOrderWithAllItems() {
        // Given
        OrderItemRequest item1 = new OrderItemRequest("product-123", 2);
        OrderItemRequest item2 = new OrderItemRequest("product-456", 1);
        CreateOrderRequest request = new CreateOrderRequest("customer-789", List.of(item1, item2));
        
        ProductServicePort.ProductInfo product1 = new ProductServicePort.ProductInfo(
            "product-123", "iPhone 15", new BigDecimal("999.99"), 10
        );
        ProductServicePort.ProductInfo product2 = new ProductServicePort.ProductInfo(
            "product-456", "AirPods", new BigDecimal("299.99"), 5
        );
        
        Order savedOrder = createMockOrderWithMultipleItems();
        
        when(productServicePort.getProduct(any()))
            .thenReturn(product1)
            .thenReturn(product2);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        
        // When
        OrderResponse response = createOrderUseCase.execute(request);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.items()).hasSize(2);
        assertThat(response.totalAmount()).isEqualTo(new BigDecimal("2299.97"));
        
        verify(productServicePort).decreaseStock(any(), eq(2));
        verify(productServicePort).decreaseStock(any(), eq(1));
    }

    @Test
    void execute_WhenProductServiceUnavailable_ShouldThrowException() {
        // Given
        OrderItemRequest itemRequest = new OrderItemRequest("product-123", 1);
        CreateOrderRequest request = new CreateOrderRequest("customer-456", List.of(itemRequest));
        
        when(productServicePort.getProduct(any()))
            .thenThrow(new ProductServicePort.ProductServiceUnavailableException("Service unavailable", new RuntimeException()));
        
        // When & Then
        assertThatThrownBy(() -> createOrderUseCase.execute(request))
            .isInstanceOf(ProductServicePort.ProductServiceUnavailableException.class)
            .hasMessage("Service unavailable");
    }

    @Test
    void execute_WhenRepositorySaveFails_ShouldCompensateStock() {
        // Given
        OrderItemRequest itemRequest = new OrderItemRequest("product-123", 2);
        CreateOrderRequest request = new CreateOrderRequest("customer-456", List.of(itemRequest));
        
        ProductServicePort.ProductInfo productInfo = new ProductServicePort.ProductInfo(
            "product-123", "iPhone 15", new BigDecimal("999.99"), 10
        );
        
        when(productServicePort.getProduct(any())).thenReturn(productInfo);
        when(orderRepository.save(any(Order.class)))
            .thenThrow(new RuntimeException("Database error"));
        
        // When & Then
        assertThatThrownBy(() -> createOrderUseCase.execute(request))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Database error");
        
        // Verify compensation
        verify(productServicePort).increaseStock(any(), eq(2));
    }

    private Order createMockOrder() {
        OrderId orderId = OrderId.from("order-789");
        CustomerId customerId = CustomerId.from("customer-456");
        ProductId productId = ProductId.from("product-123");
        Money unitPrice = Money.brl(999.99);
        OrderItem orderItem = OrderItem.create(productId, "iPhone 15", 2, unitPrice);
        Money totalAmount = Money.brl(1999.98);
        LocalDateTime now = LocalDateTime.now();
        
        OrderSnapshot snapshot = new OrderSnapshot(
            orderId, customerId, List.of(orderItem), totalAmount, 
            OrderStatus.PENDING, null, now, now
        );
        
        return Order.reconstitute(snapshot);
    }

    private Order createMockOrderWithMultipleItems() {
        OrderId orderId = OrderId.from("order-789");
        CustomerId customerId = CustomerId.from("customer-789");
        ProductId productId1 = ProductId.from("product-123");
        ProductId productId2 = ProductId.from("product-456");
        Money unitPrice1 = Money.brl(999.99);
        Money unitPrice2 = Money.brl(299.99);
        OrderItem orderItem1 = OrderItem.create(productId1, "iPhone 15", 2, unitPrice1);
        OrderItem orderItem2 = OrderItem.create(productId2, "AirPods", 1, unitPrice2);
        Money totalAmount = Money.brl(2299.97);
        LocalDateTime now = LocalDateTime.now();
        
        OrderSnapshot snapshot = new OrderSnapshot(
            orderId, customerId, List.of(orderItem1, orderItem2), totalAmount,
            OrderStatus.PENDING, null, now, now
        );
        
        return Order.reconstitute(snapshot);
    }
}