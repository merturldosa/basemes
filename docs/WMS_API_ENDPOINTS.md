# WMS 모듈 API 엔드포인트 명세

## 📌 Base URL
```
http://localhost:8080/api
```

## 📦 공통 응답 포맷
```json
{
  "success": true,
  "message": "성공 메시지",
  "data": { ... }
}
```

---

## 1. 창고 관리 (Warehouse Management)

### 1.1 창고 목록 조회
```http
GET /warehouses?activeOnly=true
Authorization: Bearer {token}
X-Tenant-ID: {tenantId}
```

**Response**:
```json
{
  "success": true,
  "data": [
    {
      "warehouseId": 1,
      "warehouseCode": "WH-RAW",
      "warehouseName": "원자재 창고",
      "warehouseType": "RAW_MATERIAL",
      "location": "A동 1층",
      "managerUserId": 1,
      "managerUserName": "홍길동",
      "capacity": 1000,
      "unit": "㎡",
      "isActive": true
    }
  ]
}
```

### 1.2 창고 생성
```http
POST /warehouses
Authorization: Bearer {token}
X-Tenant-ID: {tenantId}
```

**Request**:
```json
{
  "warehouseCode": "WH-RAW",
  "warehouseName": "원자재 창고",
  "warehouseType": "RAW_MATERIAL",
  "location": "A동 1층",
  "managerUserId": 1,
  "capacity": 1000,
  "unit": "㎡",
  "isActive": true,
  "remarks": "원자재 보관"
}
```

**창고 타입**:
- `RAW_MATERIAL`: 원자재
- `WORK_IN_PROCESS` / `WIP`: 재공품
- `FINISHED_GOODS`: 완제품
- `QUARANTINE`: 격리
- `SCRAP`: 스크랩/불량

---

## 2. 재고 관리 (Inventory Management)

### 2.1 재고 현황 조회
```http
GET /inventory
Authorization: Bearer {token}
X-Tenant-ID: {tenantId}
```

**Response**:
```json
{
  "success": true,
  "data": [
    {
      "inventoryId": 1,
      "warehouseId": 1,
      "warehouseCode": "WH-RAW",
      "warehouseName": "원자재 창고",
      "productId": 1,
      "productCode": "RAW-001",
      "productName": "원자재-001",
      "lotId": 1,
      "lotNo": "LOT-20260124-001",
      "availableQuantity": 950.000,
      "reservedQuantity": 200.000,
      "unit": "KG",
      "location": "A-1-2-3",
      "lastTransactionDate": "2026-01-24T14:30:00",
      "lastTransactionType": "IN_RECEIVE"
    }
  ]
}
```

### 2.2 창고별 재고 조회
```http
GET /inventory/warehouse/{warehouseId}
```

### 2.3 제품별 재고 조회
```http
GET /inventory/product/{productId}
```

### 2.4 저재고 알림
```http
GET /inventory/low-stock?threshold=100
```

### 2.5 재고 예약
```http
POST /inventory/reserve
Authorization: Bearer {token}
X-Tenant-ID: {tenantId}
```

**Request**:
```json
{
  "productId": 1,
  "warehouseId": 1,
  "lotId": 1,
  "quantity": 200,
  "workOrderId": 1,
  "remarks": "작업지시 WO-001 자재 예약"
}
```

### 2.6 예약 해제
```http
POST /inventory/release
Authorization: Bearer {token}
X-Tenant-ID: {tenantId}
```

**Request**:
```json
{
  "productId": 1,
  "warehouseId": 1,
  "lotId": 1,
  "quantity": 50,
  "remarks": "작업 취소로 인한 예약 해제"
}
```

---

## 3. 입하 관리 (Goods Receipt Management)

### 3.1 입하 목록 조회
```http
GET /goods-receipts?status=PENDING&purchaseOrderId=1&warehouseId=1
Authorization: Bearer {token}
X-Tenant-ID: {tenantId}
```

**Query Parameters**:
- `status`: 입하 상태 필터 (PENDING, INSPECTING, COMPLETED, REJECTED, CANCELLED)
- `purchaseOrderId`: 구매 주문 ID 필터
- `warehouseId`: 창고 ID 필터

**Response**:
```json
{
  "success": true,
  "data": [
    {
      "goodsReceiptId": 1,
      "receiptNo": "GR-20260124-0001",
      "receiptDate": "2026-01-24T14:00:00",
      "receiptType": "PURCHASE",
      "receiptStatus": "PENDING",
      "purchaseOrderId": 1,
      "purchaseOrderNo": "PO-001",
      "supplierId": 1,
      "supplierCode": "SUP-001",
      "supplierName": "테스트공급업체",
      "warehouseId": 1,
      "warehouseCode": "WH-RAW",
      "warehouseName": "원자재 창고",
      "totalQuantity": 1000.000,
      "totalAmount": 10500.00,
      "items": [
        {
          "goodsReceiptItemId": 1,
          "productId": 1,
          "productCode": "RAW-001",
          "productName": "원자재-001",
          "receivedQuantity": 1000.000,
          "lotNo": "LOT-20260124-001",
          "expiryDate": "2027-01-24",
          "inspectionStatus": "PENDING"
        }
      ]
    }
  ]
}
```

### 3.2 입하 생성
```http
POST /goods-receipts
Authorization: Bearer {token}
X-Tenant-ID: {tenantId}
```

**Request**:
```json
{
  "receiptNo": "",
  "receiptDate": "2026-01-24T14:00:00",
  "purchaseOrderId": 1,
  "supplierId": 1,
  "warehouseId": 1,
  "receiptType": "PURCHASE",
  "receiverUserId": 1,
  "items": [
    {
      "purchaseOrderItemId": 1,
      "productId": 1,
      "receivedQuantity": 1000,
      "lotNo": "LOT-20260124-001",
      "expiryDate": "2027-01-24",
      "inspectionStatus": "PENDING",
      "remarks": ""
    }
  ],
  "remarks": "구매 주문 PO-001 입하"
}
```

**참고**:
- `receiptNo`: 비워두면 자동 생성 (GR-YYYYMMDD-0001)
- `inspectionStatus`:
  - `NOT_REQUIRED`: 검사 불요
  - `PENDING`: 검사 대기
  - `PASS`: 합격
  - `FAIL`: 불합격

### 3.3 입하 완료
```http
POST /goods-receipts/{id}/complete
Authorization: Bearer {token}
X-Tenant-ID: {tenantId}
```

**워크플로우**:
1. 품질 검사 결과 확인
2. 합격품 → 가용 재고 추가
3. 불합격품 → 격리 창고 이동
4. 상태: `PENDING/INSPECTING` → `COMPLETED`

### 3.4 입하 취소
```http
POST /goods-receipts/{id}/cancel?reason=잘못된입하
Authorization: Bearer {token}
X-Tenant-ID: {tenantId}
```

**워크플로우**:
1. 재고 이동 역처리
2. LOT 비활성화
3. 상태: → `CANCELLED`

---

## 4. 재고 트랜잭션 (Inventory Transaction)

### 4.1 트랜잭션 목록 조회
```http
GET /inventory-transactions
Authorization: Bearer {token}
X-Tenant-ID: {tenantId}
```

### 4.2 승인 상태별 조회
```http
GET /inventory-transactions/approval-status/{status}
```

**승인 상태**:
- `PENDING`: 승인 대기
- `APPROVED`: 승인됨
- `REJECTED`: 거부됨

### 4.3 트랜잭션 생성
```http
POST /inventory-transactions
Authorization: Bearer {token}
X-Tenant-ID: {tenantId}
```

**Request**:
```json
{
  "transactionNo": "OUT-WO-001-001",
  "transactionDate": "2026-01-24T16:00:00",
  "transactionType": "OUT_ISSUE",
  "warehouseId": 1,
  "productId": 1,
  "lotId": 1,
  "quantity": 200,
  "unit": "KG",
  "transactionUserId": 1,
  "workOrderId": 1,
  "referenceNo": "WO-001",
  "remarks": "작업지시 자재 출고"
}
```

**트랜잭션 타입**:
- `IN_RECEIVE`: 입고 (입하)
- `IN_PRODUCTION`: 입고 (생산)
- `IN_RETURN`: 입고 (반품)
- `OUT_ISSUE`: 출고 (불출)
- `OUT_SCRAP`: 출고 (스크랩)
- `MOVE`: 창고 이동
- `ADJUST`: 재고 조정

### 4.4 트랜잭션 승인
```http
POST /inventory-transactions/{id}/approve?approverId=1
Authorization: Bearer {token}
X-Tenant-ID: {tenantId}
```

### 4.5 트랜잭션 거부
```http
POST /inventory-transactions/{id}/reject?approverId=1&reason=수량불일치
Authorization: Bearer {token}
X-Tenant-ID: {tenantId}
```

---

## 5. 권한 요구사항

### 창고 관리
- **읽기**: 모든 인증 사용자
- **쓰기**: `ADMIN`, `WAREHOUSE_MANAGER`

### 재고 관리
- **읽기**: 모든 인증 사용자
- **예약/해제**: `ADMIN`, `PRODUCTION_MANAGER`, `WAREHOUSE_MANAGER`

### 입하 관리
- **읽기**: 모든 인증 사용자
- **생성/수정**: `ADMIN`, `WAREHOUSE_MANAGER`, `INVENTORY_CLERK`
- **완료/취소**: `ADMIN`, `WAREHOUSE_MANAGER`

### 재고 트랜잭션
- **읽기**: 모든 인증 사용자
- **생성**: `ADMIN`, `INVENTORY_MANAGER`, `WAREHOUSE_MANAGER`
- **승인/거부**: `ADMIN`, `INVENTORY_MANAGER`, `WAREHOUSE_MANAGER`

---

## 6. 에러 코드

| Code | HTTP Status | Message |
|------|-------------|---------|
| WH11000 | 404 | 창고를 찾을 수 없습니다. |
| WH11001 | 409 | 이미 존재하는 창고입니다. |
| LT11100 | 404 | LOT을 찾을 수 없습니다. |
| IV11200 | 404 | 재고를 찾을 수 없습니다. |
| IV11201 | 400 | 재고가 부족합니다. |
| GR15200 | 404 | 입하를 찾을 수 없습니다. |
| GR15201 | 409 | 이미 존재하는 입하입니다. |
| PI15102 | 404 | 구매 주문 항목을 찾을 수 없습니다. |

---

## 7. Postman Collection 예제

```json
{
  "info": {
    "name": "SDS MES - WMS Module",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Warehouses",
      "item": [
        {
          "name": "Get All Warehouses",
          "request": {
            "method": "GET",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{accessToken}}"
              },
              {
                "key": "X-Tenant-ID",
                "value": "{{tenantId}}"
              }
            ],
            "url": "{{baseUrl}}/warehouses"
          }
        }
      ]
    }
  ]
}
```

---

**작성일**: 2026-01-24
**버전**: 1.0
**작성자**: Moon Myung-seop
