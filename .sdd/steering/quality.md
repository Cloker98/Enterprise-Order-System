# Quality Steering - Padrões de Qualidade

## 🎯 Objetivos de Qualidade

```yaml
Code Coverage: ≥ 95%
Build Time: < 5 minutos
Code Smells: < 50
Bugs: 0 (blocker/critical)
Vulnerabilities: 0
Technical Debt: < 5%
Duplications: < 3%
```

---

## 🧪 Estratégia de Testes

### Pirâmide de Testes

```
           /\
          /E2E\           10% - End-to-End (Cucumber)
         /------\
        /Integration\     30% - Integration (Testcontainers)
       /------------\
      /  Unit Tests  \    60% - Unit (jUnit + Mockito)
     /----------------\
```

### 1️⃣ Testes Unitários (60%)

**Objetivo**: Testar lógica isolada, sem dependências externas.

```java
// ✅ EXEMPLO - Domain Test
@DisplayName("Product Domain Tests")
class ProductTest {

    @Test
    @DisplayName("Should decrease stock when sufficient quantity available")
    void decreaseStock_WhenSufficientQuantity_ShouldSucceed() {
        // Given
        Product product = new Product(
            ProductId.of(1L),
            "Laptop",
            Price.of(1500.00),
            10  // stock
        );

        // When
        product.decreaseStock(5);

        // Then
        assertThat(product.getStockQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("Should throw exception when insufficient stock")
    void decreaseStock_WhenInsufficientQuantity_ShouldThrowException() {
        // Given
        Product product = new Product(ProductId.of(1L), "Laptop", Price.of(1500.00), 3);

        // When / Then
        assertThatThrownBy(() -> product.decreaseStock(5))
            .isInstanceOf(InsufficientStockException.class)
            .hasMessageContaining("Insufficient stock");
    }
}

// ✅ EXEMPLO - Use Case Test (com mocks)
@ExtendWith(MockitoExtension.class)
class CreateProductUseCaseTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductEventPublisher eventPublisher;

    @InjectMocks
    private CreateProductUseCase useCase;

    @Test
    void execute_WhenValidRequest_ShouldCreateProduct() {
        // Given
        CreateProductRequest request = new CreateProductRequest("Laptop", 1500.00, 10);
        Product product = Product.create(request.name(), Price.of(request.price()), request.stock());

        when(productRepository.save(any(Product.class))).thenReturn(product);

        // When
        ProductResponse response = useCase.execute(request);

        // Then
        assertThat(response.name()).isEqualTo("Laptop");
        verify(productRepository, times(1)).save(any(Product.class));
        verify(eventPublisher, times(1)).publish(any(ProductCreatedEvent.class));
    }
}
```

**REGRAS**:
- ✅ Usar @ExtendWith(MockitoExtension.class) para mocks
- ✅ Nomear testes de forma descritiva
- ✅ Given-When-Then structure
- ✅ AssertJ para assertions (mais fluente)
- ❌ SEM Spring Context (testes devem ser rápidos)

---

### 2️⃣ Testes de Integração (30%)

**Objetivo**: Testar integração entre camadas com dependências reais.

```java
// ✅ EXEMPLO - Repository Integration Test (Testcontainers)
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductRepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
        .withDatabaseName("product_test")
        .withUsername("test")
        .withPassword("test");

    @Autowired
    private ProductJpaRepository repository;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    void save_WhenValidProduct_ShouldPersist() {
        // Given
        ProductJpaEntity entity = new ProductJpaEntity();
        entity.setName("Laptop");
        entity.setPrice(BigDecimal.valueOf(1500.00));
        entity.setStockQuantity(10);

        // When
        ProductJpaEntity saved = repository.save(entity);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Laptop");
    }
}

// ✅ EXEMPLO - Controller Integration Test (REST Assured)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class ProductControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    void createProduct_WhenValidRequest_ShouldReturn201() {
        CreateProductRequest request = new CreateProductRequest("Laptop", 1500.00, 10);

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/v1/products")
        .then()
            .statusCode(HttpStatus.CREATED.value())
            .body("name", equalTo("Laptop"))
            .body("price", equalTo(1500.00F))
            .body("id", notNullValue());
    }

    @Test
    void createProduct_WhenInvalidRequest_ShouldReturn400() {
        CreateProductRequest request = new CreateProductRequest("", -100.00, -5);  // Invalid

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/v1/products")
        .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }
}
```

**REGRAS**:
- ✅ Usar Testcontainers para databases/mensageria
- ✅ REST Assured para testes de API
- ✅ @SpringBootTest para contexto completo
- ✅ Limpar estado entre testes (@BeforeEach/@AfterEach)
- ❌ NÃO mockar repository em testes de integração

---

### 3️⃣ Testes BDD (10%)

**Objetivo**: Testes de comportamento em linguagem natural (Gherkin).

```gherkin
# src/test/resources/features/product.feature
Feature: Product Management
  As a system administrator
  I want to manage products
  So that customers can purchase them

  Background:
    Given the product service is running

  Scenario: Create a new product successfully
    Given I have a valid product request with name "Laptop" and price 1500.00
    When I send a POST request to "/api/v1/products"
    Then the response status should be 201
    And the response should contain the product id
    And the product name should be "Laptop"

  Scenario: Fail to create product with invalid data
    Given I have an invalid product request with empty name
    When I send a POST request to "/api/v1/products"
    Then the response status should be 400
    And the response should contain validation errors

  Scenario: Retrieve product by ID
    Given a product exists with id 1
    When I send a GET request to "/api/v1/products/1"
    Then the response status should be 200
    And the response should contain the product details

  Scenario Outline: Update product stock
    Given a product exists with stock <initial_stock>
    When I decrease stock by <quantity>
    Then the final stock should be <final_stock>

    Examples:
      | initial_stock | quantity | final_stock |
      | 10            | 3        | 7           |
      | 5             | 5        | 0           |
```

```java
// Step Definitions
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ProductSteps {

    @LocalServerPort
    private int port;

    private Response response;
    private CreateProductRequest request;

    @Before
    public void setUp() {
        RestAssured.port = port;
    }

    @Given("I have a valid product request with name {string} and price {double}")
    public void iHaveValidProductRequest(String name, Double price) {
        request = new CreateProductRequest(name, price, 10);
    }

    @When("I send a POST request to {string}")
    public void iSendPostRequest(String endpoint) {
        response = given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post(endpoint);
    }

    @Then("the response status should be {int}")
    public void theResponseStatusShouldBe(int statusCode) {
        response.then().statusCode(statusCode);
    }

    @And("the product name should be {string}")
    public void theProductNameShouldBe(String name) {
        response.then().body("name", equalTo(name));
    }
}
```

---

## 📊 Code Coverage (Jacoco)

### Configuração (pom.xml)

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
        <execution>
            <id>jacoco-check</id>
            <goals>
                <goal>check</goal>
            </goals>
            <configuration>
                <rules>
                    <rule>
                        <element>PACKAGE</element>
                        <limits>
                            <limit>
                                <counter>LINE</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.95</minimum>  <!-- 95% -->
                            </limit>
                        </limits>
                    </rule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

### Comandos

```bash
# Executar testes + gerar relatório
mvn clean test jacoco:report

# Verificar se atende mínimo (95%)
mvn jacoco:check

# Relatório HTML
# target/site/jacoco/index.html
```

---

## 🔍 SonarQube

### Configuração (pom.xml)

```xml
<properties>
    <sonar.projectKey>enterprise-order-system</sonar.projectKey>
    <sonar.organization>seu-org</sonar.organization>
    <sonar.host.url>https://sonarcloud.io</sonar.host.url>
    <sonar.coverage.jacoco.xmlReportPaths>
        ${project.build.directory}/site/jacoco/jacoco.xml
    </sonar.coverage.jacoco.xmlReportPaths>
</properties>
```

### Quality Gates

```yaml
Metrics:
  Coverage: > 90%
  Duplications: < 3%
  Maintainability Rating: A
  Reliability Rating: A
  Security Rating: A

Conditions:
  - New Code Coverage ≥ 95%
  - New Duplications ≤ 3%
  - New Bugs = 0
  - New Vulnerabilities = 0
  - New Code Smells ≤ 5
```

### Executar Análise

```bash
# Local (com SonarQube Server rodando)
mvn clean verify sonar:sonar \
  -Dsonar.projectKey=product-service \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=your-token

# SonarCloud (CI/CD)
mvn clean verify sonar:sonar \
  -Dsonar.projectKey=enterprise-order-system \
  -Dsonar.organization=seu-org \
  -Dsonar.host.url=https://sonarcloud.io \
  -Dsonar.login=$SONAR_TOKEN
```

---

## ✅ Checkstyle

### Configuração (checkstyle.xml)

```xml
<?xml version="1.0"?>
<!DOCTYPE module PUBLIC
    "-//Checkstyle//DTD Checkstyle Configuration 1.3//EN"
    "https://checkstyle.org/dtds/configuration_1_3.dtd">

<module name="Checker">
    <property name="severity" value="error"/>

    <module name="TreeWalker">
        <!-- Naming Conventions -->
        <module name="TypeName"/>
        <module name="MethodName"/>
        <module name="ConstantName"/>
        <module name="LocalVariableName"/>
        <module name="PackageName"/>

        <!-- Imports -->
        <module name="AvoidStarImport"/>
        <module name="UnusedImports"/>

        <!-- Size Violations -->
        <module name="MethodLength">
            <property name="max" value="50"/>  <!-- Max 50 linhas por método -->
        </module>
        <module name="ParameterNumber">
            <property name="max" value="5"/>  <!-- Max 5 parâmetros -->
        </module>

        <!-- Whitespace -->
        <module name="EmptyLineSeparator"/>
        <module name="WhitespaceAfter"/>
        <module name="WhitespaceAround"/>

        <!-- Coding -->
        <module name="EqualsHashCode"/>
        <module name="SimplifyBooleanExpression"/>
        <module name="SimplifyBooleanReturn"/>
        <module name="MagicNumber"/>
    </module>

    <!-- File Length -->
    <module name="FileLength">
        <property name="max" value="500"/>  <!-- Max 500 linhas por arquivo -->
    </module>
</module>
```

### Executar

```bash
mvn checkstyle:check
```

---

## 🐛 SpotBugs

### Configuração (pom.xml)

```xml
<plugin>
    <groupId>com.github.spotbugs</groupId>
    <artifactId>spotbugs-maven-plugin</artifactId>
    <version>4.8.3.1</version>
    <configuration>
        <effort>Max</effort>
        <threshold>Low</threshold>
        <xmlOutput>true</xmlOutput>
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>check</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### Executar

```bash
mvn spotbugs:check
```

---

## 📏 PMD

### Configuração (pom.xml)

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-pmd-plugin</artifactId>
    <version>3.21.2</version>
    <configuration>
        <rulesets>
            <ruleset>/rulesets/java/quickstart.xml</ruleset>
        </rulesets>
        <printFailingErrors>true</printFailingErrors>
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>check</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

---

## 🚀 Script de Qualidade Completo

```bash
#!/bin/bash
# scripts/quality-check.sh

set -e

echo "🔍 Running Quality Checks..."

echo "1️⃣ Checkstyle..."
mvn checkstyle:check

echo "2️⃣ SpotBugs..."
mvn spotbugs:check

echo "3️⃣ PMD..."
mvn pmd:check

echo "4️⃣ Tests + Coverage..."
mvn clean test jacoco:report jacoco:check

echo "5️⃣ SonarQube Analysis..."
mvn sonar:sonar -Dsonar.login=$SONAR_TOKEN

echo "✅ All quality checks passed!"
```

---

## 🎯 Padrões de Código

### 1. **Imutabilidade**

```java
// ✅ CORRETO - Record (imutável)
public record ProductResponse(
    Long id,
    String name,
    BigDecimal price,
    int stock
) { }

// ✅ CORRETO - Value Object imutável
@Value  // Lombok (final + private + getters)
public class Price {
    BigDecimal amount;

    public static Price of(double value) {
        if (value < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        return new Price(BigDecimal.valueOf(value));
    }
}

// ❌ ERRADO - Mutável desnecessariamente
public class Price {
    private BigDecimal amount;

    public void setAmount(BigDecimal amount) {  // ❌ Setter desnecessário
        this.amount = amount;
    }
}
```

### 2. **Null Safety**

```java
// ✅ CORRETO - Optional
public Optional<Product> findById(ProductId id) {
    return productRepository.findById(id);
}

// Uso
productService.findById(id)
    .ifPresent(product -> log.info("Found: {}", product))
    .orElseThrow(() -> new ProductNotFoundException(id));

// ❌ ERRADO - Retornar null
public Product findById(ProductId id) {
    return productRepository.findById(id);  // pode ser null!
}
```

### 3. **Exception Handling**

```java
// ✅ CORRETO - Custom exceptions
public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(ProductId id) {
        super("Product not found with id: " + id);
    }
}

// ✅ CORRETO - Global Exception Handler
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> handle(ProductNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            ex.getMessage(),
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handle(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .toList();

        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Validation failed",
            errors,
            LocalDateTime.now()
        );
        return ResponseEntity.badRequest().body(error);
    }
}
```

### 4. **Logging**

```java
// ✅ CORRETO - Structured logging
@Slf4j
@Service
public class ProductService {

    public ProductResponse create(CreateProductRequest request) {
        log.info("Creating product: name={}, price={}", request.name(), request.price());

        try {
            Product product = productRepository.save(product);
            log.info("Product created successfully: id={}", product.getId());
            return mapper.toResponse(product);
        } catch (Exception ex) {
            log.error("Failed to create product: name={}, error={}", request.name(), ex.getMessage(), ex);
            throw ex;
        }
    }
}

// ❌ ERRADO - System.out.println
System.out.println("Product created");  // ❌ NUNCA!
```

---

## 📋 Checklist de Qualidade (Pull Request)

Antes de abrir PR, verificar:

- [ ] ✅ Testes passando (mvn test)
- [ ] ✅ Coverage ≥ 95% (mvn jacoco:check)
- [ ] ✅ Checkstyle OK (mvn checkstyle:check)
- [ ] ✅ SpotBugs OK (mvn spotbugs:check)
- [ ] ✅ SonarQube sem bugs críticos
- [ ] ✅ Migrations testadas localmente
- [ ] ✅ Swagger atualizado
- [ ] ✅ Código revisado por você mesmo
- [ ] ✅ Commits atômicos e descritivos
- [ ] ✅ Branch atualizado com develop

---

**Última atualização**: 2026-03-09
**Responsável**: Quality Team
