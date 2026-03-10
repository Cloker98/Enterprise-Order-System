package com.enterprise.product.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.enterprise.product.application.dto.request.UpdateProductRequest;
import com.enterprise.product.application.dto.response.ProductResponse;
import com.enterprise.product.application.mapper.ProductMapper;
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
 * Unit tests for UpdateProductUseCase.
 */
@DisplayName("UpdateProductUseCase Unit Tests")
@ExtendWith(MockitoExtension.class)
class UpdateProductUseCaseTest {

  @Mock
  private ProductRepository productRepository;

  @Mock
  private ProductMapper productMapper;

  @InjectMocks
  private UpdateProductUseCase useCase;

  @Test
  @DisplayName("Should update product successfully when exists")
  void execute_WhenProductExists_ShouldUpdateProduct() {
    // Given
    UUID uuid = UUID.randomUUID();
    String idString = uuid.toString();
    ProductId productId = ProductId.from(idString);

    Product existingProduct = Product.create(
        "Old Name",
        "Old Description",
        new BigDecimal("100.00"),
        10,
        "OLD-SKU",
        ProductCategory.ELECTRONICS
    );

    UpdateProductRequest request = new UpdateProductRequest(
        "New Name",
        "New Description",
        new BigDecimal("200.00"),
        ProductCategory.BOOKS
    );

    Product updatedProduct = Product.create(
        "New Name",
        "New Description",
        new BigDecimal("200.00"),
        10,
        "OLD-SKU",
        ProductCategory.BOOKS
    );

    ProductResponse expectedResponse = new ProductResponse(
        uuid.toString(),
        "New Name",
        "New Description",
        new BigDecimal("200.00"),
        10,
        "OLD-SKU",
        ProductCategory.BOOKS,
        updatedProduct.getStatus(),
        updatedProduct.getCreatedAt(),
        updatedProduct.getUpdatedAt()
    );

    when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
    when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);
    when(productMapper.toResponse(updatedProduct)).thenReturn(expectedResponse);

    // When
    ProductResponse response = useCase.execute(uuid.toString(), request);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.name()).isEqualTo("New Name");
    assertThat(response.description()).isEqualTo("New Description");
    assertThat(response.price()).isEqualByComparingTo(new BigDecimal("200.00"));
    assertThat(response.category()).isEqualTo(ProductCategory.BOOKS);

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

    UpdateProductRequest request = new UpdateProductRequest(
        "New Name",
        "New Description",
        new BigDecimal("200.00"),
        ProductCategory.BOOKS
    );

    when(productRepository.findById(productId)).thenReturn(Optional.empty());

    // When/Then
    assertThatThrownBy(() -> useCase.execute(uuid.toString(), request))
        .isInstanceOf(ProductNotFoundException.class)
        .hasMessageContaining(uuid.toString());

    verify(productRepository).findById(productId);
  }

  @Test
  @DisplayName("Should throw exception when id is null")
  void execute_WhenNullId_ShouldThrowException() {
    // Given
    UpdateProductRequest request = new UpdateProductRequest(
        "New Name",
        "New Description",
        new BigDecimal("200.00"),
        ProductCategory.BOOKS
    );

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
        .hasMessage("UpdateProductRequest cannot be null");
  }
}
