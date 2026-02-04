# WMS 모듈 통합 테스트 가이드

## 📋 목차
1. [사전 준비](#사전-준비)
2. [API 엔드포인트 목록](#api-엔드포인트-목록)
3. [Test #1: QMS 통합 - 품질 검사 포함 입하](#test-1-qms-통합---품질-검사-포함-입하)
4. [Test #2: Production 통합 - 재고 예약 및 출고](#test-2-production-통합---재고-예약-및-출고)
5. [Test #3: Shipping 통합 - 판매 주문 이행](#test-3-shipping-통합---판매-주문-이행)
6. [예상 문제 및 해결책](#예상-문제-및-해결책)

---

## 사전 준비

### 1. Backend 실행
```bash
cd D:\prj\softice\prj\claude\SoIceMES\backend
mvn spring-boot:run
```
- 포트: `8080`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`

### 2. Frontend 실행
```bash
cd D:\prj\softice\prj\claude\SoIceMES\frontend
npm install  # 최초 1회
npm run dev
```
- 포트: `5173`
- URL: `http://localhost:5173`

### 3. 데이터베이스 확인
- PostgreSQL 실행 중인지 확인
- 스키마 존재 확인:
  - `inventory` (warehouses, lots, inventory, inventory_transactions)
  - `wms` (goods_receipts, goods_receipt_items)
  - `qms` (quality_inspections, quality_standards)
  - `purchase` (purchase_orders, purchase_order_items)

### 4. 초기 데이터 준비
```sql
-- 창고 생성 (5가지 타입)
INSERT INTO inventory.si_warehouses (tenant_id, warehouse_code, warehouse_name, warehouse_type, is_active, created_at, updated_at)
VALUES
  ('TENANT001', 'WH-RAW', '원자재 창고', 'RAW_MATERIAL', true, NOW(), NOW()),
  ('TENANT001', 'WH-WIP', '재공 창고', 'WORK_IN_PROCESS', true, NOW(), NOW()),
  ('TENANT001', 'WH-FG', '완제품 창고', 'FINISHED_GOODS', true, NOW(), NOW()),
  ('TENANT001', 'WH-QUA', '격리 창고', 'QUARANTINE', true, NOW(), NOW()),
  ('TENANT001', 'WH-SCRAP', '불량 창고', 'SCRAP', true, NOW(), NOW());

-- 제품 생성
INSERT INTO mes.si_products (tenant_id, product_code, product_name, product_type, unit, is_active, created_at, updated_at)
VALUES
  ('TENANT001', 'RAW-001', '원자재-001', 'RAW_MATERIAL', 'KG', true, NOW(), NOW()),
  ('TENANT001', 'FG-001', '완제품-001', 'FINISHED_GOODS', 'EA', true, NOW(), NOW());

-- 공급업체 생성
INSERT INTO customer.si_suppliers (tenant_id, supplier_code, supplier_name, is_active, created_at, updated_at)
VALUES ('TENANT001', 'SUP-001', '테스트공급업체', true, NOW(), NOW());
```

---

## API 엔드포인트 목록

### 창고 관리 (Warehouse)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/warehouses` | 창고 목록 조회 |
| GET | `/api/warehouses/{id}` | 창고 상세 조회 |
| GET | `/api/warehouses/type/{type}` | 타입별 창고 조회 |
| POST | `/api/warehouses` | 창고 생성 |
| PUT | `/api/warehouses/{id}` | 창고 수정 |
| DELETE | `/api/warehouses/{id}` | 창고 비활성화 |
| PATCH | `/api/warehouses/{id}/toggle-active` | 활성/비활성 토글 |

### 재고 관리 (Inventory)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/inventory` | 재고 현황 조회 |
| GET | `/api/inventory/{id}` | 재고 상세 조회 |
| GET | `/api/inventory/warehouse/{warehouseId}` | 창고별 재고 |
| GET | `/api/inventory/product/{productId}` | 제품별 재고 |
| GET | `/api/inventory/low-stock?threshold=100` | 저재고 알림 |
| POST | `/api/inventory/reserve` | 재고 예약 |
| POST | `/api/inventory/release` | 예약 해제 |

### 입하 관리 (Goods Receipt)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/goods-receipts` | 입하 목록 조회 |
| GET | `/api/goods-receipts/{id}` | 입하 상세 조회 |
| GET | `/api/goods-receipts/date-range?startDate=...&endDate=...` | 날짜 범위별 조회 |
| POST | `/api/goods-receipts` | 입하 생성 |
| PUT | `/api/goods-receipts/{id}` | 입하 수정 |
| POST | `/api/goods-receipts/{id}/complete` | 입하 완료 |
| POST | `/api/goods-receipts/{id}/cancel?reason=...` | 입하 취소 |

### 재고 트랜잭션 (Inventory Transaction)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/inventory-transactions` | 트랜잭션 목록 |
| GET | `/api/inventory-transactions/{id}` | 트랜잭션 상세 |
| GET | `/api/inventory-transactions/approval-status/{status}` | 승인 상태별 조회 |
| POST | `/api/inventory-transactions` | 트랜잭션 생성 |
| POST | `/api/inventory-transactions/{id}/approve?approverId=...` | 승인 |
| POST | `/api/inventory-transactions/{id}/reject?approverId=...&reason=...` | 거부 |

---

## Test #1: QMS 통합 - 품질 검사 포함 입하

### 시나리오
1. 구매 주문 생성 (PO-001, RAW-001, 수량: 1000)
2. 입하 생성 (GR-001)
3. 품질 검사 실행 (합격: 950, 불합격: 50)
4. 자동 업데이트 확인

### 1단계: 구매 주문 생성
```bash
POST /api/purchase-orders
Content-Type: application/json

{
  "orderNo": "PO-001",
  "orderDate": "2026-01-24T10:00:00",
  "supplierId": 1,
  "status": "APPROVED",
  "items": [
    {
      "lineNo": 1,
      "materialId": 1,
      "orderQuantity": 1000,
      "unitPrice": 10.50,
      "unit": "KG"
    }
  ]
}
```

### 2단계: 입하 생성
```bash
POST /api/goods-receipts
Content-Type: application/json

{
  "receiptDate": "2026-01-24T14:00:00",
  "purchaseOrderId": 1,
  "warehouseId": 1,  // WH-RAW (원자재 창고)
  "receiptType": "PURCHASE",
  "items": [
    {
      "productId": 1,  // RAW-001
      "receivedQuantity": 1000,
      "lotNo": "LOT-20260124-001",
      "expiryDate": "2027-01-24",
      "inspectionStatus": "PENDING"
    }
  ]
}
```

**예상 결과**:
- 입하 상태: `PENDING` → `INSPECTING`
- LOT 생성: `quality_status=PENDING`
- 재고 트랜잭션: `approval_status=PENDING`

### 3단계: 품질 검사 생성
```bash
POST /api/quality-inspections
Content-Type: application/json

{
  "inspectionNo": "QI-001",
  "inspectionType": "INCOMING",
  "inspectionDate": "2026-01-24T15:00:00",
  "productId": 1,
  "qualityStandardId": 1,
  "inspectorUserId": 1,
  "sampleQuantity": 1000,
  "passQuantity": 950,
  "failQuantity": 50,
  "defectQuantity": 50,
  "inspectionResult": "PASS",
  "remarks": "입하 검사 - 불합격품 50개 격리"
}
```

### 4단계: 입하 완료
```bash
POST /api/goods-receipts/1/complete
```

**예상 결과**:
- LOT quality_status: `PENDING` → `PASSED`
- 입하 상태: `INSPECTING` → `COMPLETED`
- 재고 (원자재 창고): `available_quantity = 950`
- 재고 (격리 창고): `available_quantity = 50`

### 검증 SQL
```sql
-- LOT 상태 확인
SELECT lot_no, quality_status, current_quantity, initial_quantity
FROM inventory.si_lots
WHERE lot_no = 'LOT-20260124-001';

-- 재고 확인
SELECT w.warehouse_code, w.warehouse_name, i.available_quantity, i.reserved_quantity
FROM inventory.si_inventory i
JOIN inventory.si_warehouses w ON i.warehouse_id = w.warehouse_id
WHERE i.product_id = 1;

-- 재고 트랜잭션 확인
SELECT transaction_no, transaction_type, quantity, approval_status
FROM inventory.si_inventory_transactions
WHERE product_id = 1
ORDER BY transaction_date DESC;
```

---

## Test #2: Production 통합 - 재고 예약 및 출고

### 시나리오
1. 작업 지시 생성 (WO-001, FG-001, 수량: 100)
2. BOM 기반 원자재 예약 (RAW-001 x 200)
3. 자재 출고
4. 생산 완료 후 완제품 입고

### 1단계: 재고 예약
```bash
POST /api/inventory/reserve
Content-Type: application/json

{
  "productId": 1,  // RAW-001
  "warehouseId": 1,  // WH-RAW
  "lotId": 1,  // LOT-20260124-001
  "quantity": 200,
  "workOrderId": 1,
  "remarks": "작업지시 WO-001 자재 예약"
}
```

**예상 결과**:
- Before: `available=950, reserved=0`
- After: `available=750, reserved=200`

### 2단계: 자재 출고 (재고 트랜잭션)
```bash
POST /api/inventory-transactions
Content-Type: application/json

{
  "transactionNo": "OUT-WO-001-001",
  "transactionDate": "2026-01-24T16:00:00",
  "transactionType": "OUT_ISSUE",
  "warehouseId": 1,
  "productId": 1,
  "lotId": 1,
  "quantity": 200,
  "transactionUserId": 1,
  "workOrderId": 1,
  "referenceNo": "WO-001",
  "remarks": "작업지시 자재 출고"
}
```

**예상 결과**:
- `available=750, reserved=200` → `available=750, reserved=0`
- (Reserved가 먼저 차감됨)

### 3단계: 검증
```sql
SELECT available_quantity, reserved_quantity
FROM inventory.si_inventory
WHERE product_id = 1 AND warehouse_id = 1;
-- 예상: available=750, reserved=0
```

---

## Test #3: Shipping 통합 - 판매 주문 이행

### 시나리오
1. 판매 주문 생성 (SO-001, FG-001, 수량: 50)
2. 출하 생성 (FIFO LOT 선택)
3. 출하 전 품질 검사
4. 출하 완료

### 1단계: 재고 확인 (FIFO)
```bash
GET /api/inventory/product/2  // FG-001
```

### 2단계: 출하 생성
```bash
POST /api/shippings
Content-Type: application/json

{
  "shippingNo": "SH-001",
  "shippingDate": "2026-01-24T17:00:00",
  "salesOrderId": 1,
  "warehouseId": 3,  // WH-FG (완제품 창고)
  "shippingType": "DIRECT",
  "status": "PENDING",
  "items": [
    {
      "productId": 2,  // FG-001
      "shippingQuantity": 50,
      "lotId": 1,  // FIFO
      "unit": "EA"
    }
  ]
}
```

### 3단계: 출하 완료
```bash
POST /api/shippings/1/complete
```

**예상 결과**:
- 재고: `available_quantity -= 50`
- 판매 주문: `shipped_quantity += 50`
- 출하 상태: `PENDING` → `SHIPPED`

---

## 예상 문제 및 해결책

### 문제 1: LOT 자동 생성 실패
**원인**: `product_id`와 `material_id` 불일치
**해결**: GoodsReceiptService에서 ProductEntity 사용 확인

### 문제 2: 재고 업데이트 안 됨
**원인**: 승인 워크플로우 PENDING 상태
**해결**:
```bash
POST /api/inventory-transactions/{id}/approve?approverId=1
```

### 문제 3: 격리 창고 이동 안 됨
**원인**: QUARANTINE 타입 창고 없음
**해결**: 초기 데이터에 격리 창고 추가 필수

### 문제 4: CORS 오류
**원인**: Frontend → Backend CORS 설정
**해결**: `application.yml`에 CORS 허용
```yaml
spring:
  web:
    cors:
      allowed-origins: http://localhost:5173
```

### 문제 5: Tenant Context 오류
**원인**: `X-Tenant-ID` 헤더 누락
**해결**: Frontend `api.ts`에서 자동 추가 확인
```typescript
config.headers['X-Tenant-ID'] = localStorage.getItem('tenantId');
```

---

## 성공 기준

### ✅ API 응답
- 모든 엔드포인트: `200/201` 상태 코드
- Response body: `{ "success": true, "message": "...", "data": {...} }`

### ✅ 재고 정확성
- `available_quantity + reserved_quantity = total`
- 음수 재고 없음
- LOT 추적 가능

### ✅ 상태 전이
- 입하: `PENDING` → `INSPECTING` → `COMPLETED`
- 품질: `PENDING` → `PASSED/FAILED`
- 승인: `PENDING` → `APPROVED/REJECTED`

### ✅ 통합 검증
- QMS: 품질 검사 결과가 재고에 반영
- Production: 예약/출고가 정상 작동
- Shipping: FIFO 로직 정상 작동

---

## 추가 테스트 케이스

### Low Stock Alert
```bash
GET /api/inventory/low-stock?threshold=100
```

### 재고 예약 해제
```bash
POST /api/inventory/release
{
  "productId": 1,
  "warehouseId": 1,
  "lotId": 1,
  "quantity": 50
}
```

### 입하 취소
```bash
POST /api/goods-receipts/1/cancel?reason=잘못된입하
```

---

**테스트 완료 시 체크리스트**:
- [ ] 모든 API 엔드포인트 정상 응답
- [ ] 재고 잔액 계산 정확
- [ ] LOT 추적 가능
- [ ] 품질 검사 연동 정상
- [ ] 승인 워크플로우 정상
- [ ] Frontend UI 정상 동작
- [ ] Multi-tenant 격리 확인
