package com.enterprise.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Product Service - Microservice for product catalog management.
 *
 * <p>Features:
 * <ul>
 *   <li>CRUD operations for products</li>
 *   <li>Redis cache for improved performance</li>
 *   <li>Stock management (increase/decrease)</li>
 *   <li>RESTful API with Swagger documentation</li>
 * </ul>
 *
 * <p>Architecture: Hexagonal (Ports & Adapters) + DDD
 *
 * @author Enterprise Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableCaching
@EnableTransactionManagement
@EnableJpaRepositories
public class ProductServiceApplication {

  /**
   * Application entry point.
   *
   * @param args command line arguments
   */
  public static void main(String[] args) {
    SpringApplication.run(ProductServiceApplication.class, args);
  }
}
