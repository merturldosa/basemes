-- ============================================================================
-- Seed Data: Production Management Module
-- Author: Moon Myung-seop
-- Date: 2026-01-19
-- ============================================================================

-- ============================================================================
-- 1. Products (제품 마스터)
-- ============================================================================

INSERT INTO mes.sd_products (tenant_id, product_code, product_name, product_type, specification, unit, standard_cycle_time, description, created_by, updated_by) VALUES
('softice', 'PROD-001', 'LCD 패널 A형', '완제품', '10.1인치 1920x1080 IPS', 'EA', 300, '10.1인치 LCD 패널', 'admin', 'admin'),
('softice', 'PROD-002', 'LCD 패널 B형', '완제품', '15.6인치 1920x1080 IPS', 'EA', 420, '15.6인치 LCD 패널', 'admin', 'admin'),
('softice', 'PROD-003', 'PCB 메인보드 A', '반제품', '4층 PCB 150x100mm', 'EA', 180, 'LCD 제어용 PCB', 'admin', 'admin'),
('softice', 'PROD-004', 'PCB 메인보드 B', '반제품', '4층 PCB 200x150mm', 'EA', 240, '대형 LCD 제어용 PCB', 'admin', 'admin'),
('softice', 'PROD-005', '액정 원료 TYPE-A', '원자재', '고순도 액정 화합물', 'KG', NULL, 'LCD 액정 원료', 'admin', 'admin'),
('softice', 'PROD-006', '유리 기판 10인치', '원자재', '강화유리 10.1인치', 'EA', NULL, 'LCD 기판용 유리', 'admin', 'admin'),
('softice', 'PROD-007', '유리 기판 15인치', '원자재', '강화유리 15.6인치', 'EA', NULL, '대형 LCD 기판용 유리', 'admin', 'admin'),
('softice', 'PROD-008', '백라이트 유닛 Small', '반제품', 'LED 백라이트 10인치용', 'EA', 120, '소형 백라이트', 'admin', 'admin'),
('softice', 'PROD-009', '백라이트 유닛 Large', '반제품', 'LED 백라이트 15인치용', 'EA', 150, '대형 백라이트', 'admin', 'admin'),
('softice', 'PROD-010', '터치스크린 패널 10인치', '반제품', '정전식 터치패널', 'EA', 200, '10인치 터치 패널', 'admin', 'admin');

-- ============================================================================
-- 2. Processes (공정 마스터)
-- ============================================================================

INSERT INTO mes.sd_processes (tenant_id, process_code, process_name, process_type, sequence_order, description, created_by, updated_by) VALUES
('softice', 'PROC-001', 'PCB 제작', '제조', 1, 'PCB 기판 제작 공정', 'admin', 'admin'),
('softice', 'PROC-002', 'SMT 실장', '조립', 2, 'Surface Mount Technology - 부품 실장', 'admin', 'admin'),
('softice', 'PROC-003', 'DIP 실장', '조립', 3, 'Dual In-line Package - 부품 실장', 'admin', 'admin'),
('softice', 'PROC-004', 'PCB 검사', '검사', 4, 'PCB 외관 및 기능 검사', 'admin', 'admin'),
('softice', 'PROC-005', 'LCD 액정 주입', '제조', 5, 'LCD 패널 액정 주입', 'admin', 'admin'),
('softice', 'PROC-006', 'LCD 실링', '제조', 6, 'LCD 패널 밀봉 처리', 'admin', 'admin'),
('softice', 'PROC-007', '백라이트 조립', '조립', 7, '백라이트 유닛 조립', 'admin', 'admin'),
('softice', 'PROC-008', 'LCD 최종 조립', '조립', 8, 'LCD 패널 최종 조립', 'admin', 'admin'),
('softice', 'PROC-009', '기능 테스트', '검사', 9, 'LCD 기능 및 화질 테스트', 'admin', 'admin'),
('softice', 'PROC-010', '포장', '포장', 10, '완제품 포장', 'admin', 'admin');

-- ============================================================================
-- 3. Work Orders (작업 지시)
-- ============================================================================

INSERT INTO mes.sd_work_orders (
    tenant_id, work_order_no, product_id, process_id,
    planned_quantity, planned_start_date, planned_end_date,
    status, priority, assigned_user_id,
    created_by, updated_by
) VALUES
-- 진행 중인 작업지시
('softice', 'WO-2026-001', 1, 8, 100.000, '2026-01-19 08:00:00', '2026-01-19 17:00:00', 'IN_PROGRESS', 1, 1, 'admin', 'admin'),
('softice', 'WO-2026-002', 2, 8, 50.000, '2026-01-19 09:00:00', '2026-01-19 18:00:00', 'IN_PROGRESS', 2, 1, 'admin', 'admin'),

-- 준비 완료
('softice', 'WO-2026-003', 3, 2, 200.000, '2026-01-20 08:00:00', '2026-01-20 17:00:00', 'READY', 3, 1, 'admin', 'admin'),
('softice', 'WO-2026-004', 4, 2, 150.000, '2026-01-20 09:00:00', '2026-01-20 18:00:00', 'READY', 3, 1, 'admin', 'admin'),

-- 대기 중
('softice', 'WO-2026-005', 1, 9, 100.000, '2026-01-21 08:00:00', '2026-01-21 17:00:00', 'PENDING', 4, NULL, 'admin', 'admin'),
('softice', 'WO-2026-006', 8, 7, 120.000, '2026-01-21 09:00:00', '2026-01-21 18:00:00', 'PENDING', 5, NULL, 'admin', 'admin'),
('softice', 'WO-2026-007', 10, 8, 80.000, '2026-01-22 08:00:00', '2026-01-22 17:00:00', 'PENDING', 5, NULL, 'admin', 'admin'),

-- 완료된 작업지시
('softice', 'WO-2026-008', 1, 5, 150.000, '2026-01-18 08:00:00', '2026-01-18 17:00:00', 'COMPLETED', 1, 1, 'admin', 'admin'),
('softice', 'WO-2026-009', 2, 5, 100.000, '2026-01-18 09:00:00', '2026-01-18 18:00:00', 'COMPLETED', 2, 1, 'admin', 'admin');

-- 완료된 작업지시의 실적 업데이트
UPDATE mes.sd_work_orders
SET
    actual_quantity = 150.000,
    good_quantity = 145.000,
    defect_quantity = 5.000,
    actual_start_date = '2026-01-18 08:15:00',
    actual_end_date = '2026-01-18 16:45:00'
WHERE work_order_no = 'WO-2026-008';

UPDATE mes.sd_work_orders
SET
    actual_quantity = 100.000,
    good_quantity = 98.000,
    defect_quantity = 2.000,
    actual_start_date = '2026-01-18 09:10:00',
    actual_end_date = '2026-01-18 17:50:00'
WHERE work_order_no = 'WO-2026-009';

-- 진행 중인 작업지시의 부분 실적
UPDATE mes.sd_work_orders
SET
    actual_quantity = 45.000,
    good_quantity = 44.000,
    defect_quantity = 1.000,
    actual_start_date = '2026-01-19 08:20:00'
WHERE work_order_no = 'WO-2026-001';

UPDATE mes.sd_work_orders
SET
    actual_quantity = 20.000,
    good_quantity = 20.000,
    defect_quantity = 0.000,
    actual_start_date = '2026-01-19 09:15:00'
WHERE work_order_no = 'WO-2026-002';

-- ============================================================================
-- 4. Work Results (작업 실적)
-- ============================================================================

INSERT INTO mes.sd_work_results (
    work_order_id, tenant_id, result_date,
    quantity, good_quantity, defect_quantity,
    work_start_time, work_end_time, work_duration,
    worker_user_id, worker_name,
    defect_reason, remarks,
    created_by, updated_by
) VALUES
-- WO-2026-008의 실적 (완료)
(8, 'softice', '2026-01-18 12:00:00', 75.000, 73.000, 2.000, '2026-01-18 08:15:00', '2026-01-18 12:00:00', 225, 1, '문명섭', '액정 불량 2건', '오전 작업', 'admin', 'admin'),
(8, 'softice', '2026-01-18 16:45:00', 75.000, 72.000, 3.000, '2026-01-18 13:00:00', '2026-01-18 16:45:00', 225, 1, '문명섭', '실링 불량 3건', '오후 작업', 'admin', 'admin'),

-- WO-2026-009의 실적 (완료)
(9, 'softice', '2026-01-18 13:30:00', 50.000, 49.000, 1.000, '2026-01-18 09:10:00', '2026-01-18 13:30:00', 260, 1, '문명섭', '기포 발생 1건', '오전 작업', 'admin', 'admin'),
(9, 'softice', '2026-01-18 17:50:00', 50.000, 49.000, 1.000, '2026-01-18 14:00:00', '2026-01-18 17:50:00', 230, 1, '문명섭', '액정 누수 1건', '오후 작업', 'admin', 'admin'),

-- WO-2026-001의 실적 (진행 중)
(1, 'softice', '2026-01-19 10:30:00', 25.000, 25.000, 0.000, '2026-01-19 08:20:00', '2026-01-19 10:30:00', 130, 1, '문명섭', NULL, '1차 실적', 'admin', 'admin'),
(1, 'softice', '2026-01-19 13:00:00', 20.000, 19.000, 1.000, '2026-01-19 11:00:00', '2026-01-19 13:00:00', 120, 1, '문명섭', '조립 불량 1건', '2차 실적', 'admin', 'admin'),

-- WO-2026-002의 실적 (진행 중)
(2, 'softice', '2026-01-19 11:00:00', 20.000, 20.000, 0.000, '2026-01-19 09:15:00', '2026-01-19 11:00:00', 105, 1, '문명섭', NULL, '1차 실적', 'admin', 'admin');

-- ============================================================================
-- End of Seed Data
-- ============================================================================
