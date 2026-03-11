-- V002__recreate_products_table.sql
-- Recreate products table (fixing the disappearing table issue)

-- Drop table if exists (to ensure clean state)
DROP TABLE IF EXISTS products CASCADE;

-- Create products table
CREATE TABLE products (
    id UUID PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    price DECIMAL(10, 2) NOT NULL CHECK (price > 0),
    stock_quantity INTEGER NOT NULL CHECK (stock_quantity >= 0),
    sku VARCHAR(50) NOT NULL UNIQUE,
    category VARCHAR(20) NOT NULL CHECK (category IN ('ELECTRONICS', 'CLOTHING', 'FOOD', 'BOOKS', 'OTHER')),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE')),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Create indexes for performance
CREATE INDEX idx_sku ON products(sku);
CREATE INDEX idx_category ON products(category);
CREATE INDEX idx_status ON products(status);
CREATE INDEX idx_created_at ON products(created_at);

-- Insert some test data to verify table persistence
INSERT INTO products (id, name, description, price, stock_quantity, sku, category, status, created_at, updated_at)
VALUES 
    ('550e8400-e29b-41d4-a716-446655440000', 'Test Product', 'Test product to verify table creation', 99.99, 10, 'TEST-001', 'ELECTRONICS', 'ACTIVE', NOW(), NOW());

-- Verify table was created
SELECT 'Products table created successfully with ' || COUNT(*) || ' test record(s)' as result FROM products;