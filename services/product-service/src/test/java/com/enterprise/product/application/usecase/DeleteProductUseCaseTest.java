package com.enterprise.product.application.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
 * Unit tests for DeleteProductUseCase.
 */
@DisplayName("DeleteProductUseCase Unit Tests")
@ExtendWith(MockitoExtension.class)
class DeleteProductUseCaseTest {

  @Mock
  private ProductRepository productRepository;

  @InjectMocks
  private DeleteProductUseCase useCase;

  @Test
  @DisplayName("Should soft delete product successfully when exists")
  void execute_WhenProductExists_ShouldSoftDelete() {
    // Given
    UUID uuid = UUID.randomUUID();
    String idString = uuid.toString();
    ProductId productId = ProductId.from(idString);

    Product mockProduct = Product.create(
        "Test Product",
        "Test Description",
        new BigDecimal("100.00"),
        10,
        "TEST-SKU-001",
        ProductCategory.ELECTRONICS
    );

    when(productRepository.findById(productId)).thenReturn(Optional.of(mockProduct));

    // When
    useCase.execute(uuid.toString());

    // Then
    verify(productRepository).findById(productId);
    verify(productRepository).save(any(Product.class));
  }

  @Test
  @DisplayName("Should throw exception when product not found")
  void execute_WhenProductNotFound_ShouldThrowException() {
    // Given
    UUID uuid = UUID.randomUUID();
    String idString = uuid.toString();
    ProductId productId = ProductId.from(idString);

    when(productRepository.findById(productId)).thenReturn(Optional.empty());

    // When/Then
    assertThatThrownBy(() -> useCase.execute(uuid.toString()))
        .isInstanceOf(ProductNotFoundException.class)
        .hasMessageContaining(uuid.toString());

    verify(productRepository).findById(productId);
  }

  @Test
  @DisplayName("Should throw exception when id is null")
  void execute_WhenNullId_ShouldThrowException() {
    // When/Then
    assertThatThrownBy(() -> useCase.execute(null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("Should throw exception when id is invalid UUID")
  void execute_WhenInvalidUuid_ShouldThrowException() {
    // When/Then
    assertThatThrownBy(() -> useCase.execute("invalid-uuid"))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
