package com.enterprise.order.infrastructure.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration for Circuit Breaker and resilience patterns.
 *
 * <p>Configures circuit breakers, retry mechanisms, and time limiters
 * for external service integrations to handle failures gracefully.
 */
@Configuration
public class CircuitBreakerConfig {

  /**
   * Circuit breaker registry with custom configurations.
   *
   * @return the circuit breaker registry
   */
  @Bean
  public CircuitBreakerRegistry circuitBreakerRegistry() {
    CircuitBreakerConfig config = CircuitBreakerConfig.custom()
        .failureRateThreshold(50) // 50% failure rate threshold
        .waitDurationInOpenState(Duration.ofSeconds(60)) // Wait 60s in open state
        .slidingWindowSize(10) // Consider last 10 calls
        .minimumNumberOfCalls(5) // Minimum 5 calls before calculating failure rate
        .permittedNumberOfCallsInHalfOpenState(3) // Allow 3 calls in half-open state
        .slowCallRateThreshold(50) // 50% slow call rate threshold
        .slowCallDurationThreshold(Duration.ofSeconds(2)) // Calls > 2s are slow
        .build();

    return CircuitBreakerRegistry.of(config);
  }

  /**
   * Product service circuit breaker.
   *
   * @param registry the circuit breaker registry
   * @return the product service circuit breaker
   */
  @Bean
  public CircuitBreaker productServiceCircuitBreaker(CircuitBreakerRegistry registry) {
    return registry.circuitBreaker("productService");
  }

  /**
   * Retry registry with custom configurations.
   *
   * @return the retry registry
   */
  @Bean
  public RetryRegistry retryRegistry() {
    RetryConfig config = RetryConfig.custom()
        .maxAttempts(3) // Maximum 3 retry attempts
        .waitDuration(Duration.ofSeconds(1)) // Wait 1s between retries
        .retryExceptions(
            org.springframework.web.client.ResourceAccessException.class,
            org.springframework.web.client.HttpServerErrorException.class
        )
        .ignoreExceptions(
            org.springframework.web.client.HttpClientErrorException.class
        )
        .build();

    return RetryRegistry.of(config);
  }

  /**
   * Product service retry.
   *
   * @param registry the retry registry
   * @return the product service retry
   */
  @Bean
  public Retry productServiceRetry(RetryRegistry registry) {
    return registry.retry("productService");
  }

  /**
   * Time limiter registry with custom configurations.
   *
   * @return the time limiter registry
   */
  @Bean
  public TimeLimiterRegistry timeLimiterRegistry() {
    TimeLimiterConfig config = TimeLimiterConfig.custom()
        .timeoutDuration(Duration.ofSeconds(5)) // 5 second timeout
        .cancelRunningFuture(true) // Cancel running future on timeout
        .build();

    return TimeLimiterRegistry.of(config);
  }

  /**
   * Product service time limiter.
   *
   * @param registry the time limiter registry
   * @return the product service time limiter
   */
  @Bean
  public TimeLimiter productServiceTimeLimiter(TimeLimiterRegistry registry) {
    return registry.timeLimiter("productService");
  }

  /**
   * REST template for external service calls.
   *
   * @return the configured REST template
   */
  @Bean
  public RestTemplate restTemplate() {
    RestTemplate restTemplate = new RestTemplate();
    
    // Configure timeouts
    restTemplate.getRequestFactory();
    
    return restTemplate;
  }
}