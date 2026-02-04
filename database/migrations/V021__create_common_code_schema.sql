-- V021: Common Code Management Schema
-- 공통 코드 관리 스키마
-- Author: Moon Myung-seop
-- Date: 2026-01-25

-- ============================================================
-- Common Code Groups Table
-- 공통 코드 그룹 테이블
-- ============================================================
CREATE TABLE IF NOT EXISTS common.common_code_groups (
    code_group_id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    code_group VARCHAR(50) NOT NULL,
    code_group_name VARCHAR(100) NOT NULL,
    description TEXT,
    is_system BOOLEAN DEFAULT FALSE,
    display_order INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),

    CONSTRAINT fk_code_group_tenant FOREIGN KEY (tenant_id)
        REFERENCES common.tenants(tenant_id) ON DELETE CASCADE,
    CONSTRAINT uk_code_group_tenant UNIQUE (tenant_id, code_group)
);

COMMENT ON TABLE common.common_code_groups IS '공통 코드 그룹';
COMMENT ON COLUMN common.common_code_groups.code_group_id IS '코드 그룹 ID (PK)';
COMMENT ON COLUMN common.common_code_groups.tenant_id IS '테넌트 ID (FK)';
COMMENT ON COLUMN common.common_code_groups.code_group IS '코드 그룹 (예: ORDER_STATUS, PRODUCT_TYPE)';
COMMENT ON COLUMN common.common_code_groups.code_group_name IS '코드 그룹명';
COMMENT ON COLUMN common.common_code_groups.description IS '설명';
COMMENT ON COLUMN common.common_code_groups.is_system IS '시스템 코드 여부 (삭제 불가)';
COMMENT ON COLUMN common.common_code_groups.display_order IS '표시 순서';
COMMENT ON COLUMN common.common_code_groups.is_active IS '활성 여부';

CREATE INDEX idx_code_group_tenant ON common.common_code_groups(tenant_id);
CREATE INDEX idx_code_group_active ON common.common_code_groups(is_active);

-- ============================================================
-- Common Code Details Table
-- 공통 코드 상세 테이블
-- ============================================================
CREATE TABLE IF NOT EXISTS common.common_code_details (
    code_detail_id BIGSERIAL PRIMARY KEY,
    code_group_id BIGINT NOT NULL,
    code VARCHAR(50) NOT NULL,
    code_name VARCHAR(100) NOT NULL,
    description TEXT,
    display_order INTEGER DEFAULT 0,
    is_default BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,

    -- Extended fields for flexible use
    value1 VARCHAR(255),
    value2 VARCHAR(255),
    value3 VARCHAR(255),
    value4 VARCHAR(255),
    value5 VARCHAR(255),

    -- Color/Icon for UI
    color_code VARCHAR(20),
    icon_name VARCHAR(50),

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),

    CONSTRAINT fk_code_detail_group FOREIGN KEY (code_group_id)
        REFERENCES common.common_code_groups(code_group_id) ON DELETE CASCADE,
    CONSTRAINT uk_code_detail UNIQUE (code_group_id, code)
);

COMMENT ON TABLE common.common_code_details IS '공통 코드 상세';
COMMENT ON COLUMN common.common_code_details.code_detail_id IS '코드 상세 ID (PK)';
COMMENT ON COLUMN common.common_code_details.code_group_id IS '코드 그룹 ID (FK)';
COMMENT ON COLUMN common.common_code_details.code IS '코드 값';
COMMENT ON COLUMN common.common_code_details.code_name IS '코드명';
COMMENT ON COLUMN common.common_code_details.description IS '설명';
COMMENT ON COLUMN common.common_code_details.display_order IS '표시 순서';
COMMENT ON COLUMN common.common_code_details.is_default IS '기본값 여부';
COMMENT ON COLUMN common.common_code_details.is_active IS '활성 여부';
COMMENT ON COLUMN common.common_code_details.value1 IS '확장 필드 1';
COMMENT ON COLUMN common.common_code_details.value2 IS '확장 필드 2';
COMMENT ON COLUMN common.common_code_details.value3 IS '확장 필드 3';
COMMENT ON COLUMN common.common_code_details.value4 IS '확장 필드 4';
COMMENT ON COLUMN common.common_code_details.value5 IS '확장 필드 5';
COMMENT ON COLUMN common.common_code_details.color_code IS 'UI 표시용 색상 코드';
COMMENT ON COLUMN common.common_code_details.icon_name IS 'UI 표시용 아이콘명';

CREATE INDEX idx_code_detail_group ON common.common_code_details(code_group_id);
CREATE INDEX idx_code_detail_active ON common.common_code_details(is_active);
CREATE INDEX idx_code_detail_order ON common.common_code_details(display_order);

-- ============================================================
-- Insert System Code Groups
-- 시스템 코드 그룹 초기 데이터
-- ============================================================

-- Note: tenant_id는 실제 테넌트 생성 시 추가되어야 함
-- 여기서는 스키마 구조만 정의

-- Example system codes (to be inserted via seeds):
-- ORDER_STATUS: DRAFT, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED
-- PRODUCT_TYPE: RAW_MATERIAL, SEMI_FINISHED, FINISHED_GOOD
-- WAREHOUSE_TYPE: RAW_MATERIAL, WIP, FINISHED_GOOD, QUARANTINE, SCRAP
-- QUALITY_STATUS: PENDING, PASSED, FAILED, CONDITIONAL
-- INSPECTION_TYPE: IQC, PQC, OQC, FQC
-- TRANSACTION_TYPE: IN_RECEIVE, OUT_ISSUE, IN_RETURN, OUT_RETURN, ADJUSTMENT
-- APPROVAL_STATUS: PENDING, APPROVED, REJECTED, CANCELLED
-- PRIORITY: LOW, NORMAL, HIGH, URGENT
-- UNIT: EA, KG, M, L, BOX, SET
-- GENDER: MALE, FEMALE, OTHER
-- EMPLOYMENT_TYPE: FULL_TIME, PART_TIME, CONTRACT, INTERN
-- SHIFT: DAY, NIGHT, EVENING
-- DEFECT_TYPE: SCRATCH, CRACK, DIMENSION, COLOR, CONTAMINATION
-- CLAIM_TYPE: QUALITY, DELIVERY, QUANTITY, DOCUMENTATION
-- EQUIPMENT_STATUS: AVAILABLE, IN_USE, UNDER_MAINTENANCE, DOWN
