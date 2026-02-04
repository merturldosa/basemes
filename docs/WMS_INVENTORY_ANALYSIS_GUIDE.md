# WMS 재고 분석 가이드

## 📋 목차
1. [개요](#개요)
2. [분석 기능](#분석-기능)
3. [API 엔드포인트](#api-엔드포인트)
4. [사용 시나리오](#사용-시나리오)
5. [대시보드 활용](#대시보드-활용)

---

## 개요

### 재고 분석이란?
재고 분석은 재고 데이터를 다양한 관점에서 분석하여 재고 효율성을 측정하고 최적화 기회를 발견하는 프로세스입니다.

### 주요 목적
- 📊 **재고 효율성 측정** - 재고 회전율, 재고 일수 등
- 💰 **재고 비용 최적화** - 과다 재고, 불용 재고 파악
- ⚠️ **리스크 관리** - 만료 임박, 장기 보관 재고 식별
- 📈 **의사결정 지원** - 데이터 기반 재고 관리 전략 수립

---

## 분석 기능

### 1. 재고 회전율 분석 (Inventory Turnover)

#### 개념
재고 회전율 = 출고 수량 / 평균 재고

재고가 얼마나 빠르게 순환하는지 측정합니다. 높을수록 재고 효율성이 좋습니다.

#### 활용
- **높은 회전율 (5 이상)**: 빠른 순환, 효율적 재고 관리
- **보통 회전율 (2-5)**: 정상적인 재고 수준
- **낮은 회전율 (2 미만)**: 과다 재고, 개선 필요

#### API
```http
GET /api/inventory-analysis/turnover
  ?startDate=2026-01-01T00:00:00
  &endDate=2026-01-31T23:59:59
```

**Response**:
```json
{
  "success": true,
  "message": "재고 회전율 분석 완료 - 50개 제품 분석",
  "data": [
    {
      "productId": 1,
      "productCode": "RAW-001",
      "productName": "원자재-001",
      "totalOutboundQuantity": 5000.000,
      "averageInventory": 1000.000,
      "turnoverRatio": 5.00,
      "periodDays": 30
    },
    {
      "productId": 2,
      "productCode": "RAW-002",
      "productName": "원자재-002",
      "totalOutboundQuantity": 800.000,
      "averageInventory": 500.000,
      "turnoverRatio": 1.60,
      "periodDays": 30
    }
  ]
}
```

---

### 2. 불용 재고 분석 (Obsolete Inventory)

#### 개념
지정된 기간 동안 출고가 없는 재고를 식별합니다.

#### 임계값
- **30일**: 단기 불용 재고
- **90일**: 중기 불용 재고 (기본값)
- **180일**: 장기 불용 재고

#### API
```http
GET /api/inventory-analysis/obsolete?daysThreshold=90
```

**Response**:
```json
{
  "success": true,
  "message": "90일 이상 미출고 재고 15건 발견",
  "data": [
    {
      "productId": 3,
      "productCode": "RAW-003",
      "productName": "원자재-003",
      "warehouseId": 1,
      "warehouseCode": "WH-RAW",
      "warehouseName": "원자재 창고",
      "lotId": 5,
      "lotNo": "LOT-20251020-001",
      "totalQuantity": 500.000,
      "lastTransactionDate": "2025-10-20T10:00:00",
      "lastTransactionType": "IN_RECEIVE",
      "daysSinceLastTransaction": 96
    }
  ]
}
```

#### 조치 방법
1. **판매 촉진**: 할인 판매, 특별 프로모션
2. **용도 전환**: 다른 제품 생산에 활용
3. **폐기/기부**: 가치 없는 재고 처분
4. **반품**: 공급업체 반품 협상

---

### 3. 재고 연령 분석 (Inventory Aging)

#### 개념
LOT별 재고 연령 (생성일로부터 경과 일수) 및 유효기간 분석

#### 연령 구간
- **0-30일**: 신선 재고
- **31-60일**: 정상 재고
- **61-90일**: 주의 재고
- **91-180일**: 경고 재고
- **180일 초과**: 위험 재고

#### API
```http
GET /api/inventory-analysis/aging
```

**Response**:
```json
{
  "success": true,
  "message": "재고 연령 분석 완료 - 100개 LOT 분석, 만료 임박 8건",
  "data": [
    {
      "productId": 1,
      "productCode": "RAW-001",
      "productName": "원자재-001",
      "lotId": 1,
      "lotNo": "LOT-20250801-001",
      "lotCreatedDate": "2025-08-01",
      "expiryDate": "2026-02-01",
      "totalQuantity": 200.000,
      "ageInDays": 176,
      "ageCategory": "91-180일",
      "daysToExpiry": 8,
      "nearExpiry": true
    }
  ]
}
```

#### 조치 방법
- **만료 임박 (30일 이내)**: FEFO 전략으로 우선 출고
- **장기 보관 (180일 초과)**: 품질 재검사 또는 폐기 검토

---

### 4. ABC 분석

#### 개념
파레토 원칙 (80-20 법칙) 적용:
- **A등급**: 상위 20% 제품 (가치의 80%)
- **B등급**: 중위 30% 제품 (가치의 15%)
- **C등급**: 하위 50% 제품 (가치의 5%)

#### 활용
| 등급 | 관리 전략 |
|------|-----------|
| **A등급** | 엄격한 재고 관리, 정확한 수요 예측, 안전 재고 최소화 |
| **B등급** | 표준 재고 관리, 주기적 검토 |
| **C등급** | 간소화된 관리, 대량 주문으로 비용 절감 |

#### API
```http
GET /api/inventory-analysis/abc
```

**Response**:
```json
{
  "success": true,
  "message": "ABC 분석 완료 - A등급: 10, B등급: 15, C등급: 25",
  "data": {
    "items": [
      {
        "productId": 1,
        "productCode": "RAW-001",
        "productName": "원자재-001",
        "totalQuantity": 1000.000,
        "totalValue": 1000.000,
        "valuePercentage": 25.00,
        "cumulativePercentage": 25.00,
        "abcClass": "A",
        "rank": 1
      },
      {
        "productId": 2,
        "productCode": "RAW-002",
        "productName": "원자재-002",
        "totalQuantity": 800.000,
        "totalValue": 800.000,
        "valuePercentage": 20.00,
        "cumulativePercentage": 45.00,
        "abcClass": "A",
        "rank": 2
      }
    ],
    "statistics": {
      "totalProducts": 50,
      "classACount": 10,
      "classBCount": 15,
      "classCCount": 25
    }
  }
}
```

---

### 5. 재고 이동 추이 분석 (Inventory Trend)

#### 개념
일별 입출고 추이를 시계열로 분석

#### 활용
- 재고 이동 패턴 파악
- 수요 예측
- 계절성 분석

#### API
```http
GET /api/inventory-analysis/trend?days=30
```

**Response**:
```json
{
  "success": true,
  "message": "최근 30일 재고 이동 추이 분석 완료",
  "data": [
    {
      "date": "2026-01-24",
      "inboundQuantity": 5000.000,
      "inboundCount": 3,
      "outboundQuantity": 3000.000,
      "outboundCount": 8,
      "netChange": 2000.000
    },
    {
      "date": "2026-01-23",
      "inboundQuantity": 2000.000,
      "inboundCount": 1,
      "outboundQuantity": 4000.000,
      "outboundCount": 10,
      "netChange": -2000.000
    }
  ]
}
```

---

## API 엔드포인트

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/inventory-analysis/turnover` | 재고 회전율 분석 |
| GET | `/api/inventory-analysis/obsolete` | 불용 재고 분석 |
| GET | `/api/inventory-analysis/aging` | 재고 연령 분석 |
| GET | `/api/inventory-analysis/abc` | ABC 분석 |
| GET | `/api/inventory-analysis/trend` | 재고 이동 추이 |
| GET | `/api/inventory-analysis/dashboard` | 통합 대시보드 |

### 권한
모든 분석 API: `ADMIN`, `WAREHOUSE_MANAGER`, `INVENTORY_MANAGER`

---

## 사용 시나리오

### 시나리오 1: 월말 재고 분석 보고서

#### 목표
월말 경영진에게 재고 현황 보고

#### 프로세스
```
1. 통합 대시보드 조회
   GET /api/inventory-analysis/dashboard?trendDays=30&obsoleteDays=90

2. 주요 지표 확인
   - 전체 재고 가치
   - A등급 제품 수
   - 불용 재고 건수
   - 만료 임박 재고 건수

3. 상세 분석
   - 재고 회전율 낮은 제품 → 개선 계획 수립
   - 불용 재고 → 처분 계획 수립
   - 만료 임박 → 긴급 출고 계획

4. 보고서 작성
   - 요약 통계
   - 개선 필요 항목
   - 조치 계획
```

---

### 시나리오 2: 재고 최적화 프로젝트

#### 목표
재고 수준을 30% 감축하면서 서비스 수준 유지

#### 1단계: 현황 분석
```http
GET /api/inventory-analysis/abc
GET /api/inventory-analysis/turnover?startDate=...&endDate=...
```

**결과**:
- A등급 제품: 10개 (회전율 평균 5.2)
- B등급 제품: 15개 (회전율 평균 2.8)
- C등급 제품: 25개 (회전율 평균 0.9)

#### 2단계: 전략 수립
| 등급 | 현재 재고 | 목표 재고 | 전략 |
|------|-----------|-----------|------|
| A등급 | 100M | 80M (-20%) | JIT 발주 강화 |
| B등급 | 50M | 35M (-30%) | 발주 주기 조정 |
| C등급 | 30M | 15M (-50%) | 통합 발주, 대량 할인 |

#### 3단계: 실행
- 불용 재고 처분
- 안전 재고 수준 재설정
- 발주 정책 변경

#### 4단계: 모니터링
매주 분석 대시보드로 진행 상황 추적

---

### 시나리오 3: 유효기간 관리

#### 배경
식품/의약품 제조업체, 유효기간 관리 중요

#### 프로세스
```
1. 매일 재고 연령 분석 실행
   GET /api/inventory-analysis/aging

2. 만료 임박 재고 (30일 이내) 필터링
   nearExpiry: true

3. 긴급 조치
   - FEFO 전략 적용
   - 영업팀에 할인 판매 요청
   - 필요 시 타 제품 생산에 투입

4. 주간 보고
   - 만료 임박 재고 현황
   - 조치 결과
   - 폐기 재고 금액
```

---

### 시나리오 4: 불용 재고 정리

#### 목표
분기별 불용 재고 정리

#### 1단계: 불용 재고 식별
```http
GET /api/inventory-analysis/obsolete?daysThreshold=90
```

**결과**: 15건 발견 (총 가치 50M)

#### 2단계: 분류
| 카테고리 | 건수 | 조치 |
|----------|------|------|
| 판매 가능 | 5건 | 할인 판매 (30% 할인) |
| 용도 전환 | 3건 | 다른 제품 생산에 투입 |
| 반품 가능 | 2건 | 공급업체 협상 |
| 폐기 대상 | 5건 | 폐기 승인 |

#### 3단계: 실행 및 추적
- 판매 프로모션 진행
- 용도 전환 BOM 작성
- 반품 처리
- 폐기 절차 진행

#### 4단계: 결과 분석
- 회수 금액: 35M (70%)
- 폐기 손실: 15M (30%)
- 창고 공간 확보: 20㎡

---

## 대시보드 활용

### 통합 대시보드 API
```http
GET /api/inventory-analysis/dashboard?trendDays=30&obsoleteDays=90
```

### 대시보드 구성
```json
{
  "success": true,
  "message": "재고 분석 대시보드 조회 완료",
  "data": {
    "turnoverAnalysis": [...],      // 상위 10개 제품
    "obsoleteInventory": [...],     // 상위 10개 불용 재고
    "agingAnalysis": [...],         // 상위 10개 오래된 LOT
    "abcAnalysis": [...],           // 상위 10개 제품
    "trendAnalysis": [...],         // 30일 추이
    "summary": {
      "totalProducts": 50,
      "obsoleteItemsCount": 15,
      "nearExpiryItemsCount": 8,
      "classAProductsCount": 10
    }
  }
}
```

### 프론트엔드 활용

#### 1. 요약 카드
```
┌────────────────────────────────────────────────┐
│  재고 현황 요약                                  │
├────────────────────────────────────────────────┤
│  총 제품 수: 50                                 │
│  A등급 제품: 10 (20%)                           │
│  불용 재고: 15건                                │
│  만료 임박: 8건 ⚠️                              │
└────────────────────────────────────────────────┘
```

#### 2. 재고 회전율 차트 (Bar Chart)
```
RAW-001 ████████████████████ 5.2
RAW-002 ████████████ 3.0
RAW-003 ██████ 1.5
RAW-004 ████ 1.0
RAW-005 ██ 0.5
```

#### 3. 재고 이동 추이 (Line Chart)
```
입고 ━━━━  출고 ━ ━ ━  순증감 ▬▬▬
```

#### 4. ABC 분류 (Pie Chart)
```
A등급: 20% (10개)
B등급: 30% (15개)
C등급: 50% (25개)
```

---

## 분석 주기 권장사항

### 일별
- ✅ 재고 연령 분석 (만료 임박 체크)
- ✅ 재고 이동 추이

### 주별
- ✅ 불용 재고 분석
- ✅ 재고 회전율 분석

### 월별
- ✅ ABC 분석
- ✅ 통합 대시보드 보고서

### 분기별
- ✅ 재고 최적화 프로젝트
- ✅ 안전 재고 수준 재설정

---

## KPI (핵심 성과 지표)

### 재고 효율성
| KPI | 목표 | 측정 방법 |
|-----|------|-----------|
| 평균 재고 회전율 | 4 이상 | 재고 회전율 분석 |
| 불용 재고 비율 | 5% 이하 | 불용 재고 / 전체 재고 |
| 만료 재고 비율 | 1% 이하 | 만료 재고 / 전체 재고 |
| 재고 정확도 | 98% 이상 | 실사 결과 분석 |

### 재고 비용
| KPI | 목표 | 측정 방법 |
|-----|------|-----------|
| 재고 유지 비용 | 매출의 3% | 재고 가치 × 유지 비용율 |
| 재고 일수 (DOI) | 90일 이하 | 평균 재고 / 일평균 출고 |
| A등급 제품 비중 | 80% 이상 | ABC 분석 |

---

**작성일**: 2026-01-24
**버전**: 1.0
**작성자**: Moon Myung-seop
