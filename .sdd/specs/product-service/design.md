# Product Service - Design Specification

**Versão**: 1.0
**Data**: 2026-03-09
**Status**: 📐 Design → Aguardando aprovação

---

## 1. Arquitetura Overview

### 1.1 Hexagonal Architecture (Ports & Adapters)

```
product-service/
├── domain/                          # CORE - SEM dependências externas
│   ├── model/
│   │   ├── Product.java            # Aggregate Root
│   │   ├── ProductId.java          # Value Object
│   │   ├── Money.java              # Value Object
│   │   ├── ProductCategory.java    # Enum
│   │   └── ProductStatus.java      # Enum
│   ├── service/
│   │   └── ProductDomainService.java
│   ├── repository/                 # PORT (interface)
│   │   └── ProductRepository.java
│   └── exception/
│       ├── ProductNotFoundException.java
│       ├── InsufficientStockException.java
│       ├── DuplicateSkuException.java
│       └── DomainException.java
│
├── application/                     # Use Cases (orquestração)
│   ├── usecase/
│   │   ├── CreateProductUseCase.java
│   │   ├── GetProductUseCase.java
│   │   ├── ListProductsUseCase.java
│   │   ├── UpdateProductUseCase.java
│   │   ├── DeleteProductUseCase.java
│   │   ├── DecreaseStockUseCase.java
│   │   └── IncreaseStockUseCase.java
│   ├── dto/
│   │   ├── request/
│   │   │   ├── CreateProductRequest.java
│   │   │   ├── UpdateProductRequest.java
│   │   │   └── StockOperationRequest.java
│   │   └── response/
│   │       ├── ProductResponse.java
│   │       └── ProductPageResponse.java
│   └── mapper/
│       └── ProductMapper.java      # MapStruct
│
├── infrastructure/                  # ADAPTERS
│   ├── persistence/
│   │   ├── entity/
│   │   │   └── ProductJpaEntity.java
│   │   ├── repository/
│   │   │   ├── ProductJpaRepository.java  # Spring Data JPA
│   │   │   └── ProductRepositoryImpl.java # Adapter implementation
│   │   └── mapper/
│   │       └── ProductJpaMapper.java      # MapStruct
│   ├── rest/
│   │   ├── controller/
│   │   │   └── ProductController.java
│   │   └── exception/
│   │       └── GlobalExceptionHandler.java
│   ├── cache/
│   │   ├── ProductCacheService.java
│   │   └── CacheConfig.java
│   └── config/
│       ├── DatabaseConfig.java
│       ├── RedisConfig.java
│       └── SwaggerConfig.java
│
└── resources/
    ├── application.yml
    ├── application-dev.yml
    ├── application-test.yml
    └── db/migration/
        └── V001__create_products_table.sql
```

---

## 2. Domain Layer (Core Business)

### 2.1 Product Aggregate

```java
package com.enterprise.product.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Product Aggregate Root
 * IMPORTANTE: SEM anotações de framework (@Entity, @Table, etc)
 */
public class Product {
    private final ProductId id;
    private String name;
    private String description;
    private Money price;
    private int stockQuantity;
    private final String sku; // imutável
    private ProductCategory category;
    private ProductStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Construtor para CRIAR novo produto
    private Product(String name, String description, Money price,
                    int stockQuantity, String sku, ProductCategory category) {
        validateCreate(name, price, stockQuantity, sku);

        this.id = new ProductId(UUID.randomUUID());
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.sku = sku;
        this.category = category;
        this.status = ProductStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Construtor para RECONSTITUIR do banco
    private Product(ProductId id, String name, String description, Money price,
                    int stockQuantity, String sku, ProductCategory category,
                    ProductStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.sku = sku;
        this.category = category;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Factory Method para criar novo produto
    public static Product create(String name, String description, BigDecimal price,
                                 int stockQuantity, String sku, ProductCategory category) {
        Money money = new Money(price, Currency.getInstance("BRL"));
        return new Product(name, description, money, stockQuantity, sku, category);
    }

    // Factory Method para reconstituir do banco
    public static Product reconstitute(UUID id, String name, String description,
                                       BigDecimal price, int stockQuantity, String sku,
                                       ProductCategory category, ProductStatus status,
                                       LocalDateTime createdAt, LocalDateTime updatedAt) {
        ProductId productId = new ProductId(id);
        Money money = new Money(price, Currency.getInstance("BRL"));
        return new Product(productId, name, description, money, stockQuantity,
                          sku, category, status, createdAt, updatedAt);
    }

    // DOMAIN LOGIC - Decrementar estoque
    public void decreaseStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        if (quantity > this.stockQuantity) {
            throw new InsufficientStockException(
                String.format("Insufficient stock. Available: %d, Requested: %d",
                             this.stockQuantity, quantity)
            );
        }
        this.stockQuantity -= quantity;
        this.updatedAt = LocalDateTime.now();
    }

    // DOMAIN LOGIC - Incrementar estoque
    public void increaseStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        this.stockQuantity += quantity;
        this.updatedAt = LocalDateTime.now();
    }

    // DOMAIN LOGIC - Atualizar informações
    public void update(String name, String description, Money price, ProductCategory category) {
        validateUpdate(name, price);

        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.updatedAt = LocalDateTime.now();
    }

    // DOMAIN LOGIC - Inativar produto (soft delete)
    public void deactivate() {
        this.status = ProductStatus.INACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    // Validações de domínio
    private void validateCreate(String name, Money price, int stockQuantity, String sku) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be null or blank");
        }
        if (name.length() > 200) {
            throw new IllegalArgumentException("Name cannot exceed 200 characters");
        }
        if (price == null || price.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than 0");
        }
        if (stockQuantity < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }
        if (sku == null || sku.isBlank()) {
            throw new IllegalArgumentException("SKU cannot be null or blank");
        }
        if (!sku.matches("^[a-zA-Z0-9-]+$")) {
            throw new IllegalArgumentException("SKU must be alphanumeric");
        }
    }

    private void validateUpdate(String name, Money price) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be null or blank");
        }
        if (price == null || price.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than 0");
        }
    }

    // Getters (sem setters - imutabilidade controlada)
    public ProductId getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Money getPrice() { return price; }
    public int getStockQuantity() { return stockQuantity; }
    public String getSku() { return sku; }
    public ProductCategory getCategory() { return category; }
    public ProductStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
```

### 2.2 Value Objects

```java
package com.enterprise.product.domain.model;

import java.util.UUID;

public record ProductId(UUID value) {
    public ProductId {
        if (value == null) {
            throw new IllegalArgumentException("ProductId cannot be null");
        }
    }

    public static ProductId from(String value) {
        return new ProductId(UUID.fromString(value));
    }
}
```

```java
package com.enterprise.product.domain.model;

import java.math.BigDecimal;
import java.util.Currency;

public record Money(BigDecimal amount, Currency currency) {
    public Money {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }
        if (currency == null) {
            throw new IllegalArgumentException("Currency cannot be null");
        }
        // Arredondar para 2 casas decimais
        amount = amount.setScale(2, java.math.RoundingMode.HALF_UP);
    }
}
```

### 2.3 Repository Port (Interface)

```java
package com.enterprise.product.domain.repository;

import com.enterprise.product.domain.model.Product;
import com.enterprise.product.domain.model.ProductId;
import java.util.Optional;

/**
 * Repository PORT (interface do domínio)
 * Implementação estará em infrastructure/persistence
 */
public interface ProductRepository {
    Product save(Product product);
    Optional<Product> findById(ProductId id);
    Optional<Product> findBySku(String sku);
    void deleteById(ProductId id);
    boolean existsBySku(String sku);
}
```

---

## 3. Application Layer (Use Cases)

### 3.1 Create Product Use Case

```java
package com.enterprise.product.application.usecase;

import com.enterprise.product.application.dto.request.CreateProductRequest;
import com.enterprise.product.application.dto.response.ProductResponse;
import com.enterprise.product.application.mapper.ProductMapper;
import com.enterprise.product.domain.model.Product;
import com.enterprise.product.domain.repository.ProductRepository;
import com.enterprise.product.domain.exception.DuplicateSkuException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateProductUseCase {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Transactional
    public ProductResponse execute(CreateProductRequest request) {
        // Validar SKU único
        if (productRepository.existsBySku(request.getSku())) {
            throw new DuplicateSkuException("SKU already exists: " + request.getSku());
        }

        // Criar produto (lógica no domínio)
        Product product = Product.create(
            request.getName(),
            request.getDescription(),
            request.getPrice(),
            request.getStockQuantity(),
            request.getSku(),
            request.getCategory()
        );

        // Persistir
        Product savedProduct = productRepository.save(product);

        // Retornar DTO
        return productMapper.toResponse(savedProduct);
    }
}
```

### 3.2 Decrease Stock Use Case

```java
package com.enterprise.product.application.usecase;

import com.enterprise.product.application.dto.request.StockOperationRequest;
import com.enterprise.product.application.dto.response.ProductResponse;
import com.enterprise.product.application.mapper.ProductMapper;
import com.enterprise.product.domain.model.Product;
import com.enterprise.product.domain.model.ProductId;
import com.enterprise.product.domain.repository.ProductRepository;
import com.enterprise.product.domain.exception.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DecreaseStockUseCase {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Transactional
    public ProductResponse execute(String productId, StockOperationRequest request) {
        ProductId id = ProductId.from(productId);

        // Buscar produto com PESSIMISTIC_WRITE lock
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));

        // Lógica de domínio (validações dentro do aggregate)
        product.decreaseStock(request.getQuantity());

        // Persistir
        Product updatedProduct = productRepository.save(product);

        // Invalidar cache
        // (será tratado no ProductRepositoryImpl)

        return productMapper.toResponse(updatedProduct);
    }
}
```

---

## 4. Infrastructure Layer (Adapters)

### 4.1 JPA Entity

```java
package com.enterprise.product.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity - APENAS para persistência
 * NÃO expor na API ou na camada de domínio
 */
@Entity
@Table(name = "products", indexes = {
    @Index(name = "idx_sku", columnList = "sku", unique = true),
    @Index(name = "idx_category", columnList = "category"),
    @Index(name = "idx_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductJpaEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    @Column(nullable = false, unique = true, length = 50)
    private String sku;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

### 4.2 Repository Implementation (Adapter)

```java
package com.enterprise.product.infrastructure.persistence.repository;

import com.enterprise.product.domain.model.Product;
import com.enterprise.product.domain.model.ProductId;
import com.enterprise.product.domain.repository.ProductRepository;
import com.enterprise.product.infrastructure.cache.ProductCacheService;
import com.enterprise.product.infrastructure.persistence.entity.ProductJpaEntity;
import com.enterprise.product.infrastructure.persistence.mapper.ProductJpaMapper;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {
    private final ProductJpaRepository jpaRepository;
    private final ProductJpaMapper jpaMapper;
    private final ProductCacheService cacheService;

    @Override
    public Product save(Product product) {
        ProductJpaEntity entity = jpaMapper.toEntity(product);
        ProductJpaEntity saved = jpaRepository.save(entity);

        // Invalidar cache
        cacheService.evict(product.getId().value().toString());

        return jpaMapper.toDomain(saved);
    }

    @Override
    public Optional<Product> findById(ProductId id) {
        String idStr = id.value().toString();

        // 1. Tentar buscar no cache
        Optional<Product> cached = cacheService.get(idStr);
        if (cached.isPresent()) {
            return cached;
        }

        // 2. Se cache miss, buscar no DB
        Optional<ProductJpaEntity> entity = jpaRepository.findById(id.value());

        if (entity.isPresent()) {
            Product product = jpaMapper.toDomain(entity.get());
            // 3. Cachear resultado
            cacheService.put(idStr, product);
            return Optional.of(product);
        }

        return Optional.empty();
    }

    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    public Optional<Product> findByIdWithLock(ProductId id) {
        return jpaRepository.findById(id.value())
            .map(jpaMapper::toDomain);
    }

    @Override
    public Optional<Product> findBySku(String sku) {
        return jpaRepository.findBySku(sku)
            .map(jpaMapper::toDomain);
    }

    @Override
    public void deleteById(ProductId id) {
        jpaRepository.deleteById(id.value());
        cacheService.evict(id.value().toString());
    }

    @Override
    public boolean existsBySku(String sku) {
        return jpaRepository.existsBySku(sku);
    }
}
```

### 4.3 REST Controller

```java
package com.enterprise.product.infrastructure.rest.controller;

import com.enterprise.product.application.dto.request.CreateProductRequest;
import com.enterprise.product.application.dto.request.UpdateProductRequest;
import com.enterprise.product.application.dto.request.StockOperationRequest;
import com.enterprise.product.application.dto.response.ProductResponse;
import com.enterprise.product.application.usecase.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product management APIs")
public class ProductController {

    private final CreateProductUseCase createProductUseCase;
    private final GetProductUseCase getProductUseCase;
    private final ListProductsUseCase listProductsUseCase;
    private final UpdateProductUseCase updateProductUseCase;
    private final DeleteProductUseCase deleteProductUseCase;
    private final DecreaseStockUseCase decreaseStockUseCase;
    private final IncreaseStockUseCase increaseStockUseCase;

    @PostMapping
    @Operation(summary = "Create a new product")
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody CreateProductRequest request) {
        ProductResponse response = createProductUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<ProductResponse> getById(@PathVariable String id) {
        ProductResponse response = getProductUseCase.execute(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "List products with pagination and filters")
    public ResponseEntity<Page<ProductResponse>> list(
        @RequestParam(required = false) String category,
        @RequestParam(required = false) String name,
        Pageable pageable
    ) {
        Page<ProductResponse> response = listProductsUseCase.execute(category, name, pageable);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update product")
    public ResponseEntity<ProductResponse> update(
        @PathVariable String id,
        @Valid @RequestBody UpdateProductRequest request
    ) {
        ProductResponse response = updateProductUseCase.execute(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete product (soft delete)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        deleteProductUseCase.execute(id);
    }

    @PostMapping("/{id}/decrease-stock")
    @Operation(summary = "Decrease product stock")
    public ResponseEntity<ProductResponse> decreaseStock(
        @PathVariable String id,
        @Valid @RequestBody StockOperationRequest request
    ) {
        ProductResponse response = decreaseStockUseCase.execute(id, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/increase-stock")
    @Operation(summary = "Increase product stock")
    public ResponseEntity<ProductResponse> increaseStock(
        @PathVariable String id,
        @Valid @RequestBody StockOperationRequest request
    ) {
        ProductResponse response = increaseStockUseCase.execute(id, request);
        return ResponseEntity.ok(response);
    }
}
```

### 4.4 Cache Service

```java
package com.enterprise.product.infrastructure.cache;

import com.enterprise.product.domain.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductCacheService {

    private static final String CACHE_NAME = "products";

    @Cacheable(value = CACHE_NAME, key = "#id")
    public Optional<Product> get(String id) {
        // Retorna empty - cache miss será tratado no repository
        return Optional.empty();
    }

    public void put(String id, Product product) {
        // Cache será populado automaticamente pelo @Cacheable
    }

    @CacheEvict(value = CACHE_NAME, key = "#id")
    public void evict(String id) {
        // Invalida cache
    }

    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public void evictAll() {
        // Limpa todo o cache
    }
}
```

---

## 5. Configuration

### 5.1 application.yml

```yaml
server:
  port: 8081

spring:
  application:
    name: product-service

  datasource:
    url: jdbc:postgresql://localhost:5432/product_db
    username: product_user
    password: product_pass
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000

  jpa:
    hibernate:
      ddl-auto: validate  # Flyway vai gerenciar
    show-sql: false
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.PostgreSQLDialect

  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true

  data:
    redis:
      host: localhost
      port: 6379
      timeout: 2000ms

  cache:
    type: redis
    redis:
      time-to-live: 300000  # 5 minutos

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always

logging:
  level:
    com.enterprise.product: DEBUG
    org.springframework.web: INFO
    org.hibernate.SQL: DEBUG
```

---

## 6. Database Migration

### V001__create_products_table.sql

```sql
-- Product Service - Initial Schema
-- Version: 001
-- Description: Create products table with indexes

CREATE TABLE IF NOT EXISTS products (
    id UUID PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    price DECIMAL(10, 2) NOT NULL CHECK (price > 0),
    stock_quantity INTEGER NOT NULL CHECK (stock_quantity >= 0),
    sku VARCHAR(50) NOT NULL UNIQUE,
    category VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_sku ON products(sku);
CREATE INDEX idx_category ON products(category);
CREATE INDEX idx_status ON products(status);
CREATE INDEX idx_created_at ON products(created_at DESC);

-- Comments
COMMENT ON TABLE products IS 'Product catalog table';
COMMENT ON COLUMN products.sku IS 'Stock Keeping Unit - unique identifier';
COMMENT ON COLUMN products.status IS 'Product status: ACTIVE or INACTIVE';
```

---

## 7. Testing Strategy

### 7.1 Unit Tests (60% da pirâmide)

```java
// Domain
ProductTest.java - testar lógica de domínio
MoneyTest.java - testar value object

// Application
CreateProductUseCaseTest.java
DecreaseStockUseCaseTest.java
... (um teste por use case)

// Mock dependencies
@Mock ProductRepository productRepository
@Mock ProductMapper productMapper
```

### 7.2 Integration Tests (30% da pirâmide)

```java
@SpringBootTest
@Testcontainers
class ProductIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
        .withDatabaseName("test_db")
        .withUsername("test")
        .withPassword("test");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
        .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }

    @Test
    void shouldCreateProduct_WhenValidData() {
        // Given
        CreateProductRequest request = CreateProductRequest.builder()
            .name("Test Product")
            .price(new BigDecimal("99.99"))
            .stockQuantity(10)
            .sku("TEST-001")
            .category(ProductCategory.ELECTRONICS)
            .build();

        // When
        ResponseEntity<ProductResponse> response = restTemplate
            .postForEntity("/api/v1/products", request, ProductResponse.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getName()).isEqualTo("Test Product");
    }
}
```

---

## 8. Quality Metrics

### Coverage Target: 95%

```yaml
Lines: 95%
Branches: 90%
Methods: 95%
Classes: 100%
```

### SonarQube Quality Gate

```yaml
Bugs: 0 (blocker/critical)
Vulnerabilities: 0
Code Smells: < 50
Coverage: ≥ 95%
Duplications: < 3%
```

---

## 9. Performance Targets

```yaml
Endpoints:
  GET /products/{id}:
    - Latência média: < 50ms (cache hit)
    - Latência P99: < 200ms
    - Cache hit rate: > 80%

  POST /products:
    - Latência média: < 100ms
    - Latência P99: < 300ms

  POST /products/{id}/decrease-stock:
    - Latência média: < 150ms (com lock)
    - Latência P99: < 400ms
```

---

## 10. Próximos Passos

Após aprovação deste design:

1. ✅ Criar `tasks.md` - quebrar em tarefas rastreáveis
2. ✅ Implementar com TDD (teste → código → refactor)
3. ✅ Validar implementação contra requirements
4. ✅ Code review e quality checks
5. ✅ Commit e PR

---

**Aprovação Necessária**: ✅ Aguardando validação humana antes de prosseguir para tasks.md

**Próximo Passo**: Após aprovação → Criar `tasks.md` e iniciar implementação TDD
