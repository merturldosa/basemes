-- ============================================================================
-- Migration V005: BOM (Bill of Materials) Schema
-- BOM 관리 스키마
-- Author: Moon Myung-seop
-- Created: 2026-01-23
-- ============================================================================

-- Create BOM schema
CREATE SCHEMA IF NOT EXISTS bom;

-- ============================================================================
-- Table: bom.sd_boms
-- BOM 마스터 (제품별 자재 구성 정보)
-- ============================================================================
CREATE TABLE bom.sd_boms (
    bom_id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    product_id BIGINT NOT NULL,
    bom_code VARCHAR(50) NOT NULL,
    bom_name VARCHAR(200) NOT NULL,
    version VARCHAR(20) NOT NULL DEFAULT '1.0',
    effective_date DATE NOT NULL,
    expiry_date DATE,
    is_active BOOLEAN NOT NULL DEFAULT true,
    remarks TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign Keys
    CONSTRAINT fk_bom_tenant FOREIGN KEY (tenant_id)
        REFERENCES core.sd_tenants(tenant_id) ON DELETE CASCADE,
    CONSTRAINT fk_bom_product FOREIGN KEY (product_id)
        REFERENCES production.sd_products(product_id) ON DELETE RESTRICT,

    -- Unique Constraints
    CONSTRAINT uk_bom_code_version UNIQUE (tenant_id, bom_code, version)
);

-- Indexes for sd_boms
CREATE INDEX idx_bom_tenant ON bom.sd_boms(tenant_id);
CREATE INDEX idx_bom_product ON bom.sd_boms(product_id);
CREATE INDEX idx_bom_effective_date ON bom.sd_boms(effective_date);
CREATE INDEX idx_bom_active ON bom.sd_boms(is_active);

-- Comments for sd_boms
COMMENT ON TABLE bom.sd_boms IS 'BOM 마스터 - 제품별 자재 구성 정보';
COMMENT ON COLUMN bom.sd_boms.bom_id IS 'BOM ID (Primary Key)';
COMMENT ON COLUMN bom.sd_boms.tenant_id IS '테넌트 ID';
COMMENT ON COLUMN bom.sd_boms.product_id IS '제품 ID (완제품)';
COMMENT ON COLUMN bom.sd_boms.bom_code IS 'BOM 코드';
COMMENT ON COLUMN bom.sd_boms.bom_name IS 'BOM 명';
COMMENT ON COLUMN bom.sd_boms.version IS '버전';
COMMENT ON COLUMN bom.sd_boms.effective_date IS '유효 시작일';
COMMENT ON COLUMN bom.sd_boms.expiry_date IS '유효 종료일';
COMMENT ON COLUMN bom.sd_boms.is_active IS '활성 여부';
COMMENT ON COLUMN bom.sd_boms.remarks IS '비고';

-- ============================================================================
-- Table: bom.sd_bom_details
-- BOM 상세 (자재별 소요량 정보)
-- ============================================================================
CREATE TABLE bom.sd_bom_details (
    bom_detail_id BIGSERIAL PRIMARY KEY,
    bom_id BIGINT NOT NULL,
    sequence INTEGER NOT NULL,
    material_product_id BIGINT NOT NULL,
    process_id BIGINT,
    quantity DECIMAL(15,3) NOT NULL,
    unit VARCHAR(20) NOT NULL,
    usage_rate DECIMAL(5,2) DEFAULT 100.00,
    scrap_rate DECIMAL(5,2) DEFAULT 0.00,
    remarks TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign Keys
    CONSTRAINT fk_bom_detail_bom FOREIGN KEY (bom_id)
        REFERENCES bom.sd_boms(bom_id) ON DELETE CASCADE,
    CONSTRAINT fk_bom_detail_material FOREIGN KEY (material_product_id)
        REFERENCES production.sd_products(product_id) ON DELETE RESTRICT,
    CONSTRAINT fk_bom_detail_process FOREIGN KEY (process_id)
        REFERENCES production.sd_processes(process_id) ON DELETE SET NULL,

    -- Unique Constraints
    CONSTRAINT uk_bom_detail_sequence UNIQUE (bom_id, sequence),

    -- Check Constraints
    CONSTRAINT chk_bom_detail_quantity CHECK (quantity > 0),
    CONSTRAINT chk_bom_detail_usage_rate CHECK (usage_rate >= 0 AND usage_rate <= 100),
    CONSTRAINT chk_bom_detail_scrap_rate CHECK (scrap_rate >= 0 AND scrap_rate <= 100)
);

-- Indexes for sd_bom_details
CREATE INDEX idx_bom_detail_bom ON bom.sd_bom_details(bom_id);
CREATE INDEX idx_bom_detail_material ON bom.sd_bom_details(material_product_id);
CREATE INDEX idx_bom_detail_process ON bom.sd_bom_details(process_id);

-- Comments for sd_bom_details
COMMENT ON TABLE bom.sd_bom_details IS 'BOM 상세 - 자재별 소요량 정보';
COMMENT ON COLUMN bom.sd_bom_details.bom_detail_id IS 'BOM 상세 ID (Primary Key)';
COMMENT ON COLUMN bom.sd_bom_details.bom_id IS 'BOM ID';
COMMENT ON COLUMN bom.sd_bom_details.sequence IS '순서';
COMMENT ON COLUMN bom.sd_bom_details.material_product_id IS '자재 제품 ID';
COMMENT ON COLUMN bom.sd_bom_details.process_id IS '공정 ID (선택)';
COMMENT ON COLUMN bom.sd_bom_details.quantity IS '소요량';
COMMENT ON COLUMN bom.sd_bom_details.unit IS '단위';
COMMENT ON COLUMN bom.sd_bom_details.usage_rate IS '사용율 (%)';
COMMENT ON COLUMN bom.sd_bom_details.scrap_rate IS '스크랩율 (%)';
COMMENT ON COLUMN bom.sd_bom_details.remarks IS '비고';

-- ============================================================================
-- Triggers for automatic updated_at
-- ============================================================================

-- Trigger for sd_boms
CREATE OR REPLACE FUNCTION bom.update_sd_boms_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_sd_boms_updated_at
    BEFORE UPDATE ON bom.sd_boms
    FOR EACH ROW
    EXECUTE FUNCTION bom.update_sd_boms_updated_at();

-- Trigger for sd_bom_details
CREATE OR REPLACE FUNCTION bom.update_sd_bom_details_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_sd_bom_details_updated_at
    BEFORE UPDATE ON bom.sd_bom_details
    FOR EACH ROW
    EXECUTE FUNCTION bom.update_sd_bom_details_updated_at();

-- ============================================================================
-- End of Migration V005
-- ============================================================================
