-- ============================================================================
-- V027: Production Schedule Schema
-- ============================================================================
-- Description: 생산 일정 관리 스키마 생성
--              WorkOrder와 ProcessRouting을 연동하여 상세 일정 관리
--
-- Tables:
--   - mes.sd_production_schedules: 생산 일정 테이블
--
-- Changes:
--   - mes.sd_work_orders: routing_id 컬럼 추가
--
-- Features:
--   - WorkOrder-ProcessRouting 연동
--   - 공정별 상세 일정 관리
--   - 리소스 할당 (설비, 작업자)
--   - 지연 자동 감지
--   - 진행률 추적
--
-- Author: SDS MES Development Team
-- Date: 2026-01-27
-- ============================================================================

-- ============================================================================
-- 1. WorkOrder 테이블 확장
-- ============================================================================

-- routing_id 컬럼 추가
ALTER TABLE mes.sd_work_orders
ADD COLUMN routing_id BIGINT;

-- Foreign Key 추가
ALTER TABLE mes.sd_work_orders
ADD CONSTRAINT fk_work_order_routing
    FOREIGN KEY (routing_id)
    REFERENCES mes.sd_process_routings(routing_id)
    ON DELETE SET NULL;

-- Index 추가
CREATE INDEX idx_work_order_routing ON mes.sd_work_orders(routing_id);

-- Comment 추가
COMMENT ON COLUMN mes.sd_work_orders.routing_id IS '공정 라우팅 ID (FK) - 복합 공정 제품용';

-- ============================================================================
-- 2. 생산 일정 테이블
-- ============================================================================

CREATE TABLE mes.sd_production_schedules (
    -- Primary Key
    schedule_id BIGSERIAL PRIMARY KEY,

    -- 테넌트 정보
    tenant_id VARCHAR(50) NOT NULL,

    -- 작업 지시 정보
    work_order_id BIGINT NOT NULL,

    -- 공정 라우팅 단계 정보
    routing_step_id BIGINT NOT NULL,
    sequence_order INTEGER NOT NULL,

    -- 계획 일정
    planned_start_time TIMESTAMP NOT NULL,
    planned_end_time TIMESTAMP NOT NULL,
    planned_duration INTEGER NOT NULL,  -- 분 단위

    -- 실제 일정
    actual_start_time TIMESTAMP,
    actual_end_time TIMESTAMP,
    actual_duration INTEGER,

    -- 리소스 할당
    assigned_equipment_id BIGINT,
    assigned_workers INTEGER DEFAULT 1,  -- 할당된 작업자 수
    assigned_user_id BIGINT,              -- 담당자

    -- 상태: SCHEDULED, READY, IN_PROGRESS, COMPLETED, DELAYED, CANCELLED
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',

    -- 진행률 (%)
    progress_rate DECIMAL(5, 2) DEFAULT 0.00,

    -- 지연 정보
    is_delayed BOOLEAN DEFAULT false,
    delay_minutes INTEGER DEFAULT 0,
    delay_reason TEXT,

    -- 비고
    remarks TEXT,

    -- 감사 정보
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign Keys
    CONSTRAINT fk_schedule_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES core.sd_tenants(tenant_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_schedule_work_order
        FOREIGN KEY (work_order_id)
        REFERENCES mes.sd_work_orders(work_order_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_schedule_routing_step
        FOREIGN KEY (routing_step_id)
        REFERENCES mes.sd_process_routing_steps(routing_step_id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_schedule_equipment
        FOREIGN KEY (assigned_equipment_id)
        REFERENCES equipment.sd_equipments(equipment_id)
        ON DELETE SET NULL,

    CONSTRAINT fk_schedule_user
        FOREIGN KEY (assigned_user_id)
        REFERENCES core.sd_users(user_id)
        ON DELETE SET NULL,

    -- Unique Constraints
    CONSTRAINT uk_schedule_work_order_step
        UNIQUE (work_order_id, routing_step_id),

    -- Check Constraints
    CONSTRAINT chk_schedule_times
        CHECK (planned_end_time > planned_start_time),

    CONSTRAINT chk_schedule_duration
        CHECK (planned_duration > 0),

    CONSTRAINT chk_schedule_progress
        CHECK (progress_rate >= 0 AND progress_rate <= 100),

    CONSTRAINT chk_schedule_delay
        CHECK (delay_minutes >= 0),

    CONSTRAINT chk_schedule_workers
        CHECK (assigned_workers >= 0),

    CONSTRAINT chk_schedule_sequence
        CHECK (sequence_order > 0)
);

-- Indexes
CREATE INDEX idx_schedule_tenant ON mes.sd_production_schedules(tenant_id);
CREATE INDEX idx_schedule_work_order ON mes.sd_production_schedules(work_order_id);
CREATE INDEX idx_schedule_routing_step ON mes.sd_production_schedules(routing_step_id);
CREATE INDEX idx_schedule_status ON mes.sd_production_schedules(status);
CREATE INDEX idx_schedule_planned_time ON mes.sd_production_schedules(planned_start_time, planned_end_time);
CREATE INDEX idx_schedule_equipment ON mes.sd_production_schedules(assigned_equipment_id);
CREATE INDEX idx_schedule_sequence ON mes.sd_production_schedules(work_order_id, sequence_order);
CREATE INDEX idx_schedule_delayed ON mes.sd_production_schedules(is_delayed);
CREATE INDEX idx_schedule_user ON mes.sd_production_schedules(assigned_user_id);

-- Comments
COMMENT ON TABLE mes.sd_production_schedules IS '생산 일정 테이블 - 작업 지시의 공정별 상세 일정';
COMMENT ON COLUMN mes.sd_production_schedules.schedule_id IS '일정 ID (PK)';
COMMENT ON COLUMN mes.sd_production_schedules.tenant_id IS '테넌트 ID';
COMMENT ON COLUMN mes.sd_production_schedules.work_order_id IS '작업 지시 ID (FK)';
COMMENT ON COLUMN mes.sd_production_schedules.routing_step_id IS '공정 라우팅 단계 ID (FK)';
COMMENT ON COLUMN mes.sd_production_schedules.sequence_order IS '공정 순서';
COMMENT ON COLUMN mes.sd_production_schedules.planned_start_time IS '계획 시작 시간';
COMMENT ON COLUMN mes.sd_production_schedules.planned_end_time IS '계획 종료 시간';
COMMENT ON COLUMN mes.sd_production_schedules.planned_duration IS '계획 소요 시간 (분)';
COMMENT ON COLUMN mes.sd_production_schedules.actual_start_time IS '실제 시작 시간';
COMMENT ON COLUMN mes.sd_production_schedules.actual_end_time IS '실제 종료 시간';
COMMENT ON COLUMN mes.sd_production_schedules.actual_duration IS '실제 소요 시간 (분) - 자동 계산';
COMMENT ON COLUMN mes.sd_production_schedules.assigned_equipment_id IS '할당된 설비 ID (FK)';
COMMENT ON COLUMN mes.sd_production_schedules.assigned_workers IS '할당된 작업자 수';
COMMENT ON COLUMN mes.sd_production_schedules.assigned_user_id IS '담당자 ID (FK)';
COMMENT ON COLUMN mes.sd_production_schedules.status IS '일정 상태 (SCHEDULED, READY, IN_PROGRESS, COMPLETED, DELAYED, CANCELLED)';
COMMENT ON COLUMN mes.sd_production_schedules.progress_rate IS '진행률 (%)';
COMMENT ON COLUMN mes.sd_production_schedules.is_delayed IS '지연 여부';
COMMENT ON COLUMN mes.sd_production_schedules.delay_minutes IS '지연 시간 (분)';
COMMENT ON COLUMN mes.sd_production_schedules.delay_reason IS '지연 사유';
COMMENT ON COLUMN mes.sd_production_schedules.remarks IS '비고';

-- ============================================================================
-- 3. 트리거 함수 및 트리거
-- ============================================================================

-- 3.1 updated_at 자동 갱신 트리거 함수
CREATE OR REPLACE FUNCTION mes.update_schedule_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 3.2 updated_at 트리거
CREATE TRIGGER trigger_schedule_updated_at
    BEFORE UPDATE ON mes.sd_production_schedules
    FOR EACH ROW
    EXECUTE FUNCTION mes.update_schedule_updated_at();

-- 3.3 actual_duration 자동 계산 트리거 함수
CREATE OR REPLACE FUNCTION mes.calculate_actual_duration()
RETURNS TRIGGER AS $$
BEGIN
    -- 실제 시작/종료 시간이 모두 있으면 자동 계산 (분 단위)
    IF NEW.actual_start_time IS NOT NULL AND NEW.actual_end_time IS NOT NULL THEN
        NEW.actual_duration := EXTRACT(EPOCH FROM (NEW.actual_end_time - NEW.actual_start_time)) / 60;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 3.4 actual_duration 계산 트리거
CREATE TRIGGER trigger_calculate_actual_duration
    BEFORE INSERT OR UPDATE ON mes.sd_production_schedules
    FOR EACH ROW
    EXECUTE FUNCTION mes.calculate_actual_duration();

-- 3.5 지연 자동 감지 트리거 함수
CREATE OR REPLACE FUNCTION mes.check_schedule_delay()
RETURNS TRIGGER AS $$
BEGIN
    -- 현재 시간이 계획 종료 시간을 초과했는데 완료되지 않은 경우
    IF NEW.status NOT IN ('COMPLETED', 'CANCELLED')
       AND CURRENT_TIMESTAMP > NEW.planned_end_time THEN
        NEW.is_delayed := true;
        NEW.delay_minutes := EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - NEW.planned_end_time)) / 60;
    ELSE
        -- 완료되었거나 계획 시간 내인 경우
        NEW.is_delayed := false;
        NEW.delay_minutes := 0;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 3.6 지연 감지 트리거
CREATE TRIGGER trigger_check_delay
    BEFORE INSERT OR UPDATE ON mes.sd_production_schedules
    FOR EACH ROW
    EXECUTE FUNCTION mes.check_schedule_delay();

-- ============================================================================
-- 4. 권한 설정
-- ============================================================================

-- 테이블 권한
GRANT SELECT, INSERT, UPDATE, DELETE ON mes.sd_production_schedules TO mes_admin;
GRANT SELECT ON mes.sd_production_schedules TO mes_user;

-- 시퀀스 권한
GRANT USAGE, SELECT ON SEQUENCE mes.sd_production_schedules_schedule_id_seq TO mes_admin;

-- ============================================================================
-- 5. 뷰 생성 (선택사항)
-- ============================================================================

-- 일정 요약 뷰
CREATE OR REPLACE VIEW mes.v_schedule_summary AS
SELECT
    s.schedule_id,
    s.tenant_id,
    wo.work_order_no,
    p.product_code,
    p.product_name,
    proc.process_code,
    proc.process_name,
    s.sequence_order,
    s.planned_start_time,
    s.planned_end_time,
    s.planned_duration,
    s.actual_start_time,
    s.actual_end_time,
    s.actual_duration,
    e.equipment_code,
    e.equipment_name,
    s.assigned_workers,
    u.username as assigned_user_name,
    s.status,
    s.progress_rate,
    s.is_delayed,
    s.delay_minutes,
    s.delay_reason,
    s.created_at,
    s.updated_at
FROM mes.sd_production_schedules s
INNER JOIN mes.sd_work_orders wo ON s.work_order_id = wo.work_order_id
INNER JOIN mes.sd_products p ON wo.product_id = p.product_id
INNER JOIN mes.sd_process_routing_steps rs ON s.routing_step_id = rs.routing_step_id
INNER JOIN mes.sd_processes proc ON rs.process_id = proc.process_id
LEFT JOIN equipment.sd_equipments e ON s.assigned_equipment_id = e.equipment_id
LEFT JOIN core.sd_users u ON s.assigned_user_id = u.user_id;

COMMENT ON VIEW mes.v_schedule_summary IS '생산 일정 요약 뷰 - Gantt Chart 및 대시보드용';

-- 뷰 권한
GRANT SELECT ON mes.v_schedule_summary TO mes_admin, mes_user;

-- ============================================================================
-- End of V027__create_production_schedule_schema.sql
-- ============================================================================
