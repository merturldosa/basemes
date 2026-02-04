-- ============================================================================
-- Migration V015: Mold Management Schema
-- 금형 관리 스키마
-- Author: Moon Myung-seop
-- Description: Creates mold master, maintenance, and production history tables
-- ============================================================================

-- Create schema for mold management
-- CREATE SCHEMA IF NOT EXISTS mold;

-- Create Molds table (금형 마스터)
CREATE TABLE equipment.si_molds (
    mold_id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    mold_code VARCHAR(50) NOT NULL,
    mold_name VARCHAR(200) NOT NULL,

    -- Classification
    mold_type VARCHAR(30) NOT NULL,       -- INJECTION, PRESS, DIE_CASTING, FORGING, OTHER
    mold_grade VARCHAR(20),                -- A, B, C, S (등급)
    cavity_count INTEGER,                  -- 캐비티 수

    -- Shot management
    current_shot_count BIGINT DEFAULT 0,   -- 현재 Shot 수
    max_shot_count BIGINT,                 -- 최대 Shot 수
    maintenance_shot_interval BIGINT,      -- 보전 주기 (Shot)
    last_maintenance_shot BIGINT DEFAULT 0, -- 마지막 보전 시 Shot 수

    -- Relations
    site_id BIGINT,
    department_id BIGINT,

    -- Specifications
    manufacturer VARCHAR(200),
    model_name VARCHAR(200),
    serial_no VARCHAR(100),
    material VARCHAR(100),                 -- 금형 재질
    weight DECIMAL(10,2),                  -- 금형 중량 (kg)
    dimensions VARCHAR(100),               -- 금형 크기 (LxWxH)

    -- Dates
    manufacture_date DATE,
    purchase_date DATE,
    purchase_price DECIMAL(15,2),
    first_use_date DATE,
    warranty_period VARCHAR(50),
    warranty_expiry_date DATE,

    -- Status
    status VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE', -- AVAILABLE, IN_USE, MAINTENANCE, BREAKDOWN, RETIRED
    location VARCHAR(200),

    -- Common fields
    remarks TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Foreign keys
    CONSTRAINT fk_mold_tenant FOREIGN KEY (tenant_id) REFERENCES common.si_tenants(tenant_id) ON DELETE CASCADE,
    CONSTRAINT fk_mold_site FOREIGN KEY (site_id) REFERENCES common.si_sites(site_id) ON DELETE SET NULL,
    CONSTRAINT fk_mold_department FOREIGN KEY (department_id) REFERENCES common.si_departments(department_id) ON DELETE SET NULL,

    -- Unique constraint
    CONSTRAINT uk_mold_code UNIQUE (tenant_id, mold_code)
);

-- Create Mold Maintenances table (금형 보전 이력)
CREATE TABLE equipment.si_mold_maintenances (
    maintenance_id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    mold_id BIGINT NOT NULL,
    maintenance_no VARCHAR(50) NOT NULL,

    -- Maintenance classification
    maintenance_type VARCHAR(30) NOT NULL, -- DAILY_CHECK, PERIODIC, SHOT_BASED, EMERGENCY_REPAIR, OVERHAUL
    maintenance_date TIMESTAMP NOT NULL,

    -- Shot tracking
    shot_count_before BIGINT,              -- 보전 전 Shot 수
    shot_count_reset BOOLEAN DEFAULT FALSE, -- Shot 수 초기화 여부
    shot_count_after BIGINT,               -- 보전 후 Shot 수

    -- Personnel
    technician_user_id BIGINT,
    technician_name VARCHAR(100),

    -- Maintenance details
    maintenance_content TEXT,              -- 보전 내용
    parts_replaced TEXT,                   -- 교체 부품
    findings TEXT,                         -- 발견 사항
    corrective_action TEXT,                -- 조치 사항

    -- Cost
    parts_cost DECIMAL(15,2) DEFAULT 0,
    labor_cost DECIMAL(15,2) DEFAULT 0,
    total_cost DECIMAL(15,2) DEFAULT 0,    -- Auto-calculated
    labor_hours INTEGER,

    -- Result
    maintenance_result VARCHAR(30),        -- COMPLETED, PARTIAL, FAILED
    next_maintenance_date DATE,

    -- Common fields
    remarks TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Foreign keys
    CONSTRAINT fk_mold_maintenance_tenant FOREIGN KEY (tenant_id) REFERENCES common.si_tenants(tenant_id) ON DELETE CASCADE,
    CONSTRAINT fk_mold_maintenance_mold FOREIGN KEY (mold_id) REFERENCES equipment.si_molds(mold_id) ON DELETE CASCADE,
    CONSTRAINT fk_mold_maintenance_technician FOREIGN KEY (technician_user_id) REFERENCES common.si_users(user_id) ON DELETE SET NULL,

    -- Unique constraint
    CONSTRAINT uk_mold_maintenance_no UNIQUE (tenant_id, maintenance_no)
);

-- Create Mold Production History table (금형 생산 이력)
CREATE TABLE equipment.si_mold_production_history (
    history_id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    mold_id BIGINT NOT NULL,

    -- Production reference
    work_order_id BIGINT,
    work_result_id BIGINT,
    production_date DATE NOT NULL,

    -- Shot tracking
    shot_count INTEGER NOT NULL,           -- 이번 생산의 Shot 수
    cumulative_shot_count BIGINT,          -- 누적 Shot 수 (생성 시 자동 계산)

    -- Production quantities
    production_quantity DECIMAL(15,3),
    good_quantity DECIMAL(15,3),
    defect_quantity DECIMAL(15,3),

    -- Operator
    operator_user_id BIGINT,
    operator_name VARCHAR(100),

    -- Common fields
    remarks TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Foreign keys
    CONSTRAINT fk_mold_history_tenant FOREIGN KEY (tenant_id) REFERENCES common.si_tenants(tenant_id) ON DELETE CASCADE,
    CONSTRAINT fk_mold_history_mold FOREIGN KEY (mold_id) REFERENCES equipment.si_molds(mold_id) ON DELETE CASCADE,
    CONSTRAINT fk_mold_history_work_order FOREIGN KEY (work_order_id) REFERENCES production.si_work_orders(work_order_id) ON DELETE SET NULL,
    CONSTRAINT fk_mold_history_work_result FOREIGN KEY (work_result_id) REFERENCES production.si_work_results(work_result_id) ON DELETE SET NULL,
    CONSTRAINT fk_mold_history_operator FOREIGN KEY (operator_user_id) REFERENCES common.si_users(user_id) ON DELETE SET NULL
);

-- Create indexes for performance
CREATE INDEX idx_mold_tenant ON equipment.si_molds(tenant_id);
CREATE INDEX idx_mold_type ON equipment.si_molds(mold_type);
CREATE INDEX idx_mold_status ON equipment.si_molds(status);
CREATE INDEX idx_mold_site ON equipment.si_molds(site_id);
CREATE INDEX idx_mold_department ON equipment.si_molds(department_id);

CREATE INDEX idx_mold_maintenance_tenant ON equipment.si_mold_maintenances(tenant_id);
CREATE INDEX idx_mold_maintenance_mold ON equipment.si_mold_maintenances(mold_id);
CREATE INDEX idx_mold_maintenance_type ON equipment.si_mold_maintenances(maintenance_type);
CREATE INDEX idx_mold_maintenance_date ON equipment.si_mold_maintenances(maintenance_date);

CREATE INDEX idx_mold_history_tenant ON equipment.si_mold_production_history(tenant_id);
CREATE INDEX idx_mold_history_mold ON equipment.si_mold_production_history(mold_id);
CREATE INDEX idx_mold_history_date ON equipment.si_mold_production_history(production_date);
CREATE INDEX idx_mold_history_work_order ON equipment.si_mold_production_history(work_order_id);

-- Add comments
COMMENT ON TABLE equipment.si_molds IS '금형 마스터 테이블';
COMMENT ON COLUMN equipment.si_molds.mold_id IS '금형 ID (PK)';
COMMENT ON COLUMN equipment.si_molds.mold_code IS '금형 코드 (테넌트별 unique)';
COMMENT ON COLUMN equipment.si_molds.cavity_count IS '캐비티 수';
COMMENT ON COLUMN equipment.si_molds.current_shot_count IS '현재 Shot 수';
COMMENT ON COLUMN equipment.si_molds.max_shot_count IS '최대 Shot 수';
COMMENT ON COLUMN equipment.si_molds.maintenance_shot_interval IS '보전 주기 (Shot)';

COMMENT ON TABLE equipment.si_mold_maintenances IS '금형 보전 이력 테이블';
COMMENT ON COLUMN equipment.si_mold_maintenances.shot_count_reset IS 'Shot 수 초기화 여부';

COMMENT ON TABLE equipment.si_mold_production_history IS '금형 생산 이력 테이블';
COMMENT ON COLUMN equipment.si_mold_production_history.shot_count IS '이번 생산의 Shot 수';
COMMENT ON COLUMN equipment.si_mold_production_history.cumulative_shot_count IS '누적 Shot 수';

-- ============================================================================
-- Triggers for automatic calculations
-- ============================================================================

-- Trigger function for calculating total cost
CREATE OR REPLACE FUNCTION equipment.calculate_mold_maintenance_cost()
RETURNS TRIGGER AS $$
BEGIN
    NEW.total_cost := COALESCE(NEW.parts_cost, 0) + COALESCE(NEW.labor_cost, 0);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger for auto-calculating maintenance cost
CREATE TRIGGER trg_calculate_mold_maintenance_cost
    BEFORE INSERT OR UPDATE ON equipment.si_mold_maintenances
    FOR EACH ROW
    EXECUTE FUNCTION equipment.calculate_mold_maintenance_cost();

-- Trigger function for updating mold shot count from production history
CREATE OR REPLACE FUNCTION equipment.update_mold_shot_count()
RETURNS TRIGGER AS $$
BEGIN
    -- Update cumulative shot count in history record
    NEW.cumulative_shot_count := (
        SELECT COALESCE(current_shot_count, 0) + NEW.shot_count
        FROM equipment.si_molds
        WHERE mold_id = NEW.mold_id
    );

    -- Update mold's current shot count
    UPDATE equipment.si_molds
    SET current_shot_count = current_shot_count + NEW.shot_count,
        updated_at = CURRENT_TIMESTAMP
    WHERE mold_id = NEW.mold_id;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger for auto-updating shot count
CREATE TRIGGER trg_update_mold_shot_count
    BEFORE INSERT ON equipment.si_mold_production_history
    FOR EACH ROW
    EXECUTE FUNCTION equipment.update_mold_shot_count();

-- Trigger for updating updated_at timestamp
CREATE TRIGGER trg_update_mold_timestamp
    BEFORE UPDATE ON equipment.si_molds
    FOR EACH ROW
    EXECUTE FUNCTION common.update_timestamp();

CREATE TRIGGER trg_update_mold_maintenance_timestamp
    BEFORE UPDATE ON equipment.si_mold_maintenances
    FOR EACH ROW
    EXECUTE FUNCTION common.update_timestamp();

CREATE TRIGGER trg_update_mold_history_timestamp
    BEFORE UPDATE ON equipment.si_mold_production_history
    FOR EACH ROW
    EXECUTE FUNCTION common.update_timestamp();

-- ============================================================================
-- Migration complete
-- ============================================================================

COMMENT ON SCHEMA equipment IS 'Equipment management schema including equipments, operations, inspections, downtimes, and molds';
