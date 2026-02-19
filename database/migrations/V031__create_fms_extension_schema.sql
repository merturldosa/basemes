-- ============================================================
-- Migration: V031 - FMS Extension Schema
-- Description: 시설관리(FMS) 확장 스키마 생성
-- Author: Moon Myung-seop
-- Date: 2026-02-19
-- ============================================================

-- ============================================================
-- 1. INSPECTION FORMS (점검 양식)
-- ============================================================

CREATE TABLE equipment.si_inspection_forms (
    form_id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    form_code VARCHAR(50) NOT NULL,
    form_name VARCHAR(200) NOT NULL,
    description TEXT,
    equipment_type VARCHAR(30),          -- 대상 설비유형 필터 (NULL=전체)
    inspection_type VARCHAR(30),         -- DAILY, PERIODIC, PREVENTIVE, CORRECTIVE
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),

    CONSTRAINT fk_insp_form_tenant FOREIGN KEY (tenant_id) REFERENCES core.si_tenants(tenant_id),
    CONSTRAINT uk_insp_form_code UNIQUE (tenant_id, form_code)
);

CREATE INDEX idx_insp_form_tenant ON equipment.si_inspection_forms(tenant_id);
CREATE INDEX idx_insp_form_type ON equipment.si_inspection_forms(equipment_type);

COMMENT ON TABLE equipment.si_inspection_forms IS '점검 양식';
COMMENT ON COLUMN equipment.si_inspection_forms.inspection_type IS '점검유형: DAILY(일상), PERIODIC(정기), PREVENTIVE(예방), CORRECTIVE(교정)';

-- ============================================================
-- 2. INSPECTION FORM FIELDS (점검 양식 필드)
-- ============================================================

CREATE TABLE equipment.si_inspection_form_fields (
    field_id BIGSERIAL PRIMARY KEY,
    form_id BIGINT NOT NULL,
    field_name VARCHAR(200) NOT NULL,
    field_type VARCHAR(30) NOT NULL,     -- TEXT, NUMBER, BOOLEAN, SELECT
    field_order INTEGER NOT NULL DEFAULT 0,
    is_required BOOLEAN DEFAULT false,
    options TEXT,                          -- SELECT 타입일 때 선택지 (JSON)
    unit VARCHAR(30),                     -- 단위 (NUMBER 타입)
    min_value DECIMAL(15,4),
    max_value DECIMAL(15,4),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_form_field_form FOREIGN KEY (form_id) REFERENCES equipment.si_inspection_forms(form_id) ON DELETE CASCADE
);

CREATE INDEX idx_form_field_form ON equipment.si_inspection_form_fields(form_id);

COMMENT ON TABLE equipment.si_inspection_form_fields IS '점검 양식 필드';
COMMENT ON COLUMN equipment.si_inspection_form_fields.field_type IS '필드유형: TEXT(텍스트), NUMBER(숫자), BOOLEAN(체크), SELECT(선택)';

-- ============================================================
-- 3. INSPECTION PLANS (점검 계획)
-- ============================================================

CREATE TABLE equipment.si_inspection_plans (
    plan_id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    plan_code VARCHAR(50) NOT NULL,
    plan_name VARCHAR(200) NOT NULL,
    equipment_id BIGINT NOT NULL,
    form_id BIGINT,                       -- 사용할 점검 양식
    inspection_type VARCHAR(30) NOT NULL, -- DAILY, PERIODIC, PREVENTIVE, CORRECTIVE
    cycle_days INTEGER NOT NULL,          -- 점검 주기 (일)
    assigned_user_id BIGINT,              -- 담당자
    last_execution_date DATE,
    next_due_date DATE,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, PAUSED, COMPLETED
    remarks TEXT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),

    CONSTRAINT fk_insp_plan_tenant FOREIGN KEY (tenant_id) REFERENCES core.si_tenants(tenant_id),
    CONSTRAINT fk_insp_plan_equipment FOREIGN KEY (equipment_id) REFERENCES equipment.si_equipments(equipment_id),
    CONSTRAINT fk_insp_plan_form FOREIGN KEY (form_id) REFERENCES equipment.si_inspection_forms(form_id) ON DELETE SET NULL,
    CONSTRAINT fk_insp_plan_user FOREIGN KEY (assigned_user_id) REFERENCES core.si_users(user_id) ON DELETE SET NULL,
    CONSTRAINT uk_insp_plan_code UNIQUE (tenant_id, plan_code)
);

CREATE INDEX idx_insp_plan_tenant ON equipment.si_inspection_plans(tenant_id);
CREATE INDEX idx_insp_plan_equipment ON equipment.si_inspection_plans(equipment_id);
CREATE INDEX idx_insp_plan_due ON equipment.si_inspection_plans(next_due_date);
CREATE INDEX idx_insp_plan_status ON equipment.si_inspection_plans(status);

COMMENT ON TABLE equipment.si_inspection_plans IS '점검 계획';

-- ============================================================
-- 4. INSPECTION ACTIONS (점검 조치)
-- ============================================================

CREATE TABLE equipment.si_inspection_actions (
    action_id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    inspection_id BIGINT NOT NULL,        -- 원본 점검
    action_type VARCHAR(30) NOT NULL,     -- CORRECTIVE(시정), PREVENTIVE(예방)
    description TEXT NOT NULL,
    assigned_user_id BIGINT,
    due_date DATE,
    completed_date DATE,
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN', -- OPEN, IN_PROGRESS, COMPLETED
    result TEXT,
    remarks TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),

    CONSTRAINT fk_insp_action_tenant FOREIGN KEY (tenant_id) REFERENCES core.si_tenants(tenant_id),
    CONSTRAINT fk_insp_action_inspection FOREIGN KEY (inspection_id) REFERENCES equipment.si_equipment_inspections(inspection_id),
    CONSTRAINT fk_insp_action_user FOREIGN KEY (assigned_user_id) REFERENCES core.si_users(user_id) ON DELETE SET NULL
);

CREATE INDEX idx_insp_action_tenant ON equipment.si_inspection_actions(tenant_id);
CREATE INDEX idx_insp_action_inspection ON equipment.si_inspection_actions(inspection_id);
CREATE INDEX idx_insp_action_status ON equipment.si_inspection_actions(status);

COMMENT ON TABLE equipment.si_inspection_actions IS '점검 조치';
COMMENT ON COLUMN equipment.si_inspection_actions.action_type IS '조치유형: CORRECTIVE(시정), PREVENTIVE(예방)';
COMMENT ON COLUMN equipment.si_inspection_actions.status IS '상태: OPEN(접수), IN_PROGRESS(진행중), COMPLETED(완료)';

-- ============================================================
-- 5. GAUGES (계측기)
-- ============================================================

CREATE TABLE equipment.si_gauges (
    gauge_id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    gauge_code VARCHAR(50) NOT NULL,
    gauge_name VARCHAR(200) NOT NULL,
    gauge_type VARCHAR(50),               -- 계측기 유형
    manufacturer VARCHAR(100),
    model_name VARCHAR(100),
    serial_no VARCHAR(100),
    equipment_id BIGINT,                  -- 연결 설비
    department_id BIGINT,
    location VARCHAR(200),
    measurement_range VARCHAR(100),       -- 측정 범위
    accuracy VARCHAR(50),                 -- 정밀도
    calibration_cycle_days INTEGER,       -- 교정 주기 (일)
    last_calibration_date DATE,
    next_calibration_date DATE,
    calibration_status VARCHAR(30) DEFAULT 'VALID', -- VALID, EXPIRED, IN_CALIBRATION
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, INACTIVE, DISPOSED
    remarks TEXT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),

    CONSTRAINT fk_gauge_tenant FOREIGN KEY (tenant_id) REFERENCES core.si_tenants(tenant_id),
    CONSTRAINT fk_gauge_equipment FOREIGN KEY (equipment_id) REFERENCES equipment.si_equipments(equipment_id) ON DELETE SET NULL,
    CONSTRAINT fk_gauge_department FOREIGN KEY (department_id) REFERENCES core.si_departments(department_id) ON DELETE SET NULL,
    CONSTRAINT uk_gauge_code UNIQUE (tenant_id, gauge_code)
);

CREATE INDEX idx_gauge_tenant ON equipment.si_gauges(tenant_id);
CREATE INDEX idx_gauge_equipment ON equipment.si_gauges(equipment_id);
CREATE INDEX idx_gauge_cal_status ON equipment.si_gauges(calibration_status);
CREATE INDEX idx_gauge_next_cal ON equipment.si_gauges(next_calibration_date);

COMMENT ON TABLE equipment.si_gauges IS '계측기';
COMMENT ON COLUMN equipment.si_gauges.calibration_status IS '교정상태: VALID(유효), EXPIRED(만료), IN_CALIBRATION(교정중)';

-- ============================================================
-- 6. CONSUMABLES (소모품)
-- ============================================================

CREATE TABLE equipment.si_consumables (
    consumable_id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    consumable_code VARCHAR(50) NOT NULL,
    consumable_name VARCHAR(200) NOT NULL,
    category VARCHAR(50),                 -- 소모품 분류
    equipment_id BIGINT,                  -- 연결 설비
    unit VARCHAR(20),                     -- 단위
    current_stock DECIMAL(15,3) DEFAULT 0,
    minimum_stock DECIMAL(15,3) DEFAULT 0, -- 안전 재고
    maximum_stock DECIMAL(15,3),
    unit_price DECIMAL(15,2),
    supplier VARCHAR(200),
    lead_time_days INTEGER,               -- 조달 소요일
    last_replenished_date DATE,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, DISCONTINUED
    remarks TEXT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),

    CONSTRAINT fk_consumable_tenant FOREIGN KEY (tenant_id) REFERENCES core.si_tenants(tenant_id),
    CONSTRAINT fk_consumable_equipment FOREIGN KEY (equipment_id) REFERENCES equipment.si_equipments(equipment_id) ON DELETE SET NULL,
    CONSTRAINT uk_consumable_code UNIQUE (tenant_id, consumable_code)
);

CREATE INDEX idx_consumable_tenant ON equipment.si_consumables(tenant_id);
CREATE INDEX idx_consumable_equipment ON equipment.si_consumables(equipment_id);
CREATE INDEX idx_consumable_stock ON equipment.si_consumables(current_stock);

COMMENT ON TABLE equipment.si_consumables IS '소모품';

-- ============================================================
-- 7. EQUIPMENT PARTS (설비 부품)
-- ============================================================

CREATE TABLE equipment.si_equipment_parts (
    part_id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    equipment_id BIGINT NOT NULL,
    part_code VARCHAR(50) NOT NULL,
    part_name VARCHAR(200) NOT NULL,
    part_type VARCHAR(50),                -- 부품 유형
    manufacturer VARCHAR(100),
    model_name VARCHAR(100),
    serial_no VARCHAR(100),
    installation_date DATE,
    expected_life_days INTEGER,           -- 예상 수명 (일)
    replacement_date DATE,                -- 마지막 교체일
    next_replacement_date DATE,           -- 다음 교체 예정일
    replacement_count INTEGER DEFAULT 0,  -- 교체 횟수
    unit_price DECIMAL(15,2),
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, WORN, REPLACED, DISPOSED
    remarks TEXT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),

    CONSTRAINT fk_eq_part_tenant FOREIGN KEY (tenant_id) REFERENCES core.si_tenants(tenant_id),
    CONSTRAINT fk_eq_part_equipment FOREIGN KEY (equipment_id) REFERENCES equipment.si_equipments(equipment_id),
    CONSTRAINT uk_eq_part_code UNIQUE (tenant_id, equipment_id, part_code)
);

CREATE INDEX idx_eq_part_tenant ON equipment.si_equipment_parts(tenant_id);
CREATE INDEX idx_eq_part_equipment ON equipment.si_equipment_parts(equipment_id);
CREATE INDEX idx_eq_part_next_replace ON equipment.si_equipment_parts(next_replacement_date);

COMMENT ON TABLE equipment.si_equipment_parts IS '설비 부품';

-- ============================================================
-- 8. BREAKDOWNS (고장 접수/처리)
-- ============================================================

CREATE TABLE equipment.si_breakdowns (
    breakdown_id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    breakdown_no VARCHAR(50) NOT NULL,
    equipment_id BIGINT NOT NULL,
    downtime_id BIGINT,                   -- 연결 비가동
    reported_at TIMESTAMP NOT NULL,
    reported_by_user_id BIGINT,
    failure_type VARCHAR(50),             -- MECHANICAL, ELECTRICAL, SOFTWARE, PNEUMATIC, HYDRAULIC, OTHER
    severity VARCHAR(30),                 -- CRITICAL, MAJOR, MINOR
    description TEXT NOT NULL,
    -- 배정
    assigned_user_id BIGINT,
    assigned_at TIMESTAMP,
    -- 수리
    repair_started_at TIMESTAMP,
    repair_completed_at TIMESTAMP,
    repair_duration_minutes INTEGER,      -- DB 트리거 자동 계산
    repair_description TEXT,
    parts_used TEXT,
    repair_cost DECIMAL(15,2),
    -- 근본원인
    root_cause TEXT,
    preventive_action TEXT,
    -- 상태
    status VARCHAR(30) NOT NULL DEFAULT 'REPORTED', -- REPORTED, ASSIGNED, IN_PROGRESS, COMPLETED, CLOSED
    closed_at TIMESTAMP,
    closed_by_user_id BIGINT,
    remarks TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),

    CONSTRAINT fk_breakdown_tenant FOREIGN KEY (tenant_id) REFERENCES core.si_tenants(tenant_id),
    CONSTRAINT fk_breakdown_equipment FOREIGN KEY (equipment_id) REFERENCES equipment.si_equipments(equipment_id),
    CONSTRAINT fk_breakdown_downtime FOREIGN KEY (downtime_id) REFERENCES equipment.si_downtimes(downtime_id) ON DELETE SET NULL,
    CONSTRAINT fk_breakdown_reporter FOREIGN KEY (reported_by_user_id) REFERENCES core.si_users(user_id) ON DELETE SET NULL,
    CONSTRAINT fk_breakdown_assignee FOREIGN KEY (assigned_user_id) REFERENCES core.si_users(user_id) ON DELETE SET NULL,
    CONSTRAINT fk_breakdown_closer FOREIGN KEY (closed_by_user_id) REFERENCES core.si_users(user_id) ON DELETE SET NULL,
    CONSTRAINT uk_breakdown_no UNIQUE (tenant_id, breakdown_no)
);

CREATE INDEX idx_breakdown_tenant ON equipment.si_breakdowns(tenant_id);
CREATE INDEX idx_breakdown_equipment ON equipment.si_breakdowns(equipment_id);
CREATE INDEX idx_breakdown_status ON equipment.si_breakdowns(status);
CREATE INDEX idx_breakdown_reported ON equipment.si_breakdowns(reported_at);
CREATE INDEX idx_breakdown_type ON equipment.si_breakdowns(failure_type);

COMMENT ON TABLE equipment.si_breakdowns IS '고장 접수/처리';
COMMENT ON COLUMN equipment.si_breakdowns.failure_type IS '고장유형: MECHANICAL(기계), ELECTRICAL(전기), SOFTWARE(소프트웨어), PNEUMATIC(공압), HYDRAULIC(유압), OTHER(기타)';
COMMENT ON COLUMN equipment.si_breakdowns.status IS '상태: REPORTED(접수), ASSIGNED(배정), IN_PROGRESS(수리중), COMPLETED(완료), CLOSED(종결)';

-- ============================================================
-- 9. DEVIATIONS (이탈 관리)
-- ============================================================

CREATE TABLE equipment.si_deviations (
    deviation_id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    deviation_no VARCHAR(50) NOT NULL,
    equipment_id BIGINT NOT NULL,
    parameter_name VARCHAR(200) NOT NULL, -- 이탈 파라미터명
    standard_value VARCHAR(100),          -- 기준값
    actual_value VARCHAR(100),            -- 실측값
    deviation_value VARCHAR(100),         -- 이탈량
    detected_at TIMESTAMP NOT NULL,
    detected_by_user_id BIGINT,
    severity VARCHAR(30),                 -- CRITICAL, MAJOR, MINOR
    description TEXT,
    -- CAPA
    root_cause TEXT,
    corrective_action TEXT,
    preventive_action TEXT,
    -- 상태
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN', -- OPEN, INVESTIGATING, RESOLVED, CLOSED
    resolved_at TIMESTAMP,
    resolved_by_user_id BIGINT,
    remarks TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),

    CONSTRAINT fk_deviation_tenant FOREIGN KEY (tenant_id) REFERENCES core.si_tenants(tenant_id),
    CONSTRAINT fk_deviation_equipment FOREIGN KEY (equipment_id) REFERENCES equipment.si_equipments(equipment_id),
    CONSTRAINT fk_deviation_detector FOREIGN KEY (detected_by_user_id) REFERENCES core.si_users(user_id) ON DELETE SET NULL,
    CONSTRAINT fk_deviation_resolver FOREIGN KEY (resolved_by_user_id) REFERENCES core.si_users(user_id) ON DELETE SET NULL,
    CONSTRAINT uk_deviation_no UNIQUE (tenant_id, deviation_no)
);

CREATE INDEX idx_deviation_tenant ON equipment.si_deviations(tenant_id);
CREATE INDEX idx_deviation_equipment ON equipment.si_deviations(equipment_id);
CREATE INDEX idx_deviation_status ON equipment.si_deviations(status);
CREATE INDEX idx_deviation_detected ON equipment.si_deviations(detected_at);

COMMENT ON TABLE equipment.si_deviations IS '이탈 관리';
COMMENT ON COLUMN equipment.si_deviations.status IS '상태: OPEN(접수), INVESTIGATING(조사중), RESOLVED(해결), CLOSED(종결)';

-- ============================================================
-- 10. EXTERNAL CALIBRATIONS (외부 검교정)
-- ============================================================

CREATE TABLE equipment.si_external_calibrations (
    calibration_id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    calibration_no VARCHAR(50) NOT NULL,
    gauge_id BIGINT NOT NULL,
    calibration_vendor VARCHAR(200),      -- 교정 업체
    requested_date DATE NOT NULL,
    sent_date DATE,
    completed_date DATE,
    certificate_no VARCHAR(100),          -- 인증서 번호
    certificate_url VARCHAR(500),         -- 인증서 파일
    calibration_result VARCHAR(30),       -- PASS, FAIL, CONDITIONAL
    cost DECIMAL(15,2),
    next_calibration_date DATE,           -- 다음 교정일
    status VARCHAR(30) NOT NULL DEFAULT 'REQUESTED', -- REQUESTED, SENT, IN_PROGRESS, COMPLETED
    remarks TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),

    CONSTRAINT fk_ext_cal_tenant FOREIGN KEY (tenant_id) REFERENCES core.si_tenants(tenant_id),
    CONSTRAINT fk_ext_cal_gauge FOREIGN KEY (gauge_id) REFERENCES equipment.si_gauges(gauge_id),
    CONSTRAINT uk_ext_cal_no UNIQUE (tenant_id, calibration_no)
);

CREATE INDEX idx_ext_cal_tenant ON equipment.si_external_calibrations(tenant_id);
CREATE INDEX idx_ext_cal_gauge ON equipment.si_external_calibrations(gauge_id);
CREATE INDEX idx_ext_cal_status ON equipment.si_external_calibrations(status);

COMMENT ON TABLE equipment.si_external_calibrations IS '외부 검교정';
COMMENT ON COLUMN equipment.si_external_calibrations.status IS '상태: REQUESTED(의뢰), SENT(발송), IN_PROGRESS(진행중), COMPLETED(완료)';

-- ============================================================
-- 11. TRIGGERS - Auto update timestamps
-- ============================================================

-- Inspection Forms
CREATE OR REPLACE FUNCTION equipment.update_inspection_form_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_inspection_form_updated_at
    BEFORE UPDATE ON equipment.si_inspection_forms
    FOR EACH ROW
    EXECUTE FUNCTION equipment.update_inspection_form_updated_at();

-- Inspection Form Fields
CREATE OR REPLACE FUNCTION equipment.update_form_field_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_form_field_updated_at
    BEFORE UPDATE ON equipment.si_inspection_form_fields
    FOR EACH ROW
    EXECUTE FUNCTION equipment.update_form_field_updated_at();

-- Inspection Plans
CREATE OR REPLACE FUNCTION equipment.update_inspection_plan_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_inspection_plan_updated_at
    BEFORE UPDATE ON equipment.si_inspection_plans
    FOR EACH ROW
    EXECUTE FUNCTION equipment.update_inspection_plan_updated_at();

-- Inspection Actions
CREATE OR REPLACE FUNCTION equipment.update_inspection_action_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_inspection_action_updated_at
    BEFORE UPDATE ON equipment.si_inspection_actions
    FOR EACH ROW
    EXECUTE FUNCTION equipment.update_inspection_action_updated_at();

-- Gauges
CREATE OR REPLACE FUNCTION equipment.update_gauge_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_gauge_updated_at
    BEFORE UPDATE ON equipment.si_gauges
    FOR EACH ROW
    EXECUTE FUNCTION equipment.update_gauge_updated_at();

-- Consumables
CREATE OR REPLACE FUNCTION equipment.update_consumable_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_consumable_updated_at
    BEFORE UPDATE ON equipment.si_consumables
    FOR EACH ROW
    EXECUTE FUNCTION equipment.update_consumable_updated_at();

-- Equipment Parts
CREATE OR REPLACE FUNCTION equipment.update_equipment_part_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_equipment_part_updated_at
    BEFORE UPDATE ON equipment.si_equipment_parts
    FOR EACH ROW
    EXECUTE FUNCTION equipment.update_equipment_part_updated_at();

-- Breakdowns
CREATE OR REPLACE FUNCTION equipment.update_breakdown_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_breakdown_updated_at
    BEFORE UPDATE ON equipment.si_breakdowns
    FOR EACH ROW
    EXECUTE FUNCTION equipment.update_breakdown_updated_at();

-- Deviations
CREATE OR REPLACE FUNCTION equipment.update_deviation_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_deviation_updated_at
    BEFORE UPDATE ON equipment.si_deviations
    FOR EACH ROW
    EXECUTE FUNCTION equipment.update_deviation_updated_at();

-- External Calibrations
CREATE OR REPLACE FUNCTION equipment.update_ext_calibration_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_ext_calibration_updated_at
    BEFORE UPDATE ON equipment.si_external_calibrations
    FOR EACH ROW
    EXECUTE FUNCTION equipment.update_ext_calibration_updated_at();

-- ============================================================
-- 12. TRIGGER - Auto calculate breakdown repair duration
-- ============================================================

CREATE OR REPLACE FUNCTION equipment.calculate_repair_duration()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.repair_completed_at IS NOT NULL AND NEW.repair_started_at IS NOT NULL THEN
        NEW.repair_duration_minutes = EXTRACT(EPOCH FROM (NEW.repair_completed_at - NEW.repair_started_at)) / 60.0;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_calculate_repair_duration
    BEFORE INSERT OR UPDATE ON equipment.si_breakdowns
    FOR EACH ROW
    EXECUTE FUNCTION equipment.calculate_repair_duration();

-- ============================================================
-- End of Migration V031
-- ============================================================
