package com.enterprise.order.infrastructure.messaging.consumer;

import com.enterprise.order.application.usecase.ProcessPaymentResultUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ consumer for payment-related events.
 *
 * <p>Processes payment events from the Payment Service to update
 * order status and trigger SAGA compensation if needed.
 */
@Component
public class PaymentEventConsumer {

  private static final Logger log = LoggerFactory.getLogger(PaymentEventConsumer.class);

  private final ProcessPaymentResultUseCase processPaymentResultUseCase;
  private final ObjectMapper objectMapper;

  /**
   * Constructor for PaymentEventConsumer.
   *
   * @param processPaymentResultUseCase the use case for processing payment results
   * @param objectMapper the JSON object mapper
   */
  public PaymentEventConsumer(ProcessPaymentResultUseCase processPaymentResultUseCase,
                             ObjectMapper objectMapper) {
    this.processPaymentResultUseCase = processPaymentResultUseCase;
    this.objectMapper = objectMapper;
  }

  /**
   * Handles payment processed events.
   *
   * @param payload the event payload
   * @param deliveryTag the message delivery tag
   * @param eventId the event ID from headers
   * @param correlationId the correlation ID from headers
   */
  @RabbitListener(queues = "${rabbitmq.queues.payment-processed}")
  public void handlePaymentProcessed(@Payload String payload,
                                   @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
                                   @Header(value = "eventId", required = false) String eventId,
                                   @Header(value = "correlationId", required = false) 
                                   String correlationId) {
    
    log.info("Received PaymentProcessed event - eventId: {}, correlationId: {}", 
             eventId, correlationId);
    
    try {
      PaymentProcessedEvent event = parsePaymentProcessedEvent(payload);
      
      log.info("Processing payment success for order: {}, paymentId: {}", 
               event.orderId(), event.paymentId());
      
      ProcessPaymentResultUseCase.PaymentProcessedRequest request = 
          new ProcessPaymentResultUseCase.PaymentProcessedRequest(
              event.orderId(),
              event.paymentId(),
              event.amount(),
              event.paymentMethod(),
              event.transactionId()
          );
      
      processPaymentResultUseCase.handlePaymentProcessed(request);
      
      log.info("Successfully processed PaymentProcessed event for order: {}", event.orderId());
      
    } catch (Exception e) {
      log.error("Failed to process PaymentProcessed event - eventId: {}, correlationId: {}, " 
                + "deliveryTag: {}", eventId, correlationId, deliveryTag, e);
      
      // In a production system, you might want to:
      // 1. Send to dead letter queue after max retries
      // 2. Implement exponential backoff
      // 3. Alert monitoring systems
      throw new PaymentEventProcessingException("Failed to process PaymentProcessed event", e);
    }
  }

  /**
   * Handles payment failed events.
   *
   * @param payload the event payload
   * @param deliveryTag the message delivery tag
   * @param eventId the event ID from headers
   * @param correlationId the correlation ID from headers
   */
  @RabbitListener(queues = "${rabbitmq.queues.payment-failed}")
  public void handlePaymentFailed(@Payload String payload,
                                @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
                                @Header(value = "eventId", required = false) String eventId,
                                @Header(value = "correlationId", required = false) 
                                String correlationId) {
    
    log.info("Received PaymentFailed event - eventId: {}, correlationId: {}", 
             eventId, correlationId);
    
    try {
      PaymentFailedEvent event = parsePaymentFailedEvent(payload);
      
      log.info("Processing payment failure for order: {}, reason: {}", 
               event.orderId(), event.reason());
      
      ProcessPaymentResultUseCase.PaymentFailedRequest request = 
          new ProcessPaymentResultUseCase.PaymentFailedRequest(
              event.orderId(),
              event.paymentId(),
              event.reason(),
              event.errorCode()
          );
      
      processPaymentResultUseCase.handlePaymentFailed(request);
      
      log.info("Successfully processed PaymentFailed event for order: {}", event.orderId());
      
    } catch (Exception e) {
      log.error("Failed to process PaymentFailed event - eventId: {}, correlationId: {}, " 
                + "deliveryTag: {}", eventId, correlationId, deliveryTag, e);
      
      throw new PaymentEventProcessingException("Failed to process PaymentFailed event", e);
    }
  }

  /**
   * Parses PaymentProcessed event from JSON payload.
   *
   * @param payload the JSON payload
   * @return the parsed event
   */
  private PaymentProcessedEvent parsePaymentProcessedEvent(String payload) {
    try {
      // Parse the event envelope first
      EventEnvelope envelope = objectMapper.readValue(payload, EventEnvelope.class);
      
      // Extract the actual event from the payload
      return objectMapper.convertValue(envelope.payload(), PaymentProcessedEvent.class);
      
    } catch (Exception e) {
      throw new PaymentEventProcessingException("Failed to parse PaymentProcessed event", e);
    }
  }

  /**
   * Parses PaymentFailed event from JSON payload.
   *
   * @param payload the JSON payload
   * @return the parsed event
   */
  private PaymentFailedEvent parsePaymentFailedEvent(String payload) {
    try {
      // Parse the event envelope first
      EventEnvelope envelope = objectMapper.readValue(payload, EventEnvelope.class);
      
      // Extract the actual event from the payload
      return objectMapper.convertValue(envelope.payload(), PaymentFailedEvent.class);
      
    } catch (Exception e) {
      throw new PaymentEventProcessingException("Failed to parse PaymentFailed event", e);
    }
  }

  /**
   * Event envelope for standardized event structure.
   */
  public record EventEnvelope(
      String eventId,
      String eventType,
      String timestamp,
      String aggregateId,
      Object payload,
      Object metadata
  ) {
  }

  /**
   * Payment processed event.
   */
  public record PaymentProcessedEvent(
      String orderId,
      String paymentId,
      String amount,
      String paymentMethod,
      String transactionId
  ) {
  }

  /**
   * Payment failed event.
   */
  public record PaymentFailedEvent(
      String orderId,
      String paymentId,
      String reason,
      String errorCode
  ) {
  }

  /**
   * Exception thrown when payment event processing fails.
   */
  public static class PaymentEventProcessingException extends RuntimeException {
    public PaymentEventProcessingException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}