#!/bin/bash

# Enterprise Order Management System - Local Setup Script
# Este script configura o ambiente de desenvolvimento local

set -e

echo "🚀 Enterprise Order Management System - Setup Local"
echo "===================================================="
echo ""

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Check prerequisites
echo "📋 Verificando pré-requisitos..."

command -v java >/dev/null 2>&1 || {
    echo -e "${RED}❌ Java não encontrado. Instale Java 17+${NC}"
    exit 1
}

command -v mvn >/dev/null 2>&1 || {
    echo -e "${RED}❌ Maven não encontrado. Instale Maven 3.9+${NC}"
    exit 1
}

command -v docker >/dev/null 2>&1 || {
    echo -e "${RED}❌ Docker não encontrado. Instale Docker${NC}"
    exit 1
}

command -v docker-compose >/dev/null 2>&1 || {
    echo -e "${RED}❌ Docker Compose não encontrado. Instale Docker Compose${NC}"
    exit 1
}

echo -e "${GREEN}✅ Todos os pré-requisitos estão instalados${NC}"
echo ""

# Display versions
echo "📦 Versões instaladas:"
echo "  - Java: $(java -version 2>&1 | head -n 1)"
echo "  - Maven: $(mvn -version | head -n 1)"
echo "  - Docker: $(docker --version)"
echo "  - Docker Compose: $(docker-compose --version)"
echo ""

# Start infrastructure
echo "🐳 Iniciando infraestrutura (Docker Compose)..."
cd infrastructure/docker

echo "  - PostgreSQL (Product, Order services)"
echo "  - Oracle XE (Payment service)"
echo "  - MongoDB (Notification service)"
echo "  - Redis (Cache)"
echo "  - RabbitMQ (Message Broker)"
echo ""

docker-compose up -d

echo -e "${GREEN}✅ Infraestrutura iniciada com sucesso!${NC}"
echo ""

# Wait for services to be healthy
echo "⏳ Aguardando serviços ficarem prontos..."
echo "   (isso pode levar até 2 minutos, especialmente para o Oracle)"

sleep 5

# Check health
echo ""
echo "🏥 Verificando saúde dos serviços..."

# PostgreSQL
until docker exec eos-postgresql pg_isready -U admin > /dev/null 2>&1; do
    echo "  ⏳ Aguardando PostgreSQL..."
    sleep 2
done
echo -e "  ${GREEN}✅ PostgreSQL ready${NC}"

# Redis
until docker exec eos-redis redis-cli -a redis123 ping > /dev/null 2>&1; do
    echo "  ⏳ Aguardando Redis..."
    sleep 2
done
echo -e "  ${GREEN}✅ Redis ready${NC}"

# MongoDB
until docker exec eos-mongodb mongosh --eval "db.adminCommand('ping')" > /dev/null 2>&1; do
    echo "  ⏳ Aguardando MongoDB..."
    sleep 2
done
echo -e "  ${GREEN}✅ MongoDB ready${NC}"

# RabbitMQ
until docker exec eos-rabbitmq rabbitmq-diagnostics ping > /dev/null 2>&1; do
    echo "  ⏳ Aguardando RabbitMQ..."
    sleep 2
done
echo -e "  ${GREEN}✅ RabbitMQ ready${NC}"

# Oracle (pode demorar mais)
echo "  ⏳ Aguardando Oracle (pode levar 1-2 minutos)..."
until docker exec eos-oracle healthcheck.sh > /dev/null 2>&1; do
    sleep 5
done
echo -e "  ${GREEN}✅ Oracle ready${NC}"

echo ""
echo -e "${GREEN}✅ Todos os serviços estão prontos!${NC}"
echo ""

# Display connection info
echo "🔗 Informações de Conexão"
echo "========================"
echo ""
echo "📊 PostgreSQL:"
echo "   Host: localhost:5432"
echo "   Databases: product_db, order_db"
echo "   Username: admin"
echo "   Password: admin123"
echo "   JDBC URL: jdbc:postgresql://localhost:5432/product_db"
echo ""
echo "🗄️  Oracle:"
echo "   Host: localhost:1521"
echo "   SID: XE"
echo "   Database: paymentdb"
echo "   Username: payment_user"
echo "   Password: payment_pass"
echo "   JDBC URL: jdbc:oracle:thin:@localhost:1521/XE"
echo "   Enterprise Manager: http://localhost:5500/em"
echo ""
echo "🍃 MongoDB:"
echo "   Host: localhost:27017"
echo "   Database: notification_db"
echo "   Username: admin"
echo "   Password: admin123"
echo "   URI: mongodb://admin:admin123@localhost:27017/notification_db"
echo ""
echo "🔴 Redis:"
echo "   Host: localhost:6379"
echo "   Password: redis123"
echo "   CLI: docker exec -it eos-redis redis-cli -a redis123"
echo ""
echo "🐰 RabbitMQ:"
echo "   AMQP: localhost:5672"
echo "   Management UI: http://localhost:15672"
echo "   Username: admin"
echo "   Password: admin123"
echo ""
echo "🛠️  Ferramentas Web (opcional - use --profile tools):"
echo "   pgAdmin: http://localhost:5050 (admin@enterprise.com / admin123)"
echo "   Mongo Express: http://localhost:8081 (admin / admin123)"
echo ""

# Next steps
echo "📝 Próximos Passos"
echo "=================="
echo ""
echo "1. Build dos serviços:"
echo "   cd services/product-service && mvn clean install"
echo ""
echo "2. Executar um serviço:"
echo "   mvn spring-boot:run -Dspring-boot.run.profiles=local"
echo ""
echo "3. Acessar Swagger:"
echo "   http://localhost:8080/swagger-ui.html (API Gateway)"
echo "   http://localhost:8081/swagger-ui.html (Product Service)"
echo ""
echo "4. Parar infraestrutura:"
echo "   cd infrastructure/docker && docker-compose down"
echo ""
echo "5. Ver logs:"
echo "   docker-compose logs -f [service-name]"
echo ""

echo -e "${GREEN}✅ Setup concluído com sucesso!${NC}"
echo ""
echo "💡 Dica: Execute 'docker ps' para ver todos os containers rodando"
echo ""
