package com.enterprise.product.application.usecase;

import com.enterprise.product.application.dto.request.UpdateProductRequest;
import com.enterprise.product.application.dto.response.ProductResponse;
import com.enterprise.product.application.mapper.ProductMapper;
import com.enterprise.product.domain.exception.ProductNotFoundException;
import com.enterprise.product.domain.model.Money;
import com.enterprise.product.domain.model.Product;
import com.enterprise.product.domain.model.ProductId;
import com.enterprise.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for updating an existing product.
 *
 * <p>Note: Cannot update id, sku, or createdAt (immutable).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateProductUseCase {

  private final ProductRepository productRepository;
  private final ProductMapper productMapper;

  /**
   * Executes the use case to update a product.
   *
   * @param productId the product ID (as string)
   * @param request the update request
   * @return ProductResponse with updated product
   * @throws ProductNotFoundException if product not found
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional
  public ProductResponse execute(String productId, UpdateProductRequest request) {
    if (request == null) {
      throw new IllegalArgumentException("UpdateProductRequest cannot be null");
    }

    log.info("Updating product: {}", productId);

    ProductId id = ProductId.from(productId);

    Product product = productRepository.findById(id)
        .orElseThrow(() -> {
          log.warn("Product not found: {}", productId);
          return new ProductNotFoundException("Product not found with id: " + productId);
        });

    // Domain logic
    Money price = Money.brl(request.price());
    product.update(request.name(), request.description(), price, request.category());

    // Persist
    Product updatedProduct = productRepository.save(product);

    log.info("Product updated successfully: {}", productId);

    return productMapper.toResponse(updatedProduct);
  }
}
