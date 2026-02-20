-- ============================================================
-- Migration: V013 - Equipment Management Schema
-- Description: 설비 관리 스키마 생성
-- Author: Moon Myung-seop
-- Date: 2026-01-23
-- ============================================================

-- Create equipment schema if not exists
CREATE SCHEMA IF NOT EXISTS equipment;

-- ============================================================
-- 1. EQUIPMENTS (설비 마스터)
-- ============================================================

CREATE TABLE equipment.sd_equipments (
    equipment_id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    equipment_code VARCHAR(50) NOT NULL,
    equipment_name VARCHAR(200) NOT NULL,

    -- Type & Classification
    equipment_type VARCHAR(30) NOT NULL, -- MACHINE, MOLD, TOOL, FACILITY, VEHICLE, OTHER
    equipment_category VARCHAR(50), -- CNC, INJECTION, PRESS, ASSEMBLY, etc

    -- Manufacturer Info
    manufacturer VARCHAR(100),
    model_name VARCHAR(100),
    serial_no VARCHAR(100),

    -- Purchase & Installation
    purchase_date DATE,
    purchase_price DECIMAL(15,2),
    installation_date DATE,
    warranty_end_date DATE,

    -- Location
    site_id BIGINT,
    department_id BIGINT,
    location VARCHAR(200), -- Detailed location description

    -- Specifications
    capacity VARCHAR(50), -- Production capacity
    capacity_unit VARCHAR(20),
    power_rating DECIMAL(10,2), -- kW
    dimensions VARCHAR(100), -- L x W x H
    weight DECIMAL(10,2), -- kg
    specifications TEXT, -- JSON or detailed specs

    -- Status
    status VARCHAR(30) NOT NULL DEFAULT 'OPERATIONAL', -- OPERATIONAL, STOPPED, MAINTENANCE, BREAKDOWN, RETIRED
    operational_status VARCHAR(30), -- RUNNING, IDLE, STOPPED

    -- Maintenance
    maintenance_cycle_days INTEGER, -- Days between maintenance
    last_maintenance_date DATE,
    next_maintenance_date DATE,

    -- Performance
    standard_cycle_time DECIMAL(10,2), -- Standard cycle time in seconds
    actual_oee_target DECIMAL(5,2), -- OEE target percentage

    -- Additional
    image_url VARCHAR(500),
    manual_url VARCHAR(500),
    remarks TEXT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),

    -- Foreign Keys
    CONSTRAINT fk_equipment_tenant FOREIGN KEY (tenant_id) REFERENCES core.sd_tenants(tenant_id),
    CONSTRAINT fk_equipment_site FOREIGN KEY (site_id) REFERENCES common.sd_sites(site_id) ON DELETE SET NULL,
    CONSTRAINT fk_equipment_department FOREIGN KEY (department_id) REFERENCES core.sd_departments(department_id) ON DELETE SET NULL,

    -- Unique Constraint
    CONSTRAINT uk_equipment_code UNIQUE (tenant_id, equipment_code)
);

-- Indexes
CREATE INDEX idx_equipment_tenant ON equipment.sd_equipments(tenant_id);
CREATE INDEX idx_equipment_status ON equipment.sd_equipments(status);
CREATE INDEX idx_equipment_type ON equipment.sd_equipments(equipment_type);
CREATE INDEX idx_equipment_site ON equipment.sd_equipments(site_id);
CREATE INDEX idx_equipment_department ON equipment.sd_equipments(department_id);

-- Comments
COMMENT ON TABLE equipment.sd_equipments IS '설비 마스터';
COMMENT ON COLUMN equipment.sd_equipments.equipment_type IS '설비유형: MACHINE(기계), MOLD(금형), TOOL(공구), FACILITY(시설), VEHICLE(운반구), OTHER(기타)';
COMMENT ON COLUMN equipment.sd_equipments.status IS '상태: OPERATIONAL(가동), STOPPED(정지), MAINTENANCE(점검중), BREAKDOWN(고장), RETIRED(폐기)';

-- ============================================================
-- 2. EQUIPMENT OPERATIONS (설비 가동 이력)
-- ============================================================

CREATE TABLE equipment.sd_equipment_operations (
    operation_id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    equipment_id BIGINT NOT NULL,

    -- Work Order Link
    work_order_id BIGINT,
    work_result_id BIGINT,

    -- Operation Time
    operation_date DATE NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    operation_hours DECIMAL(10,2), -- Calculated: (end_time - start_time) in hours

    -- Operator
    operator_user_id BIGINT,
    operator_name VARCHAR(100),

    -- Production
    production_quantity DECIMAL(15,3),
    good_quantity DECIMAL(15,3),
    defect_quantity DECIMAL(15,3),

    -- Status
    operation_status VARCHAR(30) NOT NULL, -- RUNNING, COMPLETED, STOPPED, ABORTED

    -- Stop Information
    stop_reason VARCHAR(100),
    stop_duration_minutes INTEGER, -- Total stop time in minutes

    -- Performance Metrics
    cycle_time DECIMAL(10,2), -- Actual cycle time in seconds
    utilization_rate DECIMAL(5,2), -- Equipment utilization %
    performance_rate DECIMAL(5,2), -- Performance efficiency %
    quality_rate DECIMAL(5,2), -- Quality rate %
    oee DECIMAL(5,2), -- Overall Equipment Effectiveness %

    -- Additional
    remarks TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),

    -- Foreign Keys
    CONSTRAINT fk_operation_tenant FOREIGN KEY (tenant_id) REFERENCES core.sd_tenants(tenant_id),
    CONSTRAINT fk_operation_equipment FOREIGN KEY (equipment_id) REFERENCES equipment.sd_equipments(equipment_id),
    CONSTRAINT fk_operation_work_order FOREIGN KEY (work_order_id) REFERENCES production.sd_work_orders(work_order_id) ON DELETE SET NULL,
    CONSTRAINT fk_operation_work_result FOREIGN KEY (work_result_id) REFERENCES production.sd_work_results(work_result_id) ON DELETE SET NULL,
    CONSTRAINT fk_operation_operator FOREIGN KEY (operator_user_id) REFERENCES core.sd_users(user_id) ON DELETE SET NULL
);

-- Indexes
CREATE INDEX idx_operation_tenant ON equipment.sd_equipment_operations(tenant_id);
CREATE INDEX idx_operation_equipment ON equipment.sd_equipment_operations(equipment_id);
CREATE INDEX idx_operation_date ON equipment.sd_equipment_operations(operation_date);
CREATE INDEX idx_operation_status ON equipment.sd_equipment_operations(operation_status);
CREATE INDEX idx_operation_work_order ON equipment.sd_equipment_operations(work_order_id);

-- Comments
COMMENT ON TABLE equipment.sd_equipment_operations IS '설비 가동 이력';
COMMENT ON COLUMN equipment.sd_equipment_operations.operation_status IS '가동상태: RUNNING(가동중), COMPLETED(완료), STOPPED(정지), ABORTED(중단)';
COMMENT ON COLUMN equipment.sd_equipment_operations.oee IS 'Overall Equipment Effectiveness: 가동률 x 성능률 x 품질률';

-- ============================================================
-- 3. EQUIPMENT INSPECTIONS (설비 점검)
-- ============================================================

CREATE TABLE equipment.sd_equipment_inspections (
    inspection_id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    equipment_id BIGINT NOT NULL,
    inspection_no VARCHAR(50) NOT NULL,

    -- Inspection Info
    inspection_date TIMESTAMP NOT NULL,
    inspection_type VARCHAR(30) NOT NULL, -- DAILY, PERIODIC, PREVENTIVE, CORRECTIVE, BREAKDOWN

    -- Inspector
    inspector_user_id BIGINT,
    inspector_name VARCHAR(100),

    -- Inspection Details
    inspection_items TEXT, -- JSON array of checklist items
    inspection_result VARCHAR(30) NOT NULL, -- PASS, FAIL, CONDITIONAL

    -- Findings
    findings TEXT, -- Issues found during inspection
    abnormality_detected BOOLEAN DEFAULT false,
    severity VARCHAR(30), -- CRITICAL, MAJOR, MINOR

    -- Actions
    corrective_action TEXT,
    corrective_action_date TIMESTAMP,
    responsible_user_id BIGINT,
    responsible_user_name VARCHAR(100),

    -- Parts Replacement
    parts_replaced TEXT,
    parts_cost DECIMAL(15,2),
    labor_hours DECIMAL(10,2),
    labor_cost DECIMAL(15,2),
    total_cost DECIMAL(15,2),

    -- Next Inspection
    next_inspection_date DATE,
    next_inspection_type VARCHAR(30),

    -- Additional
    attachments TEXT, -- URLs to photos/documents
    remarks TEXT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),

    -- Foreign Keys
    CONSTRAINT fk_inspection_tenant FOREIGN KEY (tenant_id) REFERENCES core.sd_tenants(tenant_id),
    CONSTRAINT fk_inspection_equipment FOREIGN KEY (equipment_id) REFERENCES equipment.sd_equipments(equipment_id),
    CONSTRAINT fk_inspection_inspector FOREIGN KEY (inspector_user_id) REFERENCES core.sd_users(user_id) ON DELETE SET NULL,
    CONSTRAINT fk_inspection_responsible FOREIGN KEY (responsible_user_id) REFERENCES core.sd_users(user_id) ON DELETE SET NULL,

    -- Unique Constraint
    CONSTRAINT uk_inspection_no UNIQUE (tenant_id, inspection_no)
);

-- Indexes
CREATE INDEX idx_inspection_tenant ON equipment.sd_equipment_inspections(tenant_id);
CREATE INDEX idx_inspection_equipment ON equipment.sd_equipment_inspections(equipment_id);
CREATE INDEX idx_inspection_date ON equipment.sd_equipment_inspections(inspection_date);
CREATE INDEX idx_inspection_type ON equipment.sd_equipment_inspections(inspection_type);
CREATE INDEX idx_inspection_result ON equipment.sd_equipment_inspections(inspection_result);

-- Comments
COMMENT ON TABLE equipment.sd_equipment_inspections IS '설비 점검';
COMMENT ON COLUMN equipment.sd_equipment_inspections.inspection_type IS '점검유형: DAILY(일상), PERIODIC(정기), PREVENTIVE(예방), CORRECTIVE(교정), BREAKDOWN(고장)';
COMMENT ON COLUMN equipment.sd_equipment_inspections.inspection_result IS '점검결과: PASS(정상), FAIL(불량), CONDITIONAL(조건부)';

-- ============================================================
-- 4. TRIGGERS - Auto update timestamp
-- ============================================================

-- Equipments
CREATE OR REPLACE FUNCTION equipment.update_equipment_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_equipment_updated_at
    BEFORE UPDATE ON equipment.sd_equipments
    FOR EACH ROW
    EXECUTE FUNCTION equipment.update_equipment_updated_at();

-- Equipment Operations
CREATE OR REPLACE FUNCTION equipment.update_operation_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_operation_updated_at
    BEFORE UPDATE ON equipment.sd_equipment_operations
    FOR EACH ROW
    EXECUTE FUNCTION equipment.update_operation_updated_at();

-- Equipment Inspections
CREATE OR REPLACE FUNCTION equipment.update_inspection_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_inspection_updated_at
    BEFORE UPDATE ON equipment.sd_equipment_inspections
    FOR EACH ROW
    EXECUTE FUNCTION equipment.update_inspection_updated_at();

-- ============================================================
-- 5. FUNCTIONS - Calculate Operation Hours
-- ============================================================

CREATE OR REPLACE FUNCTION equipment.calculate_operation_hours()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.end_time IS NOT NULL AND NEW.start_time IS NOT NULL THEN
        NEW.operation_hours = EXTRACT(EPOCH FROM (NEW.end_time - NEW.start_time)) / 3600.0;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_calculate_operation_hours
    BEFORE INSERT OR UPDATE ON equipment.sd_equipment_operations
    FOR EACH ROW
    EXECUTE FUNCTION equipment.calculate_operation_hours();

-- ============================================================
-- End of Migration V013
-- ============================================================
