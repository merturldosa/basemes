-- V024: Create Approval Line Schema
-- 결재 라인 스키마 생성
-- Author: Moon Myung-seop
-- Date: 2026-01-25

-- ==================== Approval Line Template Table ====================
-- 결재 라인 템플릿 테이블 (문서 유형별 결재 경로 정의)

CREATE TABLE common.approval_line_templates (
    template_id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    template_name VARCHAR(200) NOT NULL,
    template_code VARCHAR(50) NOT NULL,
    document_type VARCHAR(50) NOT NULL,  -- PURCHASE_ORDER, WORK_ORDER, SALES_ORDER, HOLIDAY_REQUEST, etc.
    description TEXT,

    -- 승인 설정
    approval_type VARCHAR(20) NOT NULL DEFAULT 'SEQUENTIAL',  -- SEQUENTIAL(순차), PARALLEL(병렬), HYBRID(혼합)
    auto_approve_amount DECIMAL(15,2),  -- 자동 승인 금액 (금액 미만 자동 승인)
    skip_if_same_person BOOLEAN DEFAULT TRUE,  -- 동일인 건너뛰기

    -- 상태
    is_default BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,

    -- Audit
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_approval_template_code UNIQUE (tenant_id, template_code),
    CONSTRAINT uk_approval_template_doc_type_default UNIQUE (tenant_id, document_type, is_default) WHERE is_default = TRUE
);

CREATE INDEX idx_approval_template_tenant ON common.approval_line_templates(tenant_id);
CREATE INDEX idx_approval_template_doc_type ON common.approval_line_templates(document_type);
CREATE INDEX idx_approval_template_active ON common.approval_line_templates(is_active);

COMMENT ON TABLE common.approval_line_templates IS '결재 라인 템플릿';
COMMENT ON COLUMN common.approval_line_templates.approval_type IS '승인 방식: SEQUENTIAL(순차), PARALLEL(병렬), HYBRID(혼합)';
COMMENT ON COLUMN common.approval_line_templates.auto_approve_amount IS '자동 승인 금액 (이하 자동 승인)';
COMMENT ON COLUMN common.approval_line_templates.skip_if_same_person IS '동일인 건너뛰기 여부';

-- ==================== Approval Line Step Table ====================
-- 결재 라인 단계 테이블 (결재 라인의 각 승인 단계)

CREATE TABLE common.approval_line_steps (
    step_id BIGSERIAL PRIMARY KEY,
    template_id BIGINT NOT NULL REFERENCES common.approval_line_templates(template_id) ON DELETE CASCADE,

    -- 단계 정보
    step_order INT NOT NULL,  -- 단계 순서 (1, 2, 3, ...)
    step_name VARCHAR(100) NOT NULL,  -- 단계명 (기안, 팀장 승인, 부서장 승인, 임원 승인 등)
    step_type VARCHAR(20) NOT NULL DEFAULT 'APPROVAL',  -- APPROVAL(승인), REVIEW(검토), NOTIFICATION(통보)

    -- 승인자 설정
    approver_type VARCHAR(20) NOT NULL,  -- ROLE(역할), POSITION(직급), DEPARTMENT(부서), USER(특정 사용자)
    approver_role VARCHAR(50),  -- 역할 코드 (MANAGER, DIRECTOR, etc.)
    approver_position VARCHAR(50),  -- 직급 코드
    approver_department VARCHAR(50),  -- 부서 코드
    approver_user_id BIGINT,  -- 특정 사용자 ID

    -- 단계 설정
    is_mandatory BOOLEAN DEFAULT TRUE,  -- 필수 승인 여부
    approval_method VARCHAR(20) DEFAULT 'SINGLE',  -- SINGLE(단일), ALL(전원), MAJORITY(과반수)
    parallel_group INT,  -- 병렬 그룹 번호 (같은 번호는 동시 진행)
    auto_approve_on_timeout BOOLEAN DEFAULT FALSE,  -- 시간 초과 시 자동 승인
    timeout_hours INT,  -- 시간 제한 (시간)

    -- 대결/전결 설정
    allow_delegation BOOLEAN DEFAULT TRUE,  -- 대결 허용 여부
    allow_skip BOOLEAN DEFAULT FALSE,  -- 건너뛰기 허용 여부

    -- Audit
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_approval_step_order UNIQUE (template_id, step_order)
);

CREATE INDEX idx_approval_step_template ON common.approval_line_steps(template_id);
CREATE INDEX idx_approval_step_order ON common.approval_line_steps(template_id, step_order);

COMMENT ON TABLE common.approval_line_steps IS '결재 라인 단계';
COMMENT ON COLUMN common.approval_line_steps.step_type IS '단계 타입: APPROVAL(승인), REVIEW(검토), NOTIFICATION(통보)';
COMMENT ON COLUMN common.approval_line_steps.approver_type IS '승인자 유형: ROLE, POSITION, DEPARTMENT, USER';
COMMENT ON COLUMN common.approval_line_steps.approval_method IS '승인 방법: SINGLE(1명), ALL(전원), MAJORITY(과반수)';
COMMENT ON COLUMN common.approval_line_steps.parallel_group IS '병렬 그룹 번호 (같은 번호는 동시 진행)';

-- ==================== Approval Instance Table ====================
-- 결재 인스턴스 테이블 (실제 문서의 결재 진행 상황)

CREATE TABLE common.approval_instances (
    instance_id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    template_id BIGINT REFERENCES common.approval_line_templates(template_id),

    -- 문서 정보
    document_type VARCHAR(50) NOT NULL,
    document_id BIGINT NOT NULL,  -- 원본 문서 ID (purchase_order_id, work_order_id, etc.)
    document_no VARCHAR(100),  -- 원본 문서 번호
    document_title VARCHAR(500),  -- 문서 제목
    document_amount DECIMAL(15,2),  -- 문서 금액 (해당되는 경우)

    -- 결재 상태
    approval_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',  -- PENDING, IN_PROGRESS, APPROVED, REJECTED, CANCELLED
    current_step_order INT,  -- 현재 진행 중인 단계 순서

    -- 기안자 정보
    requester_id BIGINT NOT NULL,
    requester_name VARCHAR(100),
    requester_department VARCHAR(100),
    request_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    request_comment TEXT,

    -- 완료 정보
    completed_date TIMESTAMP,
    final_approver_id BIGINT,
    final_approver_name VARCHAR(100),

    -- Audit
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_approval_instance_document UNIQUE (tenant_id, document_type, document_id)
);

CREATE INDEX idx_approval_instance_tenant ON common.approval_instances(tenant_id);
CREATE INDEX idx_approval_instance_status ON common.approval_instances(approval_status);
CREATE INDEX idx_approval_instance_document ON common.approval_instances(document_type, document_id);
CREATE INDEX idx_approval_instance_requester ON common.approval_instances(requester_id);
CREATE INDEX idx_approval_instance_date ON common.approval_instances(request_date);

COMMENT ON TABLE common.approval_instances IS '결재 인스턴스 (실제 결재 진행)';
COMMENT ON COLUMN common.approval_instances.approval_status IS '결재 상태: PENDING(대기), IN_PROGRESS(진행중), APPROVED(승인), REJECTED(반려), CANCELLED(취소)';

-- ==================== Approval Step Instance Table ====================
-- 결재 단계 인스턴스 테이블 (각 결재 단계의 실제 처리 현황)

CREATE TABLE common.approval_step_instances (
    step_instance_id BIGSERIAL PRIMARY KEY,
    instance_id BIGINT NOT NULL REFERENCES common.approval_instances(instance_id) ON DELETE CASCADE,
    step_id BIGINT REFERENCES common.approval_line_steps(step_id),

    -- 단계 정보
    step_order INT NOT NULL,
    step_name VARCHAR(100) NOT NULL,
    step_type VARCHAR(20) NOT NULL,

    -- 승인자 정보
    approver_id BIGINT NOT NULL,
    approver_name VARCHAR(100),
    approver_department VARCHAR(100),
    approver_position VARCHAR(100),

    -- 대결자 정보
    delegated_to_id BIGINT,  -- 대결자 ID
    delegated_to_name VARCHAR(100),
    delegation_reason TEXT,

    -- 승인 상태
    step_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',  -- PENDING, IN_PROGRESS, APPROVED, REJECTED, SKIPPED, TIMEOUT

    -- 승인 결과
    approval_date TIMESTAMP,
    approval_comment TEXT,
    rejection_reason TEXT,

    -- 시간 관리
    assigned_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    due_date TIMESTAMP,  -- 승인 기한

    -- Audit
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_approval_step_inst_instance ON common.approval_step_instances(instance_id);
CREATE INDEX idx_approval_step_inst_approver ON common.approval_step_instances(approver_id);
CREATE INDEX idx_approval_step_inst_status ON common.approval_step_instances(step_status);
CREATE INDEX idx_approval_step_inst_order ON common.approval_step_instances(instance_id, step_order);

COMMENT ON TABLE common.approval_step_instances IS '결재 단계 인스턴스 (실제 승인 처리)';
COMMENT ON COLUMN common.approval_step_instances.step_status IS '단계 상태: PENDING(대기), IN_PROGRESS(진행중), APPROVED(승인), REJECTED(반려), SKIPPED(건너뜀), TIMEOUT(시간초과)';

-- ==================== Approval Delegation Table ====================
-- 결재 위임 테이블 (휴가/출장 시 결재 권한 위임)

CREATE TABLE common.approval_delegations (
    delegation_id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,

    -- 위임자 정보
    delegator_id BIGINT NOT NULL,  -- 위임자 (원래 승인자)
    delegator_name VARCHAR(100),

    -- 수임자 정보
    delegate_id BIGINT NOT NULL,  -- 수임자 (대신 승인할 사람)
    delegate_name VARCHAR(100),

    -- 위임 설정
    delegation_type VARCHAR(20) NOT NULL DEFAULT 'FULL',  -- FULL(전체), PARTIAL(부분)
    document_types TEXT,  -- 위임할 문서 유형 (JSON array: ["PURCHASE_ORDER", "WORK_ORDER"])

    -- 위임 기간
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,

    -- 상태
    is_active BOOLEAN DEFAULT TRUE,
    delegation_reason TEXT,

    -- Audit
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_delegation_dates CHECK (end_date >= start_date)
);

CREATE INDEX idx_approval_delegation_delegator ON common.approval_delegations(delegator_id);
CREATE INDEX idx_approval_delegation_delegate ON common.approval_delegations(delegate_id);
CREATE INDEX idx_approval_delegation_date ON common.approval_delegations(start_date, end_date);
CREATE INDEX idx_approval_delegation_active ON common.approval_delegations(is_active);

COMMENT ON TABLE common.approval_delegations IS '결재 위임 설정';
COMMENT ON COLUMN common.approval_delegations.delegation_type IS '위임 유형: FULL(전체), PARTIAL(부분)';

-- ==================== Sample Data ====================

-- Sample Approval Line Templates
INSERT INTO common.approval_line_templates (
    tenant_id, template_name, template_code, document_type, description,
    approval_type, auto_approve_amount, skip_if_same_person, is_default, is_active
) VALUES
-- 구매 주문 결재 라인
('TENANT001', '구매 주문 표준 결재', 'PO_STANDARD', 'PURCHASE_ORDER',
 '구매 주문서 표준 결재 라인 (100만원 미만 자동 승인)', 'SEQUENTIAL', 1000000, TRUE, TRUE, TRUE),

-- 작업 지시 결재 라인
('TENANT001', '작업 지시 표준 결재', 'WO_STANDARD', 'WORK_ORDER',
 '작업 지시서 표준 결재 라인', 'SEQUENTIAL', NULL, TRUE, TRUE, TRUE),

-- 판매 주문 결재 라인
('TENANT001', '판매 주문 표준 결재', 'SO_STANDARD', 'SALES_ORDER',
 '판매 주문서 표준 결재 라인 (500만원 미만 자동 승인)', 'SEQUENTIAL', 5000000, TRUE, TRUE, TRUE),

-- 휴가 신청 결재 라인
('TENANT001', '휴가 신청 표준 결재', 'HOLIDAY_STANDARD', 'HOLIDAY_REQUEST',
 '휴가 신청 표준 결재 라인', 'SEQUENTIAL', NULL, TRUE, TRUE, TRUE),

-- 품질 검사 결재 라인
('TENANT001', '품질 검사 표준 결재', 'QI_STANDARD', 'QUALITY_INSPECTION',
 '품질 검사 결과 승인 결재 라인', 'PARALLEL', NULL, FALSE, TRUE, TRUE);

-- Sample Approval Line Steps for Purchase Order (구매 주문)
INSERT INTO common.approval_line_steps (
    template_id, step_order, step_name, step_type,
    approver_type, approver_role, approver_position,
    is_mandatory, approval_method, parallel_group,
    allow_delegation, allow_skip, timeout_hours
) VALUES
-- Step 1: 구매 담당자 검토
((SELECT template_id FROM common.approval_line_templates WHERE template_code = 'PO_STANDARD'),
 1, '구매 담당자 검토', 'REVIEW', 'ROLE', 'PURCHASE_CLERK', NULL, TRUE, 'SINGLE', NULL, TRUE, FALSE, 24),

-- Step 2: 팀장 승인
((SELECT template_id FROM common.approval_line_templates WHERE template_code = 'PO_STANDARD'),
 2, '팀장 승인', 'APPROVAL', 'POSITION', NULL, 'TEAM_LEADER', TRUE, 'SINGLE', NULL, TRUE, FALSE, 48),

-- Step 3: 부서장 승인
((SELECT template_id FROM common.approval_line_templates WHERE template_code = 'PO_STANDARD'),
 3, '부서장 승인', 'APPROVAL', 'POSITION', NULL, 'DEPARTMENT_MANAGER', TRUE, 'SINGLE', NULL, TRUE, FALSE, 48),

-- Step 4: 임원 승인 (1000만원 이상)
((SELECT template_id FROM common.approval_line_templates WHERE template_code = 'PO_STANDARD'),
 4, '임원 승인', 'APPROVAL', 'POSITION', NULL, 'EXECUTIVE', FALSE, 'SINGLE', NULL, TRUE, TRUE, 72);

-- Sample Approval Line Steps for Work Order (작업 지시)
INSERT INTO common.approval_line_steps (
    template_id, step_order, step_name, step_type,
    approver_type, approver_role, approver_position,
    is_mandatory, approval_method, parallel_group,
    allow_delegation, allow_skip, timeout_hours
) VALUES
-- Step 1: 생산 계획 검토
((SELECT template_id FROM common.approval_line_templates WHERE template_code = 'WO_STANDARD'),
 1, '생산 계획 검토', 'REVIEW', 'ROLE', 'PRODUCTION_PLANNER', NULL, TRUE, 'SINGLE', NULL, TRUE, FALSE, 24),

-- Step 2: 생산 팀장 승인
((SELECT template_id FROM common.approval_line_templates WHERE template_code = 'WO_STANDARD'),
 2, '생산 팀장 승인', 'APPROVAL', 'POSITION', NULL, 'TEAM_LEADER', TRUE, 'SINGLE', NULL, TRUE, FALSE, 48),

-- Step 3: 품질 팀장 검토 (병렬)
((SELECT template_id FROM common.approval_line_templates WHERE template_code = 'WO_STANDARD'),
 3, '품질 팀장 검토', 'REVIEW', 'ROLE', 'QUALITY_MANAGER', NULL, TRUE, 'SINGLE', 1, TRUE, FALSE, 48),

-- Step 3: 자재 팀장 검토 (병렬)
((SELECT template_id FROM common.approval_line_templates WHERE template_code = 'WO_STANDARD'),
 3, '자재 팀장 검토', 'REVIEW', 'ROLE', 'MATERIAL_MANAGER', NULL, TRUE, 'SINGLE', 1, TRUE, FALSE, 48),

-- Step 4: 생산 부서장 최종 승인
((SELECT template_id FROM common.approval_line_templates WHERE template_code = 'WO_STANDARD'),
 4, '생산 부서장 최종 승인', 'APPROVAL', 'POSITION', NULL, 'DEPARTMENT_MANAGER', TRUE, 'SINGLE', NULL, TRUE, FALSE, 72);

-- Sample Approval Line Steps for Holiday Request (휴가 신청)
INSERT INTO common.approval_line_steps (
    template_id, step_order, step_name, step_type,
    approver_type, approver_role, approver_position,
    is_mandatory, approval_method, parallel_group,
    allow_delegation, allow_skip, timeout_hours
) VALUES
-- Step 1: 직속 상사 승인
((SELECT template_id FROM common.approval_line_templates WHERE template_code = 'HOLIDAY_STANDARD'),
 1, '직속 상사 승인', 'APPROVAL', 'POSITION', NULL, 'TEAM_LEADER', TRUE, 'SINGLE', NULL, TRUE, FALSE, 24),

-- Step 2: 인사 담당자 확인
((SELECT template_id FROM common.approval_line_templates WHERE template_code = 'HOLIDAY_STANDARD'),
 2, '인사 담당자 확인', 'REVIEW', 'ROLE', 'HR_MANAGER', NULL, TRUE, 'SINGLE', NULL, TRUE, FALSE, 48);

-- Sample Approval Line Steps for Quality Inspection (품질 검사)
INSERT INTO common.approval_line_steps (
    template_id, step_order, step_name, step_type,
    approver_type, approver_role, approver_position,
    is_mandatory, approval_method, parallel_group,
    allow_delegation, allow_skip, timeout_hours
) VALUES
-- Step 1: 품질 검사원 검토 (전원 승인)
((SELECT template_id FROM common.approval_line_templates WHERE template_code = 'QI_STANDARD'),
 1, '품질 검사원 검토', 'REVIEW', 'ROLE', 'QC_INSPECTOR', NULL, TRUE, 'ALL', NULL, FALSE, FALSE, 12),

-- Step 2: 품질 팀장 승인
((SELECT template_id FROM common.approval_line_templates WHERE template_code = 'QI_STANDARD'),
 2, '품질 팀장 승인', 'APPROVAL', 'POSITION', NULL, 'TEAM_LEADER', TRUE, 'SINGLE', NULL, TRUE, FALSE, 24);

-- Create function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_approval_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create triggers
CREATE TRIGGER update_approval_template_updated_at
    BEFORE UPDATE ON common.approval_line_templates
    FOR EACH ROW
    EXECUTE FUNCTION update_approval_updated_at();

CREATE TRIGGER update_approval_step_updated_at
    BEFORE UPDATE ON common.approval_line_steps
    FOR EACH ROW
    EXECUTE FUNCTION update_approval_updated_at();

CREATE TRIGGER update_approval_instance_updated_at
    BEFORE UPDATE ON common.approval_instances
    FOR EACH ROW
    EXECUTE FUNCTION update_approval_updated_at();

CREATE TRIGGER update_approval_step_inst_updated_at
    BEFORE UPDATE ON common.approval_step_instances
    FOR EACH ROW
    EXECUTE FUNCTION update_approval_updated_at();

CREATE TRIGGER update_approval_delegation_updated_at
    BEFORE UPDATE ON common.approval_delegations
    FOR EACH ROW
    EXECUTE FUNCTION update_approval_updated_at();
