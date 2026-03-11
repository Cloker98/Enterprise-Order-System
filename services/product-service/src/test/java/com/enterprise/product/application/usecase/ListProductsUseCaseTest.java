package com.enterprise.product.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.enterprise.product.application.dto.response.ProductResponse;
import com.enterprise.product.application.mapper.ProductMapper;
import com.enterprise.product.domain.model.Product;
import com.enterprise.product.domain.model.ProductCategory;
import com.enterprise.product.domain.model.ProductStatus;
import com.enterprise.product.domain.repository.ProductRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * Unit tests for ListProductsUseCase.
 *
 * <p>Tests the business logic for listing products with pagination and filters.
 * Uses mocks to isolate the use case from external dependencies.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ListProductsUseCase Tests")
class ListProductsUseCaseTest {

  @Mock
  private ProductRepository productRepository;

  @Mock
  private ProductMapper productMapper;

  private ListProductsUseCase listProductsUseCase;

  @BeforeEach
  void setUp() {
    listProductsUseCase = new ListProductsUseCase(productRepository, productMapper);
  }

  @Test
  @DisplayName("Should return all products when no filters provided")
  void execute_WhenNoFilters_ShouldReturnAllProducts() {
    // Given
    Pageable pageable = PageRequest.of(0, 10);
    Product product1 = createTestProduct("Product 1", "SKU-001", ProductCategory.ELECTRONICS);
    Product product2 = createTestProduct("Product 2", "SKU-002", ProductCategory.CLOTHING);
    
    Page<Product> productPage = new PageImpl<>(List.of(product1, product2), pageable, 2);
    ProductResponse response1 = createTestProductResponse("Product 1", "SKU-001");
    ProductResponse response2 = createTestProductResponse("Product 2", "SKU-002");
    
    when(productRepository.findAll(pageable)).thenReturn(productPage);
    when(productMapper.toResponse(product1)).thenReturn(response1);
    when(productMapper.toResponse(product2)).thenReturn(response2);

    // When
    Page<ProductResponse> result = listProductsUseCase.execute(null, null, pageable);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(2);
    assertThat(result.getTotalElements()).isEqualTo(2);
    assertThat(result.getContent().get(0).name()).isEqualTo("Product 1");
    assertThat(result.getContent().get(1).name()).isEqualTo("Product 2");
    
    verify(productRepository).findAll(pageable);
    verify(productMapper).toResponse(product1);
    verify(productMapper).toResponse(product2);
  }

  @Test
  @DisplayName("Should filter by category when category provided")
  void execute_WhenCategoryFilter_ShouldFilterByCategory() {
    // Given
    Pageable pageable = PageRequest.of(0, 10);
    ProductCategory category = ProductCategory.ELECTRONICS;
    Product product = createTestProduct("Notebook", "NB-001", ProductCategory.ELECTRONICS);
    
    Page<Product> productPage = new PageImpl<>(List.of(product), pageable, 1);
    ProductResponse response = createTestProductResponse("Notebook", "NB-001");
    
    when(productRepository.findByFilters(category, null, pageable)).thenReturn(productPage);
    when(productMapper.toResponse(product)).thenReturn(response);

    // When
    Page<ProductResponse> result = listProductsUseCase.execute(category, null, pageable);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getTotalElements()).isEqualTo(1);
    assertThat(result.getContent().get(0).name()).isEqualTo("Notebook");
    
    verify(productRepository).findByFilters(category, null, pageable);
    verify(productMapper).toResponse(product);
  }

  @Test
  @DisplayName("Should filter by name when name provided")
  void execute_WhenNameFilter_ShouldSearchByName() {
    // Given
    Pageable pageable = PageRequest.of(0, 10);
    String nameFilter = "notebook";
    Product product = createTestProduct("Notebook Dell", "NB-DELL-001", ProductCategory.ELECTRONICS);
    
    Page<Product> productPage = new PageImpl<>(List.of(product), pageable, 1);
    ProductResponse response = createTestProductResponse("Notebook Dell", "NB-DELL-001");
    
    when(productRepository.findByFilters(null, nameFilter, pageable)).thenReturn(productPage);
    when(productMapper.toResponse(product)).thenReturn(response);

    // When
    Page<ProductResponse> result = listProductsUseCase.execute(null, nameFilter, pageable);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getTotalElements()).isEqualTo(1);
    assertThat(result.getContent().get(0).name()).isEqualTo("Notebook Dell");
    
    verify(productRepository).findByFilters(null, nameFilter, pageable);
    verify(productMapper).toResponse(product);
  }

  @Test
  @DisplayName("Should combine filters when both category and name provided")
  void execute_WhenBothFilters_ShouldCombineFilters() {
    // Given
    Pageable pageable = PageRequest.of(0, 10);
    ProductCategory category = ProductCategory.ELECTRONICS;
    String nameFilter = "dell";
    Product product = createTestProduct("Notebook Dell", "NB-DELL-002", ProductCategory.ELECTRONICS);
    
    Page<Product> productPage = new PageImpl<>(List.of(product), pageable, 1);
    ProductResponse response = createTestProductResponse("Notebook Dell", "NB-DELL-002");
    
    when(productRepository.findByFilters(category, nameFilter, pageable)).thenReturn(productPage);
    when(productMapper.toResponse(product)).thenReturn(response);

    // When
    Page<ProductResponse> result = listProductsUseCase.execute(category, nameFilter, pageable);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getTotalElements()).isEqualTo(1);
    assertThat(result.getContent().get(0).name()).isEqualTo("Notebook Dell");
    
    verify(productRepository).findByFilters(category, nameFilter, pageable);
    verify(productMapper).toResponse(product);
  }

  @Test
  @DisplayName("Should return empty page when no products match filters")
  void execute_WhenEmptyResult_ShouldReturnEmptyPage() {
    // Given
    Pageable pageable = PageRequest.of(0, 10);
    ProductCategory category = ProductCategory.BOOKS;
    
    Page<Product> emptyPage = new PageImpl<>(List.of(), pageable, 0);
    
    when(productRepository.findByFilters(category, null, pageable)).thenReturn(emptyPage);

    // When
    Page<ProductResponse> result = listProductsUseCase.execute(category, null, pageable);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).isEmpty();
    assertThat(result.getTotalElements()).isZero();
    
    verify(productRepository).findByFilters(category, null, pageable);
  }

  /**
   * Helper method to create test products.
   */
  private Product createTestProduct(String name, String sku, ProductCategory category) {
    return Product.create(
        name,
        "Test description for " + name,
        new BigDecimal("100.00"),
        10,
        sku,
        category
    );
  }

  /**
   * Helper method to create test product responses.
   */
  private ProductResponse createTestProductResponse(String name, String sku) {
    return new ProductResponse(
        "550e8400-e29b-41d4-a716-446655440000",
        name,
        "Test description for " + name,
        new BigDecimal("100.00"),
        10,
        sku,
        ProductCategory.ELECTRONICS,
        ProductStatus.ACTIVE,
        LocalDateTime.now(),
        LocalDateTime.now()
    );
  }
}