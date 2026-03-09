# ADR-002: Cache-Aside Pattern com Redis

**Status**: ✅ Aceito
**Data**: 2026-03-09
**Decisores**: Enterprise Team
**Tags**: performance, cache, redis

---

## Contexto

O **Product Service** precisa atender requisitos de performance:
- **Latência média**: < 50ms (GET /products/{id})
- **Latência P99**: < 200ms
- **Throughput**: > 1000 req/s

Database round-trip típico: ~100-200ms (inaceitável para target de 50ms)

---

## Decisão

Implementar **Cache-Aside Pattern** (também conhecido como **Lazy Loading**) usando **Redis** como cache distribuído.

### Fluxo

```
1. GET /products/{id}
   ↓
2. ProductRepository.findById(id)
   ↓
3. Checar Redis cache
   ├─ HIT → retornar cached product ✅ (~10-30ms)
   └─ MISS → buscar PostgreSQL → cachear resultado → retornar (~100-150ms)

4. POST/PUT/DELETE
   ↓
5. Atualizar database
   ↓
6. Invalidar cache (cache.evict)
```

### Configuração

- **TTL**: 5 minutos (300 segundos)
- **Serialização**: JSON (GenericJackson2JsonRedisSerializer)
- **Key format**: `products::{productId}`
- **Eviction**: Automática após TTL + manual após write operations

---

## Alternativas Consideradas

### 1. **Write-Through Cache** (Rejected)

Escrever no cache E database simultaneamente.

**Contras**:
- ❌ Complexidade adicional
- ❌ Latência de escrita aumenta
- ❌ Não necessário para read-heavy workload

---

### 2. **Write-Behind Cache** (Rejected)

Escrever no cache imediatamente, database async.

**Contras**:
- ❌ Risco de perda de dados (cache volátil)
- ❌ Complexidade de sincronização
- ❌ Overkill para este projeto

---

### 3. **Cache-Aside** (Chosen) ✅

**Prós**:
- ✅ Simples de implementar
- ✅ Seguro (database é source of truth)
- ✅ Read-heavy workload (80/20 reads vs writes)
- ✅ TTL evita dados obsoletos
- ✅ Falha do cache não quebra aplicação (fallback para DB)

---

## Consequências

### Positivas ✅

1. **Performance**: ~70% reduction na latência (cache hit)
2. **Escalabilidade**: Reduz carga no PostgreSQL
3. **Simplicidade**: Padrão bem conhecido, fácil de debugar
4. **Resiliência**: Se Redis cai, app continua funcionando (fallback DB)

### Negativas ⚠️

1. **Cache invalidation**: Se invalidação falhar, dados obsoletos por até 5 min
2. **Memória**: Redis precisa de RAM (estimativa: ~500KB por 1000 produtos)
3. **Complexidade operacional**: Mais um componente para monitorar

### Mitigações

- **Circuit Breaker** (futuro): Se Redis falhar, bypass cache e ir direto ao DB
- **Monitoring**: Métricas de cache hit rate (target: > 80%)
- **TTL curto**: 5 minutos (balanço entre performance e staleness)

---

## Implementação

### ProductRepositoryImpl

```java
@Override
public Optional<Product> findById(ProductId id) {
    // 1. Try cache first
    Optional<Product> cached = cacheService.get(id.toString());
    if (cached.isPresent()) {
        return cached;  // CACHE HIT ✅
    }

    // 2. Cache miss - query database
    Optional<Product> product = jpaRepository.findById(id.value())
        .map(jpaMapper::toDomain);

    // 3. Cache result (if found)
    product.ifPresent(p -> cacheService.put(id.toString(), p));

    return product;
}

@Override
public Product save(Product product) {
    ProductJpaEntity saved = jpaRepository.save(...);

    // Invalidate cache on write
    cacheService.evict(product.getId().toString());

    return jpaMapper.toDomain(saved);
}
```

---

## Métricas de Sucesso

| Métrica | Target | Como Medir |
|---------|--------|------------|
| Cache Hit Rate | > 80% | Logs + Actuator metrics |
| Latência (cache hit) | < 50ms | Testes de performance |
| Latência (cache miss) | < 200ms | Testes de performance |
| Redis uptime | > 99.9% | Monitoring |

---

## Próximos Passos

### Fase 2 (futuro):
- [ ] Circuit Breaker para Redis (Resilience4j)
- [ ] Micrometer metrics para cache hit rate
- [ ] Redis Cluster (HA)
- [ ] Distributed tracing (cache latency)

---

## Referências

- [Cache-Aside Pattern](https://docs.microsoft.com/en-us/azure/architecture/patterns/cache-aside)
- [Redis Best Practices](https://redis.io/docs/manual/patterns/)
- [Spring Cache Abstraction](https://docs.spring.io/spring-framework/docs/current/reference/html/integration.html#cache)

---

## Revisões

| Data | Mudança | Autor |
|------|---------|-------|
| 2026-03-09 | Criação inicial | Enterprise Team |

---

**Próxima revisão**: Após testes de performance (validar se targets foram atingidos)
