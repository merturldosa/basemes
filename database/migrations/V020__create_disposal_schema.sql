/**
 * Migration: V020 - Create Disposal Schema
 * 폐기 관리 스키마 생성
 *
 * 테이블:
 * - wms.sd_disposals: 폐기 헤더
 * - wms.sd_disposal_items: 폐기 항목
 *
 * 워크플로우:
 * 1. 폐기 의뢰 (PENDING)
 * 2. 폐기 승인/거부 (APPROVED/REJECTED)
 * 3. 폐기 처리 (PROCESSED) - 재고 차감
 * 4. 폐기 완료 (COMPLETED)
 *
 * 폐기 유형:
 * - DEFECTIVE: 불량품 폐기
 * - EXPIRED: 만료품 폐기
 * - DAMAGED: 파손품 폐기
 * - OBSOLETE: 노후품 폐기
 * - OTHER: 기타
 *
 * @author Moon Myung-seop
 * @date 2026-01-24
 */

-- =====================================================
-- 1. sd_disposals (폐기 헤더)
-- =====================================================
CREATE TABLE wms.sd_disposals (
    disposal_id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,

    -- Disposal Information
    disposal_no VARCHAR(50) NOT NULL,                  -- DIS-YYYYMMDD-0001
    disposal_date TIMESTAMP NOT NULL,
    disposal_type VARCHAR(30) NOT NULL,                -- DEFECTIVE, EXPIRED, DAMAGED, OBSOLETE, OTHER

    -- References
    work_order_id BIGINT,                              -- 관련 작업 지시 (Optional)

    -- Requester
    requester_user_id BIGINT NOT NULL,                 -- 폐기 신청자
    requester_name VARCHAR(100),

    -- Warehouse
    warehouse_id BIGINT NOT NULL,                      -- 폐기 대상 재고 창고

    -- Status
    disposal_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
        -- PENDING: 대기
        -- APPROVED: 승인
        -- REJECTED: 거부
        -- PROCESSED: 처리 중 (재고 차감 완료)
        -- COMPLETED: 완료 (실제 폐기 완료)
        -- CANCELLED: 취소

    -- Approval
    approver_user_id BIGINT,
    approver_name VARCHAR(100),
    approved_date TIMESTAMP,

    -- Processor (폐기 처리자)
    processor_user_id BIGINT,
    processor_name VARCHAR(100),
    processed_date TIMESTAMP,                          -- 재고 차감 일시

    -- Completion
    completed_date TIMESTAMP,                          -- 실제 폐기 완료 일시
    disposal_method VARCHAR(100),                      -- 폐기 방법 (소각, 매립, 위탁 처리 등)
    disposal_location VARCHAR(200),                    -- 폐기 장소

    -- Totals (calculated)
    total_disposal_quantity NUMERIC(15,3),

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
    CONSTRAINT fk_disposal_tenant FOREIGN KEY (tenant_id) REFERENCES common.sd_tenants(tenant_id),
    CONSTRAINT fk_disposal_work_order FOREIGN KEY (work_order_id) REFERENCES production.sd_work_orders(work_order_id),
    CONSTRAINT fk_disposal_requester FOREIGN KEY (requester_user_id) REFERENCES common.sd_users(user_id),
    CONSTRAINT fk_disposal_warehouse FOREIGN KEY (warehouse_id) REFERENCES inventory.sd_warehouses(warehouse_id),
    CONSTRAINT fk_disposal_approver FOREIGN KEY (approver_user_id) REFERENCES common.sd_users(user_id),
    CONSTRAINT fk_disposal_processor FOREIGN KEY (processor_user_id) REFERENCES common.sd_users(user_id),

    -- Unique Constraints
    CONSTRAINT uk_disposal_no UNIQUE (tenant_id, disposal_no)
);

-- Indexes
CREATE INDEX idx_disposal_tenant ON wms.sd_disposals(tenant_id);
CREATE INDEX idx_disposal_date ON wms.sd_disposals(disposal_date);
CREATE INDEX idx_disposal_work_order ON wms.sd_disposals(work_order_id);
CREATE INDEX idx_disposal_requester ON wms.sd_disposals(requester_user_id);
CREATE INDEX idx_disposal_warehouse ON wms.sd_disposals(warehouse_id);
CREATE INDEX idx_disposal_status ON wms.sd_disposals(disposal_status);
CREATE INDEX idx_disposal_type ON wms.sd_disposals(disposal_type);

-- Comments
COMMENT ON TABLE wms.sd_disposals IS '폐기 헤더 - 불량품/만료품 폐기 처리';
COMMENT ON COLUMN wms.sd_disposals.disposal_type IS 'DEFECTIVE(불량품), EXPIRED(만료품), DAMAGED(파손품), OBSOLETE(노후품), OTHER(기타)';
COMMENT ON COLUMN wms.sd_disposals.disposal_status IS 'PENDING → APPROVED → PROCESSED → COMPLETED';
COMMENT ON COLUMN wms.sd_disposals.disposal_method IS '폐기 방법: 소각, 매립, 위탁 처리, 재활용 등';

-- =====================================================
-- 2. sd_disposal_items (폐기 항목)
-- =====================================================
CREATE TABLE wms.sd_disposal_items (
    disposal_item_id BIGSERIAL PRIMARY KEY,
    disposal_id BIGINT NOT NULL,

    -- Product
    product_id BIGINT NOT NULL,
    product_code VARCHAR(50),
    product_name VARCHAR(200),

    -- LOT
    lot_id BIGINT,
    lot_no VARCHAR(100),

    -- Warehouse Location
    warehouse_zone VARCHAR(50),                        -- 창고 구역
    warehouse_rack VARCHAR(50),                        -- 랙
    warehouse_shelf VARCHAR(50),                       -- 선반
    warehouse_bin VARCHAR(50),                         -- 빈

    -- Quantity
    disposal_quantity NUMERIC(15,3) NOT NULL,          -- 폐기 신청 수량
    processed_quantity NUMERIC(15,3),                  -- 실제 폐기 처리 수량

    -- Inventory Transaction Reference
    disposal_transaction_id BIGINT,                    -- 폐기 트랜잭션 (OUT_DISPOSAL)

    -- Quality Issue (불량/만료 사유)
    defect_type VARCHAR(100),                          -- 불량 유형
    defect_description TEXT,                           -- 불량 설명
    expiry_date DATE,                                  -- 유효기간 (만료품인 경우)

    -- Notes
    remarks TEXT,

    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Foreign Keys
    CONSTRAINT fk_disposal_item_disposal FOREIGN KEY (disposal_id) REFERENCES wms.sd_disposals(disposal_id),
    CONSTRAINT fk_disposal_item_product FOREIGN KEY (product_id) REFERENCES production.sd_products(product_id),
    CONSTRAINT fk_disposal_item_lot FOREIGN KEY (lot_id) REFERENCES inventory.sd_lots(lot_id),
    CONSTRAINT fk_disposal_item_transaction FOREIGN KEY (disposal_transaction_id) REFERENCES inventory.sd_inventory_transactions(inventory_transaction_id)
);

-- Indexes
CREATE INDEX idx_disposal_item_disposal ON wms.sd_disposal_items(disposal_id);
CREATE INDEX idx_disposal_item_product ON wms.sd_disposal_items(product_id);
CREATE INDEX idx_disposal_item_lot ON wms.sd_disposal_items(lot_id);
CREATE INDEX idx_disposal_item_lot_no ON wms.sd_disposal_items(lot_no);
CREATE INDEX idx_disposal_item_defect_type ON wms.sd_disposal_items(defect_type);

-- Comments
COMMENT ON TABLE wms.sd_disposal_items IS '폐기 항목 - 폐기별 제품/수량 상세';
COMMENT ON COLUMN wms.sd_disposal_items.defect_type IS '불량 유형: 외관 불량, 기능 불량, 규격 미달 등';
COMMENT ON COLUMN wms.sd_disposal_items.expiry_date IS '유효기간 (만료품인 경우)';

-- =====================================================
-- Triggers for updated_at
-- =====================================================
CREATE TRIGGER update_sd_disposals_updated_at
    BEFORE UPDATE ON wms.sd_disposals
    FOR EACH ROW
    EXECUTE FUNCTION common.update_updated_at_column();

CREATE TRIGGER update_sd_disposal_items_updated_at
    BEFORE UPDATE ON wms.sd_disposal_items
    FOR EACH ROW
    EXECUTE FUNCTION common.update_updated_at_column();
