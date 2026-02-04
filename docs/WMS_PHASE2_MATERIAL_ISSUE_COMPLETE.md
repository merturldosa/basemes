# WMS Phase 2: 불출 관리 (Material Issue Management) - 완료 보고서

**작성일**: 2026-01-24
**작성자**: Moon Myung-seop
**완료 상태**: ✅ 100% 완료

---

## 📋 개요

### 목표
창고에서 생산 현장으로 자재 불출 관리 시스템 구축

### 핵심 기능
1. **불출 신청** (Material Request): 생산 담당자가 필요한 자재 신청
2. **불출 승인/거부**: 창고 관리자의 승인 워크플로우
3. **불출 지시**: 재고 차감 및 LOT 선택 (FIFO)
4. **자재 인수인계**: 창고 담당자(출고자) ↔ 생산 담당자(인수자) 간 인수인계 확인

### 워크플로우
```
생산 담당자: 불출 신청 (PENDING)
    ↓
창고 관리자: 재고 가용성 확인 → 승인 (APPROVED) / 거부 (REJECTED)
    ↓
창고 담당자: LOT 선택 (FIFO) → 불출 지시 (ISSUED)
    ├─ 재고 트랜잭션 생성 (OUT_ISSUE)
    ├─ 재고 차감
    └─ 인수인계 레코드 생성 (PENDING)
    ↓
생산 담당자: 인수 확인 (CONFIRMED) / 거부 (REJECTED)
    ↓
시스템: 모든 인수인계 확인 시 불출 신청 자동 완료 (COMPLETED)
```

---

## 🗄️ 데이터베이스 스키마

### 마이그레이션 파일
- **V018__create_material_issue_schema.sql**

### 테이블 (3개)

#### 1. wms.si_material_requests (불출 신청)
```sql
CREATE TABLE wms.si_material_requests (
    material_request_id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    request_no VARCHAR(50) NOT NULL,              -- MR-YYYYMMDD-0001
    request_date TIMESTAMP NOT NULL,
    work_order_id BIGINT,                         -- 작업 지시 (Optional)
    requester_user_id BIGINT NOT NULL,            -- 신청자
    warehouse_id BIGINT NOT NULL,
    required_date DATE NOT NULL,                  -- 필요일자
    request_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
        -- PENDING, APPROVED, REJECTED, ISSUED, COMPLETED, CANCELLED
    priority VARCHAR(20) DEFAULT 'NORMAL',        -- URGENT, HIGH, NORMAL, LOW
    purpose VARCHAR(100),                         -- PRODUCTION, MAINTENANCE, SAMPLE, OTHER
    approver_user_id BIGINT,
    approved_date TIMESTAMP,
    issued_date TIMESTAMP,
    completed_date TIMESTAMP,
    remarks TEXT,
    rejection_reason TEXT,
    cancellation_reason TEXT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    CONSTRAINT uk_material_request_no UNIQUE (tenant_id, request_no)
);
```

**인덱스**:
- tenant_id
- request_date
- work_order_id
- requester_user_id
- warehouse_id
- request_status
- required_date
- priority

#### 2. wms.si_material_request_items (불출 신청 상세)
```sql
CREATE TABLE wms.si_material_request_items (
    material_request_item_id BIGSERIAL PRIMARY KEY,
    material_request_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_code VARCHAR(50),
    product_name VARCHAR(200),
    requested_quantity NUMERIC(15,3) NOT NULL,
    approved_quantity NUMERIC(15,3),
    issued_quantity NUMERIC(15,3),
    issue_status VARCHAR(30) DEFAULT 'PENDING',   -- PENDING, PARTIAL, COMPLETED, CANCELLED
    requested_lot_no VARCHAR(100),                -- 특정 LOT 요청 (Optional)
    issued_lot_no VARCHAR(100),                   -- 실제 불출된 LOT
    remarks TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (material_request_id) REFERENCES wms.si_material_requests(material_request_id)
);
```

**인덱스**:
- material_request_id
- product_id
- issue_status

#### 3. wms.si_material_handovers (자재 인수인계)
```sql
CREATE TABLE wms.si_material_handovers (
    material_handover_id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    handover_no VARCHAR(50) NOT NULL,             -- MH-YYYYMMDD-0001
    handover_date TIMESTAMP NOT NULL,
    material_request_id BIGINT NOT NULL,
    material_request_item_id BIGINT NOT NULL,
    inventory_transaction_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    lot_id BIGINT,
    lot_no VARCHAR(100),
    quantity NUMERIC(15,3) NOT NULL,
    unit VARCHAR(20),
    issuer_user_id BIGINT NOT NULL,               -- 출고자 (창고 담당)
    issuer_name VARCHAR(100),
    issue_location VARCHAR(200),
    receiver_user_id BIGINT NOT NULL,             -- 인수자 (생산 담당)
    receiver_name VARCHAR(100),
    receive_location VARCHAR(200),
    received_date TIMESTAMP,
    handover_status VARCHAR(30) DEFAULT 'PENDING', -- PENDING, CONFIRMED, REJECTED
    confirmation_remarks TEXT,
    remarks TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    CONSTRAINT uk_material_handover_no UNIQUE (tenant_id, handover_no)
);
```

**인덱스**:
- tenant_id
- handover_date
- material_request_id
- inventory_transaction_id
- product_id
- lot_id
- issuer_user_id
- receiver_user_id
- handover_status

---

## 🔧 백엔드 구현

### 엔티티 (Entities)

1. **MaterialRequestEntity.java**
   - 불출 신청 헤더
   - 상태 전이: PENDING → APPROVED → ISSUED → COMPLETED
   - @OneToMany items

2. **MaterialRequestItemEntity.java**
   - 불출 신청 상세 항목
   - 요청/승인/불출 수량 추적
   - 특정 LOT 요청 기능

3. **MaterialHandoverEntity.java**
   - 자재 인수인계 레코드
   - 출고자(issuer) ↔ 인수자(receiver) 추적
   - 상태: PENDING → CONFIRMED/REJECTED

### 레포지토리 (Repositories)

1. **MaterialRequestRepository.java**
   - `findByTenantIdWithAllRelations()`: 모든 관계 JOIN FETCH
   - `findByIdWithAllRelations()`: 상세 조회 (항목 포함)
   - `findByTenantIdAndStatus()`: 상태별 조회
   - `findByWorkOrderIdWithRelations()`: 작업 지시별 조회
   - `findPendingRequestsByWarehouse()`: 창고별 대기 신청
   - `findUrgentRequests()`: 긴급 신청 조회

2. **MaterialHandoverRepository.java**
   - `findByTenantIdWithAllRelations()`: 모든 관계 JOIN FETCH
   - `findByIdWithAllRelations()`: 상세 조회
   - `findByMaterialRequestIdWithRelations()`: 불출 신청별 조회
   - `findPendingHandoversByReceiver()`: 수신자별 대기 인수인계
   - `findByTenantIdAndStatusWithRelations()`: 상태별 조회

### 서비스 (Services)

1. **MaterialRequestService.java** (1,200+ 라인)

**핵심 메서드**:

```java
// 불출 신청 생성
public MaterialRequestEntity createMaterialRequest(MaterialRequestEntity request) {
    // 1. 신청번호 자동 생성 (MR-YYYYMMDD-0001)
    // 2. 상태: PENDING
    // 3. 항목별 issueStatus: PENDING
    // 4. 저장
}

// 불출 신청 승인
@Transactional
public MaterialRequestEntity approveMaterialRequest(Long requestId, Long approverId) {
    // 1. 상태 검증 (PENDING만 승인 가능)
    // 2. 재고 가용성 검증
    // 3. 승인 정보 업데이트
    // 4. 상태 → APPROVED
    // 5. 항목별 approvedQuantity 설정
}

// 불출 신청 거부
@Transactional
public MaterialRequestEntity rejectMaterialRequest(Long requestId, Long approverId, String reason) {
    // 1. 상태 검증 (PENDING만 거부 가능)
    // 2. 거부 사유 저장
    // 3. 상태 → REJECTED
}

// 불출 지시 (핵심 로직)
@Transactional
public MaterialRequestEntity issueMaterials(Long requestId, Long issuerUserId) {
    // 1. 상태 검증 (APPROVED만 불출 가능)
    // 2. 항목별 처리:
    //    a. LOT 선택 (FIFO 또는 특정 LOT)
    //    b. 재고 트랜잭션 생성 (OUT_ISSUE)
    //    c. 재고 차감
    //    d. 인수인계 레코드 생성
    // 3. 상태 → ISSUED
    // 4. issuedDate 업데이트
}

// 불출 신청 완료
@Transactional
public MaterialRequestEntity completeMaterialRequest(Long requestId) {
    // 1. 상태 검증 (ISSUED만 완료 가능)
    // 2. 상태 → COMPLETED
    // 3. completedDate 업데이트
}

// 불출 신청 취소
@Transactional
public MaterialRequestEntity cancelMaterialRequest(Long requestId, String reason) {
    // 1. 상태 검증 (PENDING/APPROVED만 취소 가능)
    // 2. 취소 사유 저장
    // 3. 상태 → CANCELLED
}

// 재고 가용성 검증 (Private Helper)
private void validateItemAvailability(MaterialRequestItemEntity item, Long warehouseId) {
    // 창고 내 제품별 가용 재고 합산
    // requested_quantity와 비교
    // 부족 시 예외 발생
}

// LOT 선택 (FIFO 또는 특정 LOT) (Private Helper)
private LotEntity selectLotForIssue(Long warehouseId, Long productId, String requestedLotNo) {
    // 특정 LOT 요청 시: 해당 LOT 검증
    // 미요청 시: FIFO (가장 오래된 LOT 선택)
    // 품질 상태: PASSED만 선택
}

// 재고 잔액 업데이트 (Private Helper)
private void updateInventoryBalance(Long warehouseId, Long productId, Long lotId, BigDecimal quantity) {
    // 재고 레코드 조회
    // available_quantity 차감
    // 저장
}
```

2. **MaterialHandoverService.java**

**핵심 메서드**:

```java
// 인수 확인
@Transactional
public MaterialHandoverEntity confirmHandover(Long handoverId, Long receiverId, String remarks) {
    // 1. 상태 검증 (PENDING만 확인 가능)
    // 2. 인수자 검증 (assigned receiver만 확인 가능)
    // 3. receivedDate 업데이트
    // 4. confirmation_remarks 저장
    // 5. 상태 → CONFIRMED
    // 6. checkAndCompleteRequest() 호출
}

// 인수 거부
@Transactional
public MaterialHandoverEntity rejectHandover(Long handoverId, Long receiverId, String reason) {
    // 1. 상태 검증 (PENDING만 거부 가능)
    // 2. 인수자 검증
    // 3. receivedDate 업데이트
    // 4. confirmation_remarks 저장 (거부 사유)
    // 5. 상태 → REJECTED
}

// 불출 신청 자동 완료 체크 (Private Helper)
private void checkAndCompleteRequest(Long requestId) {
    // 모든 인수인계 조회
    // 전부 CONFIRMED인지 확인
    // 모두 확인되면 불출 신청 상태 → COMPLETED
}
```

### DTOs

1. **MaterialRequestCreateRequest.java**
   ```java
   - requestNo (Optional)
   - requestDate
   - workOrderId (Optional)
   - requesterUserId
   - warehouseId
   - requiredDate
   - priority (URGENT, HIGH, NORMAL, LOW)
   - purpose (PRODUCTION, MAINTENANCE, SAMPLE, OTHER)
   - items: List<MaterialRequestItemRequest>
   - remarks
   ```

2. **MaterialRequestItemRequest.java**
   ```java
   - productId
   - requestedQuantity
   - requestedLotNo (Optional)
   - remarks
   ```

3. **MaterialRequestResponse.java**
   ```java
   - Header: requestNo, requestDate, status, priority, purpose
   - Work Order: workOrderId, workOrderNo
   - Requester: requesterUserId, requesterName
   - Warehouse: warehouseId, warehouseCode, warehouseName
   - Approver: approverUserId, approverName, approvedDate
   - Dates: requiredDate, issuedDate, completedDate
   - Totals: totalRequestedQuantity, totalApprovedQuantity, totalIssuedQuantity
   - Items: List<MaterialRequestItemResponse>
   - Remarks, rejectionReason, cancellationReason
   ```

4. **MaterialRequestItemResponse.java**
   ```java
   - Product: productId, productCode, productName, productType, unit
   - Quantities: requestedQuantity, approvedQuantity, issuedQuantity
   - Status: issueStatus
   - LOT: requestedLotNo, issuedLotNo
   ```

5. **MaterialHandoverResponse.java**
   ```java
   - Header: handoverNo, handoverDate, handoverStatus
   - References: materialRequestNo, transactionNo
   - Product: productCode, productName
   - LOT: lotNo, lotQualityStatus
   - Quantity: quantity, unit
   - Issuer: issuerName, issueLocation
   - Receiver: receiverName, receiveLocation, receivedDate
   - Confirmation: confirmationRemarks
   ```

### 컨트롤러 (Controllers)

1. **MaterialRequestController.java** (500+ 라인)

**API 엔드포인트**:

```java
GET    /api/material-requests                           // 목록 조회
GET    /api/material-requests?status={status}           // 상태별 조회
GET    /api/material-requests?workOrderId={id}          // 작업 지시별 조회
GET    /api/material-requests?warehouseId={id}          // 창고별 조회
GET    /api/material-requests?requesterId={id}          // 신청자별 조회
GET    /api/material-requests/{id}                      // 상세 조회
GET    /api/material-requests/urgent                    // 긴급 신청 조회
GET    /api/material-requests/warehouse/{id}/pending    // 창고별 대기 신청

POST   /api/material-requests                           // 신청 생성
POST   /api/material-requests/{id}/approve              // 승인
POST   /api/material-requests/{id}/reject               // 거부
POST   /api/material-requests/{id}/issue                // 불출 지시
POST   /api/material-requests/{id}/complete             // 완료
POST   /api/material-requests/{id}/cancel               // 취소
```

**권한**:
- 읽기: 모든 인증 사용자
- 생성: PRODUCTION_MANAGER, PRODUCTION_WORKER
- 승인/거부/불출: WAREHOUSE_MANAGER, INVENTORY_CLERK
- 완료: WAREHOUSE_MANAGER
- 취소: WAREHOUSE_MANAGER, PRODUCTION_MANAGER

2. **MaterialHandoverController.java**

**API 엔드포인트**:

```java
GET    /api/material-handovers                          // 목록 조회
GET    /api/material-handovers?status={status}          // 상태별 조회
GET    /api/material-handovers?materialRequestId={id}   // 불출 신청별 조회
GET    /api/material-handovers/{id}                     // 상세 조회
GET    /api/material-handovers/my-pending               // 내 대기 인수인계

POST   /api/material-handovers/{id}/confirm             // 인수 확인
POST   /api/material-handovers/{id}/reject              // 인수 거부
```

**권한**:
- 읽기: 모든 인증 사용자
- 인수 확인/거부: PRODUCTION_MANAGER, PRODUCTION_WORKER

### 에러 코드

```java
MATERIAL_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "MR18200", "불출 신청을 찾을 수 없습니다.")
MATERIAL_REQUEST_ALREADY_EXISTS(HttpStatus.CONFLICT, "MR18201", "이미 존재하는 불출 신청입니다.")
MATERIAL_HANDOVER_NOT_FOUND(HttpStatus.NOT_FOUND, "MH18300", "자재 인수인계를 찾을 수 없습니다.")
MATERIAL_HANDOVER_ALREADY_EXISTS(HttpStatus.CONFLICT, "MH18301", "이미 존재하는 자재 인수인계입니다.")
```

---

## 🎨 프론트엔드 구현

### 페이지

1. **MaterialRequestsPage.tsx** (550+ 라인)

**기능**:
- DataGrid 목록 (불출 신청)
- 상태 칩 (대기, 승인, 거부, 불출, 완료, 취소)
- 우선순위 칩 (긴급, 높음, 보통, 낮음)
- 수량 표시 (요청/승인/불출)
- 통계 (전체, 대기, 승인, 불출, 완료, 긴급)
- 액션:
  - 상세 보기
  - 승인 (PENDING → APPROVED)
  - 거부 다이얼로그 (PENDING → REJECTED)
  - 불출 지시 (APPROVED → ISSUED)
  - 완료 (ISSUED → COMPLETED)
  - 취소 다이얼로그 (PENDING/APPROVED → CANCELLED)
- 새로고침 버튼
- 신규 신청 버튼

**컬럼**:
- 신청번호
- 신청일시
- 상태
- 우선순위
- 용도 (생산, 보수, 샘플, 기타)
- 작업지시
- 신청자
- 창고
- 필요일자
- 수량 (요청/승인/불출)
- 승인자
- 작업 (액션 메뉴)

**상태별 색상**:
- PENDING: warning (노란색)
- APPROVED: info (파란색)
- REJECTED: error (빨간색)
- ISSUED: primary (보라색)
- COMPLETED: success (초록색)
- CANCELLED: default (회색)

2. **MaterialHandoversPage.tsx** (450+ 라인)

**기능**:
- DataGrid 목록 (자재 인수인계)
- 상태 칩 (대기, 확인, 거부)
- LOT 품질 상태 칩 (합격, 불합격, 검사대기, 조건부)
- 통계 (전체, 대기, 확인, 거부)
- 액션:
  - 인수 확인 다이얼로그 (PENDING → CONFIRMED)
  - 인수 거부 다이얼로그 (PENDING → REJECTED)
- 새로고침 버튼

**컬럼**:
- 인수인계번호
- 인계일시
- 상태
- 불출신청번호
- 제품코드
- 제품명
- LOT번호
- LOT품질
- 수량
- 출고자
- 출고위치
- 인수자
- 인수위치
- 인수일시
- 작업 (액션 메뉴)

**인수 확인 다이얼로그**:
- 인수인계 정보 요약 표시
- 확인 메모 입력 (선택)
- 인수 확인 버튼

**인수 거부 다이얼로그**:
- 인수인계 정보 요약 표시
- 거부 사유 입력 (필수)
- 인수 거부 버튼

### 라우트 추가

**App.tsx**:
```tsx
import MaterialRequestsPage from './pages/warehouse/MaterialRequestsPage';
import MaterialHandoversPage from './pages/warehouse/MaterialHandoversPage';

// Routes:
<Route path="warehouse/material-requests" element={<MaterialRequestsPage />} />
<Route path="warehouse/material-handovers" element={<MaterialHandoversPage />} />
```

---

## 📊 데이터 흐름

### 1. 불출 신청 생성 (생산 담당자)

```
POST /api/material-requests
{
  "requestDate": "2026-01-24T14:00:00",
  "requesterUserId": 1,
  "warehouseId": 1,
  "requiredDate": "2026-01-25",
  "priority": "URGENT",
  "purpose": "PRODUCTION",
  "items": [
    {
      "productId": 101,
      "requestedQuantity": 500
    }
  ]
}

→ 시스템:
  - requestNo 자동 생성: MR-20260124-0001
  - requestStatus: PENDING
  - items[].issueStatus: PENDING
```

### 2. 불출 신청 승인 (창고 관리자)

```
POST /api/material-requests/1/approve?approverUserId=2

→ 시스템:
  1. 재고 가용성 검증 (창고 내 제품별 재고 합산)
  2. approverUserId: 2
  3. approvedDate: 2026-01-24T14:05:00
  4. requestStatus: APPROVED
  5. items[].approvedQuantity: 500 (requestedQuantity 복사)
```

### 3. 불출 지시 (창고 담당자)

```
POST /api/material-requests/1/issue?issuerUserId=3

→ 시스템:
  1. LOT 선택 (FIFO):
     - 창고 내 제품 101의 LOT 중 가장 오래된 LOT 선택
     - LOT-20260120-001, 재고: 1000, 품질: PASSED

  2. 재고 트랜잭션 생성:
     - transactionNo: IT-20260124-0001
     - transactionType: OUT_ISSUE
     - warehouseId: 1
     - productId: 101
     - lotId: LOT-20260120-001
     - quantity: -500
     - referenceType: MATERIAL_REQUEST
     - referenceId: 1
     - approvalStatus: APPROVED

  3. 재고 업데이트:
     - inventory.availableQuantity: 1000 → 500

  4. 인수인계 생성:
     - handoverNo: MH-20260124-0001
     - materialRequestId: 1
     - materialRequestItemId: 1
     - inventoryTransactionId: IT-20260124-0001
     - productId: 101
     - lotId: LOT-20260120-001
     - quantity: 500
     - issuerUserId: 3 (창고 담당자)
     - receiverUserId: 1 (생산 담당자)
     - handoverStatus: PENDING

  5. 불출 신청 업데이트:
     - requestStatus: ISSUED
     - issuedDate: 2026-01-24T14:10:00
     - items[].issuedQuantity: 500
     - items[].issueStatus: COMPLETED
     - items[].issuedLotNo: LOT-20260120-001
```

### 4. 인수 확인 (생산 담당자)

```
POST /api/material-handovers/1/confirm?receiverId=1&remarks=확인완료

→ 시스템:
  1. 인수인계 업데이트:
     - receivedDate: 2026-01-24T14:15:00
     - confirmationRemarks: "확인완료"
     - handoverStatus: CONFIRMED

  2. 불출 신청 자동 완료 체크:
     - 모든 인수인계 확인 여부 확인
     - 전부 CONFIRMED → 불출 신청 상태: COMPLETED
     - completedDate: 2026-01-24T14:15:00
```

---

## 🔐 보안 및 검증

### 상태 전이 검증

1. **불출 신청**:
   - 승인: PENDING만 가능
   - 거부: PENDING만 가능
   - 불출 지시: APPROVED만 가능
   - 완료: ISSUED만 가능
   - 취소: PENDING, APPROVED만 가능

2. **인수인계**:
   - 인수 확인: PENDING만 가능
   - 인수 거부: PENDING만 가능
   - 인수자 검증: assigned receiver만 확인/거부 가능

### 재고 가용성 검증

```java
// 승인 시 재고 부족 검증
if (availableQuantity < requestedQuantity) {
    throw new IllegalStateException("Insufficient inventory");
}
```

### 권한 검증

- **Spring Security @PreAuthorize**:
  - 불출 신청 생성: PRODUCTION_MANAGER, PRODUCTION_WORKER
  - 승인/거부/불출: WAREHOUSE_MANAGER, INVENTORY_CLERK
  - 인수 확인/거부: PRODUCTION_MANAGER, PRODUCTION_WORKER

### 멀티 테넌트 격리

- 모든 쿼리: `WHERE tenant_id = :tenantId`
- 인수인계, 불출 신청 번호: `UNIQUE (tenant_id, request_no)`

---

## ✅ 테스트 시나리오

### 시나리오 1: 정상 플로우

```
1. 생산 담당자(userId=1): 불출 신청 생성
   - 제품: RAW-001
   - 수량: 500
   - 우선순위: URGENT
   - 용도: PRODUCTION
   → 상태: PENDING

2. 창고 관리자(userId=2): 승인
   - 재고 확인: available=1000 ≥ requested=500
   → 상태: APPROVED

3. 창고 담당자(userId=3): 불출 지시
   - LOT 선택: LOT-20260120-001 (FIFO)
   - 재고 차감: 1000 → 500
   - 인수인계 생성: MH-20260124-0001
   → 상태: ISSUED

4. 생산 담당자(userId=1): 인수 확인
   - receivedDate 업데이트
   - 모든 인수인계 확인 시 자동 완료
   → 상태: COMPLETED
```

### 시나리오 2: 재고 부족 시 거부

```
1. 불출 신청: 수량=1000
2. 승인 시도:
   - 재고 확인: available=500 < requested=1000
   - 예외 발생: "Insufficient inventory"
3. 창고 관리자: 거부
   - 거부 사유: "재고 부족"
   → 상태: REJECTED
```

### 시나리오 3: 인수 거부

```
1. 불출 지시 완료: ISSUED
2. 인수인계: PENDING
3. 생산 담당자: 인수 거부
   - 거부 사유: "제품 손상"
   → 인수인계 상태: REJECTED
   → 불출 신청 상태: ISSUED (완료되지 않음)
```

### 시나리오 4: 특정 LOT 요청

```
1. 불출 신청 항목:
   - requestedLotNo: "LOT-20260115-005"
2. 불출 지시:
   - 특정 LOT 검증 (존재 여부, 재고 충분, 품질=PASSED)
   - 해당 LOT에서 불출
   - issuedLotNo: "LOT-20260115-005"
```

---

## 📈 성과

### 구현 완료

✅ **백엔드**:
- 3개 테이블 (스키마 설계 완료)
- 3개 엔티티
- 2개 레포지토리 (JOIN FETCH 쿼리)
- 2개 서비스 (완전한 워크플로우)
- 5개 DTO
- 2개 컨트롤러 (16개 API 엔드포인트)
- 4개 에러 코드

✅ **프론트엔드**:
- 2개 페이지 (Material-UI 5)
- DataGrid 통합
- 상태별 칩 렌더링
- 다이얼로그 (거부, 취소, 확인)
- 통계 표시
- 라우트 설정

### 기능 완성도

- ✅ 불출 신청 CRUD
- ✅ 승인 워크플로우
- ✅ 재고 가용성 검증
- ✅ LOT 선택 (FIFO)
- ✅ 재고 트랜잭션 생성
- ✅ 재고 차감
- ✅ 인수인계 관리
- ✅ 자동 완료 로직
- ✅ 멀티 테넌트 격리
- ✅ 권한 제어

---

## 🚀 다음 단계 (Phase 3)

### 추천 작업: 반품 관리 (Returns Management)

**우선순위**: 중간
**예상 소요**: 2-3시간

**핵심 기능**:
1. **반품 신청**: 불량품/과잉 반품 신청
2. **반품 승인**: 창고 관리자 검토
3. **반품 입고**: LOT 품질 재검사
4. **재고 복원**: 합격품 재입고, 불합격품 격리

**데이터 모델**:
- wms.si_returns (반품 헤더)
- wms.si_return_items (반품 항목)

---

## 📝 주요 파일 목록

### 백엔드 파일 (11개)

1. `database/migrations/V018__create_material_issue_schema.sql`
2. `backend/src/main/java/kr/co/softice/mes/domain/entity/MaterialRequestEntity.java`
3. `backend/src/main/java/kr/co/softice/mes/domain/entity/MaterialRequestItemEntity.java`
4. `backend/src/main/java/kr/co/softice/mes/domain/entity/MaterialHandoverEntity.java`
5. `backend/src/main/java/kr/co/softice/mes/domain/repository/MaterialRequestRepository.java`
6. `backend/src/main/java/kr/co/softice/mes/domain/repository/MaterialHandoverRepository.java`
7. `backend/src/main/java/kr/co/softice/mes/domain/service/MaterialRequestService.java`
8. `backend/src/main/java/kr/co/softice/mes/domain/service/MaterialHandoverService.java`
9. `backend/src/main/java/kr/co/softice/mes/api/controller/MaterialRequestController.java`
10. `backend/src/main/java/kr/co/softice/mes/api/controller/MaterialHandoverController.java`
11. `backend/src/main/java/kr/co/softice/mes/common/exception/ErrorCode.java` (업데이트)

### DTO 파일 (5개)

12. `backend/src/main/java/kr/co/softice/mes/common/dto/wms/MaterialRequestCreateRequest.java`
13. `backend/src/main/java/kr/co/softice/mes/common/dto/wms/MaterialRequestItemRequest.java`
14. `backend/src/main/java/kr/co/softice/mes/common/dto/wms/MaterialRequestResponse.java`
15. `backend/src/main/java/kr/co/softice/mes/common/dto/wms/MaterialRequestItemResponse.java`
16. `backend/src/main/java/kr/co/softice/mes/common/dto/wms/MaterialHandoverResponse.java`

### 프론트엔드 파일 (3개)

17. `frontend/src/pages/warehouse/MaterialRequestsPage.tsx`
18. `frontend/src/pages/warehouse/MaterialHandoversPage.tsx`
19. `frontend/src/App.tsx` (업데이트)

**총 파일 수**: 19개

---

## 🎯 완료 확인

### Phase 2 체크리스트

- [x] 데이터베이스 스키마 설계 (3 테이블)
- [x] 마이그레이션 파일 작성
- [x] 엔티티 생성 (3개)
- [x] 레포지토리 생성 (2개, JOIN FETCH 쿼리)
- [x] 서비스 생성 (2개, 완전한 워크플로우)
- [x] DTO 생성 (5개)
- [x] 컨트롤러 생성 (2개, 16 API 엔드포인트)
- [x] 에러 코드 추가
- [x] 프론트엔드 페이지 생성 (2개)
- [x] 라우트 설정
- [x] 문서화

**Phase 2 완료**: ✅ 100%

---

**작성일**: 2026-01-24
**완료 시각**: 23:00 KST
**작성자**: Moon Myung-seop (문명섭)
