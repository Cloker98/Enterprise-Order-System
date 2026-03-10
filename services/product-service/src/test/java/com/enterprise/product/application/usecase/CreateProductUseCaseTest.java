package com.enterprise.product.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.enterprise.product.application.dto.request.CreateProductRequest;
import com.enterprise.product.application.dto.response.ProductResponse;
import com.enterprise.product.application.mapper.ProductMapper;
import com.enterprise.product.domain.exception.DuplicateSkuException;
import com.enterprise.product.domain.model.Product;
import com.enterprise.product.domain.model.ProductCategory;
import com.enterprise.product.domain.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for CreateProductUseCase.
 *
 * <p>Tests the product creation use case in isolation using mocks.
 */
@DisplayName("CreateProductUseCase Unit Tests")
@ExtendWith(MockitoExtension.class)
class CreateProductUseCaseTest {

  @Mock
  private ProductRepository productRepository;

  @Mock
  private ProductMapper productMapper;

  @InjectMocks
  private CreateProductUseCase useCase;

  @Test
  @DisplayName("Should create product successfully when valid data")
  void execute_WhenValidData_ShouldCreateProduct() {
    // Given
    CreateProductRequest request = new CreateProductRequest(
        "Test Product",
        "Test Description",
        new BigDecimal("100.00"),
        10,
        "TEST-SKU-001",
        ProductCategory.ELECTRONICS
    );

    Product mockProduct = Product.create(
        "Test Product",
        "Test Description",
        new BigDecimal("100.00"),
        10,
        "TEST-SKU-001",
        ProductCategory.ELECTRONICS
    );

    ProductResponse expectedResponse = new ProductResponse(
        mockProduct.getId().value().toString(),
        "Test Product",
        "Test Description",
        new BigDecimal("100.00"),
        10,
        "TEST-SKU-001",
        ProductCategory.ELECTRONICS,
        mockProduct.getStatus(),
        mockProduct.getCreatedAt(),
        mockProduct.getUpdatedAt()
    );

    when(productRepository.existsBySku("TEST-SKU-001")).thenReturn(false);
    when(productRepository.save(any(Product.class))).thenReturn(mockProduct);
    when(productMapper.toResponse(mockProduct)).thenReturn(expectedResponse);

    // When
    ProductResponse response = useCase.execute(request);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.name()).isEqualTo("Test Product");
    assertThat(response.price()).isEqualByComparingTo(new BigDecimal("100.00"));
    assertThat(response.stockQuantity()).isEqualTo(10);
    assertThat(response.sku()).isEqualTo("TEST-SKU-001");
    assertThat(response.category()).isEqualTo(ProductCategory.ELECTRONICS);
    assertThat(response.status()).isEqualTo(mockProduct.getStatus());

    verify(productRepository).existsBySku("TEST-SKU-001");
    verify(productRepository).save(any(Product.class));
    verify(productMapper).toResponse(mockProduct);
  }

  @Test
  @DisplayName("Should throw exception when SKU already exists")
  void execute_WhenSkuExists_ShouldThrowException() {
    // Given
    CreateProductRequest request = new CreateProductRequest(
        "Test Product",
        "Test Description",
        new BigDecimal("100.00"),
        10,
        "EXISTING-SKU",
        ProductCategory.ELECTRONICS
    );

    Product existingProduct = Product.create(
        "Existing Product",
        "Existing Description",
        new BigDecimal("200.00"),
        5,
        "EXISTING-SKU",
        ProductCategory.ELECTRONICS
    );

    when(productRepository.existsBySku("EXISTING-SKU")).thenReturn(true);

    // When/Then
    assertThatThrownBy(() -> useCase.execute(request))
        .isInstanceOf(DuplicateSkuException.class)
        .hasMessage("SKU already exists: EXISTING-SKU");

    verify(productRepository).existsBySku("EXISTING-SKU");
  }

  @Test
  @DisplayName("Should throw exception when request is null")
  void execute_WhenNullRequest_ShouldThrowException() {
    // When/Then
    assertThatThrownBy(() -> useCase.execute(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("CreateProductRequest cannot be null");
  }
}
