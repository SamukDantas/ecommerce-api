# 🛒 E-Commerce API

Sistema de gerenciamento de pedidos e produtos para e-commerce, desenvolvido com Spring Boot, MySQL e autenticação JWT.

## 📋 Índice

- [Visão Geral](#-visão-geral)
- [Tecnologias](#-tecnologias)
- [Arquitetura e Design Patterns](#-arquitetura-e-design-patterns)
- [Princípios SOLID](#-princípios-solid)
- [Funcionalidades](#-funcionalidades)
- [Testes](#-testes)
- [Análise Estática de Código](#-análise-estática-de-código)
- [Requisitos](#-requisitos)
- [Instalação e Execução](#-instalação-e-execução)
- [Endpoints da API](#-endpoints-da-api)
- [Regras de Negócio](#-regras-de-negócio)
- [Queries Otimizadas](#-queries-otimizadas)
- [Estrutura do Projeto](#-estrutura-do-projeto)

## 🎯 Visão Geral

Este projeto implementa uma API REST completa para um sistema de e-commerce, com foco em:

- **Segurança**: Autenticação JWT com controle de acesso baseado em roles
- **Regras de Negócio**: Gerenciamento inteligente de estoque e pedidos
- **Performance**: Queries otimizadas com Spring Data JPA
- **Qualidade**: Código limpo seguindo princípios SOLID e design patterns
- **Testes**: Cobertura abrangente com testes unitários e de integração
- **Análise Estática**: Múltiplas ferramentas para garantir qualidade do código

## 🚀 Tecnologias

### Core
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

### Testes
- **JUnit 5** - Framework de testes moderno
- **Mockito 5.8.0** - Framework de mocking
- **AssertJ** - Assertions fluentes e legíveis
- **TestContainers 1.19.3** - Containers Docker para testes de integração
- **REST Assured** - Testes de API REST
- **H2 Database** - Banco em memória para testes

### Análise Estática
- **Checkstyle 10.12.5** - Verificação de estilo de código
- **SpotBugs 4.8.3** - Detecção de bugs potenciais
- **FindSecBugs** - Análise de vulnerabilidades de segurança
- **PMD 7.0.0** - Análise de código fonte
- **JaCoCo 0.8.11** - Cobertura de código
- **SonarQube** - Análise completa de qualidade (preparado)

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

## 🧪 Testes

### Visão Geral da Estratégia de Testes

O projeto implementa uma estratégia abrangente de testes seguindo a pirâmide de testes:

```
        /\
       /  \      E2E Tests (Futuros)
      /____\
     /      \    Integration Tests (TestContainers)
    /________\   ✅ 19+ testes
   /          \  Unit Tests (JUnit + Mockito)
  /____________\ ✅ 24+ testes
```

### Testes Unitários

**Total: 24+ testes implementados**

Os testes unitários cobrem a camada de serviço com isolamento completo usando mocks:

#### Características:
- ✅ **Padrão AAA** (Arrange, Act, Assert)
- ✅ **JUnit 5** - Framework moderno de testes
- ✅ **Mockito** - Mocking de dependências
- ✅ **AssertJ** - Assertions fluentes
- ✅ **Nomenclatura clara** - @DisplayName descritivo

#### Arquivos de Teste:
- `ServicoProdutoTest.java` - 11 testes unitários
  - Criação, busca, atualização e exclusão de produtos
  - Validação de exceções
  - Casos de borda (listas vazias, produtos inexistentes)

- `ServicoPedidoTest.java` - 13 testes unitários
  - Criação de pedidos com validação de estoque
  - Processamento de pagamento
  - Cancelamento de pedidos
  - Validação de permissões

#### Exemplo de Teste:
```java
@Test
@DisplayName("Deve criar produto com sucesso")
void deveCriarProdutoComSucesso() {
    // Arrange (Given)
    when(produtoRepositorio.save(any(Produto.class)))
        .thenReturn(produtoExemplo);

    // Act (When)
    RespostaProduto resposta = servicoProduto.criarProduto(requisicao);

    // Assert (Then)
    assertThat(resposta).isNotNull();
    assertThat(resposta.getNome()).isEqualTo("Notebook Dell");
    verify(produtoRepositorio, times(1)).save(any(Produto.class));
}
```

#### Executar Testes Unitários:
```bash
# Executar todos os testes unitários
mvn test

# Executar teste específico
mvn test -Dtest=ServicoProdutoTest

# Com relatório de cobertura
mvn test jacoco:report
```

### Testes de Integração (TestContainers)

**Total: 19+ testes de integração**

Os testes de integração validam o comportamento completo da aplicação usando containers Docker reais:

#### Características:
- ✅ **Container MySQL 8.0** - Banco de dados real
- ✅ **TestContainers** - Gerenciamento automático de containers
- ✅ **REST Assured** - Testes de API HTTP
- ✅ **End-to-End** - Testa toda a stack da aplicação
- ✅ **Isolamento** - Cada teste tem ambiente limpo

#### Arquivos de Teste:
- `BaseIntegrationTest.java` - Classe base com configuração do container
- `ProdutoIntegrationTest.java` - 10 testes de integração
  - CRUD completo de produtos
  - Validação de autenticação e autorização
  - Testes de permissões (ADMIN vs USUARIO)

- `PedidoIntegrationTest.java` - 9 testes de integração
  - Fluxo completo de pedidos
  - Processamento de pagamento com atualização de estoque
  - Validações de negócio

#### Por que TestContainers?
- ✅ Testa com MySQL real (não H2)
- ✅ Detecta problemas de queries específicas
- ✅ Ambiente idêntico à produção
- ✅ Cleanup automático

#### Exemplo de Teste de Integração:
```java
@Test
@DisplayName("Deve criar produto com token de admin")
void deveCriarProdutoComTokenAdmin() {
    // Given
    RequisicaoProduto requisicao = RequisicaoProduto.builder()
            .nome("Teclado Mecânico")
            .preco(new BigDecimal("450.00"))
            .categoria("Periféricos")
            .quantidadeEstoque(20)
            .build();

    // When & Then
    given()
        .header("Authorization", "Bearer " + tokenAdmin)
        .contentType(ContentType.JSON)
        .body(requisicao)
    .when()
        .post("/api/produtos")
    .then()
        .statusCode(201)
        .body("nome", equalTo("Teclado Mecânico"))
        .body("preco", equalTo(450.00f));
}
```

#### Executar Testes de Integração:
```bash
# IMPORTANTE: Docker deve estar rodando!
docker --version
docker ps

# Executar testes de integração
mvn verify

# Executar todos os testes (unitários + integração)
mvn clean verify

# Executar teste específico
mvn verify -Dit.test=ProdutoIntegrationTest
```

### Cobertura de Código

A cobertura de código é medida pelo **JaCoCo** com meta mínima de **50%**:

```bash
# Gerar relatório de cobertura
mvn clean test jacoco:report

# Relatório HTML disponível em:
# target/site/jacoco/index.html
```

#### Métricas de Cobertura:
- ✅ Cobertura de linhas
- ✅ Cobertura de branches
- ✅ Cobertura de métodos
- ✅ Complexidade ciclomática

### Configuração de Testes

Os testes utilizam um arquivo de configuração separado:

**src/test/resources/application-test.properties:**
```properties
spring.jpa.hibernate.ddl-auto=create-drop
spring.test.database.replace=none
logging.level.org.testcontainers=INFO
```

## 🔍 Análise Estática de Código

O projeto utiliza **5 ferramentas** de análise estática para garantir qualidade do código:

### 1. Checkstyle

**Objetivo:** Verificar estilo e padrões de código

**Configuração:** `checkstyle.xml`

**Verificações:**
- ✅ Nomenclatura de classes, métodos e variáveis
- ✅ Tamanho máximo de métodos (150 linhas)
- ✅ Número máximo de parâmetros (7)
- ✅ Complexidade ciclomática (máximo 15)
- ✅ Importações e espaços em branco
- ✅ Estrutura de blocos e chaves

**Executar:**
```bash
mvn checkstyle:check

# Gerar relatório HTML
mvn checkstyle:checkstyle
# Relatório: target/site/checkstyle.html
```

### 2. SpotBugs

**Objetivo:** Detectar bugs potenciais automaticamente

**Configuração:** Incluído no pom.xml com FindSecBugs

**Tipos de bugs detectados:**
- ✅ Null pointer dereferences
- ✅ Resource leaks
- ✅ Problemas de concorrência
- ✅ Más práticas de equals/hashCode
- ✅ Vulnerabilidades de segurança

**Executar:**
```bash
mvn spotbugs:check

# Gerar relatório
mvn spotbugs:spotbugs
# Relatório: target/spotbugsXml.xml
```

### 3. PMD

**Objetivo:** Análise de código fonte para problemas comuns

**Configuração:** `pmd-ruleset.xml`

**Categorias de regras:**
- ✅ Best Practices
- ✅ Code Style
- ✅ Design (complexidade, acoplamento)
- ✅ Error Prone
- ✅ Performance
- ✅ Security

**Executar:**
```bash
mvn pmd:check

# Gerar relatório HTML
mvn pmd:pmd
# Relatório: target/site/pmd.html
```

### 4. JaCoCo

**Objetivo:** Medir cobertura de testes

**Meta:** Mínimo 50% de cobertura de linhas

**Métricas:**
- ✅ Cobertura de linhas
- ✅ Cobertura de branches
- ✅ Cobertura de métodos
- ✅ Complexidade ciclomática

**Executar:**
```bash
mvn test jacoco:report

# Verificar se atingiu meta
mvn jacoco:check

# Relatório: target/site/jacoco/index.html
```

### 5. SonarQube

**Objetivo:** Análise completa de qualidade de código

**Status:** Preparado para integração

**Métricas analisadas:**
- ✅ Code Smells
- ✅ Bugs
- ✅ Vulnerabilidades de segurança
- ✅ Duplicação de código
- ✅ Cobertura de testes
- ✅ Dívida técnica

**Executar:**
```bash
# Com SonarQube local
mvn sonar:sonar \
  -Dsonar.projectKey=ecommerce-api \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=seu_token
```

### Executar Todas as Análises

```bash
# Pipeline completo: testes + análise estática
mvn clean verify

# Gerar todos os relatórios
mvn clean verify site

# Relatórios disponíveis em:
# - target/site/jacoco/index.html (Cobertura)
# - target/site/checkstyle.html (Checkstyle)
# - target/site/pmd.html (PMD)
# - target/spotbugsXml.xml (SpotBugs)
```

### Métricas de Qualidade Configuradas

| Métrica | Limite | Ferramenta |
|---------|--------|------------|
| Complexidade Ciclomática | 15 | Checkstyle, PMD |
| Tamanho de Método | 150 linhas | Checkstyle, PMD |
| Parâmetros por Método | 7 | Checkstyle, PMD |
| Cobertura de Código | 50% | JaCoCo |
| Tamanho de Arquivo | 500 linhas | Checkstyle |

### Integração com CI/CD

O projeto está preparado para integração com pipelines CI/CD:

```yaml
# Exemplo: .github/workflows/ci.yml
name: CI

on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
    
    - name: Build and Test
      run: mvn clean verify
    
    - name: Run Static Analysis
      run: mvn checkstyle:check spotbugs:check pmd:check
    
    - name: Upload Coverage
      uses: codecov/codecov-action@v3
```

## 📋 Requisitos

- Java 17 ou superior
- Maven 3.6 ou superior
- Docker e Docker Compose (para testes de integração e ambiente de desenvolvimento)

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

# 2. Execute os testes
mvn test                    # Testes unitários
mvn verify                  # Testes unitários + integração

# 3. Compile e execute
mvn clean install
mvn spring-boot:run

# Ou usando Maven wrapper
./mvnw clean install
./mvnw spring-boot:run
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
DESCRIBE usuarios;
DESCRIBE produtos;
DESCRIBE pedidos;
DESCRIBE itens_pedido;

-- Ver todos os usuários
SELECT * FROM usuarios;

-- Ver todos os produtos
SELECT * FROM produtos;

-- Ver todos os pedidos com itens
SELECT p.id, p.status, p.valor_total, u.nome as nome_usuario
FROM pedidos p
JOIN usuarios u ON o.usuario_id = u.id;

-- Ver produtos mais vendidos
SELECT p.nome, SUM(ip.quantidade) as total_vendido
FROM produtos p
JOIN itens_pedido ip ON p.id = ip.produto_id
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
ORDER BY total_gasto DESC
    LIMIT 5
```

**Otimizações:**
- JOIN direto entre Usuario e Pedido
- GROUP BY para agregar dados por usuário
- Índices em `usuario_id` e `status`

### 2. Ticket Médio
```sql
SELECT u.id, u.nome, u.email,
       COUNT(p.id) as total_pedidos,
       AVG(p.valor_total) as ticket_medio
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

## 📁 Estrutura do Projeto

```
ecommerce-api/
├── src/
│   ├── main/
│   │   ├── java/com/ecommerce/api/
│   │   │   ├── configuration/        # Configurações (Segurança, Aplicação)
│   │   │   ├── controller/          # Controladores REST
│   │   │   ├── dto/                 # Data Transfer Objects
│   │   │   ├── entity/              # Entidades JPA
│   │   │   ├── enums/               # Enumerações (Papel, StatusPedido)
│   │   │   ├── exception/           # Exceções customizadas
│   │   │   ├── repository/          # Repositórios JPA
│   │   │   ├── security/            # JWT e filtros de segurança
│   │   │   ├── service/             # Lógica de negócio
│   │   │   └── AplicacaoEcommerceApi.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       ├── java/com/ecommerce/api/
│       │   ├── service/             # Testes unitários
│       │   │   ├── ServicoProdutoTest.java
│       │   │   └── ServicoPedidoTest.java
│       │   └── integration/         # Testes de integração
│       │       ├── BaseIntegrationTest.java
│       │       ├── ProdutoIntegrationTest.java
│       │       └── PedidoIntegrationTest.java
│       └── resources/
│           └── application-test.properties
├── Dockerfile                       # Container da aplicação
├── docker-compose.yml              # Orquestração de containers
├── pom.xml                         # Dependências Maven
├── checkstyle.xml                  # Configuração Checkstyle
├── pmd-ruleset.xml                 # Configuração PMD
├── .gitignore                      # Arquivos ignorados pelo Git
└── README.md                       # Este arquivo
```

## 🔒 Segurança

- **JWT**: Tokens com expiração de 24 horas
- **BCrypt**: Hash de senhas com salt automático
- **HTTPS**: Recomendado em produção
- **CORS**: Configurável para ambientes específicos
- **SQL Injection**: Prevenido por PreparedStatements do JPA
- **XSS**: Validação de entrada com Bean Validation
- **FindSecBugs**: Análise de vulnerabilidades de segurança

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

## 🚀 Comandos Rápidos

```bash
# Testes
mvn test                           # Testes unitários
mvn verify                         # Testes unitários + integração
mvn test jacoco:report            # Cobertura de código

# Análise Estática
mvn checkstyle:check              # Verificar estilo
mvn spotbugs:check                # Detectar bugs
mvn pmd:check                     # Análise de código

# Pipeline Completo
mvn clean verify                  # Testes + análise
mvn clean verify site             # Testes + análise + relatórios

# Execução
mvn spring-boot:run               # Executar aplicação
docker-compose up --build         # Executar com Docker
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
7. **Testes**: Executar `mvn verify` requer Docker rodando para TestContainers

## 👤 Desenvolvedor

**Samuel Dantas**
- Email: samueldantasbarbosa@hotmail.com

---

**Projeto desenvolvido com foco em qualidade, testes abrangentes e boas práticas de desenvolvimento.**
