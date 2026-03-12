# Order Service - Requirements Specification

**Data**: 2026-03-11 16:35
**Fase**: SDD Phase 2 - REQUIREMENTS
**Serviço**: Order Service
**Versão**: 1.0.0-SNAPSHOT

---

## 1. Contexto e Objetivos

### 1.1 Visão Geral
O **Order Service** é o microsserviço central do sistema de gerenciamento de pedidos empresarial, responsável por orquestrar o processo completo de criação, processamento e acompanhamento de pedidos.

### 1.2 Objetivos de Negócio
- **OB-001**: Permitir criação de pedidos com múltiplos produtos
- **OB-002**: Orquestrar processo de pagamento via SAGA pattern
- **OB-003**: Garantir consistência eventual entre serviços
- **OB-004**: Prover rastreabilidade completa do pedido
- **OB-005**: Suportar cancelamento e compensação de transações

### 1.3 Stakeholders
- **Cliente**: Usuário final que cria pedidos
- **Administrador**: Gerencia pedidos e monitora sistema
- **Product Service**: Fornece dados de produtos e controla estoque
- **Payment Service**: Processa pagamentos
- **Notification Service**: Envia notificações

---

## 2. Requisitos Funcionais (EARS Format)

### RF-001: Criar Pedido
**WHEN** o cliente submete um pedido com produtos válidos  
**THE SYSTEM SHALL** criar um novo pedido com status PENDING  
**AND** validar disponibilidade de estoque dos produtos  
**AND** calcular valor total do pedido  
**AND** retornar ID do pedido criado

**Critérios de Aceitação**:
- Pedido deve ter pelo menos 1 produto
- Quantidade solicitada deve estar disponível em estoque
- Valor total deve ser calculado corretamente (produto.preço × quantidade)
- Status inicial deve ser PENDING
- Deve gerar evento OrderCreated

### RF-002: Buscar Pedido por ID
**WHEN** o usuário solicita um pedido por ID válido  
**THE SYSTEM SHALL** retornar os dados completos do pedido  
**INCLUDING** produtos, quantidades, valores e status atual

**Critérios de Aceitação**:
- Retornar 404 se pedido não existir
- Incluir histórico de status do pedido
- Mostrar produtos com preços no momento da compra

### RF-003: Listar Pedidos do Cliente
**WHEN** o cliente solicita seus pedidos  
**THE SYSTEM SHALL** retornar lista paginada de pedidos  
**WITH** filtros por status e período

**Critérios de Aceitação**:
- Paginação padrão: 20 pedidos por página
- Filtros: status (PENDING, CONFIRMED, CANCELLED)
- Filtros: período (dataInicio, dataFim)
- Ordenação por data de criação (mais recente primeiro)

### RF-004: Processar Pagamento (SAGA Orchestration)
**WHEN** um pedido é criado com sucesso  
**THE SYSTEM SHALL** iniciar processo de pagamento via SAGA  
**AND** decrementar estoque dos produtos  
**AND** solicitar processamento do pagamento  
**AND** atualizar status baseado no resultado

**Critérios de Aceitação**:
- Decrementar estoque ANTES de solicitar pagamento
- Se pagamento falhar, reverter estoque (compensação)
- Atualizar status para CONFIRMED se pagamento suceder
- Atualizar status para CANCELLED se pagamento falhar
- Gerar eventos apropriados em cada etapa

### RF-005: Cancelar Pedido
**WHEN** o cliente ou sistema cancela um pedido  
**THE SYSTEM SHALL** verificar se cancelamento é permitido  
**AND** reverter estoque se necessário  
**AND** solicitar estorno do pagamento se aplicável  
**AND** atualizar status para CANCELLED

**Critérios de Aceitação**:
- Permitir cancelamento apenas para status PENDING ou CONFIRMED
- Reverter estoque para pedidos CONFIRMED
- Solicitar estorno para pedidos CONFIRMED com pagamento processado
- Não permitir cancelamento para pedidos já CANCELLED ou DELIVERED

### RF-006: Atualizar Status do Pedido
**WHEN** o sistema recebe evento de mudança de status  
**THE SYSTEM SHALL** atualizar status do pedido  
**AND** registrar timestamp da mudança  
**AND** gerar evento de notificação se necessário

**Critérios de Aceitação**:
- Validar transições de status permitidas
- Registrar histórico de mudanças
- Gerar evento OrderStatusChanged

### RF-007: Calcular Valor Total
**WHEN** o sistema calcula valor do pedido  
**THE SYSTEM SHALL** somar (preço × quantidade) de todos os produtos  
**AND** aplicar descontos se houver  
**AND** calcular impostos se aplicável

**Critérios de Aceitação**:
- Usar preços dos produtos no momento da criação do pedido
- Suportar descontos percentuais e valores fixos
- Calcular impostos baseado em regras de negócio

---

## 3. Requisitos Não Funcionais

### RNF-001: Performance
- **Tempo de resposta**: < 500ms para operações de leitura
- **Tempo de resposta**: < 2s para criação de pedido
- **Throughput**: Suportar 1000 pedidos/minuto
- **Concorrência**: Suportar 100 usuários simultâneos

### RNF-002: Disponibilidade
- **Uptime**: 99.9% (8.76 horas de downtime/ano)
- **Recovery Time**: < 5 minutos após falha
- **Graceful degradation**: Continuar funcionando mesmo com falhas de serviços externos

### RNF-003: Consistência
- **Eventual Consistency**: Aceitar inconsistência temporária entre serviços
- **SAGA Compensation**: Garantir compensação em caso de falhas
- **Idempotência**: Operações devem ser idempotentes

### RNF-004: Segurança
- **Autenticação**: JWT token obrigatório
- **Autorização**: Cliente só acessa seus próprios pedidos
- **Auditoria**: Log de todas as operações críticas

### RNF-005: Observabilidade
- **Logging**: Structured logging com correlation ID
- **Metrics**: Métricas de negócio e técnicas
- **Tracing**: Distributed tracing entre serviços
- **Health Checks**: Endpoint de health check

---

## 4. Regras de Negócio

### RN-001: Validação de Estoque
- Pedido só pode ser criado se todos os produtos tiverem estoque suficiente
- Estoque deve ser decrementado atomicamente durante criação do pedido
- Em caso de falha no pagamento, estoque deve ser revertido

### RN-002: Estados do Pedido
```
PENDING → CONFIRMED (pagamento aprovado)
PENDING → CANCELLED (pagamento rejeitado ou cancelamento manual)
CONFIRMED → CANCELLED (cancelamento manual)
CANCELLED → [estado final]
```

### RN-003: Cálculo de Preços
- Preços dos produtos são "congelados" no momento da criação do pedido
- Mudanças de preço posteriores não afetam pedidos já criados
- Descontos são aplicados sobre o subtotal

### RN-004: Cancelamento
- Pedidos PENDING podem ser cancelados sem restrições
- Pedidos CONFIRMED podem ser cancelados com estorno
- Pedidos CANCELLED não podem ser alterados

### RN-005: Compensação SAGA
- Se decrementar estoque falhar → cancelar pedido
- Se pagamento falhar → reverter estoque + cancelar pedido
- Se notificação falhar → não afetar pedido (retry assíncrono)

---

## 5. Eventos de Domínio

### OrderCreated
```json
{
  "eventType": "OrderCreated",
  "orderId": "uuid",
  "customerId": "uuid",
  "items": [
    {
      "productId": "uuid",
      "quantity": 2,
      "unitPrice": 999.99
    }
  ],
  "totalAmount": 1999.98,
  "timestamp": "2026-03-11T16:35:00Z"
}
```

### OrderStatusChanged
```json
{
  "eventType": "OrderStatusChanged",
  "orderId": "uuid",
  "previousStatus": "PENDING",
  "newStatus": "CONFIRMED",
  "reason": "Payment processed successfully",
  "timestamp": "2026-03-11T16:36:00Z"
}
```

### OrderCancelled
```json
{
  "eventType": "OrderCancelled",
  "orderId": "uuid",
  "reason": "Payment failed",
  "compensationRequired": true,
  "timestamp": "2026-03-11T16:37:00Z"
}
```

---

## 6. Integrações

### 6.1 Product Service
- **GET /api/v1/products/{id}**: Buscar dados do produto
- **POST /api/v1/products/{id}/decrease-stock**: Decrementar estoque
- **POST /api/v1/products/{id}/increase-stock**: Reverter estoque (compensação)

### 6.2 Payment Service
- **POST /api/v1/payments**: Processar pagamento
- **POST /api/v1/payments/{id}/refund**: Solicitar estorno

### 6.3 Notification Service
- **Eventos**: OrderCreated, OrderStatusChanged, OrderCancelled
- **Canais**: Email, SMS, Push notification

### 6.4 RabbitMQ
- **Exchange**: order.events
- **Queues**: 
  - payment.order.created
  - notification.order.events
  - order.payment.processed
  - order.payment.failed

---

## 7. Casos de Uso Principais

### UC-001: Fluxo Feliz - Pedido Aprovado
```
1. Cliente cria pedido com produtos válidos
2. Sistema valida estoque disponível
3. Sistema decrementa estoque dos produtos
4. Sistema cria pedido com status PENDING
5. Sistema publica evento OrderCreated
6. Payment Service processa pagamento
7. Payment Service publica PaymentProcessed
8. Order Service atualiza status para CONFIRMED
9. Notification Service envia confirmação
```

### UC-002: Fluxo de Compensação - Pagamento Falhou
```
1-5. (igual ao fluxo feliz)
6. Payment Service falha no processamento
7. Payment Service publica PaymentFailed
8. Order Service recebe evento
9. Order Service reverte estoque (compensação)
10. Order Service atualiza status para CANCELLED
11. Notification Service envia notificação de cancelamento
```

### UC-003: Cancelamento Manual
```
1. Cliente solicita cancelamento de pedido CONFIRMED
2. Sistema valida se cancelamento é permitido
3. Sistema solicita estorno ao Payment Service
4. Sistema reverte estoque dos produtos
5. Sistema atualiza status para CANCELLED
6. Sistema publica evento OrderCancelled
7. Notification Service envia confirmação de cancelamento
```

---

## 8. Critérios de Aceitação Globais

### Funcionalidade
- [ ] Todos os 7 requisitos funcionais implementados
- [ ] SAGA pattern funcionando com compensação
- [ ] Eventos publicados corretamente
- [ ] Integrações com outros serviços funcionais

### Qualidade
- [ ] Cobertura de testes ≥ 95%
- [ ] Testes unitários para toda lógica de domínio
- [ ] Testes de integração com Testcontainers
- [ ] Testes E2E do fluxo completo

### Arquitetura
- [ ] Hexagonal Architecture implementada
- [ ] Domain sem dependências de framework
- [ ] Event-driven communication
- [ ] Circuit breaker para chamadas externas

### Observabilidade
- [ ] Structured logging com correlation ID
- [ ] Health checks implementados
- [ ] Métricas de negócio expostas
- [ ] Swagger/OpenAPI documentado

---

## 9. Fora do Escopo (v1.0)

- **Múltiplos endereços de entrega**: Apenas um endereço por pedido
- **Pagamento parcelado**: Apenas pagamento à vista
- **Cupons de desconto**: Sistema de descontos simplificado
- **Múltiplas formas de pagamento**: Apenas uma por pedido
- **Entrega agendada**: Apenas entrega padrão
- **Pedidos recorrentes**: Apenas pedidos únicos

---

## 10. Dependências e Riscos

### Dependências
- **Product Service**: Deve estar funcionando para validar produtos e estoque
- **Payment Service**: Será desenvolvido em paralelo
- **RabbitMQ**: Infraestrutura deve estar configurada
- **PostgreSQL**: Database dedicado para Order Service

### Riscos
- **RISCO-001**: Complexidade do SAGA pattern pode causar atrasos
  - *Mitigação*: Implementar versão simplificada primeiro
- **RISCO-002**: Integração com Payment Service pode ter problemas
  - *Mitigação*: Usar mock/stub durante desenvolvimento
- **RISCO-003**: Performance pode ser impactada por múltiplas chamadas de serviço
  - *Mitigação*: Implementar cache e otimizações

---

## 11. Cronograma Estimado

| Fase | Duração | Entregáveis |
|------|---------|-------------|
| **Design** | 1 dia | Arquitetura, modelos de domínio, APIs |
| **Domain Layer** | 2 dias | Entities, Value Objects, Domain Services |
| **Application Layer** | 2 dias | Use Cases, DTOs, Mappers |
| **Infrastructure** | 2 dias | JPA, REST, Events, Cache |
| **SAGA Implementation** | 3 dias | Orchestration, Compensation |
| **Integration** | 2 dias | Product Service, Payment Service |
| **Testing** | 2 dias | Unit, Integration, E2E |
| **Documentation** | 1 dia | README, Swagger, ADRs |

**Total**: 15 dias úteis (3 semanas)

---

## 12. Aprovação

### Critérios de Aprovação
- [ ] ✅ Requisitos funcionais claros e testáveis
- [ ] ✅ Regras de negócio bem definidas
- [ ] ✅ Integrações especificadas
- [ ] ✅ Eventos de domínio modelados
- [ ] ✅ Casos de uso documentados
- [ ] ✅ Critérios de aceitação definidos

### Próximos Passos
1. **Design Phase**: Criar design.md com arquitetura detalhada
2. **Task Breakdown**: Criar tasks.md com implementação rastreável
3. **Approval**: Obter aprovação humana antes de implementar

---

**Especificado por**: Claude Sonnet 3.5  
**Metodologia**: SDD (Specification-Driven Development)  
**Timestamp**: 2026-03-11T16:35:00-03:00  
**Status**: ⏳ AGUARDANDO APROVAÇÃO