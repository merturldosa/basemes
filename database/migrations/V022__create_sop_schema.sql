-- V022: SOP (Standard Operating Procedure) Management Schema
-- SOP (표준 작업 절차) 관리 스키마
-- Author: Moon Myung-seop
-- Date: 2026-01-25

-- ============================================================
-- Document Templates Table
-- 문서 양식 테이블
-- ============================================================
CREATE TABLE IF NOT EXISTS common.document_templates (
    template_id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    template_code VARCHAR(50) NOT NULL,
    template_name VARCHAR(200) NOT NULL,
    description TEXT,
    template_type VARCHAR(50) NOT NULL, -- SOP, CHECKLIST, INSPECTION_SHEET, REPORT
    category VARCHAR(50), -- PRODUCTION, WAREHOUSE, QUALITY, FACILITY, COMMON

    -- File information
    file_name VARCHAR(255),
    file_path VARCHAR(500),
    file_type VARCHAR(50), -- EXCEL, WORD, PDF, HTML
    file_size BIGINT,

    -- Template content (for HTML templates)
    template_content TEXT,

    -- Version management
    version VARCHAR(20) DEFAULT '1.0',
    is_latest BOOLEAN DEFAULT TRUE,

    display_order INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),

    CONSTRAINT fk_template_tenant FOREIGN KEY (tenant_id)
        REFERENCES common.tenants(tenant_id) ON DELETE CASCADE,
    CONSTRAINT uk_template_code UNIQUE (tenant_id, template_code, version)
);

COMMENT ON TABLE common.document_templates IS '문서 양식 템플릿';
COMMENT ON COLUMN common.document_templates.template_id IS '템플릿 ID (PK)';
COMMENT ON COLUMN common.document_templates.tenant_id IS '테넌트 ID (FK)';
COMMENT ON COLUMN common.document_templates.template_code IS '템플릿 코드';
COMMENT ON COLUMN common.document_templates.template_name IS '템플릿명';
COMMENT ON COLUMN common.document_templates.template_type IS '템플릿 유형';
COMMENT ON COLUMN common.document_templates.category IS '카테고리';
COMMENT ON COLUMN common.document_templates.file_path IS '파일 경로';
COMMENT ON COLUMN common.document_templates.template_content IS '템플릿 내용 (HTML)';
COMMENT ON COLUMN common.document_templates.version IS '버전';
COMMENT ON COLUMN common.document_templates.is_latest IS '최신 버전 여부';

CREATE INDEX idx_template_tenant ON common.document_templates(tenant_id);
CREATE INDEX idx_template_type ON common.document_templates(template_type);
CREATE INDEX idx_template_category ON common.document_templates(category);
CREATE INDEX idx_template_active ON common.document_templates(is_active);
CREATE INDEX idx_template_latest ON common.document_templates(is_latest);

-- ============================================================
-- SOP (Standard Operating Procedure) Table
-- 표준 작업 절차 테이블
-- ============================================================
CREATE TABLE IF NOT EXISTS common.sops (
    sop_id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    sop_code VARCHAR(50) NOT NULL,
    sop_name VARCHAR(200) NOT NULL,
    description TEXT,

    -- SOP classification
    sop_type VARCHAR(50) NOT NULL, -- PRODUCTION, WAREHOUSE, QUALITY, FACILITY, SAFETY, MAINTENANCE
    category VARCHAR(50), -- Sub-category
    target_process VARCHAR(100), -- Target process/operation

    -- Template link
    template_id BIGINT,

    -- Version management
    version VARCHAR(20) DEFAULT '1.0',
    revision_date DATE,
    effective_date DATE,
    review_date DATE,
    next_review_date DATE,

    -- Approval workflow
    approval_status VARCHAR(50) DEFAULT 'DRAFT', -- DRAFT, PENDING, APPROVED, REJECTED, OBSOLETE
    approved_by BIGINT,
    approved_at TIMESTAMP,

    -- Document information
    document_url VARCHAR(500),
    attachments JSONB, -- Array of attachment info

    -- Access control
    required_role VARCHAR(100), -- Required role to execute this SOP
    restricted BOOLEAN DEFAULT FALSE,

    display_order INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),

    CONSTRAINT fk_sop_tenant FOREIGN KEY (tenant_id)
        REFERENCES common.tenants(tenant_id) ON DELETE CASCADE,
    CONSTRAINT fk_sop_template FOREIGN KEY (template_id)
        REFERENCES common.document_templates(template_id) ON DELETE SET NULL,
    CONSTRAINT fk_sop_approver FOREIGN KEY (approved_by)
        REFERENCES common.users(user_id) ON DELETE SET NULL,
    CONSTRAINT uk_sop_code UNIQUE (tenant_id, sop_code, version)
);

COMMENT ON TABLE common.sops IS '표준 작업 절차 (SOP)';
COMMENT ON COLUMN common.sops.sop_id IS 'SOP ID (PK)';
COMMENT ON COLUMN common.sops.tenant_id IS '테넌트 ID (FK)';
COMMENT ON COLUMN common.sops.sop_code IS 'SOP 코드';
COMMENT ON COLUMN common.sops.sop_name IS 'SOP 명칭';
COMMENT ON COLUMN common.sops.sop_type IS 'SOP 유형';
COMMENT ON COLUMN common.sops.template_id IS '연결된 문서 양식 ID';
COMMENT ON COLUMN common.sops.version IS '버전';
COMMENT ON COLUMN common.sops.approval_status IS '승인 상태';
COMMENT ON COLUMN common.sops.required_role IS '실행 필요 권한';

CREATE INDEX idx_sop_tenant ON common.sops(tenant_id);
CREATE INDEX idx_sop_type ON common.sops(sop_type);
CREATE INDEX idx_sop_category ON common.sops(category);
CREATE INDEX idx_sop_status ON common.sops(approval_status);
CREATE INDEX idx_sop_active ON common.sops(is_active);
CREATE INDEX idx_sop_effective ON common.sops(effective_date);

-- ============================================================
-- SOP Steps Table
-- SOP 단계 테이블
-- ============================================================
CREATE TABLE IF NOT EXISTS common.sop_steps (
    sop_step_id BIGSERIAL PRIMARY KEY,
    sop_id BIGINT NOT NULL,
    step_number INTEGER NOT NULL,
    step_title VARCHAR(200) NOT NULL,
    step_description TEXT,

    -- Step details
    step_type VARCHAR(50), -- PREPARATION, EXECUTION, INSPECTION, DOCUMENTATION, SAFETY
    estimated_duration INTEGER, -- Minutes

    -- Instructions
    detailed_instruction TEXT,
    caution_notes TEXT,
    quality_points TEXT,

    -- Media
    image_urls JSONB, -- Array of image URLs
    video_url VARCHAR(500),

    -- Checklist items
    checklist_items JSONB, -- Array of checklist items

    -- Dependencies
    prerequisite_step_id BIGINT, -- Must complete this step first

    is_critical BOOLEAN DEFAULT FALSE,
    is_mandatory BOOLEAN DEFAULT TRUE,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_sop_step_sop FOREIGN KEY (sop_id)
        REFERENCES common.sops(sop_id) ON DELETE CASCADE,
    CONSTRAINT fk_sop_step_prerequisite FOREIGN KEY (prerequisite_step_id)
        REFERENCES common.sop_steps(sop_step_id) ON DELETE SET NULL,
    CONSTRAINT uk_sop_step UNIQUE (sop_id, step_number)
);

COMMENT ON TABLE common.sop_steps IS 'SOP 실행 단계';
COMMENT ON COLUMN common.sop_steps.sop_step_id IS 'SOP 단계 ID (PK)';
COMMENT ON COLUMN common.sop_steps.sop_id IS 'SOP ID (FK)';
COMMENT ON COLUMN common.sop_steps.step_number IS '단계 번호';
COMMENT ON COLUMN common.sop_steps.step_title IS '단계 제목';
COMMENT ON COLUMN common.sop_steps.checklist_items IS '체크리스트 항목';
COMMENT ON COLUMN common.sop_steps.is_critical IS '중요 단계 여부';

CREATE INDEX idx_sop_step_sop ON common.sop_steps(sop_id);
CREATE INDEX idx_sop_step_number ON common.sop_steps(step_number);
CREATE INDEX idx_sop_step_critical ON common.sop_steps(is_critical);

-- ============================================================
-- SOP Execution Records Table
-- SOP 실행 기록 테이블
-- ============================================================
CREATE TABLE IF NOT EXISTS common.sop_executions (
    execution_id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    sop_id BIGINT NOT NULL,

    -- Execution info
    execution_no VARCHAR(50) NOT NULL,
    execution_date TIMESTAMP NOT NULL,
    executor_id BIGINT NOT NULL,
    executor_name VARCHAR(100),

    -- Context
    reference_type VARCHAR(50), -- WORK_ORDER, INSPECTION, MAINTENANCE, etc.
    reference_id BIGINT,
    reference_no VARCHAR(50),

    -- Execution status
    execution_status VARCHAR(50) DEFAULT 'IN_PROGRESS', -- IN_PROGRESS, COMPLETED, FAILED, CANCELLED
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    duration INTEGER, -- Minutes

    -- Completion data
    completion_rate DECIMAL(5,2), -- Percentage
    steps_completed INTEGER,
    steps_total INTEGER,

    -- Review
    reviewer_id BIGINT,
    review_status VARCHAR(50), -- PENDING, APPROVED, REJECTED
    review_comments TEXT,
    reviewed_at TIMESTAMP,

    remarks TEXT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_sop_exec_tenant FOREIGN KEY (tenant_id)
        REFERENCES common.tenants(tenant_id) ON DELETE CASCADE,
    CONSTRAINT fk_sop_exec_sop FOREIGN KEY (sop_id)
        REFERENCES common.sops(sop_id) ON DELETE CASCADE,
    CONSTRAINT fk_sop_exec_executor FOREIGN KEY (executor_id)
        REFERENCES common.users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_sop_exec_reviewer FOREIGN KEY (reviewer_id)
        REFERENCES common.users(user_id) ON DELETE SET NULL,
    CONSTRAINT uk_sop_exec_no UNIQUE (tenant_id, execution_no)
);

COMMENT ON TABLE common.sop_executions IS 'SOP 실행 기록';
COMMENT ON COLUMN common.sop_executions.execution_id IS '실행 기록 ID (PK)';
COMMENT ON COLUMN common.sop_executions.tenant_id IS '테넌트 ID (FK)';
COMMENT ON COLUMN common.sop_executions.sop_id IS 'SOP ID (FK)';
COMMENT ON COLUMN common.sop_executions.execution_no IS '실행 번호';
COMMENT ON COLUMN common.sop_executions.executor_id IS '실행자 ID';
COMMENT ON COLUMN common.sop_executions.reference_type IS '참조 유형';
COMMENT ON COLUMN common.sop_executions.completion_rate IS '완료율';

CREATE INDEX idx_sop_exec_tenant ON common.sop_executions(tenant_id);
CREATE INDEX idx_sop_exec_sop ON common.sop_executions(sop_id);
CREATE INDEX idx_sop_exec_date ON common.sop_executions(execution_date);
CREATE INDEX idx_sop_exec_executor ON common.sop_executions(executor_id);
CREATE INDEX idx_sop_exec_status ON common.sop_executions(execution_status);
CREATE INDEX idx_sop_exec_reference ON common.sop_executions(reference_type, reference_id);

-- ============================================================
-- SOP Execution Step Results Table
-- SOP 실행 단계 결과 테이블
-- ============================================================
CREATE TABLE IF NOT EXISTS common.sop_execution_steps (
    execution_step_id BIGSERIAL PRIMARY KEY,
    execution_id BIGINT NOT NULL,
    sop_step_id BIGINT NOT NULL,

    step_number INTEGER NOT NULL,
    step_status VARCHAR(50) DEFAULT 'PENDING', -- PENDING, IN_PROGRESS, COMPLETED, SKIPPED, FAILED

    -- Execution data
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    duration INTEGER, -- Minutes

    -- Results
    result_value TEXT,
    checklist_results JSONB, -- Results for each checklist item

    -- Evidence
    photos JSONB, -- Array of photo URLs
    signature VARCHAR(500), -- Digital signature or approver name

    remarks TEXT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_sop_exec_step_exec FOREIGN KEY (execution_id)
        REFERENCES common.sop_executions(execution_id) ON DELETE CASCADE,
    CONSTRAINT fk_sop_exec_step_sop_step FOREIGN KEY (sop_step_id)
        REFERENCES common.sop_steps(sop_step_id) ON DELETE CASCADE,
    CONSTRAINT uk_sop_exec_step UNIQUE (execution_id, sop_step_id)
);

COMMENT ON TABLE common.sop_execution_steps IS 'SOP 실행 단계별 결과';
COMMENT ON COLUMN common.sop_execution_steps.execution_step_id IS '실행 단계 ID (PK)';
COMMENT ON COLUMN common.sop_execution_steps.execution_id IS '실행 기록 ID (FK)';
COMMENT ON COLUMN common.sop_execution_steps.sop_step_id IS 'SOP 단계 ID (FK)';
COMMENT ON COLUMN common.sop_execution_steps.step_status IS '단계 상태';
COMMENT ON COLUMN common.sop_execution_steps.checklist_results IS '체크리스트 결과';

CREATE INDEX idx_sop_exec_step_exec ON common.sop_execution_steps(execution_id);
CREATE INDEX idx_sop_exec_step_sop_step ON common.sop_execution_steps(sop_step_id);
CREATE INDEX idx_sop_exec_step_status ON common.sop_execution_steps(step_status);
