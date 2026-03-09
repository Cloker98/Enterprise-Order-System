package com.enterprise.product.infrastructure.persistence.repository;

import com.enterprise.product.domain.model.Product;
import com.enterprise.product.domain.model.ProductId;
import com.enterprise.product.domain.repository.ProductRepository;
import com.enterprise.product.infrastructure.cache.ProductCacheService;
import com.enterprise.product.infrastructure.persistence.entity.ProductJpaEntity;
import com.enterprise.product.infrastructure.persistence.mapper.ProductJpaMapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of ProductRepository (Adapter for Hexagonal Architecture).
 *
 * <p>Implements the domain port using JPA and Redis cache.
 *
 * <p>Cache strategy:
 * <ul>
 *   <li>findById: Check cache first, fallback to DB, cache result</li>
 *   <li>save: Invalidate cache after save</li>
 *   <li>delete: Invalidate cache after delete</li>
 * </ul>
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class ProductRepositoryImpl implements ProductRepository {

  private final ProductJpaRepository jpaRepository;
  private final ProductJpaMapper jpaMapper;
  private final ProductCacheService cacheService;

  @Override
  @Transactional
  public Product save(Product product) {
    log.debug("Saving product: {}", product.getId().value());

    ProductJpaEntity entity = jpaMapper.toEntity(product);
    ProductJpaEntity saved = jpaRepository.save(entity);

    // Invalidate cache after save
    cacheService.evict(product.getId().value().toString());

    Product result = jpaMapper.toDomain(saved);
    log.info("Product saved successfully: {}", result.getId().value());

    return result;
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Product> findById(ProductId id) {
    String idStr = id.value().toString();
    log.debug("Finding product by id: {}", idStr);

    // 1. Try cache first
    Optional<Product> cached = cacheService.get(idStr);
    if (cached.isPresent()) {
      log.debug("Product found in cache: {}", idStr);
      return cached;
    }

    // 2. If cache miss, query database
    Optional<ProductJpaEntity> entity = jpaRepository.findById(id.value());

    if (entity.isEmpty()) {
      log.debug("Product not found: {}", idStr);
      return Optional.empty();
    }

    // 3. Convert to domain and cache result
    Product product = jpaMapper.toDomain(entity.get());
    cacheService.put(idStr, product);

    log.debug("Product found in database and cached: {}", idStr);
    return Optional.of(product);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Product> findBySku(String sku) {
    log.debug("Finding product by SKU: {}", sku);

    return jpaRepository.findBySku(sku)
        .map(jpaMapper::toDomain);
  }

  @Override
  @Transactional
  public void deleteById(ProductId id) {
    String idStr = id.value().toString();
    log.debug("Deleting product: {}", idStr);

    jpaRepository.deleteById(id.value());

    // Invalidate cache
    cacheService.evict(idStr);

    log.info("Product deleted successfully: {}", idStr);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean existsBySku(String sku) {
    log.debug("Checking if SKU exists: {}", sku);
    return jpaRepository.existsBySku(sku);
  }
}
