package com.enterprise.order.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for OrderItem entity.
 * 
 * Following TDD approach - tests first, implementation second.
 * Task: T002 - Implement OrderItem Entity
 */
class OrderItemTest {

    @Test
    void create_WhenValidData_ShouldCreateOrderItem() {
        // Given
        ProductId productId = ProductId.from("product-123");
        String productName = "iPhone 15";
        int quantity = 2;
        Money unitPrice = Money.brl(999.99);
        
        // When
        OrderItem orderItem = OrderItem.create(productId, productName, quantity, unitPrice);
        
        // Then
        assertThat(orderItem).isNotNull();
        assertThat(orderItem.getProductId()).isEqualTo(productId);
        assertThat(orderItem.getProductName()).isEqualTo(productName);
        assertThat(orderItem.getQuantity()).isEqualTo(quantity);
        assertThat(orderItem.getUnitPrice()).isEqualTo(unitPrice);
        assertThat(orderItem.getTotalPrice()).isEqualTo(Money.brl(1999.98));
    }

    @Test
    void create_WhenNullProductId_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> OrderItem.create(null, "iPhone 15", 2, Money.brl(999.99)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("ProductId cannot be null");
    }

    @Test
    void create_WhenNullProductName_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> OrderItem.create(ProductId.from("product-123"), null, 2, Money.brl(999.99)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Product name cannot be null or empty");
    }

    @Test
    void create_WhenEmptyProductName_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> OrderItem.create(ProductId.from("product-123"), "", 2, Money.brl(999.99)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Product name cannot be null or empty");
    }

    @Test
    void create_WhenBlankProductName_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> OrderItem.create(ProductId.from("product-123"), "   ", 2, Money.brl(999.99)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Product name cannot be null or empty");
    }

    @Test
    void create_WhenZeroQuantity_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> OrderItem.create(ProductId.from("product-123"), "iPhone 15", 0, Money.brl(999.99)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Quantity must be greater than 0");
    }

    @Test
    void create_WhenNegativeQuantity_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> OrderItem.create(ProductId.from("product-123"), "iPhone 15", -1, Money.brl(999.99)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Quantity must be greater than 0");
    }

    @Test
    void create_WhenNullUnitPrice_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> OrderItem.create(ProductId.from("product-123"), "iPhone 15", 2, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Unit price cannot be null");
    }

    @Test
    void create_WhenProductNameTooLong_ShouldThrowException() {
        // Given
        String tooLongName = "a".repeat(256);
        
        // When & Then
        assertThatThrownBy(() -> OrderItem.create(ProductId.from("product-123"), tooLongName, 2, Money.brl(999.99)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Product name cannot exceed 255 characters");
    }

    @Test
    void create_WhenProductNameMaxLength_ShouldCreateOrderItem() {
        // Given
        String maxLengthName = "a".repeat(255);
        
        // When
        OrderItem orderItem = OrderItem.create(ProductId.from("product-123"), maxLengthName, 1, Money.brl(100.00));
        
        // Then
        assertThat(orderItem.getProductName()).isEqualTo(maxLengthName);
    }

    @Test
    void calculateTotal_ShouldMultiplyQuantityByUnitPrice() {
        // Given
        OrderItem orderItem = OrderItem.create(
            ProductId.from("product-123"), 
            "iPhone 15", 
            3, 
            Money.brl(999.99)
        );
        
        // When
        Money total = orderItem.calculateTotal();
        
        // Then
        assertThat(total).isEqualTo(Money.brl(2999.97));
    }

    @Test
    void calculateTotal_WhenQuantityOne_ShouldReturnUnitPrice() {
        // Given
        Money unitPrice = Money.brl(999.99);
        OrderItem orderItem = OrderItem.create(
            ProductId.from("product-123"), 
            "iPhone 15", 
            1, 
            unitPrice
        );
        
        // When
        Money total = orderItem.calculateTotal();
        
        // Then
        assertThat(total).isEqualTo(unitPrice);
    }

    @Test
    void updateQuantity_WhenValidQuantity_ShouldUpdateSuccessfully() {
        // Given
        OrderItem orderItem = OrderItem.create(
            ProductId.from("product-123"), 
            "iPhone 15", 
            2, 
            Money.brl(999.99)
        );
        
        // When
        orderItem.updateQuantity(5);
        
        // Then
        assertThat(orderItem.getQuantity()).isEqualTo(5);
        assertThat(orderItem.getTotalPrice()).isEqualTo(Money.brl(4999.95));
    }

    @Test
    void updateQuantity_WhenZeroQuantity_ShouldThrowException() {
        // Given
        OrderItem orderItem = OrderItem.create(
            ProductId.from("product-123"), 
            "iPhone 15", 
            2, 
            Money.brl(999.99)
        );
        
        // When & Then
        assertThatThrownBy(() -> orderItem.updateQuantity(0))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Quantity must be greater than 0");
    }

    @Test
    void updateQuantity_WhenNegativeQuantity_ShouldThrowException() {
        // Given
        OrderItem orderItem = OrderItem.create(
            ProductId.from("product-123"), 
            "iPhone 15", 
            2, 
            Money.brl(999.99)
        );
        
        // When & Then
        assertThatThrownBy(() -> orderItem.updateQuantity(-1))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Quantity must be greater than 0");
    }

    @Test
    void getTotalPrice_ShouldReturnCalculatedTotal() {
        // Given
        OrderItem orderItem = OrderItem.create(
            ProductId.from("product-123"), 
            "iPhone 15", 
            4, 
            Money.brl(250.00)
        );
        
        // When
        Money totalPrice = orderItem.getTotalPrice();
        
        // Then
        assertThat(totalPrice).isEqualTo(Money.brl(1000.00));
    }

    @Test
    void equals_WhenSameProductId_ShouldReturnTrue() {
        // Given
        ProductId productId = ProductId.from("product-123");
        OrderItem orderItem1 = OrderItem.create(productId, "iPhone 15", 2, Money.brl(999.99));
        OrderItem orderItem2 = OrderItem.create(productId, "iPhone 15 Pro", 3, Money.brl(1199.99));
        
        // When & Then
        assertThat(orderItem1).isEqualTo(orderItem2);
        assertThat(orderItem1.hashCode()).isEqualTo(orderItem2.hashCode());
    }

    @Test
    void equals_WhenDifferentProductId_ShouldReturnFalse() {
        // Given
        OrderItem orderItem1 = OrderItem.create(ProductId.from("product-123"), "iPhone 15", 2, Money.brl(999.99));
        OrderItem orderItem2 = OrderItem.create(ProductId.from("product-456"), "iPhone 15", 2, Money.brl(999.99));
        
        // When & Then
        assertThat(orderItem1).isNotEqualTo(orderItem2);
    }

    @Test
    void toString_ShouldContainRelevantInformation() {
        // Given
        OrderItem orderItem = OrderItem.create(
            ProductId.from("product-123"), 
            "iPhone 15", 
            2, 
            Money.brl(999.99)
        );
        
        // When
        String toString = orderItem.toString();
        
        // Then
        assertThat(toString).contains("product-123");
        assertThat(toString).contains("iPhone 15");
        assertThat(toString).contains("2");
        assertThat(toString).contains("999.99");
    }
}