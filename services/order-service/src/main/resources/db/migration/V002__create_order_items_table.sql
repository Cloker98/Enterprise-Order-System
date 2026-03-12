-- Create order_items table
CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id VARCHAR(36) NOT NULL,
    product_id VARCHAR(100) NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(19,2) NOT NULL CHECK (unit_price > 0),
    total_price DECIMAL(19,2) NOT NULL CHECK (total_price > 0),
    
    -- Foreign key constraint
    CONSTRAINT fk_order_items_order_id 
        FOREIGN KEY (order_id) REFERENCES orders(id) 
        ON DELETE CASCADE
);

-- Create indexes for performance
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_product_id ON order_items(product_id);

-- Unique constraint to prevent duplicate products in same order
CREATE UNIQUE INDEX idx_order_items_order_product 
    ON order_items(order_id, product_id);

-- Add comments for documentation
COMMENT ON TABLE order_items IS 'Order items table - contains products within each order';
COMMENT ON COLUMN order_items.id IS 'Auto-generated primary key';
COMMENT ON COLUMN order_items.order_id IS 'Reference to the parent order';
COMMENT ON COLUMN order_items.product_id IS 'Product identifier';
COMMENT ON COLUMN order_items.product_name IS 'Product name (denormalized for performance)';
COMMENT ON COLUMN order_items.quantity IS 'Quantity of the product ordered';
COMMENT ON COLUMN order_items.unit_price IS 'Price per unit in BRL';
COMMENT ON COLUMN order_items.total_price IS 'Total price for this item (quantity * unit_price)';