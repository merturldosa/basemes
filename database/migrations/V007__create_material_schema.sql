-- ============================================================================
-- Material Management Schema
-- 자재/원자재 관리 스키마
-- Author: Moon Myung-seop
-- Date: 2026-01-23
-- ============================================================================

-- Create material schema
CREATE SCHEMA IF NOT EXISTS material;

-- ============================================================================
-- Table: material.si_materials
-- Description: 자재 마스터 (Material Master)
-- ============================================================================
CREATE TABLE material.si_materials (
    material_id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    material_code VARCHAR(50) NOT NULL,
    material_name VARCHAR(200) NOT NULL,
    material_type VARCHAR(30) NOT NULL,  -- RAW_MATERIAL, SUB_MATERIAL, SEMI_FINISHED, FINISHED_PRODUCT
    specification VARCHAR(500),
    model VARCHAR(100),
    unit VARCHAR(20) NOT NULL,  -- EA, KG, L, M, etc.

    -- Pricing
    standard_price NUMERIC(15, 2),
    current_price NUMERIC(15, 2),
    currency VARCHAR(10),  -- KRW, USD, EUR, JPY, CNY

    -- Supplier
    supplier_id BIGINT,
    lead_time_days INTEGER,

    -- Stock Management
    min_stock_quantity NUMERIC(15, 3),
    max_stock_quantity NUMERIC(15, 3),
    safety_stock_quantity NUMERIC(15, 3),
    reorder_point NUMERIC(15, 3),

    -- Storage
    storage_location VARCHAR(100),

    -- LOT Management
    lot_managed BOOLEAN DEFAULT FALSE,
    shelf_life_days INTEGER,

    -- Status
    is_active BOOLEAN DEFAULT TRUE,

    -- Additional Info
    remarks TEXT,

    -- Audit Fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Foreign Keys
    CONSTRAINT fk_material_tenant FOREIGN KEY (tenant_id)
        REFERENCES core.si_tenants(tenant_id) ON DELETE CASCADE,
    CONSTRAINT fk_material_supplier FOREIGN KEY (supplier_id)
        REFERENCES business.si_suppliers(supplier_id) ON DELETE SET NULL,

    -- Unique Constraints
    CONSTRAINT uk_material_code UNIQUE (tenant_id, material_code)
);

-- ============================================================================
-- Indexes
-- ============================================================================
CREATE INDEX idx_material_tenant ON material.si_materials(tenant_id);
CREATE INDEX idx_material_type ON material.si_materials(material_type);
CREATE INDEX idx_material_supplier ON material.si_materials(supplier_id);
CREATE INDEX idx_material_active ON material.si_materials(is_active);
CREATE INDEX idx_material_name ON material.si_materials(material_name);

-- ============================================================================
-- Triggers for updated_at
-- ============================================================================
CREATE OR REPLACE FUNCTION material.update_material_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_material_timestamp
    BEFORE UPDATE ON material.si_materials
    FOR EACH ROW
    EXECUTE FUNCTION material.update_material_timestamp();

-- ============================================================================
-- Comments
-- ============================================================================
COMMENT ON SCHEMA material IS '자재 관리 스키마';
COMMENT ON TABLE material.si_materials IS '자재 마스터 테이블';
COMMENT ON COLUMN material.si_materials.material_id IS '자재 ID (PK)';
COMMENT ON COLUMN material.si_materials.tenant_id IS '테넌트 ID';
COMMENT ON COLUMN material.si_materials.material_code IS '자재 코드';
COMMENT ON COLUMN material.si_materials.material_name IS '자재명';
COMMENT ON COLUMN material.si_materials.material_type IS '자재 유형 (RAW_MATERIAL, SUB_MATERIAL, SEMI_FINISHED, FINISHED_PRODUCT)';
COMMENT ON COLUMN material.si_materials.specification IS '규격';
COMMENT ON COLUMN material.si_materials.model IS '모델';
COMMENT ON COLUMN material.si_materials.unit IS '단위';
COMMENT ON COLUMN material.si_materials.standard_price IS '표준 단가';
COMMENT ON COLUMN material.si_materials.current_price IS '현재 단가';
COMMENT ON COLUMN material.si_materials.currency IS '통화';
COMMENT ON COLUMN material.si_materials.supplier_id IS '공급업체 ID';
COMMENT ON COLUMN material.si_materials.lead_time_days IS '리드타임 (일)';
COMMENT ON COLUMN material.si_materials.min_stock_quantity IS '최소 재고량';
COMMENT ON COLUMN material.si_materials.max_stock_quantity IS '최대 재고량';
COMMENT ON COLUMN material.si_materials.safety_stock_quantity IS '안전 재고량';
COMMENT ON COLUMN material.si_materials.reorder_point IS '재주문점';
COMMENT ON COLUMN material.si_materials.storage_location IS '보관 위치';
COMMENT ON COLUMN material.si_materials.lot_managed IS 'LOT 관리 여부';
COMMENT ON COLUMN material.si_materials.shelf_life_days IS '유통기한 (일)';
COMMENT ON COLUMN material.si_materials.is_active IS '활성화 여부';
COMMENT ON COLUMN material.si_materials.remarks IS '비고';
