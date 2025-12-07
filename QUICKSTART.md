# 🚀 Guia de Início Rápido - E-Commerce API

## ⚡ Executar Testes

### Testes Unitários
```bash
# Executar testes unitários
mvn clean test
P
# Com relatório de cobertura
mvn test jacoco:report

# Ver cobertura: abra target/site/jacoco/index.html no navegador
```

### Testes de Integração
```bash
# Executar testes de integração (requer Docker)
mvn verify

# Executar todos os testes
mvn clean verify
```

## 🔍 Análise Estática

### Executar Todas as Análises
```bash
# Análise completa + testes
mvn clean verify

# Gerar todos os relatórios
mvn clean verify site
```

### Análises Individuais

**Checkstyle** (Estilo de Código)
```bash
mvn checkstyle:check
# Relatório: target/site/checkstyle.html
```

**SpotBugs** (Detecção de Bugs)
```bash
mvn spotbugs:check
# Relatório: target/spotbugsXml.xml
```

**PMD** (Análise de Código)
```bash
mvn pmd:check
# Relatório: target/site/pmd.html
```

**JaCoCo** (Cobertura)
```bash
mvn test jacoco:report
# Relatório: target/site/jacoco/index.html
```

## 🐳 Docker

### Executar com Docker Compose
```bash
# Iniciar aplicação + MySQL + Adminer
docker-compose up --build

# Acessar:
# - API: http://localhost:8080
# - Swagger: http://localhost:8080/swagger-ui.html
# - Adminer: http://localhost:8081
```

## 📊 Ver Relatórios

Após executar `mvn clean verify site`, abra no navegador:

1. **Cobertura de Testes**: `target/site/jacoco/index.html`
2. **Checkstyle**: `target/site/checkstyle.html`
3. **PMD**: `target/site/pmd.html`
4. **Relatório de Testes**: `target/surefire-reports/index.html`

## 🧪 Executar Teste Específico

```bash
# Um teste específico
mvn test -Dtest=ServicoProdutoTest

# Uma classe de teste de integração
mvn verify -Dit.test=ProdutoIntegrationTest
```

## 📝 Comandos Úteis

```bash
# Limpar build anterior
mvn clean

# Compilar sem executar testes
mvn clean install -DskipTests

# Executar aplicação localmente
mvn spring-boot:run

# Criar JAR
mvn package

# Executar JAR
java -jar target/ecommerce-api-1.0.0.jar
```

## 🎯 Checklist de Qualidade

Antes de fazer commit, execute:

```bash
# 1. Executar testes
mvn clean test

# 2. Executar análise estática
mvn checkstyle:check spotbugs:check pmd:check

# 3. Verificar cobertura
mvn jacoco:check

# 4. Executar testes de integração
mvn verify
```

## 🔧 Troubleshooting

**Docker não está rodando:**
```bash
# Verificar se Docker está ativo
docker --version
docker ps

# Iniciar Docker Desktop ou Docker daemon
```

**Porta 8080 já em uso:**
```bash
# Alterar porta no application.properties
server.port=8081
```

**TestContainers com erro:**
```bash
# Verificar Docker
docker run hello-world

# Limpar containers antigos
docker system prune -a
```

## 📚 Próximos Passos

1. ✅ Executar todos os testes: `mvn verify`
2. ✅ Verificar cobertura de código
3. ✅ Revisar relatórios de análise estática
4. ✅ Testar API com Swagger UI
5. ✅ Ler documentação completa no README.md
