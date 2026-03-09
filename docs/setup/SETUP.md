# 🚀 Setup Guide - Enterprise Order Management System

Este guia contém instruções detalhadas para configurar o ambiente de desenvolvimento local.

---

## 📋 Pré-requisitos

### Obrigatórios

| Software | Versão Mínima | Como Verificar |
|----------|---------------|----------------|
| **Java JDK** | 17 (LTS) | `java -version` |
| **Maven** | 3.9+ | `mvn -version` |
| **Docker** | 24.0+ | `docker --version` |
| **Docker Compose** | 2.0+ | `docker-compose --version` |
| **Git** | 2.40+ | `git --version` |

### Opcionais (Recomendados)

- **IntelliJ IDEA** (Ultimate ou Community)
- **VS Code** com extensões Java
- **Postman** ou **Insomnia** (para testar APIs)
- **DBeaver** ou **DataGrip** (para gerenciar databases)

---

## 🛠️ Instalação dos Pré-requisitos

### Windows

#### Java 17
```powershell
# Via Chocolatey
choco install openjdk17

# Ou baixar manualmente
# https://adoptium.net/temurin/releases/?version=17
```

#### Maven
```powershell
# Via Chocolatey
choco install maven

# Verificar
mvn -version
```

#### Docker Desktop
```powershell
# Baixar e instalar
# https://www.docker.com/products/docker-desktop

# Verificar
docker --version
docker-compose --version
```

### macOS

```bash
# Homebrew
brew install openjdk@17
brew install maven
brew install --cask docker

# Verificar
java -version
mvn -version
docker --version
```

### Linux (Ubuntu/Debian)

```bash
# Java 17
sudo apt update
sudo apt install openjdk-17-jdk

# Maven
sudo apt install maven

# Docker
sudo apt install docker.io docker-compose

# Adicionar usuário ao grupo docker
sudo usermod -aG docker $USER
newgrp docker

# Verificar
java -version
mvn -version
docker --version
```

---

## 🚀 Setup Rápido (Recomendado)

### 1. Clone o Repositório

```bash
git clone https://github.com/seu-usuario/enterprise-order-system.git
cd enterprise-order-system
```

### 2. Execute o Script de Setup

```bash
# Dar permissão de execução (Linux/Mac)
chmod +x scripts/setup-local.sh

# Executar
./scripts/setup-local.sh
```

O script irá:
- ✅ Verificar todos os pré-requisitos
- ✅ Iniciar todos os containers Docker
- ✅ Aguardar serviços ficarem prontos
- ✅ Exibir informações de conexão

---

## 🐳 Setup Manual (Passo a Passo)

### 1. Iniciar Infraestrutura

```bash
cd infrastructure/docker
docker-compose up -d
```

Containers iniciados:
- **PostgreSQL** (porta 5432) - Product & Order databases
- **Oracle XE** (porta 1521) - Payment database
- **MongoDB** (porta 27017) - Notification database
- **Redis** (porta 6379) - Cache
- **RabbitMQ** (portas 5672, 15672) - Message broker

### 2. Verificar Status dos Containers

```bash
docker ps

# Deve mostrar 5 containers rodando:
# eos-postgresql
# eos-oracle
# eos-mongodb
# eos-redis
# eos-rabbitmq
```

### 3. Verificar Logs (se necessário)

```bash
# Todos os serviços
docker-compose logs -f

# Serviço específico
docker-compose logs -f postgresql
docker-compose logs -f oracle
```

### 4. Aguardar Oracle Inicializar

⚠️ **IMPORTANTE**: O Oracle pode levar 1-2 minutos para inicializar completamente.

```bash
# Verificar se Oracle está pronto
docker exec eos-oracle healthcheck.sh

# Deve retornar: "Oracle Database is ready"
```

---

## 💾 Configuração dos Databases

### PostgreSQL

#### Conexão via CLI

```bash
# Conectar ao PostgreSQL
docker exec -it eos-postgresql psql -U admin -d product_db

# Listar databases
\l

# Conectar a outro database
\c order_db

# Listar tabelas
\dt

# Sair
\q
```

#### Conexão via JDBC

```properties
# Product Service
spring.datasource.url=jdbc:postgresql://localhost:5432/product_db
spring.datasource.username=admin
spring.datasource.password=admin123

# Order Service
spring.datasource.url=jdbc:postgresql://localhost:5432/order_db
spring.datasource.username=admin
spring.datasource.password=admin123
```

### Oracle

#### Conexão via SQL*Plus

```bash
# Conectar ao Oracle
docker exec -it eos-oracle sqlplus payment_user/payment_pass@XE

# Verificar conexão
SELECT * FROM DUAL;

# Sair
EXIT;
```

#### Conexão via JDBC

```properties
# Payment Service
spring.datasource.url=jdbc:oracle:thin:@localhost:1521/XE
spring.datasource.username=payment_user
spring.datasource.password=payment_pass
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver
```

### MongoDB

#### Conexão via Mongosh

```bash
# Conectar ao MongoDB
docker exec -it eos-mongodb mongosh -u admin -p admin123

# Usar database
use notification_db

# Listar collections
show collections

# Query exemplo
db.notifications.find().pretty()

# Sair
exit
```

#### Conexão via URI

```properties
# Notification Service
spring.data.mongodb.uri=mongodb://admin:admin123@localhost:27017/notification_db
```

### Redis

#### Conexão via CLI

```bash
# Conectar ao Redis
docker exec -it eos-redis redis-cli -a redis123

# Testar conexão
PING
# Resposta: PONG

# Set/Get valor
SET test "Hello World"
GET test

# Listar todas as keys
KEYS *

# Sair
exit
```

### RabbitMQ

#### Management UI

Acesse: http://localhost:15672

```
Username: admin
Password: admin123
```

#### Criar Exchanges e Queues (via UI)

1. **Exchanges** (aba "Exchanges" → Add):
   - `order.events` (type: topic)
   - `payment.events` (type: topic)
   - `notification.events` (type: fanout)

2. **Queues** (aba "Queues" → Add):
   - `order.created`
   - `order.cancelled`
   - `payment.processed`
   - `payment.failed`
   - `notification.email`

3. **Bindings** (em cada Exchange):
   - Bind queues com routing keys apropriadas

---

## 🏗️ Build dos Microsserviços

### Product Service

```bash
cd services/product-service
mvn clean install

# Executar testes
mvn test

# Executar localmente
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Swagger: http://localhost:8081/swagger-ui.html
```

### Order Service

```bash
cd services/order-service
mvn clean install
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Swagger: http://localhost:8082/swagger-ui.html
```

### Payment Service

```bash
cd services/payment-service
mvn clean install
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Swagger: http://localhost:8083/swagger-ui.html
```

### Notification Service

```bash
cd services/notification-service
mvn clean install
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Swagger: http://localhost:8084/swagger-ui.html
```

### API Gateway

```bash
cd services/api-gateway
mvn clean install
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Swagger agregado: http://localhost:8080/swagger-ui.html
```

---

## 🧪 Executar Testes

### Todos os Testes (Todos os Serviços)

```bash
# Na raiz do projeto
./scripts/run-tests.sh
```

### Testes de um Serviço Específico

```bash
cd services/product-service

# Apenas testes unitários
mvn test

# Testes de integração (com Testcontainers)
mvn verify

# Com coverage
mvn clean test jacoco:report

# Relatório: target/site/jacoco/index.html
```

---

## 🔍 Verificar Qualidade

### SonarQube (Local)

#### 1. Iniciar SonarQube

```bash
docker run -d --name sonarqube -p 9000:9000 sonarqube:community
```

#### 2. Acessar

```
URL: http://localhost:9000
Default: admin / admin
```

#### 3. Criar Token

1. Login → My Account → Security → Generate Token
2. Copiar token

#### 4. Executar Análise

```bash
cd services/product-service

mvn clean verify sonar:sonar \
  -Dsonar.projectKey=product-service \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=seu-token-aqui
```

---

## 🐞 Troubleshooting

### Problema: Container não inicia

```bash
# Ver logs
docker-compose logs [service-name]

# Reiniciar container específico
docker-compose restart [service-name]

# Recriar container
docker-compose up -d --force-recreate [service-name]
```

### Problema: Porta já em uso

```bash
# Descobrir processo usando a porta (Linux/Mac)
lsof -i :5432

# Descobrir processo usando a porta (Windows)
netstat -ano | findstr :5432

# Matar processo (substituir PID)
kill -9 [PID]  # Linux/Mac
taskkill /PID [PID] /F  # Windows
```

### Problema: Oracle demorando muito

```bash
# Verificar logs
docker logs -f eos-oracle

# Aguardar "DATABASE IS READY TO USE!"

# Se travar, reiniciar
docker restart eos-oracle
```

### Problema: Testcontainers falhando

```bash
# Limpar containers do Testcontainers
docker rm -f $(docker ps -a -q --filter="label=org.testcontainers=true")

# Limpar imagens não usadas
docker image prune -a
```

### Problema: Maven build lento

```bash
# Build sem testes (só para desenvolvimento)
mvn clean install -DskipTests

# Build offline (se dependências já baixadas)
mvn clean install -o
```

---

## 🧹 Limpar Ambiente

### Parar Containers

```bash
cd infrastructure/docker
docker-compose down
```

### Parar e Remover Volumes (CUIDADO: apaga dados!)

```bash
docker-compose down -v
```

### Limpar Docker Completo

```bash
# Remover containers parados
docker container prune

# Remover imagens não usadas
docker image prune -a

# Remover volumes não usados
docker volume prune

# Limpar TUDO (CUIDADO!)
docker system prune -a --volumes
```

---

## 🎯 Próximos Passos

Após setup completo:

1. ✅ Implementar primeiro serviço (Product Service)
2. ✅ Configurar testes unitários + integração
3. ✅ Criar migrations (Flyway)
4. ✅ Implementar endpoints REST
5. ✅ Adicionar Swagger documentation
6. ✅ Integrar com RabbitMQ
7. ✅ Implementar outros serviços

---

## 📞 Suporte

**Issues**: Abra uma issue no GitHub
**Documentação**: Consulte `/docs`
**Steering Files**: Consulte `.sdd/steering/`

---

**Última atualização**: 2026-03-09
