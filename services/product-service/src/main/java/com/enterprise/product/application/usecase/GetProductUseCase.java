package com.enterprise.product.application.usecase;

import com.enterprise.product.application.dto.response.ProductResponse;
import com.enterprise.product.application.mapper.ProductMapper;
import com.enterprise.product.domain.exception.ProductNotFoundException;
import com.enterprise.product.domain.model.Product;
import com.enterprise.product.domain.model.ProductId;
import com.enterprise.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for retrieving a product by ID.
 *
 * <p>Uses cache-aside pattern (cache checked in repository layer).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GetProductUseCase {

  private final ProductRepository productRepository;
  private final ProductMapper productMapper;

  /**
   * Executes the use case to get a product.
   *
   * @param productId the product ID (as string)
   * @return ProductResponse with product data
   * @throws ProductNotFoundException if product not found
   * @throws IllegalArgumentException if ID format is invalid
   */
  @Transactional(readOnly = true)
  public ProductResponse execute(String productId) {
    log.debug("Getting product with id: {}", productId);

    ProductId id = ProductId.from(productId);

    Product product = productRepository.findById(id)
        .orElseThrow(() -> {
          log.warn("Product not found: {}", productId);
          return new ProductNotFoundException("Product not found with id: " + productId);
        });

    log.debug("Product found: {}", productId);

    return productMapper.toResponse(product);
  }
}
