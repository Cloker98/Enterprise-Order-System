package com.enterprise.order.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for CustomerId value object.
 * 
 * Following TDD approach - tests first, implementation second.
 * Task: T001 - Implement Value Objects
 */
class CustomerIdTest {

    @Test
    void from_WhenValidString_ShouldCreateCustomerId() {
        // Given
        String validId = "customer-123";
        
        // When
        CustomerId customerId = CustomerId.from(validId);
        
        // Then
        assertThat(customerId).isNotNull();
        assertThat(customerId.value()).isEqualTo(validId);
    }

    @Test
    void from_WhenValidUuid_ShouldCreateCustomerId() {
        // Given
        String validUuid = "123e4567-e89b-12d3-a456-426614174000";
        
        // When
        CustomerId customerId = CustomerId.from(validUuid);
        
        // Then
        assertThat(customerId).isNotNull();
        assertThat(customerId.value()).isEqualTo(validUuid);
    }

    @Test
    void from_WhenNullString_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> CustomerId.from(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("CustomerId cannot be null or empty");
    }

    @Test
    void from_WhenEmptyString_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> CustomerId.from(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("CustomerId cannot be null or empty");
    }

    @Test
    void from_WhenBlankString_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> CustomerId.from("   "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("CustomerId cannot be null or empty");
    }

    @Test
    void from_WhenTooShort_ShouldThrowException() {
        // Given
        String tooShort = "ab";
        
        // When & Then
        assertThatThrownBy(() -> CustomerId.from(tooShort))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("CustomerId must be between 3 and 100 characters");
    }

    @Test
    void from_WhenTooLong_ShouldThrowException() {
        // Given
        String tooLong = "a".repeat(101);
        
        // When & Then
        assertThatThrownBy(() -> CustomerId.from(tooLong))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("CustomerId must be between 3 and 100 characters");
    }

    @Test
    void from_WhenMinimumLength_ShouldCreateCustomerId() {
        // Given
        String minimumLength = "abc";
        
        // When
        CustomerId customerId = CustomerId.from(minimumLength);
        
        // Then
        assertThat(customerId.value()).isEqualTo(minimumLength);
    }

    @Test
    void from_WhenMaximumLength_ShouldCreateCustomerId() {
        // Given
        String maximumLength = "a".repeat(100);
        
        // When
        CustomerId customerId = CustomerId.from(maximumLength);
        
        // Then
        assertThat(customerId.value()).isEqualTo(maximumLength);
    }

    @Test
    void equals_WhenSameValue_ShouldReturnTrue() {
        // Given
        String id = "customer-123";
        CustomerId customerId1 = CustomerId.from(id);
        CustomerId customerId2 = CustomerId.from(id);
        
        // When & Then
        assertThat(customerId1).isEqualTo(customerId2);
        assertThat(customerId1.hashCode()).isEqualTo(customerId2.hashCode());
    }

    @Test
    void equals_WhenDifferentValue_ShouldReturnFalse() {
        // Given
        CustomerId customerId1 = CustomerId.from("customer-123");
        CustomerId customerId2 = CustomerId.from("customer-456");
        
        // When & Then
        assertThat(customerId1).isNotEqualTo(customerId2);
    }

    @Test
    void toString_ShouldReturnValue() {
        // Given
        String id = "customer-123";
        CustomerId customerId = CustomerId.from(id);
        
        // When & Then
        assertThat(customerId.toString()).isEqualTo(id);
    }
}