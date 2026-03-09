#!/bin/bash
set -e

# Script para criar múltiplos databases e users no PostgreSQL
# Baseado em: https://github.com/mrts/docker-postgresql-multiple-databases

echo "PostgreSQL initialization script started"

# Create product_db and product_user
echo "Creating product_db and product_user..."
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
    CREATE DATABASE product_db;
    CREATE USER product_user WITH PASSWORD 'product_pass';
    GRANT ALL PRIVILEGES ON DATABASE product_db TO product_user;

    -- Grant schema permissions
    \c product_db
    GRANT ALL ON SCHEMA public TO product_user;
    GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO product_user;
    GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO product_user;
EOSQL

# Create order_db and order_user
echo "Creating order_db and order_user..."
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
    CREATE DATABASE order_db;
    CREATE USER order_user WITH PASSWORD 'order_pass';
    GRANT ALL PRIVILEGES ON DATABASE order_db TO order_user;

    -- Grant schema permissions
    \c order_db
    GRANT ALL ON SCHEMA public TO order_user;
    GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO order_user;
    GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO order_user;
EOSQL

echo "All databases and users created successfully!"
