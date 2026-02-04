-- ============================================================
-- Migration: V012 - Defect Management Schema
-- Description: 불량/A/S/클레임 관리 스키마 생성
-- Author: Moon Myung-seop
-- Date: 2026-01-23
-- ============================================================

-- Create QMS schema if not exists (for defects)
CREATE SCHEMA IF NOT EXISTS qms;

-- ============================================================
-- 1. DEFECTS (불량)
-- ============================================================

CREATE TABLE qms.si_defects (
    defect_id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    defect_no VARCHAR(50) NOT NULL,
    defect_date TIMESTAMP NOT NULL,

    -- Source (어디서 발생했는지)
    source_type VARCHAR(30) NOT NULL, -- PRODUCTION, RECEIVING, SHIPPING, INSPECTION, CUSTOMER
    work_order_id BIGINT,
    work_result_id BIGINT,
    goods_receipt_id BIGINT,
    shipping_id BIGINT,
    quality_inspection_id BIGINT,

    -- Product
    product_id BIGINT NOT NULL,
    product_code VARCHAR(50),
    product_name VARCHAR(200),

    -- Defect Details
    defect_type VARCHAR(50), -- APPEARANCE, DIMENSION, FUNCTION, MATERIAL, ASSEMBLY, OTHER
    defect_category VARCHAR(50), -- SCRATCH, CRACK, BURR, DEFORMATION, etc
    defect_location VARCHAR(200),
    defect_description TEXT,
    defect_quantity DECIMAL(15,3),

    -- LOT
    lot_no VARCHAR(100),

    -- Severity
    severity VARCHAR(30), -- CRITICAL, MAJOR, MINOR

    -- Status & Action
    status VARCHAR(30) NOT NULL DEFAULT 'REPORTED', -- REPORTED, IN_REVIEW, REWORK, SCRAP, CLOSED
    responsible_department_id BIGINT,
    responsible_user_id BIGINT,
    root_cause TEXT,
    corrective_action TEXT,
    preventive_action TEXT,
    action_date TIMESTAMP,

    -- Reporter
    reporter_user_id BIGINT,
    reporter_name VARCHAR(100),

    -- Cost
    defect_cost DECIMAL(15,2),

    -- Additional
    remarks TEXT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),

    -- Foreign Keys
    CONSTRAINT fk_defect_tenant FOREIGN KEY (tenant_id) REFERENCES core.si_tenants(tenant_id),
    CONSTRAINT fk_defect_work_order FOREIGN KEY (work_order_id) REFERENCES production.si_work_orders(work_order_id) ON DELETE SET NULL,
    CONSTRAINT fk_defect_work_result FOREIGN KEY (work_result_id) REFERENCES production.si_work_results(work_result_id) ON DELETE SET NULL,
    CONSTRAINT fk_defect_goods_receipt FOREIGN KEY (goods_receipt_id) REFERENCES wms.si_goods_receipts(goods_receipt_id) ON DELETE SET NULL,
    CONSTRAINT fk_defect_shipping FOREIGN KEY (shipping_id) REFERENCES wms.si_shippings(shipping_id) ON DELETE SET NULL,
    CONSTRAINT fk_defect_quality_inspection FOREIGN KEY (quality_inspection_id) REFERENCES qms.si_quality_inspections(quality_inspection_id) ON DELETE SET NULL,
    CONSTRAINT fk_defect_product FOREIGN KEY (product_id) REFERENCES production.si_products(product_id),
    CONSTRAINT fk_defect_department FOREIGN KEY (responsible_department_id) REFERENCES core.si_departments(department_id) ON DELETE SET NULL,
    CONSTRAINT fk_defect_responsible_user FOREIGN KEY (responsible_user_id) REFERENCES core.si_users(user_id) ON DELETE SET NULL,
    CONSTRAINT fk_defect_reporter FOREIGN KEY (reporter_user_id) REFERENCES core.si_users(user_id) ON DELETE SET NULL,

    -- Unique Constraint
    CONSTRAINT uk_defect_no UNIQUE (tenant_id, defect_no)
);

-- Indexes
CREATE INDEX idx_defect_tenant ON qms.si_defects(tenant_id);
CREATE INDEX idx_defect_date ON qms.si_defects(defect_date);
CREATE INDEX idx_defect_status ON qms.si_defects(status);
CREATE INDEX idx_defect_product ON qms.si_defects(product_id);
CREATE INDEX idx_defect_lot ON qms.si_defects(lot_no);
CREATE INDEX idx_defect_type ON qms.si_defects(defect_type);

-- Comments
COMMENT ON TABLE qms.si_defects IS '불량 관리';
COMMENT ON COLUMN qms.si_defects.source_type IS '발생 원천: PRODUCTION(생산), RECEIVING(입하), SHIPPING(출하), INSPECTION(검사), CUSTOMER(고객)';
COMMENT ON COLUMN qms.si_defects.status IS '상태: REPORTED(보고), IN_REVIEW(검토중), REWORK(재작업), SCRAP(폐기), CLOSED(완료)';

-- ============================================================
-- 2. AFTER SALES (A/S)
-- ============================================================

CREATE TABLE qms.si_after_sales (
    after_sales_id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    as_no VARCHAR(50) NOT NULL,
    receipt_date TIMESTAMP NOT NULL,

    -- Customer
    customer_id BIGINT NOT NULL,
    customer_code VARCHAR(50),
    customer_name VARCHAR(200),
    contact_person VARCHAR(100),
    contact_phone VARCHAR(50),
    contact_email VARCHAR(100),

    -- Product
    product_id BIGINT NOT NULL,
    product_code VARCHAR(50),
    product_name VARCHAR(200),
    serial_no VARCHAR(100),
    lot_no VARCHAR(100),

    -- Sales Info
    sales_order_id BIGINT,
    sales_order_no VARCHAR(50),
    shipping_id BIGINT,
    purchase_date DATE,
    warranty_status VARCHAR(30), -- IN_WARRANTY, OUT_OF_WARRANTY, EXTENDED

    -- Issue Details
    issue_category VARCHAR(50), -- DEFECT, BREAKDOWN, INSTALLATION, USAGE, OTHER
    issue_description TEXT NOT NULL,
    symptom TEXT,

    -- Service
    service_type VARCHAR(30), -- REPAIR, REPLACEMENT, REFUND, TECHNICAL_SUPPORT
    service_status VARCHAR(30) NOT NULL DEFAULT 'RECEIVED', -- RECEIVED, IN_PROGRESS, COMPLETED, CLOSED, CANCELLED
    priority VARCHAR(30), -- URGENT, HIGH, NORMAL, LOW

    -- Assignment
    assigned_engineer_id BIGINT,
    assigned_engineer_name VARCHAR(100),
    assigned_date TIMESTAMP,

    -- Service Details
    diagnosis TEXT,
    service_action TEXT,
    parts_replaced TEXT,
    service_start_date TIMESTAMP,
    service_end_date TIMESTAMP,

    -- Cost
    service_cost DECIMAL(15,2),
    parts_cost DECIMAL(15,2),
    total_cost DECIMAL(15,2),
    charge_to_customer DECIMAL(15,2),

    -- Resolution
    resolution_description TEXT,
    customer_satisfaction VARCHAR(30), -- VERY_SATISFIED, SATISFIED, NEUTRAL, DISSATISFIED, VERY_DISSATISFIED

    -- Additional
    remarks TEXT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),

    -- Foreign Keys
    CONSTRAINT fk_after_sales_tenant FOREIGN KEY (tenant_id) REFERENCES core.si_tenants(tenant_id),
    CONSTRAINT fk_after_sales_customer FOREIGN KEY (customer_id) REFERENCES business.si_customers(customer_id),
    CONSTRAINT fk_after_sales_product FOREIGN KEY (product_id) REFERENCES production.si_products(product_id),
    CONSTRAINT fk_after_sales_sales_order FOREIGN KEY (sales_order_id) REFERENCES sales.si_sales_orders(sales_order_id) ON DELETE SET NULL,
    CONSTRAINT fk_after_sales_shipping FOREIGN KEY (shipping_id) REFERENCES wms.si_shippings(shipping_id) ON DELETE SET NULL,
    CONSTRAINT fk_after_sales_engineer FOREIGN KEY (assigned_engineer_id) REFERENCES core.si_users(user_id) ON DELETE SET NULL,

    -- Unique Constraint
    CONSTRAINT uk_after_sales_no UNIQUE (tenant_id, as_no)
);

-- Indexes
CREATE INDEX idx_after_sales_tenant ON qms.si_after_sales(tenant_id);
CREATE INDEX idx_after_sales_date ON qms.si_after_sales(receipt_date);
CREATE INDEX idx_after_sales_status ON qms.si_after_sales(service_status);
CREATE INDEX idx_after_sales_customer ON qms.si_after_sales(customer_id);
CREATE INDEX idx_after_sales_product ON qms.si_after_sales(product_id);
CREATE INDEX idx_after_sales_priority ON qms.si_after_sales(priority);

-- Comments
COMMENT ON TABLE qms.si_after_sales IS 'A/S 관리';
COMMENT ON COLUMN qms.si_after_sales.service_status IS '서비스 상태: RECEIVED(접수), IN_PROGRESS(진행중), COMPLETED(완료), CLOSED(종료), CANCELLED(취소)';
COMMENT ON COLUMN qms.si_after_sales.warranty_status IS '보증 상태: IN_WARRANTY(보증기간내), OUT_OF_WARRANTY(보증기간외), EXTENDED(연장보증)';

-- ============================================================
-- 3. CLAIMS (클레임)
-- ============================================================

CREATE TABLE qms.si_claims (
    claim_id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    claim_no VARCHAR(50) NOT NULL,
    claim_date TIMESTAMP NOT NULL,

    -- Customer
    customer_id BIGINT NOT NULL,
    customer_code VARCHAR(50),
    customer_name VARCHAR(200),
    contact_person VARCHAR(100),
    contact_phone VARCHAR(50),
    contact_email VARCHAR(100),

    -- Product
    product_id BIGINT,
    product_code VARCHAR(50),
    product_name VARCHAR(200),
    lot_no VARCHAR(100),

    -- Sales Info
    sales_order_id BIGINT,
    sales_order_no VARCHAR(50),
    shipping_id BIGINT,

    -- Claim Details
    claim_type VARCHAR(50), -- QUALITY, DELIVERY, QUANTITY, PACKAGING, DOCUMENTATION, SERVICE, PRICE, OTHER
    claim_category VARCHAR(50), -- DEFECT, DAMAGE, SHORTAGE, DELAY, MISMATCH, etc
    claim_description TEXT NOT NULL,
    claimed_quantity DECIMAL(15,3),
    claimed_amount DECIMAL(15,2),

    -- Severity & Priority
    severity VARCHAR(30), -- CRITICAL, MAJOR, MINOR
    priority VARCHAR(30), -- URGENT, HIGH, NORMAL, LOW

    -- Status
    status VARCHAR(30) NOT NULL DEFAULT 'RECEIVED', -- RECEIVED, INVESTIGATING, IN_PROGRESS, RESOLVED, CLOSED, REJECTED

    -- Assignment
    responsible_department_id BIGINT,
    responsible_user_id BIGINT,
    assigned_date TIMESTAMP,

    -- Investigation
    investigation_findings TEXT,
    root_cause_analysis TEXT,

    -- Resolution
    resolution_type VARCHAR(50), -- REPLACEMENT, REFUND, DISCOUNT, REWORK, APOLOGY, NO_ACTION
    resolution_description TEXT,
    resolution_amount DECIMAL(15,2),
    resolution_date TIMESTAMP,

    -- Action Plan
    corrective_action TEXT,
    preventive_action TEXT,
    action_completion_date TIMESTAMP,

    -- Customer Response
    customer_acceptance VARCHAR(30), -- ACCEPTED, PARTIALLY_ACCEPTED, REJECTED, PENDING
    customer_feedback TEXT,

    -- Cost
    claim_cost DECIMAL(15,2),
    compensation_amount DECIMAL(15,2),

    -- Additional
    remarks TEXT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),

    -- Foreign Keys
    CONSTRAINT fk_claim_tenant FOREIGN KEY (tenant_id) REFERENCES core.si_tenants(tenant_id),
    CONSTRAINT fk_claim_customer FOREIGN KEY (customer_id) REFERENCES business.si_customers(customer_id),
    CONSTRAINT fk_claim_product FOREIGN KEY (product_id) REFERENCES production.si_products(product_id) ON DELETE SET NULL,
    CONSTRAINT fk_claim_sales_order FOREIGN KEY (sales_order_id) REFERENCES sales.si_sales_orders(sales_order_id) ON DELETE SET NULL,
    CONSTRAINT fk_claim_shipping FOREIGN KEY (shipping_id) REFERENCES wms.si_shippings(shipping_id) ON DELETE SET NULL,
    CONSTRAINT fk_claim_department FOREIGN KEY (responsible_department_id) REFERENCES core.si_departments(department_id) ON DELETE SET NULL,
    CONSTRAINT fk_claim_responsible_user FOREIGN KEY (responsible_user_id) REFERENCES core.si_users(user_id) ON DELETE SET NULL,

    -- Unique Constraint
    CONSTRAINT uk_claim_no UNIQUE (tenant_id, claim_no)
);

-- Indexes
CREATE INDEX idx_claim_tenant ON qms.si_claims(tenant_id);
CREATE INDEX idx_claim_date ON qms.si_claims(claim_date);
CREATE INDEX idx_claim_status ON qms.si_claims(status);
CREATE INDEX idx_claim_customer ON qms.si_claims(customer_id);
CREATE INDEX idx_claim_product ON qms.si_claims(product_id);
CREATE INDEX idx_claim_type ON qms.si_claims(claim_type);
CREATE INDEX idx_claim_priority ON qms.si_claims(priority);

-- Comments
COMMENT ON TABLE qms.si_claims IS '클레임 관리';
COMMENT ON COLUMN qms.si_claims.claim_type IS '클레임 유형: QUALITY(품질), DELIVERY(납기), QUANTITY(수량), PACKAGING(포장), DOCUMENTATION(문서), SERVICE(서비스), PRICE(가격), OTHER(기타)';
COMMENT ON COLUMN qms.si_claims.status IS '상태: RECEIVED(접수), INVESTIGATING(조사중), IN_PROGRESS(처리중), RESOLVED(해결), CLOSED(종료), REJECTED(거부)';

-- ============================================================
-- 4. TRIGGERS - Auto update timestamp
-- ============================================================

-- Defects
CREATE OR REPLACE FUNCTION qms.update_defect_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_defect_updated_at
    BEFORE UPDATE ON qms.si_defects
    FOR EACH ROW
    EXECUTE FUNCTION qms.update_defect_updated_at();

-- After Sales
CREATE OR REPLACE FUNCTION qms.update_after_sales_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_after_sales_updated_at
    BEFORE UPDATE ON qms.si_after_sales
    FOR EACH ROW
    EXECUTE FUNCTION qms.update_after_sales_updated_at();

-- Claims
CREATE OR REPLACE FUNCTION qms.update_claim_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_claim_updated_at
    BEFORE UPDATE ON qms.si_claims
    FOR EACH ROW
    EXECUTE FUNCTION qms.update_claim_updated_at();

-- ============================================================
-- End of Migration V012
-- ============================================================
