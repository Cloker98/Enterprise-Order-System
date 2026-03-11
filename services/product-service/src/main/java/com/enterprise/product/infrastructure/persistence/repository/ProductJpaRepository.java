package com.enterprise.product.infrastructure.persistence.repository;

import com.enterprise.product.infrastructure.persistence.entity.ProductJpaEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

  /**
   * Finds products by category with pagination.
   *
   * @param category the product category
   * @param pageable pagination information
   * @return page of products
   */
  Page<ProductJpaEntity> findByCategory(String category, Pageable pageable);

  /**
   * Finds products by name containing (case-insensitive) with pagination.
   *
   * @param name the name to search for
   * @param pageable pagination information
   * @return page of products
   */
  Page<ProductJpaEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);

  /**
   * Finds products by category and name containing (case-insensitive) with pagination.
   *
   * @param category the product category
   * @param name the name to search for
   * @param pageable pagination information
   * @return page of products
   */
  Page<ProductJpaEntity> findByCategoryAndNameContainingIgnoreCase(
      String category, String name, Pageable pageable);

  /**
   * Custom query to find products with optional filters.
   * Uses JPQL with conditional WHERE clauses.
   *
   * @param category optional category filter
   * @param name optional name filter
   * @param pageable pagination information
   * @return page of products
   */
  @Query("SELECT p FROM ProductJpaEntity p WHERE "
         + "(:category IS NULL OR p.category = :category) AND "
         + "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')))")
  Page<ProductJpaEntity> findByFilters(
      @Param("category") String category,
      @Param("name") String name,
      Pageable pageable);
}