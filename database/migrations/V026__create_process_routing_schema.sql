-- ============================================================================
-- V026: Process Routing Schema
-- ============================================================================
-- Description: 공정 라우팅 스키마 생성
--              제품별 공정 순서 및 작업 표준을 정의하는 Process Routing 시스템
--
-- Tables:
--   - mes.sd_process_routings: 공정 라우팅 마스터 테이블
--   - mes.sd_process_routing_steps: 공정 라우팅 상세(단계) 테이블
--
-- Features:
--   - Product별 공정 순서 정의
--   - 버전 관리 (routing_code + version)
--   - 시간 정보 (표준, 준비, 대기)
--   - 병렬/선택 공정 지원
--   - 대체 공정 정의
--   - 품질 체크 요구사항
--   - 총 표준 시간 자동 계산
--
-- Author: SDS MES Development Team
-- Date: 2026-01-27
-- ============================================================================

-- ============================================================================
-- 1. 공정 라우팅 마스터 테이블
-- ============================================================================

CREATE TABLE mes.sd_process_routings (
    -- Primary Key
    routing_id BIGSERIAL PRIMARY KEY,

    -- 테넌트 정보
    tenant_id VARCHAR(50) NOT NULL,

    -- 제품 정보
    product_id BIGINT NOT NULL,

    -- 라우팅 기본 정보
    routing_code VARCHAR(50) NOT NULL,
    routing_name VARCHAR(200) NOT NULL,
    version VARCHAR(20) NOT NULL DEFAULT '1.0',

    -- 유효 기간
    effective_date DATE NOT NULL,
    expiry_date DATE,

    -- 상태
    is_active BOOLEAN NOT NULL DEFAULT true,

    -- 시간 정보
    total_standard_time INTEGER,  -- 총 표준 시간 (분) - 자동 계산

    -- 비고
    remarks TEXT,

    -- 감사 정보
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign Keys
    CONSTRAINT fk_routing_tenant
        FOREIGN KEY (tenant_id) REFERENCES core.sd_tenants(tenant_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_routing_product
        FOREIGN KEY (product_id) REFERENCES mes.sd_products(product_id)
        ON DELETE RESTRICT,

    -- Unique Constraints
    CONSTRAINT uk_routing_code_version
        UNIQUE (tenant_id, routing_code, version),

    -- Check Constraints
    CONSTRAINT chk_routing_dates
        CHECK (expiry_date IS NULL OR expiry_date >= effective_date),

    CONSTRAINT chk_routing_total_time
        CHECK (total_standard_time IS NULL OR total_standard_time >= 0)
);

-- Indexes
CREATE INDEX idx_routing_tenant ON mes.sd_process_routings(tenant_id);
CREATE INDEX idx_routing_product ON mes.sd_process_routings(product_id);
CREATE INDEX idx_routing_code ON mes.sd_process_routings(routing_code);
CREATE INDEX idx_routing_effective_date ON mes.sd_process_routings(effective_date);
CREATE INDEX idx_routing_active ON mes.sd_process_routings(is_active);
CREATE INDEX idx_routing_version ON mes.sd_process_routings(version);

-- Comments
COMMENT ON TABLE mes.sd_process_routings IS '공정 라우팅 마스터 테이블';
COMMENT ON COLUMN mes.sd_process_routings.routing_id IS '라우팅 ID (PK)';
COMMENT ON COLUMN mes.sd_process_routings.tenant_id IS '테넌트 ID';
COMMENT ON COLUMN mes.sd_process_routings.product_id IS '제품 ID';
COMMENT ON COLUMN mes.sd_process_routings.routing_code IS '라우팅 코드';
COMMENT ON COLUMN mes.sd_process_routings.routing_name IS '라우팅 명칭';
COMMENT ON COLUMN mes.sd_process_routings.version IS '버전';
COMMENT ON COLUMN mes.sd_process_routings.effective_date IS '유효 시작일';
COMMENT ON COLUMN mes.sd_process_routings.expiry_date IS '유효 종료일';
COMMENT ON COLUMN mes.sd_process_routings.is_active IS '활성화 여부';
COMMENT ON COLUMN mes.sd_process_routings.total_standard_time IS '총 표준 시간 (분) - 자동 계산';
COMMENT ON COLUMN mes.sd_process_routings.remarks IS '비고';

-- ============================================================================
-- 2. 공정 라우팅 상세(단계) 테이블
-- ============================================================================

CREATE TABLE mes.sd_process_routing_steps (
    -- Primary Key
    routing_step_id BIGSERIAL PRIMARY KEY,

    -- 라우팅 정보
    routing_id BIGINT NOT NULL,

    -- 순서 정보
    sequence_order INTEGER NOT NULL,

    -- 공정 정보
    process_id BIGINT NOT NULL,

    -- 시간 정보 (분 단위)
    standard_time INTEGER NOT NULL DEFAULT 0,  -- 표준 작업 시간
    setup_time INTEGER DEFAULT 0,              -- 준비 시간
    wait_time INTEGER DEFAULT 0,               -- 대기 시간

    -- 리소스 정보
    required_workers INTEGER DEFAULT 1,        -- 필요 작업 인원
    equipment_id BIGINT,                       -- 필요 설비

    -- 공정 흐름 제어
    is_parallel BOOLEAN DEFAULT false,         -- 병렬 공정 여부
    parallel_group INTEGER,                    -- 병렬 그룹 번호
    is_optional BOOLEAN DEFAULT false,         -- 선택 공정 여부

    -- 대체 공정
    alternate_process_id BIGINT,               -- 대체 공정 ID

    -- 품질 요구사항
    quality_check_required BOOLEAN DEFAULT false,  -- 품질 검사 필요 여부
    quality_standard TEXT,                         -- 품질 기준

    -- 비고
    remarks TEXT,

    -- 감사 정보
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign Keys
    CONSTRAINT fk_step_routing
        FOREIGN KEY (routing_id) REFERENCES mes.sd_process_routings(routing_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_step_process
        FOREIGN KEY (process_id) REFERENCES mes.sd_processes(process_id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_step_equipment
        FOREIGN KEY (equipment_id) REFERENCES equipment.sd_equipments(equipment_id)
        ON DELETE SET NULL,

    CONSTRAINT fk_step_alternate_process
        FOREIGN KEY (alternate_process_id) REFERENCES mes.sd_processes(process_id)
        ON DELETE SET NULL,

    -- Unique Constraints
    CONSTRAINT uk_step_sequence
        UNIQUE (routing_id, sequence_order),

    -- Check Constraints
    CONSTRAINT chk_step_times
        CHECK (standard_time >= 0 AND setup_time >= 0 AND wait_time >= 0),

    CONSTRAINT chk_step_workers
        CHECK (required_workers > 0),

    CONSTRAINT chk_step_sequence
        CHECK (sequence_order > 0),

    CONSTRAINT chk_step_parallel_group
        CHECK (parallel_group IS NULL OR (is_parallel = true AND parallel_group > 0))
);

-- Indexes
CREATE INDEX idx_step_routing ON mes.sd_process_routing_steps(routing_id);
CREATE INDEX idx_step_process ON mes.sd_process_routing_steps(process_id);
CREATE INDEX idx_step_equipment ON mes.sd_process_routing_steps(equipment_id);
CREATE INDEX idx_step_sequence ON mes.sd_process_routing_steps(routing_id, sequence_order);
CREATE INDEX idx_step_parallel ON mes.sd_process_routing_steps(is_parallel, parallel_group);

-- Comments
COMMENT ON TABLE mes.sd_process_routing_steps IS '공정 라우팅 상세(단계) 테이블';
COMMENT ON COLUMN mes.sd_process_routing_steps.routing_step_id IS '라우팅 단계 ID (PK)';
COMMENT ON COLUMN mes.sd_process_routing_steps.routing_id IS '라우팅 ID (FK)';
COMMENT ON COLUMN mes.sd_process_routing_steps.sequence_order IS '공정 순서';
COMMENT ON COLUMN mes.sd_process_routing_steps.process_id IS '공정 ID (FK)';
COMMENT ON COLUMN mes.sd_process_routing_steps.standard_time IS '표준 작업 시간 (분)';
COMMENT ON COLUMN mes.sd_process_routing_steps.setup_time IS '준비 시간 (분)';
COMMENT ON COLUMN mes.sd_process_routing_steps.wait_time IS '대기 시간 (분)';
COMMENT ON COLUMN mes.sd_process_routing_steps.required_workers IS '필요 작업 인원';
COMMENT ON COLUMN mes.sd_process_routing_steps.equipment_id IS '필요 설비 ID (FK)';
COMMENT ON COLUMN mes.sd_process_routing_steps.is_parallel IS '병렬 공정 여부';
COMMENT ON COLUMN mes.sd_process_routing_steps.parallel_group IS '병렬 그룹 번호';
COMMENT ON COLUMN mes.sd_process_routing_steps.is_optional IS '선택 공정 여부';
COMMENT ON COLUMN mes.sd_process_routing_steps.alternate_process_id IS '대체 공정 ID (FK)';
COMMENT ON COLUMN mes.sd_process_routing_steps.quality_check_required IS '품질 검사 필요 여부';
COMMENT ON COLUMN mes.sd_process_routing_steps.quality_standard IS '품질 기준';
COMMENT ON COLUMN mes.sd_process_routing_steps.remarks IS '비고';

-- ============================================================================
-- 3. 트리거 함수 및 트리거
-- ============================================================================

-- 3.1 updated_at 자동 갱신 트리거 함수
CREATE OR REPLACE FUNCTION mes.update_routing_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 3.2 라우팅 마스터 updated_at 트리거
CREATE TRIGGER trigger_routing_updated_at
    BEFORE UPDATE ON mes.sd_process_routings
    FOR EACH ROW
    EXECUTE FUNCTION mes.update_routing_updated_at();

-- 3.3 라우팅 단계 updated_at 트리거
CREATE TRIGGER trigger_routing_step_updated_at
    BEFORE UPDATE ON mes.sd_process_routing_steps
    FOR EACH ROW
    EXECUTE FUNCTION mes.update_routing_updated_at();

-- 3.4 총 표준 시간 자동 계산 트리거 함수
CREATE OR REPLACE FUNCTION mes.update_routing_total_time()
RETURNS TRIGGER AS $$
DECLARE
    v_routing_id BIGINT;
    v_total_time INTEGER;
BEGIN
    -- 대상 routing_id 결정
    v_routing_id := COALESCE(NEW.routing_id, OLD.routing_id);

    -- 총 시간 계산
    -- 병렬 공정은 parallel_group별로 최대값만 반영
    -- 순차 공정은 모두 합산
    WITH step_times AS (
        SELECT
            CASE
                WHEN is_parallel = true THEN parallel_group
                ELSE routing_step_id  -- 각 순차 공정을 개별 그룹으로 취급
            END as group_id,
            is_parallel,
            MAX(standard_time + COALESCE(setup_time, 0) + COALESCE(wait_time, 0)) as group_time
        FROM mes.sd_process_routing_steps
        WHERE routing_id = v_routing_id
        GROUP BY
            CASE
                WHEN is_parallel = true THEN parallel_group
                ELSE routing_step_id
            END,
            is_parallel
    )
    SELECT COALESCE(SUM(group_time), 0)
    INTO v_total_time
    FROM step_times;

    -- 마스터 테이블 업데이트
    UPDATE mes.sd_process_routings
    SET total_standard_time = v_total_time
    WHERE routing_id = v_routing_id;

    RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;

-- 3.5 총 표준 시간 계산 트리거
CREATE TRIGGER trigger_calc_total_time
    AFTER INSERT OR UPDATE OR DELETE ON mes.sd_process_routing_steps
    FOR EACH ROW
    EXECUTE FUNCTION mes.update_routing_total_time();

-- ============================================================================
-- 4. 권한 설정
-- ============================================================================

-- 테이블 권한
GRANT SELECT, INSERT, UPDATE, DELETE ON mes.sd_process_routings TO mes_admin;
GRANT SELECT, INSERT, UPDATE, DELETE ON mes.sd_process_routing_steps TO mes_admin;

GRANT SELECT ON mes.sd_process_routings TO mes_user;
GRANT SELECT ON mes.sd_process_routing_steps TO mes_user;

-- 시퀀스 권한
GRANT USAGE, SELECT ON SEQUENCE mes.sd_process_routings_routing_id_seq TO mes_admin;
GRANT USAGE, SELECT ON SEQUENCE mes.sd_process_routing_steps_routing_step_id_seq TO mes_admin;

-- ============================================================================
-- 5. 초기 데이터 (선택사항)
-- ============================================================================

-- 추후 필요시 테스트 데이터 추가

-- ============================================================================
-- End of V026__create_process_routing_schema.sql
-- ============================================================================
