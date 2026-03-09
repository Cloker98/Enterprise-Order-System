# ADR-001: Hexagonal Architecture (Ports & Adapters)

**Status**: ✅ Aceito
**Data**: 2026-03-09
**Decisores**: Enterprise Team
**Tags**: architecture, ddd, clean-architecture

---

## Contexto

O projeto **Enterprise Order Management System** precisa demonstrar competências arquiteturais para uma vaga de **Java Developer Pleno**. O sistema deve ser:

- ✅ Testável (TDD com 95% coverage)
- ✅ Manutenível (separação clara de responsabilidades)
- ✅ Independente de frameworks (domain puro)
- ✅ Escalável (microsserviços)

---

## Decisão

Adotamos **Hexagonal Architecture (Ports & Adapters)** combinada com **Domain-Driven Design (DDD)** para todos os microsserviços.

### Estrutura de Camadas

```
domain/              # CORE - Regras de negócio puras
├── model/          # Entities, Value Objects, Aggregates
├── service/        # Domain Services
├── repository/     # Ports (interfaces)
└── exception/      # Domain Exceptions

application/        # Use Cases (orquestração)
├── usecase/       # Casos de uso (orchestrators)
├── dto/           # Request/Response DTOs
└── mapper/        # Domain ↔ DTO

infrastructure/    # Adapters (frameworks)
├── persistence/   # JPA Entities, Repositories
├── rest/          # REST Controllers
├── messaging/     # RabbitMQ (futuro)
└── config/        # Spring Configuration
```

### Regras Críticas

1. **Domain NÃO depende de nada** - Zero anotações de framework (@Entity, @RestController, etc.)
2. **Domain define Ports** - Interfaces (ex: `ProductRepository`)
3. **Infrastructure implementa Adapters** - Implementações concretas (ex: `ProductRepositoryImpl`)
4. **Application orquestra** - Use Cases coordenam domain + repositories
5. **Dependency Inversion** - Setas apontam para dentro (para o domain)

---

## Alternativas Consideradas

### 1. **Arquitetura em Camadas Tradicional** (Rejected)

```
Controller → Service → Repository → Database
```

**Prós**:
- Simples e familiar
- Menos código boilerplate

**Contras**:
- ❌ Domain acoplado ao framework
- ❌ Difícil de testar (mocks complicados)
- ❌ Violação de responsabilidades
- ❌ Não demonstra competência arquitetural avançada

**Decisão**: ❌ Rejeitada - não atende os requisitos de qualidade

---

### 2. **Clean Architecture (Uncle Bob)** (Considered)

**Prós**:
- Separação clara de camadas
- Testabilidade
- Framework-agnostic

**Contras**:
- Mais camadas que Hexagonal (Entities, Use Cases, Adapters, Frameworks)
- Complexidade desnecessária para este projeto

**Decisão**: ⚠️ Considerada mas não escolhida - Hexagonal é suficiente e mais pragmática

---

### 3. **Hexagonal Architecture** (Chosen) ✅

**Prós**:
- ✅ Domain puro (framework-agnostic)
- ✅ Testabilidade máxima (mock ports, não frameworks)
- ✅ Separação clara: Domain ← Application ← Infrastructure
- ✅ Fácil trocar adapters (ex: trocar JPA por MongoDB, trocar REST por gRPC)
- ✅ DDD-friendly (Aggregates, Value Objects, Domain Services)
- ✅ **Demonstra competência técnica avançada** (importante para vaga)

**Contras**:
- Mais código inicial (mappers, adapters)
- Curva de aprendizado maior

**Decisão**: ✅ **ESCOLHIDA** - benefícios superam os contras

---

## Consequências

### Positivas ✅

1. **Testabilidade**: Unit tests do domain SEM Spring, SEM database
2. **Manutenibilidade**: Mudanças em frameworks não afetam domain
3. **Competência técnica**: Projeto demonstra conhecimento arquitetural avançado
4. **DDD**: Permite modelagem rica do domínio (Aggregates, Value Objects)
5. **Flexibilidade**: Fácil adicionar novos adapters (GraphQL, gRPC, eventos)

### Negativas ⚠️

1. **Mais código**: Mappers (Domain ↔ JPA, Domain ↔ DTO)
2. **Complexidade inicial**: Time precisa entender Hexagonal
3. **Boilerplate**: Mais classes e interfaces

### Mitigações

- **MapStruct** - gera mappers automaticamente (reduz boilerplate)
- **Lombok** - reduz getters/setters
- **CLAUDE.md** - documenta regras arquiteturais claramente
- **Code reviews** - garantir aderência aos padrões

---

## Implementação

### Exemplo: Product Service

**Domain** (puro):
```java
public class Product {  // SEM @Entity!
  public void decreaseStock(int quantity) {
    if (quantity > this.stockQuantity) {
      throw new InsufficientStockException(...);
    }
    this.stockQuantity -= quantity;
  }
}

public interface ProductRepository {  // PORT
  Product save(Product product);
}
```

**Infrastructure** (adapter):
```java
@Entity @Table(name = "products")
public class ProductJpaEntity { ... }  // JPA separado!

@Repository
public class ProductRepositoryImpl implements ProductRepository {  // ADAPTER
  // Implementa o PORT usando JPA
}
```

**Application** (orchestration):
```java
@Service
public class CreateProductUseCase {
  public ProductResponse execute(CreateProductRequest request) {
    Product product = Product.create(...);  // Domain
    Product saved = repository.save(product);  // Port
    return mapper.toResponse(saved);  // DTO
  }
}
```

---

## Validação

Esta decisão será validada através de:

- ✅ Code reviews (aderência aos padrões)
- ✅ Testes unitários (domain testado sem Spring)
- ✅ Checkstyle (não permitir anotações de framework no domain)
- ✅ SonarQube (baixo acoplamento)

---

## Referências

- [Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/)
- [DDD by Eric Evans](https://www.domainlanguage.com/ddd/)
- [Clean Architecture by Uncle Bob](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Spring Boot Hexagonal](https://www.baeldung.com/hexagonal-architecture-ddd-spring)

---

## Revisões

| Data | Mudança | Autor |
|------|---------|-------|
| 2026-03-09 | Criação inicial | Enterprise Team |

---

**Próxima revisão**: Após implementação completa do primeiro microsserviço (Product Service)
