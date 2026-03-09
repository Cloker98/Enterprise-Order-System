# 🚀 Configurar GitHub - Passo a Passo

## 1️⃣ Criar Repositório no GitHub

1. Acesse: https://github.com/new

2. Preencha:
   - **Repository name**: `enterprise-order-system`
   - **Description**: `Enterprise Order Management System - Microsserviços Java Spring Boot (DDD + Hexagonal Architecture + TDD)`
   - **Visibility**: ✅ **Public** (para mostrar no portfólio/currículo)
   - **NÃO** marque: ❌ "Add a README file"
   - **NÃO** marque: ❌ "Add .gitignore"
   - **NÃO** marque: ❌ "Choose a license"

3. Clique **Create repository**

---

## 2️⃣ Conectar Repositório Local com GitHub

### No Terminal (dentro da pasta do projeto):

```bash
# 1. Verificar se está no diretório correto
pwd
# Esperado: /c/Users/PC/Documents/Zeu/Algotrading/Automation/github/enterprise-order-system

# 2. Adicionar remote (SUBSTITUA SEU_USUARIO pelo seu username do GitHub)
git remote add origin https://github.com/SEU_USUARIO/enterprise-order-system.git

# Exemplo:
# git remote add origin https://github.com/joaosilva/enterprise-order-system.git

# 3. Verificar se foi adicionado
git remote -v

# Esperado:
# origin  https://github.com/SEU_USUARIO/enterprise-order-system.git (fetch)
# origin  https://github.com/SEU_USUARIO/enterprise-order-system.git (push)

# 4. Renomear branch para main (se necessário)
git branch -M main

# 5. Push inicial (primeira vez)
git push -u origin main
```

### Se pedir autenticação:

#### Opção 1: Personal Access Token (Recomendado)

1. No GitHub, vá em: **Settings** → **Developer settings** → **Personal access tokens** → **Tokens (classic)**
2. Clique **Generate new token (classic)**
3. Preencha:
   - **Note**: `enterprise-order-system-dev`
   - **Expiration**: `90 days` (ou mais)
   - **Scopes**: ✅ `repo` (todos os sub-checkboxes)
4. Clique **Generate token**
5. **COPIE O TOKEN** (você não vai ver novamente!)
6. Quando pedir senha no git push, cole o TOKEN (não a senha do GitHub)

#### Opção 2: GitHub CLI (Alternativa)

```bash
# Instalar GitHub CLI
winget install --id GitHub.cli

# Autenticar
gh auth login

# Seguir instruções no terminal
```

---

## 3️⃣ Adicionar Badge ao README

Após o push, adicione badges no `README.md`:

```markdown
# Enterprise Order Management System

![Java](https://img.shields.io/badge/Java-17-orange?logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen?logo=springboot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?logo=postgresql)
![Redis](https://img.shields.io/badge/Redis-7-red?logo=redis)
![Docker](https://img.shields.io/badge/Docker-Compose-blue?logo=docker)
![License](https://img.shields.io/badge/License-Private-lightgrey)

> Sistema de gerenciamento de pedidos empresarial com arquitetura de microsserviços

[Ver código](https://github.com/SEU_USUARIO/enterprise-order-system)
```

---

## 4️⃣ Verificar no GitHub

1. Acesse: https://github.com/SEU_USUARIO/enterprise-order-system
2. Você deve ver:
   - ✅ 5 commits
   - ✅ Estrutura de pastas
   - ✅ README.md renderizado
   - ✅ CLAUDE.md, SETUP_GUIDE.md

---

## 5️⃣ Comandos Git Úteis (Daqui pra Frente)

```bash
# Ver status
git status

# Ver commits
git log --oneline --graph -10

# Criar nova branch para feature
git checkout -b feature/product-service-implementation

# Adicionar arquivos
git add .

# Commit
git commit -m "feat(product): implementar Product aggregate"

# Push
git push

# Se primeira vez na branch:
git push -u origin feature/product-service-implementation

# Voltar para main
git checkout main

# Pull (atualizar do GitHub)
git pull origin main
```

---

## 6️⃣ Fluxo de Trabalho (Gitflow)

```bash
# 1. Criar branch de feature
git checkout -b feature/nome-da-feature

# 2. Desenvolver e commitar
git add .
git commit -m "feat: implementar X"

# 3. Push da branch
git push -u origin feature/nome-da-feature

# 4. No GitHub: Criar Pull Request
# - Base: main
# - Compare: feature/nome-da-feature

# 5. Após aprovação: Merge no GitHub

# 6. Atualizar main local
git checkout main
git pull origin main

# 7. Deletar branch local (opcional)
git branch -d feature/nome-da-feature
```

---

## 🎯 Próximo Passo

Após configurar GitHub:

```bash
# 1. Validar setup local
scripts\validate-setup.bat

# 2. Se tudo OK, instalar dependências faltantes (ver SETUP_GUIDE.md)

# 3. Voltar para desenvolvimento
# Próxima task: Implementar Product aggregate (TDD)
```

---

**Dúvidas?** Consulte: https://docs.github.com/en/get-started
