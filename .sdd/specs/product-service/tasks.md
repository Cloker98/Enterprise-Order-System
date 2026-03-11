# Product Service - Implementation Tasks

**Versão**: 1.1
**Data**: 2026-03-10
**Status**: 🚀 Em Progresso - 17/38 tasks concluídas (45%)
**Última atualização**: 2026-03-10 12:45

---

## 📊 Progress Summary

| Phase | Tasks | Status | Completion |
|-------|-------|--------|------------|
| 1. Setup & Infrastructure | TASK-001 a TASK-004 | ✅ Concluída | 4/4 (100%) |
| 2. Domain Layer | TASK-005 a TASK-008 | ✅ Concluída | 4/4 (100%) |
| 3. Infrastructure Adapters | TASK-009 a TASK-010 | ✅ Concluída | 2/2 (100%) |
| 4. Application Layer | TASK-011 a TASK-015 | ✅ Concluída | 5/5 (100%) |
| 5. REST Layer | TASK-016 a TASK-017 | ✅ Concluída | 2/2 (100%) |
| **6. RF-003 Implementation** | **TASK-031 a TASK-038** | **⏳ Nova** | **0/8 (0%)** |
| 7. Integration Tests | TASK-018 a TASK-020 | ✅ Concluída | 3/3 (100%) |
| 8. Documentation | TASK-021 a TASK-023 | ⏳ Pendente | 0/3 (0%) |
| 9. Quality & CI | TASK-024 a TASK-027 | ⏳ Pendente | 0/4 (0%) |
| 10. Final Validation | TASK-028 a TASK-030 | ⏳ Pendente | 0/3 (0%) |
| **TOTAL** | **38 tasks** | **45%** | **17/38** |

### Commits Realizados
- `19b1d44` - Initial project setup
- `a76b60b` - Maven setup (TASK-001)
- `ceda891` - Migration + Value Objects (TASK-002, TASK-005)
- `50462f7` - Infrastructure Layer (TASK-003, TASK-009, TASK-010)
- `f8349b4` - Application Layer (TASK-011 a TASK-015)
- `2393d81` - REST Layer (TASK-016, TASK-017)
- `latest` - Integration Tests funcionando (TASK-018 a TASK-020)

### Próxima Milestone
🎯 **Implementar RF-003 (Listar Produtos Paginado)** → TASK-031 a TASK-038

---

## Rastreabilidade

Cada task está mapeada aos **Requirements** (requirements.md):

- **RF-xxx**: Requisito Funcional
- **RNF-xxx**: Requisito Não-Funcional

---

## Task Breakdown (TDD Order)

### Phase 1: Setup & Infrastructure (2-3 horas) ✅ CONCLUÍDA

#### TASK-001: Setup Maven Project Structure ✅
**Status**: ✅ **CONCLUÍDA** (Commit: a76b60b)

#### TASK-002: Setup Database Migrations ✅
**Status**: ✅ **CONCLUÍDA** (Commit: ceda891)

#### TASK-003: Setup Redis Configuration ✅
**Status**: ✅ **CONCLUÍDA** (Commit: 50462f7)

#### TASK-004: Setup Testcontainers ✅
**Status**: ✅ **CONCLUÍDA** (Resolvido problema Docker connectivity)

---

### Phase 2: Domain Layer (TDD) ✅ CONCLUÍDA

#### TASK-005: Implement Value Objects (TDD) ✅
**Status**: ✅ **CONCLUÍDA**

#### TASK-006: Implement Product Aggregate (TDD) ✅
**Status**: ✅ **CONCLUÍDA**

#### TASK-007: Implement Domain Exceptions ✅
**Status**: ✅ **CONCLUÍDA**

#### TASK-008: Define Repository Port (Interface) ✅
**Status**: ✅ **CONCLUÍDA**

---

### Phase 3: Infrastructure Layer (TDD) ✅ CONCLUÍDA

#### TASK-009: Implement JPA Entity & Mapper (TDD) ✅
**Status**: ✅ **CONCLUÍDA**

#### TASK-010: Implement Repository Adapter (TDD) ✅
**Status**: ✅ **CONCLUÍDA**

---

### Phase 4: Application Layer (TDD) ✅ CONCLUÍDA

#### TASK-011: Implement DTOs & Mapper ✅
**Status**: ✅ **CONCLUÍDA**

#### TASK-012: Implement CreateProductUseCase (TDD) ✅
**Status**: ✅ **CONCLUÍDA**

#### TASK-013: Implement GetProductUseCase (TDD) ✅
**Status**: ✅ **CONCLUÍDA**

#### TASK-014: Implement DecreaseStockUseCase (TDD) ✅
**Status**: ✅ **CONCLUÍDA**

#### TASK-015: Implement Remaining Use Cases (TDD) ✅
**Status**: ✅ **CONCLUÍDA** (Exceto RF-003 que será implementado agora)

---

### Phase 5: REST Layer (TDD) ✅ CONCLUÍDA

#### TASK-016: Implement Global Exception Handler ✅
**Status**: ✅ **CONCLUÍDA**

#### TASK-017: Implement ProductController (TDD) ✅
**Status**: ✅ **CONCLUÍDA** (Exceto endpoint de listagem)

---

### **Phase 6: RF-003 Implementation (5h) 🆕 NOVA FASE**

#### TASK-031: Extend Repository Interface for Pagination ⏳
**Prioridade**: CRITICAL
**Tempo estimado**: 15 min
**Dependências**: TASK-008
**Rastreabilidade**: RF-003

**Objetivo**: Adicionar métodos de listagem paginada no repository port.

**Checklist**:
- [ ] Adicionar método `Page<Product> findAll(Pageable pageable)` em `ProductRepository.java`
- [ ] Adicionar método `Page<Product> findByFilters(String category, String name, Pageable pageable)`
- [ ] Documentar com Javadoc os parâmetros de filtro
- [ ] Validar que interface não tem dependências de framework

**Critério de Aceite**: Interface atualizada sem quebrar testes existentes.

---

#### TASK-032: Implement Repository Pagination (TDD) ⏳
**Prioridade**: CRITICAL
**Tempo estimado**: 45 min
**Dependências**: TASK-031
**Rastreabilidade**: RF-003

**TDD Steps**:
1. **TEST**: Criar `ProductRepositoryPaginationTest.java`
   - `findAll_ShouldReturnPagedResults()`
   - `findByCategory_ShouldFilterCorrectly()`
   - `findByName_ShouldSearchPartialMatch()`
   - `findByFilters_ShouldCombineFilters()`

2. **CODE**: Implementar métodos em `ProductRepositoryImpl.java`
   - Usar `ProductJpaRepository` com Spring Data JPA
   - Implementar filtros com Specification ou Query Methods
   - Mapear `Page<ProductJpaEntity>` → `Page<Product>`

3. **REFACTOR**: Otimizar queries e adicionar logs

**Coverage Target**: ≥ 95%

---

#### TASK-033: Create ListProductsUseCase (TDD) ⏳
**Prioridade**: CRITICAL
**Tempo estimado**: 1h
**Dependências**: TASK-032
**Rastreabilidade**: RF-003

**TDD Steps**:
1. **TEST**: Criar `ListProductsUseCaseTest.java`
   - `execute_WhenNoFilters_ShouldReturnAllProducts()`
   - `execute_WhenCategoryFilter_ShouldFilterByCategory()`
   - `execute_WhenNameFilter_ShouldSearchByName()`
   - `execute_WhenBothFilters_ShouldCombineFilters()`
   - `execute_WhenEmptyResult_ShouldReturnEmptyPage()`

2. **CODE**: Implementar `ListProductsUseCase.java`
   - Injetar `ProductRepository` e `ProductMapper`
   - Validar parâmetros de entrada
   - Chamar repository com filtros
   - Mapear `Page<Product>` → `Page<ProductResponse>`

3. **REFACTOR**: Adicionar logs e validações

**Coverage Target**: ≥ 95%

---

#### TASK-034: Add List Endpoint to Controller ⏳
**Prioridade**: CRITICAL
**Tempo estimado**: 30 min
**Dependências**: TASK-033
**Rastreabilidade**: RF-003

**Checklist**:
- [ ] Adicionar método `list()` em `ProductController.java`
- [ ] Configurar endpoint `GET /api/v1/products`
- [ ] Adicionar parâmetros: `category`, `name`, `Pageable`
- [ ] Configurar Swagger annotations (`@Operation`, `@Parameter`)
- [ ] Validar que não quebra endpoints existentes
- [ ] Testar manualmente via Swagger UI

**Endpoint Spec**:
```java
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
```

---

#### TASK-035: Unit Tests for Listing ⏳
**Prioridade**: HIGH
**Tempo estimado**: 45 min
**Dependências**: TASK-034
**Rastreabilidade**: RNF-005 (qualidade)

**Checklist**:
- [ ] Completar `ListProductsUseCaseTest.java` (se não feito no TDD)
- [ ] Adicionar testes em `ProductControllerTest.java` - endpoint de listagem
- [ ] Testar cenários de erro (parâmetros inválidos)
- [ ] Validar mapeamento de DTOs
- [ ] Garantir coverage ≥ 95% nas classes novas

**Cenários de Teste**:
- Listagem sem filtros
- Filtro por categoria válida/inválida
- Filtro por nome (busca parcial)
- Combinação de filtros
- Paginação (primeira página, última página)
- Resultado vazio

---

#### TASK-036: Integration Tests for Listing ⏳
**Prioridade**: HIGH
**Tempo estimado**: 1h
**Dependências**: TASK-035
**Rastreabilidade**: RNF-005 (qualidade)

**Checklist**:
- [ ] Adicionar testes em `ProductIT.java`
- [ ] Testar endpoint completo com Testcontainers
- [ ] Validar paginação end-to-end
- [ ] Testar filtros com dados reais
- [ ] Validar performance (< 200ms)
- [ ] Testar com cache (se aplicável)

**Cenários End-to-End**:
```java
@Test
void shouldListProducts_WithPagination() {
    // Given - criar produtos de teste
    // When - chamar GET /api/v1/products?page=0&size=5
    // Then - validar estrutura da resposta paginada
}

@Test
void shouldFilterByCategory() {
    // Given - produtos de diferentes categorias
    // When - chamar GET /api/v1/products?category=ELECTRONICS
    // Then - retornar apenas produtos da categoria
}
```

---

#### TASK-037: Performance Tests for Listing ⏳
**Prioridade**: MEDIUM
**Tempo estimado**: 30 min
**Dependências**: TASK-036
**Rastreabilidade**: RNF-001 (performance)

**Checklist**:
- [ ] Criar dataset de teste com 1000+ produtos
- [ ] Medir tempo de resposta da listagem
- [ ] Validar que P99 < 200ms
- [ ] Testar com diferentes tamanhos de página
- [ ] Verificar uso de índices no banco (EXPLAIN PLAN)

---

#### TASK-038: Update Documentation for RF-003 ⏳
**Prioridade**: MEDIUM
**Tempo estimado**: 15 min
**Dependências**: TASK-037
**Rastreabilidade**: Manutenibilidade

**Checklist**:
- [ ] Atualizar Swagger documentation
- [ ] Adicionar exemplos de request/response
- [ ] Atualizar README.md com novo endpoint
- [ ] Documentar parâmetros de filtro
- [ ] Commitar documentação

---

### Phase 7: Integration Tests ✅ CONCLUÍDA

#### TASK-018: Integration Tests - Happy Paths ✅
**Status**: ✅ **CONCLUÍDA** (67 testes executados, 0 falhas)

#### TASK-019: Integration Tests - Error Scenarios ✅
**Status**: ✅ **CONCLUÍDA**

#### TASK-020: Integration Tests - Concurrency ✅
**Status**: ✅ **CONCLUÍDA**

---

### Phase 8: Documentation & Configuration (1-2 horas)

#### TASK-021: Configure Swagger/OpenAPI ⏳
**Prioridade**: MEDIUM
**Tempo estimado**: 30 min
**Dependências**: TASK-017, TASK-038
**Rastreabilidade**: RNF-003 (documentação)

**Checklist**:
- [ ] Criar `SwaggerConfig.java`
- [ ] Adicionar annotations nos endpoints (`@Operation`, `@ApiResponse`)
- [ ] Validar em http://localhost:8081/swagger-ui.html
- [ ] Exportar OpenAPI spec para `docs/api/product-service-openapi.yaml`

---

#### TASK-022: Configure Actuator Health Checks ⏳
**Prioridade**: HIGH
**Tempo estimado**: 15 min
**Dependências**: TASK-001
**Rastreabilidade**: RNF-002 (disponibilidade)

**Checklist**:
- [ ] Configurar `/actuator/health` no `application.yml`
- [ ] Adicionar health indicator para Redis
- [ ] Adicionar health indicator para PostgreSQL
- [ ] Validar endpoint retorna status UP

---

#### TASK-023: Configure Logging ⏳
**Prioridade**: MEDIUM
**Tempo estimado**: 20 min
**Dependências**: TASK-001
**Rastreabilidade**: RNF-004 (observabilidade)

**Checklist**:
- [ ] Configurar Logback para formato JSON
- [ ] Adicionar correlation ID (MDC)
- [ ] Configurar níveis de log (DEBUG para dev, INFO para prod)
- [ ] Testar logs estruturados

---

### Phase 9: Quality & CI (1-2 horas)

#### TASK-024: Validate Coverage ≥ 95% ⏳
**Prioridade**: CRITICAL
**Tempo estimado**: 30 min
**Dependências**: TASK-037
**Rastreabilidade**: RNF-005 (qualidade)

**Status Atual**: 89% coverage (target: 95%)

**Checklist**:
- [ ] Executar `mvn clean verify`
- [ ] Validar Jacoco report: `target/site/jacoco/index.html`
- [ ] Coverage atual: Lines ≥ 95%, Branches ≥ 90%
- [ ] Adicionar testes se coverage < 95%

---

#### TASK-025: Configure Checkstyle ⏳
**Prioridade**: MEDIUM
**Tempo estimado**: 20 min
**Dependências**: TASK-024
**Rastreabilidade**: RNF-005 (qualidade)

**Checklist**:
- [ ] Adicionar plugin `maven-checkstyle-plugin` no pom.xml
- [ ] Configurar `checkstyle.xml` (Google Java Style)
- [ ] Executar `mvn checkstyle:check`
- [ ] Corrigir violações

---

#### TASK-026: Configure SonarQube (local) ⏳
**Prioridade**: MEDIUM
**Tempo estimado**: 30 min
**Dependências**: TASK-025
**Rastreabilidade**: RNF-005 (qualidade)

**Checklist**:
- [ ] Subir SonarQube local (Docker)
- [ ] Executar `mvn sonar:sonar`
- [ ] Validar quality gate: A (maintainability)
- [ ] Corrigir code smells críticos

---

#### TASK-027: Create GitHub Actions CI ⏳
**Prioridade**: HIGH
**Tempo estimado**: 30 min
**Dependências**: TASK-026
**Rastreabilidade**: RNF-005 (automação)

**Checklist**:
- [ ] Criar `.github/workflows/product-service-ci.yml`
- [ ] Jobs: build, test, coverage, checkstyle
- [ ] Trigger: push em qualquer branch
- [ ] Badge no README.md

---

### Phase 10: Final Validation (30 min)

#### TASK-028: End-to-End Manual Testing ⏳
**Prioridade**: HIGH
**Tempo estimado**: 30 min
**Dependências**: TASK-027
**Rastreabilidade**: Todos os RF-xxx

**Checklist**:
- [ ] Subir infraestrutura: `docker-compose up -d`
- [ ] Subir product-service: `mvn spring-boot:run`
- [ ] Testar via Swagger: http://localhost:8081/swagger-ui.html
  - Criar produto
  - Buscar produto
  - **Listar produtos (NOVO)**
  - Atualizar produto
  - Decrementar estoque
  - Deletar produto
- [ ] Validar cache Redis: `redis-cli KEYS product:*`
- [ ] Validar PostgreSQL: `psql -c "SELECT * FROM products;"`

---

#### TASK-029: Update Documentation ⏳
**Prioridade**: MEDIUM
**Tempo estimado**: 20 min
**Dependências**: TASK-028
**Rastreabilidade**: Manutenibilidade

**Checklist**:
- [ ] Atualizar CLAUDE.md com lições aprendidas
- [ ] Atualizar README.md do product-service
- [ ] Adicionar ADR se houve decisão arquitetural importante
- [ ] Commitar documentação

---

#### TASK-030: Final Commit & PR ⏳
**Prioridade**: CRITICAL
**Tempo estimado**: 10 min
**Dependências**: TASK-029
**Rastreabilidade**: Gitflow

**Checklist**:
- [ ] Criar branch: `feature/rf-003-list-products`
- [ ] Commitar seguindo Conventional Commits
- [ ] Push: `git push -u origin feature/rf-003-list-products`
- [ ] Criar PR para `develop` (não fazer merge ainda - aguardar review)

---

## Resumo de Tempo

| Phase | Tarefas | Tempo Total |
|-------|---------|-------------|
| 1. Setup | TASK-001 a TASK-004 | ✅ Concluída |
| 2. Domain | TASK-005 a TASK-008 | ✅ Concluída |
| 3. Infrastructure | TASK-009 a TASK-010 | ✅ Concluída |
| 4. Application | TASK-011 a TASK-015 | ✅ Concluída |
| 5. REST | TASK-016 a TASK-017 | ✅ Concluída |
| **6. RF-003** | **TASK-031 a TASK-038** | **5h** |
| 7. Integration | TASK-018 a TASK-020 | ✅ Concluída |
| 8. Docs | TASK-021 a TASK-023 | 1 - 2h |
| 9. Quality | TASK-024 a TASK-027 | 1 - 2h |
| 10. Final | TASK-028 a TASK-030 | 1h |
| **TOTAL** | **38 tasks** | **8 - 10h restantes** |

---

## Ordem de Execução (Crítica)

**PRÓXIMA SEQUÊNCIA**:

1. **RF-003 Implementation**: TASK-031 → TASK-032 → TASK-033 → TASK-034 → TASK-035 → TASK-036 → TASK-037 → TASK-038
2. **Documentation**: TASK-021 → TASK-022 → TASK-023
3. **Quality**: TASK-024 → TASK-025 → TASK-026 → TASK-027
4. **Final**: TASK-028 → TASK-029 → TASK-030

---

## Contratos de API - RF-003

### Request Examples

```http
# Listar todos (primeira página)
GET /api/v1/products?page=0&size=20&sort=name,asc

# Filtrar por categoria
GET /api/v1/products?category=ELECTRONICS&page=0&size=10

# Buscar por nome
GET /api/v1/products?name=notebook&page=0&size=5

# Filtros combinados
GET /api/v1/products?category=ELECTRONICS&name=dell&sort=price,desc
```

### Response Example

```json
{
  "content": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "name": "Notebook Dell Inspiron",
      "description": "15.6 inch, Intel i7, 16GB RAM",
      "price": 3499.99,
      "stockQuantity": 50,
      "sku": "DELL-INSP-15-I7",
      "category": "ELECTRONICS",
      "status": "ACTIVE",
      "createdAt": "2026-03-10T14:30:00Z",
      "updatedAt": "2026-03-10T14:30:00Z"
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
  "last": false,
  "first": true,
  "numberOfElements": 20
}
```

---

## Critérios de "Done" (Definition of Done)

Uma task está completa SOMENTE quando:

- ✅ Código implementado seguindo Hexagonal Architecture
- ✅ Testes escritos ANTES do código (TDD)
- ✅ Coverage da task ≥ 95%
- ✅ Build passando: `mvn clean verify`
- ✅ Checkstyle OK
- ✅ Code review (auto-review se solo)
- ✅ Commit atômico com mensagem descritiva

---

## Rastreabilidade Completa

| Requirement | Tasks |
|-------------|-------|
| RF-001 (Criar Produto) | TASK-002, TASK-006, TASK-009, TASK-010, TASK-012, TASK-017 |
| RF-002 (Buscar Produto) | TASK-010, TASK-013, TASK-017 |
| **RF-003 (Listar Produtos)** | **TASK-031, TASK-032, TASK-033, TASK-034, TASK-035, TASK-036** |
| RF-004 (Atualizar Produto) | TASK-006, TASK-010, TASK-015, TASK-017 |
| RF-005 (Deletar Produto) | TASK-010, TASK-015, TASK-017 |
| RF-006 (Decrementar Estoque) | TASK-006, TASK-014, TASK-017, TASK-020 |
| RF-007 (Incrementar Estoque) | TASK-006, TASK-015, TASK-017 |
| RNF-001 (Performance) | TASK-003, TASK-010, TASK-018, TASK-037 |
| RNF-002 (Disponibilidade) | TASK-022 |
| RNF-003 (Segurança) | TASK-011 (validações) |
| RNF-004 (Observabilidade) | TASK-023 |
| RNF-005 (Qualidade) | TASK-004, TASK-018 a TASK-027, TASK-035, TASK-036 |

---

**Próximo Passo**: Iniciar TASK-031 (Extend Repository Interface for Pagination)

**Nota**: Este documento será atualizado com checkmarks ✅ conforme tasks forem concluídas.