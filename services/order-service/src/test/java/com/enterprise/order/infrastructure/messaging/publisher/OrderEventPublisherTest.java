package com.enterprise.order.infrastructure.messaging.publisher;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.enterprise.order.application.port.EventPublisherPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderEventPublisher")
class OrderEventPublisherTest {

  @Mock
  private RabbitTemplate rabbitTemplate;

  @Mock
  private ObjectMapper objectMapper;

  private OrderEventPublisher orderEventPublisher;

  private final String orderExchange = "order.events";
  private final String orderCreatedRoutingKey = "order.created";
  private final String orderCancelledRoutingKey = "order.cancelled";
  private final String orderStatusChangedRoutingKey = "order.status.changed";

  @BeforeEach
  void setUp() {
    orderEventPublisher = new OrderEventPublisher(
        rabbitTemplate,
        objectMapper,
        orderExchange,
        orderCreatedRoutingKey,
        orderCancelledRoutingKey,
        orderStatusChangedRoutingKey
    );
  }

  @Test
  @DisplayName("Should publish OrderCreated event successfully")
  void publishOrderCreated_WhenValidEvent_ShouldPublishSuccessfully() throws Exception {
    // Given
    EventPublisherPort.OrderCreatedEvent event = new EventPublisherPort.OrderCreatedEvent(
        "order-123",
        "customer-456",
        "100.00",
        List.of(new EventPublisherPort.OrderItemEvent("product-1", 2, "50.00"))
    );

    when(objectMapper.writeValueAsString(any())).thenReturn("{\"test\":\"json\"}");

    // When
    orderEventPublisher.publishOrderCreated(event);

    // Then
    verify(rabbitTemplate).send(eq(orderExchange), eq(orderCreatedRoutingKey), any(Message.class));
  }

  @Test
  @DisplayName("Should publish OrderCancelled event successfully")
  void publishOrderCancelled_WhenValidEvent_ShouldPublishSuccessfully() throws Exception {
    // Given
    EventPublisherPort.OrderCancelledEvent event = new EventPublisherPort.OrderCancelledEvent(
        "order-123",
        "customer-456",
        "Customer requested cancellation",
        List.of(new EventPublisherPort.OrderItemEvent("product-1", 2, "50.00"))
    );

    when(objectMapper.writeValueAsString(any())).thenReturn("{\"test\":\"json\"}");

    // When
    orderEventPublisher.publishOrderCancelled(event);

    // Then
    verify(rabbitTemplate).send(eq(orderExchange), eq(orderCancelledRoutingKey), any(Message.class));
  }

  @Test
  @DisplayName("Should throw exception when JSON serialization fails")
  void publishOrderCreated_WhenSerializationFails_ShouldThrowException() throws Exception {
    // Given
    EventPublisherPort.OrderCreatedEvent event = new EventPublisherPort.OrderCreatedEvent(
        "order-123",
        "customer-456",
        "100.00",
        List.of()
    );

    when(objectMapper.writeValueAsString(any()))
        .thenThrow(new RuntimeException("Serialization failed"));

    // When & Then
    assertThatThrownBy(() -> orderEventPublisher.publishOrderCreated(event))
        .isInstanceOf(OrderEventPublisher.EventPublishingException.class)
        .hasMessageContaining("Failed to serialize event to JSON");
  }

  @Test
  @DisplayName("Should throw exception when RabbitMQ send fails")
  void publishOrderCreated_WhenRabbitSendFails_ShouldThrowException() throws Exception {
    // Given
    EventPublisherPort.OrderCreatedEvent event = new EventPublisherPort.OrderCreatedEvent(
        "order-123",
        "customer-456",
        "100.00",
        List.of()
    );

    when(objectMapper.writeValueAsString(any())).thenReturn("{\"test\":\"json\"}");
    doThrow(new RuntimeException("RabbitMQ connection failed"))
        .when(rabbitTemplate).send(any(String.class), any(String.class), any(Message.class));

    // When & Then
    assertThatThrownBy(() -> orderEventPublisher.publishOrderCreated(event))
        .isInstanceOf(OrderEventPublisher.EventPublishingException.class)
        .hasMessageContaining("Failed to send message to RabbitMQ");
  }
}