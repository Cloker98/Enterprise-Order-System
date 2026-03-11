#!/bin/bash

# Database Health Check Script
# Verifies if product table exists and recreates if necessary

set -e

echo "🔍 Checking database health..."

# Check if PostgreSQL container is running
if ! docker ps | grep -q eos-postgresql; then
    echo "❌ PostgreSQL container is not running!"
    echo "💡 Run: cd infrastructure/docker && docker-compose up -d postgresql"
    exit 1
fi

# Check if products table exists
TABLE_EXISTS=$(docker exec eos-postgresql psql -U product_user -d product_db -t -c "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'products');" | xargs)

if [ "$TABLE_EXISTS" = "f" ]; then
    echo "❌ Products table is missing!"
    echo "🔧 Recreating products table..."
    
    # Recreate table
    docker exec eos-postgresql psql -U product_user -d product_db -c "
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
        CONSTRAINT chk_price_positive CHECK (price > 0),
        CONSTRAINT chk_stock_non_negative CHECK (stock_quantity >= 0),
        CONSTRAINT uq_sku UNIQUE (sku)
    );
    
    CREATE UNIQUE INDEX IF NOT EXISTS idx_products_sku ON products(sku);
    CREATE INDEX IF NOT EXISTS idx_products_category ON products(category);
    CREATE INDEX IF NOT EXISTS idx_products_status ON products(status);
    CREATE INDEX IF NOT EXISTS idx_products_created_at ON products(created_at DESC);
    CREATE INDEX IF NOT EXISTS idx_products_category_status ON products(category, status);
    "
    
    echo "✅ Products table recreated successfully!"
else
    echo "✅ Products table exists"
fi

# Show table info
echo "📊 Database status:"
docker exec eos-postgresql psql -U product_user -d product_db -c "\dt"

echo "📈 Products count:"
docker exec eos-postgresql psql -U product_user -d product_db -c "SELECT COUNT(*) as product_count FROM products;"

echo "🔍 Flyway history:"
docker exec eos-postgresql psql -U product_user -d product_db -c "SELECT version, description, success, installed_on FROM flyway_schema_history ORDER BY installed_on;"

echo "✅ Database health check completed!"