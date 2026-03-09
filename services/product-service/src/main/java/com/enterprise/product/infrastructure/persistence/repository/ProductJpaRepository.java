package com.enterprise.product.infrastructure.persistence.repository;

import com.enterprise.product.infrastructure.persistence.entity.ProductJpaEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA Repository for ProductJpaEntity.
 *
 * <p>This interface is automatically implemented by Spring Data JPA.
 * Methods follow Spring Data naming conventions for query derivation.
 */
@Repository
public interface ProductJpaRepository extends JpaRepository<ProductJpaEntity, UUID> {

  /**
   * Finds a product by SKU.
   *
   * @param sku the stock keeping unit
   * @return Optional containing the product if found
   */
  Optional<ProductJpaEntity> findBySku(String sku);

  /**
   * Checks if a product with given SKU exists.
   *
   * @param sku the stock keeping unit
   * @return true if exists, false otherwise
   */
  boolean existsBySku(String sku);
}
