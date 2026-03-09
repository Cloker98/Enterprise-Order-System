# CLAUDE.md - Base de Conhecimento do Projeto

> **IMPORTANTE**: Este arquivo é a **memória persistente** do projeto. A IA deve consultar SEMPRE antes de implementar qualquer feature.

---

## 🎯 Sobre Este Projeto

**Enterprise Order Management System** - Sistema de gerenciamento de pedidos empresarial construído com **arquitetura de microsserviços** para demonstrar competências técnicas para vaga **Java Developer Pleno**.

### Metodologias Aplicadas

- **DDD** (Domain-Driven Design) - Modelagem orientada ao domínio
- **TDD** (Test-Driven Development) - Testes antes da implementação
- **BDD** (Behavior-Driven Development) - Comportamento em linguagem natural
- **SDD** (Specification-Driven Development) - Especificação antes do código

---

## 📋 Regras Arquiteturais Críticas

### 1. **Hexagonal Architecture (Obrigatório)**

```
domain/ (Core Business - SEM dependências de framework)
├── model/ (Entities, Value Objects)
├── service/ (Domain Services)
└── repository/ (Ports - interfaces)

application/ (Use Cases)
├── usecase/ (Orquestração)
├── dto/ (Request/Response)
└── mapper/ (Domain ↔ DTO)

infrastructure/ (Adapters)
├── persistence/ (JPA Entities, Repositories)
├── rest/ (Controllers)
├── messaging/ (RabbitMQ Publishers/Consumers)
└── config/ (Spring Configuration)
```

**❌ NUNCA violar**: Domain NÃO pode ter anotações de framework (@Entity, @RestController, etc).

---

### 2. **Database per Service**

| Serviço | Database | Rationale |
|---------|----------|-----------|
| Product | PostgreSQL | CRUD, cache Redis |
| Order | PostgreSQL | Transações, SAGA orchestration |
| Payment | **Oracle 21c** | **Requisito da vaga** |
| Notification | MongoDB | Schema flexível |

**❌ NUNCA**: Compartilhar schemas entre serviços.

---

### 3. **Event-Driven Communication**

```yaml
Pattern: Pub/Sub via RabbitMQ
Events:
  - OrderCreated → Payment Service, Notification Service
  - OrderCancelled → Payment Service (compensating transaction)
  - PaymentProcessed → Order Service, Notification Service
  - PaymentFailed → Order Service (rollback)
```

**Formato de Evento** (padrão):
```json
{
  "eventId": "uuid",
  "eventType": "OrderCreated",
  "timestamp": "ISO-8601",
  "aggregateId": "order-123",
  "payload": { },
  "metadata": {
    "userId": "user-456",
    "correlationId": "correlation-789"
  }
}
```

---

### 4. **SAGA Pattern (Orchestration)**

**Orquestrador**: Order Service

**Fluxo Feliz**:
```
1. Order Service: Create Order (status=PENDING)
2. Order Service: Publish OrderCreated event
3. Payment Service: Consume event → Process Payment
4. Payment Service: Publish PaymentProcessed event
5. Order Service: Update order (status=CONFIRMED)
6. Notification Service: Send confirmation email
```

**Fluxo de Compensação** (falha no pagamento):
```
1-3. (igual ao fluxo feliz)
4. Payment Service: Payment fails → Publish PaymentFailed
5. Order Service: Consume PaymentFailed → Update order (status=CANCELLED)
6. Notification Service: Send cancellation email
```

---

### 5. **Circuit Breaker**

**Aplicar em**: Payment Service (chamadas externas)

```yaml
Library: Resilience4j
Configuration:
  failure-rate-threshold: 50%
  wait-duration-in-open-state: 60s
  sliding-window-size: 10
  minimum-number-of-calls: 5
  fallback: ReturnPaymentFailedStatus
```

---

## 🔧 Padrões de Desenvolvimento

### Nomenclatura (OBRIGATÓRIO)

```java
// Domain
Product.java              // Entity
Price.java                // Value Object
ProductRepository.java    // Port (interface)

// Application
CreateProductUseCase.java
ProductResponse.java      // DTO (record)
ProductMapper.java        // MapStruct

// Infrastructure
ProductJpaEntity.java     // JPA Entity
ProductJpaRepository.java // JPA Repository
ProductController.java    // REST Controller
```

### Testes (Convention)

```java
// Pattern: {Method}_{Scenario}_{ExpectedBehavior}

@Test
void createProduct_WhenValidData_ShouldReturnCreatedProduct() { }

@Test
void createProduct_WhenNullName_ShouldThrowValidationException() { }

@Test
void decreaseStock_WhenInsufficientQuantity_ShouldThrowException() { }
```

### Migrations (Flyway)

```sql
-- Naming: V{version}__{description}.sql
-- Example: V001__create_products_table.sql

-- ❌ NUNCA editar migration já aplicada
-- ✅ Criar nova migration para corrigir
```

---

## 🧪 Estratégia de Testes

### Cobertura Mínima: 95%

```
Pirâmide:
- 60% Unit Tests (jUnit + Mockito)
- 30% Integration Tests (Testcontainers)
- 10% BDD Tests (Cucumber)
```

### Testcontainers (Obrigatório para Integration)

```java
@Container
static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

@Container
static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
    .withExposedPorts(6379);
```

---

## 🚫 Anti-Patterns (NÃO FAZER)

### ❌ 1. Anemic Domain Model

```java
// ❌ ERRADO - Apenas getters/setters
public class Product {
    private String name;
    private BigDecimal price;
    // apenas getters/setters
}

// ✅ CORRETO - Domain com lógica
public class Product {
    public void decreaseStock(int quantity) {
        if (quantity > stockQuantity) {
            throw new InsufficientStockException();
        }
        this.stockQuantity -= quantity;
    }
}
```

### ❌ 2. Domain com anotações de Framework

```java
// ❌ ERRADO
@Entity  // ❌ NUNCA no Domain!
public class Product { }

// ✅ CORRETO - JPA Entity separada
@Entity
@Table(name = "products")
public class ProductJpaEntity { }
```

### ❌ 3. Magic Numbers/Strings

```java
// ❌ ERRADO
if (status == 1) { }
if (type.equals("CREDIT_CARD")) { }

// ✅ CORRETO
if (status == OrderStatus.PENDING) { }
if (type == PaymentType.CREDIT_CARD) { }
```

### ❌ 4. DTOs como Entities

```java
// ❌ ERRADO
@PostMapping
public Product create(@RequestBody Product product) { }  // ❌ Entity no Controller!

// ✅ CORRETO
@PostMapping
public ProductResponse create(@RequestBody CreateProductRequest request) { }
```

---

## 📦 Dependências (Versões Aprovadas)

```yaml
Java: 17 (LTS)
Spring Boot: 3.2.5
Spring Cloud: 2023.0.1
PostgreSQL Driver: 42.7.3
Oracle Driver: 23.3.0.23.09
MongoDB Driver: 4.11.1
Redis (Jedis): 5.1.2
RabbitMQ: 3.13.0
MapStruct: 1.5.5.Final
Lombok: 1.18.30
Testcontainers: 1.19.7
REST Assured: 5.4.0
Cucumber: 7.15.0
```

---

## 🔐 Segurança

### Secrets (NUNCA commitar)

```gitignore
*.env
application-secret.yml
*.p12
*.jks
*.key
```

### JWT (Autenticação)

```yaml
Strategy: JWT via API Gateway
Flow:
  1. POST /auth/login → retorna JWT
  2. Client: Authorization: Bearer {token}
  3. Gateway valida e roteia

Nota: Implementação SIMPLIFICADA (demo)
```

---

## 🐳 Infraestrutura

### Docker Compose (Local)

```yaml
Services:
  - postgresql (Product, Order)
  - oracle-xe (Payment)
  - mongodb (Notification)
  - redis (Cache)
  - rabbitmq (Mensageria)
```

### Kubernetes

```yaml
Namespace: enterprise-order
Resources:
  - Deployment (cada serviço)
  - Service (ClusterIP)
  - ConfigMap
  - Secret
  - HPA (Horizontal Pod Autoscaler)
```

---

## 🚀 CI/CD

### GitHub Actions

```yaml
Workflows:
  1. ci.yml (Build + Test)
     - Trigger: push em qualquer branch
     - Maven build
     - jUnit tests
     - Jacoco coverage

  2. quality.yml (Quality Gates)
     - Trigger: PR para develop/main
     - SonarQube scan
     - Checkstyle
     - SpotBugs

  3. cd.yml (Deploy)
     - Trigger: push na main
     - Build Docker images
     - Push to Docker Hub
     - Deploy K8s staging
```

---

## 📐 Protocolo SDD (SEMPRE SEGUIR)

### Fluxo de Desenvolvimento

```bash
1. /sdd:spec-init "{feature description}"
   → Cria .sdd/specs/{feature}/

2. /sdd:spec-requirements {feature}
   → Gera requirements.md (formato EARS)

3. /sdd:validate-gap {feature}
   → Analisa codebase, identifica gaps

4. /sdd:spec-design {feature}
   → Cria design.md (arquitetura, contratos, modelos)

5. /sdd:spec-tasks {feature}
   → Gera tasks.md (rastreáveis aos requisitos)

6. /sdd:spec-impl {feature}
   → Implementa seguindo TDD

7. /sdd:validate-impl {feature}
   → Valida aderência ao spec
```

### Steering Files (SEMPRE LER)

Antes de implementar QUALQUER feature, ler:

- `.sdd/steering/product.md` - Contexto de negócio
- `.sdd/steering/tech.md` - Decisões técnicas
- `.sdd/steering/structure.md` - Convenções de código
- `.sdd/steering/quality.md` - Padrões de qualidade

---

## ⚠️ REGRAS CRÍTICAS (BLOQUEADORAS)

### 1. **NUNCA commitar sem testes**
- Coverage mínimo: 95%
- Build DEVE passar antes de commit

### 2. **NUNCA pular fase de spec (SDD)**
- Design ANTES de implementação
- Aprovação humana antes de code

### 3. **NUNCA editar migration já aplicada**
- Criar nova migration para corrigir

### 4. **NUNCA mockar em testes de integração**
- Usar Testcontainers para dependências reais

### 5. **NUNCA usar System.out.println**
- Usar SLF4J (log.info, log.error, etc)

### 6. **NUNCA misturar Domain e Infrastructure**
- Domain = POJO puro
- Infrastructure = JPA, REST, etc

### 7. **NUNCA commitar secrets**
- Verificar .gitignore antes de commit

---

## 📊 Qualidade (Targets)

```yaml
SonarQube Quality Gates:
  Coverage: ≥ 95%
  Code Smells: < 50
  Bugs: 0 (blocker/critical)
  Vulnerabilities: 0
  Technical Debt: < 5%
  Duplications: < 3%
```

---

## 🐛 Incidentes e Lições Aprendidas

### Incidente #001: [Placeholder para futuros incidentes]

**Quando adicionar**:
- Bug crítico descoberto
- Decisão arquitetural revertida
- Edge case importante encontrado

**Formato**:
```markdown
### Incidente #XXX: {Título}
**Data**: YYYY-MM-DD
**Severidade**: Critical/High/Medium
**Descrição**: O que aconteceu
**Root Cause**: Por quê aconteceu
**Solução**: Como foi resolvido
**Prevenção**: Como evitar no futuro
```

---

## 📚 Recursos Úteis

### Documentação

- [Spring Boot Docs](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [SDD Methodology](https://github.com/leonardocs1000/SDD)
- [C4 Model](https://c4model.com/)
- [Testcontainers](https://testcontainers.com/)

### Comandos Úteis

```bash
# Build todos os serviços
./scripts/build-all.sh

# Executar todos os testes
./scripts/run-tests.sh

# Subir infraestrutura local
cd infrastructure/docker && docker-compose up -d

# Quality check completo
./scripts/quality-check.sh
```

---

## ✅ Checklist de Feature Completa

Antes de considerar uma feature "pronta":

- [ ] ✅ Spec criada e aprovada (SDD)
- [ ] ✅ Design documentado (.sdd/specs/{feature}/design.md)
- [ ] ✅ Testes unitários (coverage ≥ 95%)
- [ ] ✅ Testes de integração (Testcontainers)
- [ ] ✅ Testes BDD (Cucumber)
- [ ] ✅ Migrations criadas e testadas
- [ ] ✅ Swagger atualizado
- [ ] ✅ SonarQube OK
- [ ] ✅ Checkstyle OK
- [ ] ✅ Build passing
- [ ] ✅ PR aprovado
- [ ] ✅ CLAUDE.md atualizado (se decisão arquitetural)

---

## 🔄 Versionamento (Gitflow)

```bash
Branches:
  - main (produção)
  - develop (integração)
  - feature/{feature-name}
  - bugfix/{bug-name}
  - release/{version}
  - hotfix/{hotfix-name}

Commits:
  - Atômicos e descritivos
  - Formato: "{type}: {description}"
  - Tipos: feat, fix, refactor, test, docs, chore
```

---

## 🎯 Roadmap

### ✅ Fase 1: Fundação (Semana 1)
- [x] Estrutura SDD
- [ ] Product Service (CRUD + Cache)
- [ ] Order Service (básico)
- [ ] Docker Compose
- [ ] PostgreSQL + Flyway

### 📅 Fase 2: Microsserviços (Semana 2)
- [ ] Payment Service (Oracle)
- [ ] Notification Service
- [ ] RabbitMQ
- [ ] API Gateway
- [ ] SAGA + Circuit Breaker

### 📅 Fase 3: Qualidade & K8s (Semana 3)
- [ ] Coverage 95%+
- [ ] Testcontainers
- [ ] SonarQube
- [ ] Kubernetes manifests
- [ ] Helm Charts
- [ ] GitHub Actions

---

**Última atualização**: 2026-03-09
**Responsável**: Dev Team

---

## 🤖 Instruções para IA (Claude)

### Antes de QUALQUER implementação:

1. ✅ Ler CLAUDE.md (este arquivo)
2. ✅ Ler .sdd/steering/* (todos os arquivos)
3. ✅ Seguir protocolo SDD (spec → design → tasks → impl)
4. ✅ Verificar se há spec existente para o feature
5. ✅ Sempre usar TDD (teste primeiro)
6. ✅ Validar com quality checks antes de commit

### Quando tomar decisão arquitetural:

1. ✅ Documentar em ADR (docs/architecture/ADRs/)
2. ✅ Atualizar CLAUDE.md
3. ✅ Atualizar steering files relevantes
4. ✅ Obter aprovação humana antes de implementar

### Quando encontrar bug crítico:

1. ✅ Adicionar incidente em CLAUDE.md
2. ✅ Criar teste que reproduz o bug
3. ✅ Implementar fix
4. ✅ Atualizar regras para prevenir recorrência

---

**FIM DO DOCUMENTO**
