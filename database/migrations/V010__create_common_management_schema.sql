-- ============================================================================
-- Common Management Schema Extension
-- 공통관리 확장 스키마
-- Author: Moon Myung-seop
-- Date: 2026-01-23
-- ============================================================================

-- ============================================================================
-- Table: core.sd_sites
-- Description: 사업장 관리 (Multi-Site Support)
-- ============================================================================
CREATE TABLE core.sd_sites (
    site_id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    site_code VARCHAR(50) NOT NULL,
    site_name VARCHAR(200) NOT NULL,

    -- Location
    address TEXT,
    postal_code VARCHAR(20),
    country VARCHAR(50),
    region VARCHAR(100),

    -- Contact
    phone VARCHAR(50),
    fax VARCHAR(50),
    email VARCHAR(100),

    -- Manager
    manager_name VARCHAR(100),
    manager_phone VARCHAR(50),
    manager_email VARCHAR(100),

    -- Type & Status
    site_type VARCHAR(30),  -- FACTORY, WAREHOUSE, OFFICE, RD_CENTER
    is_active BOOLEAN DEFAULT true,

    -- Additional Info
    remarks TEXT,

    -- Audit Fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Foreign Keys
    CONSTRAINT fk_site_tenant FOREIGN KEY (tenant_id)
        REFERENCES core.sd_tenants(tenant_id) ON DELETE CASCADE,

    -- Unique Constraints
    CONSTRAINT uk_site_code UNIQUE (tenant_id, site_code)
);

-- ============================================================================
-- Table: core.sd_departments
-- Description: 부서 관리 (Hierarchical Organization Structure)
-- ============================================================================
CREATE TABLE core.sd_departments (
    department_id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    site_id BIGINT,
    department_code VARCHAR(50) NOT NULL,
    department_name VARCHAR(200) NOT NULL,

    -- Hierarchy
    parent_department_id BIGINT,
    depth_level INTEGER DEFAULT 0,
    sort_order INTEGER DEFAULT 0,

    -- Manager
    manager_user_id BIGINT,

    -- Type
    department_type VARCHAR(30),  -- PRODUCTION, QUALITY, WAREHOUSE, PURCHASING, SALES, RD, ADMIN

    -- Status
    is_active BOOLEAN DEFAULT true,

    -- Additional Info
    remarks TEXT,

    -- Audit Fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Foreign Keys
    CONSTRAINT fk_department_tenant FOREIGN KEY (tenant_id)
        REFERENCES core.sd_tenants(tenant_id) ON DELETE CASCADE,
    CONSTRAINT fk_department_site FOREIGN KEY (site_id)
        REFERENCES core.sd_sites(site_id) ON DELETE SET NULL,
    CONSTRAINT fk_department_parent FOREIGN KEY (parent_department_id)
        REFERENCES core.sd_departments(department_id) ON DELETE SET NULL,
    CONSTRAINT fk_department_manager FOREIGN KEY (manager_user_id)
        REFERENCES core.sd_users(user_id) ON DELETE SET NULL,

    -- Unique Constraints
    CONSTRAINT uk_department_code UNIQUE (tenant_id, department_code)
);

-- ============================================================================
-- Table: core.sd_employees
-- Description: 사원 관리 (HR Information separate from User)
-- ============================================================================
CREATE TABLE core.sd_employees (
    employee_id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    employee_no VARCHAR(50) NOT NULL,

    -- Link to User Account
    user_id BIGINT,

    -- Personal Info
    full_name VARCHAR(100) NOT NULL,
    name_english VARCHAR(100),
    date_of_birth DATE,
    gender VARCHAR(10),  -- MALE, FEMALE, OTHER

    -- Organization
    site_id BIGINT,
    department_id BIGINT,
    position VARCHAR(50),  -- 직위: 사원, 대리, 과장, 차장, 부장, 임원
    job_title VARCHAR(100),  -- 직책: 팀장, 파트장, 실장

    -- Employment
    hire_date DATE,
    employment_type VARCHAR(30),  -- FULL_TIME, PART_TIME, CONTRACT, INTERN
    employment_status VARCHAR(30) DEFAULT 'ACTIVE',  -- ACTIVE, ON_LEAVE, RESIGNED, RETIRED
    resignation_date DATE,

    -- Contact
    phone VARCHAR(50),
    mobile VARCHAR(50),
    email VARCHAR(100),
    emergency_contact VARCHAR(50),
    emergency_phone VARCHAR(50),

    -- Address
    address TEXT,
    postal_code VARCHAR(20),

    -- Additional Info
    remarks TEXT,

    -- Audit Fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Foreign Keys
    CONSTRAINT fk_employee_tenant FOREIGN KEY (tenant_id)
        REFERENCES core.sd_tenants(tenant_id) ON DELETE CASCADE,
    CONSTRAINT fk_employee_user FOREIGN KEY (user_id)
        REFERENCES core.sd_users(user_id) ON DELETE SET NULL,
    CONSTRAINT fk_employee_site FOREIGN KEY (site_id)
        REFERENCES core.sd_sites(site_id) ON DELETE SET NULL,
    CONSTRAINT fk_employee_department FOREIGN KEY (department_id)
        REFERENCES core.sd_departments(department_id) ON DELETE SET NULL,

    -- Unique Constraints
    CONSTRAINT uk_employee_no UNIQUE (tenant_id, employee_no),
    CONSTRAINT uk_employee_user UNIQUE (user_id)
);

-- ============================================================================
-- Table: core.sd_holidays
-- Description: 휴일 관리 (Holiday Calendar for Production Planning)
-- ============================================================================
CREATE TABLE core.sd_holidays (
    holiday_id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    site_id BIGINT,

    holiday_date DATE NOT NULL,
    holiday_name VARCHAR(200) NOT NULL,

    -- Type
    holiday_type VARCHAR(30),  -- NATIONAL, COMPANY, SITE_SPECIFIC

    -- Recurrence
    is_recurring BOOLEAN DEFAULT false,
    recurrence_rule VARCHAR(100),  -- YEARLY, MONTHLY (for annual holidays)

    -- Additional Info
    remarks TEXT,

    -- Audit Fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Foreign Keys
    CONSTRAINT fk_holiday_tenant FOREIGN KEY (tenant_id)
        REFERENCES core.sd_tenants(tenant_id) ON DELETE CASCADE,
    CONSTRAINT fk_holiday_site FOREIGN KEY (site_id)
        REFERENCES core.sd_sites(site_id) ON DELETE CASCADE,

    -- Unique Constraints
    CONSTRAINT uk_holiday UNIQUE (tenant_id, site_id, holiday_date)
);

-- ============================================================================
-- Table: core.sd_approval_lines
-- Description: 결재라인 관리 (Approval Workflow)
-- ============================================================================
CREATE TABLE core.sd_approval_lines (
    approval_line_id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,

    line_code VARCHAR(50) NOT NULL,
    line_name VARCHAR(200) NOT NULL,

    -- Scope
    document_type VARCHAR(50) NOT NULL,  -- PURCHASE_REQUEST, PURCHASE_ORDER, WORK_ORDER, etc.
    department_id BIGINT,  -- Department-specific approval line

    -- Approval Steps (JSON Array)
    -- Example: [{"step":1,"approver_user_id":10,"approver_role":"MANAGER"},{"step":2,"approver_user_id":20,"approver_role":"DIRECTOR"}]
    approval_steps JSONB NOT NULL,

    -- Conditions (JSON)
    -- Example: {"amount_min":0,"amount_max":1000000}
    conditions JSONB,

    -- Status
    is_active BOOLEAN DEFAULT true,
    is_default BOOLEAN DEFAULT false,

    -- Priority (lower number = higher priority)
    priority INTEGER DEFAULT 0,

    -- Additional Info
    remarks TEXT,

    -- Audit Fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Foreign Keys
    CONSTRAINT fk_approval_line_tenant FOREIGN KEY (tenant_id)
        REFERENCES core.sd_tenants(tenant_id) ON DELETE CASCADE,
    CONSTRAINT fk_approval_line_department FOREIGN KEY (department_id)
        REFERENCES core.sd_departments(department_id) ON DELETE SET NULL,

    -- Unique Constraints
    CONSTRAINT uk_approval_line_code UNIQUE (tenant_id, line_code)
);

-- ============================================================================
-- Indexes
-- ============================================================================
-- Sites
CREATE INDEX idx_site_tenant ON core.sd_sites(tenant_id);
CREATE INDEX idx_site_active ON core.sd_sites(is_active);
CREATE INDEX idx_site_type ON core.sd_sites(site_type);

-- Departments
CREATE INDEX idx_department_tenant ON core.sd_departments(tenant_id);
CREATE INDEX idx_department_site ON core.sd_departments(site_id);
CREATE INDEX idx_department_parent ON core.sd_departments(parent_department_id);
CREATE INDEX idx_department_manager ON core.sd_departments(manager_user_id);
CREATE INDEX idx_department_active ON core.sd_departments(is_active);

-- Employees
CREATE INDEX idx_employee_tenant ON core.sd_employees(tenant_id);
CREATE INDEX idx_employee_user ON core.sd_employees(user_id);
CREATE INDEX idx_employee_site ON core.sd_employees(site_id);
CREATE INDEX idx_employee_department ON core.sd_employees(department_id);
CREATE INDEX idx_employee_status ON core.sd_employees(employment_status);

-- Holidays
CREATE INDEX idx_holiday_tenant ON core.sd_holidays(tenant_id);
CREATE INDEX idx_holiday_site ON core.sd_holidays(site_id);
CREATE INDEX idx_holiday_date ON core.sd_holidays(holiday_date);

-- Approval Lines
CREATE INDEX idx_approval_line_tenant ON core.sd_approval_lines(tenant_id);
CREATE INDEX idx_approval_line_document_type ON core.sd_approval_lines(document_type);
CREATE INDEX idx_approval_line_department ON core.sd_approval_lines(department_id);
CREATE INDEX idx_approval_line_active ON core.sd_approval_lines(is_active);

-- ============================================================================
-- Triggers for updated_at
-- ============================================================================
CREATE OR REPLACE FUNCTION core.update_site_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_site_timestamp
    BEFORE UPDATE ON core.sd_sites
    FOR EACH ROW
    EXECUTE FUNCTION core.update_site_timestamp();

CREATE OR REPLACE FUNCTION core.update_department_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_department_timestamp
    BEFORE UPDATE ON core.sd_departments
    FOR EACH ROW
    EXECUTE FUNCTION core.update_department_timestamp();

CREATE OR REPLACE FUNCTION core.update_employee_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_employee_timestamp
    BEFORE UPDATE ON core.sd_employees
    FOR EACH ROW
    EXECUTE FUNCTION core.update_employee_timestamp();

CREATE OR REPLACE FUNCTION core.update_holiday_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_holiday_timestamp
    BEFORE UPDATE ON core.sd_holidays
    FOR EACH ROW
    EXECUTE FUNCTION core.update_holiday_timestamp();

CREATE OR REPLACE FUNCTION core.update_approval_line_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_approval_line_timestamp
    BEFORE UPDATE ON core.sd_approval_lines
    FOR EACH ROW
    EXECUTE FUNCTION core.update_approval_line_timestamp();

-- ============================================================================
-- Comments
-- ============================================================================
-- Sites
COMMENT ON TABLE core.sd_sites IS '사업장 테이블';
COMMENT ON COLUMN core.sd_sites.site_id IS '사업장 ID (PK)';
COMMENT ON COLUMN core.sd_sites.site_type IS '사업장 유형 (FACTORY, WAREHOUSE, OFFICE, RD_CENTER)';

-- Departments
COMMENT ON TABLE core.sd_departments IS '부서 테이블';
COMMENT ON COLUMN core.sd_departments.department_id IS '부서 ID (PK)';
COMMENT ON COLUMN core.sd_departments.parent_department_id IS '상위 부서 ID';
COMMENT ON COLUMN core.sd_departments.depth_level IS '부서 계층 레벨';

-- Employees
COMMENT ON TABLE core.sd_employees IS '사원 테이블';
COMMENT ON COLUMN core.sd_employees.employee_id IS '사원 ID (PK)';
COMMENT ON COLUMN core.sd_employees.employment_status IS '재직 상태 (ACTIVE, ON_LEAVE, RESIGNED, RETIRED)';

-- Holidays
COMMENT ON TABLE core.sd_holidays IS '휴일 테이블';
COMMENT ON COLUMN core.sd_holidays.holiday_id IS '휴일 ID (PK)';
COMMENT ON COLUMN core.sd_holidays.holiday_type IS '휴일 유형 (NATIONAL, COMPANY, SITE_SPECIFIC)';

-- Approval Lines
COMMENT ON TABLE core.sd_approval_lines IS '결재라인 테이블';
COMMENT ON COLUMN core.sd_approval_lines.approval_line_id IS '결재라인 ID (PK)';
COMMENT ON COLUMN core.sd_approval_lines.approval_steps IS '결재 단계 (JSON 배열)';
COMMENT ON COLUMN core.sd_approval_lines.conditions IS '적용 조건 (JSON)';
