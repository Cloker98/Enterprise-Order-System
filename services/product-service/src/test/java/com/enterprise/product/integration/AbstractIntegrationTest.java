package com.enterprise.product.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Base class for integration tests.
 *
 * <p>Uses existing Docker containers (PostgreSQL and Redis) instead of Testcontainers
 * to avoid Docker connectivity issues on Windows.
 *
 * <p>Prerequisites: 
 * - PostgreSQL running on localhost:5432 (eos-postgresql container)
 * - Redis running on localhost:6379 (eos-redis container)
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public abstract class AbstractIntegrationTest {

  @LocalServerPort
  protected int port;

  /**
   * Configures Spring properties to use existing Docker containers.
   *
   * @param registry the property registry
   */
  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    // PostgreSQL - using existing eos-postgresql container
    registry.add("spring.datasource.url", () -> "jdbc:postgresql://localhost:5432/product_db");
    registry.add("spring.datasource.username", () -> "product_user");
    registry.add("spring.datasource.password", () -> "product_pass");
    
    // Redis - using existing eos-redis container
    registry.add("spring.data.redis.host", () -> "localhost");
    registry.add("spring.data.redis.port", () -> 6379);
    
    // Test-specific configurations
    registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    registry.add("spring.flyway.enabled", () -> "false"); // Disable Flyway for tests
    registry.add("logging.level.com.enterprise.product", () -> "DEBUG");
  }

  /**
   * Gets base URL for API calls.
   *
   * @return base URL with port
   */
  protected String getBaseUrl() {
    return "http://localhost:" + port;
  }
}