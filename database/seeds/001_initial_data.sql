-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- SoIce MES - Initial Seed Data
-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- Purpose: Insert initial data for development and testing
-- Author: Moon Myung-seop <msmoon@softice.co.kr>
-- Date: 2026-01-17
-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

\echo '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━'
\echo 'Inserting Initial Seed Data...'
\echo '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━'

-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- 1. Tenants (테넌트)
-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

INSERT INTO common.SI_Tenants (
    tenant_id, tenant_name, tenant_code,
    company_name, business_number, representative_name,
    industry_type, industry_sub_type,
    address, phone, email, website,
    config, status
) VALUES
-- SoftIce (개발사)
('softice', 'SoftIce Development', 'SOFTICE',
 '(주)소프트아이스', '123-45-67890', '문명섭',
 'other', 'software_development',
 '경기도 성남시', '031-689-4707', 'info@softice.co.kr', 'www.softice.co.kr',
 '{"features": ["all"], "max_users": 9999}'::jsonb, 'active'),

-- i-sens (의료기기 제조)
('isens', 'i-sens Medical Device', 'ISENS',
 '(주)아이센스', '456-78-90123', '김대표',
 'medical_device', 'blood_glucose_monitoring',
 '서울시 강남구', '02-1234-5678', 'info@i-sens.com', 'www.i-sens.com',
 '{"gmp_compliance": true, "fda_approved": true, "features": ["mes", "qms", "lims"]}'::jsonb, 'active'),

-- Demo Company (화학 공장)
('demo_chemical', 'Demo Chemical Co.', 'DEMOCHEM',
 '데모화학(주)', '789-01-23456', '이대표',
 'chemical', 'specialty_chemicals',
 '경기도 안산시', '031-9999-8888', 'demo@chemical.com', NULL,
 '{"features": ["mes", "qms", "wms", "ems"], "msds_management": true}'::jsonb, 'active')
ON CONFLICT DO NOTHING;

\echo '  ✓ Tenants inserted (3 rows)'

-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- 2. Users (사용자)
-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

INSERT INTO common.SI_Users (
    tenant_id, username, email, password_hash,
    full_name, employee_number, department, position,
    phone, mobile, status, is_email_verified, preferred_language
) VALUES
-- SoftIce Users
('softice', 'admin', 'msmoon@softice.co.kr',
 '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- password: admin123
 '문명섭', 'EMP001', 'Development', 'CTO',
 '031-689-4707', '010-4882-2035', 'active', TRUE, 'ko'),

('softice', 'developer', 'dev@softice.co.kr',
 '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- password: admin123
 '개발자', 'EMP002', 'Development', 'Developer',
 NULL, '010-1111-2222', 'active', TRUE, 'ko'),

-- i-sens Users
('isens', 'admin', 'admin@i-sens.com',
 '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- password: admin123
 '아이센스 관리자', 'IS001', 'IT', 'Manager',
 '02-1234-5678', '010-3333-4444', 'active', TRUE, 'ko'),

('isens', 'operator1', 'operator1@i-sens.com',
 '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- password: admin123
 '생산관리자', 'IS101', 'Production', 'Operator',
 NULL, '010-5555-6666', 'active', TRUE, 'ko'),

-- Demo Chemical Users
('demo_chemical', 'admin', 'admin@chemical.com',
 '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- password: admin123
 '화학공장 관리자', 'CH001', 'Management', 'Admin',
 '031-9999-8888', '010-7777-8888', 'active', TRUE, 'ko')
ON CONFLICT DO NOTHING;

\echo '  ✓ Users inserted (5 rows)'

-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- 3. Permissions (권한)
-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

INSERT INTO common.SI_Permissions (
    permission_code, permission_name, description, module, category
) VALUES
-- COMMON Module
('COMMON_USER_VIEW', '사용자 조회', '사용자 정보 조회 권한', 'COMMON', 'user_management'),
('COMMON_USER_CREATE', '사용자 생성', '신규 사용자 생성 권한', 'COMMON', 'user_management'),
('COMMON_USER_UPDATE', '사용자 수정', '사용자 정보 수정 권한', 'COMMON', 'user_management'),
('COMMON_USER_DELETE', '사용자 삭제', '사용자 삭제 권한', 'COMMON', 'user_management'),
('COMMON_ROLE_MANAGE', '역할 관리', '역할 및 권한 관리', 'COMMON', 'role_management'),
('COMMON_CODE_MANAGE', '코드 관리', '공통 코드 관리', 'COMMON', 'code_management'),
('COMMON_AUDIT_VIEW', '감사 조회', '감사 로그 조회', 'COMMON', 'audit'),

-- MES Module
('MES_PRODUCTION_VIEW', '생산 조회', '생산 현황 조회', 'MES', 'production'),
('MES_PRODUCTION_CREATE', '생산 지시', '생산 지시 생성', 'MES', 'production'),
('MES_PRODUCTION_UPDATE', '생산 수정', '생산 정보 수정', 'MES', 'production'),
('MES_WORK_ORDER_MANAGE', '작업 지시 관리', '작업 지시 관리', 'MES', 'work_order'),

-- QMS Module
('QMS_INSPECTION_VIEW', '검사 조회', '품질 검사 결과 조회', 'QMS', 'inspection'),
('QMS_INSPECTION_EXECUTE', '검사 실행', '품질 검사 실행', 'QMS', 'inspection'),
('QMS_STANDARD_MANAGE', '품질 기준 관리', '품질 기준 설정 및 관리', 'QMS', 'standard'),

-- WMS Module
('WMS_INVENTORY_VIEW', '재고 조회', '재고 현황 조회', 'WMS', 'inventory'),
('WMS_INVENTORY_ADJUST', '재고 조정', '재고 조정 권한', 'WMS', 'inventory'),
('WMS_INBOUND', '입고 처리', '입고 처리 권한', 'WMS', 'transaction'),
('WMS_OUTBOUND', '출고 처리', '출고 처리 권한', 'WMS', 'transaction'),

-- EMS Module
('EMS_EQUIPMENT_VIEW', '설비 조회', '설비 정보 조회', 'EMS', 'equipment'),
('EMS_EQUIPMENT_MANAGE', '설비 관리', '설비 등록 및 관리', 'EMS', 'equipment'),
('EMS_MAINTENANCE_MANAGE', '보전 관리', '예방 보전 및 수리 관리', 'EMS', 'maintenance'),

-- LIMS Module
('LIMS_TEST_VIEW', '시험 조회', '시험 결과 조회', 'LIMS', 'test'),
('LIMS_TEST_EXECUTE', '시험 실행', '시험 실행 및 결과 입력', 'LIMS', 'test'),
('LIMS_COA_ISSUE', 'CoA 발행', 'Certificate of Analysis 발행', 'LIMS', 'coa')
ON CONFLICT DO NOTHING;

\echo '  ✓ Permissions inserted (23 rows)'

-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- 4. Roles (역할)
-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

INSERT INTO common.SI_Roles (tenant_id, role_code, role_name, description, role_type) VALUES
-- SoftIce Roles
('softice', 'SUPER_ADMIN', '슈퍼 관리자', '모든 권한을 가진 최고 관리자', 'system'),
('softice', 'DEVELOPER', '개발자', '개발 및 시스템 관리', 'system'),

-- i-sens Roles
('isens', 'ADMIN', '관리자', '시스템 관리자', 'system'),
('isens', 'PRODUCTION_MANAGER', '생산 관리자', '생산 관련 모든 권한', 'custom'),
('isens', 'QA_MANAGER', '품질 관리자', '품질 관련 모든 권한', 'custom'),
('isens', 'OPERATOR', '작업자', '생산 작업 및 조회', 'custom'),

-- Demo Chemical Roles
('demo_chemical', 'ADMIN', '관리자', '시스템 관리자', 'system'),
('demo_chemical', 'PLANT_MANAGER', '공장장', '공장 운영 총괄', 'custom')
ON CONFLICT DO NOTHING;

\echo '  ✓ Roles inserted (8 rows)'

-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- 5. Code Groups (코드 그룹)
-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

INSERT INTO common.SI_CodeGroups (tenant_id, code_group_code, code_group_name, description, scope) VALUES
(NULL, 'COMMON_STATUS', '공통 상태', '시스템 공통 상태 코드', 'system'),
(NULL, 'COMMON_YES_NO', '예/아니오', '예/아니오 선택 코드', 'system'),
('isens', 'PRODUCT_TYPE', '제품 유형', '제품 분류 코드', 'tenant'),
('isens', 'INSPECTION_TYPE', '검사 유형', '검사 종류 코드', 'tenant'),
('demo_chemical', 'CHEMICAL_TYPE', '화학물질 유형', '화학물질 분류', 'tenant')
ON CONFLICT DO NOTHING;

\echo '  ✓ Code Groups inserted (5 rows)'

-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- 6. Codes (코드 상세)
-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

INSERT INTO common.SI_Codes (code_group_id, code, code_name, code_name_en, display_order) VALUES
-- COMMON_STATUS
((SELECT code_group_id FROM common.SI_CodeGroups WHERE code_group_code = 'COMMON_STATUS' AND tenant_id IS NULL),
 'ACTIVE', '활성', 'Active', 1),
((SELECT code_group_id FROM common.SI_CodeGroups WHERE code_group_code = 'COMMON_STATUS' AND tenant_id IS NULL),
 'INACTIVE', '비활성', 'Inactive', 2),
((SELECT code_group_id FROM common.SI_CodeGroups WHERE code_group_code = 'COMMON_STATUS' AND tenant_id IS NULL),
 'DELETED', '삭제', 'Deleted', 3),

-- COMMON_YES_NO
((SELECT code_group_id FROM common.SI_CodeGroups WHERE code_group_code = 'COMMON_YES_NO' AND tenant_id IS NULL),
 'Y', '예', 'Yes', 1),
((SELECT code_group_id FROM common.SI_CodeGroups WHERE code_group_code = 'COMMON_YES_NO' AND tenant_id IS NULL),
 'N', '아니오', 'No', 2)
ON CONFLICT DO NOTHING;

\echo '  ✓ Codes inserted (5 rows)'

-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- Success Message
-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

\echo ''
\echo '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━'
\echo '✅ Initial Seed Data Inserted Successfully!'
\echo '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━'
\echo ''
\echo 'Test Credentials:'
\echo '  SoftIce Admin: admin / admin123'
\echo '  i-sens Admin: admin / admin123'
\echo '  Demo Chemical Admin: admin / admin123'
\echo ''
\echo 'Note: Please change default passwords in production!'
\echo ''
