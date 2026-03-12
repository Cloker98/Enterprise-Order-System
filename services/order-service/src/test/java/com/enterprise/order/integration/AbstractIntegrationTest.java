package com.enterprise.order.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base class for integration tests with Testcontainers.
 *
 * <p>Provides shared infrastructure containers (PostgreSQL, RabbitMQ)
 * and common configuration for integration testing.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
public abstract class AbstractIntegrationTest {

  @Container
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
      .withDatabaseName("order_test_db")
      .withUsername("test_user")
      .withPassword("test_pass")
      .withReuse(true);

  @Container
  static RabbitMQContainer rabbitmq = new RabbitMQContainer("rabbitmq:3.13-management-alpine")
      .withReuse(true);

  /**
   * Configures dynamic properties for test containers.
   *
   * @param registry the dynamic property registry
   */
  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    // PostgreSQL configuration
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");

    // RabbitMQ configuration
    registry.add("spring.rabbitmq.host", rabbitmq::getHost);
    registry.add("spring.rabbitmq.port", rabbitmq::getAmqpPort);
    registry.add("spring.rabbitmq.username", () -> "guest");
    registry.add("spring.rabbitmq.password", () -> "guest");

    // Disable Redis for tests (use in-memory cache)
    registry.add("spring.cache.type", () -> "simple");
    
    // Disable security for tests
    registry.add("spring.security.enabled", () -> "false");
    
    // External service URLs (will be mocked)
    registry.add("external.services.product-service.url", () -> "http://localhost:8081/api/v1");
    registry.add("external.services.payment-service.url", () -> "http://localhost:8083/api/v1");
  }
}