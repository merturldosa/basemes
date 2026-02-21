-- =============================================================================
-- Migration V003: Create QMS (Quality Management System) Schema
-- =============================================================================
-- Description: Creates quality management tables for quality standards and inspections
-- Author: 문명섭 (Moon Myeong-seop)
-- Company: (주)스마트도킹스테이션 (SmartDockingStation Co., Ltd.)
-- Date: 2026-01-23
-- =============================================================================

-- Create QMS schema
CREATE SCHEMA IF NOT EXISTS qms;

-- =============================================================================
-- Table: qms.sd_quality_standards
-- Description: Quality criteria master data per product
-- =============================================================================
CREATE TABLE qms.sd_quality_standards (
    quality_standard_id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    product_id BIGINT NOT NULL,

    -- Identification
    standard_code VARCHAR(50) NOT NULL,
    standard_name VARCHAR(200) NOT NULL,
    standard_version VARCHAR(20) NOT NULL DEFAULT '1.0',

    -- Inspection Configuration
    inspection_type VARCHAR(20) NOT NULL, -- INCOMING, IN_PROCESS, OUTGOING, FINAL
    inspection_method VARCHAR(100),

    -- Quality Criteria
    min_value DECIMAL(15,3),
    max_value DECIMAL(15,3),
    target_value DECIMAL(15,3),
    tolerance_value DECIMAL(15,3),
    unit VARCHAR(20),

    -- Measurement
    measurement_item VARCHAR(200),
    measurement_equipment VARCHAR(100),

    -- Sampling
    sampling_method VARCHAR(100),
    sample_size INTEGER,

    -- Status and Validity
    is_active BOOLEAN NOT NULL DEFAULT true,
    effective_date DATE NOT NULL,
    expiry_date DATE,

    -- Additional Information
    remarks TEXT,

    -- Audit Fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign Keys
    CONSTRAINT fk_quality_standard_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES common.sd_tenants(tenant_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_quality_standard_product
        FOREIGN KEY (product_id)
        REFERENCES mes.sd_products(product_id)
        ON DELETE CASCADE,

    -- Unique Constraints
    CONSTRAINT uq_quality_standard_code
        UNIQUE (tenant_id, standard_code, standard_version),

    -- Check Constraints
    CONSTRAINT chk_inspection_type
        CHECK (inspection_type IN ('INCOMING', 'IN_PROCESS', 'OUTGOING', 'FINAL')),
    CONSTRAINT chk_effective_dates
        CHECK (expiry_date IS NULL OR expiry_date > effective_date)
);

-- Indexes for sd_quality_standards
CREATE INDEX idx_quality_standard_tenant ON qms.sd_quality_standards(tenant_id);
CREATE INDEX idx_quality_standard_product ON qms.sd_quality_standards(product_id);
CREATE INDEX idx_quality_standard_code ON qms.sd_quality_standards(standard_code);
CREATE INDEX idx_quality_standard_type ON qms.sd_quality_standards(inspection_type);
CREATE INDEX idx_quality_standard_active ON qms.sd_quality_standards(is_active);
CREATE INDEX idx_quality_standard_effective ON qms.sd_quality_standards(effective_date);

-- Trigger for updating updated_at on sd_quality_standards
CREATE TRIGGER trg_quality_standard_updated_at
    BEFORE UPDATE ON qms.sd_quality_standards
    FOR EACH ROW
    EXECUTE FUNCTION common.update_modified_timestamp();

-- =============================================================================
-- Table: qms.sd_quality_inspections
-- Description: Quality inspection records
-- =============================================================================
CREATE TABLE qms.sd_quality_inspections (
    quality_inspection_id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    quality_standard_id BIGINT NOT NULL,

    -- Optional References
    work_order_id BIGINT,
    work_result_id BIGINT,

    -- Product Information
    product_id BIGINT NOT NULL,

    -- Inspection Identification
    inspection_no VARCHAR(50) NOT NULL,
    inspection_date TIMESTAMP NOT NULL,
    inspection_type VARCHAR(20) NOT NULL, -- INCOMING, IN_PROCESS, OUTGOING, FINAL

    -- Inspector
    inspector_user_id BIGINT NOT NULL,

    -- Quantities
    inspected_quantity DECIMAL(15,3) NOT NULL DEFAULT 0,
    passed_quantity DECIMAL(15,3) NOT NULL DEFAULT 0,
    failed_quantity DECIMAL(15,3) NOT NULL DEFAULT 0,

    -- Measurement
    measured_value DECIMAL(15,3),
    measurement_unit VARCHAR(20),

    -- Result
    inspection_result VARCHAR(20) NOT NULL, -- PASS, FAIL, CONDITIONAL

    -- Defect Information
    defect_type VARCHAR(100),
    defect_reason TEXT,
    defect_location VARCHAR(200),

    -- Corrective Action
    corrective_action TEXT,
    corrective_action_date DATE,

    -- Additional Information
    remarks TEXT,

    -- Audit Fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign Keys
    CONSTRAINT fk_quality_inspection_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES common.sd_tenants(tenant_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_quality_inspection_standard
        FOREIGN KEY (quality_standard_id)
        REFERENCES qms.sd_quality_standards(quality_standard_id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_quality_inspection_work_order
        FOREIGN KEY (work_order_id)
        REFERENCES mes.sd_work_orders(work_order_id)
        ON DELETE SET NULL,
    CONSTRAINT fk_quality_inspection_work_result
        FOREIGN KEY (work_result_id)
        REFERENCES mes.sd_work_results(work_result_id)
        ON DELETE SET NULL,
    CONSTRAINT fk_quality_inspection_product
        FOREIGN KEY (product_id)
        REFERENCES mes.sd_products(product_id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_quality_inspection_inspector
        FOREIGN KEY (inspector_user_id)
        REFERENCES common.sd_users(user_id)
        ON DELETE RESTRICT,

    -- Unique Constraints
    CONSTRAINT uq_quality_inspection_no
        UNIQUE (tenant_id, inspection_no),

    -- Check Constraints
    CONSTRAINT chk_quality_inspection_type
        CHECK (inspection_type IN ('INCOMING', 'IN_PROCESS', 'OUTGOING', 'FINAL')),
    CONSTRAINT chk_quality_inspection_result
        CHECK (inspection_result IN ('PASS', 'FAIL', 'CONDITIONAL')),
    CONSTRAINT chk_quality_inspection_quantities
        CHECK (inspected_quantity >= 0 AND passed_quantity >= 0 AND failed_quantity >= 0),
    CONSTRAINT chk_quality_inspection_total
        CHECK (passed_quantity + failed_quantity <= inspected_quantity)
);

-- Indexes for sd_quality_inspections
CREATE INDEX idx_quality_inspection_tenant ON qms.sd_quality_inspections(tenant_id);
CREATE INDEX idx_quality_inspection_standard ON qms.sd_quality_inspections(quality_standard_id);
CREATE INDEX idx_quality_inspection_work_order ON qms.sd_quality_inspections(work_order_id);
CREATE INDEX idx_quality_inspection_work_result ON qms.sd_quality_inspections(work_result_id);
CREATE INDEX idx_quality_inspection_product ON qms.sd_quality_inspections(product_id);
CREATE INDEX idx_quality_inspection_inspector ON qms.sd_quality_inspections(inspector_user_id);
CREATE INDEX idx_quality_inspection_no ON qms.sd_quality_inspections(inspection_no);
CREATE INDEX idx_quality_inspection_date ON qms.sd_quality_inspections(inspection_date);
CREATE INDEX idx_quality_inspection_type ON qms.sd_quality_inspections(inspection_type);
CREATE INDEX idx_quality_inspection_result ON qms.sd_quality_inspections(inspection_result);

-- Trigger for updating updated_at on sd_quality_inspections
CREATE TRIGGER trg_quality_inspection_updated_at
    BEFORE UPDATE ON qms.sd_quality_inspections
    FOR EACH ROW
    EXECUTE FUNCTION common.update_modified_timestamp();

-- =============================================================================
-- Comments
-- =============================================================================
COMMENT ON SCHEMA qms IS 'Quality Management System schema';

COMMENT ON TABLE qms.sd_quality_standards IS 'Quality criteria master data per product';
COMMENT ON COLUMN qms.sd_quality_standards.inspection_type IS 'INCOMING: 입고검사, IN_PROCESS: 공정검사, OUTGOING: 출하검사, FINAL: 최종검사';
COMMENT ON COLUMN qms.sd_quality_standards.is_active IS 'Whether this quality standard is currently active';
COMMENT ON COLUMN qms.sd_quality_standards.effective_date IS 'Date when this standard becomes effective';
COMMENT ON COLUMN qms.sd_quality_standards.expiry_date IS 'Date when this standard expires (null if no expiry)';

COMMENT ON TABLE qms.sd_quality_inspections IS 'Quality inspection records';
COMMENT ON COLUMN qms.sd_quality_inspections.inspection_result IS 'PASS: 합격, FAIL: 불합격, CONDITIONAL: 조건부';
COMMENT ON COLUMN qms.sd_quality_inspections.work_order_id IS 'Optional link to work order';
COMMENT ON COLUMN qms.sd_quality_inspections.work_result_id IS 'Optional link to work result for in-process inspections';

-- =============================================================================
-- Grant Permissions
-- =============================================================================
-- Note: Permissions are managed at application level
