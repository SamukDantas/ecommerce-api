# 🛒 E-Commerce API

Sistema de gerenciamento de pedidos e produtos para e-commerce, desenvolvido com Spring Boot, MySQL e autenticação JWT.

## 📋 Índice

- [Visão Geral](#-visão-geral)
- [Tecnologias](#-tecnologias)
- [Arquitetura e Design Patterns](#-arquitetura-e-design-patterns)
- [Princípios SOLID](#-princípios-solid)
- [Funcionalidades](#-funcionalidades)
- [Requisitos](#-requisitos)
- [Instalação e Execução](#-instalação-e-execução)
- [Endpoints da API](#-endpoints-da-api)
- [Regras de Negócio](#-regras-de-negócio)
- [Queries Otimizadas](#-queries-otimizadas)
- [Testes](#-testes)
- [Estrutura do Projeto](#-estrutura-do-projeto)

## 🎯 Visão Geral

Este projeto implementa uma API REST completa para um sistema de e-commerce, com foco em:

- **Segurança**: Autenticação JWT com controle de acesso baseado em roles
- **Regras de Negócio**: Gerenciamento inteligente de estoque e pedidos
- **Performance**: Queries otimizadas com Spring Data JPA
- **Qualidade**: Código limpo seguindo princípios SOLID e design patterns

## 🚀 Tecnologias

- **Java 17**
- **Spring Boot 3.2.0**
  - Spring Web
  - Spring Data JPA
  - Spring Security
  - Spring Validation
- **MySQL 8.0**
- **JWT (JSON Web Tokens)** - io.jsonwebtoken 0.12.3
- **Lombok** - Redução de boilerplate
- **Springdoc OpenAPI** - Documentação Swagger
- **Docker & Docker Compose**
- **Maven**

## 🏗️ Arquitetura e Design Patterns

### Arquitetura em Camadas

O projeto segue uma arquitetura em camadas bem definida:

```
┌─────────────────────────────────────┐
│         Controller Layer            │  ← Recebe requisições HTTP
├─────────────────────────────────────┤
│          Service Layer              │  ← Lógica de negócio
├─────────────────────────────────────┤
│        Repository Layer             │  ← Acesso a dados
├─────────────────────────────────────┤
│         Database (MySQL)            │  ← Persistência
└─────────────────────────────────────┘
```

### Design Patterns Implementados

#### 1. **Singleton Pattern**
- **Onde**: Services, Repositories, Configurations
- **Como**: Gerenciado pelo Spring Container através de injeção de dependência
- **Benefício**: Uma única instância compartilhada, reduzindo uso de memória

```java
@Service
@RequiredArgsConstructor
public class OrderService {
    // Spring garante que há apenas uma instância desta classe
}
```

#### 2. **Strategy Pattern**
- **Onde**: `UserDetailsService` para autenticação
- **Como**: Customização da estratégia de busca de usuários
- **Benefício**: Flexibilidade para mudar a estratégia de autenticação

```java
@Bean
public UserDetailsService userDetailsService() {
    return username -> userRepository.findByEmail(username)
            .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
}
```

#### 3. **Chain of Responsibility Pattern**
- **Onde**: `JwtAuthenticationFilter` no Spring Security
- **Como**: Filtros encadeados processam requisições sequencialmente
- **Benefício**: Separação de responsabilidades na validação de autenticação

```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(...) {
        // Processa e passa para o próximo filtro na cadeia
        filterChain.doFilter(request, response);
    }
}
```

#### 4. **Builder Pattern**
- **Onde**: Entities e DTOs
- **Como**: Uso de `@Builder` do Lombok
- **Benefício**: Construção fluida e legível de objetos complexos

```java
Usuario usuario = Usuario.builder()
        .nome("João")
        .email("joao@email.com")
        .papel(Papel.USUARIO)
        .build();
```

#### 5. **DTO Pattern (Data Transfer Object)**
- **Onde**: Camada de comunicação entre Controller e Service
- **Como**: Classes específicas para transferência de dados
- **Benefício**: Desacopla a API dos modelos internos, controla exposição de dados

```java
public class RequisicaoProduto { ... }   // Entrada
public class RespostaProduto { ... }     // Saída
```

#### 6. **Repository Pattern**
- **Onde**: Camada de acesso a dados
- **Como**: Interfaces que estendem `JpaRepository`
- **Benefício**: Abstração do acesso a dados, facilita testes

```java
@Repository
public interface ProdutoRepositorio extends JpaRepository<Produto, UUID> {
    List<Produto> findByCategoria(String categoria);
}
```

#### 7. **Dependency Injection Pattern**
- **Onde**: Em toda a aplicação
- **Como**: `@RequiredArgsConstructor` do Lombok + Spring
- **Benefício**: Baixo acoplamento, facilita testes e manutenção

```java
@Service
@RequiredArgsConstructor
public class ServicoPedido {
    private final PedidoRepositorio pedidoRepositorio;  // Injetado automaticamente
    private final ProdutoRepositorio produtoRepositorio;
}
```

#### 8. **Facade Pattern**
- **Onde**: Services
- **Como**: Services fornecem interface simplificada para operações complexas
- **Benefício**: Simplifica uso de subsistemas complexos

```java
// ServicoPedido esconde a complexidade de validação de estoque,
// cálculo de totais, e persistência
public RespostaPedido criarPedido(RequisicaoCriarPedido requisicao, Usuario usuario) {
    // Lógica complexa simplificada em um único método
}
```

#### 9. **Template Method Pattern**
- **Onde**: Entidades com `@PrePersist` e `@PreUpdate`
- **Como**: Callbacks do JPA para operações antes de persistir
- **Benefício**: Comportamento padronizado para auditoria

```java
@PrePersist
protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
}
```

## 📐 Princípios SOLID

### S - Single Responsibility Principle (Princípio da Responsabilidade Única)
Cada classe tem uma única responsabilidade:
- `ServicoPedido`: Gerencia apenas a lógica de pedidos
- `ServicoProduto`: Gerencia apenas a lógica de produtos
- `ServicoAutenticacao`: Gerencia apenas autenticação

### O - Open/Closed Principle (Princípio Aberto/Fechado)
Classes abertas para extensão, fechadas para modificação:
- Novos tipos de autenticação podem ser adicionados sem modificar código existente
- Novas estratégias de cálculo podem ser implementadas através de interfaces

### L - Liskov Substitution Principle (Princípio da Substituição de Liskov)
Subtipos podem substituir tipos base:
- `Usuario implements UserDetails` - pode ser usado onde `UserDetails` é esperado
- Repositórios implementam interfaces padrão do Spring Data

### I - Interface Segregation Principle (Princípio da Segregação de Interface)
Interfaces específicas ao invés de genéricas:
- DTOs separados para Request e Response
- Interfaces de repositório com métodos específicos

### D - Dependency Inversion Principle (Princípio da Inversão de Dependência)
Depender de abstrações, não de implementações:
- Services dependem de interfaces de Repository, não de implementações concretas
- Uso de `UserDetailsService` ao invés de implementação direta

## ✨ Funcionalidades

### 🔐 Autenticação e Autorização
- ✅ Registro de usuários com senha criptografada (BCrypt)
- ✅ Login com geração de token JWT
- ✅ Dois perfis de acesso:
    - **ADMIN**: Gerenciar produtos (CRUD completo)
    - **USUARIO**: Criar pedidos e visualizar produtos

### 📦 Gerenciamento de Produtos
- ✅ CRUD completo (Create, Read, Update, Delete)
- ✅ Campos: ID (UUID), Nome, Descrição, Preço, Categoria, Quantidade em Estoque
- ✅ Timestamps automáticos (criadoEm, atualizadoEm)
- ✅ Busca por categoria
- ✅ Apenas ADMIN pode criar, atualizar e deletar
- ✅ Visualização pública (GET)

### 🛍️ Gerenciamento de Pedidos
- ✅ Criação de pedidos com múltiplos produtos
- ✅ Status do pedido: PENDENTE → PAGO / CANCELADO
- ✅ Validação de estoque na criação e pagamento
- ✅ Cálculo dinâmico do valor total baseado no preço atual
- ✅ Atualização de estoque apenas após pagamento
- ✅ Cancelamento automático se estoque insuficiente
- ✅ Listagem de pedidos do usuário autenticado

### 📊 Relatórios e Análises
- ✅ Top 5 usuários que mais compraram
- ✅ Ticket médio por usuário
- ✅ Valor total faturado no mês
- ✅ Valor faturado por período customizado

## 📋 Requisitos

- Java 17 ou superior
- Docker e Docker Compose
- Maven (opcional, já incluído no Docker)

## 🔧 Instalação e Execução

### Opção 1: Docker com Script Automatizado (Windows - RECOMENDADO)

```bash
# 1. Clone o repositório
git clone <url-do-repositorio>
cd ecommerce-api

# 2. Execute o script de inicialização
start.bat

# O script irá:
# - Verificar se Docker está rodando
# - Detectar conflitos de porta automaticamente
# - Iniciar os containers
# - Mostrar os endpoints disponíveis
```

### Opção 2: Teste Rápido da API (Windows)

```bash
# Após iniciar os containers, teste a API:
test.bat

# O script irá:
# - Verificar se API está respondendo
# - Criar usuário ADMIN e USER
# - Criar um produto de teste
# - Listar os produtos
```

### Opção 3: Execução Local (Sem Docker)

```bash
# 1. Certifique-se de ter MySQL rodando localmente
# Configure as credenciais em application.properties

# 2. Compile e execute
./mvnw clean install
./mvnw spring-boot:run

# Ou usando Maven instalado
mvn clean install
mvn spring-boot:run
```

### Importar Dump do Banco de Dados

```bash
# Se quiser popular o banco com a estrutura
mysql -u root -p < database_dump.sql
```

### Para acessar o MySQL via container (Docker)
```bash
docker exec -it ecommerce-api-db-1 mysql -uroot -proot ecommerce

-- Ver todas as tabelas
SHOW TABLES;

-- Ver estrutura de uma tabela
DESCRIBE users;
DESCRIBE products;
DESCRIBE orders;
DESCRIBE order_items;

-- Ver todos os usuários
SELECT * FROM users;

-- Ver todos os produtos
SELECT * FROM products;

-- Ver todos os pedidos com itens
SELECT o.id, o.status, o.total_value, u.name as user_name
FROM orders o
JOIN users u ON o.user_id = u.id;

-- Ver produtos mais vendidos
SELECT p.nome, SUM(oi.quantity) as total_vendido
FROM products p
JOIN order_items oi ON p.id = oi.product_id
GROUP BY p.id, p.nome
ORDER BY total_vendido DESC;

-- Sair do MySQL
EXIT;
```

### Para acessar o MySQL via Adminer (Interface Web - Docker)
**Acesse no navegador:** http://localhost:8081
#### Credenciais de login:

- **Sistema**: MySQL/MariaDB
- **Servidor**: db
- **Usuário**: root
- **Senha**: root
- **Base de dados**: ecommerce

### Rebuild da aplicação no container, porém mantendo o banco de dados

```bash
# 1. Para e remove os containers
docker-compose down

# 2. Rebuild da imagem SEM cache (garante que tudo será recompilado)
docker-compose build --no-cache

# 3. Inicia os containers novamente
docker-compose up -d

# 4. Acompanha os logs para ver se subiu corretamente
docker-compose logs -f app
```

## 📡 Endpoints da API

A documentação completa está disponível via Swagger:
- **URL**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs

### Autenticação

#### Registrar Usuário
```http
POST /api/autenticacao/registrar
Content-Type: application/json

{
  "nome": "Samuel Dantas",
  "email": "samuel@email.com",
  "senha": "senha123",
  "papel": "USUARIO"  // ou "ADMIN"
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "samuel@email.com",
  "password": "senha123"
}

Resposta:
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "userId": "uuid",
  "name": "Samuel Dantas",
  "email": "samuel@email.com",
  "papel": "USUARIO"
}
```

### Produtos

#### Criar Produto (ADMIN apenas)
```http
POST /api/produtos
Authorization: Bearer {token}
Content-Type: application/json

{
  "nome": "Notebook Dell",
  "descricao": "Notebook i7 16GB RAM",
  "preco": 3499.90,
  "categoria": "Eletrônicos",
  "quantidadeEstoque": 10
}
```

#### Listar Todos os Produtos (Público)
```http
GET /api/produtos
```

#### Buscar Produto por ID (Público)
```http
GET /api/produtos/{id}
```

#### Buscar por Categoria (Público)
```http
GET /api/produtos/categoria/Eletrônicos
```

#### Atualizar Produto (ADMIN apenas)
```http
PUT /api/produtos/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "nome": "Notebook Dell Atualizado",
  "descricao": "Nova descrição",
  "preco": 3299.90,
  "categoria": "Eletrônicos",
  "quantidadeEstoque": 15
}
```

#### Deletar Produto (ADMIN apenas)
```http
DELETE /api/produtos/{id}
Authorization: Bearer {token}
```

### Pedidos

#### Criar Pedido
```http
POST /api/pedidos
Authorization: Bearer {token}
Content-Type: application/json

{
  "itens": [
    {
      "produtoId": "uuid-do-produto-1",
      "quantidade": 2
    },
    {
      "produtoId": "uuid-do-produto-2",
      "quantidade": 1
    }
  ]
}
```

#### Listar Meus Pedidos
```http
GET /api/pedidos/meus-pedidos
Authorization: Bearer {token}
```

#### Buscar Pedido por ID
```http
GET /api/pedidos/{id}
Authorization: Bearer {token}
```

#### Processar Pagamento
```http
POST /api/pedidos/{id}/pagamento
Authorization: Bearer {token}
```

#### Cancelar Pedido
```http
POST /api/pedidos/{id}/cancelar
Authorization: Bearer {token}
```

### Relatórios (ADMIN apenas)

#### Top 5 Compradores
```http
GET /api/relatorios/top-compradores
Authorization: Bearer {token}
```

#### Ticket Médio por Usuário
```http
GET /api/relatorios/ticket-medio
Authorization: Bearer {token}
```

#### Faturamento do Mês Atual
```http
GET /api/relatorios/receita/mes-atual
Authorization: Bearer {token}
```

#### Faturamento por Mês Específico
```http
GET /api/relatorios/receita/mes?ano=2024&mes=11
Authorization: Bearer {token}
```

#### Faturamento por Período
```http
GET /api/relatorios/receita/periodo?dataInicio=2024-01-01T00:00:00&dataFim=2024-12-31T23:59:59
Authorization: Bearer {token}
```

## 🎯 Regras de Negócio

### Pedidos

1. **Criação de Pedido**
   - Pedido inicia com status `PENDENTE`
   - Valida estoque disponível antes de criar
   - Captura o preço atual do produto no momento da criação
   - Calcula o valor total dinamicamente
   - **NÃO atualiza o estoque** (apenas reserva)

2. **Pagamento de Pedido**
   - Apenas pedidos `PENDENTE` podem ser pagos
   - Valida estoque novamente no momento do pagamento
   - **Atualiza o estoque** apenas após confirmação de pagamento
   - Se estoque insuficiente, cancela automaticamente
   - Muda status para `PAGO` e registra data de pagamento

3. **Cancelamento de Pedido**
   - Apenas pedidos `PENDENTE` podem ser cancelados
   - Usuário só pode cancelar seus próprios pedidos
   - Muda status para `CANCELADO`

### Estoque

- Validação em dois momentos: criação e pagamento
- Baixa do estoque apenas após pagamento confirmado
- Exceção lançada se estoque insuficiente

### Segurança

- Token JWT válido por 24 horas
- Senhas hasheadas com BCrypt
- Controle de acesso baseado em roles (ADMIN/USER)

## ⚡ Queries Otimizadas

### 1. Top 5 Compradores
```sql
SELECT u.id, u.nome, u.email,
       COUNT(p.id) as total_pedidos,
       SUM(p.valor_total) as total_gasto
FROM usuarios u
         JOIN pedidos p ON p.usuario_id = u.id
WHERE p.status = 'PAGO'
GROUP BY u.id, u.nome, u.email
ORDER BY COUNT(p.id) DESC
    LIMIT 5;
```

**Otimizações:**
- JOIN direto entre Usuario e Pedido
- GROUP BY para agregar dados por usuário
- Índices em `usuario_id` e `status`

### 2. Ticket Médio
```sql
SELECT u.id, u.nome, u.email,
       COUNT(p.id) as totalPedidos,
       AVG(p.valor_total) as ticketMedio
FROM usuarios u
         JOIN pedidos p ON p.usuario_id = u.id
WHERE p.status = 'PAGO'
GROUP BY u.id
ORDER BY AVG(p.valor_total) DESC
```

**Otimizações:**
- Função AVG agregada
- WHERE para filtrar apenas pedidos pagos

### 3. Faturamento Mensal
```sql
SELECT
    COALESCE(SUM(valor_total), 0) as faturamento_mes,
    COUNT(*) as total_pedidos
FROM pedidos
WHERE status = 'PAGO'
    AND YEAR(pago_em) = YEAR(CURRENT_DATE)
    AND MONTH(pago_em) = MONTH(CURRENT_DATE);
```

**Otimizações:**
- Funções YEAR e MONTH indexáveis
- COALESCE para evitar NULL
- Índice em `pago_em`

### 4. Evitando N+1 com FETCH JOIN
```sql
SELECT DISTINCT
    p.id as pedido_id,
    p.usuario_id,
    p.status,
    p.valor_total,
    p.criado_em,
    p.atualizado_em,
    p.pago_em,
    i.id as item_id,
    i.quantidade,
    i.preco_unitario,
    pr.id as produto_id,
    pr.nome as produto_nome,
    pr.preco as produto_preco,
    pr.categoria as produto_categoria
FROM pedidos p
         LEFT JOIN itens_pedido i ON p.id = i.pedido_id
         LEFT JOIN produtos pr ON i.produto_id = pr.id
WHERE p.usuario_id = (SELECT id FROM usuarios LIMIT 1)
ORDER BY p.criado_em DESC;
```

**Otimizações:**
- FETCH JOIN carrega relacionamentos em uma única query
- Elimina problema de N+1 queries

## 🧪 Testes

### Testando com cURL

```bash
# 1. Registrar um usuário ADMIN
curl -X POST http://localhost:8080/api/autenticacao/registrar \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Admin User",
    "email": "admin@email.com",
    "senha": "admin123",
    "papel": "ADMIN"
  }'

# 2. Fazer login e capturar o token
TOKEN=$(curl -X POST http://localhost:8080/api/autenticacao/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@email.com",
    "senha": "admin123"
  }' | jq -r '.token')

# 3. Criar um produto
curl -X POST http://localhost:8080/api/produtos \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "nome": "Produto Teste",
    "descricao": "Descrição do produto",
    "preco": 99.90,
    "categoria": "Testes",
    "quantidadeEstoque": 50
  }'

# 4. Listar produtos
curl http://localhost:8080/api/produtos

# 5. Registrar um usuário normal
curl -X POST http://localhost:8080/api/autenticacao/registrar \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Cliente Teste",
    "email": "cliente@email.com",
    "senha": "cliente123",
    "papel": "USUARIO"
  }'

# 6. Login como usuário normal
USER_TOKEN=$(curl -X POST http://localhost:8080/api/autenticacao/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "cliente@email.com",
    "senha": "cliente123"
  }' | jq -r '.token')

# 7. Criar um pedido
curl -X POST http://localhost:8080/api/pedidos \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $USER_TOKEN" \
  -d '{
    "itens": [
      {
        "produtoId": "COLE-AQUI-O-UUID-DO-PRODUTO",
        "quantidade": 2
      }
    ]
  }'
```

## 📁 Estrutura do Projeto

```
ecommerce-api-pt/
├── src/
│   ├── main/
│   │   ├── java/com/ecommerce/api/
│   │   │   ├── configuracao/        # Configurações (Segurança, Aplicação)
│   │   │   ├── controlador/         # Controladores REST
│   │   │   ├── dto/                 # Data Transfer Objects
│   │   │   ├── entidade/            # Entidades JPA
│   │   │   ├── enums/               # Enumerações (Papel, StatusPedido)
│   │   │   ├── excecao/             # Exceções customizadas
│   │   │   ├── repositorio/         # Repositórios JPA
│   │   │   ├── seguranca/           # JWT e filtros de segurança
│   │   │   ├── servico/             # Lógica de negócio
│   │   │   └── AplicacaoEcommerceApi.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/                        # Testes unitários e integração
├── Dockerfile                       # Container da aplicação
├── docker-compose.yml              # Orquestração de containers
├── pom.xml                         # Dependências Maven
└── README.md                       # Este arquivo
```

## 🔒 Segurança

- **JWT**: Tokens com expiração de 24 horas
- **BCrypt**: Hash de senhas com salt automático
- **HTTPS**: Recomendado em produção
- **CORS**: Configurável para ambientes específicos
- **SQL Injection**: Prevenido por PreparedStatements do JPA
- **XSS**: Validação de entrada com Bean Validation

## 📈 Performance

### Otimizações Implementadas

1. **Connection Pooling**: HikariCP (padrão do Spring Boot)
2. **Lazy Loading**: Relacionamentos carregados apenas quando necessário
3. **FETCH JOIN**: Evita N+1 queries
4. **Índices**: Criados em colunas de busca frequente
5. **Batch Operations**: Configurado para inserções em lote
6. **DTOs**: Reduz carga de serialização

### Monitoramento

```bash
# Logs da aplicação
docker-compose logs -f app

# Métricas do MySQL
docker exec -it ecommerce-mysql mysql -u root -p -e "SHOW PROCESSLIST;"
```

## 🐛 Tratamento de Erros

### Erro 401 - Não Autorizado
```json
{
  "timestamp": "2024-11-05T10:00:00",
  "status": 401,
  "erro": "Unauthorized",
  "mensagem": "Credenciais inválidas"
}
```

### Erro 403 - Acesso Negado
```json
{
  "timestamp": "2024-11-05T10:00:00",
  "status": 403,
  "erro": "Forbidden",
  "mensagem": "Acesso negado"
}
```

### Erro 404 - Não Encontrado
```json
{
  "timestamp": "2024-11-05T10:00:00",
  "status": 404,
  "erro": "Not Found",
  "mensagem": "Produto não encontrado com ID: 123"
}
```

### Erro 400 - Validação
```json
{
  "timestamp": "2024-11-05T10:00:00",
  "status": 400,
  "erro": "Validation Failed",
  "erros": {
    "nome": "Nome é obrigatório",
    "preco": "Preço deve ser maior que zero"
  }
}
```

## 📝 Notas Importantes

1. **Tokens JWT**: Válidos por 24 horas após o login
2. **UUIDs**: Use os IDs reais retornados pela API
3. **Estoque**: Sempre validado na criação e no pagamento do pedido
4. **Status**: Pedidos iniciam como PENDENTE e só podem ser pagos uma vez
5. **Permissões**:
    - USUARIO pode: criar pedidos, visualizar produtos
    - ADMIN pode: tudo que USUARIO pode + gerenciar produtos + acessar relatórios
6. **Nomenclatura**: Todo o código utiliza termos em português para facilitar o entendimento

## 👤 Desenvolvedor

**Samuel Dantas**
- Email: samueldantasbarbosa@hotmail.com

---