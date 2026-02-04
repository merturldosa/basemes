-- ============================================================
-- Migration V016: Employee Management Extension (사원 관리 확장)
-- Description: 기존 사원 테이블에 스킬 매트릭스 및 HR 기능 추가
-- Author: Moon Myung-seop
-- Date: 2026-01-23
-- ============================================================

-- ============================================================
-- Alter existing si_employees table to add HR fields
-- ============================================================
ALTER TABLE core.si_employees
ADD COLUMN IF NOT EXISTS position VARCHAR(30),
ADD COLUMN IF NOT EXISTS blood_type VARCHAR(5),
ADD COLUMN IF NOT EXISTS education_level VARCHAR(30),
ADD COLUMN IF NOT EXISTS major VARCHAR(100),
ADD COLUMN IF NOT EXISTS certifications TEXT,
ADD COLUMN IF NOT EXISTS skills_summary TEXT,
ADD COLUMN IF NOT EXISTS emergency_relationship VARCHAR(50),
ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE;

-- Update employment_status column name if needed (already exists as employment_status)
-- Add comments
COMMENT ON COLUMN core.si_employees.position IS '직책/직위 (OPERATOR, TECHNICIAN, SUPERVISOR, ENGINEER, MANAGER, FOREMAN)';
COMMENT ON COLUMN core.si_employees.employment_status IS '재직상태 (ACTIVE, ON_LEAVE, RESIGNED, RETIRED, SUSPENDED)';

-- Create index on position if not exists
CREATE INDEX IF NOT EXISTS idx_employee_position ON core.si_employees(position);

-- ============================================================
-- Table: si_skill_matrix (스킬 매트릭스 마스터)
-- Description: 스킬 종류 및 정의
-- ============================================================
CREATE TABLE IF NOT EXISTS core.si_skill_matrix (
    skill_id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    skill_code VARCHAR(50) NOT NULL,
    skill_name VARCHAR(200) NOT NULL,
    skill_category VARCHAR(30) NOT NULL, -- TECHNICAL, OPERATIONAL, QUALITY, SAFETY, MANAGEMENT
    skill_level_definition TEXT, -- 레벨별 정의 (JSON 또는 텍스트)
    description TEXT,
    certification_required BOOLEAN DEFAULT FALSE,
    certification_name VARCHAR(200),
    validity_period_months INTEGER, -- 유효기간 (개월)
    remarks TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Foreign Keys
    CONSTRAINT fk_skill_matrix_tenant FOREIGN KEY (tenant_id)
        REFERENCES public.si_tenants(tenant_id) ON DELETE CASCADE,

    -- Unique Constraints
    CONSTRAINT uk_skill_matrix_code UNIQUE (tenant_id, skill_code)
);

-- Indexes for si_skill_matrix
CREATE INDEX IF NOT EXISTS idx_skill_matrix_tenant ON core.si_skill_matrix(tenant_id);
CREATE INDEX IF NOT EXISTS idx_skill_matrix_category ON core.si_skill_matrix(skill_category);
CREATE INDEX IF NOT EXISTS idx_skill_matrix_active ON core.si_skill_matrix(is_active);

-- ============================================================
-- Table: si_employee_skills (사원 스킬/자격)
-- Description: 사원별 보유 스킬 및 자격 정보
-- ============================================================
CREATE TABLE IF NOT EXISTS core.si_employee_skills (
    employee_skill_id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    employee_id BIGINT NOT NULL,
    skill_id BIGINT NOT NULL,

    -- Skill Level
    skill_level VARCHAR(30), -- BEGINNER, INTERMEDIATE, ADVANCED, EXPERT, MASTER
    skill_level_numeric INTEGER, -- 1~5

    -- Dates
    acquisition_date DATE, -- 습득일자
    expiry_date DATE, -- 만료일자
    last_assessment_date DATE, -- 최근 평가일자
    next_assessment_date DATE, -- 차기 평가일자

    -- Certification Info
    certification_no VARCHAR(100), -- 자격증 번호
    issuing_authority VARCHAR(200), -- 발급기관

    -- Assessment
    assessor_name VARCHAR(100), -- 평가자
    assessment_score DECIMAL(5,2), -- 평가점수
    assessment_result VARCHAR(30), -- PASS, FAIL, CONDITIONAL

    -- Common
    remarks TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Foreign Keys
    CONSTRAINT fk_employee_skill_tenant FOREIGN KEY (tenant_id)
        REFERENCES public.si_tenants(tenant_id) ON DELETE CASCADE,
    CONSTRAINT fk_employee_skill_employee FOREIGN KEY (employee_id)
        REFERENCES core.si_employees(employee_id) ON DELETE CASCADE,
    CONSTRAINT fk_employee_skill_skill FOREIGN KEY (skill_id)
        REFERENCES core.si_skill_matrix(skill_id) ON DELETE CASCADE,

    -- Unique Constraints
    CONSTRAINT uk_employee_skill UNIQUE (tenant_id, employee_id, skill_id)
);

-- Indexes for si_employee_skills
CREATE INDEX IF NOT EXISTS idx_employee_skill_tenant ON core.si_employee_skills(tenant_id);
CREATE INDEX IF NOT EXISTS idx_employee_skill_employee ON core.si_employee_skills(employee_id);
CREATE INDEX IF NOT EXISTS idx_employee_skill_skill ON core.si_employee_skills(skill_id);
CREATE INDEX IF NOT EXISTS idx_employee_skill_level ON core.si_employee_skills(skill_level);
CREATE INDEX IF NOT EXISTS idx_employee_skill_expiry ON core.si_employee_skills(expiry_date);
CREATE INDEX IF NOT EXISTS idx_employee_skill_active ON core.si_employee_skills(is_active);

-- ============================================================
-- Triggers for updated_at
-- ============================================================

-- Trigger for si_skill_matrix
DROP TRIGGER IF EXISTS trigger_skill_matrix_updated_at ON core.si_skill_matrix;
CREATE TRIGGER trigger_skill_matrix_updated_at
    BEFORE UPDATE ON core.si_skill_matrix
    FOR EACH ROW
    EXECUTE FUNCTION public.update_updated_at_column();

-- Trigger for si_employee_skills
DROP TRIGGER IF EXISTS trigger_employee_skill_updated_at ON core.si_employee_skills;
CREATE TRIGGER trigger_employee_skill_updated_at
    BEFORE UPDATE ON core.si_employee_skills
    FOR EACH ROW
    EXECUTE FUNCTION public.update_updated_at_column();

-- ============================================================
-- Comments
-- ============================================================

COMMENT ON TABLE core.si_skill_matrix IS '스킬 매트릭스 마스터 - 스킬 종류 및 정의';
COMMENT ON TABLE core.si_employee_skills IS '사원 스킬/자격 - 사원별 보유 스킬 정보';

COMMENT ON COLUMN core.si_employee_skills.skill_level IS '스킬 레벨 (BEGINNER, INTERMEDIATE, ADVANCED, EXPERT, MASTER)';
COMMENT ON COLUMN core.si_employee_skills.skill_level_numeric IS '스킬 레벨 숫자 (1~5)';
COMMENT ON COLUMN core.si_employee_skills.expiry_date IS '자격 만료일 (정기 갱신 필요시)';

-- ============================================================
-- Sample Data (Optional - for testing)
-- ============================================================

-- Insert sample skill categories for DEMO001 tenant
INSERT INTO core.si_skill_matrix (tenant_id, skill_code, skill_name, skill_category, description, certification_required, is_active)
VALUES
('DEMO001', 'WELDING', '용접', 'TECHNICAL', '각종 용접 작업 능력', true, TRUE),
('DEMO001', 'FORKLIFT', '지게차 운전', 'OPERATIONAL', '지게차 운전 자격', true, TRUE),
('DEMO001', 'QC_INSPECTION', '품질검사', 'QUALITY', '제품 품질 검사 능력', false, TRUE),
('DEMO001', 'SAFETY_MANAGER', '안전관리', 'SAFETY', '안전관리자 자격', true, TRUE),
('DEMO001', 'CNC_OPERATION', 'CNC 조작', 'TECHNICAL', 'CNC 기계 조작 능력', false, TRUE),
('DEMO001', 'INJECTION_MOLDING', '사출성형', 'TECHNICAL', '사출성형기 조작 능력', false, TRUE),
('DEMO001', 'ELECTRICAL', '전기', 'TECHNICAL', '전기 작업 자격', true, TRUE),
('DEMO001', 'FIRST_AID', '응급처치', 'SAFETY', '응급처치 자격', true, TRUE)
ON CONFLICT DO NOTHING;
