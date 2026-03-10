package com.enterprise.product.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.enterprise.product.application.dto.request.StockOperationRequest;
import com.enterprise.product.application.dto.response.ProductResponse;
import com.enterprise.product.application.mapper.ProductMapper;
import com.enterprise.product.domain.exception.InsufficientStockException;
import com.enterprise.product.domain.exception.ProductNotFoundException;
import com.enterprise.product.domain.model.Product;
import com.enterprise.product.domain.model.ProductCategory;
import com.enterprise.product.domain.model.ProductId;
import com.enterprise.product.domain.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for DecreaseStockUseCase.
 */
@DisplayName("DecreaseStockUseCase Unit Tests")
@ExtendWith(MockitoExtension.class)
class DecreaseStockUseCaseTest {

  @Mock
  private ProductRepository productRepository;

  @Mock
  private ProductMapper productMapper;

  @InjectMocks
  private DecreaseStockUseCase useCase;

  @Test
  @DisplayName("Should decrease stock successfully when sufficient quantity")
  void execute_WhenSufficientStock_ShouldDecreaseStock() {
    // Given
    UUID uuid = UUID.randomUUID();
    String idString = uuid.toString();
    ProductId productId = ProductId.from(idString);

    Product mockProduct = Product.create(
        "Test Product",
        "Test Description",
        new BigDecimal("100.00"),
        50,
        "TEST-SKU-001",
        ProductCategory.ELECTRONICS
    );

    StockOperationRequest request = new StockOperationRequest(10);

    Product updatedProduct = Product.create(
        "Test Product",
        "Test Description",
        new BigDecimal("100.00"),
        40,
        "TEST-SKU-001",
        ProductCategory.ELECTRONICS
    );

    ProductResponse expectedResponse = new ProductResponse(
        uuid.toString(),
        "Test Product",
        "Test Description",
        new BigDecimal("100.00"),
        40,
        "TEST-SKU-001",
        ProductCategory.ELECTRONICS,
        updatedProduct.getStatus(),
        updatedProduct.getCreatedAt(),
        updatedProduct.getUpdatedAt()
    );

    when(productRepository.findById(productId)).thenReturn(Optional.of(mockProduct));
    when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);
    when(productMapper.toResponse(updatedProduct)).thenReturn(expectedResponse);

    // When
    ProductResponse response = useCase.execute(uuid.toString(), request);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.stockQuantity()).isEqualTo(40);

    verify(productRepository).findById(productId);
    verify(productRepository).save(any(Product.class));
    verify(productMapper).toResponse(updatedProduct);
  }

  @Test
  @DisplayName("Should throw exception when product not found")
  void execute_WhenProductNotFound_ShouldThrowException() {
    // Given
    UUID uuid = UUID.randomUUID();
    String idString = uuid.toString();
    ProductId productId = ProductId.from(idString);

    StockOperationRequest request = new StockOperationRequest(10);

    when(productRepository.findById(productId)).thenReturn(Optional.empty());

    // When/Then
    assertThatThrownBy(() -> useCase.execute(uuid.toString(), request))
        .isInstanceOf(ProductNotFoundException.class)
        .hasMessageContaining(uuid.toString());

    verify(productRepository).findById(productId);
  }

  @Test
  @DisplayName("Should throw exception when insufficient stock")
  void execute_WhenInsufficientStock_ShouldThrowException() {
    // Given
    UUID uuid = UUID.randomUUID();
    String idString = uuid.toString();
    ProductId productId = ProductId.from(idString);

    Product mockProduct = Product.create(
        "Test Product",
        "Test Description",
        new BigDecimal("100.00"),
        5,
        "TEST-SKU-001",
        ProductCategory.ELECTRONICS
    );

    StockOperationRequest request = new StockOperationRequest(10);

    when(productRepository.findById(productId)).thenReturn(Optional.of(mockProduct));

    // When/Then
    assertThatThrownBy(() -> useCase.execute(uuid.toString(), request))
        .isInstanceOf(InsufficientStockException.class)
        .hasMessage("Insufficient stock. Available: 5, Requested: 10");

    verify(productRepository).findById(productId);
  }

  @Test
  @DisplayName("Should throw exception when id is null")
  void execute_WhenNullId_ShouldThrowException() {
    // Given
    StockOperationRequest request = new StockOperationRequest(10);

    // When/Then
    assertThatThrownBy(() -> useCase.execute(null, request))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("Should throw exception when request is null")
  void execute_WhenNullRequest_ShouldThrowException() {
    // When/Then
    assertThatThrownBy(() -> useCase.execute(UUID.randomUUID().toString(), null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("StockOperationRequest cannot be null");
  }
}
