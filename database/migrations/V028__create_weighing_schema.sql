-- ====================================================================
-- Migration: V028__create_weighing_schema.sql
-- Description: Create Weighing Management Schema for GMP Compliance
-- Author: SoftIce MES Development Team
-- Date: 2026-02-04
-- ====================================================================

-- ====================================================================
-- Table: wms.sd_weighings
-- Description: Weighing records for incoming/outgoing/production materials
--              Supports GMP compliance with dual verification
-- ====================================================================

CREATE TABLE wms.sd_weighings (
    -- Primary Key
    weighing_id BIGSERIAL PRIMARY KEY,

    -- Multi-tenancy
    tenant_id VARCHAR(50) NOT NULL,

    -- Weighing Information
    weighing_no VARCHAR(50) NOT NULL,
    weighing_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    weighing_type VARCHAR(30) NOT NULL, -- INCOMING, OUTGOING, PRODUCTION, SAMPLING

    -- Reference Information (Polymorphic)
    reference_type VARCHAR(50), -- MATERIAL_REQUEST, WORK_ORDER, GOODS_RECEIPT, SHIPPING, QUALITY_INSPECTION
    reference_id BIGINT,

    -- Product/Material Information
    product_id BIGINT NOT NULL,
    lot_id BIGINT,

    -- Weight Measurements
    tare_weight DECIMAL(15,3) NOT NULL,
    gross_weight DECIMAL(15,3) NOT NULL,
    net_weight DECIMAL(15,3) NOT NULL,
    expected_weight DECIMAL(15,3),
    variance DECIMAL(15,3),
    variance_percentage DECIMAL(10,4),
    unit VARCHAR(20) NOT NULL DEFAULT 'kg',

    -- Equipment Information
    scale_id BIGINT,
    scale_name VARCHAR(100),

    -- Personnel Information (GMP Dual Verification)
    operator_user_id BIGINT NOT NULL,
    verifier_user_id BIGINT,
    verification_date TIMESTAMP,
    verification_status VARCHAR(30) NOT NULL DEFAULT 'PENDING', -- PENDING, VERIFIED, REJECTED

    -- Tolerance Control
    tolerance_exceeded BOOLEAN NOT NULL DEFAULT false,
    tolerance_percentage DECIMAL(10,4) DEFAULT 2.0, -- Default 2% tolerance

    -- Additional Information
    remarks TEXT,
    attachments JSONB, -- File attachments

    -- Environmental Conditions (GMP)
    temperature DECIMAL(5,2),
    humidity DECIMAL(5,2),

    -- Audit Fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    deleted_by BIGINT,

    -- Constraints
    CONSTRAINT uk_weighing_no UNIQUE (tenant_id, weighing_no),
    CONSTRAINT chk_weighing_weights CHECK (net_weight = gross_weight - tare_weight),
    CONSTRAINT chk_weighing_type CHECK (weighing_type IN ('INCOMING', 'OUTGOING', 'PRODUCTION', 'SAMPLING')),
    CONSTRAINT chk_verification_status CHECK (verification_status IN ('PENDING', 'VERIFIED', 'REJECTED')),
    CONSTRAINT chk_positive_weights CHECK (tare_weight >= 0 AND gross_weight >= 0 AND net_weight >= 0),
    CONSTRAINT chk_gross_greater_than_tare CHECK (gross_weight >= tare_weight)
);

-- ====================================================================
-- Indexes for Performance
-- ====================================================================

-- Multi-tenancy index
CREATE INDEX idx_weighings_tenant ON wms.sd_weighings(tenant_id) WHERE deleted_at IS NULL;

-- Date range queries
CREATE INDEX idx_weighings_date ON wms.sd_weighings(weighing_date DESC) WHERE deleted_at IS NULL;

-- Reference lookup
CREATE INDEX idx_weighings_reference ON wms.sd_weighings(reference_type, reference_id) WHERE deleted_at IS NULL;

-- Product lookup
CREATE INDEX idx_weighings_product ON wms.sd_weighings(product_id) WHERE deleted_at IS NULL;

-- Lot tracking
CREATE INDEX idx_weighings_lot ON wms.sd_weighings(lot_id) WHERE deleted_at IS NULL AND lot_id IS NOT NULL;

-- Verification workflow
CREATE INDEX idx_weighings_verification ON wms.sd_weighings(verification_status) WHERE deleted_at IS NULL;

-- Tolerance exception management
CREATE INDEX idx_weighings_tolerance ON wms.sd_weighings(tolerance_exceeded) WHERE deleted_at IS NULL AND tolerance_exceeded = true;

-- Personnel tracking
CREATE INDEX idx_weighings_operator ON wms.sd_weighings(operator_user_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_weighings_verifier ON wms.sd_weighings(verifier_user_id) WHERE deleted_at IS NULL AND verifier_user_id IS NOT NULL;

-- Composite index for common queries
CREATE INDEX idx_weighings_tenant_date_type ON wms.sd_weighings(tenant_id, weighing_date DESC, weighing_type) WHERE deleted_at IS NULL;

-- ====================================================================
-- Comments
-- ====================================================================

COMMENT ON TABLE wms.sd_weighings IS 'Weighing records for GMP compliance and material tracking';
COMMENT ON COLUMN wms.sd_weighings.weighing_no IS 'Auto-generated unique weighing number (WG-YYYYMMDD-0001)';
COMMENT ON COLUMN wms.sd_weighings.weighing_type IS 'Type of weighing: INCOMING, OUTGOING, PRODUCTION, SAMPLING';
COMMENT ON COLUMN wms.sd_weighings.reference_type IS 'Polymorphic reference to source document';
COMMENT ON COLUMN wms.sd_weighings.reference_id IS 'ID of source document';
COMMENT ON COLUMN wms.sd_weighings.tare_weight IS 'Weight of empty container (kg)';
COMMENT ON COLUMN wms.sd_weighings.gross_weight IS 'Total weight including container (kg)';
COMMENT ON COLUMN wms.sd_weighings.net_weight IS 'Net material weight (gross - tare)';
COMMENT ON COLUMN wms.sd_weighings.expected_weight IS 'Expected weight from source document';
COMMENT ON COLUMN wms.sd_weighings.variance IS 'Difference between net and expected weight';
COMMENT ON COLUMN wms.sd_weighings.variance_percentage IS 'Variance as percentage of expected weight';
COMMENT ON COLUMN wms.sd_weighings.operator_user_id IS 'User who performed weighing';
COMMENT ON COLUMN wms.sd_weighings.verifier_user_id IS 'User who verified weighing (GMP dual verification)';
COMMENT ON COLUMN wms.sd_weighings.verification_status IS 'Verification status: PENDING, VERIFIED, REJECTED';
COMMENT ON COLUMN wms.sd_weighings.tolerance_exceeded IS 'Flag indicating if variance exceeds tolerance';
COMMENT ON COLUMN wms.sd_weighings.tolerance_percentage IS 'Acceptable variance percentage (default 2%)';

-- ====================================================================
-- Sample Data for Testing
-- ====================================================================

-- Insert sample weighing records for tenant1
INSERT INTO wms.sd_weighings (
    tenant_id, weighing_no, weighing_date, weighing_type,
    reference_type, reference_id,
    product_id, lot_id,
    tare_weight, gross_weight, net_weight,
    expected_weight, variance, variance_percentage,
    unit,
    operator_user_id, verifier_user_id, verification_date, verification_status,
    tolerance_exceeded, tolerance_percentage,
    remarks,
    created_by
) VALUES
-- Incoming material weighing
(
    'tenant1', 'WG-20260204-0001', '2026-02-04 09:15:00', 'INCOMING',
    'GOODS_RECEIPT', 1,
    1, NULL,
    50.000, 1050.000, 1000.000,
    1000.000, 0.000, 0.0000,
    'kg',
    1, 2, '2026-02-04 09:20:00', 'VERIFIED',
    false, 2.0,
    'Raw material receipt weighing - within tolerance',
    1
),
-- Production weighing with variance
(
    'tenant1', 'WG-20260204-0002', '2026-02-04 10:30:00', 'PRODUCTION',
    'WORK_ORDER', 1,
    2, NULL,
    100.000, 550.000, 450.000,
    500.000, -50.000, -10.0000,
    'kg',
    1, 2, '2026-02-04 10:35:00', 'VERIFIED',
    true, 2.0,
    'Production weighing - variance exceeds tolerance, investigate material loss',
    1
),
-- Outgoing weighing for shipping
(
    'tenant1', 'WG-20260204-0003', '2026-02-04 14:00:00', 'OUTGOING',
    'SHIPPING', 1,
    3, NULL,
    30.000, 530.000, 500.000,
    500.000, 0.000, 0.0000,
    'kg',
    1, 2, '2026-02-04 14:05:00', 'VERIFIED',
    false, 2.0,
    'Finished goods shipping weighing - exact match',
    1
),
-- Sampling weighing (pending verification)
(
    'tenant1', 'WG-20260204-0004', '2026-02-04 15:30:00', 'SAMPLING',
    'QUALITY_INSPECTION', 1,
    1, NULL,
    5.000, 10.500, 5.500,
    5.000, 0.500, 10.0000,
    'kg',
    1, NULL, NULL, 'PENDING',
    true, 2.0,
    'Quality sample - awaiting verification',
    1
);

-- ====================================================================
-- End of Migration
-- ====================================================================
