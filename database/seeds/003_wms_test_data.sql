-- ============================================================
-- WMS 모듈 통합 테스트 데이터
-- ============================================================
-- File: database/seeds/003_wms_test_data.sql
-- Purpose: WMS E2E 통합 테스트를 위한 기본 데이터 생성
-- Date: 2026-01-25
-- ============================================================

-- ============================================================
-- 1. 창고 생성 (Warehouses)
-- ============================================================

-- 원자재 창고
INSERT INTO inventory.si_warehouses (
    tenant_id, warehouse_code, warehouse_name, warehouse_type,
    location, manager_name, capacity, is_active
) VALUES
('DEMO001', 'WH-RAW', '원자재 창고', 'RAW_MATERIAL',
 '1동 1층', '김재고', 10000.00, TRUE)
ON CONFLICT (tenant_id, warehouse_code) DO NOTHING;

-- 재공품 창고
INSERT INTO inventory.si_warehouses (
    tenant_id, warehouse_code, warehouse_name, warehouse_type,
    location, manager_name, capacity, is_active
) VALUES
('DEMO001', 'WH-WIP', '재공품 창고', 'WIP',
 '1동 2층', '김재고', 5000.00, TRUE)
ON CONFLICT (tenant_id, warehouse_code) DO NOTHING;

-- 완제품 창고
INSERT INTO inventory.si_warehouses (
    tenant_id, warehouse_code, warehouse_name, warehouse_type,
    location, manager_name, capacity, is_active
) VALUES
('DEMO001', 'WH-FG', '완제품 창고', 'FINISHED_GOODS',
 '2동 1층', '김재고', 8000.00, TRUE)
ON CONFLICT (tenant_id, warehouse_code) DO NOTHING;

-- 격리 창고
INSERT INTO inventory.si_warehouses (
    tenant_id, warehouse_code, warehouse_name, warehouse_type,
    location, manager_name, capacity, is_active
) VALUES
('DEMO001', 'WH-QRT', '격리 창고', 'QUARANTINE',
 '3동 지하', '김재고', 1000.00, TRUE)
ON CONFLICT (tenant_id, warehouse_code) DO NOTHING;

-- 스크랩 창고
INSERT INTO inventory.si_warehouses (
    tenant_id, warehouse_code, warehouse_name, warehouse_type,
    location, manager_name, capacity, is_active
) VALUES
('DEMO001', 'WH-SCRAP', '스크랩 창고', 'SCRAP',
 '3동 외부', '김재고', 500.00, TRUE)
ON CONFLICT (tenant_id, warehouse_code) DO NOTHING;

-- ============================================================
-- 2. 공급업체 생성 (Suppliers)
-- ============================================================

INSERT INTO business.si_suppliers (
    tenant_id, supplier_code, supplier_name, business_number,
    representative_name, contact_person, contact_phone, contact_email,
    address, supplier_type, payment_terms, is_active
) VALUES
('DEMO001', 'SUP-001', 'ABC 전자부품', '123-45-67890',
 '박공급', '이담당', '02-1234-5678', 'contact@abc-elec.com',
 '서울시 금천구 가산디지털1로 123', 'RAW_MATERIAL', 'NET30', TRUE)
ON CONFLICT (tenant_id, supplier_code) DO NOTHING;

INSERT INTO business.si_suppliers (
    tenant_id, supplier_code, supplier_name, business_number,
    representative_name, contact_person, contact_phone, contact_email,
    address, supplier_type, payment_terms, is_active
) VALUES
('DEMO001', 'SUP-002', 'XYZ 화학', '987-65-43210',
 '최공급', '김담당', '031-9876-5432', 'info@xyz-chem.com',
 '경기도 안산시 단원구 신길동 456', 'RAW_MATERIAL', 'NET45', TRUE)
ON CONFLICT (tenant_id, supplier_code) DO NOTHING;

-- ============================================================
-- 3. 고객 생성 (Customers)
-- ============================================================

INSERT INTO business.si_customers (
    tenant_id, customer_code, customer_name, business_number,
    representative_name, contact_person, contact_phone, contact_email,
    address, customer_type, payment_terms, is_active
) VALUES
('DEMO001', 'CUST-001', '대한전자', '111-22-33444',
 '김대표', '박구매', '02-2222-3333', 'purchase@daehan.com',
 '서울시 강남구 테헤란로 789', 'MANUFACTURER', 'NET30', TRUE)
ON CONFLICT (tenant_id, customer_code) DO NOTHING;

INSERT INTO business.si_customers (
    tenant_id, customer_code, customer_name, business_number,
    representative_name, contact_person, contact_phone, contact_email,
    address, customer_type, payment_terms, is_active
) VALUES
('DEMO001', 'CUST-002', '한국디스플레이', '555-66-77888',
 '이대표', '정구매', '031-4444-5555', 'buy@korea-display.com',
 '경기도 수원시 영통구 광교로 321', 'MANUFACTURER', 'NET45', TRUE)
ON CONFLICT (tenant_id, customer_code) DO NOTHING;

-- ============================================================
-- 4. 품질 기준 생성 (Quality Standards)
-- ============================================================

-- IQC (입고 품질 검사) 기준 - PCB
INSERT INTO qms.si_quality_standards (
    tenant_id, product_id, standard_code, standard_name, standard_version,
    inspection_type, min_value, max_value, target_value, tolerance_value,
    unit, measurement_item, measurement_equipment, sampling_method,
    effective_date, is_active
) VALUES
(
    'DEMO001',
    (SELECT product_id FROM mes.si_products WHERE tenant_id = 'DEMO001' AND product_code = 'P-PCB-001' LIMIT 1),
    'QS-PCB-IQC-001', 'PCB 입고 검사 기준', '1.0',
    'INCOMING', 0.95, 1.00, 0.99, 0.02,
    '%', '양품율', '육안 검사', '전수 검사',
    CURRENT_DATE, TRUE
)
ON CONFLICT (tenant_id, standard_code) DO NOTHING;

-- OQC (출하 품질 검사) 기준 - LCD 패널
INSERT INTO qms.si_quality_standards (
    tenant_id, product_id, standard_code, standard_name, standard_version,
    inspection_type, min_value, max_value, target_value, tolerance_value,
    unit, measurement_item, measurement_equipment, sampling_method,
    effective_date, is_active
) VALUES
(
    'DEMO001',
    (SELECT product_id FROM mes.si_products WHERE tenant_id = 'DEMO001' AND product_code = 'P-LCD-001' LIMIT 1),
    'QS-LCD-OQC-001', 'LCD 출하 검사 기준', '1.0',
    'OUTGOING', 0.98, 1.00, 0.99, 0.01,
    '%', '외관 양품율', '육안 검사 + 화질 테스트', '샘플링 (10%)',
    CURRENT_DATE, TRUE
)
ON CONFLICT (tenant_id, standard_code) DO NOTHING;

-- ============================================================
-- 5. BOM (Bill of Materials) 생성
-- ============================================================

-- P-LCD-001 (32인치 LCD 패널) BOM
-- 1개 생산에 필요한 부품: PCB 2개

INSERT INTO bom.si_boms (
    tenant_id, product_id, bom_code, bom_name, bom_version,
    bom_type, effective_date, is_active
) VALUES
(
    'DEMO001',
    (SELECT product_id FROM mes.si_products WHERE tenant_id = 'DEMO001' AND product_code = 'P-LCD-001' LIMIT 1),
    'BOM-LCD-001', '32인치 LCD 패널 BOM', '1.0',
    'PRODUCTION', CURRENT_DATE, TRUE
)
ON CONFLICT (tenant_id, bom_code) DO NOTHING;

-- BOM 항목: PCB 2개 필요
INSERT INTO bom.si_bom_items (
    tenant_id, bom_id, item_seq, component_id, required_quantity,
    unit, scrap_rate, remarks
) VALUES
(
    'DEMO001',
    (SELECT bom_id FROM bom.si_boms WHERE tenant_id = 'DEMO001' AND bom_code = 'BOM-LCD-001' LIMIT 1),
    1,
    (SELECT product_id FROM mes.si_products WHERE tenant_id = 'DEMO001' AND product_code = 'P-PCB-001' LIMIT 1),
    2.00, 'EA', 0.00, 'LCD 구동용 PCB 2개 필요'
)
ON CONFLICT (tenant_id, bom_id, item_seq) DO NOTHING;

-- ============================================================
-- 6. 알람 템플릿 추가 (WMS 관련)
-- ============================================================

-- 저재고 알림
INSERT INTO common.si_alarm_templates (
    tenant_id, template_code, template_name, alarm_type, event_type,
    title_template, message_template,
    enable_email, enable_sms, enable_push, enable_system,
    priority, is_active
) VALUES
('DEMO001', 'WMS_LOW_STOCK', '저재고 알림', 'INVENTORY', 'LOW_STOCK',
 '[저재고] {{productName}} 재고 부족',
 '{{productName}}의 재고가 안전 재고({{safetyStock}}) 이하로 떨어졌습니다. 현재: {{currentStock}}',
 TRUE, TRUE, TRUE, TRUE, 'HIGH', TRUE)
ON CONFLICT (tenant_id, template_code) DO NOTHING;

-- 유효기간 임박 알림
INSERT INTO common.si_alarm_templates (
    tenant_id, template_code, template_name, alarm_type, event_type,
    title_template, message_template,
    enable_email, enable_sms, enable_push, enable_system,
    priority, is_active
) VALUES
('DEMO001', 'WMS_EXPIRY_WARNING', '유효기간 임박', 'INVENTORY', 'EXPIRY_WARNING',
 '[유효기간] LOT {{lotNo}} 유효기간 임박',
 'LOT {{lotNo}}의 유효기간이 {{daysRemaining}}일 남았습니다. 제품: {{productName}}',
 TRUE, FALSE, TRUE, TRUE, 'NORMAL', TRUE)
ON CONFLICT (tenant_id, template_code) DO NOTHING;

-- ============================================================
-- 7. 테스트 확인 쿼리 (선택적 실행)
-- ============================================================

-- 창고 목록 확인
-- SELECT warehouse_code, warehouse_name, warehouse_type, is_active
-- FROM inventory.si_warehouses
-- WHERE tenant_id = 'DEMO001'
-- ORDER BY warehouse_code;

-- 공급업체 확인
-- SELECT supplier_code, supplier_name, contact_person, is_active
-- FROM business.si_suppliers
-- WHERE tenant_id = 'DEMO001'
-- ORDER BY supplier_code;

-- 고객 확인
-- SELECT customer_code, customer_name, contact_person, is_active
-- FROM business.si_customers
-- WHERE tenant_id = 'DEMO001'
-- ORDER BY customer_code;

-- 품질 기준 확인
-- SELECT qs.standard_code, qs.standard_name, qs.inspection_type,
--        p.product_code, p.product_name
-- FROM qms.si_quality_standards qs
-- JOIN mes.si_products p ON qs.product_id = p.product_id
-- WHERE qs.tenant_id = 'DEMO001'
-- ORDER BY qs.standard_code;

-- BOM 확인
-- SELECT b.bom_code, b.bom_name, p.product_code, p.product_name
-- FROM bom.si_boms b
-- JOIN mes.si_products p ON b.product_id = p.product_id
-- WHERE b.tenant_id = 'DEMO001'
-- ORDER BY b.bom_code;

-- ============================================================
-- 완료
-- ============================================================
-- 이 스크립트를 실행한 후 WMS E2E 통합 테스트를 시작할 수 있습니다.
-- ============================================================
