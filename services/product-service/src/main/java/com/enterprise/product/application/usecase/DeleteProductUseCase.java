package com.enterprise.product.application.usecase;

import com.enterprise.product.domain.exception.ProductNotFoundException;
import com.enterprise.product.domain.model.Product;
import com.enterprise.product.domain.model.ProductId;
import com.enterprise.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for deleting a product (soft delete).
 *
 * <p>Sets status to INACTIVE instead of physical deletion.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeleteProductUseCase {

  private final ProductRepository productRepository;

  /**
   * Executes the use case to delete a product.
   *
   * @param productId the product ID (as string)
   * @throws ProductNotFoundException if product not found
   */
  @Transactional
  public void execute(String productId) {
    log.info("Deleting product: {}", productId);

    ProductId id = ProductId.from(productId);

    Product product = productRepository.findById(id)
        .orElseThrow(() -> {
          log.warn("Product not found: {}", productId);
          return new ProductNotFoundException("Product not found with id: " + productId);
        });

    // Soft delete
    product.deactivate();

    // Persist
    productRepository.save(product);

    log.info("Product deactivated successfully: {}", productId);
  }
}
