package com.enterprise.order.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for ProductId value object.
 * 
 * Following TDD approach - tests first, implementation second.
 * Task: T001 - Implement Value Objects (ProductId for Order Service)
 */
class ProductIdTest {

    @Test
    void from_WhenValidString_ShouldCreateProductId() {
        // Given
        String validId = "product-123";
        
        // When
        ProductId productId = ProductId.from(validId);
        
        // Then
        assertThat(productId).isNotNull();
        assertThat(productId.value()).isEqualTo(validId);
    }

    @Test
    void from_WhenValidUuid_ShouldCreateProductId() {
        // Given
        String validUuid = "123e4567-e89b-12d3-a456-426614174000";
        
        // When
        ProductId productId = ProductId.from(validUuid);
        
        // Then
        assertThat(productId).isNotNull();
        assertThat(productId.value()).isEqualTo(validUuid);
    }

    @Test
    void from_WhenNullString_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> ProductId.from(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("ProductId cannot be null or empty");
    }

    @Test
    void from_WhenEmptyString_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> ProductId.from(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("ProductId cannot be null or empty");
    }

    @Test
    void from_WhenBlankString_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> ProductId.from("   "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("ProductId cannot be null or empty");
    }

    @Test
    void from_WhenTooShort_ShouldThrowException() {
        // Given
        String tooShort = "ab";
        
        // When & Then
        assertThatThrownBy(() -> ProductId.from(tooShort))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("ProductId must be between 3 and 100 characters");
    }

    @Test
    void from_WhenTooLong_ShouldThrowException() {
        // Given
        String tooLong = "a".repeat(101);
        
        // When & Then
        assertThatThrownBy(() -> ProductId.from(tooLong))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("ProductId must be between 3 and 100 characters");
    }

    @Test
    void from_WhenMinimumLength_ShouldCreateProductId() {
        // Given
        String minimumLength = "abc";
        
        // When
        ProductId productId = ProductId.from(minimumLength);
        
        // Then
        assertThat(productId.value()).isEqualTo(minimumLength);
    }

    @Test
    void from_WhenMaximumLength_ShouldCreateProductId() {
        // Given
        String maximumLength = "a".repeat(100);
        
        // When
        ProductId productId = ProductId.from(maximumLength);
        
        // Then
        assertThat(productId.value()).isEqualTo(maximumLength);
    }

    @Test
    void equals_WhenSameValue_ShouldReturnTrue() {
        // Given
        String id = "product-123";
        ProductId productId1 = ProductId.from(id);
        ProductId productId2 = ProductId.from(id);
        
        // When & Then
        assertThat(productId1).isEqualTo(productId2);
        assertThat(productId1.hashCode()).isEqualTo(productId2.hashCode());
    }

    @Test
    void equals_WhenDifferentValue_ShouldReturnFalse() {
        // Given
        ProductId productId1 = ProductId.from("product-123");
        ProductId productId2 = ProductId.from("product-456");
        
        // When & Then
        assertThat(productId1).isNotEqualTo(productId2);
    }

    @Test
    void toString_ShouldReturnValue() {
        // Given
        String id = "product-123";
        ProductId productId = ProductId.from(id);
        
        // When & Then
        assertThat(productId.toString()).isEqualTo(id);
    }
}