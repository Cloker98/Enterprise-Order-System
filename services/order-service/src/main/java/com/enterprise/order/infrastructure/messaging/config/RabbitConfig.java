package com.enterprise.order.infrastructure.messaging.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for Order Service.
 *
 * <p>Configures exchanges, queues, bindings, and message converters
 * for reliable event-driven communication between microservices.
 */
@Configuration
public class RabbitConfig {

  @Value("${rabbitmq.exchanges.order-events}")
  private String orderEventsExchange;

  @Value("${rabbitmq.exchanges.payment-events}")
  private String paymentEventsExchange;

  @Value("${rabbitmq.queues.order-created}")
  private String orderCreatedQueue;

  @Value("${rabbitmq.queues.order-cancelled}")
  private String orderCancelledQueue;

  @Value("${rabbitmq.queues.payment-processed}")
  private String paymentProcessedQueue;

  @Value("${rabbitmq.queues.payment-failed}")
  private String paymentFailedQueue;

  @Value("${rabbitmq.routing-keys.order-created}")
  private String orderCreatedRoutingKey;

  @Value("${rabbitmq.routing-keys.order-cancelled}")
  private String orderCancelledRoutingKey;

  @Value("${rabbitmq.routing-keys.payment-processed}")
  private String paymentProcessedRoutingKey;

  @Value("${rabbitmq.routing-keys.payment-failed}")
  private String paymentFailedRoutingKey;

  /**
   * Order events exchange for publishing order-related events.
   *
   * @return the order events exchange
   */
  @Bean
  public DirectExchange orderEventsExchange() {
    return new DirectExchange(orderEventsExchange, true, false);
  }

  /**
   * Payment events exchange for consuming payment-related events.
   *
   * @return the payment events exchange
   */
  @Bean
  public DirectExchange paymentEventsExchange() {
    return new DirectExchange(paymentEventsExchange, true, false);
  }

  /**
   * Queue for order created events.
   *
   * @return the order created queue
   */
  @Bean
  public Queue orderCreatedQueue() {
    return QueueBuilder.durable(orderCreatedQueue)
        .withArgument("x-dead-letter-exchange", orderEventsExchange + ".dlx")
        .withArgument("x-dead-letter-routing-key", orderCreatedRoutingKey + ".dlq")
        .withArgument("x-message-ttl", 300000) // 5 minutes TTL
        .build();
  }

  /**
   * Queue for order cancelled events.
   *
   * @return the order cancelled queue
   */
  @Bean
  public Queue orderCancelledQueue() {
    return QueueBuilder.durable(orderCancelledQueue)
        .withArgument("x-dead-letter-exchange", orderEventsExchange + ".dlx")
        .withArgument("x-dead-letter-routing-key", orderCancelledRoutingKey + ".dlq")
        .withArgument("x-message-ttl", 300000) // 5 minutes TTL
        .build();
  }

  /**
   * Queue for payment processed events.
   *
   * @return the payment processed queue
   */
  @Bean
  public Queue paymentProcessedQueue() {
    return QueueBuilder.durable(paymentProcessedQueue)
        .withArgument("x-dead-letter-exchange", paymentEventsExchange + ".dlx")
        .withArgument("x-dead-letter-routing-key", paymentProcessedRoutingKey + ".dlq")
        .withArgument("x-message-ttl", 300000) // 5 minutes TTL
        .build();
  }

  /**
   * Queue for payment failed events.
   *
   * @return the payment failed queue
   */
  @Bean
  public Queue paymentFailedQueue() {
    return QueueBuilder.durable(paymentFailedQueue)
        .withArgument("x-dead-letter-exchange", paymentEventsExchange + ".dlx")
        .withArgument("x-dead-letter-routing-key", paymentFailedRoutingKey + ".dlq")
        .withArgument("x-message-ttl", 300000) // 5 minutes TTL
        .build();
  }

  /**
   * Dead letter queue for order created events.
   *
   * @return the order created dead letter queue
   */
  @Bean
  public Queue orderCreatedDeadLetterQueue() {
    return QueueBuilder.durable(orderCreatedQueue + ".dlq").build();
  }

  /**
   * Dead letter queue for order cancelled events.
   *
   * @return the order cancelled dead letter queue
   */
  @Bean
  public Queue orderCancelledDeadLetterQueue() {
    return QueueBuilder.durable(orderCancelledQueue + ".dlq").build();
  }

  /**
   * Dead letter queue for payment processed events.
   *
   * @return the payment processed dead letter queue
   */
  @Bean
  public Queue paymentProcessedDeadLetterQueue() {
    return QueueBuilder.durable(paymentProcessedQueue + ".dlq").build();
  }

  /**
   * Dead letter queue for payment failed events.
   *
   * @return the payment failed dead letter queue
   */
  @Bean
  public Queue paymentFailedDeadLetterQueue() {
    return QueueBuilder.durable(paymentFailedQueue + ".dlq").build();
  }

  /**
   * Dead letter exchange for failed messages.
   *
   * @return the dead letter exchange
   */
  @Bean
  public DirectExchange deadLetterExchange() {
    return new DirectExchange(orderEventsExchange + ".dlx", true, false);
  }

  /**
   * Binding for order created events.
   *
   * @return the order created binding
   */
  @Bean
  public Binding orderCreatedBinding() {
    return BindingBuilder
        .bind(orderCreatedQueue())
        .to(orderEventsExchange())
        .with(orderCreatedRoutingKey);
  }

  /**
   * Binding for order cancelled events.
   *
   * @return the order cancelled binding
   */
  @Bean
  public Binding orderCancelledBinding() {
    return BindingBuilder
        .bind(orderCancelledQueue())
        .to(orderEventsExchange())
        .with(orderCancelledRoutingKey);
  }

  /**
   * Binding for payment processed events.
   *
   * @return the payment processed binding
   */
  @Bean
  public Binding paymentProcessedBinding() {
    return BindingBuilder
        .bind(paymentProcessedQueue())
        .to(paymentEventsExchange())
        .with(paymentProcessedRoutingKey);
  }

  /**
   * Binding for payment failed events.
   *
   * @return the payment failed binding
   */
  @Bean
  public Binding paymentFailedBinding() {
    return BindingBuilder
        .bind(paymentFailedQueue())
        .to(paymentEventsExchange())
        .with(paymentFailedRoutingKey);
  }

  /**
   * Dead letter binding for order created events.
   *
   * @return the order created dead letter binding
   */
  @Bean
  public Binding orderCreatedDeadLetterBinding() {
    return BindingBuilder
        .bind(orderCreatedDeadLetterQueue())
        .to(deadLetterExchange())
        .with(orderCreatedRoutingKey + ".dlq");
  }

  /**
   * Dead letter binding for order cancelled events.
   *
   * @return the order cancelled dead letter binding
   */
  @Bean
  public Binding orderCancelledDeadLetterBinding() {
    return BindingBuilder
        .bind(orderCancelledDeadLetterQueue())
        .to(deadLetterExchange())
        .with(orderCancelledRoutingKey + ".dlq");
  }

  /**
   * Dead letter binding for payment processed events.
   *
   * @return the payment processed dead letter binding
   */
  @Bean
  public Binding paymentProcessedDeadLetterBinding() {
    return BindingBuilder
        .bind(paymentProcessedDeadLetterQueue())
        .to(deadLetterExchange())
        .with(paymentProcessedRoutingKey + ".dlq");
  }

  /**
   * Dead letter binding for payment failed events.
   *
   * @return the payment failed dead letter binding
   */
  @Bean
  public Binding paymentFailedDeadLetterBinding() {
    return BindingBuilder
        .bind(paymentFailedDeadLetterQueue())
        .to(deadLetterExchange())
        .with(paymentFailedRoutingKey + ".dlq");
  }

  /**
   * JSON message converter for RabbitMQ.
   *
   * @return the JSON message converter
   */
  @Bean
  public Jackson2JsonMessageConverter messageConverter() {
    return new Jackson2JsonMessageConverter();
  }

  /**
   * RabbitMQ template with JSON converter.
   *
   * @param connectionFactory the connection factory
   * @return the configured RabbitTemplate
   */
  @Bean
  public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
    RabbitTemplate template = new RabbitTemplate(connectionFactory);
    template.setMessageConverter(messageConverter());
    template.setMandatory(true); // Ensure messages are routed
    template.setConfirmCallback((correlationData, ack, cause) -> {
      if (!ack) {
        // Log failed message delivery
        System.err.println("Message delivery failed: " + cause);
      }
    });
    return template;
  }

  /**
   * Rabbit listener container factory with JSON converter.
   *
   * @param connectionFactory the connection factory
   * @return the configured listener container factory
   */
  @Bean
  public RabbitListenerContainerFactory<SimpleMessageListenerContainer> 
      rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
    SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
    factory.setConnectionFactory(connectionFactory);
    factory.setMessageConverter(messageConverter());
    factory.setConcurrentConsumers(1);
    factory.setMaxConcurrentConsumers(5);
    factory.setDefaultRequeueRejected(false); // Send to DLQ on failure
    return factory;
  }
}