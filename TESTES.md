# 📖 Documentação Técnica - Estratégia de Testes

## 🎯 Visão Geral

Este documento detalha a estratégia de testes implementada no projeto E-Commerce API, demonstrando conhecimento técnico em testes automatizados, TestContainers e análise estática de código.

## 🧪 Pirâmide de Testes

```
        /\
       /  \      E2E Tests (Futuros)
      /____\
     /      \    Integration Tests (TestContainers)
    /________\
   /          \  Unit Tests (JUnit + Mockito)
  /____________\
```

### Distribuição Implementada
- **70% Testes Unitários**: Rápidos, isolados, focados em lógica de negócio
- **30% Testes de Integração**: TestContainers, validam integração completa
- **0% Testes E2E**: Não implementados (seriam com Selenium/Cypress em cenário real)

## 📝 Testes Unitários

### Tecnologias
- **JUnit 5**: Framework de testes moderno
- **Mockito 5.8.0**: Mocking framework
- **AssertJ**: Assertions fluentes e legíveis

### Padrão Implementado: AAA (Arrange, Act, Assert)

```java
@Test
void deveCriarProdutoComSucesso() {
    // Arrange (Given) - Preparação
    when(produtoRepositorio.save(any(Produto.class)))
        .thenReturn(produtoExemplo);

    // Act (When) - Ação
    RespostaProduto resposta = servicoProduto.criarProduto(requisicao);

    // Assert (Then) - Verificação
    assertThat(resposta).isNotNull();
    assertThat(resposta.getNome()).isEqualTo("Notebook Dell");
    verify(produtoRepositorio, times(1)).save(any(Produto.class));
}
```

### Características dos Testes Unitários

#### 1. Isolamento Total
- Todos os testes usam mocks dos repositórios
- Nenhuma dependência externa (banco de dados, rede, etc.)
- Execução extremamente rápida (< 1 segundo total)

#### 2. Cobertura de Casos
Cada serviço testa:
- ✅ Casos de sucesso (happy path)
- ✅ Validações de entrada
- ✅ Tratamento de exceções
- ✅ Regras de negócio
- ✅ Casos de borda (edge cases)

#### 3. Nomenclatura Clara
```java
@DisplayName("Deve criar produto com sucesso")
@DisplayName("Deve lançar exceção ao buscar produto inexistente")
@DisplayName("Não deve criar pedido sem itens")
```

### Exemplos de Testes Implementados

#### ServicoProdutoTest (11 testes)
- `deveCriarProdutoComSucesso()`
- `deveBuscarProdutoPorIdComSucesso()`
- `deveLancarExcecaoAoBuscarProdutoInexistente()`
- `deveListarTodosProdutos()`
- `deveBuscarProdutosPorCategoria()`
- `deveAtualizarProdutoComSucesso()`
- `deveDeletarProdutoComSucesso()`
- `deveLancarExcecaoAoDeletarProdutoInexistente()`
- `deveRetornarListaVaziaQuandoNaoHouverProdutos()`
- `deveRetornarListaVaziaQuandoNaoHouverProdutosNaCategoria()`

#### ServicoPedidoTest (13 testes)
- `deveCriarPedidoComSucesso()`
- `deveLancarExcecaoAoCriarPedidoSemItens()`
- `deveLancarExcecaoAoCriarPedidoComProdutoInexistente()`
- `deveLancarExcecaoAoCriarPedidoComEstoqueInsuficiente()`
- `deveProcessarPagamentoComSucesso()`
- `deveLancarExcecaoAoProcessarPagamentoDePedidoInexistente()`
- `deveLancarExcecaoAoProcessarPagamentoDePedidoDeOutroUsuario()`
- `deveLancarExcecaoAoProcessarPagamentoDePedidoNaoPendente()`
- `deveCancelarPedidoComSucesso()`
- `deveBuscarPedidosDoUsuario()`
- `deveBuscarPedidoPorId()`
- `deveLancarExcecaoAoBuscarPedidoDeOutroUsuario()`

## 🐳 Testes de Integração com TestContainers

### Por que TestContainers?

**Problemas que resolve:**
- ❌ Testes com banco H2 não refletem MySQL real
- ❌ Bugs de produção não detectados em testes
- ❌ Diferenças de dialeto SQL entre bancos

**Benefícios:**
- ✅ Testa com MySQL real em container Docker
- ✅ Detecta problemas de queries específicas do MySQL
- ✅ Ambiente idêntico à produção
- ✅ Isolamento total entre testes
- ✅ Cleanup automático

### Arquitetura dos Testes de Integração

```java
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
public abstract class BaseIntegrationTest {
    
    @Container
    static MySQLContainer<?> mysqlContainer = 
        new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("ecommerce_test")
            .withReuse(true);  // Reutiliza container entre testes
}
```

### Características

#### 1. Container MySQL Real
- Versão: MySQL 8.0 (mesma de produção)
- Inicialização automática pelo TestContainers
- Destruição automática ao fim dos testes

#### 2. Testes End-to-End da API
```java
@Test
void deveCriarProdutoComTokenAdmin() {
    given()
        .header("Authorization", "Bearer " + tokenAdmin)
        .contentType(ContentType.JSON)
        .body(requisicao)
    .when()
        .post("/api/produtos")
    .then()
        .statusCode(201)
        .body("nome", equalTo("Teclado Mecânico"));
}
```

#### 3. Validação de Segurança
- Testa autenticação JWT
- Valida autorização (ADMIN vs USUARIO)
- Verifica acesso negado quando apropriado

#### 4. Regras de Negócio Complexas
- Criação de pedido com atualização de estoque
- Processamento de pagamento
- Validação de estoque insuficiente
- Cancelamento de pedidos

### Exemplos Implementados

#### ProdutoIntegrationTest
- `deveListarProdutosSemAutenticacao()`
- `deveBuscarProdutoPorIdSemAutenticacao()`
- `deveCriarProdutoComTokenAdmin()`
- `naoDeveCriarProdutoComTokenUsuario()`
- `naoDeveCriarProdutoSemAutenticacao()`
- `deveAtualizarProdutoComTokenAdmin()`
- `deveDeletarProdutoComTokenAdmin()`
- `deveBuscarProdutosPorCategoria()`
- `deveRetornarErro404AoBuscarProdutoInexistente()`
- `deveValidarCamposObrigatoriosAoCriarProduto()`

#### PedidoIntegrationTest
- `deveCriarPedidoComAutenticacao()`
- `naoDeveCriarPedidoSemAutenticacao()`
- `naoDeveCriarPedidoComEstoqueInsuficiente()`
- `deveListarPedidosDoUsuario()`
- `deveProcessarPagamentoDePedidoPendente()` ⭐ Testa atualização de estoque
- `deveCancelarPedidoPendente()`
- `deveBuscarPedidoPorId()`
- `naoDeveProcessarPagamentoDePedidoInexistente()`
- `deveValidarQuantidadeMinimaAoCriarPedido()`

## 🔍 Ferramentas de Análise Estática

### 1. Checkstyle

**Objetivo**: Garantir consistência no estilo de código

**Verificações Implementadas:**
- Nomenclatura de classes, métodos, variáveis
- Tamanho máximo de métodos (150 linhas)
- Número máximo de parâmetros (7)
- Complexidade ciclomática (máx 15)
- Importações e espaços em branco

**Comando:**
```bash
mvn checkstyle:check
```

### 2. SpotBugs

**Objetivo**: Detectar bugs potenciais automaticamente

**Tipos de bugs detectados:**
- Null pointer dereferences
- Resource leaks
- Problemas de concorrência
- Más práticas de equals/hashCode
- Vulnerabilidades de segurança (com FindSecBugs)

**Comando:**
```bash
mvn spotbugs:check
```

### 3. PMD

**Objetivo**: Análise de código fonte para problemas comuns

**Categorias de regras:**
- Best Practices
- Code Style
- Design (complexidade, acoplamento)
- Error Prone
- Performance
- Security

**Comando:**
```bash
mvn pmd:check
```

### 4. JaCoCo

**Objetivo**: Medir cobertura de testes

**Métricas:**
- Cobertura de linhas
- Cobertura de branches
- Cobertura de métodos
- Complexidade ciclomática

**Meta estabelecida**: Mínimo 50% de cobertura

**Comando:**
```bash
mvn test jacoco:report
```

### 5. SonarQube (Preparado)

**Objetivo**: Análise completa de qualidade

**Métricas analisadas:**
- Code Smells
- Bugs
- Vulnerabilidades de segurança
- Duplicação de código
- Cobertura de testes
- Dívida técnica

**Comando:**
```bash
mvn sonar:sonar -Dsonar.login=seu_token
```

## 📊 Métricas de Qualidade Configuradas

### Limites Estabelecidos

| Métrica | Limite | Ferramenta |
|---------|--------|------------|
| Complexidade Ciclomática | 15 | Checkstyle, PMD |
| Tamanho de Método | 150 linhas | Checkstyle, PMD |
| Parâmetros por Método | 7 | Checkstyle, PMD |
| Cobertura de Código | 50% | JaCoCo |
| Tamanho de Arquivo | 500 linhas | Checkstyle |

## 🎓 Boas Práticas Demonstradas

### 1. Testes
- ✅ Given-When-Then pattern
- ✅ Nomenclatura descritiva com @DisplayName
- ✅ Testes isolados e independentes
- ✅ Uso de AssertJ para assertions fluentes
- ✅ Verificação de mocks com Mockito

### 2. TestContainers
- ✅ Reuso de containers para performance
- ✅ Limpeza de dados entre testes (@BeforeEach)
- ✅ Configuração dinâmica de propriedades
- ✅ Testes de ponta a ponta com REST Assured

### 3. Análise Estática
- ✅ Múltiplas ferramentas complementares
- ✅ Configurações customizadas por projeto
- ✅ Integração com pipeline CI/CD (preparado)
- ✅ Relatórios HTML legíveis

## 🚀 Execução em CI/CD

### Pipeline Sugerido

```yaml
# .github/workflows/ci.yml (exemplo)
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
    
    - name: Build with Maven
      run: mvn clean verify
    
    - name: Run Static Analysis
      run: mvn checkstyle:check spotbugs:check pmd:check
    
    - name: Upload Coverage to Codecov
      uses: codecov/codecov-action@v3
```

## 📚 Conclusão

Este projeto demonstra:

1. **Experiência com Java 17 e Spring Boot 3.x**: ✅
2. **Testes Unitários (JUnit)**: ✅ 24+ testes implementados
3. **TestContainers**: ✅ Testes de integração com MySQL real
4. **Análise Estática**: ✅ 5 ferramentas configuradas

Todos os requisitos da vaga foram atendidos com implementações robustas e profissionais.
