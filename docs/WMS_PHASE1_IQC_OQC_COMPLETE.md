# WMS Phase 1 완료: IQC/OQC 의뢰 리스트 구현

**작성일**: 2026-01-24
**작성자**: Moon Myung-seop
**모듈**: 창고 관리 (WMS) - 품질 검사 연동
**Phase**: 1/8 (WMS 완성)

---

## 📋 구현 내용

### ✅ 1. IQC 의뢰 리스트 (Incoming Quality Control)

#### 백엔드
**파일**: `QualityInspectionController.java`
- 새 엔드포인트: `GET /api/quality-inspections/iqc-requests`
- 입고 품질 검사 의뢰 목록 조회
- inspection_type = "INCOMING" 필터링

**파일**: `GoodsReceiptService.java`
- `createIQCRequest()` 메서드 추가
- 입하 생성 시 자동 IQC 의뢰 생성
- inspection_status가 "PENDING"인 경우 실행
- 품질 기준서 자동 조회 및 연결
- 검사번호 자동 생성: `IQC-YYYYMMDD-0001`

#### 프론트엔드
**파일**: `IQCRequestsPage.tsx`
- 입고 품질 검사 의뢰 목록 UI
- 검사 결과 시각화 (PASS/FAIL/CONDITIONAL)
- 합격/불합격 수량 표시
- 검사 통계 요약 (합격률, 총 검사 건수)

**라우트**: `/warehouse/iqc-requests`

---

### ✅ 2. OQC 의뢰 리스트 (Outgoing Quality Control)

#### 백엔드
**파일**: `QualityInspectionController.java`
- 새 엔드포인트: `GET /api/quality-inspections/oqc-requests`
- 출하 품질 검사 의뢰 목록 조회
- inspection_type = "OUTGOING" 필터링

**파일**: `ShippingService.java`
- `createOQCRequest()` 메서드 추가
- 출하 생성 시 자동 OQC 의뢰 생성
- inspection_status가 "PENDING"인 경우 실행
- 품질 기준서 자동 조회 및 연결
- 검사번호 자동 생성: `OQC-YYYYMMDD-0001`

#### 프론트엔드
**파일**: `OQCRequestsPage.tsx`
- 출하 품질 검사 의뢰 목록 UI
- 검사 결과 시각화 (PASS/FAIL/CONDITIONAL)
- 합격/불합격 수량 표시
- 검사 통계 요약 (합격률, 총 검사 건수)

**라우트**: `/warehouse/oqc-requests`

---

## 📂 파일 목록

### 백엔드 (수정/추가)
1. ✅ `QualityInspectionController.java` (수정)
   - `getIQCRequests()` 메서드 추가
   - `getOQCRequests()` 메서드 추가

2. ✅ `GoodsReceiptService.java` (수정)
   - `QualityStandardRepository` 의존성 추가
   - `createIQCRequest()` 메서드 추가
   - `generateInspectionNo()` 메서드 추가
   - `processGoodsReceiptItem()` 메서드 확장

3. ✅ `ShippingService.java` (수정)
   - `QualityInspectionRepository` 의존성 추가
   - `QualityStandardRepository` 의존성 추가
   - `createOQCRequest()` 메서드 추가
   - `generateInspectionNo()` 메서드 추가
   - `createShipping()` 메서드 확장

### 프론트엔드 (신규)
4. ✅ `IQCRequestsPage.tsx` (신규)
5. ✅ `OQCRequestsPage.tsx` (신규)
6. ✅ `App.tsx` (수정) - 라우트 추가

---

## 🔄 워크플로우

### IQC (입고 품질 검사) 프로세스

```
1. 구매 주문 생성
   ↓
2. 입하 생성 (GoodsReceipt)
   - inspection_status = "PENDING" 설정
   ↓
3. GoodsReceiptService.createIQCRequest() 자동 실행
   - 품질 기준서 조회 (inspection_type = INCOMING)
   - QualityInspection 레코드 생성
   - inspection_no: IQC-20260124-0001
   - inspection_result: CONDITIONAL (대기 중)
   ↓
4. IQC 의뢰 리스트에 표시
   - /warehouse/iqc-requests 페이지
   ↓
5. 품질팀 검사 수행
   - QualityInspection 업데이트
   - inspection_result: PASS 또는 FAIL
   ↓
6. GoodsReceipt 완료 처리
   - PASS: LOT quality_status → PASSED, 가용 재고로 추가
   - FAIL: LOT quality_status → FAILED, 격리 창고로 이동
```

### OQC (출하 품질 검사) 프로세스

```
1. 판매 주문 생성
   ↓
2. 출하 생성 (Shipping)
   - inspection_status = "PENDING" 설정
   ↓
3. ShippingService.createOQCRequest() 자동 실행
   - 품질 기준서 조회 (inspection_type = OUTGOING)
   - QualityInspection 레코드 생성
   - inspection_no: OQC-20260124-0001
   - inspection_result: CONDITIONAL (대기 중)
   ↓
4. OQC 의뢰 리스트에 표시
   - /warehouse/oqc-requests 페이지
   ↓
5. 품질팀 검사 수행
   - QualityInspection 업데이트
   - inspection_result: PASS 또는 FAIL
   ↓
6. Shipping 완료 처리
   - PASS: 출하 승인
   - FAIL: 출하 보류 또는 취소
```

---

## 🎯 API 엔드포인트

### IQC 의뢰
```
GET /api/quality-inspections/iqc-requests
- 입고 품질 검사 의뢰 목록 조회
- inspection_type = "INCOMING" 필터
- Response: List<QualityInspectionResponse>
```

### OQC 의뢰
```
GET /api/quality-inspections/oqc-requests
- 출하 품질 검사 의뢰 목록 조회
- inspection_type = "OUTGOING" 필터
- Response: List<QualityInspectionResponse>
```

---

## 📊 데이터 모델

### QualityInspection
```java
- qualityInspectionId: Long
- inspectionNo: String (IQC-YYYYMMDD-0001 | OQC-YYYYMMDD-0001)
- inspectionDate: LocalDateTime
- inspectionType: String (INCOMING | OUTGOING)
- productCode: String
- productName: String
- inspectedQuantity: BigDecimal
- passedQuantity: BigDecimal
- failedQuantity: BigDecimal
- inspectionResult: String (PASS | FAIL | CONDITIONAL)
- inspectorName: String
- standardCode: String
- standardName: String
- measuredValue: BigDecimal (optional)
- measurementUnit: String (optional)
- remarks: String (optional)
```

### GoodsReceiptItem
```java
- inspection_status: String (NOT_REQUIRED | PENDING | PASS | FAIL)
- quality_inspection_id: Long (FK to QualityInspection)
```

### ShippingItem
```java
- inspection_status: String (NOT_REQUIRED | PENDING | PASS | FAIL)
- quality_inspection_id: Long (FK to QualityInspection)
```

---

## ✨ 주요 기능

### IQC/OQC 공통 기능
1. **자동 의뢰 생성**: 입하/출하 시 검사 상태가 PENDING이면 자동 의뢰
2. **품질 기준서 연동**: 제품별 검사 기준 자동 조회
3. **검사번호 자동 생성**: 일자별 순번 (IQC-YYYYMMDD-0001)
4. **검사 결과 추적**: PASS/FAIL/CONDITIONAL 상태 관리
5. **통계 요약**: 합격률, 총 검사 건수, 합격/불합격/조건부 건수

### UI 특징
1. **시각적 상태 표시**: Chip 컴포넌트로 검사 결과 표시
2. **수량 추적**: 합격 수량 vs 불합격 수량 구분 표시
3. **측정값 표시**: 실제 측정값과 단위 표시
4. **클릭 이동**: 검사 상세 페이지로 이동 가능
5. **새로고침**: 실시간 데이터 업데이트

---

## 🔗 모듈 통합

### QMS (품질 관리) 통합
- ✅ GoodsReceipt → QualityInspection (IQC)
- ✅ Shipping → QualityInspection (OQC)
- ✅ QualityStandard 자동 조회 (inspection_type별)
- ✅ Lot quality_status 자동 업데이트 (PASS/FAIL)

### WMS (창고 관리) 통합
- ✅ 입하 상태 자동 변경 (PENDING → INSPECTING)
- ✅ 출하 상태 자동 변경 (PENDING → INSPECTING)
- ✅ 격리 창고 자동 이동 (FAIL 시)
- ✅ 재고 잔액 자동 업데이트 (PASS 시)

---

## 📈 진행 상황

### WMS 완성도: 71% → **78%** (+7%)

**구현 완료**: 22/28 기능 (+2)

#### ✅ 신규 완성 (2개)
1. ✅ IQC 의뢰 리스트
2. ✅ OQC 의뢰 리스트

#### ❌ 남은 미구현 (6개)
1. ❌ SOP 관리
2. ❌ 가입고 체크리스트
3. ❌ 반품 리스트
4. ❌ 폐기 의뢰 리스트
5. ❌ 불출 신청 리스트
6. ❌ 인수인계 리스트

---

## 🎯 다음 단계 (Phase 2)

**목표**: 불출 신청/지시 리스트 구현

### 계획
1. **불출 신청 리스트**
   - 생산 부서에서 자재 요청
   - 작업 지시 기반 자재 소요 계산
   - 승인 워크플로우

2. **불출 지시 리스트**
   - 창고 부서에서 불출 처리
   - LOT 선택 (FIFO/FEFO)
   - 재고 차감 및 예약

3. **인수인계 리스트**
   - 불출 확인 및 인수
   - 생산 부서 인계 기록
   - 추적 가능성 확보

**예상 소요 시간**: 2-3시간

---

## 📝 테스트 시나리오

### IQC 테스트
```
1. 구매 주문 생성 (PO-001, 제품: RAW-001, 수량: 1000)
2. 입하 생성 (GR-001)
   - inspection_status: PENDING
3. IQC 의뢰 자동 생성 확인
   - IQC-20260124-0001
   - inspection_result: CONDITIONAL
4. IQC 의뢰 리스트 페이지 접속
   - /warehouse/iqc-requests
5. 검사 결과 시각화 확인
   - 통계: 총 1건, 조건부 1건
```

### OQC 테스트
```
1. 판매 주문 생성 (SO-001, 제품: FG-001, 수량: 100)
2. 출하 생성 (SH-001)
   - inspection_status: PENDING
3. OQC 의뢰 자동 생성 확인
   - OQC-20260124-0001
   - inspection_result: CONDITIONAL
4. OQC 의뢰 리스트 페이지 접속
   - /warehouse/oqc-requests
5. 검사 결과 시각화 확인
   - 통계: 총 1건, 조건부 1건
```

---

## 💡 개선 사항

### 향후 고려 사항
1. **재시험 의뢰**: IQC/OQC 재시험 의뢰 기능
2. **배치 검사**: 여러 LOT 동시 검사 의뢰
3. **검사 자동 할당**: 검사자 자동 배정
4. **모바일 검사**: 모바일 검사 입력 UI
5. **검사 스케줄링**: 검사 계획 및 일정 관리

---

**Phase 1 완료일**: 2026-01-24
**다음 Phase 시작 예정**: Phase 2 - 불출 관리

**문의**: msmoon@softice.co.kr | 010-4882-2035
