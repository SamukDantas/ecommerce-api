-- =====================================================
-- DUMP DO BANCO DE DADOS - E-COMMERCE API
-- =====================================================
-- Este dump contém a estrutura das tabelas e dados de exemplo
-- para facilitar o teste da aplicação
-- =====================================================

-- Criação do banco de dados
CREATE DATABASE IF NOT EXISTS ecommerce_db;
USE ecommerce_db;

-- =====================================================
-- TABELA: users
-- =====================================================
CREATE TABLE IF NOT EXISTS users (
    id BINARY(16) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_email (email),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- TABELA: products
-- =====================================================
CREATE TABLE IF NOT EXISTS products (
    id BINARY(16) PRIMARY KEY,
    nome VARCHAR(200) NOT NULL,
    descricao TEXT,
    preco DECIMAL(10,2) NOT NULL,
    categoria VARCHAR(100) NOT NULL,
    quantidade_estoque INT NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_categoria (categoria),
    INDEX idx_preco (preco),
    INDEX idx_estoque (quantidade_estoque)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- TABELA: orders
-- =====================================================
CREATE TABLE IF NOT EXISTS orders (
    id BINARY(16) PRIMARY KEY,
    user_id BINARY(16) NOT NULL,
    status VARCHAR(20) NOT NULL,
    total_value DECIMAL(10,2) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    paid_at DATETIME,
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_paid_at (paid_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- TABELA: order_items
-- =====================================================
CREATE TABLE IF NOT EXISTS order_items (
    id BINARY(16) PRIMARY KEY,
    order_id BINARY(16) NOT NULL,
    product_id BINARY(16) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id),
    INDEX idx_order_id (order_id),
    INDEX idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- DADOS DE EXEMPLO
-- =====================================================

-- Observação: Os usuários devem ser criados via endpoint /api/auth/register
-- pois as senhas precisam ser hasheadas com BCrypt

-- Exemplo de produtos (use o endpoint POST /api/products após criar um usuário ADMIN)
-- ou execute os INSERTs abaixo manualmente após iniciar a aplicação:

/*
-- Exemplo de produtos que podem ser inseridos:

-- Eletrônicos
INSERT INTO products VALUES 
(UUID_TO_BIN(UUID()), 'Notebook Dell Inspiron 15', 'Notebook com processador Intel i7, 16GB RAM, SSD 512GB', 3499.90, 'Eletrônicos', 10, NOW(), NOW()),
(UUID_TO_BIN(UUID()), 'Mouse Logitech MX Master 3', 'Mouse sem fio ergonômico para produtividade', 549.90, 'Eletrônicos', 25, NOW(), NOW()),
(UUID_TO_BIN(UUID()), 'Teclado Mecânico Keychron K2', 'Teclado mecânico wireless com switches Gateron', 799.90, 'Eletrônicos', 15, NOW(), NOW());

-- Livros
INSERT INTO products VALUES 
(UUID_TO_BIN(UUID()), 'Clean Code - Robert C. Martin', 'Guia essencial para escrever código limpo e sustentável', 89.90, 'Livros', 50, NOW(), NOW()),
(UUID_TO_BIN(UUID()), 'Design Patterns - Gang of Four', 'Livro clássico sobre padrões de design de software', 129.90, 'Livros', 30, NOW(), NOW());

-- Roupas
INSERT INTO products VALUES 
(UUID_TO_BIN(UUID()), 'Camiseta Básica Preta', 'Camiseta 100% algodão, tamanho M', 49.90, 'Roupas', 100, NOW(), NOW()),
(UUID_TO_BIN(UUID()), 'Calça Jeans Masculina', 'Calça jeans slim fit, cor azul escuro', 159.90, 'Roupas', 45, NOW(), NOW());
*/

-- =====================================================
-- INSTRUÇÕES DE USO
-- =====================================================
-- 1. Execute este dump no MySQL: mysql -u root -p < database_dump.sql
-- 2. Inicie a aplicação com Docker: docker-compose up -d
-- 3. Registre usuários via POST /api/auth/register
-- 4. Crie produtos via POST /api/products (com usuário ADMIN)
-- 5. Teste os endpoints de pedidos e relatórios

-- =====================================================
-- QUERIES ÚTEIS PARA ANÁLISE
-- =====================================================

-- Top 5 usuários que mais compraram
/*
SELECT 
    u.id as userId, 
    u.name as userName, 
    u.email as userEmail, 
    COUNT(o.id) as totalOrders, 
    SUM(o.total_value) as totalSpent
FROM users u
JOIN orders o ON o.user_id = u.id
WHERE o.status = 'PAGO'
GROUP BY u.id, u.name, u.email
ORDER BY COUNT(o.id) DESC, SUM(o.total_value) DESC
LIMIT 5;
*/

-- Ticket médio por usuário
/*
SELECT 
    u.id as userId, 
    u.name as userName, 
    u.email as userEmail,
    COUNT(o.id) as totalOrders, 
    AVG(o.total_value) as averageTicket,
    SUM(o.total_value) as totalSpent
FROM users u
JOIN orders o ON o.user_id = u.id
WHERE o.status = 'PAGO'
GROUP BY u.id, u.name, u.email
ORDER BY AVG(o.total_value) DESC;
*/

-- Faturamento do mês atual
/*
SELECT COALESCE(SUM(total_value), 0) as monthlyRevenue
FROM orders
WHERE status = 'PAGO'
AND YEAR(paid_at) = YEAR(CURRENT_DATE)
AND MONTH(paid_at) = MONTH(CURRENT_DATE);
*/

-- =====================================================
-- FIM DO DUMP
-- =====================================================
