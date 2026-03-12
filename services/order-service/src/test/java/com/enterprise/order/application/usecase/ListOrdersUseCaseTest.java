package com.enterprise.order.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.enterprise.order.application.dto.request.ListOrdersRequest;
import com.enterprise.order.application.dto.response.OrderSummaryResponse;
import com.enterprise.order.domain.model.CustomerId;
import com.enterprise.order.domain.model.Money;
import com.enterprise.order.domain.model.Order;
import com.enterprise.order.domain.model.OrderItem;
import com.enterprise.order.domain.model.OrderStatus;
import com.enterprise.order.domain.model.ProductId;
import com.enterprise.order.domain.repository.OrderRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
@DisplayName("ListOrdersUseCase")
class ListOrdersUseCaseTest {

  @Mock
  private OrderRepository orderRepository;

  private ListOrdersUseCase listOrdersUseCase;

  @BeforeEach
  void setUp() {
    listOrdersUseCase = new ListOrdersUseCase(orderRepository);
  }

  @Test
  @DisplayName("Should return paginated orders for customer")
  void execute_WhenValidRequest_ShouldReturnPaginatedOrders() {
    // Given
    String customerId = "customer-123";
    ListOrdersRequest request = ListOrdersRequest.withDefaults(customerId);
    
    CustomerId customerIdObj = CustomerId.from(customerId);
    
    Order order1 = createTestOrder(customerIdObj, "product-1", 2, BigDecimal.valueOf(50.00));
    Order order2 = createTestOrder(customerIdObj, "product-2", 1, BigDecimal.valueOf(100.00));
    
    Page<Order> ordersPage = new PageImpl<>(List.of(order1, order2));
    
    when(orderRepository.findByCustomerId(eq(customerIdObj), any(Pageable.class)))
        .thenReturn(ordersPage);

    // When
    Page<OrderSummaryResponse> result = listOrdersUseCase.execute(request);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(2);
    assertThat(result.getTotalElements()).isEqualTo(2);
    
    OrderSummaryResponse summary1 = result.getContent().get(0);
    assertThat(summary1.id()).isEqualTo(order1.getId().value());
    assertThat(summary1.customerId()).isEqualTo(customerId);
    assertThat(summary1.itemCount()).isEqualTo(1);
    assertThat(summary1.totalAmount()).isEqualTo(BigDecimal.valueOf(100.00));
    assertThat(summary1.status()).isEqualTo("PENDING");
    assertThat(summary1.createdAt()).isNotNull();
  }

  @Test
  @DisplayName("Should return filtered orders by status")
  void execute_WhenStatusFilter_ShouldReturnFilteredOrders() {
    // Given
    String customerId = "customer-123";
    ListOrdersRequest request = ListOrdersRequest.withStatusFilter(
        customerId, "CONFIRMED", 0, 20);
    
    CustomerId customerIdObj = CustomerId.from(customerId);
    
    Order order = createTestOrder(customerIdObj, "product-1", 1, BigDecimal.valueOf(50.00));
    order.confirm();
    
    Page<Order> ordersPage = new PageImpl<>(List.of(order));
    
    when(orderRepository.findByCustomerIdWithFilters(
        eq(customerIdObj), eq(OrderStatus.CONFIRMED), eq(null), eq(null), any(Pageable.class)))
        .thenReturn(ordersPage);

    // When
    Page<OrderSummaryResponse> result = listOrdersUseCase.execute(request);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).status()).isEqualTo("CONFIRMED");
  }

  @Test
  @DisplayName("Should return filtered orders by date range")
  void execute_WhenDateRangeFilter_ShouldReturnFilteredOrders() {
    // Given
    String customerId = "customer-123";
    LocalDateTime startDate = LocalDateTime.now().minusDays(7);
    LocalDateTime endDate = LocalDateTime.now();
    
    ListOrdersRequest request = ListOrdersRequest.withDateRangeFilter(
        customerId, startDate, endDate, 0, 20);
    
    CustomerId customerIdObj = CustomerId.from(customerId);
    
    Order order = createTestOrder(customerIdObj, "product-1", 1, BigDecimal.valueOf(50.00));
    
    Page<Order> ordersPage = new PageImpl<>(List.of(order));
    
    when(orderRepository.findByCustomerIdWithFilters(
        eq(customerIdObj), eq(null), eq(startDate), eq(endDate), any(Pageable.class)))
        .thenReturn(ordersPage);

    // When
    Page<OrderSummaryResponse> result = listOrdersUseCase.execute(request);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(1);
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when request is null")
  void execute_WhenRequestIsNull_ShouldThrowIllegalArgumentException() {
    // When & Then
    assertThatThrownBy(() -> listOrdersUseCase.execute(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("ListOrdersRequest cannot be null");
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when customer ID is null")
  void execute_WhenCustomerIdIsNull_ShouldThrowIllegalArgumentException() {
    // Given
    ListOrdersRequest request = new ListOrdersRequest(null, null, null, null, 0, 20);

    // When & Then
    assertThatThrownBy(() -> listOrdersUseCase.execute(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Customer ID cannot be null or empty");
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when customer ID is empty")
  void execute_WhenCustomerIdIsEmpty_ShouldThrowIllegalArgumentException() {
    // Given
    ListOrdersRequest request = new ListOrdersRequest("", null, null, null, 0, 20);

    // When & Then
    assertThatThrownBy(() -> listOrdersUseCase.execute(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Customer ID cannot be null or empty");
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when page is negative")
  void execute_WhenPageIsNegative_ShouldThrowIllegalArgumentException() {
    // Given
    ListOrdersRequest request = new ListOrdersRequest("customer-123", null, null, null, -1, 20);

    // When & Then
    assertThatThrownBy(() -> listOrdersUseCase.execute(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Page number cannot be negative");
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when size is zero")
  void execute_WhenSizeIsZero_ShouldThrowIllegalArgumentException() {
    // Given
    ListOrdersRequest request = new ListOrdersRequest("customer-123", null, null, null, 0, 0);

    // When & Then
    assertThatThrownBy(() -> listOrdersUseCase.execute(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Page size must be between 1 and 100");
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when size exceeds maximum")
  void execute_WhenSizeExceedsMaximum_ShouldThrowIllegalArgumentException() {
    // Given
    ListOrdersRequest request = new ListOrdersRequest("customer-123", null, null, null, 0, 101);

    // When & Then
    assertThatThrownBy(() -> listOrdersUseCase.execute(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Page size must be between 1 and 100");
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when start date is after end date")
  void execute_WhenStartDateAfterEndDate_ShouldThrowIllegalArgumentException() {
    // Given
    LocalDateTime startDate = LocalDateTime.now();
    LocalDateTime endDate = LocalDateTime.now().minusDays(1);
    
    ListOrdersRequest request = new ListOrdersRequest(
        "customer-123", null, startDate, endDate, 0, 20);

    // When & Then
    assertThatThrownBy(() -> listOrdersUseCase.execute(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Start date cannot be after end date");
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when status is invalid")
  void execute_WhenStatusIsInvalid_ShouldThrowIllegalArgumentException() {
    // Given
    ListOrdersRequest request = new ListOrdersRequest(
        "customer-123", "INVALID_STATUS", null, null, 0, 20);

    // When & Then
    assertThatThrownBy(() -> listOrdersUseCase.execute(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid order status: INVALID_STATUS");
  }

  @Test
  @DisplayName("Should return empty page when no orders found")
  void execute_WhenNoOrdersFound_ShouldReturnEmptyPage() {
    // Given
    String customerId = "customer-123";
    ListOrdersRequest request = ListOrdersRequest.withDefaults(customerId);
    
    CustomerId customerIdObj = CustomerId.from(customerId);
    
    Page<Order> emptyPage = new PageImpl<>(List.of());
    
    when(orderRepository.findByCustomerId(eq(customerIdObj), any(Pageable.class)))
        .thenReturn(emptyPage);

    // When
    Page<OrderSummaryResponse> result = listOrdersUseCase.execute(request);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).isEmpty();
    assertThat(result.getTotalElements()).isZero();
  }

  private Order createTestOrder(CustomerId customerId, String productId, 
                               int quantity, BigDecimal unitPrice) {
    OrderItem orderItem = OrderItem.create(
        ProductId.from(productId),
        "Test Product",
        quantity,
        Money.brl(unitPrice)
    );
    
    return Order.create(customerId, List.of(orderItem));
  }
}