-- ============================================================
-- Shop 연동용 시드 데이터
-- ============================================================
-- File: database/seeds/004_shop_integration.sql
-- Purpose: SynDock.Shop ↔ MES E2E 통합 테스트용 테넌트/사용자/상품/재고
-- Date: 2026-03-02
-- ============================================================

\echo '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━'
\echo 'Inserting Shop Integration Seed Data...'
\echo '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━'

-- ============================================================
-- 1. 테넌트 (Shop 연동 전용)
-- ============================================================

INSERT INTO common.SD_Tenants (
    tenant_id, tenant_name, tenant_code,
    company_name, business_number, representative_name,
    industry_type, industry_sub_type,
    address, phone, email, website,
    config, status
) VALUES
('smartdocking', 'SmartDocking Shop', 'SMARTDOCK',
 '(주)스마트도킹스테이션', '123-45-67890', '문명섭',
 'retail', 'online_shop',
 '경기도 성남시', '031-689-4707', 'shop@smartdocking.co.kr', 'www.smartdocking.co.kr',
 '{"features": ["mes", "wms"], "shop_integration": true, "max_users": 10}'::jsonb, 'active')
ON CONFLICT DO NOTHING;

\echo '  ✓ Tenant inserted (smartdocking)'

-- ============================================================
-- 2. 사용자 (Shop API 연동 전용)
-- ============================================================

INSERT INTO common.SD_Users (
    tenant_id, username, email, password_hash,
    full_name, employee_number, department, position,
    phone, mobile, status, is_email_verified, preferred_language
) VALUES
('smartdocking', 'shop_api', 'shop_api@smartdocking.co.kr',
 '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- password: shop_api123
 'Shop API User', 'API001', 'Integration', 'API_USER',
 NULL, NULL, 'active', TRUE, 'ko'),

('smartdocking', 'admin', 'admin@smartdocking.co.kr',
 '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- password: admin123
 'SmartDocking 관리자', 'ADM001', 'Management', 'Admin',
 '031-689-4707', '010-4882-2035', 'active', TRUE, 'ko')
ON CONFLICT DO NOTHING;

\echo '  ✓ Users inserted (shop_api, admin)'

-- ============================================================
-- 3. 상품 (Catholia 쇼핑몰 매핑용)
-- ============================================================

INSERT INTO mes.sd_products (
    tenant_id, product_code, product_name, product_type,
    specification, unit, standard_cycle_time, description,
    created_by, updated_by
) VALUES
('smartdocking', 'CATH-001', '묵주', '완제품',
 '가톨릭 성물 묵주', 'EA', NULL, 'Catholia 쇼핑몰 연동 상품 - 묵주',
 'shop_api', 'shop_api'),

('smartdocking', 'CATH-002', '성수병', '완제품',
 '가톨릭 성물 성수병', 'EA', NULL, 'Catholia 쇼핑몰 연동 상품 - 성수병',
 'shop_api', 'shop_api'),

('smartdocking', 'CATH-003', '성경', '완제품',
 '가톨릭 성경책', 'EA', NULL, 'Catholia 쇼핑몰 연동 상품 - 성경',
 'shop_api', 'shop_api'),

('smartdocking', 'CATH-004', '성상', '완제품',
 '가톨릭 성물 성상', 'EA', NULL, 'Catholia 쇼핑몰 연동 상품 - 성상',
 'shop_api', 'shop_api'),

('smartdocking', 'CATH-005', '십자가', '완제품',
 '가톨릭 성물 십자가', 'EA', NULL, 'Catholia 쇼핑몰 연동 상품 - 십자가',
 'shop_api', 'shop_api');

\echo '  ✓ Products inserted (CATH-001 ~ CATH-005)'

-- ============================================================
-- 4. 창고 (Shop 완제품 창고)
-- ============================================================

INSERT INTO inventory.sd_warehouses (
    tenant_id, warehouse_code, warehouse_name, warehouse_type,
    location, manager_name, capacity, is_active
) VALUES
('smartdocking', 'WH-SHOP', 'Shop 완제품 창고', 'FINISHED_GOODS',
 '물류센터 1층', 'Shop 관리자', 5000.00, TRUE)
ON CONFLICT (tenant_id, warehouse_code) DO NOTHING;

\echo '  ✓ Warehouse inserted (WH-SHOP)'

-- ============================================================
-- 5. 재고 (테스트용 초기 재고)
-- ============================================================

-- 묵주 재고
INSERT INTO inventory.sd_inventory (
    tenant_id, warehouse_id, product_id, lot_id,
    available_quantity, reserved_quantity,
    last_transaction_date, last_transaction_type
) VALUES
('smartdocking',
 (SELECT warehouse_id FROM inventory.sd_warehouses WHERE tenant_id = 'smartdocking' AND warehouse_code = 'WH-SHOP'),
 (SELECT product_id FROM mes.sd_products WHERE tenant_id = 'smartdocking' AND product_code = 'CATH-001'),
 NULL,
 150.000, 0.000,
 NOW(), 'IN_RECEIVE')
ON CONFLICT (tenant_id, warehouse_id, product_id, lot_id) DO NOTHING;

-- 성수병 재고
INSERT INTO inventory.sd_inventory (
    tenant_id, warehouse_id, product_id, lot_id,
    available_quantity, reserved_quantity,
    last_transaction_date, last_transaction_type
) VALUES
('smartdocking',
 (SELECT warehouse_id FROM inventory.sd_warehouses WHERE tenant_id = 'smartdocking' AND warehouse_code = 'WH-SHOP'),
 (SELECT product_id FROM mes.sd_products WHERE tenant_id = 'smartdocking' AND product_code = 'CATH-002'),
 NULL,
 80.000, 5.000,
 NOW(), 'IN_RECEIVE')
ON CONFLICT (tenant_id, warehouse_id, product_id, lot_id) DO NOTHING;

-- 성경 재고
INSERT INTO inventory.sd_inventory (
    tenant_id, warehouse_id, product_id, lot_id,
    available_quantity, reserved_quantity,
    last_transaction_date, last_transaction_type
) VALUES
('smartdocking',
 (SELECT warehouse_id FROM inventory.sd_warehouses WHERE tenant_id = 'smartdocking' AND warehouse_code = 'WH-SHOP'),
 (SELECT product_id FROM mes.sd_products WHERE tenant_id = 'smartdocking' AND product_code = 'CATH-003'),
 NULL,
 200.000, 10.000,
 NOW(), 'IN_RECEIVE')
ON CONFLICT (tenant_id, warehouse_id, product_id, lot_id) DO NOTHING;

-- 성상 재고
INSERT INTO inventory.sd_inventory (
    tenant_id, warehouse_id, product_id, lot_id,
    available_quantity, reserved_quantity,
    last_transaction_date, last_transaction_type
) VALUES
('smartdocking',
 (SELECT warehouse_id FROM inventory.sd_warehouses WHERE tenant_id = 'smartdocking' AND warehouse_code = 'WH-SHOP'),
 (SELECT product_id FROM mes.sd_products WHERE tenant_id = 'smartdocking' AND product_code = 'CATH-004'),
 NULL,
 45.000, 0.000,
 NOW(), 'IN_RECEIVE')
ON CONFLICT (tenant_id, warehouse_id, product_id, lot_id) DO NOTHING;

-- 십자가 재고
INSERT INTO inventory.sd_inventory (
    tenant_id, warehouse_id, product_id, lot_id,
    available_quantity, reserved_quantity,
    last_transaction_date, last_transaction_type
) VALUES
('smartdocking',
 (SELECT warehouse_id FROM inventory.sd_warehouses WHERE tenant_id = 'smartdocking' AND warehouse_code = 'WH-SHOP'),
 (SELECT product_id FROM mes.sd_products WHERE tenant_id = 'smartdocking' AND product_code = 'CATH-005'),
 NULL,
 30.000, 2.000,
 NOW(), 'IN_RECEIVE')
ON CONFLICT (tenant_id, warehouse_id, product_id, lot_id) DO NOTHING;

\echo '  ✓ Inventory inserted (5 products)'

-- ============================================================
-- 6. 고객 (Shop 연동용)
-- ============================================================

INSERT INTO business.sd_customers (
    tenant_id, customer_code, customer_name, customer_type,
    business_number, representative_name,
    address, phone, email,
    payment_terms, is_active
) VALUES
('smartdocking', 'CUST-SHOP', 'Catholia 온라인몰', 'ONLINE',
 '123-45-67890', '문명섭',
 '경기도 성남시', '031-689-4707', 'shop@smartdocking.co.kr',
 'NET30', TRUE)
ON CONFLICT (tenant_id, customer_code) DO NOTHING;

\echo '  ✓ Customer inserted (CUST-SHOP)'

\echo '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━'
\echo 'Shop Integration Seed Data Complete!'
\echo '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━'
\echo ''
\echo 'Shop 연동 정보:'
\echo '  - Tenant ID: smartdocking'
\echo '  - API User: shop_api / shop_api123'
\echo '  - Products: CATH-001 ~ CATH-005'
\echo '  - Warehouse: WH-SHOP (완제품 창고)'
\echo '  - Customer: CUST-SHOP'
\echo ''
\echo 'Shop appsettings.json 설정:'
\echo '  Mes:TenantId = "smartdocking"'
\echo '  Mes:Username = "shop_api"'
\echo '  Mes:Password = "shop_api123"'
