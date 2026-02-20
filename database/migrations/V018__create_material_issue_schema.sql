-- ============================================================
-- Migration: V018 - Material Issue Management Schema (불출 관리)
-- Description: 생산 자재 불출 신청 및 지시 관리
-- Author: Moon Myung-seop
-- Date: 2026-01-24
-- ============================================================

-- Create schema if not exists
CREATE SCHEMA IF NOT EXISTS wms;

-- ============================================================
-- 1. MATERIAL REQUESTS (불출 신청)
-- ============================================================

CREATE TABLE wms.sd_material_requests (
    material_request_id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,

    -- Identification
    request_no VARCHAR(50) NOT NULL,
    request_date TIMESTAMP NOT NULL,

    -- Work Order Reference
    work_order_id BIGINT,
    work_order_no VARCHAR(50),

    -- Requester Information
    requester_user_id BIGINT NOT NULL,
    requester_name VARCHAR(100),
    requester_department VARCHAR(100),

    -- Warehouse
    warehouse_id BIGINT NOT NULL,

    -- Status
    request_status VARCHAR(30) NOT NULL DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED, ISSUED, COMPLETED, CANCELLED

    -- Approval
    approver_user_id BIGINT,
    approver_name VARCHAR(100),
    approval_date TIMESTAMP,
    approval_remarks TEXT,

    -- Required Date
    required_date DATE,

    -- Priority
    priority VARCHAR(20) DEFAULT 'NORMAL', -- URGENT, HIGH, NORMAL, LOW

    -- Purpose
    purpose VARCHAR(100), -- PRODUCTION, MAINTENANCE, SAMPLE, OTHER

    -- Notes
    remarks TEXT,

    -- Audit Fields
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),

    -- Foreign Keys
    CONSTRAINT fk_material_request_tenant
        FOREIGN KEY (tenant_id) REFERENCES common.sd_tenants(tenant_id) ON DELETE CASCADE,
    CONSTRAINT fk_material_request_work_order
        FOREIGN KEY (work_order_id) REFERENCES mes.sd_work_orders(work_order_id) ON DELETE SET NULL,
    CONSTRAINT fk_material_request_requester
        FOREIGN KEY (requester_user_id) REFERENCES common.sd_users(user_id) ON DELETE RESTRICT,
    CONSTRAINT fk_material_request_approver
        FOREIGN KEY (approver_user_id) REFERENCES common.sd_users(user_id) ON DELETE SET NULL,
    CONSTRAINT fk_material_request_warehouse
        FOREIGN KEY (warehouse_id) REFERENCES inventory.sd_warehouses(warehouse_id) ON DELETE RESTRICT,

    -- Unique Constraint
    CONSTRAINT uk_material_request_no UNIQUE (tenant_id, request_no),

    -- Check Constraints
    CONSTRAINT chk_material_request_status
        CHECK (request_status IN ('PENDING', 'APPROVED', 'REJECTED', 'ISSUED', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT chk_material_request_priority
        CHECK (priority IN ('URGENT', 'HIGH', 'NORMAL', 'LOW')),
    CONSTRAINT chk_material_request_purpose
        CHECK (purpose IN ('PRODUCTION', 'MAINTENANCE', 'SAMPLE', 'OTHER'))
);

-- Indexes
CREATE INDEX idx_material_request_tenant ON wms.sd_material_requests(tenant_id);
CREATE INDEX idx_material_request_date ON wms.sd_material_requests(request_date);
CREATE INDEX idx_material_request_status ON wms.sd_material_requests(request_status);
CREATE INDEX idx_material_request_work_order ON wms.sd_material_requests(work_order_id);
CREATE INDEX idx_material_request_requester ON wms.sd_material_requests(requester_user_id);
CREATE INDEX idx_material_request_warehouse ON wms.sd_material_requests(warehouse_id);
CREATE INDEX idx_material_request_required_date ON wms.sd_material_requests(required_date);
CREATE INDEX idx_material_request_priority ON wms.sd_material_requests(priority);

-- Comments
COMMENT ON TABLE wms.sd_material_requests IS '불출 신청 헤더';
COMMENT ON COLUMN wms.sd_material_requests.request_status IS '신청 상태: PENDING(대기), APPROVED(승인), REJECTED(거부), ISSUED(불출), COMPLETED(완료), CANCELLED(취소)';
COMMENT ON COLUMN wms.sd_material_requests.priority IS '우선순위: URGENT(긴급), HIGH(높음), NORMAL(보통), LOW(낮음)';
COMMENT ON COLUMN wms.sd_material_requests.purpose IS '용도: PRODUCTION(생산), MAINTENANCE(보전), SAMPLE(샘플), OTHER(기타)';

-- ============================================================
-- 2. MATERIAL REQUEST ITEMS (불출 신청 상세)
-- ============================================================

CREATE TABLE wms.sd_material_request_items (
    material_request_item_id BIGSERIAL PRIMARY KEY,
    material_request_id BIGINT NOT NULL,

    -- Product Information
    product_id BIGINT NOT NULL,
    product_code VARCHAR(50),
    product_name VARCHAR(200),

    -- Quantity
    requested_quantity DECIMAL(15,3) NOT NULL,
    approved_quantity DECIMAL(15,3),
    issued_quantity DECIMAL(15,3) DEFAULT 0,
    unit VARCHAR(20),

    -- LOT Specification (optional - for specific LOT request)
    requested_lot_no VARCHAR(100),

    -- Inventory Transaction Link
    inventory_transaction_id BIGINT,

    -- Issue Status
    issue_status VARCHAR(30) DEFAULT 'PENDING', -- PENDING, PARTIAL, COMPLETED, CANCELLED

    -- Notes
    remarks TEXT,

    -- Audit Fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Foreign Keys
    CONSTRAINT fk_material_request_item_header
        FOREIGN KEY (material_request_id) REFERENCES wms.sd_material_requests(material_request_id) ON DELETE CASCADE,
    CONSTRAINT fk_material_request_item_product
        FOREIGN KEY (product_id) REFERENCES mes.sd_products(product_id) ON DELETE RESTRICT,
    CONSTRAINT fk_material_request_item_transaction
        FOREIGN KEY (inventory_transaction_id) REFERENCES inventory.sd_inventory_transactions(inventory_transaction_id) ON DELETE SET NULL,

    -- Check Constraints
    CONSTRAINT chk_material_request_item_quantities
        CHECK (requested_quantity > 0),
    CONSTRAINT chk_material_request_item_issue_status
        CHECK (issue_status IN ('PENDING', 'PARTIAL', 'COMPLETED', 'CANCELLED'))
);

-- Indexes
CREATE INDEX idx_material_request_item_header ON wms.sd_material_request_items(material_request_id);
CREATE INDEX idx_material_request_item_product ON wms.sd_material_request_items(product_id);
CREATE INDEX idx_material_request_item_lot ON wms.sd_material_request_items(requested_lot_no);
CREATE INDEX idx_material_request_item_transaction ON wms.sd_material_request_items(inventory_transaction_id);
CREATE INDEX idx_material_request_item_status ON wms.sd_material_request_items(issue_status);

-- Comments
COMMENT ON TABLE wms.sd_material_request_items IS '불출 신청 상세';
COMMENT ON COLUMN wms.sd_material_request_items.issue_status IS '불출 상태: PENDING(대기), PARTIAL(부분), COMPLETED(완료), CANCELLED(취소)';
COMMENT ON COLUMN wms.sd_material_request_items.requested_lot_no IS '요청 LOT 번호 (특정 LOT 지정 시)';

-- ============================================================
-- 3. MATERIAL HANDOVERS (인수인계)
-- ============================================================

CREATE TABLE wms.sd_material_handovers (
    material_handover_id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,

    -- Reference
    material_request_id BIGINT NOT NULL,
    material_request_item_id BIGINT NOT NULL,
    inventory_transaction_id BIGINT NOT NULL,

    -- Handover Information
    handover_no VARCHAR(50) NOT NULL,
    handover_date TIMESTAMP NOT NULL,

    -- Product and LOT
    product_id BIGINT NOT NULL,
    lot_id BIGINT,
    lot_no VARCHAR(100),
    quantity DECIMAL(15,3) NOT NULL,
    unit VARCHAR(20),

    -- Issuer (출고자 - 창고 담당)
    issuer_user_id BIGINT NOT NULL,
    issuer_name VARCHAR(100),
    issue_location VARCHAR(200),

    -- Receiver (인수자 - 생산 담당)
    receiver_user_id BIGINT NOT NULL,
    receiver_name VARCHAR(100),
    receive_location VARCHAR(200),
    received_date TIMESTAMP,

    -- Status
    handover_status VARCHAR(30) DEFAULT 'PENDING', -- PENDING, CONFIRMED, REJECTED

    -- Confirmation
    confirmation_remarks TEXT,

    -- Notes
    remarks TEXT,

    -- Audit Fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Foreign Keys
    CONSTRAINT fk_material_handover_tenant
        FOREIGN KEY (tenant_id) REFERENCES common.sd_tenants(tenant_id) ON DELETE CASCADE,
    CONSTRAINT fk_material_handover_request
        FOREIGN KEY (material_request_id) REFERENCES wms.sd_material_requests(material_request_id) ON DELETE RESTRICT,
    CONSTRAINT fk_material_handover_request_item
        FOREIGN KEY (material_request_item_id) REFERENCES wms.sd_material_request_items(material_request_item_id) ON DELETE RESTRICT,
    CONSTRAINT fk_material_handover_transaction
        FOREIGN KEY (inventory_transaction_id) REFERENCES inventory.sd_inventory_transactions(inventory_transaction_id) ON DELETE RESTRICT,
    CONSTRAINT fk_material_handover_product
        FOREIGN KEY (product_id) REFERENCES mes.sd_products(product_id) ON DELETE RESTRICT,
    CONSTRAINT fk_material_handover_lot
        FOREIGN KEY (lot_id) REFERENCES inventory.sd_lots(lot_id) ON DELETE SET NULL,
    CONSTRAINT fk_material_handover_issuer
        FOREIGN KEY (issuer_user_id) REFERENCES common.sd_users(user_id) ON DELETE RESTRICT,
    CONSTRAINT fk_material_handover_receiver
        FOREIGN KEY (receiver_user_id) REFERENCES common.sd_users(user_id) ON DELETE RESTRICT,

    -- Unique Constraint
    CONSTRAINT uk_material_handover_no UNIQUE (tenant_id, handover_no),

    -- Check Constraints
    CONSTRAINT chk_material_handover_status
        CHECK (handover_status IN ('PENDING', 'CONFIRMED', 'REJECTED')),
    CONSTRAINT chk_material_handover_quantity
        CHECK (quantity > 0)
);

-- Indexes
CREATE INDEX idx_material_handover_tenant ON wms.sd_material_handovers(tenant_id);
CREATE INDEX idx_material_handover_date ON wms.sd_material_handovers(handover_date);
CREATE INDEX idx_material_handover_request ON wms.sd_material_handovers(material_request_id);
CREATE INDEX idx_material_handover_transaction ON wms.sd_material_handovers(inventory_transaction_id);
CREATE INDEX idx_material_handover_product ON wms.sd_material_handovers(product_id);
CREATE INDEX idx_material_handover_lot ON wms.sd_material_handovers(lot_id);
CREATE INDEX idx_material_handover_issuer ON wms.sd_material_handovers(issuer_user_id);
CREATE INDEX idx_material_handover_receiver ON wms.sd_material_handovers(receiver_user_id);
CREATE INDEX idx_material_handover_status ON wms.sd_material_handovers(handover_status);

-- Comments
COMMENT ON TABLE wms.sd_material_handovers IS '자재 인수인계 기록';
COMMENT ON COLUMN wms.sd_material_handovers.handover_status IS '인수 상태: PENDING(대기), CONFIRMED(확인), REJECTED(거부)';
COMMENT ON COLUMN wms.sd_material_handovers.issuer_user_id IS '출고자 (창고 담당자)';
COMMENT ON COLUMN wms.sd_material_handovers.receiver_user_id IS '인수자 (생산 담당자)';

-- ============================================================
-- Trigger for updating updated_at
-- ============================================================

CREATE TRIGGER trg_material_request_updated_at
    BEFORE UPDATE ON wms.sd_material_requests
    FOR EACH ROW
    EXECUTE FUNCTION common.update_modified_timestamp();

CREATE TRIGGER trg_material_request_item_updated_at
    BEFORE UPDATE ON wms.sd_material_request_items
    FOR EACH ROW
    EXECUTE FUNCTION common.update_modified_timestamp();

CREATE TRIGGER trg_material_handover_updated_at
    BEFORE UPDATE ON wms.sd_material_handovers
    FOR EACH ROW
    EXECUTE FUNCTION common.update_modified_timestamp();

-- ============================================================
-- Grant Permissions
-- ============================================================
-- Note: Permissions are managed at application level
