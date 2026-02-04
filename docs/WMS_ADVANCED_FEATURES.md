# WMS 모듈 고급 기능 가이드

## 📋 목차
1. [FIFO/FEFO LOT 선택 전략](#1-fifofefo-lot-선택-전략)
2. [바코드/QR 코드 통합](#2-바코드qr-코드-통합)
3. [API 엔드포인트](#3-api-엔드포인트)
4. [사용 시나리오](#4-사용-시나리오)

---

## 1. FIFO/FEFO LOT 선택 전략

### 개요
재고 출고 시 LOT을 자동으로 선택하는 전략을 제공합니다.

### 전략 종류

#### 1.1 FIFO (First-In-First-Out)
**설명**: 가장 먼저 입고된 LOT부터 출고
**적용 시나리오**: 일반 원자재, 완제품
**로직**:
- LOT 생성일(`created_at`) 기준 오름차순 정렬
- 가용 재고가 있는 LOT만 선택
- 필요 수량이 채워질 때까지 순차 할당

#### 1.2 FEFO (First-Expired-First-Out)
**설명**: 유효기간이 가장 빠른 LOT부터 출고
**적용 시나리오**: 유효기간 있는 원자재, 식품, 의약품
**로직**:
- LOT 유효기간(`expiry_date`) 기준 오름차순 정렬
- 유효기간 없는 LOT은 FIFO 방식으로 후순위 처리
- 만료 임박 재고 우선 출고

#### 1.3 특정 LOT 지정
**설명**: 사용자가 직접 LOT 선택
**적용 시나리오**: 특수 자재, 고객 지정 LOT
**로직**:
- LOT ID 직접 지정
- 가용 재고 검증
- 단일 LOT 할당

### API 엔드포인트

#### FIFO 선택
```http
POST /api/lot-selection/fifo
Content-Type: application/json
Authorization: Bearer {token}
X-Tenant-ID: {tenantId}

{
  "warehouseId": 1,
  "productId": 1,
  "requiredQuantity": 200.0,
  "workOrderId": 1  // 선택사항
}
```

**Response**:
```json
{
  "success": true,
  "message": "FIFO LOT 선택 완료",
  "data": [
    {
      "lotId": 1,
      "lotNo": "LOT-20260124-001",
      "allocatedQuantity": 150.0,
      "availableQuantity": 500.0,
      "expiryDate": "2027-01-24"
    },
    {
      "lotId": 2,
      "lotNo": "LOT-20260120-002",
      "allocatedQuantity": 50.0,
      "availableQuantity": 300.0,
      "expiryDate": "2027-01-20"
    }
  ]
}
```

#### FEFO 선택
```http
POST /api/lot-selection/fefo
Content-Type: application/json

{
  "warehouseId": 1,
  "productId": 1,
  "requiredQuantity": 200.0,
  "salesOrderId": 1  // 선택사항
}
```

#### 특정 LOT 선택
```http
POST /api/lot-selection/specific
Content-Type: application/json

{
  "warehouseId": 1,
  "productId": 1,
  "lotId": 5,
  "requiredQuantity": 100.0
}
```

#### 만료 예정 LOT 조회
```http
GET /api/lot-selection/expiring?daysUntilExpiry=30
```

**Response**:
```json
{
  "success": true,
  "message": "30일 내 만료 예정 LOT 5건 조회 완료",
  "data": [
    {
      "lotId": 3,
      "lotNo": "LOT-20260101-003",
      "productCode": "RAW-001",
      "productName": "원자재-001",
      "expiryDate": "2026-02-15",
      "currentQuantity": 150.0,
      "qualityStatus": "PASSED"
    }
  ]
}
```

---

## 2. 바코드/QR 코드 통합

### 개요
LOT 추적성을 향상시키기 위한 QR 코드 생성 및 스캔 기능

### 2.1 QR 코드 데이터 포맷
```
LOT:{lotNo}|PRODUCT:{productCode}|PRODUCT_NAME:{productName}|QTY:{currentQuantity}|EXPIRY:{expiryDate}|STATUS:{qualityStatus}|UNIT:{unit}
```

**예시**:
```
LOT:LOT-20260124-001|PRODUCT:RAW-001|PRODUCT_NAME:원자재-001|QTY:1000.000|EXPIRY:2027-01-24|STATUS:PASSED|UNIT:KG
```

### 2.2 QR 코드 생성

#### LOT ID로 생성
```http
GET /api/barcodes/lot/{lotId}/qrcode
Authorization: Bearer {token}
X-Tenant-ID: {tenantId}
```

**Response**:
```json
{
  "success": true,
  "message": "QR 코드 생성 완료",
  "data": {
    "lotId": 1,
    "lotNo": "LOT-20260124-001",
    "qrCodeImage": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA..."
  }
}
```

#### LOT 번호로 생성
```http
GET /api/barcodes/lot/number/LOT-20260124-001/qrcode
```

#### 일반 텍스트 QR 코드 생성
```http
POST /api/barcodes/qrcode/generate
Content-Type: application/json

{
  "data": "임의의 텍스트"
}
```

### 2.3 QR 코드 스캔

#### 스캔 API
```http
POST /api/barcodes/scan
Content-Type: application/json
Authorization: Bearer {token}
X-Tenant-ID: {tenantId}

{
  "qrData": "LOT:LOT-20260124-001|PRODUCT:RAW-001|...",
  "scanLocation": "입하구역",
  "scanPurpose": "RECEIVING"
}
```

**Response**:
```json
{
  "success": true,
  "message": "LOT 정보 조회 완료",
  "data": {
    "lotId": 1,
    "lotNo": "LOT-20260124-001",
    "productCode": "RAW-001",
    "productName": "원자재-001",
    "currentQuantity": 1000.000,
    "expiryDate": "2027-01-24",
    "qualityStatus": "PASSED",
    "unit": "KG",
    "isActive": true
  }
}
```

### 2.4 QR 코드 설정
- **크기**: 300 x 300 픽셀
- **포맷**: PNG
- **에러 정정**: High (30%)
- **인코딩**: UTF-8
- **출력**: Base64 Data URI (HTML `<img>` 태그에 직접 사용 가능)

---

## 3. API 엔드포인트

### LOT 선택 API
| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| POST | `/api/lot-selection/fifo` | FIFO LOT 선택 | WAREHOUSE_MANAGER, PRODUCTION_MANAGER |
| POST | `/api/lot-selection/fefo` | FEFO LOT 선택 | WAREHOUSE_MANAGER, PRODUCTION_MANAGER |
| POST | `/api/lot-selection/specific` | 특정 LOT 선택 | WAREHOUSE_MANAGER, PRODUCTION_MANAGER |
| GET | `/api/lot-selection/expiring` | 만료 예정 LOT 조회 | WAREHOUSE_MANAGER, QMS_MANAGER |

### 바코드 API
| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| GET | `/api/barcodes/lot/{lotId}/qrcode` | LOT QR 코드 생성 | 모든 인증 사용자 |
| GET | `/api/barcodes/lot/number/{lotNo}/qrcode` | LOT 번호로 QR 생성 | 모든 인증 사용자 |
| POST | `/api/barcodes/scan` | QR 코드 스캔 | WAREHOUSE_MANAGER, WAREHOUSE_OPERATOR |
| POST | `/api/barcodes/qrcode/generate` | 텍스트 QR 생성 | 모든 인증 사용자 |

---

## 4. 사용 시나리오

### 시나리오 1: 생산 자재 출고 (FIFO)

**단계**:
1. 작업 지시 생성 (WO-001, 제품: FG-001, 수량: 100)
2. BOM 기준 필요 원자재 계산 (RAW-001 x 200)
3. **FIFO LOT 선택 API 호출**:
   ```http
   POST /api/lot-selection/fifo
   {
     "warehouseId": 1,
     "productId": 1,
     "requiredQuantity": 200,
     "workOrderId": 1
   }
   ```
4. 응답 받은 LOT 목록으로 재고 출고 트랜잭션 생성
5. 각 LOT별로 출고 처리

**결과**:
- 가장 오래된 LOT부터 자동 출고
- 재고 회전율 향상
- 장기 재고 방지

### 시나리오 2: 식품 출하 (FEFO)

**단계**:
1. 판매 주문 생성 (SO-001, 제품: FOOD-001, 수량: 50)
2. **FEFO LOT 선택 API 호출**:
   ```http
   POST /api/lot-selection/fefo
   {
     "warehouseId": 3,
     "productId": 2,
     "requiredQuantity": 50,
     "salesOrderId": 1
   }
   ```
3. 유효기간 빠른 LOT 우선 출하
4. 품질 검사 (출하 전 검사)
5. 출하 완료

**결과**:
- 유효기간 만료 전 출고
- 제품 폐기 최소화
- 고객 만족도 향상

### 시나리오 3: 입하 후 QR 코드 발행

**단계**:
1. 구매 주문에서 입하 생성
2. 입하 완료 시 LOT 생성 (LOT-20260124-001)
3. **QR 코드 생성 API 호출**:
   ```http
   GET /api/barcodes/lot/1/qrcode
   ```
4. 응답 받은 Base64 이미지를 라벨 프린터로 인쇄
5. 실물 포장에 QR 코드 라벨 부착

**결과**:
- LOT 추적성 확보
- 스캔을 통한 빠른 입고/출고 처리
- 실물 재고 vs 시스템 재고 정합성

### 시나리오 4: 창고 실사 (QR 스캔)

**단계**:
1. 창고 작업자가 모바일 장치로 QR 코드 스캔
2. **QR 스캔 API 호출**:
   ```http
   POST /api/barcodes/scan
   {
     "qrData": "LOT:LOT-20260124-001|...",
     "scanLocation": "A동-1층-A구역",
     "scanPurpose": "INVENTORY_CHECK"
   }
   ```
3. LOT 정보 조회 (제품, 수량, 유효기간 등)
4. 실물 수량과 시스템 수량 비교
5. 차이 발생 시 재고 조정 트랜잭션 생성

**결과**:
- 정확한 재고 파악
- 실사 작업 시간 단축
- 인적 오류 방지

### 시나리오 5: 만료 예정 재고 관리

**단계**:
1. 매일 배치 작업으로 **만료 예정 LOT 조회**:
   ```http
   GET /api/lot-selection/expiring?daysUntilExpiry=30
   ```
2. 30일 내 만료 예정 LOT 목록 반환
3. 우선 출고 대상으로 지정
4. 출하 계획에 FEFO 전략 적용
5. 만료 임박 재고 조기 소진

**결과**:
- 폐기 비용 절감
- 재고 가치 최대화
- 효율적인 재고 회전

---

## 5. 기술 스택

### Backend
- **QR 생성**: ZXing (Zebra Crossing) 3.5.2
- **이미지 포맷**: PNG, Base64 인코딩
- **에러 정정**: Level H (30%)

### 라이브러리
```xml
<dependency>
    <groupId>com.google.zxing</groupId>
    <artifactId>core</artifactId>
    <version>3.5.2</version>
</dependency>

<dependency>
    <groupId>com.google.zxing</groupId>
    <artifactId>javase</artifactId>
    <version>3.5.2</version>
</dependency>
```

---

## 6. 향후 확장 계획

### 6.1 프론트엔드 통합
- QR 코드 스캔 컴포넌트 (웹캠 사용)
- LOT 선택 다이얼로그 (FIFO/FEFO 전략 선택)
- QR 코드 인쇄 기능

### 6.2 모바일 앱
- 창고 작업자용 모바일 앱
- 카메라로 QR 스캔
- 오프라인 모드 지원

### 6.3 고급 분석
- LOT 회전율 분석
- 재고 연령 분석
- 만료 예측 알고리즘

---

**작성일**: 2026-01-24
**버전**: 1.0
**작성자**: Moon Myung-seop
