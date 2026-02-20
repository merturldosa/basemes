-- ============================================================================
-- Sales Management Schema
-- 판매 관리 스키마
-- Author: Moon Myung-seop
-- Date: 2026-01-23
-- ============================================================================

-- Create sales schema
CREATE SCHEMA IF NOT EXISTS sales;

-- ============================================================================
-- Table: sales.sd_sales_orders
-- Description: 판매 주문
-- ============================================================================
CREATE TABLE sales.sd_sales_orders (
    sales_order_id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    order_no VARCHAR(50) NOT NULL,
    order_date TIMESTAMP NOT NULL,

    -- Customer
    customer_id BIGINT NOT NULL,

    -- Delivery
    requested_delivery_date TIMESTAMP,
    delivery_address TEXT,

    -- Payment
    payment_terms VARCHAR(20),  -- NET30, NET60, COD, ADVANCE
    currency VARCHAR(10),

    -- Status
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',  -- DRAFT, CONFIRMED, PARTIALLY_DELIVERED, DELIVERED, CANCELLED

    -- Totals
    total_amount NUMERIC(15, 2),

    -- User
    sales_user_id BIGINT NOT NULL,

    -- Additional Info
    remarks TEXT,

    -- Audit Fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Foreign Keys
    CONSTRAINT fk_sales_order_tenant FOREIGN KEY (tenant_id)
        REFERENCES core.sd_tenants(tenant_id) ON DELETE CASCADE,
    CONSTRAINT fk_sales_order_customer FOREIGN KEY (customer_id)
        REFERENCES business.sd_customers(customer_id) ON DELETE RESTRICT,
    CONSTRAINT fk_sales_order_sales_user FOREIGN KEY (sales_user_id)
        REFERENCES core.sd_users(user_id) ON DELETE RESTRICT,

    -- Unique Constraints
    CONSTRAINT uk_sales_order_no UNIQUE (tenant_id, order_no)
);

-- ============================================================================
-- Table: sales.sd_sales_order_items
-- Description: 판매 주문 상세
-- ============================================================================
CREATE TABLE sales.sd_sales_order_items (
    sales_order_item_id BIGSERIAL PRIMARY KEY,
    sales_order_id BIGINT NOT NULL,
    line_no INTEGER NOT NULL,

    -- Product/Material
    product_id BIGINT,
    material_id BIGINT,

    -- Quantity
    ordered_quantity NUMERIC(15, 3) NOT NULL,
    delivered_quantity NUMERIC(15, 3) DEFAULT 0,
    unit VARCHAR(20) NOT NULL,

    -- Price
    unit_price NUMERIC(15, 2),
    amount NUMERIC(15, 2),

    -- Delivery
    requested_date TIMESTAMP,

    -- Additional Info
    remarks TEXT,

    -- Audit Fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Foreign Keys
    CONSTRAINT fk_sales_order_item_order FOREIGN KEY (sales_order_id)
        REFERENCES sales.sd_sales_orders(sales_order_id) ON DELETE CASCADE,
    CONSTRAINT fk_sales_order_item_product FOREIGN KEY (product_id)
        REFERENCES production.sd_products(product_id) ON DELETE RESTRICT,
    CONSTRAINT fk_sales_order_item_material FOREIGN KEY (material_id)
        REFERENCES material.sd_materials(material_id) ON DELETE RESTRICT,

    -- Unique Constraints
    CONSTRAINT uk_sales_order_item_line UNIQUE (sales_order_id, line_no),

    -- Check Constraints
    CONSTRAINT chk_sales_order_item_product_or_material CHECK (
        (product_id IS NOT NULL AND material_id IS NULL) OR
        (product_id IS NULL AND material_id IS NOT NULL)
    )
);

-- ============================================================================
-- Table: sales.sd_deliveries
-- Description: 출하
-- ============================================================================
CREATE TABLE sales.sd_deliveries (
    delivery_id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    delivery_no VARCHAR(50) NOT NULL,
    delivery_date TIMESTAMP NOT NULL,

    -- Sales Order
    sales_order_id BIGINT NOT NULL,

    -- Warehouse
    warehouse_id BIGINT NOT NULL,

    -- Quality Check
    quality_check_status VARCHAR(20) DEFAULT 'PENDING',  -- PENDING, INSPECTING, PASSED, FAILED
    inspector_user_id BIGINT,
    inspection_date TIMESTAMP,

    -- Shipment
    shipping_method VARCHAR(50),
    tracking_no VARCHAR(100),
    carrier VARCHAR(100),

    -- Status
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',  -- PENDING, COMPLETED

    -- User
    shipper_user_id BIGINT NOT NULL,

    -- Additional Info
    remarks TEXT,

    -- Audit Fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Foreign Keys
    CONSTRAINT fk_delivery_tenant FOREIGN KEY (tenant_id)
        REFERENCES core.sd_tenants(tenant_id) ON DELETE CASCADE,
    CONSTRAINT fk_delivery_order FOREIGN KEY (sales_order_id)
        REFERENCES sales.sd_sales_orders(sales_order_id) ON DELETE RESTRICT,
    CONSTRAINT fk_delivery_warehouse FOREIGN KEY (warehouse_id)
        REFERENCES inventory.sd_warehouses(warehouse_id) ON DELETE RESTRICT,
    CONSTRAINT fk_delivery_shipper FOREIGN KEY (shipper_user_id)
        REFERENCES core.sd_users(user_id) ON DELETE RESTRICT,
    CONSTRAINT fk_delivery_inspector FOREIGN KEY (inspector_user_id)
        REFERENCES core.sd_users(user_id) ON DELETE RESTRICT,

    -- Unique Constraints
    CONSTRAINT uk_delivery_no UNIQUE (tenant_id, delivery_no)
);

-- ============================================================================
-- Table: sales.sd_delivery_items
-- Description: 출하 상세
-- ============================================================================
CREATE TABLE sales.sd_delivery_items (
    delivery_item_id BIGSERIAL PRIMARY KEY,
    delivery_id BIGINT NOT NULL,
    line_no INTEGER NOT NULL,

    -- Sales Order Item
    sales_order_item_id BIGINT NOT NULL,

    -- Product/Material
    product_id BIGINT,
    material_id BIGINT,

    -- Quantity
    delivered_quantity NUMERIC(15, 3) NOT NULL,
    unit VARCHAR(20) NOT NULL,

    -- LOT
    lot_id BIGINT,

    -- Location
    location VARCHAR(100),

    -- Additional Info
    remarks TEXT,

    -- Audit Fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Foreign Keys
    CONSTRAINT fk_delivery_item_delivery FOREIGN KEY (delivery_id)
        REFERENCES sales.sd_deliveries(delivery_id) ON DELETE CASCADE,
    CONSTRAINT fk_delivery_item_order_item FOREIGN KEY (sales_order_item_id)
        REFERENCES sales.sd_sales_order_items(sales_order_item_id) ON DELETE RESTRICT,
    CONSTRAINT fk_delivery_item_product FOREIGN KEY (product_id)
        REFERENCES production.sd_products(product_id) ON DELETE RESTRICT,
    CONSTRAINT fk_delivery_item_material FOREIGN KEY (material_id)
        REFERENCES material.sd_materials(material_id) ON DELETE RESTRICT,
    CONSTRAINT fk_delivery_item_lot FOREIGN KEY (lot_id)
        REFERENCES inventory.sd_lots(lot_id) ON DELETE SET NULL,

    -- Unique Constraints
    CONSTRAINT uk_delivery_item_line UNIQUE (delivery_id, line_no),

    -- Check Constraints
    CONSTRAINT chk_delivery_item_product_or_material CHECK (
        (product_id IS NOT NULL AND material_id IS NULL) OR
        (product_id IS NULL AND material_id IS NOT NULL)
    )
);

-- ============================================================================
-- Indexes
-- ============================================================================
-- Sales Orders
CREATE INDEX idx_sales_order_tenant ON sales.sd_sales_orders(tenant_id);
CREATE INDEX idx_sales_order_customer ON sales.sd_sales_orders(customer_id);
CREATE INDEX idx_sales_order_status ON sales.sd_sales_orders(status);
CREATE INDEX idx_sales_order_date ON sales.sd_sales_orders(order_date);

-- Sales Order Items
CREATE INDEX idx_sales_order_item_order ON sales.sd_sales_order_items(sales_order_id);
CREATE INDEX idx_sales_order_item_product ON sales.sd_sales_order_items(product_id);
CREATE INDEX idx_sales_order_item_material ON sales.sd_sales_order_items(material_id);

-- Deliveries
CREATE INDEX idx_delivery_tenant ON sales.sd_deliveries(tenant_id);
CREATE INDEX idx_delivery_order ON sales.sd_deliveries(sales_order_id);
CREATE INDEX idx_delivery_warehouse ON sales.sd_deliveries(warehouse_id);
CREATE INDEX idx_delivery_status ON sales.sd_deliveries(status);
CREATE INDEX idx_delivery_date ON sales.sd_deliveries(delivery_date);

-- Delivery Items
CREATE INDEX idx_delivery_item_delivery ON sales.sd_delivery_items(delivery_id);
CREATE INDEX idx_delivery_item_product ON sales.sd_delivery_items(product_id);
CREATE INDEX idx_delivery_item_material ON sales.sd_delivery_items(material_id);

-- ============================================================================
-- Triggers for updated_at
-- ============================================================================
CREATE OR REPLACE FUNCTION sales.update_sales_order_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_sales_order_timestamp
    BEFORE UPDATE ON sales.sd_sales_orders
    FOR EACH ROW
    EXECUTE FUNCTION sales.update_sales_order_timestamp();

CREATE OR REPLACE FUNCTION sales.update_sales_order_item_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_sales_order_item_timestamp
    BEFORE UPDATE ON sales.sd_sales_order_items
    FOR EACH ROW
    EXECUTE FUNCTION sales.update_sales_order_item_timestamp();

CREATE OR REPLACE FUNCTION sales.update_delivery_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_delivery_timestamp
    BEFORE UPDATE ON sales.sd_deliveries
    FOR EACH ROW
    EXECUTE FUNCTION sales.update_delivery_timestamp();

CREATE OR REPLACE FUNCTION sales.update_delivery_item_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_delivery_item_timestamp
    BEFORE UPDATE ON sales.sd_delivery_items
    FOR EACH ROW
    EXECUTE FUNCTION sales.update_delivery_item_timestamp();

-- ============================================================================
-- Comments
-- ============================================================================
COMMENT ON SCHEMA sales IS '판매 관리 스키마';

-- Sales Orders
COMMENT ON TABLE sales.sd_sales_orders IS '판매 주문 테이블';
COMMENT ON COLUMN sales.sd_sales_orders.sales_order_id IS '판매 주문 ID (PK)';
COMMENT ON COLUMN sales.sd_sales_orders.order_no IS '주문 번호';
COMMENT ON COLUMN sales.sd_sales_orders.status IS '상태 (DRAFT, CONFIRMED, PARTIALLY_DELIVERED, DELIVERED, CANCELLED)';

-- Sales Order Items
COMMENT ON TABLE sales.sd_sales_order_items IS '판매 주문 상세 테이블';
COMMENT ON COLUMN sales.sd_sales_order_items.sales_order_item_id IS '판매 주문 상세 ID (PK)';

-- Deliveries
COMMENT ON TABLE sales.sd_deliveries IS '출하 테이블';
COMMENT ON COLUMN sales.sd_deliveries.delivery_id IS '출하 ID (PK)';
COMMENT ON COLUMN sales.sd_deliveries.delivery_no IS '출하 번호';
COMMENT ON COLUMN sales.sd_deliveries.quality_check_status IS '품질 검사 상태 (PENDING, INSPECTING, PASSED, FAILED)';

-- Delivery Items
COMMENT ON TABLE sales.sd_delivery_items IS '출하 상세 테이블';
COMMENT ON COLUMN sales.sd_delivery_items.delivery_item_id IS '출하 상세 ID (PK)';
