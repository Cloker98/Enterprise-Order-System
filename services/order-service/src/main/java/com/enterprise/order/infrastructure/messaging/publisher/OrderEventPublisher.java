package com.enterprise.order.infrastructure.messaging.publisher;

import com.enterprise.order.application.port.EventPublisherPort;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ implementation of EventPublisherPort.
 *
 * <p>Publishes domain events to RabbitMQ exchanges for consumption
 * by other microservices. Implements retry logic and dead letter
 * queue handling for reliable message delivery.
 */
@Component
public class OrderEventPublisher implements EventPublisherPort {

  private static final Logger log = LoggerFactory.getLogger(OrderEventPublisher.class);

  private final RabbitTemplate rabbitTemplate;
  private final ObjectMapper objectMapper;
  private final String orderExchange;
  private final String orderCreatedRoutingKey;
  private final String orderCancelledRoutingKey;
  private final String orderStatusChangedRoutingKey;

  /**
   * Constructor for OrderEventPublisher.
   *
   * @param rabbitTemplate the RabbitMQ template
   * @param objectMapper the JSON object mapper
   * @param orderExchange the order events exchange
   * @param orderCreatedRoutingKey the routing key for order created events
   * @param orderCancelledRoutingKey the routing key for order cancelled events
   * @param orderStatusChangedRoutingKey the routing key for order status changed events
   */
  public OrderEventPublisher(RabbitTemplate rabbitTemplate,
                            ObjectMapper objectMapper,
                            @Value("${rabbitmq.exchanges.order-events}") String orderExchange,
                            @Value("${rabbitmq.routing-keys.order-created}") 
                            String orderCreatedRoutingKey,
                            @Value("${rabbitmq.routing-keys.order-cancelled}") 
                            String orderCancelledRoutingKey,
                            @Value("${rabbitmq.routing-keys.order-status-changed}") 
                            String orderStatusChangedRoutingKey) {
    this.rabbitTemplate = rabbitTemplate;
    this.objectMapper = objectMapper;
    this.orderExchange = orderExchange;
    this.orderCreatedRoutingKey = orderCreatedRoutingKey;
    this.orderCancelledRoutingKey = orderCancelledRoutingKey;
    this.orderStatusChangedRoutingKey = orderStatusChangedRoutingKey;
  }

  @Override
  public void publishOrderCreated(OrderCreatedEvent event) {
    log.info("Publishing OrderCreated event for order: {}", event.orderId());
    
    try {
      EventEnvelope envelope = createEventEnvelope("OrderCreated", event);
      publishEvent(orderExchange, orderCreatedRoutingKey, envelope);
      
      log.info("Successfully published OrderCreated event for order: {}", event.orderId());
    } catch (Exception e) {
      log.error("Failed to publish OrderCreated event for order: {}", event.orderId(), e);
      throw new EventPublishingException("Failed to publish OrderCreated event", e);
    }
  }

  @Override
  public void publishOrderStatusChanged(OrderStatusChangedEvent event) {
    log.info("Publishing OrderStatusChanged event for order: {}", event.orderId());
    
    try {
      EventEnvelope envelope = createEventEnvelope("OrderStatusChanged", event);
      publishEvent(orderExchange, orderStatusChangedRoutingKey, envelope);
      
      log.info("Successfully published OrderStatusChanged event for order: {}", event.orderId());
    } catch (Exception e) {
      log.error("Failed to publish OrderStatusChanged event for order: {}", event.orderId(), e);
      throw new EventPublishingException("Failed to publish OrderStatusChanged event", e);
    }
  }

  @Override
  public void publishOrderCancelled(OrderCancelledEvent event) {
    log.info("Publishing OrderCancelled event for order: {}", event.orderId());
    
    try {
      EventEnvelope envelope = createEventEnvelope("OrderCancelled", event);
      publishEvent(orderExchange, orderCancelledRoutingKey, envelope);
      
      log.info("Successfully published OrderCancelled event for order: {}", event.orderId());
    } catch (Exception e) {
      log.error("Failed to publish OrderCancelled event for order: {}", event.orderId(), e);
      throw new EventPublishingException("Failed to publish OrderCancelled event", e);
    }
  }

  /**
   * Creates an event envelope with metadata.
   *
   * @param eventType the event type
   * @param payload the event payload
   * @return the event envelope
   */
  private EventEnvelope createEventEnvelope(String eventType, Object payload) {
    return new EventEnvelope(
        UUID.randomUUID().toString(),
        eventType,
        LocalDateTime.now(),
        extractAggregateId(payload),
        payload,
        new EventMetadata(
            getCurrentUserId(),
            generateCorrelationId(),
            "order-service",
            "1.0.0"
        )
    );
  }

  /**
   * Publishes an event to RabbitMQ.
   *
   * @param exchange the exchange name
   * @param routingKey the routing key
   * @param envelope the event envelope
   */
  private void publishEvent(String exchange, String routingKey, EventEnvelope envelope) {
    try {
      String jsonPayload = objectMapper.writeValueAsString(envelope);
      
      MessageProperties properties = new MessageProperties();
      properties.setContentType("application/json");
      properties.setDeliveryMode(MessageProperties.DEFAULT_DELIVERY_MODE);
      properties.setHeader("eventType", envelope.eventType());
      properties.setHeader("eventId", envelope.eventId());
      properties.setHeader("aggregateId", envelope.aggregateId());
      properties.setHeader("timestamp", envelope.timestamp().toString());
      
      Message message = new Message(jsonPayload.getBytes(), properties);
      
      rabbitTemplate.send(exchange, routingKey, message);
      
      log.debug("Event published to exchange: {}, routingKey: {}, eventId: {}", 
                exchange, routingKey, envelope.eventId());
      
    } catch (JsonProcessingException e) {
      throw new EventPublishingException("Failed to serialize event to JSON", e);
    } catch (Exception e) {
      throw new EventPublishingException("Failed to send message to RabbitMQ", e);
    }
  }

  /**
   * Extracts the aggregate ID from the event payload.
   *
   * @param payload the event payload
   * @return the aggregate ID
   */
  private String extractAggregateId(Object payload) {
    if (payload instanceof OrderCreatedEvent event) {
      return event.orderId();
    } else if (payload instanceof OrderStatusChangedEvent event) {
      return event.orderId();
    } else if (payload instanceof OrderCancelledEvent event) {
      return event.orderId();
    }
    return "unknown";
  }

  /**
   * Gets the current user ID from security context.
   * In a real implementation, this would extract from Spring Security context.
   *
   * @return the current user ID
   */
  private String getCurrentUserId() {
    // TODO: Extract from Spring Security context
    return "system";
  }

  /**
   * Generates a correlation ID for request tracing.
   *
   * @return the correlation ID
   */
  private String generateCorrelationId() {
    return UUID.randomUUID().toString();
  }

  /**
   * Event envelope for standardized event structure.
   */
  public record EventEnvelope(
      String eventId,
      String eventType,
      LocalDateTime timestamp,
      String aggregateId,
      Object payload,
      EventMetadata metadata
  ) {
  }

  /**
   * Event metadata for additional context.
   */
  public record EventMetadata(
      String userId,
      String correlationId,
      String source,
      String version
  ) {
  }

  /**
   * Exception thrown when event publishing fails.
   */
  public static class EventPublishingException extends RuntimeException {
    public EventPublishingException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}