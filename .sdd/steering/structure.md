# Structure Steering - Convenções de Estrutura

## 📁 Estrutura de Microsserviço (Padrão)

Cada microsserviço segue **Hexagonal Architecture** (Ports & Adapters):

```
{service-name}/
├── src/
│   ├── main/
│   │   ├── java/com/enterprise/{service}/
│   │   │   ├── domain/                    # 🟦 DOMAIN LAYER (Core Business)
│   │   │   │   ├── model/
│   │   │   │   │   ├── Product.java       # Entity (Aggregate Root)
│   │   │   │   │   ├── ProductId.java     # Value Object
│   │   │   │   │   └── Price.java         # Value Object
│   │   │   │   ├── service/
│   │   │   │   │   └── ProductDomainService.java
│   │   │   │   ├── repository/            # Port (Interface)
│   │   │   │   │   └── ProductRepository.java
│   │   │   │   ├── event/
│   │   │   │   │   └── ProductCreatedEvent.java
│   │   │   │   └── exception/
│   │   │   │       └── ProductNotFoundException.java
│   │   │   │
│   │   │   ├── application/               # 🟩 APPLICATION LAYER (Use Cases)
│   │   │   │   ├── usecase/
│   │   │   │   │   ├── CreateProductUseCase.java
│   │   │   │   │   ├── FindProductUseCase.java
│   │   │   │   │   └── UpdateProductUseCase.java
│   │   │   │   ├── dto/
│   │   │   │   │   ├── request/
│   │   │   │   │   │   └── CreateProductRequest.java
│   │   │   │   │   └── response/
│   │   │   │   │       └── ProductResponse.java
│   │   │   │   ├── mapper/
│   │   │   │   │   └── ProductMapper.java  # MapStruct
│   │   │   │   └── service/
│   │   │   │       └── ProductApplicationService.java
│   │   │   │
│   │   │   └── infrastructure/            # 🟨 INFRASTRUCTURE LAYER (Adapters)
│   │   │       ├── persistence/
│   │   │       │   ├── entity/
│   │   │       │   │   └── ProductJpaEntity.java
│   │   │       │   ├── repository/
│   │   │       │   │   └── ProductJpaRepository.java
│   │   │       │   └── mapper/
│   │   │       │       └── ProductEntityMapper.java
│   │   │       ├── rest/
│   │   │       │   ├── controller/
│   │   │       │   │   └── ProductController.java
│   │   │       │   └── exception/
│   │   │       │       └── GlobalExceptionHandler.java
│   │   │       ├── messaging/
│   │   │       │   ├── publisher/
│   │   │       │   │   └── ProductEventPublisher.java
│   │   │       │   └── consumer/
│   │   │       │       └── OrderEventConsumer.java
│   │   │       └── config/
│   │   │           ├── DatabaseConfig.java
│   │   │           ├── RabbitMQConfig.java
│   │   │           ├── SwaggerConfig.java
│   │   │           └── SecurityConfig.java
│   │   │
│   │   └── resources/
│   │       ├── db/
│   │       │   └── migration/
│   │       │       ├── V001__create_products_table.sql
│   │       │       └── V002__add_indexes.sql
│   │       ├── application.yml
│   │       ├── application-local.yml
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       └── logback-spring.xml
│   │
│   └── test/
│       ├── java/com/enterprise/{service}/
│       │   ├── domain/
│       │   │   └── model/
│       │   │       └── ProductTest.java           # Unit (Domain)
│       │   ├── application/
│       │   │   └── usecase/
│       │   │       └── CreateProductUseCaseTest.java  # Unit
│       │   ├── infrastructure/
│       │   │   ├── persistence/
│       │   │   │   └── ProductRepositoryIntegrationTest.java  # Integration
│       │   │   └── rest/
│       │   │       └── ProductControllerIntegrationTest.java  # Integration
│       │   └── bdd/
│       │       ├── steps/
│       │       │   └── ProductSteps.java          # Cucumber Steps
│       │       └── CucumberTest.java
│       └── resources/
│           ├── features/
│           │   └── product.feature                # Gherkin
│           └── application-test.yml
│
├── pom.xml
├── Dockerfile
├── .dockerignore
└── README.md
```

---

## 🧩 Responsabilidade das Camadas

### 1️⃣ Domain Layer (Core)

**O QUE**: Lógica de negócio pura, agnóstica de tecnologia.

```java
// ✅ CORRETO - Domain Entity
@Getter
@AllArgsConstructor
public class Product {
    private ProductId id;
    private String name;
    private Price price;
    private int stockQuantity;

    // Business logic (domain rules)
    public void decreaseStock(int quantity) {
        if (quantity > stockQuantity) {
            throw new InsufficientStockException(id, quantity, stockQuantity);
        }
        this.stockQuantity -= quantity;
    }

    public boolean isAvailable() {
        return stockQuantity > 0 && price.isPositive();
    }
}

// ❌ ERRADO - Domain com anotações JPA
@Entity  // ❌ Nunca!
@Table(name = "products")  // ❌ Nunca!
public class Product { }
```

**REGRAS**:
- ✅ Apenas POJOs (Plain Old Java Objects)
- ✅ Lógica de negócio (validações, cálculos)
- ✅ Immutability onde possível
- ❌ SEM anotações de framework (JPA, Jackson, etc)
- ❌ SEM dependências externas (Spring, Jakarta, etc)

---

### 2️⃣ Application Layer (Use Cases)

**O QUE**: Orquestração de casos de uso, coordena domain + infra.

```java
// ✅ CORRETO - Use Case
@Service
@RequiredArgsConstructor
@Transactional
public class CreateProductUseCase {
    private final ProductRepository productRepository;  // Port (interface)
    private final ProductEventPublisher eventPublisher;  // Port (interface)
    private final ProductMapper mapper;

    public ProductResponse execute(CreateProductRequest request) {
        // 1. Map DTO → Domain
        Product product = mapper.toDomain(request);

        // 2. Business validation (delegada ao domain)
        product.validate();

        // 3. Persist
        Product saved = productRepository.save(product);

        // 4. Publish event
        eventPublisher.publish(new ProductCreatedEvent(saved.getId()));

        // 5. Map Domain → DTO
        return mapper.toResponse(saved);
    }
}
```

**REGRAS**:
- ✅ Orquestrar chamadas (repository, eventos, etc)
- ✅ Transações (@Transactional)
- ✅ Trabalhar com DTOs (entrada/saída)
- ✅ Delegar lógica de negócio ao Domain
- ❌ SEM lógica de negócio (isso é Domain!)
- ❌ SEM detalhes de implementação (SQL, REST, etc)

---

### 3️⃣ Infrastructure Layer (Adapters)

**O QUE**: Implementação de portas, detalhes técnicos.

#### 📦 Persistence

```java
// JPA Entity (NÃO é Domain Entity!)
@Entity
@Table(name = "products")
@Data
public class ProductJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private BigDecimal price;
    private Integer stockQuantity;
}

// Repository Implementation
@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {
    private final ProductJpaRepository jpaRepository;
    private final ProductEntityMapper entityMapper;

    @Override
    public Product save(Product product) {
        ProductJpaEntity entity = entityMapper.toEntity(product);
        ProductJpaEntity saved = jpaRepository.save(entity);
        return entityMapper.toDomain(saved);
    }
}
```

#### 🌐 REST

```java
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product management API")
public class ProductController {
    private final CreateProductUseCase createProductUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new product")
    public ResponseEntity<ProductResponse> create(
        @Valid @RequestBody CreateProductRequest request
    ) {
        ProductResponse response = createProductUseCase.execute(request);
        return ResponseEntity.created(URI.create("/api/v1/products/" + response.id()))
            .body(response);
    }
}
```

---

## 📝 Nomenclatura (Convenções)

### Classes

| Tipo | Sufixo | Exemplo |
|------|--------|---------|
| Entity (Domain) | - | `Product`, `Order` |
| Value Object | - | `Price`, `Email`, `Address` |
| Repository (Port) | Repository | `ProductRepository` |
| Use Case | UseCase | `CreateProductUseCase` |
| Controller | Controller | `ProductController` |
| Service | Service | `ProductApplicationService` |
| JPA Entity | JpaEntity | `ProductJpaEntity` |
| JPA Repo | JpaRepository | `ProductJpaRepository` |
| DTO Request | Request | `CreateProductRequest` |
| DTO Response | Response | `ProductResponse` |
| Mapper | Mapper | `ProductMapper` |
| Exception | Exception | `ProductNotFoundException` |
| Event | Event | `ProductCreatedEvent` |
| Config | Config | `DatabaseConfig` |

### Métodos

```java
// Use Cases
public ProductResponse execute(CreateProductRequest request)

// Repository
Product save(Product product);
Optional<Product> findById(ProductId id);
List<Product> findAll();
void deleteById(ProductId id);

// Domain (business logic)
void decreaseStock(int quantity);
boolean isAvailable();
void validate();

// Controller
ResponseEntity<ProductResponse> create(@RequestBody CreateProductRequest request);
ResponseEntity<ProductResponse> findById(@PathVariable Long id);
ResponseEntity<List<ProductResponse>> findAll();
ResponseEntity<Void> delete(@PathVariable Long id);
```

---

## 🗂️ Pacotes (Package Naming)

```
com.enterprise.{service-name}.{layer}.{feature}
```

### Exemplos

```
# Product Service
com.enterprise.product.domain.model
com.enterprise.product.domain.repository
com.enterprise.product.application.usecase
com.enterprise.product.application.dto.request
com.enterprise.product.infrastructure.rest.controller

# Order Service
com.enterprise.order.domain.model
com.enterprise.order.application.usecase
com.enterprise.order.infrastructure.persistence.entity
```

---

## 🧪 Testes (Estrutura)

### Localização

```
# Mirror da estrutura de src/main
src/test/java/com/enterprise/{service}/
├── domain/
│   └── model/
│       └── ProductTest.java                    # Unit (sem Spring)
├── application/
│   └── usecase/
│       └── CreateProductUseCaseTest.java       # Unit (com mocks)
└── infrastructure/
    ├── persistence/
    │   └── ProductRepositoryIntegrationTest.java  # Integration (Testcontainers)
    └── rest/
        └── ProductControllerIntegrationTest.java  # Integration (REST Assured)
```

### Nomenclatura de Testes

```java
// Pattern: {MethodName}_{Scenario}_{ExpectedBehavior}

// ✅ CORRETO
@Test
void createProduct_WhenValidData_ShouldReturnCreatedProduct() { }

@Test
void createProduct_WhenNullName_ShouldThrowValidationException() { }

@Test
void decreaseStock_WhenInsufficientQuantity_ShouldThrowException() { }

// ❌ ERRADO (não descritivo)
@Test
void test1() { }

@Test
void testCreateProduct() { }
```

---

## 🔧 Arquivos de Configuração

### application.yml (Hierarquia)

```yaml
# 1. application.yml (base - valores padrão)
spring:
  application:
    name: product-service
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:local}

# 2. application-local.yml (override para dev local)
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/product_db
    username: admin
    password: admin123

# 3. application-dev.yml (override para ambiente dev)
spring:
  datasource:
    url: jdbc:postgresql://dev-db:5432/product_db
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

# 4. application-prod.yml (override para produção)
spring:
  datasource:
    url: jdbc:postgresql://prod-db:5432/product_db
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

---

## 📋 Migrations (Flyway)

### Convenção de Nomenclatura

```
V{version}__{description}.sql
```

### Exemplos

```sql
-- V001__create_products_table.sql
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price NUMERIC(10, 2) NOT NULL,
    stock_quantity INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP
);

-- V002__add_category_to_products.sql
ALTER TABLE products ADD COLUMN category VARCHAR(100);

-- V003__create_indexes.sql
CREATE INDEX idx_products_name ON products(name);
CREATE INDEX idx_products_category ON products(category);
```

**REGRAS**:
- ✅ Versionamento sequencial (V001, V002, V003)
- ✅ Descrição clara e concisa
- ✅ Underscore duplo `__` entre versão e descrição
- ❌ NUNCA editar migration já aplicada (criar nova)

---

## 🐳 Dockerfile (Padrão)

```dockerfile
# Multi-stage build para otimizar tamanho da imagem

# Stage 1: Build
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

# Copy Maven files
COPY pom.xml .
COPY src ./src

# Build (pular testes para CI separado)
RUN ./mvnw clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Criar usuário não-root (segurança)
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy JAR do stage anterior
COPY --from=build /app/target/*.jar app.jar

# Expor porta
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Entrypoint
ENTRYPOINT ["java", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", \
  "app.jar"]
```

---

## 📦 pom.xml (Estrutura Padrão)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.5</version>
    </parent>

    <groupId>com.enterprise</groupId>
    <artifactId>{service-name}</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <name>{Service Name}</name>
    <description>{Service Description}</description>

    <properties>
        <java.version>17</java.version>
        <spring-cloud.version>2023.0.1</spring-cloud.version>
        <mapstruct.version>1.5.5.Final</mapstruct.version>
        <testcontainers.version>1.19.7</testcontainers.version>
    </properties>

    <dependencies>
        <!-- Spring Boot Starters -->
        <!-- OpenAPI -->
        <!-- Database -->
        <!-- Messaging -->
        <!-- Testing -->
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
            <plugin>
                <groupId>org.jacoco</groupId>
                <artifactId>jacoco-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

---

## 🚫 Anti-Patterns (NÃO FAZER)

### ❌ Anemic Domain Model

```java
// ❌ ERRADO - Apenas getters/setters, sem lógica
public class Product {
    private Long id;
    private String name;
    private BigDecimal price;
    // apenas getters/setters
}

// ✅ CORRETO - Domain com lógica
public class Product {
    public void decreaseStock(int quantity) {
        // validações + lógica de negócio
    }
}
```

### ❌ God Classes

```java
// ❌ ERRADO - Classe faz tudo
public class ProductService {
    public void create() { }
    public void update() { }
    public void delete() { }
    public void sendEmail() { }  // ❌ Não deveria estar aqui!
    public void generateReport() { }  // ❌ Não deveria estar aqui!
}
```

### ❌ Magic Numbers/Strings

```java
// ❌ ERRADO
if (status == 1) { }
if (type.equals("CREDIT_CARD")) { }

// ✅ CORRETO - Usar enums
if (status == OrderStatus.PENDING) { }
if (type == PaymentType.CREDIT_CARD) { }
```

---

**Última atualização**: 2026-03-09
**Responsável**: Architecture Team
