-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- SoIce MES - Common Schema Migration V001
-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- Purpose: Create common schema tables (multi-tenant core)
-- Author: Moon Myung-seop <msmoon@softice.co.kr>
-- Date: 2026-01-17
-- Version: 001
-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

SET search_path TO common, public;

-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- 1. Tenant Management (테넌트 관리)
-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

CREATE TABLE IF NOT EXISTS common.SI_Tenants (
    tenant_id VARCHAR(50) PRIMARY KEY,
    tenant_name VARCHAR(200) NOT NULL,
    tenant_code VARCHAR(50) UNIQUE NOT NULL,

    -- Company Info
    company_name VARCHAR(200) NOT NULL,
    business_number VARCHAR(50),
    representative_name VARCHAR(100),

    -- Industry Classification
    industry_type VARCHAR(50) NOT NULL, -- medical_device, chemical, electronics, automotive
    industry_sub_type VARCHAR(100),

    -- Contact Info
    address VARCHAR(500),
    phone VARCHAR(50),
    email VARCHAR(100),
    website VARCHAR(200),

    -- Configuration
    config JSONB DEFAULT '{}'::jsonb,

    -- Status
    status VARCHAR(20) NOT NULL DEFAULT 'active', -- active, inactive, suspended
    subscription_plan VARCHAR(50),
    subscription_start_date DATE,
    subscription_end_date DATE,

    -- Metadata
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,

    CONSTRAINT chk_tenant_status CHECK (status IN ('active', 'inactive', 'suspended')),
    CONSTRAINT chk_industry_type CHECK (industry_type IN ('medical_device', 'chemical', 'electronics', 'automotive', 'food', 'pharmaceutical', 'other'))
);

CREATE INDEX idx_si_tenants_status ON common.SI_Tenants(status);
CREATE INDEX idx_si_tenants_industry_type ON common.SI_Tenants(industry_type);

COMMENT ON TABLE common.SI_Tenants IS 'Multi-tenant 테넌트 관리 테이블';
COMMENT ON COLUMN common.SI_Tenants.config IS '테넌트별 커스텀 설정 (JSON)';

-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- 2. User Management (사용자 관리)
-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

CREATE TABLE IF NOT EXISTS common.SI_Users (
    user_id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL REFERENCES common.SI_Tenants(tenant_id),

    -- Login Info
    username VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,

    -- Personal Info
    full_name VARCHAR(100) NOT NULL,
    employee_number VARCHAR(50),
    department VARCHAR(100),
    position VARCHAR(100),
    phone VARCHAR(50),
    mobile VARCHAR(50),

    -- Status
    status VARCHAR(20) NOT NULL DEFAULT 'active', -- active, inactive, locked
    is_email_verified BOOLEAN DEFAULT FALSE,
    email_verified_at TIMESTAMP,

    -- Password Management
    last_password_change TIMESTAMP,
    password_expires_at TIMESTAMP,
    failed_login_attempts INTEGER DEFAULT 0,
    locked_at TIMESTAMP,

    -- Session Management
    last_login_at TIMESTAMP,
    last_login_ip VARCHAR(45),

    -- Multi-language
    preferred_language VARCHAR(10) DEFAULT 'ko', -- ko, en, zh
    timezone VARCHAR(50) DEFAULT 'Asia/Seoul',

    -- Metadata
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,

    CONSTRAINT uk_si_users_tenant_username UNIQUE (tenant_id, username),
    CONSTRAINT uk_si_users_tenant_email UNIQUE (tenant_id, email),
    CONSTRAINT chk_user_status CHECK (status IN ('active', 'inactive', 'locked')),
    CONSTRAINT chk_preferred_language CHECK (preferred_language IN ('ko', 'en', 'zh', 'ja'))
);

CREATE INDEX idx_si_users_tenant_id ON common.SI_Users(tenant_id);
CREATE INDEX idx_si_users_username ON common.SI_Users(username);
CREATE INDEX idx_si_users_email ON common.SI_Users(email);
CREATE INDEX idx_si_users_status ON common.SI_Users(tenant_id, status);

COMMENT ON TABLE common.SI_Users IS '사용자 관리 테이블 (Multi-tenant)';

-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- 3. Role Management (역할 관리)
-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

CREATE TABLE IF NOT EXISTS common.SI_Roles (
    role_id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL REFERENCES common.SI_Tenants(tenant_id),

    role_code VARCHAR(100) NOT NULL,
    role_name VARCHAR(200) NOT NULL,
    description TEXT,

    -- Role Type
    role_type VARCHAR(50) DEFAULT 'custom', -- system, custom

    -- Status
    is_active BOOLEAN DEFAULT TRUE,

    -- Metadata
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,

    CONSTRAINT uk_si_roles_tenant_code UNIQUE (tenant_id, role_code)
);

CREATE INDEX idx_si_roles_tenant_id ON common.SI_Roles(tenant_id);

COMMENT ON TABLE common.SI_Roles IS '역할 관리 테이블';

-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- 4. Permission Management (권한 관리)
-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

CREATE TABLE IF NOT EXISTS common.SI_Permissions (
    permission_id BIGSERIAL PRIMARY KEY,

    permission_code VARCHAR(100) UNIQUE NOT NULL,
    permission_name VARCHAR(200) NOT NULL,
    description TEXT,

    -- Permission Grouping
    module VARCHAR(50) NOT NULL, -- MES, QMS, WMS, EMS, LIMS, COMMON
    category VARCHAR(100),

    -- Status
    is_active BOOLEAN DEFAULT TRUE,

    -- Metadata
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_si_permissions_module ON common.SI_Permissions(module);

COMMENT ON TABLE common.SI_Permissions IS '권한 관리 테이블 (전체 시스템 공통)';

-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- 5. Role-Permission Mapping (역할-권한 매핑)
-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

CREATE TABLE IF NOT EXISTS common.SI_RolePermissions (
    role_permission_id BIGSERIAL PRIMARY KEY,
    role_id BIGINT NOT NULL REFERENCES common.SI_Roles(role_id) ON DELETE CASCADE,
    permission_id BIGINT NOT NULL REFERENCES common.SI_Permissions(permission_id) ON DELETE CASCADE,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,

    CONSTRAINT uk_si_role_permissions UNIQUE (role_id, permission_id)
);

CREATE INDEX idx_si_role_permissions_role_id ON common.SI_RolePermissions(role_id);
CREATE INDEX idx_si_role_permissions_permission_id ON common.SI_RolePermissions(permission_id);

COMMENT ON TABLE common.SI_RolePermissions IS '역할-권한 매핑 테이블';

-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- 6. User-Role Mapping (사용자-역할 매핑)
-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

CREATE TABLE IF NOT EXISTS common.SI_UserRoles (
    user_role_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES common.SI_Users(user_id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES common.SI_Roles(role_id) ON DELETE CASCADE,

    -- Effective Period
    effective_from DATE,
    effective_to DATE,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,

    CONSTRAINT uk_si_user_roles UNIQUE (user_id, role_id)
);

CREATE INDEX idx_si_user_roles_user_id ON common.SI_UserRoles(user_id);
CREATE INDEX idx_si_user_roles_role_id ON common.SI_UserRoles(role_id);

COMMENT ON TABLE common.SI_UserRoles IS '사용자-역할 매핑 테이블';

-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- 7. Code Management (공통 코드 관리)
-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

CREATE TABLE IF NOT EXISTS common.SI_CodeGroups (
    code_group_id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) REFERENCES common.SI_Tenants(tenant_id),

    code_group_code VARCHAR(100) NOT NULL,
    code_group_name VARCHAR(200) NOT NULL,
    description TEXT,

    -- Scope
    scope VARCHAR(20) DEFAULT 'tenant', -- system, tenant

    -- Status
    is_active BOOLEAN DEFAULT TRUE,

    -- Metadata
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,

    CONSTRAINT uk_si_code_groups UNIQUE (tenant_id, code_group_code)
);

CREATE INDEX idx_si_code_groups_tenant_id ON common.SI_CodeGroups(tenant_id);

COMMENT ON TABLE common.SI_CodeGroups IS '코드 그룹 관리 테이블';

CREATE TABLE IF NOT EXISTS common.SI_Codes (
    code_id BIGSERIAL PRIMARY KEY,
    code_group_id BIGINT NOT NULL REFERENCES common.SI_CodeGroups(code_group_id) ON DELETE CASCADE,

    code VARCHAR(100) NOT NULL,
    code_name VARCHAR(200) NOT NULL,
    code_name_en VARCHAR(200),
    description TEXT,

    -- Additional Data
    additional_data JSONB,

    -- Display Order
    display_order INTEGER DEFAULT 0,

    -- Status
    is_active BOOLEAN DEFAULT TRUE,

    -- Metadata
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,

    CONSTRAINT uk_si_codes UNIQUE (code_group_id, code)
);

CREATE INDEX idx_si_codes_group_id ON common.SI_Codes(code_group_id);

COMMENT ON TABLE common.SI_Codes IS '코드 상세 관리 테이블';

-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- 8. Triggers (자동 업데이트 트리거)
-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

CREATE TRIGGER trg_si_tenants_updated_at
    BEFORE UPDATE ON common.SI_Tenants
    FOR EACH ROW
    EXECUTE FUNCTION common.update_modified_timestamp();

CREATE TRIGGER trg_si_users_updated_at
    BEFORE UPDATE ON common.SI_Users
    FOR EACH ROW
    EXECUTE FUNCTION common.update_modified_timestamp();

CREATE TRIGGER trg_si_roles_updated_at
    BEFORE UPDATE ON common.SI_Roles
    FOR EACH ROW
    EXECUTE FUNCTION common.update_modified_timestamp();

CREATE TRIGGER trg_si_permissions_updated_at
    BEFORE UPDATE ON common.SI_Permissions
    FOR EACH ROW
    EXECUTE FUNCTION common.update_modified_timestamp();

CREATE TRIGGER trg_si_code_groups_updated_at
    BEFORE UPDATE ON common.SI_CodeGroups
    FOR EACH ROW
    EXECUTE FUNCTION common.update_modified_timestamp();

CREATE TRIGGER trg_si_codes_updated_at
    BEFORE UPDATE ON common.SI_Codes
    FOR EACH ROW
    EXECUTE FUNCTION common.update_modified_timestamp();

-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- Success Message
-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

DO $$
BEGIN
    RAISE NOTICE '✅ Common Schema V001 Migration Completed Successfully!';
    RAISE NOTICE 'Tables Created: SI_Tenants, SI_Users, SI_Roles, SI_Permissions, SI_RolePermissions, SI_UserRoles, SI_CodeGroups, SI_Codes';
END
$$;
