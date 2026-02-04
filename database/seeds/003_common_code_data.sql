-- Common Code Seed Data
-- 공통 코드 초기 데이터
-- Author: Moon Myung-seop
-- Date: 2026-01-25

-- ============================================================
-- System Code Groups
-- ============================================================

-- Note: Replace 'DEFAULT_TENANT' with actual tenant_id

INSERT INTO common.common_code_groups (tenant_id, code_group, code_group_name, description, is_system, display_order) VALUES
('DEFAULT_TENANT', 'ORDER_STATUS', '주문 상태', '판매/구매 주문 상태 코드', TRUE, 1),
('DEFAULT_TENANT', 'PRODUCT_TYPE', '제품 유형', '제품 분류 코드', TRUE, 2),
('DEFAULT_TENANT', 'WAREHOUSE_TYPE', '창고 유형', '창고 분류 코드', TRUE, 3),
('DEFAULT_TENANT', 'QUALITY_STATUS', '품질 상태', 'LOT/제품 품질 상태 코드', TRUE, 4),
('DEFAULT_TENANT', 'INSPECTION_TYPE', '검사 유형', '품질 검사 유형 코드', TRUE, 5),
('DEFAULT_TENANT', 'TRANSACTION_TYPE', '재고 트랜잭션 유형', '재고 이동 유형 코드', TRUE, 6),
('DEFAULT_TENANT', 'APPROVAL_STATUS', '승인 상태', '승인 워크플로우 상태 코드', TRUE, 7),
('DEFAULT_TENANT', 'PRIORITY', '우선순위', '작업/요청 우선순위 코드', TRUE, 8),
('DEFAULT_TENANT', 'UNIT', '단위', '수량 단위 코드', TRUE, 9),
('DEFAULT_TENANT', 'GENDER', '성별', '직원 성별 코드', TRUE, 10),
('DEFAULT_TENANT', 'EMPLOYMENT_TYPE', '고용 형태', '직원 고용 형태 코드', TRUE, 11),
('DEFAULT_TENANT', 'SHIFT', '근무 시간대', '근무 시프트 코드', TRUE, 12),
('DEFAULT_TENANT', 'DEFECT_TYPE', '불량 유형', '불량 분류 코드', TRUE, 13),
('DEFAULT_TENANT', 'CLAIM_TYPE', '클레임 유형', '클레임 분류 코드', TRUE, 14),
('DEFAULT_TENANT', 'EQUIPMENT_STATUS', '설비 상태', '설비 상태 코드', TRUE, 15),
('DEFAULT_TENANT', 'WORK_ORDER_STATUS', '작업지시 상태', '작업지시 상태 코드', TRUE, 16),
('DEFAULT_TENANT', 'SHIPPING_STATUS', '출하 상태', '출하 상태 코드', TRUE, 17),
('DEFAULT_TENANT', 'MATERIAL_TYPE', '자재 유형', '자재 분류 코드', TRUE, 18);

-- ============================================================
-- Code Details: ORDER_STATUS
-- ============================================================
INSERT INTO common.common_code_details (code_group_id, code, code_name, display_order, color_code) VALUES
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'ORDER_STATUS' AND tenant_id = 'DEFAULT_TENANT'), 'DRAFT', '임시저장', 1, '#9E9E9E'),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'ORDER_STATUS' AND tenant_id = 'DEFAULT_TENANT'), 'CONFIRMED', '확정', 2, '#2196F3'),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'ORDER_STATUS' AND tenant_id = 'DEFAULT_TENANT'), 'IN_PROGRESS', '진행중', 3, '#FF9800'),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'ORDER_STATUS' AND tenant_id = 'DEFAULT_TENANT'), 'PARTIALLY_DELIVERED', '부분 완료', 4, '#FFC107'),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'ORDER_STATUS' AND tenant_id = 'DEFAULT_TENANT'), 'DELIVERED', '완료', 5, '#4CAF50'),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'ORDER_STATUS' AND tenant_id = 'DEFAULT_TENANT'), 'COMPLETED', '종료', 6, '#00BCD4'),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'ORDER_STATUS' AND tenant_id = 'DEFAULT_TENANT'), 'CANCELLED', '취소', 7, '#F44336');

-- ============================================================
-- Code Details: PRODUCT_TYPE
-- ============================================================
INSERT INTO common.common_code_details (code_group_id, code, code_name, display_order) VALUES
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'PRODUCT_TYPE' AND tenant_id = 'DEFAULT_TENANT'), 'RAW_MATERIAL', '원자재', 1),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'PRODUCT_TYPE' AND tenant_id = 'DEFAULT_TENANT'), 'SEMI_FINISHED', '반제품', 2),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'PRODUCT_TYPE' AND tenant_id = 'DEFAULT_TENANT'), 'FINISHED_GOOD', '완제품', 3),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'PRODUCT_TYPE' AND tenant_id = 'DEFAULT_TENANT'), 'CONSUMABLE', '소모품', 4),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'PRODUCT_TYPE' AND tenant_id = 'DEFAULT_TENANT'), 'SPARE_PART', '예비품', 5);

-- ============================================================
-- Code Details: WAREHOUSE_TYPE
-- ============================================================
INSERT INTO common.common_code_details (code_group_id, code, code_name, display_order, color_code) VALUES
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'WAREHOUSE_TYPE' AND tenant_id = 'DEFAULT_TENANT'), 'RAW_MATERIAL', '원자재 창고', 1, '#795548'),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'WAREHOUSE_TYPE' AND tenant_id = 'DEFAULT_TENANT'), 'WIP', '재공품 창고', 2, '#FF9800'),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'WAREHOUSE_TYPE' AND tenant_id = 'DEFAULT_TENANT'), 'FINISHED_GOOD', '완제품 창고', 3, '#4CAF50'),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'WAREHOUSE_TYPE' AND tenant_id = 'DEFAULT_TENANT'), 'QUARANTINE', '격리 창고', 4, '#FF5722'),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'WAREHOUSE_TYPE' AND tenant_id = 'DEFAULT_TENANT'), 'SCRAP', '스크랩 창고', 5, '#9E9E9E');

-- ============================================================
-- Code Details: QUALITY_STATUS
-- ============================================================
INSERT INTO common.common_code_details (code_group_id, code, code_name, display_order, color_code) VALUES
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'QUALITY_STATUS' AND tenant_id = 'DEFAULT_TENANT'), 'PENDING', '검사대기', 1, '#FFC107'),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'QUALITY_STATUS' AND tenant_id = 'DEFAULT_TENANT'), 'PASSED', '합격', 2, '#4CAF50'),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'QUALITY_STATUS' AND tenant_id = 'DEFAULT_TENANT'), 'FAILED', '불합격', 3, '#F44336'),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'QUALITY_STATUS' AND tenant_id = 'DEFAULT_TENANT'), 'CONDITIONAL', '조건부', 4, '#FF9800');

-- ============================================================
-- Code Details: INSPECTION_TYPE
-- ============================================================
INSERT INTO common.common_code_details (code_group_id, code, code_name, display_order, description) VALUES
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'INSPECTION_TYPE' AND tenant_id = 'DEFAULT_TENANT'), 'IQC', '수입 검사', 1, 'Incoming Quality Control'),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'INSPECTION_TYPE' AND tenant_id = 'DEFAULT_TENANT'), 'PQC', '공정 검사', 2, 'Process Quality Control'),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'INSPECTION_TYPE' AND tenant_id = 'DEFAULT_TENANT'), 'OQC', '출하 검사', 3, 'Outgoing Quality Control'),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'INSPECTION_TYPE' AND tenant_id = 'DEFAULT_TENANT'), 'FQC', '최종 검사', 4, 'Final Quality Control');

-- ============================================================
-- Code Details: TRANSACTION_TYPE
-- ============================================================
INSERT INTO common.common_code_details (code_group_id, code, code_name, display_order, value1) VALUES
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'TRANSACTION_TYPE' AND tenant_id = 'DEFAULT_TENANT'), 'IN_RECEIVE', '입하', 1, 'IN'),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'TRANSACTION_TYPE' AND tenant_id = 'DEFAULT_TENANT'), 'IN_PRODUCTION', '생산입고', 2, 'IN'),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'TRANSACTION_TYPE' AND tenant_id = 'DEFAULT_TENANT'), 'IN_RETURN', '반품입고', 3, 'IN'),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'TRANSACTION_TYPE' AND tenant_id = 'DEFAULT_TENANT'), 'IN_TRANSFER', '이동입고', 4, 'IN'),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'TRANSACTION_TYPE' AND tenant_id = 'DEFAULT_TENANT'), 'OUT_ISSUE', '출고', 5, 'OUT'),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'TRANSACTION_TYPE' AND tenant_id = 'DEFAULT_TENANT'), 'OUT_SHIPPING', '출하', 6, 'OUT'),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'TRANSACTION_TYPE' AND tenant_id = 'DEFAULT_TENANT'), 'OUT_SCRAP', '스크랩출고', 7, 'OUT'),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'TRANSACTION_TYPE' AND tenant_id = 'DEFAULT_TENANT'), 'OUT_TRANSFER', '이동출고', 8, 'OUT'),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'TRANSACTION_TYPE' AND tenant_id = 'DEFAULT_TENANT'), 'ADJUSTMENT', '재고조정', 9, 'ADJUST');

-- ============================================================
-- Code Details: APPROVAL_STATUS
-- ============================================================
INSERT INTO common.common_code_details (code_group_id, code, code_name, display_order, color_code) VALUES
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'APPROVAL_STATUS' AND tenant_id = 'DEFAULT_TENANT'), 'PENDING', '대기', 1, '#FFC107'),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'APPROVAL_STATUS' AND tenant_id = 'DEFAULT_TENANT'), 'APPROVED', '승인', 2, '#4CAF50'),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'APPROVAL_STATUS' AND tenant_id = 'DEFAULT_TENANT'), 'REJECTED', '반려', 3, '#F44336'),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'APPROVAL_STATUS' AND tenant_id = 'DEFAULT_TENANT'), 'CANCELLED', '취소', 4, '#9E9E9E');

-- ============================================================
-- Code Details: PRIORITY
-- ============================================================
INSERT INTO common.common_code_details (code_group_id, code, code_name, display_order, color_code, value1) VALUES
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'PRIORITY' AND tenant_id = 'DEFAULT_TENANT'), 'LOW', '낮음', 1, '#4CAF50', '1'),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'PRIORITY' AND tenant_id = 'DEFAULT_TENANT'), 'NORMAL', '보통', 2, '#2196F3', '2'),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'PRIORITY' AND tenant_id = 'DEFAULT_TENANT'), 'HIGH', '높음', 3, '#FF9800', '3'),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'PRIORITY' AND tenant_id = 'DEFAULT_TENANT'), 'URGENT', '긴급', 4, '#F44336', '4');

-- ============================================================
-- Code Details: UNIT
-- ============================================================
INSERT INTO common.common_code_details (code_group_id, code, code_name, display_order) VALUES
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'UNIT' AND tenant_id = 'DEFAULT_TENANT'), 'EA', '개', 1),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'UNIT' AND tenant_id = 'DEFAULT_TENANT'), 'KG', '킬로그램', 2),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'UNIT' AND tenant_id = 'DEFAULT_TENANT'), 'G', '그램', 3),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'UNIT' AND tenant_id = 'DEFAULT_TENANT'), 'M', '미터', 4),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'UNIT' AND tenant_id = 'DEFAULT_TENANT'), 'L', '리터', 5),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'UNIT' AND tenant_id = 'DEFAULT_TENANT'), 'BOX', '박스', 6),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'UNIT' AND tenant_id = 'DEFAULT_TENANT'), 'SET', '세트', 7),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'UNIT' AND tenant_id = 'DEFAULT_TENANT'), 'ROLL', '롤', 8),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'UNIT' AND tenant_id = 'DEFAULT_TENANT'), 'PALLET', '파렛트', 9);

-- ============================================================
-- Code Details: GENDER
-- ============================================================
INSERT INTO common.common_code_details (code_group_id, code, code_name, display_order) VALUES
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'GENDER' AND tenant_id = 'DEFAULT_TENANT'), 'MALE', '남성', 1),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'GENDER' AND tenant_id = 'DEFAULT_TENANT'), 'FEMALE', '여성', 2),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'GENDER' AND tenant_id = 'DEFAULT_TENANT'), 'OTHER', '기타', 3);

-- ============================================================
-- Code Details: EMPLOYMENT_TYPE
-- ============================================================
INSERT INTO common.common_code_details (code_group_id, code, code_name, display_order) VALUES
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'EMPLOYMENT_TYPE' AND tenant_id = 'DEFAULT_TENANT'), 'FULL_TIME', '정규직', 1),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'EMPLOYMENT_TYPE' AND tenant_id = 'DEFAULT_TENANT'), 'PART_TIME', '계약직', 2),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'EMPLOYMENT_TYPE' AND tenant_id = 'DEFAULT_TENANT'), 'CONTRACT', '파견직', 3),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'EMPLOYMENT_TYPE' AND tenant_id = 'DEFAULT_TENANT'), 'INTERN', '인턴', 4);

-- ============================================================
-- Code Details: SHIFT
-- ============================================================
INSERT INTO common.common_code_details (code_group_id, code, code_name, display_order, value1, value2) VALUES
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'SHIFT' AND tenant_id = 'DEFAULT_TENANT'), 'DAY', '주간', 1, '08:00', '17:00'),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'SHIFT' AND tenant_id = 'DEFAULT_TENANT'), 'EVENING', '오후', 2, '16:00', '24:00'),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'SHIFT' AND tenant_id = 'DEFAULT_TENANT'), 'NIGHT', '야간', 3, '22:00', '06:00');

-- ============================================================
-- Code Details: DEFECT_TYPE
-- ============================================================
INSERT INTO common.common_code_details (code_group_id, code, code_name, display_order) VALUES
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'DEFECT_TYPE' AND tenant_id = 'DEFAULT_TENANT'), 'SCRATCH', '스크래치', 1),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'DEFECT_TYPE' AND tenant_id = 'DEFAULT_TENANT'), 'CRACK', '균열', 2),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'DEFECT_TYPE' AND tenant_id = 'DEFAULT_TENANT'), 'DIMENSION', '치수불량', 3),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'DEFECT_TYPE' AND tenant_id = 'DEFAULT_TENANT'), 'COLOR', '색상불량', 4),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'DEFECT_TYPE' AND tenant_id = 'DEFAULT_TENANT'), 'CONTAMINATION', '오염', 5),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'DEFECT_TYPE' AND tenant_id = 'DEFAULT_TENANT'), 'DEFORMATION', '변형', 6),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'DEFECT_TYPE' AND tenant_id = 'DEFAULT_TENANT'), 'BURR', '버', 7),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'DEFECT_TYPE' AND tenant_id = 'DEFAULT_TENANT'), 'SHORT_SHOT', '미성형', 8),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'DEFECT_TYPE' AND tenant_id = 'DEFAULT_TENANT'), 'OTHER', '기타', 99);

-- ============================================================
-- Code Details: CLAIM_TYPE
-- ============================================================
INSERT INTO common.common_code_details (code_group_id, code, code_name, display_order) VALUES
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'CLAIM_TYPE' AND tenant_id = 'DEFAULT_TENANT'), 'QUALITY', '품질 클레임', 1),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'CLAIM_TYPE' AND tenant_id = 'DEFAULT_TENANT'), 'DELIVERY', '납기 클레임', 2),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'CLAIM_TYPE' AND tenant_id = 'DEFAULT_TENANT'), 'QUANTITY', '수량 클레임', 3),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'CLAIM_TYPE' AND tenant_id = 'DEFAULT_TENANT'), 'DOCUMENTATION', '서류 클레임', 4),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'CLAIM_TYPE' AND tenant_id = 'DEFAULT_TENANT'), 'PACKAGING', '포장 클레임', 5),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'CLAIM_TYPE' AND tenant_id = 'DEFAULT_TENANT'), 'OTHER', '기타', 99);

-- ============================================================
-- Code Details: EQUIPMENT_STATUS
-- ============================================================
INSERT INTO common.common_code_details (code_group_id, code, code_name, display_order, color_code) VALUES
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'EQUIPMENT_STATUS' AND tenant_id = 'DEFAULT_TENANT'), 'AVAILABLE', '사용가능', 1, '#4CAF50'),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'EQUIPMENT_STATUS' AND tenant_id = 'DEFAULT_TENANT'), 'IN_USE', '사용중', 2, '#2196F3'),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'EQUIPMENT_STATUS' AND tenant_id = 'DEFAULT_TENANT'), 'UNDER_MAINTENANCE', '보전중', 3, '#FF9800'),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'EQUIPMENT_STATUS' AND tenant_id = 'DEFAULT_TENANT'), 'DOWN', '고장', 4, '#F44336'),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'EQUIPMENT_STATUS' AND tenant_id = 'DEFAULT_TENANT'), 'IDLE', '대기', 5, '#9E9E9E');

-- ============================================================
-- Code Details: WORK_ORDER_STATUS
-- ============================================================
INSERT INTO common.common_code_details (code_group_id, code, code_name, display_order, color_code) VALUES
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'WORK_ORDER_STATUS' AND tenant_id = 'DEFAULT_TENANT'), 'PENDING', '대기', 1, '#9E9E9E'),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'WORK_ORDER_STATUS' AND tenant_id = 'DEFAULT_TENANT'), 'READY', '준비완료', 2, '#2196F3'),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'WORK_ORDER_STATUS' AND tenant_id = 'DEFAULT_TENANT'), 'IN_PROGRESS', '작업중', 3, '#FF9800'),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'WORK_ORDER_STATUS' AND tenant_id = 'DEFAULT_TENANT'), 'COMPLETED', '완료', 4, '#4CAF50'),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'WORK_ORDER_STATUS' AND tenant_id = 'DEFAULT_TENANT'), 'CANCELLED', '취소', 5, '#F44336');

-- ============================================================
-- Code Details: SHIPPING_STATUS
-- ============================================================
INSERT INTO common.common_code_details (code_group_id, code, code_name, display_order, color_code) VALUES
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'SHIPPING_STATUS' AND tenant_id = 'DEFAULT_TENANT'), 'PENDING', '대기', 1, '#9E9E9E'),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'SHIPPING_STATUS' AND tenant_id = 'DEFAULT_TENANT'), 'PROCESSING', '처리중', 2, '#2196F3'),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'SHIPPING_STATUS' AND tenant_id = 'DEFAULT_TENANT'), 'INSPECTING', '검사중', 3, '#FF9800'),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'SHIPPING_STATUS' AND tenant_id = 'DEFAULT_TENANT'), 'SHIPPED', '출하완료', 4, '#4CAF50'),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'SHIPPING_STATUS' AND tenant_id = 'DEFAULT_TENANT'), 'CANCELLED', '취소', 5, '#F44336');

-- ============================================================
-- Code Details: MATERIAL_TYPE
-- ============================================================
INSERT INTO common.common_code_details (code_group_id, code, code_name, display_order) VALUES
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'MATERIAL_TYPE' AND tenant_id = 'DEFAULT_TENANT'), 'PLASTIC', '플라스틱', 1),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'MATERIAL_TYPE' AND tenant_id = 'DEFAULT_TENANT'), 'METAL', '금속', 2),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'MATERIAL_TYPE' AND tenant_id = 'DEFAULT_TENANT'), 'CHEMICAL', '화학약품', 3),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'MATERIAL_TYPE' AND tenant_id = 'DEFAULT_TENANT'), 'ELECTRONIC', '전자부품', 4),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'MATERIAL_TYPE' AND tenant_id = 'DEFAULT_TENANT'), 'PACKAGING', '포장재', 5),
((SELECT code_group_id FROM common.common_code_groups WHERE code_group = 'MATERIAL_TYPE' AND tenant_id = 'DEFAULT_TENANT'), 'OTHER', '기타', 99);
