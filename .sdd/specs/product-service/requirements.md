# Product Service - Requirements Specification

**Formato**: EARS (Easy Approach to Requirements Syntax)
**Versão**: 1.0
**Data**: 2026-03-09
**Status**: 📝 Draft → Aguardando aprovação

---

## 1. Contexto e Objetivos

### 1.1 Visão Geral
O **Product Service** é o microsserviço responsável pelo gerenciamento do catálogo de produtos no sistema de pedidos empresarial. Ele é a base para operações de Order Service e deve garantir:
- CRUD completo de produtos
- Cache distribuído para performance
- Controle de estoque
- Validações de domínio

### 1.2 Objetivos
- ✅ Implementar CRUD de produtos com validações de negócio
- ✅ Implementar cache Redis para reduzir latência (target: < 50ms)
- ✅ Implementar controle de estoque com concorrência segura
- ✅ Expor API REST documentada com Swagger
- ✅ Garantir cobertura de testes ≥ 95%
- ✅ Seguir Hexagonal Architecture (DDD)

---

## 2. Requisitos Funcionais (EARS)

### RF-001: Criar Produto
**WHEN** o usuário envia uma requisição POST /api/v1/products com dados válidos,
**THE SYSTEM SHALL** criar um novo produto no banco de dados e retornar status 201 com o produto criado.

**Validações Obrigatórias**:
- Nome: não pode ser nulo ou vazio, max 200 caracteres
- Descrição: max 1000 caracteres
- Preço: > 0, max 2 casas decimais
- Estoque: ≥ 0
- Categoria: enum válido (ELECTRONICS, CLOTHING, FOOD, BOOKS, OTHER)
- SKU: único, formato alfanumérico, max 50 caracteres

**Regras de Negócio**:
- SKU deve ser único no sistema
- Produto criado com status ACTIVE por padrão
- Timestamp de criação deve ser automaticamente definido

---

### RF-002: Buscar Produto por ID
**WHEN** o usuário envia uma requisição GET /api/v1/products/{id},
**IF** o produto existe,
**THE SYSTEM SHALL** retornar status 200 com os dados do produto.

**WHEN** o usuário envia uma requisição GET /api/v1/products/{id},
**IF** o produto NÃO existe,
**THE SYSTEM SHALL** retornar status 404 com mensagem de erro.

**Performance**:
- Cache Redis DEVE ser consultado primeiro
- Se cache miss, buscar no PostgreSQL e cachear resultado
- TTL do cache: 5 minutos

---

### RF-003: Listar Produtos (Paginado)
**WHEN** o usuário envia uma requisição GET /api/v1/products,
**THE SYSTEM SHALL** retornar lista paginada de produtos com status 200.

**Parâmetros de Paginação**:
- `page` (default: 0)
- `size` (default: 20, max: 100)
- `sort` (default: "name,asc")

**Filtros Opcionais**:
- `category`: filtrar por categoria
- `name`: busca parcial por nome (case-insensitive)
- `minPrice` / `maxPrice`: filtro de preço

---

### RF-004: Atualizar Produto
**WHEN** o usuário envia uma requisição PUT /api/v1/products/{id} com dados válidos,
**IF** o produto existe,
**THE SYSTEM SHALL** atualizar o produto e retornar status 200 com dados atualizados.

**WHEN** o usuário envia uma requisição PUT /api/v1/products/{id},
**IF** o produto NÃO existe,
**THE SYSTEM SHALL** retornar status 404.

**Regras de Negócio**:
- Não pode alterar `id`, `createdAt`, `sku` (imutáveis)
- Validações iguais ao RF-001
- Cache DEVE ser invalidado após atualização

---

### RF-005: Deletar Produto
**WHEN** o usuário envia uma requisição DELETE /api/v1/products/{id},
**IF** o produto existe E não está vinculado a pedidos,
**THE SYSTEM SHALL** deletar o produto (soft delete) e retornar status 204.

**WHEN** o usuário envia uma requisição DELETE /api/v1/products/{id},
**IF** o produto está vinculado a pedidos,
**THE SYSTEM SHALL** retornar status 409 com mensagem de erro.

**Regras de Negócio**:
- Soft delete: alterar status para INACTIVE, manter registro
- Cache DEVE ser invalidado após deleção
- Não pode deletar produto com estoque reservado

---

### RF-006: Decrementar Estoque
**WHEN** o Order Service envia requisição POST /api/v1/products/{id}/decrease-stock com quantidade válida,
**IF** há estoque suficiente,
**THE SYSTEM SHALL** decrementar o estoque e retornar status 200.

**WHEN** há estoque insuficiente,
**THE SYSTEM SHALL** retornar status 409 com erro "InsufficientStockException".

**Regras de Negócio**:
- Operação DEVE ser atômica (pessimistic lock)
- Quantidade DEVE ser > 0
- Estoque não pode ficar negativo
- Cache DEVE ser invalidado após alteração

---

### RF-007: Incrementar Estoque (Reposição)
**WHEN** o usuário envia requisição POST /api/v1/products/{id}/increase-stock com quantidade válida,
**THE SYSTEM SHALL** incrementar o estoque e retornar status 200.

**Regras de Negócio**:
- Quantidade DEVE ser > 0
- Cache DEVE ser invalidado após alteração

---

## 3. Requisitos Não-Funcionais

### RNF-001: Performance
- **Latência média**: < 50ms (com cache hit)
- **Latência P99**: < 200ms
- **Throughput**: > 1000 req/s

### RNF-002: Disponibilidade
- **SLA**: 99.9% (uptime mensal)
- **Circuit Breaker**: para chamadas ao Redis (fallback: buscar direto no DB)

### RNF-003: Segurança
- **Autenticação**: JWT via API Gateway (simplificado para demo)
- **Autorização**: RBAC (futuro - não implementar agora)
- **Validação**: sanitização de inputs (XSS, SQL Injection)

### RNF-004: Observabilidade
- **Logs estruturados**: formato JSON (SLF4J + Logback)
- **Métricas**: Micrometer + Prometheus (futuro)
- **Health checks**: /actuator/health

### RNF-005: Qualidade
- **Cobertura de testes**: ≥ 95% (line coverage)
- **SonarQube**: A (maintainability rating)
- **Bugs críticos**: 0

---

## 4. Modelo de Dados

### 4.1 Domain Entity: Product

```java
public class Product {
    private ProductId id;              // Value Object: UUID
    private String name;               // max 200 chars
    private String description;        // max 1000 chars
    private Money price;               // Value Object: BigDecimal + Currency
    private int stockQuantity;         // >= 0
    private String sku;                // unique, max 50 chars
    private ProductCategory category;  // enum
    private ProductStatus status;      // enum (ACTIVE, INACTIVE)
    private LocalDateTime createdAt;   // timestamp
    private LocalDateTime updatedAt;   // timestamp
}
```

### 4.2 Value Objects

```java
public record ProductId(UUID value) { }

public record Money(BigDecimal amount, Currency currency) {
    // Validação: amount > 0
}

public enum ProductCategory {
    ELECTRONICS, CLOTHING, FOOD, BOOKS, OTHER
}

public enum ProductStatus {
    ACTIVE, INACTIVE
}
```

---

## 5. Contratos de API

### 5.1 Criar Produto

**Request**:
```http
POST /api/v1/products
Content-Type: application/json

{
  "name": "Notebook Dell Inspiron",
  "description": "15.6 inch, Intel i7, 16GB RAM",
  "price": 3499.99,
  "stockQuantity": 50,
  "sku": "DELL-INSP-15-I7",
  "category": "ELECTRONICS"
}
```

**Response 201**:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Notebook Dell Inspiron",
  "description": "15.6 inch, Intel i7, 16GB RAM",
  "price": 3499.99,
  "stockQuantity": 50,
  "sku": "DELL-INSP-15-I7",
  "category": "ELECTRONICS",
  "status": "ACTIVE",
  "createdAt": "2026-03-09T14:30:00Z",
  "updatedAt": "2026-03-09T14:30:00Z"
}
```

**Response 400** (validação):
```json
{
  "timestamp": "2026-03-09T14:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": [
    "name: must not be blank",
    "price: must be greater than 0"
  ]
}
```

---

### 5.2 Buscar Produto

**Request**:
```http
GET /api/v1/products/550e8400-e29b-41d4-a716-446655440000
```

**Response 200**:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Notebook Dell Inspiron",
  "price": 3499.99,
  "stockQuantity": 50,
  ...
}
```

**Response 404**:
```json
{
  "timestamp": "2026-03-09T14:30:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Product not found with id: 550e8400-e29b-41d4-a716-446655440000"
}
```

---

### 5.3 Listar Produtos

**Request**:
```http
GET /api/v1/products?page=0&size=20&category=ELECTRONICS&sort=price,desc
```

**Response 200**:
```json
{
  "content": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "name": "Notebook Dell Inspiron",
      ...
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "sort": {
      "sorted": true,
      "unsorted": false
    }
  },
  "totalElements": 150,
  "totalPages": 8,
  "last": false
}
```

---

### 5.4 Decrementar Estoque

**Request**:
```http
POST /api/v1/products/550e8400-e29b-41d4-a716-446655440000/decrease-stock
Content-Type: application/json

{
  "quantity": 5
}
```

**Response 200**:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "stockQuantity": 45,
  "message": "Stock decreased successfully"
}
```

**Response 409**:
```json
{
  "timestamp": "2026-03-09T14:30:00Z",
  "status": 409,
  "error": "Conflict",
  "message": "Insufficient stock. Available: 2, Requested: 5"
}
```

---

## 6. Tratamento de Erros

### Exceções de Domínio

```java
public class ProductNotFoundException extends DomainException { }
public class InsufficientStockException extends DomainException { }
public class DuplicateSkuException extends DomainException { }
public class ValidationException extends DomainException { }
```

### Mapeamento HTTP

| Exception | HTTP Status | Message |
|-----------|-------------|---------|
| ProductNotFoundException | 404 | Product not found with id: {id} |
| InsufficientStockException | 409 | Insufficient stock. Available: {available}, Requested: {requested} |
| DuplicateSkuException | 409 | SKU already exists: {sku} |
| ValidationException | 400 | Validation failed: {details} |
| IllegalArgumentException | 400 | Invalid argument: {message} |
| RuntimeException | 500 | Internal server error |

---

## 7. Regras de Validação

### Produto
- ✅ name: NOT NULL, NOT BLANK, max 200 chars
- ✅ description: max 1000 chars
- ✅ price: > 0, max 2 decimals
- ✅ stockQuantity: >= 0
- ✅ sku: NOT NULL, UNIQUE, alphanumeric, max 50 chars
- ✅ category: valid enum value

### Operações de Estoque
- ✅ quantity: > 0
- ✅ stockQuantity após operação: >= 0

---

## 8. Dependências

### Técnicas
- Spring Boot 3.2.5
- Spring Data JPA
- Spring Cache + Redis
- PostgreSQL 16
- Flyway (migrations)
- Lombok
- MapStruct
- SpringDoc OpenAPI (Swagger)

### Testes
- jUnit 5
- Mockito
- Testcontainers (PostgreSQL + Redis)
- REST Assured

---

## 9. Critérios de Aceite

### Feature Completa Quando:
- [ ] ✅ CRUD completo implementado
- [ ] ✅ Cache Redis funcional (hit rate > 80% em testes)
- [ ] ✅ Testes unitários (coverage ≥ 95%)
- [ ] ✅ Testes de integração (Testcontainers)
- [ ] ✅ Migrations Flyway criadas
- [ ] ✅ Swagger documentado
- [ ] ✅ Health checks funcionando
- [ ] ✅ Hexagonal Architecture respeitada
- [ ] ✅ Build passando (Maven)
- [ ] ✅ SonarQube OK (quality gate)

---

## 10. Out of Scope (Não Implementar Agora)

- ❌ Autenticação/Autorização (será no API Gateway)
- ❌ Event-Driven (será na Fase 2 com RabbitMQ)
- ❌ Métricas Prometheus
- ❌ Distributed Tracing
- ❌ Rate Limiting
- ❌ GraphQL API
- ❌ Versionamento avançado de API

---

## 11. Riscos e Mitigações

| Risco | Impacto | Probabilidade | Mitigação |
|-------|---------|---------------|-----------|
| Redis indisponível | Alto | Média | Circuit Breaker + fallback para DB |
| Concorrência em estoque | Alto | Alta | Pessimistic lock + testes de concorrência |
| Performance com muitos produtos | Médio | Baixa | Paginação + índices no DB |

---

## 12. Referências

- [CLAUDE.md](../../CLAUDE.md) - Regras arquiteturais
- [.sdd/steering/tech.md](../../.sdd/steering/tech.md) - Decisões técnicas
- [.sdd/steering/structure.md](../../.sdd/steering/structure.md) - Estrutura de código

---

**Aprovação Necessária**: ✅ Aguardando validação humana antes de prosseguir para design.md

**Próximo Passo**: Após aprovação → Criar `design.md` com arquitetura detalhada
