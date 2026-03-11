package com.enterprise.product.domain.repository;

import com.enterprise.product.domain.model.Product;
import com.enterprise.product.domain.model.ProductCategory;
import com.enterprise.product.domain.model.ProductId;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Repository PORT (interface) for Product aggregate.
 *
 * <p>This is a domain interface (PORT in Hexagonal Architecture).
 * The implementation (ADAPTER) is in the infrastructure layer.
 *
 * <p>Domain layer depends on this interface, not on the implementation.
 * This allows us to keep the domain pure and independent of frameworks.
 */
public interface ProductRepository {

  /**
   * Saves a product (create or update).
   *
   * @param product the product to save
   * @return the saved product
   */
  Product save(Product product);

  /**
   * Finds a product by its ID.
   *
   * @param id the product ID
   * @return Optional containing the product if found, empty otherwise
   */
  Optional<Product> findById(ProductId id);

  /**
   * Finds a product by SKU.
   *
   * @param sku the stock keeping unit
   * @return Optional containing the product if found, empty otherwise
   */
  Optional<Product> findBySku(String sku);

  /**
   * Deletes a product by its ID.
   *
   * @param id the product ID
   */
  void deleteById(ProductId id);

  /**
   * Checks if a product with given SKU exists.
   *
   * @param sku the stock keeping unit
   * @return true if exists, false otherwise
   */
  boolean existsBySku(String sku);

  /**
   * Finds all products with pagination.
   *
   * @param pageable pagination information (page, size, sort)
   * @return page of products
   */
  Page<Product> findAll(Pageable pageable);

  /**
   * Finds products by filters with pagination.
   *
   * <p>Filters are applied with AND logic:
   * - If category is provided, filter by exact category match
   * - If name is provided, filter by partial name match (case-insensitive)
   * - Both filters can be combined
   *
   * @param category optional category filter (exact match)
   * @param name optional name filter (partial match, case-insensitive)
   * @param pageable pagination information (page, size, sort)
   * @return page of products matching the filters
   */
  Page<Product> findByFilters(ProductCategory category, String name, Pageable pageable);
}