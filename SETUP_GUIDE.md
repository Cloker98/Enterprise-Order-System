# 🚀 Guia Completo de Setup - Enterprise Order System

> **Pré-requisitos** para rodar o projeto localmente (Windows)

---

## 📋 Checklist de Instalação

- [ ] Java 17 (JDK)
- [ ] Maven 3.8+
- [ ] Docker Desktop
- [ ] Git (já instalado ✅)
- [ ] IDE (opcional: IntelliJ IDEA ou VS Code)

---

## 1️⃣ **Java 17 (JDK) - OBRIGATÓRIO**

### Download

**Opção 1: Eclipse Temurin (Recomendado)**
- Link: https://adoptium.net/temurin/releases/
- Selecione:
  - **Version**: 17 (LTS)
  - **Operating System**: Windows
  - **Architecture**: x64
  - **Package Type**: JDK (não JRE)
- Baixe o **MSI installer** (mais fácil)

**Opção 2: Oracle JDK**
- Link: https://www.oracle.com/java/technologies/downloads/#java17
- Baixe: **Windows x64 Installer**

### Instalação

1. Execute o instalador `.msi`
2. **IMPORTANTE**: Durante instalação, marque opção **"Set JAVA_HOME variable"**
3. Pasta padrão: `C:\Program Files\Eclipse Adoptium\jdk-17.x.x-hotspot\`

### Configurar PATH (se não foi automático)

1. Abra: **Painel de Controle** → **Sistema** → **Configurações avançadas do sistema**
2. Clique em **Variáveis de Ambiente**
3. Em **Variáveis do sistema**, clique em **Novo**:
   - **Nome**: `JAVA_HOME`
   - **Valor**: `C:\Program Files\Eclipse Adoptium\jdk-17.0.10.7-hotspot` (ajuste versão)
4. Edite a variável **Path** (em Variáveis do sistema):
   - Clique em **Novo**
   - Adicione: `%JAVA_HOME%\bin`
5. Clique **OK** em todas as janelas

### Validar Instalação

```bash
# Fechar e reabrir o terminal
java -version
# Esperado: openjdk version "17.0.x"

javac -version
# Esperado: javac 17.0.x

echo %JAVA_HOME%
# Esperado: C:\Program Files\Eclipse Adoptium\jdk-17.0.10.7-hotspot
```

---

## 2️⃣ **Maven 3.8+ - OBRIGATÓRIO**

### Download

- Link: https://maven.apache.org/download.cgi
- Baixe: **apache-maven-3.9.6-bin.zip** (Binary zip archive)

### Instalação

1. Extraia o ZIP para: `C:\Program Files\Apache\maven\`
2. Caminho final: `C:\Program Files\Apache\maven\apache-maven-3.9.6\`

### Configurar PATH

1. Abra: **Variáveis de Ambiente** (mesmo processo do Java)
2. Em **Variáveis do sistema**, clique em **Novo**:
   - **Nome**: `MAVEN_HOME`
   - **Valor**: `C:\Program Files\Apache\maven\apache-maven-3.9.6`
3. Edite a variável **Path**:
   - Adicione: `%MAVEN_HOME%\bin`
4. Clique **OK**

### Validar Instalação

```bash
# Fechar e reabrir o terminal
mvn -version
# Esperado:
# Apache Maven 3.9.6
# Maven home: C:\Program Files\Apache\maven\apache-maven-3.9.6
# Java version: 17.0.x
```

---

## 3️⃣ **Docker Desktop - OBRIGATÓRIO**

### Download

- Link: https://www.docker.com/products/docker-desktop/
- Baixe: **Docker Desktop for Windows**

### Instalação

1. Execute o instalador
2. **IMPORTANTE**: Durante instalação, marque:
   - ✅ Use WSL 2 instead of Hyper-V (recomendado)
   - ✅ Add shortcut to desktop
3. **Reinicie o computador** após instalação
4. Abra Docker Desktop
5. Aceite os termos de serviço
6. Aguarde até Docker estar "running" (ícone verde)

### Validar Instalação

```bash
docker --version
# Esperado: Docker version 24.x.x

docker-compose --version
# Esperado: Docker Compose version v2.x.x

# Testar se funciona
docker run hello-world
# Esperado: mensagem "Hello from Docker!"
```

---

## 4️⃣ **Git - JÁ INSTALADO ✅**

```bash
git --version
# Esperado: git version 2.x.x
```

---

## 5️⃣ **IDE (Opcional mas Recomendado)**

### Opção 1: IntelliJ IDEA Community (Recomendado para Java)

- Link: https://www.jetbrains.com/idea/download/
- Baixe: **IntelliJ IDEA Community Edition** (grátis)
- Durante instalação, marque:
  - ✅ Create Desktop Shortcut
  - ✅ Update PATH variable
  - ✅ .java association
  - ✅ Add "Open Folder as Project"

**Plugins necessários** (instalar após abrir):
- Lombok Plugin (já vem por padrão)
- MapStruct Support
- Docker
- Spring Boot

### Opção 2: VS Code (se preferir)

- Link: https://code.visualstudio.com/
- Extensões necessárias:
  - Extension Pack for Java (Microsoft)
  - Spring Boot Extension Pack (VMware)
  - Docker (Microsoft)
  - Lombok Annotations Support

---

## 6️⃣ **Configurar Repositório GitHub**

### Criar Repositório no GitHub

1. Acesse: https://github.com/new
2. Preencha:
   - **Repository name**: `enterprise-order-system`
   - **Description**: `Enterprise Order Management System - Java Spring Boot microservices for job application demo`
   - **Visibility**: ✅ Public (para mostrar no currículo)
   - **NÃO** marque: "Initialize with README" (já temos)
3. Clique **Create repository**

### Conectar Local com GitHub

```bash
# No terminal, dentro do projeto:
cd c:/Users/PC/Documents/Zeu/Algotrading/Automation/github/enterprise-order-system

# Adicionar remote (SUBSTITUA SEU_USUARIO pelo seu username do GitHub)
git remote add origin https://github.com/SEU_USUARIO/enterprise-order-system.git

# Verificar
git remote -v

# Push inicial
git branch -M main
git push -u origin main
```

---

## 7️⃣ **Validar Setup Completo**

Execute estes comandos para validar TUDO:

```bash
# Java
java -version
javac -version
echo %JAVA_HOME%

# Maven
mvn -version

# Docker
docker --version
docker-compose --version
docker ps

# Git
git --version
git remote -v

# Testar build do projeto
cd services/product-service
mvn clean compile
```

---

## 8️⃣ **Iniciar Infraestrutura Local**

```bash
# Subir PostgreSQL + Redis + RabbitMQ
cd infrastructure/docker
docker-compose up -d

# Verificar se containers estão rodando
docker ps

# Esperado:
# - product-postgres (porta 5432)
# - product-redis (porta 6379)
# - rabbitmq (portas 5672, 15672)
```

---

## 9️⃣ **Rodar Product Service**

```bash
cd services/product-service

# Build
mvn clean install

# Run
mvn spring-boot:run

# Ou com perfil específico
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Acessar:

- **API**: http://localhost:8081
- **Swagger**: http://localhost:8081/swagger-ui.html
- **Health**: http://localhost:8081/actuator/health

---

## 🐛 **Troubleshooting Comum**

### ❌ "mvn: command not found"

**Solução**:
1. Verificar se `MAVEN_HOME` está configurado
2. Verificar se `%MAVEN_HOME%\bin` está no PATH
3. **Fechar e reabrir o terminal**

### ❌ "JAVA_HOME is not set"

**Solução**:
1. Configurar variável `JAVA_HOME` apontando para JDK 17
2. Adicionar `%JAVA_HOME%\bin` ao PATH
3. **Fechar e reabrir o terminal**

### ❌ "Docker daemon is not running"

**Solução**:
1. Abrir Docker Desktop
2. Aguardar até ficar verde (running)
3. Testar: `docker ps`

### ❌ "Port 8081 already in use"

**Solução**:
```bash
# Windows - matar processo na porta 8081
netstat -ano | findstr :8081
taskkill /PID <PID> /F
```

### ❌ "Cannot connect to database"

**Solução**:
```bash
# Verificar se PostgreSQL está rodando
docker ps | findstr postgres

# Se não estiver, subir:
cd infrastructure/docker
docker-compose up -d postgres

# Verificar logs
docker logs product-postgres
```

---

## 📚 **Recursos Úteis**

### Documentação

- Java 17: https://docs.oracle.com/en/java/javase/17/
- Maven: https://maven.apache.org/guides/
- Spring Boot: https://docs.spring.io/spring-boot/docs/current/reference/html/
- Docker: https://docs.docker.com/

### Atalhos IDE (IntelliJ)

- `Ctrl + Shift + F10` - Run
- `Shift + F10` - Run last
- `Ctrl + F9` - Build project
- `Alt + Shift + F10` - Run tests
- `Ctrl + Shift + T` - Navigate to test

---

## ✅ **Checklist Final**

Antes de continuar desenvolvimento, garantir que:

- [ ] `java -version` retorna **17.0.x**
- [ ] `mvn -version` retorna **3.8+**
- [ ] `docker ps` mostra containers (postgres, redis)
- [ ] `git remote -v` mostra **origin** configurado
- [ ] `mvn clean compile` no product-service **compila sem erros**
- [ ] http://localhost:8081/actuator/health retorna **UP**

---

## 🚀 **Próximo Passo**

Após validar TODOS os itens acima, retornar para implementação seguindo SDD:

```bash
# Ver tasks pendentes
cat .sdd/specs/product-service/tasks.md

# Próxima task: TASK-005 (Implement Domain Layer - TDD)
```

---

**Última atualização**: 2026-03-09
**Autor**: Enterprise Team

---

**Dúvidas?** Consulte CLAUDE.md ou README.md de cada serviço.
