-- =============================================================================
-- Migration V004: Create Inventory Management Schema
-- =============================================================================
-- Description: Creates inventory, warehouse, and lot tracking tables
-- Author: 문명섭 (Moon Myeong-seop)
-- Company: (주)소프트아이스 (SoftIce Co., Ltd.)
-- Date: 2026-01-23
-- =============================================================================

-- Create Inventory schema
CREATE SCHEMA IF NOT EXISTS inventory;

-- =============================================================================
-- Table: inventory.sd_warehouses
-- Description: Warehouse master data
-- =============================================================================
CREATE TABLE inventory.sd_warehouses (
    warehouse_id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,

    -- Identification
    warehouse_code VARCHAR(50) NOT NULL,
    warehouse_name VARCHAR(200) NOT NULL,
    warehouse_type VARCHAR(20) NOT NULL, -- RAW_MATERIAL, WORK_IN_PROCESS, FINISHED_GOODS, QUARANTINE, SCRAP

    -- Location
    location VARCHAR(200),
    building VARCHAR(100),
    floor VARCHAR(50),

    -- Manager
    manager_user_id BIGINT,

    -- Capacity
    total_capacity DECIMAL(15,3),
    capacity_unit VARCHAR(20),

    -- Status
    is_active BOOLEAN NOT NULL DEFAULT true,

    -- Additional Information
    remarks TEXT,

    -- Audit Fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign Keys
    CONSTRAINT fk_warehouse_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES core.sd_tenants(tenant_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_warehouse_manager
        FOREIGN KEY (manager_user_id)
        REFERENCES core.sd_users(user_id)
        ON DELETE SET NULL,

    -- Unique Constraints
    CONSTRAINT uq_warehouse_code
        UNIQUE (tenant_id, warehouse_code),

    -- Check Constraints
    CONSTRAINT chk_warehouse_type
        CHECK (warehouse_type IN ('RAW_MATERIAL', 'WORK_IN_PROCESS', 'FINISHED_GOODS', 'QUARANTINE', 'SCRAP'))
);

-- Indexes for sd_warehouses
CREATE INDEX idx_warehouse_tenant ON inventory.sd_warehouses(tenant_id);
CREATE INDEX idx_warehouse_code ON inventory.sd_warehouses(warehouse_code);
CREATE INDEX idx_warehouse_type ON inventory.sd_warehouses(warehouse_type);
CREATE INDEX idx_warehouse_active ON inventory.sd_warehouses(is_active);

-- Trigger for updating updated_at on sd_warehouses
CREATE TRIGGER trg_warehouse_updated_at
    BEFORE UPDATE ON inventory.sd_warehouses
    FOR EACH ROW
    EXECUTE FUNCTION core.update_updated_at_column();

-- =============================================================================
-- Table: inventory.sd_lots
-- Description: LOT/Batch tracking
-- =============================================================================
CREATE TABLE inventory.sd_lots (
    lot_id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,

    -- LOT Identification
    lot_no VARCHAR(100) NOT NULL,
    batch_no VARCHAR(100),

    -- Manufacturing Information
    manufacturing_date DATE,
    expiry_date DATE,

    -- Quantities
    initial_quantity DECIMAL(15,3) NOT NULL DEFAULT 0,
    current_quantity DECIMAL(15,3) NOT NULL DEFAULT 0,
    reserved_quantity DECIMAL(15,3) NOT NULL DEFAULT 0,
    unit VARCHAR(20),

    -- Supplier Information
    supplier_name VARCHAR(200),
    supplier_lot_no VARCHAR(100),

    -- Quality Status
    quality_status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, PASSED, FAILED, QUARANTINE

    -- Reference
    work_order_id BIGINT,

    -- Status
    is_active BOOLEAN NOT NULL DEFAULT true,

    -- Additional Information
    remarks TEXT,

    -- Audit Fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign Keys
    CONSTRAINT fk_lot_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES core.sd_tenants(tenant_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_lot_product
        FOREIGN KEY (product_id)
        REFERENCES production.sd_products(product_id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_lot_work_order
        FOREIGN KEY (work_order_id)
        REFERENCES production.sd_work_orders(work_order_id)
        ON DELETE SET NULL,

    -- Unique Constraints
    CONSTRAINT uq_lot_no
        UNIQUE (tenant_id, lot_no),

    -- Check Constraints
    CONSTRAINT chk_lot_quality_status
        CHECK (quality_status IN ('PENDING', 'PASSED', 'FAILED', 'QUARANTINE')),
    CONSTRAINT chk_lot_quantities
        CHECK (initial_quantity >= 0 AND current_quantity >= 0 AND reserved_quantity >= 0),
    CONSTRAINT chk_lot_current_quantity
        CHECK (current_quantity <= initial_quantity)
);

-- Indexes for sd_lots
CREATE INDEX idx_lot_tenant ON inventory.sd_lots(tenant_id);
CREATE INDEX idx_lot_product ON inventory.sd_lots(product_id);
CREATE INDEX idx_lot_no ON inventory.sd_lots(lot_no);
CREATE INDEX idx_lot_quality_status ON inventory.sd_lots(quality_status);
CREATE INDEX idx_lot_active ON inventory.sd_lots(is_active);
CREATE INDEX idx_lot_expiry_date ON inventory.sd_lots(expiry_date);

-- Trigger for updating updated_at on sd_lots
CREATE TRIGGER trg_lot_updated_at
    BEFORE UPDATE ON inventory.sd_lots
    FOR EACH ROW
    EXECUTE FUNCTION core.update_updated_at_column();

-- =============================================================================
-- Table: inventory.sd_inventory
-- Description: Current inventory status (Product x Warehouse x LOT)
-- =============================================================================
CREATE TABLE inventory.sd_inventory (
    inventory_id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    lot_id BIGINT,

    -- Quantities
    available_quantity DECIMAL(15,3) NOT NULL DEFAULT 0,
    reserved_quantity DECIMAL(15,3) NOT NULL DEFAULT 0,
    unit VARCHAR(20),

    -- Location in Warehouse
    zone VARCHAR(50),
    rack VARCHAR(50),
    shelf VARCHAR(50),
    bin VARCHAR(50),

    -- Last Movement
    last_transaction_date TIMESTAMP,
    last_transaction_type VARCHAR(20),

    -- Additional Information
    remarks TEXT,

    -- Audit Fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign Keys
    CONSTRAINT fk_inventory_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES core.sd_tenants(tenant_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_inventory_warehouse
        FOREIGN KEY (warehouse_id)
        REFERENCES inventory.sd_warehouses(warehouse_id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_inventory_product
        FOREIGN KEY (product_id)
        REFERENCES production.sd_products(product_id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_inventory_lot
        FOREIGN KEY (lot_id)
        REFERENCES inventory.sd_lots(lot_id)
        ON DELETE SET NULL,

    -- Unique Constraints
    CONSTRAINT uq_inventory_product_warehouse_lot
        UNIQUE (tenant_id, warehouse_id, product_id, lot_id),

    -- Check Constraints
    CONSTRAINT chk_inventory_quantities
        CHECK (available_quantity >= 0 AND reserved_quantity >= 0)
);

-- Indexes for sd_inventory
CREATE INDEX idx_inventory_tenant ON inventory.sd_inventory(tenant_id);
CREATE INDEX idx_inventory_warehouse ON inventory.sd_inventory(warehouse_id);
CREATE INDEX idx_inventory_product ON inventory.sd_inventory(product_id);
CREATE INDEX idx_inventory_lot ON inventory.sd_inventory(lot_id);

-- Trigger for updating updated_at on sd_inventory
CREATE TRIGGER trg_inventory_updated_at
    BEFORE UPDATE ON inventory.sd_inventory
    FOR EACH ROW
    EXECUTE FUNCTION core.update_updated_at_column();

-- =============================================================================
-- Table: inventory.sd_inventory_transactions
-- Description: Inventory movement history
-- =============================================================================
CREATE TABLE inventory.sd_inventory_transactions (
    transaction_id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,

    -- Transaction Identification
    transaction_no VARCHAR(50) NOT NULL,
    transaction_date TIMESTAMP NOT NULL,
    transaction_type VARCHAR(20) NOT NULL, -- IN_RECEIVE, IN_PRODUCTION, IN_RETURN, OUT_ISSUE, OUT_SCRAP, MOVE, ADJUST

    -- Related Entities
    warehouse_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    lot_id BIGINT,

    -- For MOVE type
    from_warehouse_id BIGINT,
    to_warehouse_id BIGINT,

    -- Quantities
    quantity DECIMAL(15,3) NOT NULL,
    unit VARCHAR(20),

    -- References
    work_order_id BIGINT,
    quality_inspection_id BIGINT,
    reference_no VARCHAR(100),

    -- User
    transaction_user_id BIGINT NOT NULL,

    -- Approval
    approval_status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED
    approved_by_user_id BIGINT,
    approved_date TIMESTAMP,

    -- Additional Information
    remarks TEXT,

    -- Audit Fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign Keys
    CONSTRAINT fk_inv_trans_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES core.sd_tenants(tenant_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_inv_trans_warehouse
        FOREIGN KEY (warehouse_id)
        REFERENCES inventory.sd_warehouses(warehouse_id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_inv_trans_from_warehouse
        FOREIGN KEY (from_warehouse_id)
        REFERENCES inventory.sd_warehouses(warehouse_id)
        ON DELETE SET NULL,
    CONSTRAINT fk_inv_trans_to_warehouse
        FOREIGN KEY (to_warehouse_id)
        REFERENCES inventory.sd_warehouses(warehouse_id)
        ON DELETE SET NULL,
    CONSTRAINT fk_inv_trans_product
        FOREIGN KEY (product_id)
        REFERENCES production.sd_products(product_id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_inv_trans_lot
        FOREIGN KEY (lot_id)
        REFERENCES inventory.sd_lots(lot_id)
        ON DELETE SET NULL,
    CONSTRAINT fk_inv_trans_work_order
        FOREIGN KEY (work_order_id)
        REFERENCES production.sd_work_orders(work_order_id)
        ON DELETE SET NULL,
    CONSTRAINT fk_inv_trans_quality_inspection
        FOREIGN KEY (quality_inspection_id)
        REFERENCES qms.sd_quality_inspections(quality_inspection_id)
        ON DELETE SET NULL,
    CONSTRAINT fk_inv_trans_user
        FOREIGN KEY (transaction_user_id)
        REFERENCES core.sd_users(user_id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_inv_trans_approved_by
        FOREIGN KEY (approved_by_user_id)
        REFERENCES core.sd_users(user_id)
        ON DELETE SET NULL,

    -- Unique Constraints
    CONSTRAINT uq_inv_trans_no
        UNIQUE (tenant_id, transaction_no),

    -- Check Constraints
    CONSTRAINT chk_inv_trans_type
        CHECK (transaction_type IN ('IN_RECEIVE', 'IN_PRODUCTION', 'IN_RETURN', 'OUT_ISSUE', 'OUT_SCRAP', 'MOVE', 'ADJUST')),
    CONSTRAINT chk_inv_trans_approval
        CHECK (approval_status IN ('PENDING', 'APPROVED', 'REJECTED')),
    CONSTRAINT chk_inv_trans_quantity
        CHECK (quantity > 0)
);

-- Indexes for sd_inventory_transactions
CREATE INDEX idx_inv_trans_tenant ON inventory.sd_inventory_transactions(tenant_id);
CREATE INDEX idx_inv_trans_no ON inventory.sd_inventory_transactions(transaction_no);
CREATE INDEX idx_inv_trans_date ON inventory.sd_inventory_transactions(transaction_date);
CREATE INDEX idx_inv_trans_type ON inventory.sd_inventory_transactions(transaction_type);
CREATE INDEX idx_inv_trans_warehouse ON inventory.sd_inventory_transactions(warehouse_id);
CREATE INDEX idx_inv_trans_product ON inventory.sd_inventory_transactions(product_id);
CREATE INDEX idx_inv_trans_lot ON inventory.sd_inventory_transactions(lot_id);
CREATE INDEX idx_inv_trans_approval ON inventory.sd_inventory_transactions(approval_status);

-- Trigger for updating updated_at on sd_inventory_transactions
CREATE TRIGGER trg_inv_trans_updated_at
    BEFORE UPDATE ON inventory.sd_inventory_transactions
    FOR EACH ROW
    EXECUTE FUNCTION core.update_updated_at_column();

-- =============================================================================
-- Comments
-- =============================================================================
COMMENT ON SCHEMA inventory IS 'Inventory Management System schema';

COMMENT ON TABLE inventory.sd_warehouses IS 'Warehouse master data';
COMMENT ON COLUMN inventory.sd_warehouses.warehouse_type IS 'RAW_MATERIAL: 원자재, WORK_IN_PROCESS: 재공품, FINISHED_GOODS: 완제품, QUARANTINE: 검역, SCRAP: 폐기';

COMMENT ON TABLE inventory.sd_lots IS 'LOT/Batch tracking for traceability';
COMMENT ON COLUMN inventory.sd_lots.quality_status IS 'PENDING: 검사대기, PASSED: 합격, FAILED: 불합격, QUARANTINE: 격리';

COMMENT ON TABLE inventory.sd_inventory IS 'Current inventory status aggregated by Product, Warehouse, and LOT';
COMMENT ON COLUMN inventory.sd_inventory.available_quantity IS 'Available quantity for use';
COMMENT ON COLUMN inventory.sd_inventory.reserved_quantity IS 'Reserved quantity for work orders';

COMMENT ON TABLE inventory.sd_inventory_transactions IS 'All inventory movements history';
COMMENT ON COLUMN inventory.sd_inventory_transactions.transaction_type IS 'IN_RECEIVE: 입고, IN_PRODUCTION: 생산입고, IN_RETURN: 반품입고, OUT_ISSUE: 불출, OUT_SCRAP: 폐기, MOVE: 이동, ADJUST: 조정';

-- =============================================================================
-- Grant Permissions
-- =============================================================================
GRANT USAGE ON SCHEMA inventory TO mes_user;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA inventory TO mes_user;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA inventory TO mes_user;
