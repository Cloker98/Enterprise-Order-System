-- Product Service - Initial Schema
-- Version: 001
-- Description: Create products table with indexes and constraints
-- Author: Enterprise Team
-- Date: 2026-03-09

-- =========================================
-- TABLE: products
-- =========================================
CREATE TABLE IF NOT EXISTS products (
    id UUID PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    price DECIMAL(10, 2) NOT NULL,
    stock_quantity INTEGER NOT NULL,
    sku VARCHAR(50) NOT NULL,
    category VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT chk_price_positive CHECK (price > 0),
    CONSTRAINT chk_stock_non_negative CHECK (stock_quantity >= 0),
    CONSTRAINT uq_sku UNIQUE (sku)
);

-- =========================================
-- INDEXES for performance
-- =========================================

-- Unique index for SKU (already created by UNIQUE constraint, but explicit)
CREATE UNIQUE INDEX IF NOT EXISTS idx_products_sku ON products(sku);

-- Index for category filtering
CREATE INDEX IF NOT EXISTS idx_products_category ON products(category);

-- Index for status filtering (ACTIVE vs INACTIVE)
CREATE INDEX IF NOT EXISTS idx_products_status ON products(status);

-- Index for sorting by creation date (most recent first)
CREATE INDEX IF NOT EXISTS idx_products_created_at ON products(created_at DESC);

-- Composite index for listing products by category + status
CREATE INDEX IF NOT EXISTS idx_products_category_status ON products(category, status);

-- =========================================
-- COMMENTS (documentation)
-- =========================================

COMMENT ON TABLE products IS 'Product catalog table - stores all products available for ordering';
COMMENT ON COLUMN products.id IS 'Unique product identifier (UUID)';
COMMENT ON COLUMN products.name IS 'Product name (max 200 characters)';
COMMENT ON COLUMN products.description IS 'Product description (max 1000 characters)';
COMMENT ON COLUMN products.price IS 'Product price in BRL (must be positive, 2 decimal places)';
COMMENT ON COLUMN products.stock_quantity IS 'Available stock quantity (must be non-negative)';
COMMENT ON COLUMN products.sku IS 'Stock Keeping Unit - unique alphanumeric identifier';
COMMENT ON COLUMN products.category IS 'Product category: ELECTRONICS, CLOTHING, FOOD, BOOKS, OTHER';
COMMENT ON COLUMN products.status IS 'Product status: ACTIVE or INACTIVE (soft delete)';
COMMENT ON COLUMN products.created_at IS 'Timestamp when product was created';
COMMENT ON COLUMN products.updated_at IS 'Timestamp when product was last updated';
