# Production Management Phase 6: Enhancement Complete

**Author**: Claude Code
**Date**: 2026-01-24
**Module**: Production Management - 생산 관리 모듈 강화
**Status**: ✅ Complete

---

## Overview

Phase 6에서는 Production Management 모듈을 강화하여 WMS 모듈과 완전히 통합했습니다. BOM 기반 자재 소요 계산, 자재 예약, 자재 출고 요청 자동 생성, 생산 실적 등록 시 완제품 자동 입고 기능을 구현했습니다.

---

## 구현 내용

### 1. WorkOrderService 강화

**파일**: `backend/src/main/java/kr/co/softice/mes/domain/service/WorkOrderService.java`

#### 추가된 메서드

##### 1.1 BOM 기반 자재 소요 계산
```java
public Map<Long, MaterialRequirement> calculateMaterialRequirements(Long workOrderId)
```

**기능**:
- Work Order의 제품에 대한 활성 BOM 조회
- BOM Detail별 필요 수량 계산
- 사용률(usage_rate)과 폐기율(scrap_rate) 적용

**계산 공식**:
```
필요 수량 = (기본 수량 × 생산 수량) × 사용률 × (1 + 폐기율)
```

**예시**:
```java
// BOM Detail 정보
- Material: RAW-001
- Base Quantity: 2.0 (per 1 product)
- Usage Rate: 1.0 (100%)
- Scrap Rate: 0.05 (5%)

// Work Order 정보
- Target Quantity: 100

// 계산
Required = (2.0 × 100) × 1.0 × (1 + 0.05)
        = 200 × 1.0 × 1.05
        = 210.000
```

**반환값**:
```java
MaterialRequirement {
    materialProduct: ProductEntity (RAW-001)
    requiredQuantity: 210.000
    unit: "KG"
    bomDetail: BomDetailEntity
}
```

##### 1.2 자재 예약
```java
@Transactional
public List<Long> reserveMaterials(Long workOrderId, Long warehouseId)
```

**워크플로우**:
```
1. Work Order 상태 검증 (PENDING만 가능)
   ↓
2. BOM 기반 자재 소요 계산
   ↓
3. 각 자재별 재고 예약 (InventoryService.reserveInventory)
   - Available Quantity → Reserved Quantity
   - FIFO 방식으로 LOT 자동 선택
   ↓
4. Work Order 상태 업데이트 (PENDING → READY)
   ↓
5. 예약된 재고 ID 목록 반환
```

**재고 예약 로직**:
```java
// InventoryService.reserveInventory 호출
inventoryService.reserveInventory(
    tenantId,
    warehouseId,
    productId,
    null,  // LOT ID (null = FIFO)
    requiredQuantity
);

// Inventory 업데이트
available_quantity -= requiredQuantity
reserved_quantity += requiredQuantity
```

**예외 처리**:
- 재고 부족 시: `INSUFFICIENT_INVENTORY` 예외 발생
- 일부 자재 예약 실패 시: 전체 롤백 (트랜잭션)

##### 1.3 Material Request 자동 생성
```java
@Transactional
public MaterialRequestEntity createMaterialRequests(
    Long workOrderId,
    Long warehouseId,
    Long requesterUserId)
```

**생성 프로세스**:
```
1. BOM 기반 자재 소요 계산
   ↓
2. Material Request 헤더 생성
   - Request No: MR-YYYYMMDD-0001
   - Status: PENDING
   - Work Order 연결
   ↓
3. Material Request Items 생성
   - 각 필요 자재별 Item 생성
   - Item Status: PENDING
   ↓
4. MaterialRequestService.createMaterialRequest 호출
   ↓
5. 생성된 Material Request 반환
```

**Material Request 구조**:
```java
MaterialRequestEntity {
    requestNo: "MR-20260124-0001"
    requestDate: "2026-01-24T10:00:00"
    workOrder: WorkOrderEntity
    workOrderNo: "WO-20260124-0001"
    requester: UserEntity
    warehouse: WarehouseEntity (source)
    requestStatus: "PENDING"
    priority: "NORMAL"
    requiredDate: LocalDate
    items: [
        {
            product: RAW-001
            requestedQuantity: 210.000
            unit: "KG"
            itemStatus: "PENDING"
        },
        ...
    ]
}
```

##### 1.4 자재 예약 해제
```java
@Transactional
public void releaseMaterials(Long workOrderId, Long warehouseId)
```

**사용 시점**: Work Order 취소 시

**워크플로우**:
```
1. BOM 기반 자재 소요 계산
   ↓
2. 각 자재별 예약 해제
   - InventoryService.releaseReservedInventory 호출
   - Reserved Quantity → Available Quantity
   ↓
3. 일부 실패해도 계속 진행 (best effort)
```

#### MaterialRequirement 내부 클래스
```java
@Data
@Builder
public static class MaterialRequirement {
    private ProductEntity materialProduct;  // 필요 자재
    private BigDecimal requiredQuantity;    // 필요 수량
    private String unit;                    // 단위
    private BomDetailEntity bomDetail;      // BOM 상세 정보
}
```

---

### 2. WorkResultService 강화

**파일**: `backend/src/main/java/kr/co/softice/mes/domain/service/WorkResultService.java`

#### 추가된 메서드

##### 2.1 생산 실적 등록 + 완제품 입고
```java
@Transactional
public WorkResultEntity recordProductionWithInventory(
    WorkResultEntity workResult,
    Long warehouseId,
    boolean createLot)
```

**통합 프로세스**:
```
1. Work Result 생성
   - Quantity, Good Quantity, Defect Quantity 기록
   - Work Duration 계산
   ↓
2. 완제품 LOT 생성 (createLot=true인 경우)
   - LOT No: FG-YYYYMMDD-WO{no}-001
   - LOT Type: PRODUCTION
   - Quality Status: PENDING (OQC 대기)
   ↓
3. 완제품 재고 업데이트
   - Finished Goods Warehouse에 입고
   - Available Quantity 증가
   - Inventory Transaction 생성 (IN_PRODUCTION)
   ↓
4. 불량품 처리
   - Scrap/Quarantine Warehouse로 이동
   - IN_SCRAP 트랜잭션 생성
   ↓
5. Work Order 집계 업데이트
   - Actual Quantity, Good Quantity, Defect Quantity 재계산
```

**파라미터**:
- `workResult`: 작업 실적 정보 (수량, 작업 시간 등)
- `warehouseId`: 완제품 창고 ID
- `createLot`: LOT 생성 여부 (true = 신규 LOT 생성)

##### 2.2 완제품 LOT 생성
```java
private LotEntity createFinishedGoodsLot(
    WorkResultEntity workResult,
    Long warehouseId)
```

**LOT 번호 생성 규칙**:
```
FG-YYYYMMDD-WO{workOrderNo}-{sequence}

예시:
- FG-20260124-WO0001-001
- FG-20260124-WO0001-002
- FG-20260125-WO0002-001
```

**LOT 정보**:
```java
LotEntity {
    lotNo: "FG-20260124-WO0001-001"
    lotType: "PRODUCTION"
    product: ProductEntity (완제품)
    warehouse: WarehouseEntity (완제품 창고)
    initialQuantity: 95.000 (Good Quantity)
    currentQuantity: 95.000
    reservedQuantity: 0.000
    qualityStatus: "PENDING"  // OQC 대기
    productionDate: "2026-01-24"
    remarks: "Produced from work order: WO-20260124-0001"
}
```

##### 2.3 완제품 재고 업데이트
```java
private void updateFinishedGoodsInventory(
    WorkResultEntity workResult,
    Long warehouseId,
    LotEntity lot)
```

**재고 업데이트 로직**:
```java
// 1. 기존 재고 찾기 또는 생성
Optional<InventoryEntity> existing = inventoryRepository
    .findByWarehouseAndProductAndLot(...);

if (existing.isPresent()) {
    // 기존 재고에 추가
    availableQuantity += goodQuantity;
} else {
    // 신규 재고 생성
    InventoryEntity.builder()
        .availableQuantity(goodQuantity)
        .reservedQuantity(0)
        .build();
}

// 2. 마지막 트랜잭션 정보 업데이트
lastTransactionDate = now;
lastTransactionType = "IN_PRODUCTION";
```

##### 2.4 생산 재고 트랜잭션 생성
```java
private void createProductionTransaction(
    WorkResultEntity workResult,
    WarehouseEntity warehouse,
    ProductEntity product,
    LotEntity lot)
```

**트랜잭션 정보**:
```java
InventoryTransactionEntity {
    transactionNo: "PROD-WO0001-001"
    transactionDate: workResult.workDate
    transactionType: "IN_PRODUCTION"
    warehouse: WarehouseEntity (완제품 창고)
    product: ProductEntity (완제품)
    lot: LotEntity (생성된 LOT)
    quantity: goodQuantity
    transactionUser: workResult.worker
    approvalStatus: "APPROVED"  // 자동 승인
    referenceNo: "WO-20260124-0001"
    remarks: "Production from work result ID: 123"
}
```

##### 2.5 불량품 처리
```java
private void handleDefectiveProducts(WorkResultEntity workResult)
```

**처리 프로세스**:
```
1. Scrap 창고 조회 (warehouse_type = 'SCRAP')
   - 없으면 Quarantine 창고 조회
   ↓
2. 불량품 재고 생성
   - Scrap/Quarantine 창고에 입고
   - Defect Quantity 만큼 추가
   ↓
3. 트랜잭션 유형: IN_SCRAP
```

**불량품 재고**:
```java
InventoryEntity {
    warehouse: ScrapWarehouse/QuarantineWarehouse
    product: ProductEntity (완제품)
    availableQuantity: defectQuantity
    reservedQuantity: 0
    lastTransactionType: "IN_SCRAP"
}
```

---

## 통합 워크플로우

### 전체 생산 프로세스

```
┌─────────────────────────────────────────────────────────────────┐
│ 1. 작업 지시 생성 (Work Order Creation)                          │
└─────────────────────────────────────────────────────────────────┘
   ↓
   - Product: FG-001 (완제품)
   - Target Quantity: 100
   - Status: PENDING

┌─────────────────────────────────────────────────────────────────┐
│ 2. BOM 기반 자재 소요 계산 (Material Requirements Calculation)   │
└─────────────────────────────────────────────────────────────────┘
   ↓
   calculateMaterialRequirements(workOrderId)

   결과:
   - RAW-001: 210 KG (base: 2.0, scrap: 5%)
   - RAW-002: 52.5 KG (base: 0.5, scrap: 5%)

┌─────────────────────────────────────────────────────────────────┐
│ 3. 자재 예약 (Material Reservation)                             │
└─────────────────────────────────────────────────────────────────┘
   ↓
   reserveMaterials(workOrderId, warehouseId)

   재고 변화:
   RAW-001: available 1000 → 790, reserved 0 → 210
   RAW-002: available 500 → 447.5, reserved 0 → 52.5

   Work Order: PENDING → READY

┌─────────────────────────────────────────────────────────────────┐
│ 4. 자재 출고 요청 생성 (Material Request Creation)               │
└─────────────────────────────────────────────────────────────────┘
   ↓
   createMaterialRequests(workOrderId, warehouseId, userId)

   생성:
   - Material Request: MR-20260124-0001
   - Status: PENDING
   - Items: 2 (RAW-001, RAW-002)

┌─────────────────────────────────────────────────────────────────┐
│ 5. 자재 출고 승인 및 처리 (Material Issue Approval & Processing) │
└─────────────────────────────────────────────────────────────────┘
   ↓
   MaterialRequestService.approveRequest()
   MaterialRequestService.issueMaterials()

   재고 변화:
   - Reserved → 0 (예약 해제)
   - OUT_ISSUE 트랜잭션 생성

   Material Request: PENDING → APPROVED → ISSUED

┌─────────────────────────────────────────────────────────────────┐
│ 6. 작업 시작 (Work Start)                                       │
└─────────────────────────────────────────────────────────────────┘
   ↓
   WorkOrderService.startWorkOrder(workOrderId)

   Work Order: READY → IN_PROGRESS

┌─────────────────────────────────────────────────────────────────┐
│ 7. 생산 실적 등록 + 완제품 입고 (Production Recording)           │
└─────────────────────────────────────────────────────────────────┘
   ↓
   recordProductionWithInventory(workResult, warehouseId, true)

   Work Result 생성:
   - Quantity: 100
   - Good Quantity: 95
   - Defect Quantity: 5

   완제품 LOT 생성:
   - LOT No: FG-20260124-WO0001-001
   - Quantity: 95
   - Quality Status: PENDING

   완제품 재고 추가:
   - Warehouse: FINISHED_GOODS
   - Available Quantity: +95
   - Transaction: IN_PRODUCTION

   불량품 처리:
   - Warehouse: SCRAP
   - Quantity: 5
   - Transaction: IN_SCRAP

┌─────────────────────────────────────────────────────────────────┐
│ 8. OQC 검사 (Outgoing Quality Control)                          │
└─────────────────────────────────────────────────────────────────┘
   ↓
   QualityInspectionService.createOQCInspection()

   검사 결과:
   - Inspected: 95
   - Passed: 90
   - Failed: 5

   LOT 상태 업데이트:
   - Quality Status: PENDING → PASSED

   재고 조정:
   - Passed (90): FINISHED_GOODS 창고 유지
   - Failed (5): QUARANTINE 창고로 이동

┌─────────────────────────────────────────────────────────────────┐
│ 9. 작업 완료 (Work Completion)                                  │
└─────────────────────────────────────────────────────────────────┘
   ↓
   WorkOrderService.completeWorkOrder(workOrderId)

   Work Order: IN_PROGRESS → COMPLETED

   최종 집계:
   - Target Quantity: 100
   - Actual Quantity: 100
   - Good Quantity: 90
   - Defect Quantity: 10
```

---

## 데이터 흐름 예시

### 시나리오: 제품 FG-001 생산 (100개)

#### 초기 상태
```sql
-- BOM 정보
bom_details:
  RAW-001: quantity=2.0, usage_rate=1.0, scrap_rate=0.05
  RAW-002: quantity=0.5, usage_rate=1.0, scrap_rate=0.05

-- 재고 현황
inventory (RAW_MATERIAL warehouse):
  RAW-001: available=1000, reserved=0
  RAW-002: available=500, reserved=0

inventory (FINISHED_GOODS warehouse):
  FG-001: available=50, reserved=0
```

#### 1단계: Work Order 생성 & 자재 소요 계산
```java
// Work Order
WO-20260124-0001:
  product: FG-001
  targetQuantity: 100
  status: PENDING

// Material Requirements
calculateMaterialRequirements(workOrderId):
  RAW-001: 210 KG = (2.0 × 100) × 1.0 × 1.05
  RAW-002: 52.5 KG = (0.5 × 100) × 1.0 × 1.05
```

#### 2단계: 자재 예약
```java
// Inventory 변화
reserveMaterials(workOrderId, rawWarehouseId):

RAW-001:
  available: 1000 → 790
  reserved: 0 → 210

RAW-002:
  available: 500 → 447.5
  reserved: 0 → 52.5

// Work Order 상태 변경
status: PENDING → READY
```

#### 3단계: Material Request 생성
```java
// Material Request
MR-20260124-0001:
  workOrder: WO-20260124-0001
  status: PENDING
  items: [
    {product: RAW-001, quantity: 210},
    {product: RAW-002, quantity: 52.5}
  ]
```

#### 4단계: 자재 출고
```java
// Material Request 승인 및 출고
approveRequest(MR-20260124-0001)
issueMaterials(MR-20260124-0001)

// Inventory 변화
RAW-001:
  available: 790 (변화 없음)
  reserved: 210 → 0

RAW-002:
  available: 447.5 (변화 없음)
  reserved: 52.5 → 0

// Inventory Transactions
OUT_ISSUE: RAW-001, -210
OUT_ISSUE: RAW-002, -52.5
```

#### 5단계: 생산 실적 등록
```java
// Work Result
recordProductionWithInventory(
  workResult: {
    quantity: 100,
    goodQuantity: 95,
    defectQuantity: 5
  },
  warehouseId: finishedGoodsWarehouseId,
  createLot: true
)

// LOT 생성
FG-20260124-WO0001-001:
  product: FG-001
  quantity: 95
  qualityStatus: PENDING

// 완제품 재고 추가
inventory (FINISHED_GOODS):
  FG-001:
    available: 50 → 145
    reserved: 0

// Inventory Transaction
IN_PRODUCTION: FG-001, +95, LOT: FG-20260124-WO0001-001

// 불량품 처리
inventory (SCRAP):
  FG-001:
    available: 5 (신규 생성)

// Inventory Transaction
IN_SCRAP: FG-001, +5
```

#### 6단계: OQC 검사 및 완료
```java
// OQC 검사
createOQCInspection(lotId: FG-20260124-WO0001-001)
  inspected: 95
  passed: 90
  failed: 5

// LOT 상태 업데이트
LOT FG-20260124-WO0001-001:
  qualityStatus: PENDING → PASSED
  currentQuantity: 95 → 90

// 재고 조정
inventory (FINISHED_GOODS):
  FG-001:
    available: 145 → 140

inventory (QUARANTINE):
  FG-001:
    available: 5 (신규)

// Work Order 완료
completeWorkOrder(workOrderId)
  status: IN_PROGRESS → COMPLETED
  actualQuantity: 100
  goodQuantity: 90
  defectQuantity: 10
```

---

## API 사용 예시

### 1. 작업 지시 생성 후 자재 처리

```java
// 1. Work Order 생성
WorkOrderEntity workOrder = WorkOrderEntity.builder()
    .tenant(tenant)
    .workOrderNo("WO-20260124-0001")
    .product(product)  // FG-001
    .targetQuantity(new BigDecimal("100"))
    .plannedStartDate(LocalDateTime.now().plusDays(1))
    .status("PENDING")
    .build();

WorkOrderEntity created = workOrderService.createWorkOrder(workOrder);

// 2. 자재 소요 계산
Map<Long, MaterialRequirement> requirements =
    workOrderService.calculateMaterialRequirements(created.getWorkOrderId());

// 결과 출력
requirements.forEach((productId, req) -> {
    System.out.println(String.format("%s: %s %s",
        req.getMaterialProduct().getProductCode(),
        req.getRequiredQuantity(),
        req.getUnit()));
});
// 출력: RAW-001: 210.000 KG
//      RAW-002: 52.5 KG

// 3. 자재 예약
List<Long> reservedIds = workOrderService.reserveMaterials(
    created.getWorkOrderId(),
    rawMaterialWarehouseId
);

System.out.println("Reserved inventories: " + reservedIds);
// 출력: Reserved inventories: [101, 102]

// 4. Material Request 생성
MaterialRequestEntity materialRequest = workOrderService.createMaterialRequests(
    created.getWorkOrderId(),
    rawMaterialWarehouseId,
    requesterUserId
);

System.out.println("Material request created: " + materialRequest.getRequestNo());
// 출력: Material request created: MR-20260124-0001
```

### 2. 생산 실적 등록 및 완제품 입고

```java
// 1. Work Order 시작
workOrderService.startWorkOrder(workOrderId);

// 2. Work Result 생성 및 완제품 입고
WorkResultEntity workResult = WorkResultEntity.builder()
    .tenant(tenant)
    .workOrder(workOrder)
    .worker(worker)
    .workDate(LocalDateTime.now())
    .workStartTime(LocalDateTime.now().minusHours(8))
    .workEndTime(LocalDateTime.now())
    .quantity(new BigDecimal("100"))
    .goodQuantity(new BigDecimal("95"))
    .defectQuantity(new BigDecimal("5"))
    .build();

WorkResultEntity recorded = workResultService.recordProductionWithInventory(
    workResult,
    finishedGoodsWarehouseId,
    true  // Create LOT
);

System.out.println("Work result recorded: " + recorded.getWorkResultId());
System.out.println("Good quantity: " + recorded.getGoodQuantity());
System.out.println("Defect quantity: " + recorded.getDefectQuantity());

// 3. 재고 확인
List<InventoryEntity> fgInventory = inventoryService.findByTenantAndWarehouse(
    tenantId,
    finishedGoodsWarehouseId
);

fgInventory.forEach(inv -> {
    System.out.println(String.format("%s: %s (LOT: %s)",
        inv.getProduct().getProductCode(),
        inv.getAvailableQuantity(),
        inv.getLot() != null ? inv.getLot().getLotNo() : "N/A"));
});
// 출력: FG-001: 145 (LOT: FG-20260124-WO0001-001)

// 4. Work Order 완료
workOrderService.completeWorkOrder(workOrderId);
```

### 3. 작업 지시 취소 시 자재 해제

```java
// Work Order 취소
workOrderService.cancelWorkOrder(workOrderId);

// 자재 예약 해제
workOrderService.releaseMaterials(workOrderId, rawMaterialWarehouseId);

// 재고 확인 (예약 해제 확인)
List<InventoryEntity> inventory = inventoryService.findByTenantAndWarehouse(
    tenantId,
    rawMaterialWarehouseId
);

inventory.forEach(inv -> {
    System.out.println(String.format("%s: available=%s, reserved=%s",
        inv.getProduct().getProductCode(),
        inv.getAvailableQuantity(),
        inv.getReservedQuantity()));
});
// 출력: RAW-001: available=1000, reserved=0
//      RAW-002: available=500, reserved=0
```

---

## 주요 특징

### 1. BOM 기반 자동화
- BOM에서 자재 소요량 자동 계산
- 사용률 및 폐기율 적용
- 복잡한 계산 오류 방지

### 2. 재고 예약 시스템
- 작업 지시 시 자재 자동 예약
- Available → Reserved 이동
- 재고 부족 사전 방지

### 3. Material Request 자동 생성
- Work Order에서 Material Request 자동 생성
- 수작업 없이 자재 출고 요청
- Work Order 추적성 확보

### 4. 완제품 자동 입고
- Work Result 등록 시 자동 입고
- LOT 자동 생성 및 추적
- Inventory Transaction 자동 기록

### 5. 불량품 자동 처리
- Scrap/Quarantine 창고로 자동 이동
- 재고 분리 관리
- 폐기 대상 명확화

### 6. 트랜잭션 추적
- 모든 재고 변동 기록
- 생산 → 입고 추적 가능
- 감사 및 품질 추적성 확보

---

## 파일 목록

### 수정된 파일
1. ✅ `WorkOrderService.java` (216 → 438 lines, +222 lines)
   - calculateMaterialRequirements()
   - reserveMaterials()
   - createMaterialRequests()
   - releaseMaterials()
   - MaterialRequirement 내부 클래스

2. ✅ `WorkResultService.java` (152 → 368 lines, +216 lines)
   - recordProductionWithInventory()
   - createFinishedGoodsLot()
   - updateFinishedGoodsInventory()
   - createProductionTransaction()
   - handleDefectiveProducts()

### 새로 생성된 파일
3. ✅ `PRODUCTION_PHASE6_ENHANCEMENT_COMPLETE.md` (이 문서)

---

## 다음 단계 (Optional)

### 1. 추가 기능
- **자재 대체**: BOM에서 대체 자재 지정 및 자동 선택
- **역산 BOM**: 완제품에서 사용된 원자재 역추적
- **Batch Production**: 여러 작업 지시 일괄 처리
- **Production Scheduling**: 작업 지시 스케줄링 최적화

### 2. 성능 최적화
- BOM 계산 캐싱
- 재고 예약 Lock 최적화
- Batch Insert for Transactions

### 3. 통합 테스트
- 전체 생산 프로세스 E2E 테스트
- 동시성 테스트 (Multiple Work Orders)
- 재고 부족 시나리오 테스트

---

## 결론

Phase 6를 통해 Production Management 모듈이 WMS와 완전히 통합되었습니다:

✅ **BOM 기반 자재 소요 계산** - 자동화된 정확한 계산
✅ **자재 예약 시스템** - 재고 부족 사전 방지
✅ **Material Request 자동 생성** - 수작업 제거
✅ **완제품 자동 입고** - LOT 생성 및 재고 업데이트
✅ **불량품 자동 처리** - Scrap/Quarantine 분리 관리
✅ **완전한 추적성** - 원자재 → 완제품 전 과정 추적

이제 SDS MES는 생산 계획부터 완제품 출하까지 **완전히 통합된 제조 실행 시스템**입니다.

---

**Next Steps**:
- Equipment Management 모듈 강화
- Sales Order 통합 (MRP 기능)
- Dashboard 고도화 (실시간 생산 모니터링)
