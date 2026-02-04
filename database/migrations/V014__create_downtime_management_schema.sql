-- ============================================================================
-- Migration V014: Downtime Management Schema
-- 비가동 관리 스키마
-- Author: Moon Myung-seop
-- Description: Creates downtime tracking table for equipment downtime analysis
-- ============================================================================

-- Create Downtimes table (설비 비가동 이력)
CREATE TABLE equipment.si_downtimes (
    downtime_id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    equipment_id BIGINT NOT NULL,
    downtime_code VARCHAR(50) NOT NULL,

    -- Downtime classification
    downtime_type VARCHAR(30) NOT NULL,  -- BREAKDOWN, SETUP_CHANGE, MATERIAL_SHORTAGE, QUALITY_ISSUE, PLANNED_MAINTENANCE, UNPLANNED_MAINTENANCE, NO_ORDER, OTHER
    downtime_category VARCHAR(100),      -- 비가동 대분류 (자유 입력)

    -- Time tracking
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    duration_minutes INTEGER,            -- Auto-calculated by trigger

    -- Relations (optional)
    work_order_id BIGINT,
    operation_id BIGINT,

    -- Responsible person
    responsible_user_id BIGINT,
    responsible_name VARCHAR(100),

    -- Analysis
    cause TEXT,                          -- 비가동 원인
    countermeasure TEXT,                 -- 조치사항
    preventive_action TEXT,              -- 재발 방지 대책

    -- Status
    is_resolved BOOLEAN DEFAULT FALSE,   -- 해결 여부
    resolved_at TIMESTAMP,               -- 해결 시간

    -- Common fields
    remarks TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Foreign keys
    CONSTRAINT fk_downtime_tenant FOREIGN KEY (tenant_id) REFERENCES common.si_tenants(tenant_id) ON DELETE CASCADE,
    CONSTRAINT fk_downtime_equipment FOREIGN KEY (equipment_id) REFERENCES equipment.si_equipments(equipment_id) ON DELETE CASCADE,
    CONSTRAINT fk_downtime_work_order FOREIGN KEY (work_order_id) REFERENCES production.si_work_orders(work_order_id) ON DELETE SET NULL,
    CONSTRAINT fk_downtime_operation FOREIGN KEY (operation_id) REFERENCES equipment.si_equipment_operations(operation_id) ON DELETE SET NULL,
    CONSTRAINT fk_downtime_responsible_user FOREIGN KEY (responsible_user_id) REFERENCES common.si_users(user_id) ON DELETE SET NULL,

    -- Unique constraint
    CONSTRAINT uk_downtime_code UNIQUE (tenant_id, downtime_code)
);

-- Create indexes for performance
CREATE INDEX idx_downtime_tenant ON equipment.si_downtimes(tenant_id);
CREATE INDEX idx_downtime_equipment ON equipment.si_downtimes(equipment_id);
CREATE INDEX idx_downtime_type ON equipment.si_downtimes(downtime_type);
CREATE INDEX idx_downtime_start_time ON equipment.si_downtimes(start_time);
CREATE INDEX idx_downtime_work_order ON equipment.si_downtimes(work_order_id);
CREATE INDEX idx_downtime_operation ON equipment.si_downtimes(operation_id);
CREATE INDEX idx_downtime_is_resolved ON equipment.si_downtimes(is_resolved);

-- Add comments
COMMENT ON TABLE equipment.si_downtimes IS '설비 비가동 이력 테이블';
COMMENT ON COLUMN equipment.si_downtimes.downtime_id IS '비가동 ID (PK)';
COMMENT ON COLUMN equipment.si_downtimes.tenant_id IS '테넌트 ID';
COMMENT ON COLUMN equipment.si_downtimes.equipment_id IS '설비 ID';
COMMENT ON COLUMN equipment.si_downtimes.downtime_code IS '비가동 코드 (테넌트별 unique)';
COMMENT ON COLUMN equipment.si_downtimes.downtime_type IS '비가동 유형 (BREAKDOWN, SETUP_CHANGE, MATERIAL_SHORTAGE, QUALITY_ISSUE, PLANNED_MAINTENANCE, UNPLANNED_MAINTENANCE, NO_ORDER, OTHER)';
COMMENT ON COLUMN equipment.si_downtimes.downtime_category IS '비가동 대분류';
COMMENT ON COLUMN equipment.si_downtimes.start_time IS '비가동 시작 시간';
COMMENT ON COLUMN equipment.si_downtimes.end_time IS '비가동 종료 시간';
COMMENT ON COLUMN equipment.si_downtimes.duration_minutes IS '비가동 지속 시간 (분)';
COMMENT ON COLUMN equipment.si_downtimes.cause IS '비가동 원인';
COMMENT ON COLUMN equipment.si_downtimes.countermeasure IS '조치사항';
COMMENT ON COLUMN equipment.si_downtimes.preventive_action IS '재발 방지 대책';
COMMENT ON COLUMN equipment.si_downtimes.is_resolved IS '해결 여부';
COMMENT ON COLUMN equipment.si_downtimes.resolved_at IS '해결 시간';

-- ============================================================================
-- Triggers for automatic timestamp updates
-- ============================================================================

-- Trigger function for calculating downtime duration
CREATE OR REPLACE FUNCTION equipment.calculate_downtime_duration()
RETURNS TRIGGER AS $$
BEGIN
    -- Calculate duration in minutes when both start_time and end_time are set
    IF NEW.end_time IS NOT NULL THEN
        NEW.duration_minutes := EXTRACT(EPOCH FROM (NEW.end_time - NEW.start_time)) / 60;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger for auto-calculating downtime duration
CREATE TRIGGER trg_calculate_downtime_duration
    BEFORE INSERT OR UPDATE ON equipment.si_downtimes
    FOR EACH ROW
    EXECUTE FUNCTION equipment.calculate_downtime_duration();

-- Trigger for updating updated_at timestamp
CREATE TRIGGER trg_update_downtime_timestamp
    BEFORE UPDATE ON equipment.si_downtimes
    FOR EACH ROW
    EXECUTE FUNCTION common.update_timestamp();

-- ============================================================================
-- Initial data / Sample data (Optional)
-- ============================================================================

-- Add sample downtime types as code data if needed
-- This can be managed through the code management system

-- ============================================================================
-- Migration complete
-- ============================================================================

COMMENT ON SCHEMA equipment IS 'Equipment management schema including equipments, operations, inspections, and downtimes';
