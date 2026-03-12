package com.enterprise.order.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for OrderStatus enum.
 */
class OrderStatusTest {

    @Test
    void canTransitionTo_FromPending_ShouldAllowConfirmedAndCancelled() {
        // Given
        OrderStatus pending = OrderStatus.PENDING;
        
        // When & Then
        assertThat(pending.canTransitionTo(OrderStatus.CONFIRMED)).isTrue();
        assertThat(pending.canTransitionTo(OrderStatus.CANCELLED)).isTrue();
        assertThat(pending.canTransitionTo(OrderStatus.DELIVERED)).isFalse();
        assertThat(pending.canTransitionTo(OrderStatus.PENDING)).isFalse();
    }

    @Test
    void canTransitionTo_FromConfirmed_ShouldAllowCancelledAndDelivered() {
        // Given
        OrderStatus confirmed = OrderStatus.CONFIRMED;
        
        // When & Then
        assertThat(confirmed.canTransitionTo(OrderStatus.CANCELLED)).isTrue();
        assertThat(confirmed.canTransitionTo(OrderStatus.DELIVERED)).isTrue();
        assertThat(confirmed.canTransitionTo(OrderStatus.PENDING)).isFalse();
        assertThat(confirmed.canTransitionTo(OrderStatus.CONFIRMED)).isFalse();
    }

    @Test
    void canTransitionTo_FromCancelled_ShouldNotAllowAnyTransition() {
        // Given
        OrderStatus cancelled = OrderStatus.CANCELLED;
        
        // When & Then
        assertThat(cancelled.canTransitionTo(OrderStatus.PENDING)).isFalse();
        assertThat(cancelled.canTransitionTo(OrderStatus.CONFIRMED)).isFalse();
        assertThat(cancelled.canTransitionTo(OrderStatus.DELIVERED)).isFalse();
        assertThat(cancelled.canTransitionTo(OrderStatus.CANCELLED)).isFalse();
    }

    @Test
    void canTransitionTo_FromDelivered_ShouldNotAllowAnyTransition() {
        // Given
        OrderStatus delivered = OrderStatus.DELIVERED;
        
        // When & Then
        assertThat(delivered.canTransitionTo(OrderStatus.PENDING)).isFalse();
        assertThat(delivered.canTransitionTo(OrderStatus.CONFIRMED)).isFalse();
        assertThat(delivered.canTransitionTo(OrderStatus.CANCELLED)).isFalse();
        assertThat(delivered.canTransitionTo(OrderStatus.DELIVERED)).isFalse();
    }
}