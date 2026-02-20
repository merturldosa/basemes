-- ============================================================
-- Migration: V011 - Warehouse Operations Schema (Receiving/Shipping)
-- Description: 입하/출하 관리 스키마 생성
-- Author: Moon Myung-seop
-- Date: 2026-01-23
-- ============================================================

-- Create WMS schema if not exists
CREATE SCHEMA IF NOT EXISTS wms;

-- ============================================================
-- 1. GOODS RECEIPTS (입하)
-- ============================================================

CREATE TABLE wms.sd_goods_receipts (
    goods_receipt_id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    purchase_order_id BIGINT,
    receipt_no VARCHAR(50) NOT NULL,
    receipt_date TIMESTAMP NOT NULL,
    supplier_id BIGINT,
    warehouse_id BIGINT NOT NULL,
    receipt_type VARCHAR(30) NOT NULL DEFAULT 'PURCHASE', -- PURCHASE, RETURN, TRANSFER, OTHER
    receipt_status VARCHAR(30) NOT NULL DEFAULT 'PENDING', -- PENDING, INSPECTING, COMPLETED, REJECTED, CANCELLED
    total_quantity DECIMAL(15,3),
    total_amount DECIMAL(15,2),
    receiver_user_id BIGINT,
    receiver_name VARCHAR(100),
    remarks TEXT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),

    -- Foreign Keys
    CONSTRAINT fk_goods_receipt_tenant FOREIGN KEY (tenant_id) REFERENCES core.sd_tenants(tenant_id),
    CONSTRAINT fk_goods_receipt_purchase_order FOREIGN KEY (purchase_order_id) REFERENCES purchase.sd_purchase_orders(purchase_order_id) ON DELETE SET NULL,
    CONSTRAINT fk_goods_receipt_supplier FOREIGN KEY (supplier_id) REFERENCES business.sd_suppliers(supplier_id) ON DELETE SET NULL,
    CONSTRAINT fk_goods_receipt_warehouse FOREIGN KEY (warehouse_id) REFERENCES inventory.sd_warehouses(warehouse_id),
    CONSTRAINT fk_goods_receipt_receiver FOREIGN KEY (receiver_user_id) REFERENCES core.sd_users(user_id) ON DELETE SET NULL,

    -- Unique Constraint
    CONSTRAINT uk_goods_receipt_no UNIQUE (tenant_id, receipt_no)
);

-- Indexes
CREATE INDEX idx_goods_receipt_tenant ON wms.sd_goods_receipts(tenant_id);
CREATE INDEX idx_goods_receipt_date ON wms.sd_goods_receipts(receipt_date);
CREATE INDEX idx_goods_receipt_status ON wms.sd_goods_receipts(receipt_status);
CREATE INDEX idx_goods_receipt_po ON wms.sd_goods_receipts(purchase_order_id);
CREATE INDEX idx_goods_receipt_supplier ON wms.sd_goods_receipts(supplier_id);
CREATE INDEX idx_goods_receipt_warehouse ON wms.sd_goods_receipts(warehouse_id);

-- Comments
COMMENT ON TABLE wms.sd_goods_receipts IS '입하 헤더';
COMMENT ON COLUMN wms.sd_goods_receipts.receipt_type IS '입하 유형: PURCHASE(구매입하), RETURN(반품입하), TRANSFER(이동입하), OTHER(기타)';
COMMENT ON COLUMN wms.sd_goods_receipts.receipt_status IS '입하 상태: PENDING(대기), INSPECTING(검사중), COMPLETED(완료), REJECTED(반려), CANCELLED(취소)';

-- ============================================================
-- 2. GOODS RECEIPT ITEMS (입하 상세)
-- ============================================================

CREATE TABLE wms.sd_goods_receipt_items (
    goods_receipt_item_id BIGSERIAL PRIMARY KEY,
    goods_receipt_id BIGINT NOT NULL,
    purchase_order_item_id BIGINT,
    product_id BIGINT NOT NULL,
    product_code VARCHAR(50),
    product_name VARCHAR(200),
    ordered_quantity DECIMAL(15,3),
    received_quantity DECIMAL(15,3) NOT NULL,
    unit_price DECIMAL(15,2),
    line_amount DECIMAL(15,2),
    lot_no VARCHAR(100),
    expiry_date DATE,
    inspection_status VARCHAR(30) DEFAULT 'NOT_REQUIRED', -- NOT_REQUIRED, PENDING, PASS, FAIL
    quality_inspection_id BIGINT,
    remarks TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Foreign Keys
    CONSTRAINT fk_goods_receipt_item_header FOREIGN KEY (goods_receipt_id) REFERENCES wms.sd_goods_receipts(goods_receipt_id) ON DELETE CASCADE,
    CONSTRAINT fk_goods_receipt_item_po_item FOREIGN KEY (purchase_order_item_id) REFERENCES purchase.sd_purchase_order_items(purchase_order_item_id) ON DELETE SET NULL,
    CONSTRAINT fk_goods_receipt_item_product FOREIGN KEY (product_id) REFERENCES production.sd_products(product_id),
    CONSTRAINT fk_goods_receipt_item_inspection FOREIGN KEY (quality_inspection_id) REFERENCES qms.sd_quality_inspections(quality_inspection_id) ON DELETE SET NULL
);

-- Indexes
CREATE INDEX idx_goods_receipt_item_header ON wms.sd_goods_receipt_items(goods_receipt_id);
CREATE INDEX idx_goods_receipt_item_product ON wms.sd_goods_receipt_items(product_id);
CREATE INDEX idx_goods_receipt_item_lot ON wms.sd_goods_receipt_items(lot_no);
CREATE INDEX idx_goods_receipt_item_inspection ON wms.sd_goods_receipt_items(quality_inspection_id);

-- Comments
COMMENT ON TABLE wms.sd_goods_receipt_items IS '입하 상세';
COMMENT ON COLUMN wms.sd_goods_receipt_items.inspection_status IS '검사 상태: NOT_REQUIRED(검사불필요), PENDING(검사대기), PASS(합격), FAIL(불합격)';

-- ============================================================
-- 3. SHIPPINGS (출하)
-- ============================================================

CREATE TABLE wms.sd_shippings (
    shipping_id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    sales_order_id BIGINT,
    shipping_no VARCHAR(50) NOT NULL,
    shipping_date TIMESTAMP NOT NULL,
    customer_id BIGINT,
    warehouse_id BIGINT NOT NULL,
    shipping_type VARCHAR(30) NOT NULL DEFAULT 'SALES', -- SALES, RETURN, TRANSFER, OTHER
    shipping_status VARCHAR(30) NOT NULL DEFAULT 'PENDING', -- PENDING, INSPECTING, SHIPPED, CANCELLED
    total_quantity DECIMAL(15,3),
    total_amount DECIMAL(15,2),
    shipper_user_id BIGINT,
    shipper_name VARCHAR(100),
    delivery_address TEXT,
    tracking_number VARCHAR(100),
    carrier_name VARCHAR(100),
    remarks TEXT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),

    -- Foreign Keys
    CONSTRAINT fk_shipping_tenant FOREIGN KEY (tenant_id) REFERENCES core.sd_tenants(tenant_id),
    CONSTRAINT fk_shipping_sales_order FOREIGN KEY (sales_order_id) REFERENCES sales.sd_sales_orders(sales_order_id) ON DELETE SET NULL,
    CONSTRAINT fk_shipping_customer FOREIGN KEY (customer_id) REFERENCES business.sd_customers(customer_id) ON DELETE SET NULL,
    CONSTRAINT fk_shipping_warehouse FOREIGN KEY (warehouse_id) REFERENCES inventory.sd_warehouses(warehouse_id),
    CONSTRAINT fk_shipping_shipper FOREIGN KEY (shipper_user_id) REFERENCES core.sd_users(user_id) ON DELETE SET NULL,

    -- Unique Constraint
    CONSTRAINT uk_shipping_no UNIQUE (tenant_id, shipping_no)
);

-- Indexes
CREATE INDEX idx_shipping_tenant ON wms.sd_shippings(tenant_id);
CREATE INDEX idx_shipping_date ON wms.sd_shippings(shipping_date);
CREATE INDEX idx_shipping_status ON wms.sd_shippings(shipping_status);
CREATE INDEX idx_shipping_so ON wms.sd_shippings(sales_order_id);
CREATE INDEX idx_shipping_customer ON wms.sd_shippings(customer_id);
CREATE INDEX idx_shipping_warehouse ON wms.sd_shippings(warehouse_id);

-- Comments
COMMENT ON TABLE wms.sd_shippings IS '출하 헤더';
COMMENT ON COLUMN wms.sd_shippings.shipping_type IS '출하 유형: SALES(판매출하), RETURN(반품출하), TRANSFER(이동출하), OTHER(기타)';
COMMENT ON COLUMN wms.sd_shippings.shipping_status IS '출하 상태: PENDING(대기), INSPECTING(검사중), SHIPPED(출하완료), CANCELLED(취소)';

-- ============================================================
-- 4. SHIPPING ITEMS (출하 상세)
-- ============================================================

CREATE TABLE wms.sd_shipping_items (
    shipping_item_id BIGSERIAL PRIMARY KEY,
    shipping_id BIGINT NOT NULL,
    sales_order_item_id BIGINT,
    product_id BIGINT NOT NULL,
    product_code VARCHAR(50),
    product_name VARCHAR(200),
    ordered_quantity DECIMAL(15,3),
    shipped_quantity DECIMAL(15,3) NOT NULL,
    unit_price DECIMAL(15,2),
    line_amount DECIMAL(15,2),
    lot_no VARCHAR(100),
    expiry_date DATE,
    inspection_status VARCHAR(30) DEFAULT 'NOT_REQUIRED', -- NOT_REQUIRED, PENDING, PASS, FAIL
    quality_inspection_id BIGINT,
    remarks TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Foreign Keys
    CONSTRAINT fk_shipping_item_header FOREIGN KEY (shipping_id) REFERENCES wms.sd_shippings(shipping_id) ON DELETE CASCADE,
    CONSTRAINT fk_shipping_item_so_item FOREIGN KEY (sales_order_item_id) REFERENCES sales.sd_sales_order_items(sales_order_item_id) ON DELETE SET NULL,
    CONSTRAINT fk_shipping_item_product FOREIGN KEY (product_id) REFERENCES production.sd_products(product_id),
    CONSTRAINT fk_shipping_item_inspection FOREIGN KEY (quality_inspection_id) REFERENCES qms.sd_quality_inspections(quality_inspection_id) ON DELETE SET NULL
);

-- Indexes
CREATE INDEX idx_shipping_item_header ON wms.sd_shipping_items(shipping_id);
CREATE INDEX idx_shipping_item_product ON wms.sd_shipping_items(product_id);
CREATE INDEX idx_shipping_item_lot ON wms.sd_shipping_items(lot_no);
CREATE INDEX idx_shipping_item_inspection ON wms.sd_shipping_items(quality_inspection_id);

-- Comments
COMMENT ON TABLE wms.sd_shipping_items IS '출하 상세';
COMMENT ON COLUMN wms.sd_shipping_items.inspection_status IS '검사 상태: NOT_REQUIRED(검사불필요), PENDING(검사대기), PASS(합격), FAIL(불합격)';

-- ============================================================
-- 5. TRIGGERS - Auto update timestamp
-- ============================================================

-- Goods Receipts
CREATE OR REPLACE FUNCTION wms.update_goods_receipt_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_goods_receipt_updated_at
    BEFORE UPDATE ON wms.sd_goods_receipts
    FOR EACH ROW
    EXECUTE FUNCTION wms.update_goods_receipt_updated_at();

-- Goods Receipt Items
CREATE OR REPLACE FUNCTION wms.update_goods_receipt_item_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_goods_receipt_item_updated_at
    BEFORE UPDATE ON wms.sd_goods_receipt_items
    FOR EACH ROW
    EXECUTE FUNCTION wms.update_goods_receipt_item_updated_at();

-- Shippings
CREATE OR REPLACE FUNCTION wms.update_shipping_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_shipping_updated_at
    BEFORE UPDATE ON wms.sd_shippings
    FOR EACH ROW
    EXECUTE FUNCTION wms.update_shipping_updated_at();

-- Shipping Items
CREATE OR REPLACE FUNCTION wms.update_shipping_item_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_shipping_item_updated_at
    BEFORE UPDATE ON wms.sd_shipping_items
    FOR EACH ROW
    EXECUTE FUNCTION wms.update_shipping_item_updated_at();

-- ============================================================
-- End of Migration V011
-- ============================================================
