# WMS Phase 3: 반품 관리 (Returns Management) - 완료 보고서

**작성일**: 2026-01-24
**작성자**: Moon Myung-seop
**완료 상태**: ✅ 100% 완료

---

## 📋 개요

### 목표
생산/창고에서 불량품, 과잉 반품 처리 시스템 구축

### 핵심 기능
1. **반품 신청** (Return Request): 생산 담당자가 자재 반품 신청
2. **반품 승인/거부**: 창고 관리자의 승인 워크플로우
3. **반품 입고**: 반품 물품 입고 및 품질 검사 요청
4. **재고 복원**: 합격품 재입고, 불합격품 격리

### 반품 유형
- **DEFECTIVE**: 불량품 반품
- **EXCESS**: 과잉 반품 (필요 수량 초과)
- **WRONG_DELIVERY**: 오배송 반품
- **OTHER**: 기타

### 워크플로우
```
생산 담당자: 반품 신청 (PENDING)
    ↓
창고 관리자: 승인 (APPROVED) / 거부 (REJECTED)
    ↓
창고 담당자: 반품 입고 (RECEIVED)
    ├─ 재고 트랜잭션 생성 (IN_RETURN)
    └─ 불량품인 경우: 품질 검사 요청 생성 (INSPECTING)
    ↓
품질 검사 완료 (PASS/FAIL)
    ↓
시스템: 재고 복원 (COMPLETED)
    ├─ 합격품: 원래 창고 재입고 (새 LOT 생성)
    └─ 불합격품: 격리 창고 이동 (새 LOT 생성)
```

---

## 🗄️ 데이터베이스 스키마

### 마이그레이션 파일
- **V019__create_returns_schema.sql**

### 테이블 (2개)

#### 1. wms.si_returns (반품 헤더)
```sql
CREATE TABLE wms.si_returns (
    return_id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    return_no VARCHAR(50) NOT NULL,              -- RT-YYYYMMDD-0001
    return_date TIMESTAMP NOT NULL,
    return_type VARCHAR(30) NOT NULL,            -- DEFECTIVE, EXCESS, WRONG_DELIVERY, OTHER
    material_request_id BIGINT,                  -- 원본 불출 신청 (Optional)
    work_order_id BIGINT,                        -- 관련 작업 지시 (Optional)
    requester_user_id BIGINT NOT NULL,           -- 신청자
    warehouse_id BIGINT NOT NULL,                -- 반품 입고 창고
    return_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
        -- PENDING, APPROVED, REJECTED, RECEIVED, INSPECTING, COMPLETED, CANCELLED
    approver_user_id BIGINT,
    approved_date TIMESTAMP,
    received_date TIMESTAMP,
    completed_date TIMESTAMP,
    total_return_quantity NUMERIC(15,3),
    total_received_quantity NUMERIC(15,3),
    total_passed_quantity NUMERIC(15,3),
    total_failed_quantity NUMERIC(15,3),
    remarks TEXT,
    rejection_reason TEXT,
    cancellation_reason TEXT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    CONSTRAINT uk_return_no UNIQUE (tenant_id, return_no)
);
```

**인덱스**:
- tenant_id
- return_date
- material_request_id
- work_order_id
- requester_user_id
- warehouse_id
- return_status
- return_type

#### 2. wms.si_return_items (반품 항목)
```sql
CREATE TABLE wms.si_return_items (
    return_item_id BIGSERIAL PRIMARY KEY,
    return_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_code VARCHAR(50),
    product_name VARCHAR(200),
    original_lot_no VARCHAR(100),                -- 원래 불출된 LOT
    new_lot_no VARCHAR(100),                     -- 재입고 시 생성된 새 LOT
    return_quantity NUMERIC(15,3) NOT NULL,
    received_quantity NUMERIC(15,3),
    passed_quantity NUMERIC(15,3),
    failed_quantity NUMERIC(15,3),
    inspection_status VARCHAR(30) DEFAULT 'NOT_REQUIRED',
        -- NOT_REQUIRED, PENDING, PASS, FAIL
    quality_inspection_id BIGINT,
    receive_transaction_id BIGINT,
    pass_transaction_id BIGINT,
    fail_transaction_id BIGINT,
    return_reason TEXT,
    remarks TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (return_id) REFERENCES wms.si_returns(return_id)
);
```

**인덱스**:
- return_id
- product_id
- inspection_status
- quality_inspection_id
- original_lot_no

---

## 🔧 백엔드 구현

### 엔티티 (Entities)

1. **ReturnEntity.java**
   - 반품 헤더
   - 상태 전이: PENDING → APPROVED → RECEIVED → INSPECTING → COMPLETED
   - @OneToMany items
   - Helper method: calculateTotals()

2. **ReturnItemEntity.java**
   - 반품 항목 상세
   - 반품/입고/합격/불합격 수량 추적
   - 원래 LOT + 새 LOT 관리
   - 재고 트랜잭션 참조 (입고, 합격, 불합격)

### 레포지토리 (Repositories)

1. **ReturnRepository.java**
   - `findByTenantIdWithAllRelations()`: 모든 관계 JOIN FETCH
   - `findByIdWithAllRelations()`: 상세 조회 (항목 포함)
   - `findByTenantIdAndStatusWithRelations()`: 상태별 조회
   - `findByTenantIdAndTypeWithRelations()`: 유형별 조회
   - `findPendingReturnsByWarehouse()`: 창고별 대기 반품
   - `findReturnsRequiringInspection()`: 검사 필요 반품

### 서비스 (Services)

1. **ReturnService.java** (700+ 라인)

**핵심 메서드**:

```java
// 반품 신청 생성
public ReturnEntity createReturn(ReturnEntity returnEntity) {
    // 1. 반품번호 자동 생성 (RT-YYYYMMDD-0001)
    // 2. 상태: PENDING
    // 3. 항목별 검사 상태 설정
    //    - DEFECTIVE: 검사 필요 (PENDING)
    //    - EXCESS/WRONG_DELIVERY/OTHER: 검사 불필요 (NOT_REQUIRED)
    // 4. 합계 계산
    // 5. 저장
}

// 반품 승인
@Transactional
public ReturnEntity approveReturn(Long returnId, Long approverId) {
    // 1. 상태 검증 (PENDING만 승인 가능)
    // 2. 승인 정보 업데이트
    // 3. 상태 → APPROVED
}

// 반품 거부
@Transactional
public ReturnEntity rejectReturn(Long returnId, Long approverId, String reason) {
    // 1. 상태 검증 (PENDING만 거부 가능)
    // 2. 거부 사유 저장
    // 3. 상태 → REJECTED
}

// 반품 입고 (핵심 로직)
@Transactional
public ReturnEntity receiveReturn(Long returnId, Long receiverUserId) {
    // 1. 상태 검증 (APPROVED만 입고 가능)
    // 2. 항목별 처리:
    //    a. received_quantity 설정 (전체 반품 수량)
    //    b. 재고 트랜잭션 생성 (IN_RETURN)
    //    c. 검사 필요 시: 품질 검사 요청 생성
    // 3. 상태 → RECEIVED (검사 필요 시 INSPECTING)
    // 4. receivedDate 업데이트
}

// 반품 완료 (재고 복원)
@Transactional
public ReturnEntity completeReturn(Long returnId) {
    // 1. 상태 검증 (RECEIVED/INSPECTING만 완료 가능)
    // 2. 항목별 재고 복원:
    //    a. 검사 결과 확인 (PASS/FAIL/NOT_REQUIRED)
    //    b. PASS 또는 NOT_REQUIRED:
    //       - 새 LOT 생성 (quality_status=PASSED)
    //       - 재고 트랜잭션 생성 (IN_RETURN_RESTORE)
    //       - 원래 창고 재입고
    //    c. FAIL:
    //       - 새 LOT 생성 (quality_status=FAILED)
    //       - 재고 트랜잭션 생성 (IN_QUARANTINE)
    //       - 격리 창고 이동
    // 3. 상태 → COMPLETED
    // 4. completedDate 업데이트
}

// 반품 취소
@Transactional
public ReturnEntity cancelReturn(Long returnId, String reason) {
    // 1. 상태 검증 (PENDING/APPROVED만 취소 가능)
    // 2. 취소 사유 저장
    // 3. 상태 → CANCELLED
}

// Private Helper Methods:
- createReturnReceiveTransaction(): 입고 트랜잭션 생성
- createReturnInspection(): 품질 검사 요청 생성
- restoreInventory(): 합격품 재입고 (새 LOT + 재고 업데이트)
- moveToQuarantine(): 불합격품 격리 (새 LOT + 격리창고 업데이트)
- updateInventoryBalance(): 재고 잔액 업데이트
```

### DTOs

1. **ReturnCreateRequest.java**
   ```java
   - returnNo (Optional)
   - returnDate
   - returnType (DEFECTIVE, EXCESS, WRONG_DELIVERY, OTHER)
   - materialRequestId (Optional)
   - workOrderId (Optional)
   - requesterUserId
   - warehouseId
   - items: List<ReturnItemRequest>
   - remarks
   ```

2. **ReturnItemRequest.java**
   ```java
   - productId
   - returnQuantity
   - originalLotNo (Optional)
   - returnReason
   - remarks
   ```

3. **ReturnResponse.java**
   ```java
   - Header: returnNo, returnDate, returnType, returnStatus
   - References: materialRequestNo, workOrderNo
   - Requester: requesterUserId, requesterName
   - Warehouse: warehouseId, warehouseCode, warehouseName
   - Approver: approverUserId, approverName, approvedDate
   - Dates: receivedDate, completedDate
   - Totals: totalReturnQuantity, totalReceivedQuantity, totalPassedQuantity, totalFailedQuantity
   - Items: List<ReturnItemResponse>
   - Remarks, rejectionReason, cancellationReason
   ```

4. **ReturnItemResponse.java**
   ```java
   - Product: productId, productCode, productName, productType, unit
   - LOT: originalLotNo, newLotNo
   - Quantities: returnQuantity, receivedQuantity, passedQuantity, failedQuantity
   - Inspection: inspectionStatus, qualityInspectionId
   - Transactions: receiveTransactionId, passTransactionId, failTransactionId
   - ReturnReason
   ```

### 컨트롤러 (Controllers)

1. **ReturnController.java**

**API 엔드포인트**:

```java
GET    /api/returns                                // 목록 조회
GET    /api/returns?status={status}                // 상태별 조회
GET    /api/returns?type={type}                    // 유형별 조회
GET    /api/returns?warehouseId={id}               // 창고별 조회
GET    /api/returns?materialRequestId={id}         // 불출 신청별 조회
GET    /api/returns?workOrderId={id}               // 작업 지시별 조회
GET    /api/returns/{id}                           // 상세 조회
GET    /api/returns/warehouse/{id}/pending         // 창고별 대기 반품
GET    /api/returns/requiring-inspection           // 검사 필요 반품

POST   /api/returns                                // 반품 생성
POST   /api/returns/{id}/approve                   // 승인
POST   /api/returns/{id}/reject                    // 거부
POST   /api/returns/{id}/receive                   // 입고
POST   /api/returns/{id}/complete                  // 완료 (재고 복원)
POST   /api/returns/{id}/cancel                    // 취소
```

**권한**:
- 읽기: 모든 인증 사용자
- 생성: PRODUCTION_MANAGER, PRODUCTION_WORKER, WAREHOUSE_MANAGER
- 승인/거부: WAREHOUSE_MANAGER
- 입고: WAREHOUSE_MANAGER, INVENTORY_CLERK
- 완료: WAREHOUSE_MANAGER
- 취소: WAREHOUSE_MANAGER, PRODUCTION_MANAGER

### 에러 코드

```java
RETURN_NOT_FOUND(HttpStatus.NOT_FOUND, "RT18400", "반품을 찾을 수 없습니다.")
RETURN_ALREADY_EXISTS(HttpStatus.CONFLICT, "RT18401", "이미 존재하는 반품입니다.")
```

---

## 🎨 프론트엔드 구현

### 페이지

1. **ReturnsPage.tsx** (550+ 라인)

**기능**:
- DataGrid 목록 (반품)
- 상태 칩 (대기, 승인, 거부, 입고, 검사중, 완료, 취소)
- 유형 칩 (불량품, 과잉, 오배송, 기타)
- 수량 표시 (반품/입고/합격/불합격)
- 통계 (전체, 대기, 승인, 입고, 검사중, 완료, 불량품)
- 액션:
  - 상세 보기
  - 승인 (PENDING → APPROVED)
  - 거부 다이얼로그 (PENDING → REJECTED)
  - 입고 (APPROVED → RECEIVED/INSPECTING)
  - 완료 (RECEIVED/INSPECTING → COMPLETED)
  - 취소 다이얼로그 (PENDING/APPROVED → CANCELLED)
- 새로고침 버튼
- 신규 반품 버튼

**컬럼**:
- 반품번호
- 반품일시
- 상태
- 유형
- 불출신청
- 작업지시
- 신청자
- 창고
- 수량 (반품/입고/합격/불합격)
- 승인자
- 작업 (액션 메뉴)

**상태별 색상**:
- PENDING: warning (노란색)
- APPROVED: info (파란색)
- REJECTED: error (빨간색)
- RECEIVED: primary (보라색)
- INSPECTING: info (파란색)
- COMPLETED: success (초록색)
- CANCELLED: default (회색)

**유형별 색상**:
- DEFECTIVE: error (빨간색)
- EXCESS: warning (노란색)
- WRONG_DELIVERY: info (파란색)
- OTHER: default (회색)

### 라우트 추가

**App.tsx**:
```tsx
import ReturnsPage from './pages/warehouse/ReturnsPage';

// Route:
<Route path="warehouse/returns" element={<ReturnsPage />} />
```

---

## 📊 데이터 흐름

### 1. 반품 신청 생성 (생산 담당자 - 불량품)

```
POST /api/returns
{
  "returnDate": "2026-01-24T16:00:00",
  "returnType": "DEFECTIVE",
  "materialRequestId": 1,
  "requesterUserId": 1,
  "warehouseId": 1,
  "items": [
    {
      "productId": 101,
      "returnQuantity": 50,
      "originalLotNo": "LOT-20260120-001",
      "returnReason": "불량 발견 - 표면 결함"
    }
  ]
}

→ 시스템:
  - returnNo 자동 생성: RT-20260124-0001
  - returnStatus: PENDING
  - items[].inspectionStatus: PENDING (불량품이므로 검사 필요)
```

### 2. 반품 승인 (창고 관리자)

```
POST /api/returns/1/approve?approverUserId=2

→ 시스템:
  1. approverUserId: 2
  2. approvedDate: 2026-01-24T16:05:00
  3. returnStatus: APPROVED
```

### 3. 반품 입고 (창고 담당자)

```
POST /api/returns/1/receive?receiverUserId=3

→ 시스템:
  1. 항목별 처리:
     - receivedQuantity: 50 (전체 반품 수량)

  2. 재고 트랜잭션 생성:
     - transactionNo: IT-20260124-0002
     - transactionType: IN_RETURN
     - warehouseId: 1
     - productId: 101
     - quantity: 50
     - referenceType: RETURN
     - referenceId: 1
     - approvalStatus: PENDING

  3. 품질 검사 요청 생성:
     - inspectionNo: QI-20260124-0003
     - inspectionType: RETURN
     - productId: 101
     - inspectedQuantity: 50
     - inspectionResult: CONDITIONAL (검사 대기)

  4. 반품 업데이트:
     - receivedDate: 2026-01-24T16:10:00
     - returnStatus: INSPECTING (검사 필요)
```

### 4. 품질 검사 완료 (QMS)

```
품질 검사 결과:
  - inspectedQuantity: 50
  - passedQuantity: 45
  - failedQuantity: 5
  - inspectionResult: PASS (합격 기준 충족)

→ 반품 항목 업데이트:
  - items[].inspectionStatus: PASS
```

### 5. 반품 완료 - 재고 복원 (창고 관리자)

```
POST /api/returns/1/complete

→ 시스템:
  1. 합격품 재입고 (45개):
     a. 새 LOT 생성:
        - lotNo: LOT-20260124-0005
        - productId: 101
        - quantity: 45
        - qualityStatus: PASSED
        - lotType: RETURN

     b. 재고 트랜잭션 생성:
        - transactionNo: IT-20260124-0003
        - transactionType: IN_RETURN_RESTORE
        - warehouseId: 1 (원래 창고)
        - productId: 101
        - lotId: LOT-20260124-0005
        - quantity: 45
        - approvalStatus: APPROVED

     c. 재고 업데이트:
        - inventory.availableQuantity: +45

  2. 불합격품 격리 (5개):
     a. 새 LOT 생성:
        - lotNo: LOT-20260124-0006
        - productId: 101
        - quantity: 5
        - qualityStatus: FAILED
        - lotType: QUARANTINE

     b. 재고 트랜잭션 생성:
        - transactionNo: IT-20260124-0004
        - transactionType: IN_QUARANTINE
        - warehouseId: 5 (격리 창고)
        - productId: 101
        - lotId: LOT-20260124-0006
        - quantity: 5
        - approvalStatus: APPROVED

     c. 격리 재고 업데이트:
        - quarantine_inventory.availableQuantity: +5

  3. 반품 완료:
     - completedDate: 2026-01-24T16:30:00
     - returnStatus: COMPLETED
     - items[].passedQuantity: 45
     - items[].failedQuantity: 5
     - items[].newLotNo: "LOT-20260124-0005" (합격품 LOT)
```

---

## 🔐 보안 및 검증

### 상태 전이 검증

1. **반품**:
   - 승인: PENDING만 가능
   - 거부: PENDING만 가능
   - 입고: APPROVED만 가능
   - 완료: RECEIVED, INSPECTING만 가능
   - 취소: PENDING, APPROVED만 가능

### 품질 검사 자동 생성

```java
// 반품 유형에 따라 검사 상태 자동 설정
if ("DEFECTIVE".equals(returnEntity.getReturnType())) {
    item.setInspectionStatus("PENDING"); // 불량품은 검사 필요
} else {
    item.setInspectionStatus("NOT_REQUIRED"); // 과잉/오배송은 검사 불필요
}
```

### 권한 검증

- **Spring Security @PreAuthorize**:
  - 반품 생성: PRODUCTION_MANAGER, PRODUCTION_WORKER, WAREHOUSE_MANAGER
  - 승인/거부: WAREHOUSE_MANAGER
  - 입고: WAREHOUSE_MANAGER, INVENTORY_CLERK
  - 완료: WAREHOUSE_MANAGER

### 멀티 테넌트 격리

- 모든 쿼리: `WHERE tenant_id = :tenantId`
- 반품 번호: `UNIQUE (tenant_id, return_no)`

---

## ✅ 테스트 시나리오

### 시나리오 1: 불량품 반품 (정상 플로우)

```
1. 생산 담당자(userId=1): 불량품 반품 신청
   - 제품: RAW-001
   - 수량: 50
   - 유형: DEFECTIVE
   - 원래 LOT: LOT-20260120-001
   → 상태: PENDING, 검사: PENDING

2. 창고 관리자(userId=2): 승인
   → 상태: APPROVED

3. 창고 담당자(userId=3): 입고
   - 재고 트랜잭션 생성 (IN_RETURN)
   - 품질 검사 요청 생성
   → 상태: INSPECTING

4. 품질 팀: 검사 실행
   - 합격: 45개
   - 불합격: 5개
   - 결과: PASS
   → 검사 상태: PASS

5. 창고 관리자(userId=2): 완료
   - 합격품 45개: 원래 창고 재입고 (새 LOT 생성)
   - 불합격품 5개: 격리 창고 이동 (새 LOT 생성)
   → 상태: COMPLETED
```

### 시나리오 2: 과잉 반품 (검사 불필요)

```
1. 반품 신청: 유형=EXCESS, 수량=100
   → 검사 상태: NOT_REQUIRED

2. 승인 → 상태: APPROVED

3. 입고:
   - 재고 트랜잭션 생성
   - 품질 검사 없음
   → 상태: RECEIVED (INSPECTING이 아님)

4. 완료:
   - 전체 100개 재입고 (새 LOT 생성)
   - 격리 창고 이동 없음
   → 상태: COMPLETED
```

### 시나리오 3: 반품 거부

```
1. 반품 신청: PENDING
2. 창고 관리자: 거부
   - 거부 사유: "해당 제품 재입고 불가"
   → 상태: REJECTED
   → 재고 영향 없음
```

---

## 📈 성과

### 구현 완료

✅ **백엔드**:
- 2개 테이블 (스키마 설계 완료)
- 2개 엔티티
- 1개 레포지토리 (JOIN FETCH 쿼리)
- 1개 서비스 (완전한 워크플로우)
- 4개 DTO
- 1개 컨트롤러 (12개 API 엔드포인트)
- 2개 에러 코드

✅ **프론트엔드**:
- 1개 페이지 (Material-UI 5)
- DataGrid 통합
- 상태/유형별 칩 렌더링
- 다이얼로그 (거부, 취소)
- 통계 표시
- 라우트 설정

### 기능 완성도

- ✅ 반품 신청 CRUD
- ✅ 승인 워크플로우
- ✅ 반품 입고 처리
- ✅ 품질 검사 자동 생성
- ✅ 재고 복원 (합격품 재입고)
- ✅ 불합격품 격리
- ✅ 새 LOT 생성 관리
- ✅ 멀티 테넌트 격리
- ✅ 권한 제어

---

## 🚀 다음 단계

### WMS 모듈 완성도 현황

**Phase 완료**:
- ✅ Phase 1: IQC/OQC 의뢰 리스트
- ✅ Phase 2: 불출 관리 (Material Issue)
- ✅ Phase 3: 반품 관리 (Returns)

**다음 추천 작업**:

### Phase 4: 폐기 관리 (Disposal Management)

**우선순위**: 중간
**예상 소요**: 2-3시간

**핵심 기능**:
1. **폐기 의뢰**: 불량품/만료품 폐기 신청
2. **폐기 승인**: 관리자 검토
3. **폐기 처리**: 재고 차감 및 처분
4. **폐기 기록**: 추적 및 감사

**데이터 모델**:
- wms.si_disposals (폐기 헤더)
- wms.si_disposal_items (폐기 항목)

---

## 📝 주요 파일 목록

### 백엔드 파일 (8개)

1. `database/migrations/V019__create_returns_schema.sql`
2. `backend/src/main/java/kr/co/softice/mes/domain/entity/ReturnEntity.java`
3. `backend/src/main/java/kr/co/softice/mes/domain/entity/ReturnItemEntity.java`
4. `backend/src/main/java/kr/co/softice/mes/domain/repository/ReturnRepository.java`
5. `backend/src/main/java/kr/co/softice/mes/domain/service/ReturnService.java`
6. `backend/src/main/java/kr/co/softice/mes/api/controller/ReturnController.java`
7. `backend/src/main/java/kr/co/softice/mes/common/exception/ErrorCode.java` (업데이트)

### DTO 파일 (4개)

8. `backend/src/main/java/kr/co/softice/mes/common/dto/wms/ReturnCreateRequest.java`
9. `backend/src/main/java/kr/co/softice/mes/common/dto/wms/ReturnItemRequest.java`
10. `backend/src/main/java/kr/co/softice/mes/common/dto/wms/ReturnResponse.java`
11. `backend/src/main/java/kr/co/softice/mes/common/dto/wms/ReturnItemResponse.java`

### 프론트엔드 파일 (2개)

12. `frontend/src/pages/warehouse/ReturnsPage.tsx`
13. `frontend/src/App.tsx` (업데이트)

**총 파일 수**: 13개

---

## 🎯 완료 확인

### Phase 3 체크리스트

- [x] 데이터베이스 스키마 설계 (2 테이블)
- [x] 마이그레이션 파일 작성
- [x] 엔티티 생성 (2개)
- [x] 레포지토리 생성 (1개, JOIN FETCH 쿼리)
- [x] 서비스 생성 (1개, 완전한 워크플로우)
- [x] DTO 생성 (4개)
- [x] 컨트롤러 생성 (1개, 12 API 엔드포인트)
- [x] 에러 코드 추가
- [x] 프론트엔드 페이지 생성 (1개)
- [x] 라우트 설정
- [x] 문서화

**Phase 3 완료**: ✅ 100%

---

**작성일**: 2026-01-24
**완료 시각**: 23:30 KST
**작성자**: Moon Myung-seop (문명섭)
