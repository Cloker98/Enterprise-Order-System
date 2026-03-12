package com.enterprise.order.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for Order aggregate root.
 * 
 * Following TDD approach - tests first, implementation second.
 * Task: T003 - Implement Order Aggregate Root
 */
class OrderTest {

    @Test
    void create_WhenValidData_ShouldCreatePendingOrder() {
        // Given
        CustomerId customerId = CustomerId.from("customer-123");
        ProductId productId = ProductId.from("product-456");
        Money unitPrice = Money.brl(999.99);
        OrderItem orderItem = OrderItem.create(productId, "iPhone 15", 2, unitPrice);
        List<OrderItem> items = List.of(orderItem);
        Money expectedTotal = Money.brl(1999.98);
        
        // When
        Order order = Order.create(customerId, items);
        
        // Then
        assertThat(order).isNotNull();
        assertThat(order.getId()).isNotNull();
        assertThat(order.getCustomerId()).isEqualTo(customerId);
        assertThat(order.getItems()).hasSize(1);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.getTotalAmount()).isEqualTo(expectedTotal);
        assertThat(order.getCreatedAt()).isNotNull();
        assertThat(order.getUpdatedAt()).isNotNull();
        assertThat(order.getCancellationReason()).isNull();
    }

    @Test
    void create_WhenNullCustomerId_ShouldThrowException() {
        // Given
        ProductId productId = ProductId.from("product-456");
        Money unitPrice = Money.brl(999.99);
        OrderItem orderItem = OrderItem.create(productId, "iPhone 15", 2, unitPrice);
        List<OrderItem> items = List.of(orderItem);
        
        // When & Then
        assertThatThrownBy(() -> Order.create(null, items))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("CustomerId cannot be null");
    }

    @Test
    void create_WhenNullItems_ShouldThrowException() {
        // Given
        CustomerId customerId = CustomerId.from("customer-123");
        
        // When & Then
        assertThatThrownBy(() -> Order.create(customerId, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Items cannot be null or empty");
    }

    @Test
    void create_WhenEmptyItems_ShouldThrowException() {
        // Given
        CustomerId customerId = CustomerId.from("customer-123");
        List<OrderItem> emptyItems = List.of();
        
        // When & Then
        assertThatThrownBy(() -> Order.create(customerId, emptyItems))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Items cannot be null or empty");
    }

    @Test
    void create_WhenMultipleItems_ShouldCalculateTotalCorrectly() {
        // Given
        CustomerId customerId = CustomerId.from("customer-123");
        ProductId productId1 = ProductId.from("product-1");
        ProductId productId2 = ProductId.from("product-2");
        Money unitPrice1 = Money.brl(999.99);
        Money unitPrice2 = Money.brl(299.99);
        OrderItem orderItem1 = OrderItem.create(productId1, "iPhone 15", 2, unitPrice1);
        OrderItem orderItem2 = OrderItem.create(productId2, "AirPods", 1, unitPrice2);
        List<OrderItem> items = List.of(orderItem1, orderItem2);
        Money expectedTotal = Money.brl(2299.97);
        
        // When
        Order order = Order.create(customerId, items);
        
        // Then
        assertThat(order.getTotalAmount()).isEqualTo(expectedTotal);
    }

    @Test
    void confirm_WhenPending_ShouldUpdateStatusToConfirmed() {
        // Given
        Order order = createTestOrder();
        LocalDateTime originalUpdatedAt = order.getUpdatedAt();
        
        // When
        order.confirm();
        
        // Then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(order.getUpdatedAt()).isAfter(originalUpdatedAt);
    }

    @Test
    void confirm_WhenAlreadyConfirmed_ShouldThrowException() {
        // Given
        Order order = createTestOrder();
        order.confirm();
        
        // When & Then
        assertThatThrownBy(order::confirm)
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Cannot transition from CONFIRMED to CONFIRMED");
    }

    @Test
    void confirm_WhenCancelled_ShouldThrowException() {
        // Given
        Order order = createTestOrder();
        order.cancel("Test cancellation");
        
        // When & Then
        assertThatThrownBy(order::confirm)
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Cannot transition from CANCELLED to CONFIRMED");
    }

    @Test
    void cancel_WhenPending_ShouldUpdateStatusToCancelled() {
        // Given
        Order order = createTestOrder();
        String reason = "Customer requested cancellation";
        LocalDateTime originalUpdatedAt = order.getUpdatedAt();
        
        // When
        order.cancel(reason);
        
        // Then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(order.getCancellationReason()).isEqualTo(reason);
        assertThat(order.getUpdatedAt()).isAfter(originalUpdatedAt);
    }

    @Test
    void cancel_WhenConfirmed_ShouldUpdateStatusToCancelled() {
        // Given
        Order order = createTestOrder();
        order.confirm();
        String reason = "Payment failed";
        
        // When
        order.cancel(reason);
        
        // Then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(order.getCancellationReason()).isEqualTo(reason);
    }

    @Test
    void cancel_WhenAlreadyCancelled_ShouldThrowException() {
        // Given
        Order order = createTestOrder();
        order.cancel("First cancellation");
        
        // When & Then
        assertThatThrownBy(() -> order.cancel("Second cancellation"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Cannot transition from CANCELLED to CANCELLED");
    }

    @Test
    void cancel_WhenDelivered_ShouldThrowException() {
        // Given
        Order order = createTestOrder();
        order.confirm();
        // Simulate delivery (would be done by external process)
        order.updateStatus(OrderStatus.DELIVERED);
        
        // When & Then
        assertThatThrownBy(() -> order.cancel("Too late"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Cannot transition from DELIVERED to CANCELLED");
    }

    @Test
    void cancel_WhenNullReason_ShouldThrowException() {
        // Given
        Order order = createTestOrder();
        
        // When & Then
        assertThatThrownBy(() -> order.cancel(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Cancellation reason cannot be null or empty");
    }

    @Test
    void cancel_WhenEmptyReason_ShouldThrowException() {
        // Given
        Order order = createTestOrder();
        
        // When & Then
        assertThatThrownBy(() -> order.cancel(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Cancellation reason cannot be null or empty");
    }

    @Test
    void calculateTotal_ShouldSumAllItemTotals() {
        // Given
        CustomerId customerId = CustomerId.from("customer-123");
        ProductId productId1 = ProductId.from("product-1");
        ProductId productId2 = ProductId.from("product-2");
        ProductId productId3 = ProductId.from("product-3");
        Money unitPrice1 = Money.brl(999.99);
        Money unitPrice2 = Money.brl(299.99);
        Money unitPrice3 = Money.brl(49.99);
        OrderItem orderItem1 = OrderItem.create(productId1, "iPhone 15", 2, unitPrice1);
        OrderItem orderItem2 = OrderItem.create(productId2, "AirPods", 3, unitPrice2);
        OrderItem orderItem3 = OrderItem.create(productId3, "Case", 1, unitPrice3);
        List<OrderItem> items = List.of(orderItem1, orderItem2, orderItem3);
        Order order = Order.create(customerId, items);
        Money expectedTotal = Money.brl(2949.94);
        
        // When
        Money total = order.calculateTotal();
        
        // Then
        // (999.99 * 2) + (299.99 * 3) + (49.99 * 1) = 1999.98 + 899.97 + 49.99 = 2949.94
        assertThat(total).isEqualTo(expectedTotal);
    }

    @Test
    void canBeCancelled_WhenPending_ShouldReturnTrue() {
        // Given
        Order order = createTestOrder();
        
        // When & Then
        assertThat(order.canBeCancelled()).isTrue();
    }

    @Test
    void canBeCancelled_WhenConfirmed_ShouldReturnTrue() {
        // Given
        Order order = createTestOrder();
        order.confirm();
        
        // When & Then
        assertThat(order.canBeCancelled()).isTrue();
    }

    @Test
    void canBeCancelled_WhenCancelled_ShouldReturnFalse() {
        // Given
        Order order = createTestOrder();
        order.cancel("Test cancellation");
        
        // When & Then
        assertThat(order.canBeCancelled()).isFalse();
    }

    @Test
    void canBeCancelled_WhenDelivered_ShouldReturnFalse() {
        // Given
        Order order = createTestOrder();
        order.confirm();
        order.updateStatus(OrderStatus.DELIVERED);
        
        // When & Then
        assertThat(order.canBeCancelled()).isFalse();
    }

    @Test
    void addItem_WhenValidItem_ShouldAddSuccessfully() {
        // Given
        Order order = createTestOrder();
        ProductId productId = ProductId.from("product-2");
        Money unitPrice = Money.brl(299.99);
        OrderItem newItem = OrderItem.create(productId, "AirPods", 1, unitPrice);
        Money expectedTotal = Money.brl(2299.97); // 1999.98 + 299.99
        
        // When
        order.addItem(newItem);
        
        // Then
        assertThat(order.getItems()).hasSize(2);
        assertThat(order.getTotalAmount()).isEqualTo(expectedTotal);
    }

    @Test
    void addItem_WhenNullItem_ShouldThrowException() {
        // Given
        Order order = createTestOrder();
        
        // When & Then
        assertThatThrownBy(() -> order.addItem(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("OrderItem cannot be null");
    }

    @Test
    void addItem_WhenDuplicateProduct_ShouldThrowException() {
        // Given
        Order order = createTestOrder();
        ProductId duplicateProductId = ProductId.from("product-456");
        Money unitPrice = Money.brl(1199.99);
        OrderItem duplicateItem = OrderItem.create(duplicateProductId, "iPhone 15 Pro", 1, unitPrice);
        
        // When & Then
        assertThatThrownBy(() -> order.addItem(duplicateItem))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Product product-456 already exists in this order");
    }

    @Test
    void removeItem_WhenValidProductId_ShouldRemoveSuccessfully() {
        // Given - Create order with multiple items
        CustomerId customerId = CustomerId.from("customer-123");
        ProductId productId1 = ProductId.from("product-456");
        ProductId productId2 = ProductId.from("product-789");
        Money unitPrice1 = Money.brl(999.99);
        Money unitPrice2 = Money.brl(299.99);
        OrderItem orderItem1 = OrderItem.create(productId1, "iPhone 15", 2, unitPrice1);
        OrderItem orderItem2 = OrderItem.create(productId2, "AirPods", 1, unitPrice2);
        List<OrderItem> items = List.of(orderItem1, orderItem2);
        Order order = Order.create(customerId, items);
        ProductId productIdToRemove = ProductId.from("product-456");
        Money expectedTotal = Money.brl(299.99);
        
        // When
        order.removeItem(productIdToRemove);
        
        // Then
        assertThat(order.getItems()).hasSize(1);
        assertThat(order.getTotalAmount()).isEqualTo(expectedTotal);
    }

    @Test
    void removeItem_WhenProductNotFound_ShouldThrowException() {
        // Given
        Order order = createTestOrder();
        ProductId nonExistentProductId = ProductId.from("product-999");
        
        // When & Then
        assertThatThrownBy(() -> order.removeItem(nonExistentProductId))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Product product-999 not found in this order");
    }

    @Test
    void removeItem_WhenLastItem_ShouldThrowException() {
        // Given
        Order order = createTestOrder();
        ProductId productId = ProductId.from("product-456");
        
        // When & Then
        assertThatThrownBy(() -> order.removeItem(productId))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Cannot remove last item from order");
    }

    @Test
    void reconstitute_WhenValidSnapshot_ShouldReconstituteOrder() {
        // Given
        OrderId orderId = OrderId.generate();
        CustomerId customerId = CustomerId.from("customer-123");
        ProductId productId = ProductId.from("product-456");
        Money unitPrice = Money.brl(999.99);
        OrderItem orderItem = OrderItem.create(productId, "iPhone 15", 2, unitPrice);
        List<OrderItem> items = List.of(orderItem);
        Money totalAmount = Money.brl(1999.98);
        OrderStatus status = OrderStatus.CONFIRMED;
        String cancellationReason = null;
        LocalDateTime createdAt = LocalDateTime.now().minusHours(1);
        LocalDateTime updatedAt = LocalDateTime.now();
        
        OrderSnapshot snapshot = new OrderSnapshot(
            orderId, customerId, items, totalAmount, status, cancellationReason, createdAt, updatedAt
        );
        
        // When
        Order order = Order.reconstitute(snapshot);
        
        // Then
        assertThat(order.getId()).isEqualTo(orderId);
        assertThat(order.getCustomerId()).isEqualTo(customerId);
        assertThat(order.getItems()).hasSize(1);
        assertThat(order.getTotalAmount()).isEqualTo(totalAmount);
        assertThat(order.getStatus()).isEqualTo(status);
        assertThat(order.getCancellationReason()).isEqualTo(cancellationReason);
        assertThat(order.getCreatedAt()).isEqualTo(createdAt);
        assertThat(order.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    void toSnapshot_ShouldCreateSnapshotWithCurrentState() {
        // Given
        Order order = createTestOrder();
        order.confirm();
        
        // When
        OrderSnapshot snapshot = order.toSnapshot();
        
        // Then
        assertThat(snapshot.id()).isEqualTo(order.getId());
        assertThat(snapshot.customerId()).isEqualTo(order.getCustomerId());
        assertThat(snapshot.items()).isEqualTo(order.getItems());
        assertThat(snapshot.totalAmount()).isEqualTo(order.getTotalAmount());
        assertThat(snapshot.status()).isEqualTo(order.getStatus());
        assertThat(snapshot.cancellationReason()).isEqualTo(order.getCancellationReason());
        assertThat(snapshot.createdAt()).isEqualTo(order.getCreatedAt());
        assertThat(snapshot.updatedAt()).isEqualTo(order.getUpdatedAt());
    }

    private Order createTestOrder() {
        CustomerId customerId = CustomerId.from("customer-123");
        ProductId productId = ProductId.from("product-456");
        Money unitPrice = Money.brl(999.99);
        OrderItem orderItem = OrderItem.create(productId, "iPhone 15", 2, unitPrice);
        List<OrderItem> items = List.of(orderItem);
        return Order.create(customerId, items);
    }
}