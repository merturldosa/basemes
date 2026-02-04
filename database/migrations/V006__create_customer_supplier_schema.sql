-- ============================================================================
-- Migration V006: Customer & Supplier Schema
-- 고객/공급업체 관리 스키마
-- Author: Moon Myung-seop
-- Created: 2026-01-23
-- ============================================================================

-- Create business schema
CREATE SCHEMA IF NOT EXISTS business;

-- ============================================================================
-- Table: business.si_customers
-- 고객 마스터
-- ============================================================================
CREATE TABLE business.si_customers (
    customer_id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    customer_code VARCHAR(50) NOT NULL,
    customer_name VARCHAR(200) NOT NULL,
    customer_type VARCHAR(20) NOT NULL,
    business_number VARCHAR(50),
    representative_name VARCHAR(100),
    industry VARCHAR(100),
    address VARCHAR(500),
    postal_code VARCHAR(20),
    phone_number VARCHAR(50),
    fax_number VARCHAR(50),
    email VARCHAR(100),
    website VARCHAR(200),
    contact_person VARCHAR(100),
    contact_phone VARCHAR(50),
    contact_email VARCHAR(100),
    payment_terms VARCHAR(20),
    credit_limit DECIMAL(15,2),
    currency VARCHAR(10) DEFAULT 'KRW',
    tax_type VARCHAR(20),
    is_active BOOLEAN NOT NULL DEFAULT true,
    remarks TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign Keys
    CONSTRAINT fk_customer_tenant FOREIGN KEY (tenant_id)
        REFERENCES core.si_tenants(tenant_id) ON DELETE CASCADE,

    -- Unique Constraints
    CONSTRAINT uk_customer_code UNIQUE (tenant_id, customer_code)
);

-- Indexes for si_customers
CREATE INDEX idx_customer_tenant ON business.si_customers(tenant_id);
CREATE INDEX idx_customer_type ON business.si_customers(customer_type);
CREATE INDEX idx_customer_active ON business.si_customers(is_active);
CREATE INDEX idx_customer_name ON business.si_customers(customer_name);

-- Comments for si_customers
COMMENT ON TABLE business.si_customers IS '고객 마스터';
COMMENT ON COLUMN business.si_customers.customer_id IS '고객 ID (Primary Key)';
COMMENT ON COLUMN business.si_customers.tenant_id IS '테넌트 ID';
COMMENT ON COLUMN business.si_customers.customer_code IS '고객 코드';
COMMENT ON COLUMN business.si_customers.customer_name IS '고객명';
COMMENT ON COLUMN business.si_customers.customer_type IS '고객 유형 (DOMESTIC, OVERSEAS, BOTH)';
COMMENT ON COLUMN business.si_customers.business_number IS '사업자번호';
COMMENT ON COLUMN business.si_customers.representative_name IS '대표자명';
COMMENT ON COLUMN business.si_customers.industry IS '업종';
COMMENT ON COLUMN business.si_customers.address IS '주소';
COMMENT ON COLUMN business.si_customers.postal_code IS '우편번호';
COMMENT ON COLUMN business.si_customers.phone_number IS '전화번호';
COMMENT ON COLUMN business.si_customers.fax_number IS '팩스번호';
COMMENT ON COLUMN business.si_customers.email IS '이메일';
COMMENT ON COLUMN business.si_customers.website IS '웹사이트';
COMMENT ON COLUMN business.si_customers.contact_person IS '담당자명';
COMMENT ON COLUMN business.si_customers.contact_phone IS '담당자 전화';
COMMENT ON COLUMN business.si_customers.contact_email IS '담당자 이메일';
COMMENT ON COLUMN business.si_customers.payment_terms IS '결제 조건 (NET30, NET60, COD, ADVANCE 등)';
COMMENT ON COLUMN business.si_customers.credit_limit IS '여신 한도';
COMMENT ON COLUMN business.si_customers.currency IS '통화';
COMMENT ON COLUMN business.si_customers.tax_type IS '세금 유형 (TAXABLE, EXEMPT, ZERO_RATE)';
COMMENT ON COLUMN business.si_customers.is_active IS '활성 여부';
COMMENT ON COLUMN business.si_customers.remarks IS '비고';

-- ============================================================================
-- Table: business.si_suppliers
-- 공급업체 마스터
-- ============================================================================
CREATE TABLE business.si_suppliers (
    supplier_id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    supplier_code VARCHAR(50) NOT NULL,
    supplier_name VARCHAR(200) NOT NULL,
    supplier_type VARCHAR(20) NOT NULL,
    business_number VARCHAR(50),
    representative_name VARCHAR(100),
    industry VARCHAR(100),
    address VARCHAR(500),
    postal_code VARCHAR(20),
    phone_number VARCHAR(50),
    fax_number VARCHAR(50),
    email VARCHAR(100),
    website VARCHAR(200),
    contact_person VARCHAR(100),
    contact_phone VARCHAR(50),
    contact_email VARCHAR(100),
    payment_terms VARCHAR(20),
    currency VARCHAR(10) DEFAULT 'KRW',
    tax_type VARCHAR(20),
    lead_time_days INTEGER DEFAULT 0,
    min_order_amount DECIMAL(15,2),
    is_active BOOLEAN NOT NULL DEFAULT true,
    rating VARCHAR(20),
    remarks TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign Keys
    CONSTRAINT fk_supplier_tenant FOREIGN KEY (tenant_id)
        REFERENCES core.si_tenants(tenant_id) ON DELETE CASCADE,

    -- Unique Constraints
    CONSTRAINT uk_supplier_code UNIQUE (tenant_id, supplier_code)
);

-- Indexes for si_suppliers
CREATE INDEX idx_supplier_tenant ON business.si_suppliers(tenant_id);
CREATE INDEX idx_supplier_type ON business.si_suppliers(supplier_type);
CREATE INDEX idx_supplier_active ON business.si_suppliers(is_active);
CREATE INDEX idx_supplier_name ON business.si_suppliers(supplier_name);
CREATE INDEX idx_supplier_rating ON business.si_suppliers(rating);

-- Comments for si_suppliers
COMMENT ON TABLE business.si_suppliers IS '공급업체 마스터';
COMMENT ON COLUMN business.si_suppliers.supplier_id IS '공급업체 ID (Primary Key)';
COMMENT ON COLUMN business.si_suppliers.tenant_id IS '테넌트 ID';
COMMENT ON COLUMN business.si_suppliers.supplier_code IS '공급업체 코드';
COMMENT ON COLUMN business.si_suppliers.supplier_name IS '공급업체명';
COMMENT ON COLUMN business.si_suppliers.supplier_type IS '공급업체 유형 (MATERIAL, SERVICE, EQUIPMENT, BOTH)';
COMMENT ON COLUMN business.si_suppliers.business_number IS '사업자번호';
COMMENT ON COLUMN business.si_suppliers.representative_name IS '대표자명';
COMMENT ON COLUMN business.si_suppliers.industry IS '업종';
COMMENT ON COLUMN business.si_suppliers.address IS '주소';
COMMENT ON COLUMN business.si_suppliers.postal_code IS '우편번호';
COMMENT ON COLUMN business.si_suppliers.phone_number IS '전화번호';
COMMENT ON COLUMN business.si_suppliers.fax_number IS '팩스번호';
COMMENT ON COLUMN business.si_suppliers.email IS '이메일';
COMMENT ON COLUMN business.si_suppliers.website IS '웹사이트';
COMMENT ON COLUMN business.si_suppliers.contact_person IS '담당자명';
COMMENT ON COLUMN business.si_suppliers.contact_phone IS '담당자 전화';
COMMENT ON COLUMN business.si_suppliers.contact_email IS '담당자 이메일';
COMMENT ON COLUMN business.si_suppliers.payment_terms IS '결제 조건 (NET30, NET60, COD, ADVANCE 등)';
COMMENT ON COLUMN business.si_suppliers.currency IS '통화';
COMMENT ON COLUMN business.si_suppliers.tax_type IS '세금 유형 (TAXABLE, EXEMPT, ZERO_RATE)';
COMMENT ON COLUMN business.si_suppliers.lead_time_days IS '리드타임 (일)';
COMMENT ON COLUMN business.si_suppliers.min_order_amount IS '최소 주문 금액';
COMMENT ON COLUMN business.si_suppliers.is_active IS '활성 여부';
COMMENT ON COLUMN business.si_suppliers.rating IS '평가 등급 (EXCELLENT, GOOD, AVERAGE, POOR)';
COMMENT ON COLUMN business.si_suppliers.remarks IS '비고';

-- ============================================================================
-- Triggers for automatic updated_at
-- ============================================================================

-- Trigger for si_customers
CREATE OR REPLACE FUNCTION business.update_si_customers_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_si_customers_updated_at
    BEFORE UPDATE ON business.si_customers
    FOR EACH ROW
    EXECUTE FUNCTION business.update_si_customers_updated_at();

-- Trigger for si_suppliers
CREATE OR REPLACE FUNCTION business.update_si_suppliers_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_si_suppliers_updated_at
    BEFORE UPDATE ON business.si_suppliers
    FOR EACH ROW
    EXECUTE FUNCTION business.update_si_suppliers_updated_at();

-- ============================================================================
-- End of Migration V006
-- ============================================================================
