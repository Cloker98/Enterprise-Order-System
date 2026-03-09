# Product Steering - Contexto de Negócio

## 📌 Propósito do Produto

**Enterprise Order Management System** é um sistema de gerenciamento de pedidos empresarial que demonstra arquitetura de microsserviços de nível produção.

### Objetivos

1. **Demonstração Técnica**: Showcasing de habilidades técnicas para posição Java Developer Pleno
2. **Referência de Arquitetura**: Implementação de padrões e práticas de mercado
3. **Portfólio**: Projeto público no GitHub para avaliação de recrutadores

---

## 🎯 Domínio de Negócio

### Core Domain: E-commerce Order Management

O sistema gerencia o **ciclo de vida completo de pedidos** em um contexto empresarial:

```
Cliente → Catálogo → Carrinho → Pedido → Pagamento → Notificação
```

### Bounded Contexts (DDD)

#### 1. **Product Context** (Catálogo)
- Gerenciamento de produtos
- Controle de estoque
- Categorização
- Pricing

#### 2. **Order Context** (Pedidos)
- Criação de pedidos
- Estados do pedido (Draft, Pending, Confirmed, Cancelled)
- Orquestração de transações distribuídas (SAGA)

#### 3. **Payment Context** (Pagamentos)
- Processamento de pagamentos
- Múltiplos métodos (Cartão, Boleto, PIX)
- Integração com gateways
- Resiliência (Circuit Breaker)

#### 4. **Notification Context** (Notificações)
- Envio de emails
- SMS
- Push notifications
- Logs de auditoria

---

## 👥 Usuários (Personas)

### 1. **Administrador do Sistema**
- Gerencia catálogo de produtos
- Monitora pedidos
- Acessa dashboards
- Configura regras de negócio

### 2. **Cliente Final**
- Navega catálogo
- Cria pedidos
- Realiza pagamentos
- Recebe notificações

### 3. **Desenvolvedor/Operações**
- Integra APIs
- Monitora saúde dos serviços
- Deploy e manutenção
- Análise de logs

---

## 🔑 Requisitos de Negócio (Alto Nível)

### Funcionais

1. **Gestão de Produtos**
   - CRUD completo
   - Busca por filtros
   - Cache para performance

2. **Gestão de Pedidos**
   - Criação de pedidos multi-itens
   - Cálculo automático de totais
   - Validação de estoque
   - Transações distribuídas (SAGA)

3. **Processamento de Pagamentos**
   - Múltiplos métodos
   - Validação de dados
   - Retry em falhas
   - Rollback automático

4. **Notificações**
   - Email de confirmação
   - Status de pagamento
   - Atualizações do pedido
   - Histórico completo

### Não-Funcionais

1. **Performance**
   - Tempo de resposta < 500ms (95th percentile)
   - Cache Redis para queries frequentes
   - Batch processing para notificações

2. **Escalabilidade**
   - Horizontal scaling via Kubernetes
   - Stateless services
   - Event-driven architecture

3. **Resiliência**
   - Circuit Breaker em integrações
   - Retry policies
   - Fallback mechanisms
   - Health checks

4. **Observabilidade**
   - Logs estruturados (JSON)
   - Metrics (Prometheus)
   - Tracing distribuído (Zipkin/Jaeger)
   - Alerting

---

## 🚫 Fora do Escopo (Explicitamente)

Para manter o projeto focado e demonstrável, **NÃO** será implementado:

- ❌ Frontend (foco é backend)
- ❌ Autenticação real (será mock/básico)
- ❌ Integrações com gateways de pagamento reais
- ❌ Envio real de emails/SMS (será simulado)
- ❌ Infraestrutura cloud real (AWS/Azure)
- ❌ CI/CD em produção real

---

## 📊 Métricas de Sucesso

### Técnicas

- ✅ Coverage de testes > 90%
- ✅ Build time < 5 min
- ✅ Zero bugs críticos no SonarQube
- ✅ Deploy em K8s funcional

### Demonstração

- ✅ README completo e profissional
- ✅ Diagramas de arquitetura (C4)
- ✅ ADRs documentando decisões
- ✅ Swagger UI funcional
- ✅ Demo em vídeo (opcional)

---

## 🎓 Contexto: Vaga Java Developer Pleno

### Requisitos da Vaga Cobertos

| Requisito | Onde Demonstrado |
|-----------|------------------|
| Java 17 + Spring Boot | Todos os serviços |
| APIs REST + Swagger | Todos os endpoints documentados |
| Microsserviços + Mensageria | Arquitetura completa |
| Git + Gitflow | Branches e commits organizados |
| jUnit + Versionamento BD | Testes + Flyway |
| Kubernetes | Manifests + Helm |
| Padrões de Projeto | SAGA, Circuit Breaker, etc |
| Oracle DB | Payment Service |
| Arquitetura de Software | Diagramas C4 + ADRs |

---

## 📝 Glossário de Negócio

- **Order**: Pedido realizado por um cliente
- **Product**: Item do catálogo disponível para venda
- **Payment**: Transação financeira associada a um pedido
- **Notification**: Comunicação enviada ao cliente
- **SKU**: Stock Keeping Unit (identificador único do produto)
- **SAGA**: Padrão de transação distribuída
- **Idempotência**: Garantia de que operações duplicadas não causam efeitos colaterais

---

## 🔄 Evolução do Produto

### Versão 1.0 (MVP)
- CRUD básico de todos os contextos
- Fluxo completo: Produto → Pedido → Pagamento → Notificação
- Deploy local (Docker Compose)

### Versão 2.0 (Futuro)
- Kubernetes production-ready
- Observabilidade completa
- Performance tuning
- Load testing

---

**Última atualização**: 2026-03-09
**Responsável**: Dev Team
