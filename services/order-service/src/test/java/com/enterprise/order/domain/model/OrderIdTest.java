package com.enterprise.order.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for OrderId value object.
 * 
 * Following TDD approach - tests first, implementation second.
 * Task: T001 - Implement Value Objects
 */
class OrderIdTest {

    @Test
    void generate_ShouldCreateValidUUID() {
        // When
        OrderId orderId = OrderId.generate();
        
        // Then
        assertThat(orderId).isNotNull();
        assertThat(orderId.value()).isNotNull();
        assertThat(orderId.value()).hasSize(36); // UUID format: 8-4-4-4-12
        assertThat(orderId.value()).matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
    }

    @Test
    void generate_ShouldCreateUniqueIds() {
        // When
        OrderId orderId1 = OrderId.generate();
        OrderId orderId2 = OrderId.generate();
        
        // Then
        assertThat(orderId1.value()).isNotEqualTo(orderId2.value());
    }

    @Test
    void from_WhenValidString_ShouldCreateOrderId() {
        // Given
        String validUuid = "123e4567-e89b-12d3-a456-426614174000";
        
        // When
        OrderId orderId = OrderId.from(validUuid);
        
        // Then
        assertThat(orderId).isNotNull();
        assertThat(orderId.value()).isEqualTo(validUuid);
    }

    @Test
    void from_WhenNullString_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> OrderId.from(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("OrderId cannot be null or empty");
    }

    @Test
    void from_WhenEmptyString_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> OrderId.from(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("OrderId cannot be null or empty");
    }

    @Test
    void from_WhenBlankString_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> OrderId.from("   "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("OrderId cannot be null or empty");
    }

    @Test
    void from_WhenInvalidUuidFormat_ShouldThrowException() {
        // Given
        String invalidUuid = "invalid-uuid-format";
        
        // When & Then
        assertThatThrownBy(() -> OrderId.from(invalidUuid))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Invalid UUID format: " + invalidUuid);
    }

    @Test
    void equals_WhenSameValue_ShouldReturnTrue() {
        // Given
        String uuid = "123e4567-e89b-12d3-a456-426614174000";
        OrderId orderId1 = OrderId.from(uuid);
        OrderId orderId2 = OrderId.from(uuid);
        
        // When & Then
        assertThat(orderId1)
            .isEqualTo(orderId2)
            .hasSameHashCodeAs(orderId2);
    }

    @Test
    void equals_WhenDifferentValue_ShouldReturnFalse() {
        // Given
        OrderId orderId1 = OrderId.from("123e4567-e89b-12d3-a456-426614174000");
        OrderId orderId2 = OrderId.from("987fcdeb-51a2-43d1-9f12-123456789abc");
        
        // When & Then
        assertThat(orderId1).isNotEqualTo(orderId2);
    }

    @Test
    void toString_ShouldReturnValue() {
        // Given
        String uuid = "123e4567-e89b-12d3-a456-426614174000";
        OrderId orderId = OrderId.from(uuid);
        
        // When & Then
        assertThat(orderId).hasToString(uuid);
    }
}