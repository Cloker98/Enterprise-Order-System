-- Create orders table
CREATE TABLE orders (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    customer_id VARCHAR(100) NOT NULL,
    total_amount DECIMAL(19,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    cancellation_reason VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT DEFAULT 0
);

-- Create indexes for performance
CREATE INDEX idx_orders_customer_id ON orders(customer_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created_at ON orders(created_at);
CREATE INDEX idx_orders_customer_status ON orders(customer_id, status);
CREATE INDEX idx_orders_customer_created ON orders(customer_id, created_at);

-- Add comments for documentation
COMMENT ON TABLE orders IS 'Order aggregate root table';
COMMENT ON COLUMN orders.id IS 'Unique order identifier (UUID)';
COMMENT ON COLUMN orders.customer_id IS 'Customer identifier who placed the order';
COMMENT ON COLUMN orders.total_amount IS 'Total amount of the order in BRL';
COMMENT ON COLUMN orders.status IS 'Current order status (PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED)';
COMMENT ON COLUMN orders.cancellation_reason IS 'Reason for order cancellation (if applicable)';
COMMENT ON COLUMN orders.created_at IS 'Order creation timestamp';
COMMENT ON COLUMN orders.updated_at IS 'Last update timestamp';
COMMENT ON COLUMN orders.version IS 'Optimistic locking version';