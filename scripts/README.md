# Product Service API Test Scripts

Este diretório contém scripts abrangentes para testar todos os endpoints do Product Service.

## 📋 Arquivos Disponíveis

### 🔧 Scripts de Teste Automatizados

1. **`test-product-service.sh`** - Script Bash completo para Linux/macOS
2. **`test-product-service.ps1`** - Script PowerShell para Windows
3. **`quick-test.sh`** - Teste rápido dos endpoints principais
4. **`postman-collection.json`** - Coleção Postman/Insomnia

### 📊 Cenários de Teste Cobertos

#### ✅ **Testes de Funcionalidade**
- ✅ Health Check
- ✅ Criar produto (cenários válidos e inválidos)
- ✅ Buscar produto por ID
- ✅ Listar produtos com paginação e filtros
- ✅ Atualizar produto
- ✅ Operações de estoque (diminuir)
- ✅ Deletar produto (soft delete)

#### ✅ **Testes de Validação**
- ✅ Campos obrigatórios
- ✅ Validação de tipos de dados
- ✅ Validação de formato (UUID, SKU, etc.)
- ✅ Validação de valores (preços negativos, etc.)
- ✅ Validação de limites (tamanho de strings)

#### ✅ **Testes de Erro**
- ✅ Recursos não encontrados (404)
- ✅ Dados inválidos (400)
- ✅ Conflitos (409 - SKU duplicado)
- ✅ Estoque insuficiente
- ✅ Formatos inválidos

#### ✅ **Testes de Performance**
- ✅ Criação de múltiplos produtos
- ✅ Listagem com páginas grandes
- ✅ Requisições concorrentes

## 🚀 Como Usar

### Pré-requisitos

#### Para Scripts Bash (Linux/macOS)
```bash
# Instalar dependências
sudo apt-get install curl jq  # Ubuntu/Debian
brew install curl jq          # macOS
```

#### Para Scripts PowerShell (Windows)
```powershell
# PowerShell 5.1+ já inclui Invoke-RestMethod
# Opcional: Instalar jq para formatação JSON
choco install jq
```

### 1. Teste Completo (Bash)

```bash
# Tornar executável
chmod +x scripts/test-product-service.sh

# Executar com URL padrão (localhost:8081)
./scripts/test-product-service.sh

# Executar com URL customizada
./scripts/test-product-service.sh http://localhost:8081
```

### 2. Teste Completo (PowerShell)

```powershell
# Executar com URL padrão
.\scripts\test-product-service.ps1

# Executar com URL customizada
.\scripts\test-product-service.ps1 -BaseUrl "http://localhost:8081"
```

### 3. Teste Rápido

```bash
# Tornar executável
chmod +x scripts/quick-test.sh

# Executar teste rápido
./scripts/quick-test.sh
```

### 4. Postman/Insomnia

1. Importar o arquivo `postman-collection.json`
2. Configurar a variável `baseUrl` (padrão: http://localhost:8081)
3. Executar os testes individualmente ou em sequência

## 📊 Saída dos Testes

### Exemplo de Saída Bem-sucedida

```
=== PRODUCT SERVICE API TEST SUITE ===
Base URL: http://localhost:8081
API Base: http://localhost:8081/api/v1/products

[INFO] Waiting for Product Service to be ready...
[PASS] Service is ready!

=== HEALTH CHECK ===
[INFO] Testing: Health Check
[PASS] Status: 200 (Expected: 200)

=== CREATE PRODUCT TESTS ===
[INFO] Testing: Create Product - Valid Data
[PASS] Status: 201 (Expected: 201)
Created product ID: 550e8400-e29b-41d4-a716-446655440001

=== TEST SUMMARY ===
Total Tests: 45
[PASS] Passed: 45
[FAIL] Failed: 0
🎉 ALL TESTS PASSED!
```

### Logs Detalhados

Os scripts geram logs detalhados em `./test-results/test_TIMESTAMP.log` com:
- Timestamp de cada teste
- Requisições HTTP completas
- Respostas JSON formatadas
- Status codes e mensagens de erro

## 🔍 Estrutura dos Testes

### 1. Health Check
- Verifica se o serviço está rodando
- Endpoint: `GET /actuator/health`

### 2. Testes de Criação
```json
{
  "name": "iPhone 15 Pro",
  "description": "Latest iPhone with advanced features",
  "price": 999.99,
  "stockQuantity": 50,
  "sku": "IPHONE-15-PRO",
  "category": "ELECTRONICS"
}
```

### 3. Testes de Listagem
- Paginação: `?page=0&size=20`
- Filtros: `?category=ELECTRONICS&name=iPhone`
- Ordenação: `?sort=name,asc`

### 4. Testes de Validação
- Campos obrigatórios ausentes
- Valores inválidos (preços negativos)
- Formatos inválidos (SKU com caracteres especiais)

### 5. Testes de Erro
- IDs inexistentes
- UUIDs malformados
- Categorias inválidas
- Estoque insuficiente

## 🎯 Cenários de Teste Específicos

### Validação de SKU
```bash
# SKU válido: apenas alfanuméricos e hífens
"sku": "IPHONE-15-PRO"     ✅
"sku": "GALAXY_S24"        ❌ (underscore não permitido)
"sku": "PRODUCT@123"       ❌ (@ não permitido)
```

### Validação de Preço
```bash
"price": 0.01      ✅ (mínimo válido)
"price": 999.99    ✅ (valor normal)
"price": 0.00      ❌ (deve ser > 0)
"price": -10.00    ❌ (não pode ser negativo)
```

### Categorias Válidas
```bash
"ELECTRONICS"  ✅
"CLOTHING"     ✅
"FOOD"         ✅
"BOOKS"        ✅
"OTHER"        ✅
"INVALID"      ❌
```

## 🐛 Troubleshooting

### Serviço não está rodando
```bash
# Verificar se o serviço está ativo
curl -f http://localhost:8081/actuator/health

# Iniciar o serviço
cd services/product-service
mvn spring-boot:run
```

### Problemas de Permissão (Linux/macOS)
```bash
# Dar permissão de execução
chmod +x scripts/*.sh
```

### Problemas com jq
```bash
# Instalar jq
sudo apt-get install jq  # Ubuntu/Debian
brew install jq          # macOS
choco install jq         # Windows
```

### Porta em uso
```bash
# Verificar processo na porta 8081
netstat -tulpn | grep :8081
lsof -i :8081

# Matar processo se necessário
kill -9 <PID>
```

## 📈 Métricas de Teste

Os scripts testam:
- **45+ cenários** diferentes
- **6 endpoints** principais
- **4 métodos HTTP** (GET, POST, PUT, DELETE)
- **10+ validações** de entrada
- **8+ cenários de erro**
- **Performance** com múltiplas requisições

## 🔄 Integração Contínua

Para usar em CI/CD:

```yaml
# GitHub Actions example
- name: Test Product Service API
  run: |
    ./scripts/test-product-service.sh http://localhost:8081
    if [ $? -ne 0 ]; then
      echo "API tests failed"
      exit 1
    fi
```

## 📝 Contribuindo

Para adicionar novos testes:

1. Adicione o cenário em `test-product-service.sh`
2. Implemente a versão PowerShell em `test-product-service.ps1`
3. Adicione o endpoint na coleção Postman
4. Atualize esta documentação

## 🎉 Conclusão

Estes scripts fornecem cobertura completa de testes para o Product Service, garantindo que todos os endpoints funcionem corretamente em diferentes cenários e condições de erro.