package com.enterprise.product.infrastructure.cache;

import com.enterprise.product.domain.model.Product;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

/**
 * Service for managing Product cache with Redis.
 *
 * <p>Provides explicit cache operations (get, put, evict).
 * TTL is configured in application.yml (5 minutes).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductCacheService {

  private static final String CACHE_NAME = "products";

  private final CacheManager cacheManager;

  /**
   * Gets product from cache.
   *
   * @param id the product ID (as string)
   * @return Optional containing cached product, empty if cache miss
   */
  public Optional<Product> get(String id) {
    Cache cache = cacheManager.getCache(CACHE_NAME);
    if (cache != null) {
      Product cachedProduct = cache.get(id, Product.class);
      if (cachedProduct != null) {
        log.debug("Cache HIT for product: {}", id);
        return Optional.of(cachedProduct);
      }
    }
    log.debug("Cache MISS for product: {}", id);
    return Optional.empty();
  }

  /**
   * Puts product into cache.
   *
   * @param id the product ID (as string)
   * @param product the product to cache
   */
  public void put(String id, Product product) {
    Cache cache = cacheManager.getCache(CACHE_NAME);
    if (cache != null) {
      cache.put(id, product);
      log.debug("Cached product: {}", id);
    }
  }

  /**
   * Evicts product from cache.
   *
   * @param id the product ID (as string)
   */
  public void evict(String id) {
    Cache cache = cacheManager.getCache(CACHE_NAME);
    if (cache != null) {
      cache.evict(id);
      log.debug("Evicted product from cache: {}", id);
    }
  }

  /**
   * Clears all products from cache.
   */
  public void evictAll() {
    Cache cache = cacheManager.getCache(CACHE_NAME);
    if (cache != null) {
      cache.clear();
      log.debug("Cleared all products from cache");
    }
  }
}
