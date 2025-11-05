# 📬 Collection de Exemplos de Requisições

Este arquivo contém exemplos práticos de todas as requisições da API para facilitar os testes.

## 🔐 Autenticação

### 1. Registrar Usuário ADMIN

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Administrador",
    "email": "admin@ecommerce.com",
    "password": "admin123",
    "role": "ADMIN"
  }'
```

### 2. Registrar Usuário USER

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "João Silva",
    "email": "joao@email.com",
    "password": "senha123",
    "role": "USER"
  }'
```

### 3. Login (ADMIN)

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@ecommerce.com",
    "password": "admin123"
  }'
```

**Resposta:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Administrador",
  "email": "admin@ecommerce.com",
  "role": "ADMIN"
}
```

## 📦 Produtos

### 4. Criar Produtos (ADMIN)

```bash
# Guardar o token do admin
export ADMIN_TOKEN="SEU_TOKEN_AQUI"

# Produto 1: Notebook
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{
    "nome": "Notebook Dell Inspiron 15",
    "descricao": "Notebook com processador Intel i7, 16GB RAM, SSD 512GB",
    "preco": 3499.90,
    "categoria": "Eletrônicos",
    "quantidadeEstoque": 10
  }'

# Produto 2: Mouse
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{
    "nome": "Mouse Logitech MX Master 3",
    "descricao": "Mouse sem fio ergonômico para produtividade",
    "preco": 549.90,
    "categoria": "Eletrônicos",
    "quantidadeEstoque": 25
  }'

# Produto 3: Teclado
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{
    "nome": "Teclado Mecânico Keychron K2",
    "descricao": "Teclado mecânico wireless com switches Gateron",
    "preco": 799.90,
    "categoria": "Eletrônicos",
    "quantidadeEstoque": 15
  }'

# Produto 4: Livro
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{
    "nome": "Clean Code - Robert C. Martin",
    "descricao": "Guia essencial para escrever código limpo e sustentável",
    "preco": 89.90,
    "categoria": "Livros",
    "quantidadeEstoque": 50
  }'

# Produto 5: Camiseta
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{
    "nome": "Camiseta Básica Preta",
    "descricao": "Camiseta 100% algodão, tamanho M",
    "preco": 49.90,
    "categoria": "Roupas",
    "quantidadeEstoque": 100
  }'
```

### 5. Listar Todos os Produtos (Público)

```bash
curl http://localhost:8080/api/products
```

### 6. Buscar Produto por ID

```bash
# Substitua pelo ID real retornado na criação
export PRODUCT_ID="550e8400-e29b-41d4-a716-446655440001"

curl http://localhost:8080/api/products/$PRODUCT_ID
```

### 7. Buscar Produtos por Categoria

```bash
curl http://localhost:8080/api/products/category/Eletrônicos
```

### 8. Atualizar Produto (ADMIN)

```bash
curl -X PUT http://localhost:8080/api/products/$PRODUCT_ID \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{
    "nome": "Notebook Dell Inspiron 15 - ATUALIZADO",
    "descricao": "Notebook com processador Intel i7, 16GB RAM, SSD 512GB - Nova versão",
    "preco": 3299.90,
    "categoria": "Eletrônicos",
    "quantidadeEstoque": 20
  }'
```

### 9. Deletar Produto (ADMIN)

```bash
curl -X DELETE http://localhost:8080/api/products/$PRODUCT_ID \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

## 🛍️ Pedidos

### 10. Login como USER

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "joao@email.com",
    "password": "senha123"
  }'

# Guardar o token do usuário
export USER_TOKEN="SEU_TOKEN_USER_AQUI"
```

### 11. Criar Pedido

```bash
# Substitua pelos IDs reais dos produtos
export PRODUCT_1_ID="550e8400-e29b-41d4-a716-446655440001"
export PRODUCT_2_ID="550e8400-e29b-41d4-a716-446655440002"

curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $USER_TOKEN" \
  -d "{
    \"items\": [
      {
        \"productId\": \"$PRODUCT_1_ID\",
        \"quantity\": 1
      },
      {
        \"productId\": \"$PRODUCT_2_ID\",
        \"quantity\": 2
      }
    ]
  }"
```

**Resposta:**
```json
{
  "id": "660e8400-e29b-41d4-a716-446655440003",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "userName": "João Silva",
  "status": "PENDENTE",
  "totalValue": 4599.70,
  "items": [
    {
      "id": "770e8400-e29b-41d4-a716-446655440004",
      "productId": "550e8400-e29b-41d4-a716-446655440001",
      "productName": "Notebook Dell Inspiron 15",
      "quantity": 1,
      "unitPrice": 3499.90,
      "subtotal": 3499.90
    },
    {
      "id": "770e8400-e29b-41d4-a716-446655440005",
      "productId": "550e8400-e29b-41d4-a716-446655440002",
      "productName": "Mouse Logitech MX Master 3",
      "quantity": 2,
      "unitPrice": 549.90,
      "subtotal": 1099.80
    }
  ],
  "createdAt": "2024-11-04T10:30:00",
  "updatedAt": "2024-11-04T10:30:00",
  "paidAt": null
}
```

### 12. Listar Meus Pedidos

```bash
curl http://localhost:8080/api/orders/my-orders \
  -H "Authorization: Bearer $USER_TOKEN"
```

### 13. Buscar Pedido por ID

```bash
export ORDER_ID="660e8400-e29b-41d4-a716-446655440003"

curl http://localhost:8080/api/orders/$ORDER_ID \
  -H "Authorization: Bearer $USER_TOKEN"
```

### 14. Processar Pagamento

```bash
curl -X POST http://localhost:8080/api/orders/$ORDER_ID/payment \
  -H "Authorization: Bearer $USER_TOKEN"
```

**Resposta (Sucesso):**
```json
{
  "id": "660e8400-e29b-41d4-a716-446655440003",
  "status": "PAGO",
  "totalValue": 4599.70,
  "paidAt": "2024-11-04T10:35:00",
  ...
}
```

**Resposta (Estoque Insuficiente - Pedido Cancelado):**
```json
{
  "timestamp": "2024-11-04T10:35:00",
  "status": 400,
  "error": "Insufficient Stock",
  "message": "Pedido cancelado: Estoque insuficiente para o produto 'Notebook Dell Inspiron 15'. Disponível: 0, Necessário: 1"
}
```

### 15. Cancelar Pedido

```bash
curl -X POST http://localhost:8080/api/orders/$ORDER_ID/cancel \
  -H "Authorization: Bearer $USER_TOKEN"
```

## 📊 Relatórios (ADMIN)

### 16. Top 5 Compradores

```bash
curl http://localhost:8080/api/reports/top-buyers \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

**Resposta:**
```json
[
  {
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "userName": "João Silva",
    "userEmail": "joao@email.com",
    "totalOrders": 5,
    "totalSpent": 15000.50
  },
  ...
]
```

### 17. Ticket Médio por Usuário

```bash
curl http://localhost:8080/api/reports/average-ticket \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

**Resposta:**
```json
[
  {
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "userName": "João Silva",
    "userEmail": "joao@email.com",
    "totalOrders": 5,
    "averageTicket": 3000.10,
    "totalSpent": 15000.50
  },
  ...
]
```

### 18. Faturamento do Mês Atual

```bash
curl http://localhost:8080/api/reports/revenue/current-month \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

**Resposta:**
```json
{
  "period": "Mês atual",
  "totalRevenue": 45678.90
}
```

### 19. Faturamento por Mês Específico

```bash
curl "http://localhost:8080/api/reports/revenue/month?year=2024&month=10" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

**Resposta:**
```json
{
  "year": 2024,
  "month": 10,
  "totalRevenue": 32456.78
}
```

### 20. Faturamento por Período

```bash
curl "http://localhost:8080/api/reports/revenue/period?startDate=2024-01-01T00:00:00&endDate=2024-12-31T23:59:59" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

**Resposta:**
```json
{
  "startDate": "2024-01-01T00:00:00",
  "endDate": "2024-12-31T23:59:59",
  "totalRevenue": 500000.00
}
```

## 🧪 Cenário Completo de Teste

```bash
#!/bin/bash

echo "=== Iniciando Testes da API E-Commerce ==="

# 1. Registrar Admin
echo -e "\n1. Registrando usuário ADMIN..."
ADMIN_RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Admin Teste",
    "email": "admin@teste.com",
    "password": "admin123",
    "role": "ADMIN"
  }')
echo $ADMIN_RESPONSE | jq

# Extrair token do admin
ADMIN_TOKEN=$(echo $ADMIN_RESPONSE | jq -r '.token')
echo "Token Admin: $ADMIN_TOKEN"

# 2. Criar produtos
echo -e "\n2. Criando produtos..."
PRODUCT1=$(curl -s -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{
    "nome": "Produto Teste 1",
    "descricao": "Descrição do produto 1",
    "preco": 100.00,
    "categoria": "Teste",
    "quantidadeEstoque": 10
  }')
echo $PRODUCT1 | jq

PRODUCT1_ID=$(echo $PRODUCT1 | jq -r '.id')
echo "Product 1 ID: $PRODUCT1_ID"

# 3. Registrar User
echo -e "\n3. Registrando usuário USER..."
USER_RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "User Teste",
    "email": "user@teste.com",
    "password": "user123",
    "role": "USER"
  }')
echo $USER_RESPONSE | jq

USER_TOKEN=$(echo $USER_RESPONSE | jq -r '.token')
echo "Token User: $USER_TOKEN"

# 4. Criar pedido
echo -e "\n4. Criando pedido..."
ORDER=$(curl -s -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $USER_TOKEN" \
  -d "{
    \"items\": [
      {
        \"productId\": \"$PRODUCT1_ID\",
        \"quantity\": 2
      }
    ]
  }")
echo $ORDER | jq

ORDER_ID=$(echo $ORDER | jq -r '.id')
echo "Order ID: $ORDER_ID"

# 5. Processar pagamento
echo -e "\n5. Processando pagamento..."
PAYMENT=$(curl -s -X POST http://localhost:8080/api/orders/$ORDER_ID/payment \
  -H "Authorization: Bearer $USER_TOKEN")
echo $PAYMENT | jq

# 6. Verificar relatórios
echo -e "\n6. Verificando Top 5 Compradores..."
curl -s http://localhost:8080/api/reports/top-buyers \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq

echo -e "\n7. Verificando Ticket Médio..."
curl -s http://localhost:8080/api/reports/average-ticket \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq

echo -e "\n8. Verificando Faturamento do Mês..."
curl -s http://localhost:8080/api/reports/revenue/current-month \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq

echo -e "\n=== Testes Concluídos ==="
```

## 📝 Notas Importantes

1. **Tokens JWT**: Válidos por 24 horas após o login
2. **UUIDs**: Use os IDs reais retornados pela API
3. **Estoque**: Sempre validado na criação e no pagamento do pedido
4. **Status**: Pedidos iniciam como PENDENTE e só podem ser pagos uma vez
5. **Permissões**: 
   - USER pode: criar pedidos, visualizar produtos
   - ADMIN pode: tudo que USER pode + gerenciar produtos + acessar relatórios

## 🐛 Tratamento de Erros

### Erro 401 - Não Autorizado
```json
{
  "timestamp": "2024-11-04T10:00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Credenciais inválidas"
}
```

### Erro 403 - Acesso Negado
```json
{
  "timestamp": "2024-11-04T10:00:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Acesso negado"
}
```

### Erro 404 - Não Encontrado
```json
{
  "timestamp": "2024-11-04T10:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Produto não encontrado com ID: 123"
}
```

### Erro 400 - Validação
```json
{
  "timestamp": "2024-11-04T10:00:00",
  "status": 400,
  "error": "Validation Failed",
  "errors": {
    "nome": "Nome é obrigatório",
    "preco": "Preço deve ser maior que zero"
  }
}
```
