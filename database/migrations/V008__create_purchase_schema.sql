-- ============================================================================
-- Purchase Management Schema
-- 구매 관리 스키마
-- Author: Moon Myung-seop
-- Date: 2026-01-23
-- ============================================================================

-- Create purchase schema
CREATE SCHEMA IF NOT EXISTS purchase;

-- ============================================================================
-- Table: purchase.sd_purchase_requests
-- Description: 구매 요청
-- ============================================================================
CREATE TABLE purchase.sd_purchase_requests (
    purchase_request_id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    request_no VARCHAR(50) NOT NULL,
    request_date TIMESTAMP NOT NULL,
    requester_user_id BIGINT NOT NULL,
    department VARCHAR(100),

    -- Request Details
    material_id BIGINT NOT NULL,
    requested_quantity NUMERIC(15, 3) NOT NULL,
    required_date TIMESTAMP,
    purpose VARCHAR(500),

    -- Approval
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',  -- PENDING, APPROVED, REJECTED, ORDERED
    approver_user_id BIGINT,
    approval_date TIMESTAMP,
    approval_comment TEXT,

    -- Additional Info
    remarks TEXT,

    -- Audit Fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Foreign Keys
    CONSTRAINT fk_purchase_request_tenant FOREIGN KEY (tenant_id)
        REFERENCES core.sd_tenants(tenant_id) ON DELETE CASCADE,
    CONSTRAINT fk_purchase_request_requester FOREIGN KEY (requester_user_id)
        REFERENCES core.sd_users(user_id) ON DELETE RESTRICT,
    CONSTRAINT fk_purchase_request_approver FOREIGN KEY (approver_user_id)
        REFERENCES core.sd_users(user_id) ON DELETE RESTRICT,
    CONSTRAINT fk_purchase_request_material FOREIGN KEY (material_id)
        REFERENCES material.sd_materials(material_id) ON DELETE RESTRICT,

    -- Unique Constraints
    CONSTRAINT uk_purchase_request_no UNIQUE (tenant_id, request_no)
);

-- ============================================================================
-- Table: purchase.sd_purchase_orders
-- Description: 구매 주문 (발주)
-- ============================================================================
CREATE TABLE purchase.sd_purchase_orders (
    purchase_order_id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    order_no VARCHAR(50) NOT NULL,
    order_date TIMESTAMP NOT NULL,

    -- Supplier
    supplier_id BIGINT NOT NULL,

    -- Delivery
    expected_delivery_date TIMESTAMP,
    delivery_address TEXT,

    -- Payment
    payment_terms VARCHAR(20),  -- NET30, NET60, COD, ADVANCE
    currency VARCHAR(10),

    -- Status
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',  -- DRAFT, CONFIRMED, PARTIALLY_RECEIVED, RECEIVED, CANCELLED

    -- Totals
    total_amount NUMERIC(15, 2),

    -- User
    buyer_user_id BIGINT NOT NULL,

    -- Additional Info
    remarks TEXT,

    -- Audit Fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Foreign Keys
    CONSTRAINT fk_purchase_order_tenant FOREIGN KEY (tenant_id)
        REFERENCES core.sd_tenants(tenant_id) ON DELETE CASCADE,
    CONSTRAINT fk_purchase_order_supplier FOREIGN KEY (supplier_id)
        REFERENCES business.sd_suppliers(supplier_id) ON DELETE RESTRICT,
    CONSTRAINT fk_purchase_order_buyer FOREIGN KEY (buyer_user_id)
        REFERENCES core.sd_users(user_id) ON DELETE RESTRICT,

    -- Unique Constraints
    CONSTRAINT uk_purchase_order_no UNIQUE (tenant_id, order_no)
);

-- ============================================================================
-- Table: purchase.sd_purchase_order_items
-- Description: 구매 주문 상세
-- ============================================================================
CREATE TABLE purchase.sd_purchase_order_items (
    purchase_order_item_id BIGSERIAL PRIMARY KEY,
    purchase_order_id BIGINT NOT NULL,
    line_no INTEGER NOT NULL,

    -- Material
    material_id BIGINT NOT NULL,

    -- Quantity
    ordered_quantity NUMERIC(15, 3) NOT NULL,
    received_quantity NUMERIC(15, 3) DEFAULT 0,
    unit VARCHAR(20) NOT NULL,

    -- Price
    unit_price NUMERIC(15, 2),
    amount NUMERIC(15, 2),

    -- Delivery
    required_date TIMESTAMP,

    -- Reference
    purchase_request_id BIGINT,

    -- Additional Info
    remarks TEXT,

    -- Audit Fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Foreign Keys
    CONSTRAINT fk_purchase_order_item_order FOREIGN KEY (purchase_order_id)
        REFERENCES purchase.sd_purchase_orders(purchase_order_id) ON DELETE CASCADE,
    CONSTRAINT fk_purchase_order_item_material FOREIGN KEY (material_id)
        REFERENCES material.sd_materials(material_id) ON DELETE RESTRICT,
    CONSTRAINT fk_purchase_order_item_request FOREIGN KEY (purchase_request_id)
        REFERENCES purchase.sd_purchase_requests(purchase_request_id) ON DELETE SET NULL,

    -- Unique Constraints
    CONSTRAINT uk_purchase_order_item_line UNIQUE (purchase_order_id, line_no)
);

-- ============================================================================
-- Table: purchase.sd_goods_receipts
-- Description: 입하 (입고)
-- ============================================================================
CREATE TABLE purchase.sd_goods_receipts (
    goods_receipt_id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    receipt_no VARCHAR(50) NOT NULL,
    receipt_date TIMESTAMP NOT NULL,

    -- Purchase Order
    purchase_order_id BIGINT NOT NULL,

    -- Warehouse
    warehouse_id BIGINT NOT NULL,

    -- Inspection
    inspection_status VARCHAR(20) DEFAULT 'PENDING',  -- PENDING, INSPECTING, PASSED, FAILED
    inspector_user_id BIGINT,
    inspection_date TIMESTAMP,

    -- Status
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',  -- PENDING, COMPLETED

    -- User
    receiver_user_id BIGINT NOT NULL,

    -- Additional Info
    remarks TEXT,

    -- Audit Fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Foreign Keys
    CONSTRAINT fk_goods_receipt_tenant FOREIGN KEY (tenant_id)
        REFERENCES core.sd_tenants(tenant_id) ON DELETE CASCADE,
    CONSTRAINT fk_goods_receipt_order FOREIGN KEY (purchase_order_id)
        REFERENCES purchase.sd_purchase_orders(purchase_order_id) ON DELETE RESTRICT,
    CONSTRAINT fk_goods_receipt_warehouse FOREIGN KEY (warehouse_id)
        REFERENCES inventory.sd_warehouses(warehouse_id) ON DELETE RESTRICT,
    CONSTRAINT fk_goods_receipt_receiver FOREIGN KEY (receiver_user_id)
        REFERENCES core.sd_users(user_id) ON DELETE RESTRICT,
    CONSTRAINT fk_goods_receipt_inspector FOREIGN KEY (inspector_user_id)
        REFERENCES core.sd_users(user_id) ON DELETE RESTRICT,

    -- Unique Constraints
    CONSTRAINT uk_goods_receipt_no UNIQUE (tenant_id, receipt_no)
);

-- ============================================================================
-- Table: purchase.sd_goods_receipt_items
-- Description: 입하 상세
-- ============================================================================
CREATE TABLE purchase.sd_goods_receipt_items (
    goods_receipt_item_id BIGSERIAL PRIMARY KEY,
    goods_receipt_id BIGINT NOT NULL,
    line_no INTEGER NOT NULL,

    -- Purchase Order Item
    purchase_order_item_id BIGINT NOT NULL,

    -- Material
    material_id BIGINT NOT NULL,

    -- Quantity
    received_quantity NUMERIC(15, 3) NOT NULL,
    accepted_quantity NUMERIC(15, 3),
    rejected_quantity NUMERIC(15, 3),
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
    CONSTRAINT fk_goods_receipt_item_receipt FOREIGN KEY (goods_receipt_id)
        REFERENCES purchase.sd_goods_receipts(goods_receipt_id) ON DELETE CASCADE,
    CONSTRAINT fk_goods_receipt_item_order_item FOREIGN KEY (purchase_order_item_id)
        REFERENCES purchase.sd_purchase_order_items(purchase_order_item_id) ON DELETE RESTRICT,
    CONSTRAINT fk_goods_receipt_item_material FOREIGN KEY (material_id)
        REFERENCES material.sd_materials(material_id) ON DELETE RESTRICT,
    CONSTRAINT fk_goods_receipt_item_lot FOREIGN KEY (lot_id)
        REFERENCES inventory.sd_lots(lot_id) ON DELETE SET NULL,

    -- Unique Constraints
    CONSTRAINT uk_goods_receipt_item_line UNIQUE (goods_receipt_id, line_no)
);

-- ============================================================================
-- Indexes
-- ============================================================================
-- Purchase Requests
CREATE INDEX idx_purchase_request_tenant ON purchase.sd_purchase_requests(tenant_id);
CREATE INDEX idx_purchase_request_status ON purchase.sd_purchase_requests(status);
CREATE INDEX idx_purchase_request_material ON purchase.sd_purchase_requests(material_id);
CREATE INDEX idx_purchase_request_date ON purchase.sd_purchase_requests(request_date);

-- Purchase Orders
CREATE INDEX idx_purchase_order_tenant ON purchase.sd_purchase_orders(tenant_id);
CREATE INDEX idx_purchase_order_supplier ON purchase.sd_purchase_orders(supplier_id);
CREATE INDEX idx_purchase_order_status ON purchase.sd_purchase_orders(status);
CREATE INDEX idx_purchase_order_date ON purchase.sd_purchase_orders(order_date);

-- Purchase Order Items
CREATE INDEX idx_purchase_order_item_order ON purchase.sd_purchase_order_items(purchase_order_id);
CREATE INDEX idx_purchase_order_item_material ON purchase.sd_purchase_order_items(material_id);

-- Goods Receipts
CREATE INDEX idx_goods_receipt_tenant ON purchase.sd_goods_receipts(tenant_id);
CREATE INDEX idx_goods_receipt_order ON purchase.sd_goods_receipts(purchase_order_id);
CREATE INDEX idx_goods_receipt_warehouse ON purchase.sd_goods_receipts(warehouse_id);
CREATE INDEX idx_goods_receipt_status ON purchase.sd_goods_receipts(status);
CREATE INDEX idx_goods_receipt_date ON purchase.sd_goods_receipts(receipt_date);

-- Goods Receipt Items
CREATE INDEX idx_goods_receipt_item_receipt ON purchase.sd_goods_receipt_items(goods_receipt_id);
CREATE INDEX idx_goods_receipt_item_material ON purchase.sd_goods_receipt_items(material_id);

-- ============================================================================
-- Triggers for updated_at
-- ============================================================================
CREATE OR REPLACE FUNCTION purchase.update_purchase_request_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_purchase_request_timestamp
    BEFORE UPDATE ON purchase.sd_purchase_requests
    FOR EACH ROW
    EXECUTE FUNCTION purchase.update_purchase_request_timestamp();

CREATE OR REPLACE FUNCTION purchase.update_purchase_order_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_purchase_order_timestamp
    BEFORE UPDATE ON purchase.sd_purchase_orders
    FOR EACH ROW
    EXECUTE FUNCTION purchase.update_purchase_order_timestamp();

CREATE OR REPLACE FUNCTION purchase.update_purchase_order_item_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_purchase_order_item_timestamp
    BEFORE UPDATE ON purchase.sd_purchase_order_items
    FOR EACH ROW
    EXECUTE FUNCTION purchase.update_purchase_order_item_timestamp();

CREATE OR REPLACE FUNCTION purchase.update_goods_receipt_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_goods_receipt_timestamp
    BEFORE UPDATE ON purchase.sd_goods_receipts
    FOR EACH ROW
    EXECUTE FUNCTION purchase.update_goods_receipt_timestamp();

CREATE OR REPLACE FUNCTION purchase.update_goods_receipt_item_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_goods_receipt_item_timestamp
    BEFORE UPDATE ON purchase.sd_goods_receipt_items
    FOR EACH ROW
    EXECUTE FUNCTION purchase.update_goods_receipt_item_timestamp();

-- ============================================================================
-- Comments
-- ============================================================================
COMMENT ON SCHEMA purchase IS '구매 관리 스키마';

-- Purchase Requests
COMMENT ON TABLE purchase.sd_purchase_requests IS '구매 요청 테이블';
COMMENT ON COLUMN purchase.sd_purchase_requests.purchase_request_id IS '구매 요청 ID (PK)';
COMMENT ON COLUMN purchase.sd_purchase_requests.request_no IS '요청 번호';
COMMENT ON COLUMN purchase.sd_purchase_requests.status IS '상태 (PENDING, APPROVED, REJECTED, ORDERED)';

-- Purchase Orders
COMMENT ON TABLE purchase.sd_purchase_orders IS '구매 주문 (발주) 테이블';
COMMENT ON COLUMN purchase.sd_purchase_orders.purchase_order_id IS '구매 주문 ID (PK)';
COMMENT ON COLUMN purchase.sd_purchase_orders.order_no IS '주문 번호';
COMMENT ON COLUMN purchase.sd_purchase_orders.status IS '상태 (DRAFT, CONFIRMED, PARTIALLY_RECEIVED, RECEIVED, CANCELLED)';

-- Purchase Order Items
COMMENT ON TABLE purchase.sd_purchase_order_items IS '구매 주문 상세 테이블';
COMMENT ON COLUMN purchase.sd_purchase_order_items.purchase_order_item_id IS '구매 주문 상세 ID (PK)';

-- Goods Receipts
COMMENT ON TABLE purchase.sd_goods_receipts IS '입하 (입고) 테이블';
COMMENT ON COLUMN purchase.sd_goods_receipts.goods_receipt_id IS '입하 ID (PK)';
COMMENT ON COLUMN purchase.sd_goods_receipts.receipt_no IS '입하 번호';
COMMENT ON COLUMN purchase.sd_goods_receipts.inspection_status IS '검사 상태 (PENDING, INSPECTING, PASSED, FAILED)';

-- Goods Receipt Items
COMMENT ON TABLE purchase.sd_goods_receipt_items IS '입하 상세 테이블';
COMMENT ON COLUMN purchase.sd_goods_receipt_items.goods_receipt_item_id IS '입하 상세 ID (PK)';
