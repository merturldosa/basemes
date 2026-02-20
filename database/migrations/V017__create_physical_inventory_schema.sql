-- ====================================================================
-- Physical Inventory Management Schema
-- 실사 관리 스키마
-- ====================================================================

-- --------------------------------------------------------------------
-- 1. sd_physical_inventories (실사 계획)
-- --------------------------------------------------------------------
CREATE TABLE inventory.sd_physical_inventories (
    physical_inventory_id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    inventory_no VARCHAR(50) NOT NULL,
    inventory_date TIMESTAMP NOT NULL,
    warehouse_id BIGINT NOT NULL,
    inventory_status VARCHAR(20) NOT NULL DEFAULT 'PLANNED',
    planned_by_user_id BIGINT,
    approved_by_user_id BIGINT,
    approval_date TIMESTAMP,
    remarks TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(50),
    updated_by VARCHAR(50),

    CONSTRAINT fk_physical_inventory_tenant
        FOREIGN KEY (tenant_id) REFERENCES common.sd_tenants(tenant_id),
    CONSTRAINT fk_physical_inventory_warehouse
        FOREIGN KEY (warehouse_id) REFERENCES inventory.sd_warehouses(warehouse_id),
    CONSTRAINT uk_physical_inventory_no
        UNIQUE (tenant_id, inventory_no)
);

COMMENT ON TABLE inventory.sd_physical_inventories IS '실사 계획';
COMMENT ON COLUMN inventory.sd_physical_inventories.physical_inventory_id IS '실사 ID';
COMMENT ON COLUMN inventory.sd_physical_inventories.inventory_no IS '실사 번호 (PI-YYYYMMDD-0001)';
COMMENT ON COLUMN inventory.sd_physical_inventories.inventory_date IS '실사 일자';
COMMENT ON COLUMN inventory.sd_physical_inventories.warehouse_id IS '창고 ID';
COMMENT ON COLUMN inventory.sd_physical_inventories.inventory_status IS '실사 상태 (PLANNED, IN_PROGRESS, COMPLETED, CANCELLED)';
COMMENT ON COLUMN inventory.sd_physical_inventories.planned_by_user_id IS '계획자 ID';
COMMENT ON COLUMN inventory.sd_physical_inventories.approved_by_user_id IS '승인자 ID';

-- 인덱스
CREATE INDEX idx_physical_inventories_tenant_warehouse
    ON inventory.sd_physical_inventories(tenant_id, warehouse_id);
CREATE INDEX idx_physical_inventories_status
    ON inventory.sd_physical_inventories(inventory_status);
CREATE INDEX idx_physical_inventories_date
    ON inventory.sd_physical_inventories(inventory_date);

-- --------------------------------------------------------------------
-- 2. sd_physical_inventory_items (실사 항목)
-- --------------------------------------------------------------------
CREATE TABLE inventory.sd_physical_inventory_items (
    physical_inventory_item_id BIGSERIAL PRIMARY KEY,
    physical_inventory_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    lot_id BIGINT,
    location VARCHAR(50),
    system_quantity DECIMAL(15, 3) NOT NULL DEFAULT 0,
    counted_quantity DECIMAL(15, 3),
    difference_quantity DECIMAL(15, 3),
    adjustment_status VARCHAR(20) NOT NULL DEFAULT 'NOT_REQUIRED',
    adjustment_transaction_id BIGINT,
    counted_by_user_id BIGINT,
    counted_at TIMESTAMP,
    remarks TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_physical_inventory_item_header
        FOREIGN KEY (physical_inventory_id)
        REFERENCES inventory.sd_physical_inventories(physical_inventory_id),
    CONSTRAINT fk_physical_inventory_item_product
        FOREIGN KEY (product_id) REFERENCES mes.sd_products(product_id),
    CONSTRAINT fk_physical_inventory_item_lot
        FOREIGN KEY (lot_id) REFERENCES inventory.sd_lots(lot_id),
    CONSTRAINT fk_physical_inventory_item_transaction
        FOREIGN KEY (adjustment_transaction_id)
        REFERENCES inventory.sd_inventory_transactions(inventory_transaction_id)
);

COMMENT ON TABLE inventory.sd_physical_inventory_items IS '실사 항목';
COMMENT ON COLUMN inventory.sd_physical_inventory_items.physical_inventory_item_id IS '실사 항목 ID';
COMMENT ON COLUMN inventory.sd_physical_inventory_items.physical_inventory_id IS '실사 ID';
COMMENT ON COLUMN inventory.sd_physical_inventory_items.product_id IS '제품 ID';
COMMENT ON COLUMN inventory.sd_physical_inventory_items.lot_id IS 'LOT ID (선택사항)';
COMMENT ON COLUMN inventory.sd_physical_inventory_items.location IS '위치';
COMMENT ON COLUMN inventory.sd_physical_inventory_items.system_quantity IS '시스템 재고 수량';
COMMENT ON COLUMN inventory.sd_physical_inventory_items.counted_quantity IS '실사 수량';
COMMENT ON COLUMN inventory.sd_physical_inventory_items.difference_quantity IS '차이 수량';
COMMENT ON COLUMN inventory.sd_physical_inventory_items.adjustment_status IS '조정 상태 (NOT_REQUIRED, PENDING, APPROVED, REJECTED)';
COMMENT ON COLUMN inventory.sd_physical_inventory_items.adjustment_transaction_id IS '조정 트랜잭션 ID';
COMMENT ON COLUMN inventory.sd_physical_inventory_items.counted_by_user_id IS '실사자 ID';

-- 인덱스
CREATE INDEX idx_physical_inventory_items_header
    ON inventory.sd_physical_inventory_items(physical_inventory_id);
CREATE INDEX idx_physical_inventory_items_product
    ON inventory.sd_physical_inventory_items(product_id);
CREATE INDEX idx_physical_inventory_items_lot
    ON inventory.sd_physical_inventory_items(lot_id);
CREATE INDEX idx_physical_inventory_items_status
    ON inventory.sd_physical_inventory_items(adjustment_status);
