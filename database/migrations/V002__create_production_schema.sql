-- ============================================================================
-- Migration: V002 - Production Management Schema
-- Description: Creates tables for MES Production Management Module
-- Author: Moon Myung-seop
-- Date: 2026-01-19
-- ============================================================================

-- ============================================================================
-- 1. Products Table (제품 정보)
-- ============================================================================
CREATE TABLE mes.sd_products (
    product_id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    product_code VARCHAR(50) NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    product_type VARCHAR(50),
    specification TEXT,
    unit VARCHAR(20) NOT NULL DEFAULT 'EA',
    standard_cycle_time INTEGER,  -- 표준 사이클 타임 (초)
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),

    CONSTRAINT fk_sd_products_tenant
        FOREIGN KEY (tenant_id) REFERENCES common.sd_tenants(tenant_id),
    CONSTRAINT uk_sd_products_tenant_code
        UNIQUE (tenant_id, product_code)
);

CREATE INDEX idx_sd_products_tenant ON mes.sd_products(tenant_id);
CREATE INDEX idx_sd_products_code ON mes.sd_products(product_code);
CREATE INDEX idx_sd_products_name ON mes.sd_products(product_name);

COMMENT ON TABLE mes.sd_products IS '제품 마스터';
COMMENT ON COLUMN mes.sd_products.product_code IS '제품 코드';
COMMENT ON COLUMN mes.sd_products.product_name IS '제품명';
COMMENT ON COLUMN mes.sd_products.product_type IS '제품 유형 (완제품, 반제품, 원자재 등)';
COMMENT ON COLUMN mes.sd_products.specification IS '제품 규격';
COMMENT ON COLUMN mes.sd_products.unit IS '단위 (EA, KG, L 등)';
COMMENT ON COLUMN mes.sd_products.standard_cycle_time IS '표준 사이클 타임 (초)';

-- ============================================================================
-- 2. Processes Table (공정 정보)
-- ============================================================================
CREATE TABLE mes.sd_processes (
    process_id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    process_code VARCHAR(50) NOT NULL,
    process_name VARCHAR(200) NOT NULL,
    process_type VARCHAR(50),
    sequence_order INTEGER NOT NULL DEFAULT 1,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),

    CONSTRAINT fk_sd_processes_tenant
        FOREIGN KEY (tenant_id) REFERENCES common.sd_tenants(tenant_id),
    CONSTRAINT uk_sd_processes_tenant_code
        UNIQUE (tenant_id, process_code)
);

CREATE INDEX idx_sd_processes_tenant ON mes.sd_processes(tenant_id);
CREATE INDEX idx_sd_processes_code ON mes.sd_processes(process_code);
CREATE INDEX idx_sd_processes_sequence ON mes.sd_processes(sequence_order);

COMMENT ON TABLE mes.sd_processes IS '공정 마스터';
COMMENT ON COLUMN mes.sd_processes.process_code IS '공정 코드';
COMMENT ON COLUMN mes.sd_processes.process_name IS '공정명';
COMMENT ON COLUMN mes.sd_processes.process_type IS '공정 유형';
COMMENT ON COLUMN mes.sd_processes.sequence_order IS '공정 순서';

-- ============================================================================
-- 3. Work Orders Table (작업 지시)
-- ============================================================================
CREATE TABLE mes.sd_work_orders (
    work_order_id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    work_order_no VARCHAR(50) NOT NULL,
    product_id BIGINT NOT NULL,
    process_id BIGINT NOT NULL,

    -- 생산 계획
    planned_quantity DECIMAL(15, 3) NOT NULL,
    planned_start_date TIMESTAMP NOT NULL,
    planned_end_date TIMESTAMP NOT NULL,

    -- 실적 집계
    actual_quantity DECIMAL(15, 3) DEFAULT 0,
    good_quantity DECIMAL(15, 3) DEFAULT 0,
    defect_quantity DECIMAL(15, 3) DEFAULT 0,

    -- 작업 상태
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    -- PENDING: 대기, READY: 준비완료, IN_PROGRESS: 진행중, COMPLETED: 완료, CANCELLED: 취소

    priority INTEGER DEFAULT 5,  -- 1(높음) ~ 10(낮음)

    -- 일정
    actual_start_date TIMESTAMP,
    actual_end_date TIMESTAMP,

    -- 담당자
    assigned_user_id BIGINT,

    remarks TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),

    CONSTRAINT fk_sd_work_orders_tenant
        FOREIGN KEY (tenant_id) REFERENCES common.sd_tenants(tenant_id),
    CONSTRAINT fk_sd_work_orders_product
        FOREIGN KEY (product_id) REFERENCES mes.sd_products(product_id),
    CONSTRAINT fk_sd_work_orders_process
        FOREIGN KEY (process_id) REFERENCES mes.sd_processes(process_id),
    CONSTRAINT fk_sd_work_orders_assigned_user
        FOREIGN KEY (assigned_user_id) REFERENCES common.sd_users(user_id),
    CONSTRAINT uk_sd_work_orders_tenant_no
        UNIQUE (tenant_id, work_order_no),
    CONSTRAINT ck_sd_work_orders_status
        CHECK (status IN ('PENDING', 'READY', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'))
);

CREATE INDEX idx_sd_work_orders_tenant ON mes.sd_work_orders(tenant_id);
CREATE INDEX idx_sd_work_orders_no ON mes.sd_work_orders(work_order_no);
CREATE INDEX idx_sd_work_orders_status ON mes.sd_work_orders(status);
CREATE INDEX idx_sd_work_orders_product ON mes.sd_work_orders(product_id);
CREATE INDEX idx_sd_work_orders_process ON mes.sd_work_orders(process_id);
CREATE INDEX idx_sd_work_orders_date ON mes.sd_work_orders(planned_start_date, planned_end_date);

COMMENT ON TABLE mes.sd_work_orders IS '작업 지시서';
COMMENT ON COLUMN mes.sd_work_orders.work_order_no IS '작업지시 번호';
COMMENT ON COLUMN mes.sd_work_orders.planned_quantity IS '계획 수량';
COMMENT ON COLUMN mes.sd_work_orders.actual_quantity IS '실적 수량';
COMMENT ON COLUMN mes.sd_work_orders.good_quantity IS '양품 수량';
COMMENT ON COLUMN mes.sd_work_orders.defect_quantity IS '불량 수량';
COMMENT ON COLUMN mes.sd_work_orders.status IS '작업 상태';
COMMENT ON COLUMN mes.sd_work_orders.priority IS '우선순위 (1=높음, 10=낮음)';

-- ============================================================================
-- 4. Work Results Table (작업 실적)
-- ============================================================================
CREATE TABLE mes.sd_work_results (
    work_result_id BIGSERIAL PRIMARY KEY,
    work_order_id BIGINT NOT NULL,
    tenant_id VARCHAR(50) NOT NULL,

    -- 실적 정보
    result_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    quantity DECIMAL(15, 3) NOT NULL,
    good_quantity DECIMAL(15, 3) NOT NULL DEFAULT 0,
    defect_quantity DECIMAL(15, 3) NOT NULL DEFAULT 0,

    -- 작업 시간
    work_start_time TIMESTAMP NOT NULL,
    work_end_time TIMESTAMP NOT NULL,
    work_duration INTEGER,  -- 작업 시간 (분)

    -- 작업자
    worker_user_id BIGINT NOT NULL,
    worker_name VARCHAR(100),

    -- 추가 정보
    defect_reason TEXT,
    remarks TEXT,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),

    CONSTRAINT fk_sd_work_results_work_order
        FOREIGN KEY (work_order_id) REFERENCES mes.sd_work_orders(work_order_id) ON DELETE CASCADE,
    CONSTRAINT fk_sd_work_results_tenant
        FOREIGN KEY (tenant_id) REFERENCES common.sd_tenants(tenant_id),
    CONSTRAINT fk_sd_work_results_worker
        FOREIGN KEY (worker_user_id) REFERENCES common.sd_users(user_id),
    CONSTRAINT ck_sd_work_results_quantity
        CHECK (quantity >= 0 AND good_quantity >= 0 AND defect_quantity >= 0),
    CONSTRAINT ck_sd_work_results_time
        CHECK (work_end_time >= work_start_time)
);

CREATE INDEX idx_sd_work_results_work_order ON mes.sd_work_results(work_order_id);
CREATE INDEX idx_sd_work_results_tenant ON mes.sd_work_results(tenant_id);
CREATE INDEX idx_sd_work_results_date ON mes.sd_work_results(result_date);
CREATE INDEX idx_sd_work_results_worker ON mes.sd_work_results(worker_user_id);

COMMENT ON TABLE mes.sd_work_results IS '작업 실적';
COMMENT ON COLUMN mes.sd_work_results.result_date IS '실적 등록 일시';
COMMENT ON COLUMN mes.sd_work_results.quantity IS '생산 수량';
COMMENT ON COLUMN mes.sd_work_results.good_quantity IS '양품 수량';
COMMENT ON COLUMN mes.sd_work_results.defect_quantity IS '불량 수량';
COMMENT ON COLUMN mes.sd_work_results.work_duration IS '작업 시간 (분)';
COMMENT ON COLUMN mes.sd_work_results.defect_reason IS '불량 사유';

-- ============================================================================
-- Triggers for updated_at
-- ============================================================================

CREATE OR REPLACE FUNCTION mes.update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER trigger_sd_products_updated_at
    BEFORE UPDATE ON mes.sd_products
    FOR EACH ROW
    EXECUTE FUNCTION mes.update_updated_at_column();

CREATE TRIGGER trigger_sd_processes_updated_at
    BEFORE UPDATE ON mes.sd_processes
    FOR EACH ROW
    EXECUTE FUNCTION mes.update_updated_at_column();

CREATE TRIGGER trigger_sd_work_orders_updated_at
    BEFORE UPDATE ON mes.sd_work_orders
    FOR EACH ROW
    EXECUTE FUNCTION mes.update_updated_at_column();

CREATE TRIGGER trigger_sd_work_results_updated_at
    BEFORE UPDATE ON mes.sd_work_results
    FOR EACH ROW
    EXECUTE FUNCTION mes.update_updated_at_column();

-- ============================================================================
-- End of Migration V002
-- ============================================================================
