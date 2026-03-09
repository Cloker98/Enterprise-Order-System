# Tech Steering - Decisões Técnicas

## 🏗️ Stack Core

### Backend Framework
```yaml
Language: Java 17 (LTS)
Framework: Spring Boot 3.2.5
Build Tool: Maven 3.9+
Package Manager: Maven Central
```

**Rationale**: Java 17 é requisito da vaga, Spring Boot 3.x traz performance melhorada e suporte a Virtual Threads.

---

## 📐 Arquitetura de Microsserviços

### Padrões Adotados

#### 1. **Database per Service**
- Cada serviço tem seu próprio banco de dados
- Sem compartilhamento de schemas
- Autonomia completa de dados

**Rationale**: Desacoplamento, escalabilidade independente, tecnologia específica por contexto.

#### 2. **API Gateway Pattern**
- Spring Cloud Gateway como ponto único de entrada
- Routing, load balancing, rate limiting
- Autenticação centralizada (JWT)

**Rationale**: Simplifica client-side, segurança centralizada, cross-cutting concerns.

#### 3. **Event-Driven Communication**
- RabbitMQ para comunicação assíncrona
- Eventos de domínio (OrderCreated, PaymentProcessed, etc)
- Pub/Sub pattern

**Rationale**: Desacoplamento temporal, resiliência, escalabilidade.

#### 4. **SAGA Pattern (Orchestration)**
- Order Service como orquestrador
- Compensating transactions em caso de falha
- Estado da SAGA persistido

**Rationale**: Transações distribuídas sem 2PC, rastreabilidade, rollback controlado.

#### 5. **Circuit Breaker Pattern**
- Resilience4j em chamadas externas
- Payment Service → Gateway de pagamento
- Fallback strategies

**Rationale**: Resiliência, fail-fast, evita cascading failures.

---

## 🗄️ Databases

### Estratégia por Serviço

| Serviço | Database | Rationale |
|---------|----------|-----------|
| **Product** | PostgreSQL | Relacional, ACID, queries complexas |
| **Order** | PostgreSQL | Transações, integridade referencial |
| **Payment** | Oracle 21c | **Requisito da vaga**, enterprise-grade |
| **Notification** | MongoDB | Schema flexível, logs não-estruturados |
| **Cache** | Redis | In-memory, TTL, distributed cache |

### Versionamento de Schema

```yaml
Tool: Flyway
Convention: V{version}__{description}.sql
Example: V001__create_products_table.sql
Location: src/main/resources/db/migration
```

**Regras**:
- ✅ Migrations SEMPRE versionadas
- ✅ Rollback via nova migration (não editar existentes)
- ✅ Testar em ambiente local antes de commit
- ❌ NUNCA dropar tabelas em prod sem backup

---

## 🔧 Spring Boot - Convenções

### Estrutura de Pacotes (Hexagonal Architecture)

```
com.enterprise.{service-name}
├── domain/               # Entities, Value Objects, Domain Services
│   ├── model/
│   ├── service/
│   └── repository/       # Interfaces (ports)
├── application/          # Use Cases, DTOs
│   ├── usecase/
│   ├── dto/
│   └── mapper/
├── infrastructure/       # Implementações (adapters)
│   ├── persistence/      # JPA Repositories
│   ├── messaging/        # RabbitMQ/Kafka
│   ├── rest/             # Controllers
│   └── config/           # Spring Configuration
└── OrderServiceApplication.java
```

**Rationale**: Clean Architecture, testabilidade, separação de concerns.

### Nomenclatura

```java
// Controllers
@RestController
@RequestMapping("/api/v1/products")
public class ProductController { }

// Use Cases
public class CreateProductUseCase { }

// Repositories
public interface ProductRepository extends JpaRepository<Product, Long> { }

// Entities
@Entity
@Table(name = "products")
public class Product { }

// DTOs
public record ProductRequestDTO(...) { }
public record ProductResponseDTO(...) { }

// Mappers
@Mapper(componentModel = "spring")
public interface ProductMapper { }
```

### Configurações

```yaml
# application.yml (padrão)
spring:
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:local}
  application:
    name: product-service

# application-local.yml
# application-dev.yml
# application-prod.yml
```

---

## 📡 API Design

### REST Conventions

```http
# Resources (plural)
GET    /api/v1/products          # List
GET    /api/v1/products/{id}     # Get by ID
POST   /api/v1/products          # Create
PUT    /api/v1/products/{id}     # Update (full)
PATCH  /api/v1/products/{id}     # Update (partial)
DELETE /api/v1/products/{id}     # Delete

# HTTP Status Codes
200 OK                 # Success (GET, PUT, PATCH)
201 Created            # Success (POST)
204 No Content         # Success (DELETE)
400 Bad Request        # Validation error
404 Not Found          # Resource not found
409 Conflict           # Business rule violation
500 Internal Error     # Unexpected error
```

### Swagger/OpenAPI

```yaml
Tool: SpringDoc OpenAPI 3
Annotations: @Operation, @ApiResponse, @Schema
UI: /swagger-ui.html
JSON Spec: /v3/api-docs
```

**Obrigatório**:
- ✅ Documentar TODOS os endpoints
- ✅ Exemplos de request/response
- ✅ Status codes esperados
- ✅ Descrição dos campos

---

## 🔐 Segurança

### Autenticação/Autorização

```yaml
Strategy: JWT (JSON Web Token)
Library: Spring Security + jjwt
Flow:
  1. Client → POST /auth/login (username, password)
  2. API Gateway valida e retorna JWT
  3. Client envia JWT em header: Authorization: Bearer {token}
  4. Gateway valida e roteia para serviços
```

**Nota**: Por ser projeto de demonstração, autenticação será **simplificada** (sem OAuth2/OIDC real).

---

## 📨 Mensageria

### RabbitMQ

```yaml
Exchanges:
  - order.events (topic)
  - payment.events (topic)
  - notification.events (fanout)

Queues:
  - order.created
  - order.cancelled
  - payment.processed
  - payment.failed
  - notification.email
  - notification.sms

Routing Keys:
  - order.created
  - order.cancelled.{reason}
  - payment.processed.{method}
```

### Mensagens (Event Schema)

```json
{
  "eventId": "uuid",
  "eventType": "OrderCreated",
  "timestamp": "2026-03-09T10:30:00Z",
  "aggregateId": "order-123",
  "payload": {
    // domain-specific data
  },
  "metadata": {
    "userId": "user-456",
    "correlationId": "correlation-789"
  }
}
```

---

## 🧪 Testes

### Pirâmide de Testes

```
        /\
       /E2E\         10% - Testes End-to-End
      /------\
     /Integration\   30% - Testes de Integração
    /------------\
   /   Unit Tests  \ 60% - Testes Unitários
  /----------------\
```

### Ferramentas

```yaml
Unit:
  - jUnit 5
  - Mockito
  - AssertJ

Integration:
  - Spring Boot Test
  - Testcontainers (PostgreSQL, Oracle, MongoDB, Redis, RabbitMQ)
  - REST Assured (API tests)

BDD:
  - Cucumber JVM
  - Gherkin syntax

Coverage:
  - Jacoco
  - Target: 95%+
```

### Convenções de Testes

```java
// Naming: {MethodName}_{Scenario}_{ExpectedBehavior}
@Test
void createProduct_WhenValidData_ShouldReturnCreatedProduct() { }

@Test
void createProduct_WhenInvalidPrice_ShouldThrowValidationException() { }

// Given-When-Then (BDD style)
@Test
void shouldProcessPaymentSuccessfully() {
    // Given
    var order = createValidOrder();
    var payment = createValidPayment();

    // When
    var result = paymentService.process(payment);

    // Then
    assertThat(result.getStatus()).isEqualTo(PaymentStatus.APPROVED);
}
```

---

## 🐳 Containerização

### Docker

```dockerfile
# Multi-stage build
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Docker Compose

```yaml
# Ambiente local com TODAS as dependências
services:
  - PostgreSQL (Product, Order)
  - Oracle XE 21c (Payment)
  - MongoDB (Notification)
  - Redis (Cache)
  - RabbitMQ (Mensageria)
```

---

## ☸️ Kubernetes

### Manifests

```yaml
Resources:
  - Deployment (cada serviço)
  - Service (ClusterIP)
  - ConfigMap (configurações)
  - Secret (senhas, tokens)
  - HorizontalPodAutoscaler (HPA)
  - Ingress (API Gateway)

Labels:
  app: {service-name}
  version: v1
  environment: {env}
  managed-by: helm
```

### Helm

```yaml
Chart Structure:
  charts/enterprise-order-system/
    ├── Chart.yaml
    ├── values.yaml
    ├── values-dev.yaml
    ├── values-prod.yaml
    └── templates/
        ├── deployment.yaml
        ├── service.yaml
        ├── configmap.yaml
        └── secret.yaml
```

---

## 🔍 Observabilidade

### Logging

```yaml
Format: JSON (structured)
Library: Logback + Logstash Encoder
Level: INFO (prod), DEBUG (dev)
Fields:
  - timestamp
  - level
  - service
  - traceId
  - spanId
  - message
```

### Metrics

```yaml
Tool: Micrometer + Prometheus
Endpoint: /actuator/prometheus
Metrics:
  - JVM (heap, threads, GC)
  - HTTP (request rate, latency)
  - Custom business metrics
```

### Health Checks

```yaml
Endpoint: /actuator/health
Components:
  - Database
  - RabbitMQ
  - Redis
  - Disk space
```

---

## 📦 Dependências (pom.xml padrão)

```xml
<dependencies>
    <!-- Spring Boot Starters -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>

    <!-- OpenAPI -->
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        <version>2.3.0</version>
    </dependency>

    <!-- MapStruct -->
    <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct</artifactId>
        <version>1.5.5.Final</version>
    </dependency>

    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>

    <!-- Testing -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>junit-jupiter</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

---

## 🚀 CI/CD

### GitHub Actions

```yaml
Workflows:
  1. CI (Build + Test)
     - Trigger: Push em qualquer branch
     - Maven build
     - jUnit tests
     - Jacoco coverage
     - Docker build

  2. Quality (SonarQube)
     - Trigger: PR para develop/main
     - SonarQube scan
     - Quality gates

  3. CD (Deploy)
     - Trigger: Push na main
     - Build images
     - Push to Docker Hub
     - Deploy K8s (staging)
```

---

## ⚠️ REGRAS CRÍTICAS (NÃO VIOLAR)

### 1. **Nunca commitar secrets**
```bash
# .gitignore SEMPRE deve conter:
*.env
application-secret.yml
*.p12
*.jks
```

### 2. **Migrations são imutáveis**
- ✅ Nova migration para corrigir
- ❌ Editar migration já commitada

### 3. **Testes devem ser idempotentes**
- Limpar estado antes/depois
- Não depender de ordem de execução

### 4. **DTOs NUNCA são Entities**
- Controller recebe/retorna DTOs
- Service trabalha com Domain Models
- Mapper entre camadas

### 5. **Exception Handling centralizado**
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    // Tratar TODAS as exceptions
}
```

---

**Última atualização**: 2026-03-09
**Responsável**: Architecture Team
