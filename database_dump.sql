-- =====================================================
-- DUMP DO BANCO DE DADOS - E-COMMERCE API
-- =====================================================
-- Este dump contém a estrutura das tabelas e dados de exemplo
-- para facilitar o teste da aplicação
-- =====================================================

-- Criação do banco de dados
CREATE DATABASE IF NOT EXISTS ecommerce;
USE ecommerce;

-- =====================================================
-- TABELA: usuarios
-- =====================================================
CREATE TABLE IF NOT EXISTS usuarios (
                                        id BINARY(16) PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    papel VARCHAR(20) NOT NULL,
    criado_em DATETIME NOT NULL,
    atualizado_em DATETIME NOT NULL,
    INDEX idx_email (email),
    INDEX idx_papel (papel)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- TABELA: produtos
-- =====================================================
CREATE TABLE IF NOT EXISTS produtos (
                                        id BINARY(16) PRIMARY KEY,
    nome VARCHAR(200) NOT NULL,
    descricao TEXT,
    preco DECIMAL(10,2) NOT NULL,
    categoria VARCHAR(100) NOT NULL,
    quantidade_estoque INT NOT NULL,
    criado_em DATETIME NOT NULL,
    atualizado_em DATETIME NOT NULL,
    INDEX idx_categoria (categoria),
    INDEX idx_preco (preco),
    INDEX idx_estoque (quantidade_estoque)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- TABELA: pedidos
-- =====================================================
CREATE TABLE IF NOT EXISTS pedidos (
                                       id BINARY(16) PRIMARY KEY,
    usuario_id BINARY(16) NOT NULL,
    status VARCHAR(20) NOT NULL,
    valor_total DECIMAL(10,2) NOT NULL,
    criado_em DATETIME NOT NULL,
    atualizado_em DATETIME NOT NULL,
    pago_em DATETIME,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    INDEX idx_usuario_id (usuario_id),
    INDEX idx_status (status),
    INDEX idx_criado_em (criado_em),
    INDEX idx_pago_em (pago_em)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- TABELA: itens_pedido
-- =====================================================
CREATE TABLE IF NOT EXISTS itens_pedido (
                                            id BINARY(16) PRIMARY KEY,
    pedido_id BINARY(16) NOT NULL,
    produto_id BINARY(16) NOT NULL,
    quantidade INT NOT NULL,
    preco_unitario DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (pedido_id) REFERENCES pedidos(id) ON DELETE CASCADE,
    FOREIGN KEY (produto_id) REFERENCES produtos(id),
    INDEX idx_pedido_id (pedido_id),
    INDEX idx_produto_id (produto_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- DADOS DE EXEMPLO
-- =====================================================

-- Observação: Os usuários devem ser criados via endpoint /api/autenticacao/registrar
-- pois as senhas precisam ser hasheadas com BCrypt

-- Exemplos de produtos que podem ser inseridos após criar um usuário ADMIN:

/*
-- Eletrônicos
INSERT INTO produtos (id, nome, descricao, preco, categoria, quantidade_estoque, criado_em, atualizado_em) VALUES
(UUID_TO_BIN(UUID()), 'Notebook Dell Inspiron 15', 'Notebook com processador Intel i7, 16GB RAM, SSD 512GB', 3499.90, 'Eletrônicos', 10, NOW(), NOW()),
(UUID_TO_BIN(UUID()), 'Mouse Logitech MX Master 3', 'Mouse sem fio ergonômico para produtividade', 549.90, 'Eletrônicos', 25, NOW(), NOW()),
(UUID_TO_BIN(UUID()), 'Teclado Mecânico Keychron K2', 'Teclado mecânico wireless com switches Gateron', 799.90, 'Eletrônicos', 15, NOW(), NOW());

-- Livros
INSERT INTO produtos (id, nome, descricao, preco, categoria, quantidade_estoque, criado_em, atualizado_em) VALUES
(UUID_TO_BIN(UUID()), 'Clean Code - Robert C. Martin', 'Guia essencial para escrever código limpo e sustentável', 89.90, 'Livros', 50, NOW(), NOW()),
(UUID_TO_BIN(UUID()), 'Design Patterns - Gang of Four', 'Livro clássico sobre padrões de design de software', 129.90, 'Livros', 30, NOW(), NOW());

-- Roupas
INSERT INTO produtos (id, nome, descricao, preco, categoria, quantidade_estoque, criado_em, atualizado_em) VALUES
(UUID_TO_BIN(UUID()), 'Camiseta Básica Preta', 'Camiseta 100% algodão, tamanho M', 49.90, 'Roupas', 100, NOW(), NOW()),
(UUID_TO_BIN(UUID()), 'Calça Jeans Masculina', 'Calça jeans slim fit, cor azul escuro', 159.90, 'Roupas', 45, NOW(), NOW());
*/

-- =====================================================
-- INSTRUÇÕES DE USO
-- =====================================================
-- 1. Execute este dump no MySQL: mysql -u root -p < database_dump.sql
-- 2. Inicie a aplicação com Docker: docker-compose up -d
-- 3. Registre usuários via POST /api/autenticacao/registrar
-- 4. Crie produtos via POST /api/produtos (com usuário ADMIN)
-- 5. Teste os endpoints de pedidos e relatórios

-- =====================================================
-- QUERIES ÚTEIS PARA ANÁLISE
-- =====================================================

-- Top 5 usuários que mais compraram
/*
SELECT
    u.id as usuarioId,
    u.nome as nomeUsuario,
    u.email as emailUsuario,
    COUNT(p.id) as totalPedidos,
    SUM(p.valor_total) as totalGasto
FROM usuarios u
JOIN pedidos p ON p.usuario_id = u.id
WHERE p.status = 'PAGO'
GROUP BY u.id, u.nome, u.email
ORDER BY COUNT(p.id) DESC, SUM(p.valor_total) DESC
LIMIT 5;
*/

-- Ticket médio por usuário
/*
SELECT
    u.id as usuarioId,
    u.nome as nomeUsuario,
    u.email as emailUsuario,
    COUNT(p.id) as totalPedidos,
    AVG(p.valor_total) as ticketMedio,
    SUM(p.valor_total) as totalGasto
FROM usuarios u
JOIN pedidos p ON p.usuario_id = u.id
WHERE p.status = 'PAGO'
GROUP BY u.id, u.nome, u.email
ORDER BY AVG(p.valor_total) DESC;
*/

-- Faturamento do mês atual
/*
SELECT COALESCE(SUM(valor_total), 0) as faturamentoMensal
FROM pedidos
WHERE status = 'PAGO'
AND YEAR(pago_em) = YEAR(CURRENT_DATE)
AND MONTH(pago_em) = MONTH(CURRENT_DATE);
*/

-- Produtos mais vendidos
/*
SELECT
    p.id,
    p.nome,
    p.categoria,
    SUM(ip.quantidade) as totalVendido,
    SUM(ip.quantidade * ip.preco_unitario) as receitaTotal
FROM produtos p
JOIN itens_pedido ip ON ip.produto_id = p.id
JOIN pedidos ped ON ped.id = ip.pedido_id
WHERE ped.status = 'PAGO'
GROUP BY p.id, p.nome, p.categoria
ORDER BY totalVendido DESC
LIMIT 10;
*/

-- Produtos com estoque baixo (menos de 5 unidades)
/*
SELECT
    id,
    nome,
    categoria,
    quantidade_estoque,
    preco
FROM produtos
WHERE quantidade_estoque < 5
ORDER BY quantidade_estoque ASC;
*/

-- =====================================================
-- FIM DO DUMP
-- =====================================================