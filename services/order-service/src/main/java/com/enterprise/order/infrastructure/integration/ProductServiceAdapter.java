package com.enterprise.order.infrastructure.integration;

import com.enterprise.order.application.port.ProductServicePort;
import com.enterprise.order.domain.model.ProductId;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/**
 * Adapter for Product Service integration with circuit breaker pattern.
 *
 * <p>Implements resilience patterns including circuit breaker, retry,
 * and timeout to handle failures gracefully when communicating with
 * the Product Service.
 */
@Component
public class ProductServiceAdapter implements ProductServicePort {

  private static final Logger log = LoggerFactory.getLogger(ProductServiceAdapter.class);
  private static final String CIRCUIT_BREAKER_NAME = "productService";

  private final RestTemplate restTemplate;
  private final String productServiceUrl;

  /**
   * Constructor for ProductServiceAdapter.
   *
   * @param restTemplate the REST template
   * @param productServiceUrl the product service base URL
   */
  public ProductServiceAdapter(RestTemplate restTemplate,
                              @Value("${external.services.product-service.url}") 
                              String productServiceUrl) {
    this.restTemplate = restTemplate;
    this.productServiceUrl = productServiceUrl;
  }

  @Override
  @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "getProductFallback")
  @Retry(name = CIRCUIT_BREAKER_NAME)
  @TimeLimiter(name = CIRCUIT_BREAKER_NAME)
  public ProductInfo getProduct(ProductId productId) {
    log.debug("Getting product information for: {}", productId);
    
    try {
      String url = productServiceUrl + "/products/" + productId.value();
      ProductServiceResponse response = restTemplate.getForObject(url, ProductServiceResponse.class);
      
      if (response == null) {
        throw new ProductNotFoundException("Product not found: " + productId);
      }
      
      log.debug("Successfully retrieved product: {}", productId);
      return new ProductInfo(
          response.id(),
          response.name(),
          response.price(),
          response.stockQuantity()
      );
      
    } catch (HttpClientErrorException.NotFound e) {
      log.debug("Product not found: {}", productId);
      throw new ProductNotFoundException("Product not found: " + productId);
    } catch (HttpServerErrorException | ResourceAccessException e) {
      log.error("Product service unavailable for product: {}", productId, e);
      throw new ProductServiceUnavailableException("Product service unavailable", e);
    } catch (Exception e) {
      log.error("Unexpected error getting product: {}", productId, e);
      throw new ProductServiceUnavailableException("Unexpected error from product service", e);
    }
  }

  @Override
  @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "decreaseStockFallback")
  @Retry(name = CIRCUIT_BREAKER_NAME)
  @TimeLimiter(name = CIRCUIT_BREAKER_NAME)
  public void decreaseStock(ProductId productId, int quantity) {
    log.debug("Decreasing stock for product: {}, quantity: {}", productId, quantity);
    
    try {
      String url = productServiceUrl + "/products/" + productId.value() + "/decrease-stock";
      DecreaseStockRequest request = new DecreaseStockRequest(quantity);
      
      restTemplate.postForObject(url, request, Void.class);
      
      log.debug("Successfully decreased stock for product: {}", productId);
      
    } catch (HttpClientErrorException.NotFound e) {
      log.debug("Product not found for stock decrease: {}", productId);
      throw new ProductNotFoundException("Product not found: " + productId);
    } catch (HttpClientErrorException.Conflict e) {
      log.debug("Insufficient stock for product: {}", productId);
      throw new InsufficientStockException("Insufficient stock for product: " + productId);
    } catch (HttpServerErrorException | ResourceAccessException e) {
      log.error("Product service unavailable for stock decrease: {}", productId, e);
      throw new ProductServiceUnavailableException("Product service unavailable", e);
    } catch (Exception e) {
      log.error("Unexpected error decreasing stock for product: {}", productId, e);
      throw new ProductServiceUnavailableException("Unexpected error from product service", e);
    }
  }

  @Override
  @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "increaseStockFallback")
  @Retry(name = CIRCUIT_BREAKER_NAME)
  @TimeLimiter(name = CIRCUIT_BREAKER_NAME)
  public void increaseStock(ProductId productId, int quantity) {
    log.debug("Increasing stock for product: {}, quantity: {}", productId, quantity);
    
    try {
      String url = productServiceUrl + "/products/" + productId.value() + "/increase-stock";
      IncreaseStockRequest request = new IncreaseStockRequest(quantity);
      
      restTemplate.postForObject(url, request, Void.class);
      
      log.debug("Successfully increased stock for product: {}", productId);
      
    } catch (HttpClientErrorException.NotFound e) {
      log.debug("Product not found for stock increase: {}", productId);
      throw new ProductNotFoundException("Product not found: " + productId);
    } catch (HttpServerErrorException | ResourceAccessException e) {
      log.error("Product service unavailable for stock increase: {}", productId, e);
      throw new ProductServiceUnavailableException("Product service unavailable", e);
    } catch (Exception e) {
      log.error("Unexpected error increasing stock for product: {}", productId, e);
      throw new ProductServiceUnavailableException("Unexpected error from product service", e);
    }
  }

  /**
   * Fallback method for getProduct when circuit breaker is open.
   *
   * @param productId the product ID
   * @param ex the exception that triggered the fallback
   * @return fallback product info
   */
  public ProductInfo getProductFallback(ProductId productId, Exception ex) {
    log.warn("Using fallback for getProduct: {}, reason: {}", productId, ex.getMessage());
    
    // In a real system, this might:
    // 1. Return cached data
    // 2. Return default values
    // 3. Query a backup service
    throw new ProductServiceUnavailableException(
        "Product service is currently unavailable. Please try again later.", ex);
  }

  /**
   * Fallback method for decreaseStock when circuit breaker is open.
   *
   * @param productId the product ID
   * @param quantity the quantity
   * @param ex the exception that triggered the fallback
   */
  public void decreaseStockFallback(ProductId productId, int quantity, Exception ex) {
    log.warn("Using fallback for decreaseStock: {}, quantity: {}, reason: {}", 
             productId, quantity, ex.getMessage());
    
    // In a real system, this might:
    // 1. Queue the operation for later
    // 2. Use eventual consistency
    // 3. Notify administrators
    throw new ProductServiceUnavailableException(
        "Product service is currently unavailable. Stock reservation failed.", ex);
  }

  /**
   * Fallback method for increaseStock when circuit breaker is open.
   *
   * @param productId the product ID
   * @param quantity the quantity
   * @param ex the exception that triggered the fallback
   */
  public void increaseStockFallback(ProductId productId, int quantity, Exception ex) {
    log.warn("Using fallback for increaseStock: {}, quantity: {}, reason: {}", 
             productId, quantity, ex.getMessage());
    
    // In a real system, this might:
    // 1. Queue the operation for later
    // 2. Use eventual consistency
    // 3. Log for manual intervention
    throw new ProductServiceUnavailableException(
        "Product service is currently unavailable. Stock compensation failed.", ex);
  }

  /**
   * Product service response DTO.
   */
  public record ProductServiceResponse(
      String id,
      String name,
      BigDecimal price,
      Integer stockQuantity
  ) {
  }

  /**
   * Decrease stock request DTO.
   */
  public record DecreaseStockRequest(int quantity) {
  }

  /**
   * Increase stock request DTO.
   */
  public record IncreaseStockRequest(int quantity) {
  }
}