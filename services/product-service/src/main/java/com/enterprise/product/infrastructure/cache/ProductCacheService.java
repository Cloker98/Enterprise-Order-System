package com.enterprise.product.infrastructure.cache;

import com.enterprise.product.infrastructure.persistence.entity.ProductJpaEntity;
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
 *
 * <p>Caches JPA entities instead of domain objects to avoid serialization issues
 * with complex value objects (Money, ProductId, Currency, etc.).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductCacheService {

  private static final String CACHE_NAME = "products";

  private final CacheManager cacheManager;

  /**
   * Gets product entity from cache.
   *
   * @param id the product ID (as string)
   * @return Optional containing cached entity, empty if cache miss
   */
  public Optional<ProductJpaEntity> get(String id) {
    Cache cache = cacheManager.getCache(CACHE_NAME);
    if (cache != null) {
      ProductJpaEntity cachedEntity = cache.get(id, ProductJpaEntity.class);
      if (cachedEntity != null) {
        log.debug("Cache HIT for product: {}", id);
        return Optional.of(cachedEntity);
      }
    }
    log.debug("Cache MISS for product: {}", id);
    return Optional.empty();
  }

  /**
   * Puts product entity into cache.
   *
   * @param id the product ID (as string)
   * @param entity the entity to cache
   */
  public void put(String id, ProductJpaEntity entity) {
    Cache cache = cacheManager.getCache(CACHE_NAME);
    if (cache != null) {
      cache.put(id, entity);
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
