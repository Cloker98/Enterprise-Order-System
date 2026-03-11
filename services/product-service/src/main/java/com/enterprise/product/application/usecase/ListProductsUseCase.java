package com.enterprise.product.application.usecase;

import com.enterprise.product.application.dto.response.ProductResponse;
import com.enterprise.product.application.mapper.ProductMapper;
import com.enterprise.product.domain.model.Product;
import com.enterprise.product.domain.model.ProductCategory;
import com.enterprise.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Use case for listing products with pagination and optional filters.
 *
 * <p>This use case handles:
 * <ul>
 *   <li>Pagination of product results</li>
 *   <li>Optional filtering by category</li>
 *   <li>Optional filtering by name (partial match, case-insensitive)</li>
 *   <li>Combination of filters</li>
 * </ul>
 *
 * <p>Business Rules:
 * <ul>
 *   <li>If no filters provided, return all products</li>
 *   <li>Category filter is exact match</li>
 *   <li>Name filter is partial match, case-insensitive</li>
 *   <li>Filters are combined with AND logic</li>
 *   <li>Results are paginated according to Pageable parameter</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ListProductsUseCase {

  private final ProductRepository productRepository;
  private final ProductMapper productMapper;

  /**
   * Executes the list products use case.
   *
   * @param category optional category filter (exact match)
   * @param name optional name filter (partial match, case-insensitive)
   * @param pageable pagination information (page, size, sort)
   * @return page of product responses
   */
  public Page<ProductResponse> execute(ProductCategory category, String name, Pageable pageable) {
    log.debug("Executing ListProductsUseCase with filters: category={}, name={}, page={}, size={}",
        category, name, pageable.getPageNumber(), pageable.getPageSize());

    // Validate input parameters
    validateInput(pageable);

    // Apply filters and pagination
    Page<Product> products = findProductsWithFilters(category, name, pageable);

    // Convert to response DTOs
    Page<ProductResponse> responses = products.map(productMapper::toResponse);

    log.info("Listed {} products (total: {}) with filters: category={}, name={}",
        responses.getNumberOfElements(), responses.getTotalElements(), category, name);

    return responses;
  }

  /**
   * Finds products applying filters and pagination.
   */
  private Page<Product> findProductsWithFilters(
      ProductCategory category, String name, Pageable pageable) {
    // If no filters provided, return all products
    if (category == null && (name == null || name.trim().isEmpty())) {
      log.debug("No filters provided, finding all products");
      return productRepository.findAll(pageable);
    }

    // Apply filters
    String trimmedName = name != null ? name.trim() : null;
    if (trimmedName != null && trimmedName.isEmpty()) {
      trimmedName = null; // Convert empty string to null
    }

    log.debug("Applying filters: category={}, name={}", category, trimmedName);
    return productRepository.findByFilters(category, trimmedName, pageable);
  }

  /**
   * Validates input parameters.
   */
  private void validateInput(Pageable pageable) {
    if (pageable == null) {
      throw new IllegalArgumentException("Pageable cannot be null");
    }

    if (pageable.getPageSize() <= 0) {
      throw new IllegalArgumentException("Page size must be greater than 0");
    }

    if (pageable.getPageNumber() < 0) {
      throw new IllegalArgumentException("Page number must be greater than or equal to 0");
    }

    // Limit maximum page size to prevent performance issues
    if (pageable.getPageSize() > 100) {
      throw new IllegalArgumentException("Page size cannot exceed 100");
    }
  }
}