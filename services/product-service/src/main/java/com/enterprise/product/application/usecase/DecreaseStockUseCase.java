package com.enterprise.product.application.usecase;

import com.enterprise.product.application.dto.request.StockOperationRequest;
import com.enterprise.product.application.dto.response.ProductResponse;
import com.enterprise.product.application.mapper.ProductMapper;
import com.enterprise.product.domain.exception.InsufficientStockException;
import com.enterprise.product.domain.exception.ProductNotFoundException;
import com.enterprise.product.domain.model.Product;
import com.enterprise.product.domain.model.ProductId;
import com.enterprise.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for decreasing product stock.
 *
 * <p>Used when an order is placed.
 *
 * <p>Business rules:
 * <ul>
 *   <li>Quantity must be > 0</li>
 *   <li>Stock cannot become negative</li>
 *   <li>Operation is atomic (within transaction)</li>
 *   <li>Cache is invalidated after update</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DecreaseStockUseCase {

  private final ProductRepository productRepository;
  private final ProductMapper productMapper;

  /**
   * Executes the use case to decrease stock.
   *
   * @param productId the product ID (as string)
   * @param request the stock operation request
   * @return ProductResponse with updated product
   * @throws ProductNotFoundException if product not found
   * @throws InsufficientStockException if insufficient stock
   * @throws IllegalArgumentException if quantity invalid
   */
  @Transactional
  public ProductResponse execute(String productId, StockOperationRequest request) {
    log.info("Decreasing stock for product: {} by quantity: {}", productId, request.quantity());

    ProductId id = ProductId.from(productId);

    // Find product (pessimistic lock would be added here for high concurrency)
    Product product = productRepository.findById(id)
        .orElseThrow(() -> {
          log.warn("Product not found: {}", productId);
          return new ProductNotFoundException("Product not found with id: " + productId);
        });

    // Domain logic (validations inside)
    product.decreaseStock(request.quantity());

    // Persist (cache will be invalidated in repository)
    Product updatedProduct = productRepository.save(product);

    log.info("Stock decreased successfully for product: {}, new stock: {}",
        productId, updatedProduct.getStockQuantity());

    return productMapper.toResponse(updatedProduct);
  }
}
