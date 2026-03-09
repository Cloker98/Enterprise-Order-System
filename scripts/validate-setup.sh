#!/bin/bash

# ===========================================================
# Script de Validação de Setup - Enterprise Order System
# ===========================================================
# Este script valida se todos os pré-requisitos estão instalados
# ===========================================================

set -e

echo "=========================================="
echo "🔍 Validando Setup do Ambiente"
echo "=========================================="
echo ""

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

SUCCESS=0
FAILURES=0

# Function to check command
check_command() {
    local cmd=$1
    local name=$2
    local expected=$3

    echo -n "Checking $name... "

    if command -v $cmd &> /dev/null; then
        local version=$($cmd --version 2>&1 | head -n 1)
        echo -e "${GREEN}✅ INSTALLED${NC}"
        echo "   └─ $version"
        ((SUCCESS++))
    else
        echo -e "${RED}❌ NOT FOUND${NC}"
        echo "   └─ Expected: $expected"
        ((FAILURES++))
    fi
    echo ""
}

# 1. Java
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "1. Java JDK"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
check_command "java" "Java Runtime" "17.0.x"
check_command "javac" "Java Compiler" "17.0.x"

if [ -n "$JAVA_HOME" ]; then
    echo -e "${GREEN}✅ JAVA_HOME is set${NC}"
    echo "   └─ $JAVA_HOME"
    ((SUCCESS++))
else
    echo -e "${RED}❌ JAVA_HOME is NOT set${NC}"
    echo "   └─ Set JAVA_HOME environment variable"
    ((FAILURES++))
fi
echo ""

# 2. Maven
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "2. Maven"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
check_command "mvn" "Maven" "3.8+"

# 3. Docker
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "3. Docker"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
check_command "docker" "Docker" "24.x+"
check_command "docker-compose" "Docker Compose" "2.x+"

echo -n "Checking Docker daemon... "
if docker ps &> /dev/null; then
    echo -e "${GREEN}✅ RUNNING${NC}"
    ((SUCCESS++))
else
    echo -e "${RED}❌ NOT RUNNING${NC}"
    echo "   └─ Start Docker Desktop"
    ((FAILURES++))
fi
echo ""

# 4. Git
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "4. Git"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
check_command "git" "Git" "2.x+"

echo -n "Checking Git remote... "
if git remote -v | grep -q "origin"; then
    echo -e "${GREEN}✅ CONFIGURED${NC}"
    git remote -v | sed 's/^/   └─ /'
    ((SUCCESS++))
else
    echo -e "${YELLOW}⚠️  NOT CONFIGURED${NC}"
    echo "   └─ Run: git remote add origin <your-repo-url>"
    ((FAILURES++))
fi
echo ""

# 5. Test Maven Build
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "5. Maven Build Test"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo -n "Testing Maven build... "

cd services/product-service 2>/dev/null || {
    echo -e "${RED}❌ FAILED${NC}"
    echo "   └─ Directory services/product-service not found"
    ((FAILURES++))
    cd ../..
}

if [ -f "pom.xml" ]; then
    if mvn clean compile -q &> /dev/null; then
        echo -e "${GREEN}✅ BUILD SUCCESS${NC}"
        ((SUCCESS++))
    else
        echo -e "${RED}❌ BUILD FAILED${NC}"
        echo "   └─ Run 'mvn clean compile' to see errors"
        ((FAILURES++))
    fi
else
    echo -e "${YELLOW}⚠️  SKIPPED${NC}"
    echo "   └─ pom.xml not found"
fi

cd ../.. 2>/dev/null || true
echo ""

# 6. Docker Containers
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "6. Docker Infrastructure"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

if docker ps &> /dev/null; then
    echo "Checking containers..."

    # PostgreSQL
    echo -n "  PostgreSQL (port 5432)... "
    if docker ps | grep -q "postgres"; then
        echo -e "${GREEN}✅ RUNNING${NC}"
        ((SUCCESS++))
    else
        echo -e "${YELLOW}⚠️  NOT RUNNING${NC}"
        echo "     └─ Run: cd infrastructure/docker && docker-compose up -d postgres"
        ((FAILURES++))
    fi

    # Redis
    echo -n "  Redis (port 6379)... "
    if docker ps | grep -q "redis"; then
        echo -e "${GREEN}✅ RUNNING${NC}"
        ((SUCCESS++))
    else
        echo -e "${YELLOW}⚠️  NOT RUNNING${NC}"
        echo "     └─ Run: cd infrastructure/docker && docker-compose up -d redis"
        ((FAILURES++))
    fi

    # RabbitMQ
    echo -n "  RabbitMQ (ports 5672, 15672)... "
    if docker ps | grep -q "rabbitmq"; then
        echo -e "${GREEN}✅ RUNNING${NC}"
        ((SUCCESS++))
    else
        echo -e "${YELLOW}⚠️  NOT RUNNING${NC}"
        echo "     └─ Run: cd infrastructure/docker && docker-compose up -d rabbitmq"
        ((FAILURES++))
    fi
fi
echo ""

# Summary
echo "=========================================="
echo "📊 Summary"
echo "=========================================="
echo -e "Success: ${GREEN}$SUCCESS${NC}"
echo -e "Failures: ${RED}$FAILURES${NC}"
echo ""

if [ $FAILURES -eq 0 ]; then
    echo -e "${GREEN}✅ ALL CHECKS PASSED!${NC}"
    echo ""
    echo "🚀 You're ready to start development!"
    echo ""
    echo "Next steps:"
    echo "  1. cd services/product-service"
    echo "  2. mvn clean install"
    echo "  3. mvn spring-boot:run"
    echo "  4. Open http://localhost:8081/swagger-ui.html"
    exit 0
else
    echo -e "${RED}❌ SOME CHECKS FAILED${NC}"
    echo ""
    echo "Please fix the issues above and run this script again."
    echo "See SETUP_GUIDE.md for detailed instructions."
    exit 1
fi
