/**
 * Migration: V019 - Create Returns Schema
 * 반품 관리 스키마 생성
 *
 * 테이블:
 * - wms.sd_returns: 반품 헤더
 * - wms.sd_return_items: 반품 항목
 *
 * 워크플로우:
 * 1. 반품 신청 (PENDING)
 * 2. 반품 승인/거부 (APPROVED/REJECTED)
 * 3. 반품 입고 (RECEIVED)
 * 4. 품질 검사 (INSPECTING)
 * 5. 재고 복원 (COMPLETED)
 *    - 합격품: 원래 창고 재입고
 *    - 불합격품: 격리 창고 이동
 *
 * @author Moon Myung-seop
 * @date 2026-01-24
 */

-- =====================================================
-- 1. sd_returns (반품 헤더)
-- =====================================================
CREATE TABLE wms.sd_returns (
    return_id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,

    -- Return Information
    return_no VARCHAR(50) NOT NULL,                     -- RT-YYYYMMDD-0001
    return_date TIMESTAMP NOT NULL,
    return_type VARCHAR(30) NOT NULL,                   -- DEFECTIVE, EXCESS, WRONG_DELIVERY, OTHER

    -- References
    material_request_id BIGINT,                         -- 원본 불출 신청 (Optional)
    work_order_id BIGINT,                               -- 관련 작업 지시 (Optional)

    -- Requester
    requester_user_id BIGINT NOT NULL,                  -- 반품 신청자
    requester_name VARCHAR(100),

    -- Warehouse
    warehouse_id BIGINT NOT NULL,                       -- 반품 입고 창고

    -- Status
    return_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
        -- PENDING: 대기
        -- APPROVED: 승인
        -- REJECTED: 거부
        -- RECEIVED: 입고 완료
        -- INSPECTING: 검사 중
        -- COMPLETED: 완료 (재고 복원 완료)
        -- CANCELLED: 취소

    -- Approval
    approver_user_id BIGINT,
    approver_name VARCHAR(100),
    approved_date TIMESTAMP,

    -- Dates
    received_date TIMESTAMP,                            -- 입고 완료 일시
    completed_date TIMESTAMP,                           -- 재고 복원 완료 일시

    -- Totals (calculated)
    total_return_quantity NUMERIC(15,3),
    total_received_quantity NUMERIC(15,3),
    total_passed_quantity NUMERIC(15,3),
    total_failed_quantity NUMERIC(15,3),

    -- Notes
    remarks TEXT,
    rejection_reason TEXT,
    cancellation_reason TEXT,

    -- Common
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),

    -- Foreign Keys
    CONSTRAINT fk_return_tenant FOREIGN KEY (tenant_id) REFERENCES common.sd_tenants(tenant_id),
    CONSTRAINT fk_return_material_request FOREIGN KEY (material_request_id) REFERENCES wms.sd_material_requests(material_request_id),
    CONSTRAINT fk_return_work_order FOREIGN KEY (work_order_id) REFERENCES production.sd_work_orders(work_order_id),
    CONSTRAINT fk_return_requester FOREIGN KEY (requester_user_id) REFERENCES common.sd_users(user_id),
    CONSTRAINT fk_return_warehouse FOREIGN KEY (warehouse_id) REFERENCES inventory.sd_warehouses(warehouse_id),
    CONSTRAINT fk_return_approver FOREIGN KEY (approver_user_id) REFERENCES common.sd_users(user_id),

    -- Unique Constraints
    CONSTRAINT uk_return_no UNIQUE (tenant_id, return_no)
);

-- Indexes
CREATE INDEX idx_return_tenant ON wms.sd_returns(tenant_id);
CREATE INDEX idx_return_date ON wms.sd_returns(return_date);
CREATE INDEX idx_return_material_request ON wms.sd_returns(material_request_id);
CREATE INDEX idx_return_work_order ON wms.sd_returns(work_order_id);
CREATE INDEX idx_return_requester ON wms.sd_returns(requester_user_id);
CREATE INDEX idx_return_warehouse ON wms.sd_returns(warehouse_id);
CREATE INDEX idx_return_status ON wms.sd_returns(return_status);
CREATE INDEX idx_return_type ON wms.sd_returns(return_type);

-- Comments
COMMENT ON TABLE wms.sd_returns IS '반품 헤더 - 생산/창고에서 반품 처리';
COMMENT ON COLUMN wms.sd_returns.return_type IS 'DEFECTIVE(불량품), EXCESS(과잉), WRONG_DELIVERY(오배송), OTHER(기타)';
COMMENT ON COLUMN wms.sd_returns.return_status IS 'PENDING → APPROVED → RECEIVED → INSPECTING → COMPLETED';

-- =====================================================
-- 2. sd_return_items (반품 항목)
-- =====================================================
CREATE TABLE wms.sd_return_items (
    return_item_id BIGSERIAL PRIMARY KEY,
    return_id BIGINT NOT NULL,

    -- Product
    product_id BIGINT NOT NULL,
    product_code VARCHAR(50),
    product_name VARCHAR(200),

    -- LOT
    original_lot_no VARCHAR(100),                       -- 원래 불출된 LOT
    new_lot_no VARCHAR(100),                            -- 재입고 시 생성된 새 LOT

    -- Quantities
    return_quantity NUMERIC(15,3) NOT NULL,             -- 반품 신청 수량
    received_quantity NUMERIC(15,3),                    -- 실제 입고 수량
    passed_quantity NUMERIC(15,3),                      -- 합격 수량 (재입고)
    failed_quantity NUMERIC(15,3),                      -- 불합격 수량 (격리)

    -- Quality Inspection
    inspection_status VARCHAR(30) DEFAULT 'NOT_REQUIRED',
        -- NOT_REQUIRED: 검사 불필요
        -- PENDING: 검사 대기
        -- PASS: 합격
        -- FAIL: 불합격
    quality_inspection_id BIGINT,                       -- 품질 검사 ID

    -- Inventory Transaction References
    receive_transaction_id BIGINT,                      -- 입고 트랜잭션 (IN_RETURN)
    pass_transaction_id BIGINT,                         -- 합격품 재입고 트랜잭션
    fail_transaction_id BIGINT,                         -- 불합격품 격리 트랜잭션

    -- Return Reason
    return_reason TEXT,                                 -- 반품 사유
    remarks TEXT,

    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Foreign Keys
    CONSTRAINT fk_return_item_return FOREIGN KEY (return_id) REFERENCES wms.sd_returns(return_id),
    CONSTRAINT fk_return_item_product FOREIGN KEY (product_id) REFERENCES production.sd_products(product_id),
    CONSTRAINT fk_return_item_quality_inspection FOREIGN KEY (quality_inspection_id) REFERENCES qms.sd_quality_inspections(quality_inspection_id),
    CONSTRAINT fk_return_item_receive_transaction FOREIGN KEY (receive_transaction_id) REFERENCES inventory.sd_inventory_transactions(inventory_transaction_id),
    CONSTRAINT fk_return_item_pass_transaction FOREIGN KEY (pass_transaction_id) REFERENCES inventory.sd_inventory_transactions(inventory_transaction_id),
    CONSTRAINT fk_return_item_fail_transaction FOREIGN KEY (fail_transaction_id) REFERENCES inventory.sd_inventory_transactions(inventory_transaction_id)
);

-- Indexes
CREATE INDEX idx_return_item_return ON wms.sd_return_items(return_id);
CREATE INDEX idx_return_item_product ON wms.sd_return_items(product_id);
CREATE INDEX idx_return_item_inspection_status ON wms.sd_return_items(inspection_status);
CREATE INDEX idx_return_item_quality_inspection ON wms.sd_return_items(quality_inspection_id);
CREATE INDEX idx_return_item_original_lot ON wms.sd_return_items(original_lot_no);

-- Comments
COMMENT ON TABLE wms.sd_return_items IS '반품 항목 - 반품별 제품/수량 상세';
COMMENT ON COLUMN wms.sd_return_items.original_lot_no IS '원래 불출된 LOT 번호';
COMMENT ON COLUMN wms.sd_return_items.new_lot_no IS '재입고 시 생성된 새 LOT 번호';
COMMENT ON COLUMN wms.sd_return_items.return_reason IS '반품 사유 (불량 유형, 과잉 사유 등)';

-- =====================================================
-- Triggers for updated_at
-- =====================================================
CREATE TRIGGER update_sd_returns_updated_at
    BEFORE UPDATE ON wms.sd_returns
    FOR EACH ROW
    EXECUTE FUNCTION common.update_updated_at_column();

CREATE TRIGGER update_sd_return_items_updated_at
    BEFORE UPDATE ON wms.sd_return_items
    FOR EACH ROW
    EXECUTE FUNCTION common.update_updated_at_column();
