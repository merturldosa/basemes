# WMS 실사 관리 가이드

## 📋 목차
1. [개요](#개요)
2. [실사 프로세스](#실사-프로세스)
3. [API 엔드포인트](#api-엔드포인트)
4. [사용 시나리오](#사용-시나리오)
5. [QR 코드 통합](#qr-코드-통합)

---

## 개요

### 실사 관리란?
실사(Physical Inventory)는 창고의 실제 재고와 시스템 재고를 비교하여 차이를 파악하고 조정하는 프로세스입니다.

### 주요 기능
- ✅ **실사 계획 자동 생성** - 창고의 현재 재고를 기준으로 실사 항목 자동 구성
- ✅ **실사 수량 입력** - 실제 재고 수량 입력 (수동 또는 QR 스캔)
- ✅ **차이 분석** - 시스템 재고와 실제 재고 차이 자동 계산
- ✅ **재고 조정 승인** - 차이 발생 시 재고 조정 트랜잭션 생성 및 승인
- ✅ **실사 이력 관리** - 모든 실사 결과 기록 및 추적

### 실사 상태
| 상태 | 설명 |
|------|------|
| `PLANNED` | 실사 계획 생성됨 |
| `IN_PROGRESS` | 실사 진행 중 (일부 항목 실사 완료) |
| `COMPLETED` | 실사 완료 (모든 항목 실사 완료) |
| `CANCELLED` | 실사 취소 |

### 조정 상태
| 상태 | 설명 |
|------|------|
| `NOT_REQUIRED` | 조정 불필요 (차이 없음) |
| `PENDING` | 조정 승인 대기 |
| `APPROVED` | 조정 승인됨 (재고 반영 완료) |
| `REJECTED` | 조정 거부됨 |

---

## 실사 프로세스

### 1단계: 실사 계획 생성
```
창고 선택 → 실사 일자 설정 → 자동 항목 생성
```

**특징**:
- 창고의 모든 재고 자동 조회
- 재고가 있는 항목만 실사 대상
- 시스템 재고 수량 자동 설정

### 2단계: 실사 수행
```
실사 항목 조회 → 실제 수량 입력 → 차이 자동 계산
```

**방법**:
- **수동 입력**: 실사자가 직접 수량 입력
- **QR 스캔**: QR 코드 스캔 후 수량 입력 (권장)

### 3단계: 실사 완료
```
모든 항목 실사 완료 → 차이 분석 → 조정 필요 항목 확인
```

### 4단계: 재고 조정
```
조정 필요 항목 → 승인/거부 → 재고 반영
```

**워크플로우**:
1. 차이 발생 항목 → 조정 상태 `PENDING`
2. 승인자 검토
3. 승인 → 재고 조정 트랜잭션 생성 → 재고 반영
4. 거부 → 재실사 또는 원인 조사

---

## API 엔드포인트

### 1. 실사 계획 생성
```http
POST /api/physical-inventories
Authorization: Bearer {token}
X-Tenant-ID: {tenantId}
Content-Type: application/json

{
  "warehouseId": 1,
  "inventoryDate": "2026-01-24T09:00:00",
  "plannedByUserId": 1,
  "remarks": "정기 실사"
}
```

**Response**:
```json
{
  "success": true,
  "message": "실사 계획이 생성되었습니다",
  "data": {
    "physicalInventoryId": 1,
    "inventoryNo": "PI-20260124-0001",
    "inventoryDate": "2026-01-24T09:00:00",
    "warehouseId": 1,
    "warehouseCode": "WH-RAW",
    "warehouseName": "원자재 창고",
    "inventoryStatus": "PLANNED",
    "items": [
      {
        "physicalInventoryItemId": 1,
        "productCode": "RAW-001",
        "productName": "원자재-001",
        "lotNo": "LOT-20260124-001",
        "location": "A-1-2-3",
        "systemQuantity": 1000.000,
        "countedQuantity": null,
        "differenceQuantity": null,
        "adjustmentStatus": "NOT_REQUIRED"
      }
    ],
    "statistics": {
      "totalItems": 50,
      "countedItems": 0,
      "itemsRequiringAdjustment": 0,
      "approvedAdjustments": 0,
      "rejectedAdjustments": 0
    }
  }
}
```

### 2. 실사 수량 입력
```http
POST /api/physical-inventories/{physicalInventoryId}/count
Content-Type: application/json

{
  "itemId": 1,
  "countedQuantity": 980.000,
  "countedByUserId": 2
}
```

**Response**:
```json
{
  "success": true,
  "message": "실사 수량이 입력되었습니다",
  "data": {
    "physicalInventoryId": 1,
    "inventoryStatus": "IN_PROGRESS",
    "items": [
      {
        "physicalInventoryItemId": 1,
        "systemQuantity": 1000.000,
        "countedQuantity": 980.000,
        "differenceQuantity": -20.000,
        "adjustmentStatus": "PENDING"
      }
    ],
    "statistics": {
      "totalItems": 50,
      "countedItems": 1,
      "itemsRequiringAdjustment": 1
    }
  }
}
```

### 3. 실사 완료
```http
POST /api/physical-inventories/{physicalInventoryId}/complete
```

**조건**:
- 모든 항목의 실사 수량이 입력되어야 함

### 4. 재고 조정 승인
```http
POST /api/physical-inventories/{physicalInventoryId}/items/{itemId}/approve?approverId=1
```

**결과**:
- 재고 조정 트랜잭션 생성 (`ADJUST` 타입)
- 자동 승인 및 재고 반영
- 조정 상태: `PENDING` → `APPROVED`

### 5. 재고 조정 거부
```http
POST /api/physical-inventories/{physicalInventoryId}/items/{itemId}/reject
  ?approverId=1
  &reason=재실사필요
```

### 6. 실사 목록 조회
```http
GET /api/physical-inventories
```

### 7. 실사 상세 조회
```http
GET /api/physical-inventories/{physicalInventoryId}
```

---

## 사용 시나리오

### 시나리오 1: 정기 실사 (월말 실사)

#### 1단계: 실사 계획 생성
```http
POST /api/physical-inventories
{
  "warehouseId": 1,
  "inventoryDate": "2026-01-31T18:00:00",
  "plannedByUserId": 1,
  "remarks": "2026년 1월 월말 정기 실사"
}
```

**결과**:
- 실사 번호: `PI-20260131-0001`
- 원자재 창고의 모든 재고 (50개 항목) 자동 생성
- 상태: `PLANNED`

#### 2단계: 실사 수행 (QR 스캔 방식)
작업자가 창고를 돌며 QR 코드 스캔:

```
1. QR 코드 스캔
   GET /api/barcodes/lot/number/LOT-20260124-001/qrcode

2. LOT 정보 확인
   LOT: LOT-20260124-001
   제품: RAW-001 (원자재-001)
   시스템 재고: 1000 KG

3. 실제 재고 카운트: 980 KG

4. 실사 수량 입력
   POST /api/physical-inventories/1/count
   {
     "itemId": 1,
     "countedQuantity": 980.000,
     "countedByUserId": 2
   }

5. 차이 계산: -20 KG (부족)
   조정 상태: PENDING
```

이 과정을 50개 항목 모두 반복

#### 3단계: 실사 완료
```http
POST /api/physical-inventories/1/complete
```

**결과**:
- 상태: `IN_PROGRESS` → `COMPLETED`
- 조정 필요 항목: 5개 (차이 발생)

#### 4단계: 재고 조정 승인
```http
POST /api/physical-inventories/1/items/1/approve?approverId=1
```

**재고 조정 트랜잭션**:
```
트랜잭션 번호: ADJ-PI-20260131-0001-1
트랜잭션 타입: ADJUST
수량: 20 KG (절대값)
참조 번호: PI-20260131-0001
비고: 실사 조정 - 시스템: 1000, 실사: 980, 차이: -20
```

**재고 업데이트**:
- 원자재 창고 RAW-001: `1000 → 980`

---

### 시나리오 2: 긴급 실사 (재고 부정 의심)

#### 배경
고객 주문 출하 시 재고 부족 발생 → 긴급 실사 필요

#### 프로세스
1. **특정 제품만 실사 계획 생성** (전체 창고 X)
   - 실사 계획 생성 후 불필요한 항목 제거
   - 또는 수동으로 항목 추가

2. **즉시 실사 수행**
   - QR 스캔으로 신속하게 처리
   - 차이 발견 즉시 원인 조사

3. **조정 승인**
   - 차이 원인 파악 후 승인
   - 비고에 원인 기록

4. **재발 방지**
   - 실사 이력 분석
   - 재고 관리 프로세스 개선

---

### 시나리오 3: 순환 실사 (Cycle Count)

#### 개념
창고 전체를 한 번에 실사하지 않고, 매일 일부 구역씩 순환 실사

#### 계획
- **월요일**: A구역 (원자재 1-20번)
- **화요일**: B구역 (원자재 21-40번)
- **수요일**: C구역 (원자재 41-60번)
- **목요일**: D구역 (완제품 1-20번)
- **금요일**: E구역 (완제품 21-40번)

#### 장점
- 작업 부담 분산
- 지속적인 재고 정확도 유지
- 창고 운영 중단 최소화

---

## QR 코드 통합

### QR 스캔 기반 실사 워크플로우

#### 1. QR 코드 스캔
```http
POST /api/barcodes/scan
{
  "qrData": "LOT:LOT-20260124-001|PRODUCT:RAW-001|...",
  "scanLocation": "A구역",
  "scanPurpose": "INVENTORY_CHECK"
}
```

**Response**:
```json
{
  "success": true,
  "data": {
    "lotId": 1,
    "lotNo": "LOT-20260124-001",
    "productCode": "RAW-001",
    "productName": "원자재-001",
    "currentQuantity": 1000.000,
    "unit": "KG"
  }
}
```

#### 2. 실사 항목 매칭
LOT 번호로 실사 항목 찾기

#### 3. 실사 수량 입력
```http
POST /api/physical-inventories/1/count
{
  "itemId": 1,
  "countedQuantity": 980.000,
  "countedByUserId": 2
}
```

### 모바일 앱 UI 흐름
```
1. 실사 계획 선택 화면
   ↓
2. QR 스캔 화면
   ↓
3. LOT 정보 표시
   - 제품명: 원자재-001
   - LOT: LOT-20260124-001
   - 시스템 재고: 1000 KG
   ↓
4. 실사 수량 입력 화면
   [실사 수량: _____ KG] [입력]
   ↓
5. 차이 확인 화면
   시스템: 1000 KG
   실사:    980 KG
   차이:    -20 KG ⚠️
   [다음 항목]
```

---

## 실사 통계 및 분석

### 실사 결과 통계
```json
{
  "statistics": {
    "totalItems": 50,
    "countedItems": 50,
    "itemsRequiringAdjustment": 5,
    "approvedAdjustments": 4,
    "rejectedAdjustments": 1,
    "accuracyRate": 90.0,
    "totalDifferenceValue": -5000.00
  }
}
```

### 재고 정확도 계산
```
정확도 = (차이 없는 항목 수 / 전체 항목 수) × 100
       = (45 / 50) × 100
       = 90%
```

### 차이 분석
| 항목 | 시스템 재고 | 실사 재고 | 차이 | 차이율 |
|------|------------|----------|------|-------|
| RAW-001 | 1000 | 980 | -20 | -2% |
| RAW-002 | 500 | 520 | +20 | +4% |
| RAW-003 | 800 | 750 | -50 | -6.25% |

---

## 권한 설정

### API 권한
| 기능 | 권한 |
|------|------|
| 실사 계획 생성 | WAREHOUSE_MANAGER, INVENTORY_MANAGER |
| 실사 수량 입력 | WAREHOUSE_MANAGER, WAREHOUSE_OPERATOR |
| 실사 완료 | WAREHOUSE_MANAGER |
| 재고 조정 승인/거부 | WAREHOUSE_MANAGER, INVENTORY_MANAGER |
| 실사 조회 | 모든 인증 사용자 |

---

## 모범 사례

### 1. 실사 준비
- ✅ 실사 전 재고 이동 최소화
- ✅ 실사 구역 명확하게 구분
- ✅ QR 코드 라벨 상태 점검

### 2. 실사 수행
- ✅ 2인 1조 실사 (1인 카운트, 1인 기록)
- ✅ QR 스캔 우선 사용
- ✅ 차이 발견 시 즉시 재확인

### 3. 조정 승인
- ✅ 차이 원인 파악 후 승인
- ✅ 비고에 상세 기록
- ✅ 일정 금액 이상은 상위 관리자 승인

### 4. 사후 관리
- ✅ 실사 결과 분석
- ✅ 정확도 저하 원인 조사
- ✅ 재발 방지 대책 수립

---

**작성일**: 2026-01-24
**버전**: 1.0
**작성자**: Moon Myung-seop
