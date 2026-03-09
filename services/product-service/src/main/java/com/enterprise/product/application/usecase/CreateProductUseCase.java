package com.enterprise.product.application.usecase;

import com.enterprise.product.application.dto.request.CreateProductRequest;
import com.enterprise.product.application.dto.response.ProductResponse;
import com.enterprise.product.application.mapper.ProductMapper;
import com.enterprise.product.domain.exception.DuplicateSkuException;
import com.enterprise.product.domain.model.Product;
import com.enterprise.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for creating a new product.
 *
 * <p>Business rules:
 * <ul>
 *   <li>SKU must be unique</li>
 *   <li>All validations are performed in domain layer</li>
 *   <li>Product is created with ACTIVE status</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CreateProductUseCase {

  private final ProductRepository productRepository;
  private final ProductMapper productMapper;

  /**
   * Executes the use case to create a product.
   *
   * @param request the create product request
   * @return ProductResponse with created product data
   * @throws DuplicateSkuException if SKU already exists
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional
  public ProductResponse execute(CreateProductRequest request) {
    log.info("Creating product with SKU: {}", request.sku());

    // Validate SKU uniqueness
    if (productRepository.existsBySku(request.sku())) {
      log.warn("Attempt to create product with duplicate SKU: {}", request.sku());
      throw new DuplicateSkuException("SKU already exists: " + request.sku());
    }

    // Create domain product (validations inside)
    Product product = Product.create(
        request.name(),
        request.description(),
        request.price(),
        request.stockQuantity(),
        request.sku(),
        request.category()
    );

    // Persist
    Product savedProduct = productRepository.save(product);

    log.info("Product created successfully with id: {}", savedProduct.getId().value());

    // Return DTO
    return productMapper.toResponse(savedProduct);
  }
}
