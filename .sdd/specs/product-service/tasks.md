# Product Service - Implementation Tasks

**Versão**: 1.0
**Data**: 2026-03-09
**Status**: 🚀 Em Progresso - 17/30 tasks concluídas (57%)
**Última atualização**: 2026-03-09 18:20

---

## 📊 Progress Summary

| Phase | Tasks | Status | Completion |
|-------|-------|--------|------------|
| 1. Setup & Infrastructure | TASK-001 a TASK-004 | ✅ Concluída | 4/4 (100%) |
| 2. Domain Layer | TASK-005 a TASK-008 | ✅ Concluída | 4/4 (100%) |
| 3. Infrastructure Adapters | TASK-009 a TASK-010 | ✅ Concluída | 2/2 (100%) |
| 4. Application Layer | TASK-011 a TASK-015 | ✅ Concluída | 5/5 (100%) |
| 5. REST Layer | TASK-016 a TASK-017 | ✅ Concluída | 2/2 (100%) |
| 6. Integration Tests | TASK-018 a TASK-020 | ⏳ Pendente | 0/3 (0%) |
| 7. Documentation | TASK-021 a TASK-023 | ⏳ Pendente | 0/3 (0%) |
| 8. Quality & CI | TASK-024 a TASK-027 | ⏳ Pendente | 0/4 (0%) |
| 9. Final Validation | TASK-028 a TASK-030 | ⏳ Pendente | 0/3 (0%) |
| **TOTAL** | **30 tasks** | **57%** | **17/30** |

### Commits Realizados
- `19b1d44` - Initial project setup
- `a76b60b` - Maven setup (TASK-001)
- `ceda891` - Migration + Value Objects (TASK-002, TASK-005)
- `50462f7` - Infrastructure Layer (TASK-003, TASK-009, TASK-010)
- `f8349b4` - Application Layer (TASK-011 a TASK-015)
- `2393d81` - REST Layer (TASK-016, TASK-017)

### Próxima Milestone
🎯 **Rodar aplicação e testar endpoints** → depois implementar testes (TASK-018 a TASK-020)

---

## Rastreabilidade

Cada task está mapeada aos **Requirements** (requirements.md):

- **RF-xxx**: Requisito Funcional
- **RNF-xxx**: Requisito Não-Funcional

---

## Task Breakdown (TDD Order)

### Phase 1: Setup & Infrastructure (2-3 horas)

#### TASK-001: Setup Maven Project Structure ✅
**Prioridade**: CRITICAL
**Tempo estimado**: 30 min
**Dependências**: Nenhuma
**Rastreabilidade**: Setup inicial

**Checklist**:
- [x] Criar `pom.xml` root do product-service
- [x] Configurar dependências (Spring Boot, PostgreSQL, Redis, Lombok, MapStruct)
- [x] Configurar plugins (Maven Compiler, Surefire, Jacoco)
- [x] Criar estrutura de pacotes (domain, application, infrastructure)
- [x] Criar `application.yml` / `application-test.yml`
- [x] Build passando: `mvn clean compile`

**Status**: ✅ **CONCLUÍDA** (Commit: a76b60b)

---

#### TASK-002: Setup Database Migrations ✅
**Prioridade**: CRITICAL
**Tempo estimado**: 20 min
**Dependências**: TASK-001
**Rastreabilidade**: RF-001 (criar produto requer tabela)

**Checklist**:
- [x] Criar `src/main/resources/db/migration/V001__create_products_table.sql`
- [x] Configurar Flyway no `application.yml`
- [x] Validar migration: `mvn flyway:migrate`

**Status**: ✅ **CONCLUÍDA** (Commit: ceda891)

---

#### TASK-003: Setup Redis Configuration ✅
**Prioridade**: HIGH
**Tempo estimado**: 15 min
**Dependências**: TASK-001
**Rastreabilidade**: RNF-001 (performance com cache)

**Checklist**:
- [x] Criar `RedisConfig.java`
- [x] Configurar `CacheManager` com TTL de 5 minutos
- [x] Criar `ProductCacheService.java` (base)

**Status**: ✅ **CONCLUÍDA** (Commit: 50462f7)

---

#### TASK-004: Setup Testcontainers ✅
**Prioridade**: HIGH
**Tempo estimado**: 30 min
**Dependências**: TASK-001
**Rastreabilidade**: RNF-005 (qualidade - testes de integração)

**Checklist**:
- [ ] Adicionar dependência `testcontainers` no pom.xml
- [ ] Criar `BaseIntegrationTest.java` com PostgreSQL + Redis containers
- [ ] Validar containers: executar teste simples
- [ ] Configurar `@DynamicPropertySource` para injetar URLs

---

### Phase 2: Domain Layer (TDD) (3-4 horas)

#### TASK-005: Implement Value Objects (TDD) ✅
**Prioridade**: CRITICAL
**Tempo estimado**: 1 hora
**Dependências**: TASK-001
**Rastreabilidade**: RF-001 (modelo de domínio)

**TDD Steps**:
1. **TEST**: Criar `ProductIdTest.java`
   - `shouldCreateValidProductId_WhenUUIDProvided()`
   - `shouldThrowException_WhenNullUUID()`
   - `shouldParseFromString_WhenValidUUID()`

2. **CODE**: Implementar `ProductId.java` (record)

3. **TEST**: Criar `MoneyTest.java`
   - `shouldCreateValidMoney_WhenPositiveAmount()`
   - `shouldThrowException_WhenNegativeAmount()`
   - `shouldRoundToTwoDecimals()`

4. **CODE**: Implementar `Money.java` (record)

5. **REFACTOR**: Garantir imutabilidade e validações

---

#### TASK-006: Implement Product Aggregate (TDD) ✅
**Prioridade**: CRITICAL
**Tempo estimado**: 2 horas
**Dependências**: TASK-005
**Rastreabilidade**: RF-001, RF-004, RF-006, RF-007

**TDD Steps**:
1. **TEST**: Criar `ProductTest.java` - Factory Method `create()`
   - `shouldCreateProduct_WhenValidData()`
   - `shouldThrowException_WhenNullName()`
   - `shouldThrowException_WhenNegativePrice()`
   - `shouldThrowException_WhenInvalidSKU()`
   - `shouldSetStatusActiveByDefault()`

2. **CODE**: Implementar `Product.create()` com validações

3. **TEST**: Decrementar estoque
   - `decreaseStock_WhenSufficientQuantity_ShouldDecrease()`
   - `decreaseStock_WhenInsufficientQuantity_ShouldThrowException()`
   - `decreaseStock_WhenZeroQuantity_ShouldThrowException()`

4. **CODE**: Implementar `Product.decreaseStock()`

5. **TEST**: Incrementar estoque
   - `increaseStock_WhenPositiveQuantity_ShouldIncrease()`
   - `increaseStock_WhenZeroQuantity_ShouldThrowException()`

6. **CODE**: Implementar `Product.increaseStock()`

7. **TEST**: Atualizar produto
   - `update_WhenValidData_ShouldUpdate()`
   - `update_WhenInvalidData_ShouldThrowException()`

8. **CODE**: Implementar `Product.update()`

9. **TEST**: Inativar produto
   - `deactivate_ShouldSetStatusInactive()`

10. **CODE**: Implementar `Product.deactivate()`

11. **REFACTOR**: Extrair validações, garantir encapsulamento

**Coverage Target**: ≥ 95%

---

#### TASK-007: Implement Domain Exceptions ✅
**Prioridade**: HIGH
**Tempo estimado**: 15 min
**Dependências**: TASK-006
**Rastreabilidade**: Tratamento de erros

**Checklist**:
- [ ] Criar `DomainException.java` (base)
- [ ] Criar `ProductNotFoundException.java`
- [ ] Criar `InsufficientStockException.java`
- [ ] Criar `DuplicateSkuException.java`
- [ ] Criar testes unitários para exceções

---

#### TASK-008: Define Repository Port (Interface) ✅
**Prioridade**: HIGH
**Tempo estimado**: 10 min
**Dependências**: TASK-006
**Rastreabilidade**: Hexagonal Architecture (port)

**Checklist**:
- [ ] Criar `ProductRepository.java` (interface)
- [ ] Definir métodos: `save()`, `findById()`, `findBySku()`, `deleteById()`, `existsBySku()`
- [ ] Documentar com Javadoc

---

### Phase 3: Infrastructure Layer (TDD) (2-3 horas)

#### TASK-009: Implement JPA Entity & Mapper (TDD) ✅
**Prioridade**: HIGH
**Tempo estimado**: 1 hora
**Dependências**: TASK-008
**Rastreabilidade**: Persistência

**TDD Steps**:
1. **TEST**: Criar `ProductJpaMapperTest.java`
   - `shouldMapDomainToEntity_WhenValidProduct()`
   - `shouldMapEntityToDomain_WhenValidEntity()`

2. **CODE**: Criar `ProductJpaEntity.java` (anotações JPA)

3. **CODE**: Criar `ProductJpaMapper.java` (MapStruct interface)

4. **REFACTOR**: Validar mapeamento bidirecional

---

#### TASK-010: Implement Repository Adapter (TDD) ✅
**Prioridade**: CRITICAL
**Tempo estimado**: 1.5 horas
**Dependências**: TASK-009
**Rastreabilidade**: RF-001, RF-002, RF-004, RF-005

**TDD Steps**:
1. **TEST**: Criar `ProductRepositoryImplTest.java` (Integration)
   - `save_ShouldPersistProduct()`
   - `findById_WhenExists_ShouldReturnProduct()`
   - `findById_WhenNotExists_ShouldReturnEmpty()`
   - `findBySku_WhenExists_ShouldReturnProduct()`
   - `deleteById_ShouldRemoveProduct()`
   - `existsBySku_WhenExists_ShouldReturnTrue()`

2. **CODE**: Criar `ProductJpaRepository.java` (Spring Data JPA)

3. **CODE**: Criar `ProductRepositoryImpl.java` (implementação do port)

4. **TEST**: Validar cache (hit/miss)
   - `findById_WhenCached_ShouldNotHitDatabase()`

5. **CODE**: Integrar `ProductCacheService`

6. **REFACTOR**: Adicionar logs, otimizar queries

**Coverage Target**: ≥ 95%

---

### Phase 4: Application Layer (TDD) (3-4 horas)

#### TASK-011: Implement DTOs & Mapper ✅
**Prioridade**: HIGH
**Tempo estimado**: 45 min
**Dependências**: TASK-006
**Rastreabilidade**: RF-001 (contratos de API)

**Checklist**:
- [ ] Criar `CreateProductRequest.java` (record com validações @Valid)
- [ ] Criar `UpdateProductRequest.java`
- [ ] Criar `StockOperationRequest.java`
- [ ] Criar `ProductResponse.java`
- [ ] Criar `ProductPageResponse.java`
- [ ] Criar `ProductMapper.java` (MapStruct) - Domain ↔ DTO
- [ ] Criar testes: `ProductMapperTest.java`

---

#### TASK-012: Implement CreateProductUseCase (TDD) ✅
**Prioridade**: CRITICAL
**Tempo estimado**: 1 hora
**Dependências**: TASK-010, TASK-011
**Rastreabilidade**: RF-001

**TDD Steps**:
1. **TEST**: Criar `CreateProductUseCaseTest.java`
   - `execute_WhenValidRequest_ShouldCreateProduct()`
   - `execute_WhenDuplicateSKU_ShouldThrowException()`

2. **CODE**: Implementar `CreateProductUseCase.java`
   - Validar SKU único
   - Chamar `Product.create()`
   - Persistir via repository
   - Retornar DTO

3. **REFACTOR**: Adicionar logs, validações

**Coverage Target**: ≥ 95%

---

#### TASK-013: Implement GetProductUseCase (TDD) ✅
**Prioridade**: HIGH
**Tempo estimado**: 30 min
**Dependências**: TASK-010, TASK-011
**Rastreabilidade**: RF-002

**TDD Steps**:
1. **TEST**: `GetProductUseCaseTest.java`
   - `execute_WhenExists_ShouldReturnProduct()`
   - `execute_WhenNotExists_ShouldThrowNotFoundException()`

2. **CODE**: Implementar `GetProductUseCase.java`

---

#### TASK-014: Implement DecreaseStockUseCase (TDD) ✅
**Prioridade**: CRITICAL
**Tempo estimado**: 1 hora
**Dependências**: TASK-010, TASK-011
**Rastreabilidade**: RF-006

**TDD Steps**:
1. **TEST**: `DecreaseStockUseCaseTest.java`
   - `execute_WhenSufficientStock_ShouldDecreaseAndInvalidateCache()`
   - `execute_WhenInsufficientStock_ShouldThrowException()`
   - `execute_WhenProductNotFound_ShouldThrowException()`
   - `execute_WhenConcurrentRequests_ShouldHandleCorrectly()` (Pessimistic Lock)

2. **CODE**: Implementar `DecreaseStockUseCase.java`
   - Buscar com lock (`@Lock(PESSIMISTIC_WRITE)`)
   - Chamar `product.decreaseStock()`
   - Invalidar cache

3. **REFACTOR**: Garantir atomicidade

**Coverage Target**: ≥ 95%

---

#### TASK-015: Implement Remaining Use Cases (TDD) ✅
**Prioridade**: MEDIUM
**Tempo estimado**: 2 horas
**Dependências**: TASK-010, TASK-011
**Rastreabilidade**: RF-003, RF-004, RF-005, RF-007

**Implementar**:
- `ListProductsUseCase.java` (com paginação e filtros)
- `UpdateProductUseCase.java`
- `DeleteProductUseCase.java` (soft delete)
- `IncreaseStockUseCase.java`

**Para cada use case**:
1. Criar testes (cenários positivos e negativos)
2. Implementar lógica
3. Garantir coverage ≥ 95%

---

### Phase 5: REST Layer (TDD) (2-3 horas)

#### TASK-016: Implement Global Exception Handler ✅
**Prioridade**: HIGH
**Tempo estimado**: 45 min
**Dependências**: TASK-007
**Rastreabilidade**: Tratamento de erros (requirements.md seção 6)

**TDD Steps**:
1. **TEST**: `GlobalExceptionHandlerTest.java`
   - `shouldReturn404_WhenProductNotFoundException()`
   - `shouldReturn409_WhenInsufficientStockException()`
   - `shouldReturn409_WhenDuplicateSkuException()`
   - `shouldReturn400_WhenValidationException()`
   - `shouldReturn500_WhenUnexpectedException()`

2. **CODE**: Criar `GlobalExceptionHandler.java` (`@ControllerAdvice`)
   - Mapear exceções → status HTTP
   - Retornar `ErrorResponse` padronizado

3. **REFACTOR**: Adicionar logs de erro

---

#### TASK-017: Implement ProductController (TDD) ✅
**Prioridade**: CRITICAL
**Tempo estimado**: 2 horas
**Dependências**: TASK-012 a TASK-016
**Rastreabilidade**: Todos os RF-xxx

**TDD Steps**:
1. **TEST**: Criar `ProductControllerTest.java` (WebMvcTest)
   - `create_WhenValidRequest_ShouldReturn201()`
   - `create_WhenInvalidRequest_ShouldReturn400()`
   - `getById_WhenExists_ShouldReturn200()`
   - `getById_WhenNotExists_ShouldReturn404()`
   - `list_ShouldReturnPaginatedResults()`
   - `update_WhenExists_ShouldReturn200()`
   - `delete_WhenExists_ShouldReturn204()`
   - `decreaseStock_WhenSufficientStock_ShouldReturn200()`
   - `decreaseStock_WhenInsufficientStock_ShouldReturn409()`

2. **CODE**: Implementar `ProductController.java`
   - Injetar use cases
   - Mapear endpoints
   - Adicionar Swagger annotations

3. **REFACTOR**: Validar aderência aos contratos de API

**Coverage Target**: ≥ 95%

---

### Phase 6: Integration Tests (2-3 horas)

#### TASK-018: Integration Tests - Happy Paths ✅
**Prioridade**: HIGH
**Tempo estimado**: 1.5 horas
**Dependências**: TASK-017
**Rastreabilidade**: RNF-005 (qualidade)

**Checklist**:
- [ ] `shouldCreateAndRetrieveProduct_EndToEnd()`
- [ ] `shouldUpdateProduct_EndToEnd()`
- [ ] `shouldDeleteProduct_EndToEnd()`
- [ ] `shouldDecreaseStock_EndToEnd()`
- [ ] `shouldListProductsWithPagination_EndToEnd()`

**Usar**:
- Testcontainers (PostgreSQL + Redis)
- REST Assured para chamadas HTTP
- Validar cache hit/miss

---

#### TASK-019: Integration Tests - Error Scenarios ✅
**Prioridade**: MEDIUM
**Tempo estimado**: 1 hora
**Dependências**: TASK-018
**Rastreabilidade**: RNF-005 (qualidade)

**Checklist**:
- [ ] `shouldReturn404_WhenProductNotFound()`
- [ ] `shouldReturn409_WhenDuplicateSKU()`
- [ ] `shouldReturn409_WhenInsufficientStock()`
- [ ] `shouldReturn400_WhenInvalidInput()`

---

#### TASK-020: Integration Tests - Concurrency ✅
**Prioridade**: HIGH
**Tempo estimado**: 1 hora
**Dependências**: TASK-018
**Rastreabilidade**: RF-006 (atomicidade)

**Checklist**:
- [ ] `shouldHandleConcurrentStockDecreases_WithPessimisticLock()`
- [ ] Criar 10 threads tentando decrementar estoque simultaneamente
- [ ] Validar que estoque final está correto (sem race condition)

---

### Phase 7: Documentation & Configuration (1-2 horas)

#### TASK-021: Configure Swagger/OpenAPI ✅
**Prioridade**: MEDIUM
**Tempo estimado**: 30 min
**Dependências**: TASK-017
**Rastreabilidade**: RNF-003 (documentação)

**Checklist**:
- [ ] Criar `SwaggerConfig.java`
- [ ] Adicionar annotations nos endpoints (`@Operation`, `@ApiResponse`)
- [ ] Validar em http://localhost:8081/swagger-ui.html
- [ ] Exportar OpenAPI spec para `docs/api/product-service-openapi.yaml`

---

#### TASK-022: Configure Actuator Health Checks ✅
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

#### TASK-023: Configure Logging ✅
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

### Phase 8: Quality & CI (1-2 horas)

#### TASK-024: Validate Coverage ≥ 95% ✅
**Prioridade**: CRITICAL
**Tempo estimado**: 30 min
**Dependências**: TASK-020
**Rastreabilidade**: RNF-005 (qualidade)

**Checklist**:
- [ ] Executar `mvn clean verify`
- [ ] Validar Jacoco report: `target/site/jacoco/index.html`
- [ ] Coverage atual: Lines ≥ 95%, Branches ≥ 90%
- [ ] Adicionar testes se coverage < 95%

---

#### TASK-025: Configure Checkstyle ✅
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

#### TASK-026: Configure SonarQube (local) ✅
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

#### TASK-027: Create GitHub Actions CI ✅
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

### Phase 9: Final Validation (30 min)

#### TASK-028: End-to-End Manual Testing ✅
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
  - Listar produtos
  - Atualizar produto
  - Decrementar estoque
  - Deletar produto
- [ ] Validar cache Redis: `redis-cli KEYS product:*`
- [ ] Validar PostgreSQL: `psql -c "SELECT * FROM products;"`

---

#### TASK-029: Update Documentation ✅
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

#### TASK-030: Final Commit & PR ✅
**Prioridade**: CRITICAL
**Tempo estimado**: 10 min
**Dependências**: TASK-029
**Rastreabilidade**: Gitflow

**Checklist**:
- [ ] Criar branch: `feature/product-service-crud`
- [ ] Commitar seguindo Conventional Commits
- [ ] Push: `git push -u origin feature/product-service-crud`
- [ ] Criar PR para `develop` (não fazer merge ainda - aguardar review)

---

## Resumo de Tempo

| Phase | Tarefas | Tempo Total |
|-------|---------|-------------|
| 1. Setup | TASK-001 a TASK-004 | 1.5 - 2h |
| 2. Domain | TASK-005 a TASK-008 | 3 - 4h |
| 3. Infrastructure | TASK-009 a TASK-010 | 2 - 3h |
| 4. Application | TASK-011 a TASK-015 | 3 - 4h |
| 5. REST | TASK-016 a TASK-017 | 2 - 3h |
| 6. Integration | TASK-018 a TASK-020 | 2 - 3h |
| 7. Docs | TASK-021 a TASK-023 | 1 - 2h |
| 8. Quality | TASK-024 a TASK-027 | 1 - 2h |
| 9. Final | TASK-028 a TASK-030 | 1h |
| **TOTAL** | **30 tasks** | **16 - 24h** |

---

## Ordem de Execução (Crítica)

**IMPORTANTE**: Seguir ESTRITAMENTE esta ordem:

1. TASK-001 → TASK-002 → TASK-003 → TASK-004 (setup completo)
2. TASK-005 → TASK-006 → TASK-007 → TASK-008 (domain TDD)
3. TASK-009 → TASK-010 (infrastructure TDD)
4. TASK-011 → TASK-012 → TASK-013 → TASK-014 → TASK-015 (application TDD)
5. TASK-016 → TASK-017 (REST TDD)
6. TASK-018 → TASK-019 → TASK-020 (integration tests)
7. TASK-021 → TASK-022 → TASK-023 (docs & config)
8. TASK-024 → TASK-025 → TASK-026 → TASK-027 (quality)
9. TASK-028 → TASK-029 → TASK-030 (final validation)

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
| RF-003 (Listar Produtos) | TASK-015, TASK-017 |
| RF-004 (Atualizar Produto) | TASK-006, TASK-010, TASK-015, TASK-017 |
| RF-005 (Deletar Produto) | TASK-010, TASK-015, TASK-017 |
| RF-006 (Decrementar Estoque) | TASK-006, TASK-014, TASK-017, TASK-020 |
| RF-007 (Incrementar Estoque) | TASK-006, TASK-015, TASK-017 |
| RNF-001 (Performance) | TASK-003, TASK-010, TASK-018 |
| RNF-002 (Disponibilidade) | TASK-022 |
| RNF-003 (Segurança) | TASK-011 (validações) |
| RNF-004 (Observabilidade) | TASK-023 |
| RNF-005 (Qualidade) | TASK-004, TASK-018 a TASK-027 |

---

**Próximo Passo**: Iniciar implementação pela TASK-001 (Setup Maven Project)

**Nota**: Este documento será atualizado com checkmarks ✅ conforme tasks forem concluídas.
