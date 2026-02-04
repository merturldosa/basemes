# WMS 모듈 통합 테스트 시나리오

**작성일**: 2026-01-25
**작성자**: Claude Sonnet 4.5
**버전**: 1.0

---

## 개요

WMS (Warehouse Management System) 모듈과 다른 모듈(QMS, Production, Purchase) 간의 통합을 검증하는 E2E 테스트 시나리오입니다.

## 전제 조건

### 테스트 환경
- Backend: Spring Boot 3.2.1 + Java 21
- Frontend: React 18 + TypeScript + Vite
- Database: PostgreSQL 16
- 테스트 계정: DEMO001/admin/admin123

### 기본 데이터 준비
1. 테넌트: DEMO001 (데모 회사)
2. 창고 (Warehouses):
   - WH-RAW: 원자재 창고 (RAW_MATERIAL)
   - WH-WIP: 재공품 창고 (WIP)
   - WH-FG: 완제품 창고 (FINISHED_GOODS)
   - WH-QRT: 격리 창고 (QUARANTINE)
3. 제품 (Products):
   - P-LCD-001: 32인치 LCD 패널
   - P-PCB-001: LCD 구동 PCB
4. 공급업체 (Supplier):
   - SUP-001: ABC 전자부품
5. 사용자 (Users):
   - admin (관리자)
   - manager (생산 관리자)
   - operator (작업자)

---

## 시나리오 1: 입하 → 품질 검사 → 재고 업데이트 플로우

**목적**: 구매 주문 기반 입하가 품질 검사를 거쳐 재고에 반영되는 전체 프로세스 검증

### Step 1.1: 구매 주문 생성
```http
POST /api/purchase-orders
Authorization: Bearer {jwt_token}
X-Tenant-ID: DEMO001

{
  "orderNo": "PO-20260125-001",
  "orderDate": "2026-01-25",
  "supplierId": 1,  // SUP-001: ABC 전자부품
  "items": [
    {
      "productId": 1,  // P-PCB-001: LCD 구동 PCB
      "orderedQuantity": 1000,
      "unitPrice": 5000,
      "lineAmount": 5000000
    }
  ],
  "totalAmount": 5000000,
  "deliveryDate": "2026-01-30",
  "orderStatus": "APPROVED"
}
```

**예상 결과**:
- purchaseOrderId: 1
- orderStatus: APPROVED
- totalAmount: 5,000,000원

---

### Step 1.2: 입하 생성 (품질 검사 필요)
```http
POST /api/goods-receipts
Authorization: Bearer {jwt_token}
X-Tenant-ID: DEMO001

{
  "receiptNo": "GR-20260125-0001",
  "receiptDate": "2026-01-25T10:00:00",
  "receiptType": "PURCHASE",
  "purchaseOrderId": 1,
  "supplierId": 1,
  "warehouseId": 1,  // WH-RAW: 원자재 창고
  "receiverId": 1,  // admin
  "items": [
    {
      "productId": 1,  // P-PCB-001
      "receivedQuantity": 1000,
      "unitPrice": 5000,
      "lineAmount": 5000000,
      "lotNo": "LOT-20260125-001",
      "expiryDate": "2027-01-25",
      "inspectionStatus": "PENDING"  // 품질 검사 필요
    }
  ]
}
```

**예상 결과**:
- goodsReceiptId: 1
- receiptStatus: INSPECTING (품질 검사 대기 중)
- totalQuantity: 1000
- totalAmount: 5,000,000원

**자동 생성 데이터**:
1. **LOT 레코드**:
   - lotNo: LOT-20260125-001
   - qualityStatus: PENDING (검사 대기)
   - initialQuantity: 1000
   - currentQuantity: 1000

2. **재고 트랜잭션**:
   - transactionNo: IN-GR-20260125-0001-001
   - transactionType: IN_RECEIVE
   - quantity: 1000
   - approvalStatus: PENDING (검사 완료 전)

3. **품질 검사 요청 (IQC)**:
   - inspectionNo: IQC-20260125-0001
   - inspectionType: INCOMING
   - inspectionResult: CONDITIONAL (검사 대기)
   - inspectedQuantity: 1000

---

### Step 1.3: 품질 기준 확인
```http
GET /api/quality-standards/product/1
Authorization: Bearer {jwt_token}
X-Tenant-ID: DEMO001
```

**예상 결과**:
- standardCode: QS-PCB-IQC-001
- inspectionType: INCOMING
- minValue: 0.95 (95% 이상 합격)
- maxValue: 1.00
- targetValue: 0.99

---

### Step 1.4: 품질 검사 실행
```http
PUT /api/quality-inspections/1
Authorization: Bearer {jwt_token}
X-Tenant-ID: DEMO001

{
  "inspectedQuantity": 1000,
  "passedQuantity": 950,
  "failedQuantity": 50,
  "measuredValue": 0.95,  // 95% 합격률
  "inspectionResult": "PASS",  // 자동 판정: PASS (95% >= minValue)
  "inspectorId": 1,
  "inspectionDate": "2026-01-25T14:00:00",
  "remarks": "입고 검사 완료. 불량률 5%로 기준 이내."
}
```

**예상 결과**:
- inspectionResult: PASS (자동 판정)
- passedQuantity: 950
- failedQuantity: 50

**자동 업데이트**:
- GoodsReceiptItem.inspectionStatus: PENDING → PASS
- LOT quality_status: PENDING → PASSED

---

### Step 1.5: 입하 완료
```http
POST /api/goods-receipts/1/complete
Authorization: Bearer {jwt_token}
X-Tenant-ID: DEMO001

{
  "completedByUserId": 1
}
```

**예상 결과**:
- receiptStatus: PENDING → COMPLETED
- LOT quality_status: PASSED

**자동 재고 업데이트**:
1. **합격품 (950개) - 원자재 창고 (WH-RAW)**:
   - InventoryEntity 생성/업데이트
   - warehouse: WH-RAW
   - product: P-PCB-001
   - lot: LOT-20260125-001
   - availableQuantity: 950 (가용 재고)
   - reservedQuantity: 0

2. **불합격품 (50개) - 격리 창고 (WH-QRT)**:
   - InventoryEntity 생성/업데이트
   - warehouse: WH-QRT
   - product: P-PCB-001
   - lot: LOT-20260125-001
   - availableQuantity: 50
   - qualityStatus: FAILED

---

### Step 1.6: 재고 확인
```http
GET /api/inventory?tenantId=DEMO001
Authorization: Bearer {jwt_token}
X-Tenant-ID: DEMO001
```

**예상 결과**:
```json
[
  {
    "warehouse": "WH-RAW",
    "product": "P-PCB-001",
    "lotNo": "LOT-20260125-001",
    "availableQuantity": 950,
    "reservedQuantity": 0,
    "totalQuantity": 950,
    "qualityStatus": "PASSED"
  },
  {
    "warehouse": "WH-QRT",
    "product": "P-PCB-001",
    "lotNo": "LOT-20260125-001",
    "availableQuantity": 50,
    "reservedQuantity": 0,
    "totalQuantity": 50,
    "qualityStatus": "FAILED"
  }
]
```

---

## 시나리오 2: 재고 예약 → 생산 → 완제품 입고 플로우

**목적**: 생산 작업 지시 시 원자재 재고를 예약하고, 생산 완료 후 완제품을 재고에 추가하는 프로세스 검증

### Step 2.1: 작업 지시 생성
```http
POST /api/work-orders
Authorization: Bearer {jwt_token}
X-Tenant-ID: DEMO001

{
  "workOrderNo": "WO-2026-004",
  "productId": 2,  // P-LCD-001: 32인치 LCD 패널
  "processId": 3,  // PROC-003: 패널 조립
  "plannedQuantity": 100,
  "workOrderStatus": "PENDING",
  "plannedStartDate": "2026-01-26T08:00:00",
  "plannedEndDate": "2026-01-26T17:00:00"
}
```

**예상 결과**:
- workOrderId: 4
- workOrderNo: WO-2026-004
- workOrderStatus: PENDING
- plannedQuantity: 100

---

### Step 2.2: BOM 조회 및 자재 소요량 계산
```http
GET /api/boms/product/2
Authorization: Bearer {jwt_token}
X-Tenant-ID: DEMO001
```

**예상 BOM 구성**:
- P-LCD-001 (32인치 LCD 패널) = 1개당
  - P-PCB-001 (LCD 구동 PCB) × 2개

**자재 소요량 계산**:
- 생산 수량: 100개
- P-PCB-001 필요 수량: 100 × 2 = 200개

---

### Step 2.3: 원자재 재고 예약
```http
POST /api/inventory/reserve
Authorization: Bearer {jwt_token}
X-Tenant-ID: DEMO001

{
  "warehouseId": 1,  // WH-RAW: 원자재 창고
  "productId": 1,  // P-PCB-001
  "lotId": 1,  // LOT-20260125-001
  "quantity": 200,
  "workOrderId": 4,
  "reservedBy": 1
}
```

**예상 결과**:
- Inventory 업데이트:
  - availableQuantity: 950 → 750
  - reservedQuantity: 0 → 200
  - totalQuantity: 950 (변화 없음)

---

### Step 2.4: 작업 지시 시작
```http
POST /api/work-orders/4/start
Authorization: Bearer {jwt_token}
X-Tenant-ID: DEMO001
```

**예상 결과**:
- workOrderStatus: PENDING → IN_PROGRESS
- actualStartDate: 2026-01-26T08:00:00

---

### Step 2.5: 자재 출고 (예약 재고 → 실제 출고)
```http
POST /api/inventory-transactions
Authorization: Bearer {jwt_token}
X-Tenant-ID: DEMO001

{
  "transactionNo": "OUT-WO-2026-004-001",
  "transactionDate": "2026-01-26T08:00:00",
  "transactionType": "OUT_ISSUE",
  "warehouseId": 1,  // WH-RAW
  "productId": 1,  // P-PCB-001
  "lotId": 1,
  "quantity": 200,
  "referenceNo": "WO-2026-004",
  "approvalStatus": "APPROVED"
}
```

**예상 결과**:
- Inventory 업데이트:
  - reservedQuantity: 200 → 0 (예약 해제)
  - availableQuantity: 750 (변화 없음)
  - totalQuantity: 950 → 750

---

### Step 2.6: 작업 실적 등록
```http
POST /api/work-results
Authorization: Bearer {jwt_token}
X-Tenant-ID: DEMO001

{
  "workOrderId": 4,
  "resultDate": "2026-01-26T17:00:00",
  "quantity": 100,
  "goodQuantity": 98,
  "defectQuantity": 2,
  "workerId": 14,  // operator
  "workStartTime": "2026-01-26T08:00:00",
  "workEndTime": "2026-01-26T17:00:00"
}
```

**예상 결과**:
- workResultId: 생성됨
- WorkOrder 자동 집계:
  - actualQuantity: 100
  - goodQuantity: 98
  - defectQuantity: 2

---

### Step 2.7: 작업 지시 완료
```http
POST /api/work-orders/4/complete
Authorization: Bearer {jwt_token}
X-Tenant-ID: DEMO001
```

**예상 결과**:
- workOrderStatus: IN_PROGRESS → COMPLETED
- actualEndDate: 2026-01-26T17:00:00

---

### Step 2.8: 완제품 입고
```http
POST /api/goods-receipts
Authorization: Bearer {jwt_token}
X-Tenant-ID: DEMO001

{
  "receiptNo": "GR-20260126-0001",
  "receiptDate": "2026-01-26T17:30:00",
  "receiptType": "PRODUCTION",
  "workOrderId": 4,
  "warehouseId": 3,  // WH-FG: 완제품 창고
  "receiverId": 1,
  "items": [
    {
      "productId": 2,  // P-LCD-001: 32인치 LCD 패널
      "receivedQuantity": 98,  // 양품 수량
      "lotNo": "LOT-20260126-FG-001",
      "inspectionStatus": "NOT_REQUIRED"  // 최종 검사 완료 상태
    }
  ]
}
```

**예상 결과**:
- goodsReceiptId: 2
- receiptStatus: COMPLETED (검사 불요)
- Inventory 업데이트 (WH-FG):
  - product: P-LCD-001
  - lot: LOT-20260126-FG-001
  - availableQuantity: 98

**불량품 (2개) 처리**:
```http
POST /api/goods-receipts
{
  "receiptType": "PRODUCTION_DEFECT",
  "warehouseId": 4,  // WH-QRT: 격리 창고
  "items": [{
    "productId": 2,
    "receivedQuantity": 2,
    "lotNo": "LOT-20260126-FG-001-DEF",
    "inspectionStatus": "FAIL"
  }]
}
```

---

### Step 2.9: 재고 현황 확인
```http
GET /api/inventory?tenantId=DEMO001
```

**예상 재고 상태**:

| 창고 | 제품 | LOT | 가용 | 예약 | 합계 | 상태 |
|------|------|-----|------|------|------|------|
| WH-RAW | P-PCB-001 | LOT-20260125-001 | 750 | 0 | 750 | PASSED |
| WH-FG | P-LCD-001 | LOT-20260126-FG-001 | 98 | 0 | 98 | PASSED |
| WH-QRT | P-LCD-001 | LOT-20260126-FG-001-DEF | 2 | 0 | 2 | FAILED |

---

## 시나리오 3: 출하 → 재고 차감 → 판매 완료 플로우

**목적**: 판매 주문 기반 출하 시 FIFO 로직으로 LOT 선택 및 재고 차감 검증

### Step 3.1: 판매 주문 생성
```http
POST /api/sales-orders
Authorization: Bearer {jwt_token}
X-Tenant-ID: DEMO001

{
  "orderNo": "SO-20260127-001",
  "orderDate": "2026-01-27",
  "customerId": 1,  // CUST-001
  "items": [
    {
      "productId": 2,  // P-LCD-001
      "orderedQuantity": 50,
      "unitPrice": 300000,
      "lineAmount": 15000000
    }
  ],
  "totalAmount": 15000000,
  "deliveryDate": "2026-01-30",
  "orderStatus": "APPROVED"
}
```

**예상 결과**:
- salesOrderId: 1
- orderStatus: APPROVED

---

### Step 3.2: LOT 선택 (FIFO)
```http
POST /api/lot-selection/fifo
Authorization: Bearer {jwt_token}
X-Tenant-ID: DEMO001

{
  "warehouseId": 3,  // WH-FG
  "productId": 2,  // P-LCD-001
  "requiredQuantity": 50
}
```

**예상 결과**:
```json
{
  "allocations": [
    {
      "lot": "LOT-20260126-FG-001",
      "allocatedQuantity": 50,
      "availableQuantity": 98
    }
  ],
  "totalAllocated": 50,
  "fullyAllocated": true
}
```

---

### Step 3.3: 출하 검사 (OQC) 생성
```http
POST /api/quality-inspections
Authorization: Bearer {jwt_token}
X-Tenant-ID: DEMO001

{
  "qualityStandardId": 2,  // QS-LCD-OQC-001 (출하 검사 기준)
  "productId": 2,
  "inspectionNo": "OQC-20260127-0001",
  "inspectionDate": "2026-01-27T09:00:00",
  "inspectionType": "OUTGOING",
  "inspectorId": 1,
  "inspectedQuantity": 50,
  "measuredValue": 0.98,
  "remarks": "출하 전 최종 검사"
}
```

**예상 결과**:
- inspectionResult: PASS (자동 판정)
- passedQuantity: 50
- failedQuantity: 0

---

### Step 3.4: 출하 생성
```http
POST /api/shippings
Authorization: Bearer {jwt_token}
X-Tenant-ID: DEMO001

{
  "shippingNo": "SH-20260127-0001",
  "shippingDate": "2026-01-27T15:00:00",
  "shippingType": "SALES",
  "salesOrderId": 1,
  "customerId": 1,
  "warehouseId": 3,  // WH-FG
  "shipperId": 1,
  "items": [
    {
      "productId": 2,
      "shippedQuantity": 50,
      "lotId": 1,  // LOT-20260126-FG-001
      "qualityInspectionId": 2  // OQC-20260127-0001
    }
  ]
}
```

**예상 결과**:
- shippingId: 1
- shippingStatus: PENDING

---

### Step 3.5: 출하 완료
```http
POST /api/shippings/1/complete
Authorization: Bearer {jwt_token}
X-Tenant-ID: DEMO001
```

**예상 결과**:
- shippingStatus: PENDING → SHIPPED

**자동 재고 차감**:
- InventoryTransaction 생성:
  - transactionType: OUT_ISSUE
  - quantity: 50
  - referenceNo: SH-20260127-0001

- Inventory 업데이트 (WH-FG):
  - product: P-LCD-001
  - lot: LOT-20260126-FG-001
  - availableQuantity: 98 → 48

**판매 주문 업데이트**:
- SalesOrderItem.shippedQuantity: 0 → 50
- SalesOrder.orderStatus: APPROVED → SHIPPED

---

### Step 3.6: 최종 재고 확인
```http
GET /api/inventory/warehouse/3
Authorization: Bearer {jwt_token}
X-Tenant-ID: DEMO001
```

**예상 결과**:
```json
[
  {
    "warehouse": "WH-FG",
    "product": "P-LCD-001",
    "lotNo": "LOT-20260126-FG-001",
    "availableQuantity": 48,
    "reservedQuantity": 0,
    "totalQuantity": 48
  }
]
```

---

## 시나리오 4: 재고 조정 및 실사

**목적**: 재고 실사 후 차이 조정 프로세스 검증

### Step 4.1: 재고 실사 시작
```http
POST /api/physical-inventories
Authorization: Bearer {jwt_token}
X-Tenant-ID: DEMO001

{
  "countNo": "PI-20260128-0001",
  "countDate": "2026-01-28",
  "warehouseId": 3,  // WH-FG
  "countType": "FULL",
  "countStatus": "IN_PROGRESS",
  "counterId": 1
}
```

---

### Step 4.2: 실사 항목 등록
```http
POST /api/physical-inventory-items
Authorization: Bearer {jwt_token}
X-Tenant-ID: DEMO001

{
  "physicalInventoryId": 1,
  "productId": 2,
  "lotId": 1,
  "systemQuantity": 48,  // 시스템 재고
  "countedQuantity": 45,  // 실사 수량
  "differenceQuantity": -3,  // 차이 (부족)
  "remarks": "재고 실사 중 3개 부족 확인"
}
```

---

### Step 4.3: 실사 완료 및 조정
```http
POST /api/physical-inventories/1/complete
Authorization: Bearer {jwt_token}
X-Tenant-ID: DEMO001
```

**예상 결과**:
- physicalInventoryStatus: IN_PROGRESS → COMPLETED

**자동 재고 조정**:
- InventoryTransaction 생성:
  - transactionType: ADJUST
  - quantity: -3
  - referenceNo: PI-20260128-0001

- Inventory 업데이트:
  - availableQuantity: 48 → 45

---

## 시나리오 5: 예외 상황 처리

### Case 5.1: 재고 부족 시 예약 실패
```http
POST /api/inventory/reserve
{
  "warehouseId": 3,
  "productId": 2,
  "quantity": 1000  // 가용 재고(45) 초과
}
```

**예상 결과**:
- HTTP 400 Bad Request
- Error: "Insufficient inventory: available=45, requested=1000"

---

### Case 5.2: 품질 검사 불합격 시 입하 취소
```http
PUT /api/quality-inspections/1
{
  "inspectionResult": "FAIL",
  "failedQuantity": 1000,
  "remarks": "전량 불량으로 반품 필요"
}
```

```http
POST /api/goods-receipts/1/cancel
{
  "cancelReason": "품질 검사 불합격으로 인한 반품"
}
```

**예상 결과**:
- receiptStatus: COMPLETED → CANCELLED
- LOT.isActive: true → false
- 재고 역처리 (보정 트랜잭션)

---

### Case 5.3: 유효기간 만료 LOT 출고 방지
```http
GET /api/lots/expired
Authorization: Bearer {jwt_token}
X-Tenant-ID: DEMO001
```

**예상 결과**: 유효기간 만료 LOT 목록

---

## 검증 포인트

### 1. 재고 일관성
- ✅ 가용 재고 + 예약 재고 = 총 재고
- ✅ 모든 재고 이동 시 트랜잭션 생성
- ✅ 트랜잭션 합계 = 실제 재고

### 2. LOT 추적성
- ✅ 입하부터 출하까지 전체 이력 추적
- ✅ 품질 상태 변경 이력
- ✅ 유효기간 관리

### 3. 품질 검사 연동
- ✅ 입하 시 IQC 자동 생성
- ✅ 출하 시 OQC 자동 생성
- ✅ 검사 결과에 따른 자동 처리 (합격/불합격 분리)

### 4. 생산 연동
- ✅ 작업 지시 시 자재 자동 예약
- ✅ BOM 기반 소요량 계산
- ✅ 생산 완료 시 완제품 자동 입고

### 5. 판매 연동
- ✅ FIFO 로직 LOT 자동 선택
- ✅ 출하 완료 시 판매 주문 업데이트
- ✅ 재고 자동 차감

### 6. 권한 제어
- ✅ Multi-tenant 격리 (tenant_id 필터)
- ✅ 역할 기반 권한 (ADMIN, WAREHOUSE_MANAGER, INVENTORY_CLERK)

---

## 성능 테스트

### 대량 데이터 처리
1. **입하 1000건 동시 처리**
   - 목표: 5분 이내 완료
   - 검증: 재고 일관성 유지

2. **재고 조회 성능**
   - 10,000개 재고 항목
   - 목표: 1초 이내 응답
   - 인덱스: (tenant_id, warehouse_id, product_id)

3. **LOT 선택 성능 (FIFO)**
   - 1,000개 LOT 중 최적 선택
   - 목표: 0.5초 이내

---

## 자동화 테스트 코드 (선택 사항)

### JUnit 테스트 예시
```java
@SpringBootTest
@Transactional
class WMSIntegrationTest {

    @Test
    void testGoodsReceiptToInventoryFlow() {
        // Given: 구매 주문 생성
        PurchaseOrder po = createPurchaseOrder();

        // When: 입하 생성
        GoodsReceipt gr = createGoodsReceipt(po);

        // Then: LOT 자동 생성 확인
        assertNotNull(gr.getItems().get(0).getLot());
        assertEquals("PENDING", gr.getReceiptStatus());

        // When: 품질 검사 통과
        QualityInspection qi = completeInspection(gr, "PASS");

        // When: 입하 완료
        gr = goodsReceiptService.completeGoodsReceipt(gr.getId());

        // Then: 재고 업데이트 확인
        Inventory inventory = inventoryRepository.findByWarehouseAndProduct(
            warehouse.getId(), product.getId());
        assertEquals(950, inventory.getAvailableQuantity());
    }
}
```

---

## 실행 체크리스트

### 사전 준비
- [ ] 백엔드 서버 실행 (port 8080)
- [ ] 프론트엔드 서버 실행 (port 3001)
- [ ] 데이터베이스 마이그레이션 완료
- [ ] 테스트 데이터 준비 (창고, 제품, 사용자)

### 시나리오 1 실행
- [ ] 구매 주문 생성
- [ ] 입하 생성 (품질 검사 필요)
- [ ] 품질 검사 실행
- [ ] 입하 완료
- [ ] 재고 확인 (합격품/불합격품 분리)

### 시나리오 2 실행
- [ ] 작업 지시 생성
- [ ] 재고 예약
- [ ] 작업 시작
- [ ] 자재 출고
- [ ] 작업 완료
- [ ] 완제품 입고

### 시나리오 3 실행
- [ ] 판매 주문 생성
- [ ] LOT 선택 (FIFO)
- [ ] 출하 검사
- [ ] 출하 완료
- [ ] 재고 차감 확인

### 시나리오 4 실행
- [ ] 재고 실사 시작
- [ ] 실사 항목 등록
- [ ] 실사 완료 및 조정

### 시나리오 5 실행
- [ ] 재고 부족 예외 처리
- [ ] 품질 불합격 예외 처리
- [ ] 유효기간 만료 LOT 확인

---

## 문제 해결 가이드

### 문제 1: LazyInitializationException
**증상**: "could not initialize proxy - no Session"
**원인**: JOIN FETCH 쿼리 미사용
**해결**: Repository에 `findByIdWithAllRelations()` 사용

### 문제 2: 재고 수량 불일치
**증상**: available + reserved ≠ total
**원인**: 트랜잭션 롤백 또는 중복 처리
**해결**: 트랜잭션 로그 확인, 재고 재계산

### 문제 3: Multi-tenant 격리 실패
**증상**: 다른 tenant 데이터 조회됨
**원인**: tenant_id 필터 누락
**해결**: 모든 쿼리에 tenant_id WHERE 절 추가

---

**작성자**: Claude Sonnet 4.5
**문서 버전**: 1.0
**다음 업데이트**: 실제 테스트 결과 반영
