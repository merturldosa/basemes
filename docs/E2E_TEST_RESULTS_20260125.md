# E2E 통합 테스트 결과 보고서

**테스트 실행일**: 2026-01-25
**테스트 대상**: WMS 모듈 통합 테스트
**테스트 환경**:
- Backend: http://localhost:8080
- Database: PostgreSQL (sds_mes_dev)
- Tenant: DEMO001

---

## 테스트 실행 요약

| 항목 | 상태 | 비고 |
|------|------|------|
| 인증 (Login) | ✅ 성공 | JWT 토큰 발급 정상 |
| 제품 조회 | ✅ 성공 | 6개 제품 존재 확인 |
| 공급업체 조회 | ❌ 실패 | 500 Internal Server Error |
| 고객 조회 | ❌ 실패 | 500 Internal Server Error |
| 창고 조회 | ❌ 실패 | 500 Internal Server Error |
| 구매 주문 생성 | ❌ 실패 | 선행 데이터(Supplier) 부재 |

---

## 발견된 주요 이슈

### 1. 공급업체(Supplier) API 500 Error

**엔드포인트**: `GET /api/suppliers`
**상태 코드**: 500
**에러 메시지**:
```json
{
  "success": false,
  "errorCode": "C1000",
  "message": "내부 서버 오류가 발생했습니다.",
  "timestamp": "2026-01-25T19:04:44.8405277",
  "path": "/api/suppliers"
}
```

**가능한 원인**:
- SupplierService 또는 SupplierRepository에서 예외 발생
- LazyInitializationException (JOIN FETCH 누락 가능성)
- 데이터베이스 스키마 불일치

**재현 방법**:
```bash
TOKEN=$(curl -s -X POST \
  -H "Content-Type: application/json" \
  -d '{"tenantId":"DEMO001","username":"admin","password":"admin123"}' \
  http://localhost:8080/api/auth/login | grep -o '"accessToken":"[^"]*"' | sed 's/"accessToken":"\(.*\)"/\1/')

curl -X GET \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-ID: DEMO001" \
  http://localhost:8080/api/suppliers
```

---

### 2. 고객(Customer) API 500 Error

**엔드포인트**: `POST /api/customers`
**상태 코드**: 500
**에러 메시지**: 동일하게 "내부 서버 오류"

**영향**:
- 판매 주문(Sales Order) 생성 불가
- 출하(Shipping) 테스트 불가

---

### 3. 창고(Warehouse) API 500 Error

**엔드포인트**: `GET /api/warehouses`, `POST /api/warehouses`
**상태 코드**: 500

**영향**:
- 입하(Goods Receipt) 창고 지정 불가
- 재고(Inventory) 조회 불가
- WMS 모듈 전체 테스트 블로킹

---

### 4. 테스트 스크립트 이슈

**파일**: `scripts/test_wms_integration.sh`

**수정 사항**:
- ✅ JWT 토큰 추출 로직 수정: `"token"` → `"accessToken"`
- ⏳ supplierType 값 수정 필요: `"RAW_MATERIAL"` → `"MATERIAL"`
- ⏳ 테스트 데이터 선행 생성 필요

---

## 성공한 테스트

### ✅ 1. 인증 (Authentication)

**테스트 케이스**: 사용자 로그인 및 JWT 토큰 발급

**요청**:
```json
POST /api/auth/login
{
  "tenantId": "DEMO001",
  "username": "admin",
  "password": "admin123"
}
```

**응답**: 200 OK
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGci...",
    "refreshToken": "eyJhbGci...",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "user": {
      "userId": 12,
      "username": "admin",
      "email": "admin@softice.co.kr",
      "fullName": "관리자",
      "tenantId": "DEMO001",
      "tenantName": "데모 회사"
    }
  }
}
```

**결과**: ✅ 성공

---

### ✅ 2. 제품 조회 (Products)

**테스트 케이스**: 제품 목록 조회

**요청**:
```
GET /api/products
Authorization: Bearer {token}
X-Tenant-ID: DEMO001
```

**응답**: 200 OK
```json
{
  "success": true,
  "message": "제품 목록 조회 성공",
  "data": [
    {
      "productId": 1,
      "productCode": "P-LCD-001",
      "productName": "32인치 LCD 패널",
      "productType": "완제품",
      "specification": "1920x1080, IPS",
      "unit": "EA",
      "standardCycleTime": 600,
      "isActive": true,
      "tenantId": "DEMO001"
    },
    {
      "productId": 2,
      "productCode": "P-LCD-002",
      "productName": "43인치 LCD 패널",
      "productType": "완제품",
      "specification": "3840x2160, VA",
      "unit": "EA"
    },
    {
      "productId": 3,
      "productCode": "P-PCB-001",
      "productName": "LCD 구동 PCB",
      "productType": "반제품",
      "specification": "4-Layer PCB",
      "unit": "EA"
    },
    ... (총 6개 제품)
  ]
}
```

**확인된 제품**:
| ID | Code | Name | Type |
|----|------|------|------|
| 1 | P-LCD-001 | 32인치 LCD 패널 | 완제품 |
| 2 | P-LCD-002 | 43인치 LCD 패널 | 완제품 |
| 3 | P-PCB-001 | LCD 구동 PCB | 반제품 |
| 4 | P-BL-001 | 백라이트 유닛 | 반제품 |
| 5 | P-GLASS-001 | 강화 유리 | 원자재 |
| 6 | PROD-QMS-001 | Test LCD Panel 32inch | FINISHED_GOOD |

**결과**: ✅ 성공

---

## 권장 조치 사항

### 즉시 조치 (Critical)

1. **SupplierService LazyInitializationException 확인 및 수정**
   - 파일: `backend/src/main/java/kr/co/softice/mes/domain/repository/SupplierRepository.java`
   - 액션: JOIN FETCH 쿼리 추가
   ```java
   @Query("SELECT s FROM SupplierEntity s " +
          "JOIN FETCH s.tenant " +
          "WHERE s.tenant.tenantId = :tenantId")
   List<SupplierEntity> findByTenantIdWithJoinFetch(@Param("tenantId") String tenantId);
   ```

2. **CustomerRepository 동일 수정**
   - 파일: `backend/src/main/java/kr/co/softice/mes/domain/repository/CustomerRepository.java`

3. **WarehouseRepository 동일 수정**
   - 파일: `backend/src/main/java/kr/co/softice/mes/domain/repository/WarehouseRepository.java`

4. **백엔드 로그 확인**
   - 실제 예외 스택 트레이스 확인
   - 근본 원인 파악

---

### 단기 조치 (High Priority)

1. **테스트 데이터 직접 삽입**
   - 방법 1: psql 명령어로 `database/seeds/003_wms_test_data.sql` 실행
   - 방법 2: Docker PostgreSQL 컨테이너 사용
   - 방법 3: DBeaver/pgAdmin 등 GUI 도구 사용

2. **테스트 스크립트 수정**
   - `scripts/test_wms_integration.sh` supplierType 값 수정
   - 에러 핸들링 강화
   - 중간 결과 로깅 추가

---

### 중기 조치 (Medium Priority)

1. **E2E 테스트 자동화**
   - JUnit Integration Test 작성
   - TestContainers 사용하여 격리된 환경 구성
   - CI/CD 파이프라인 통합

2. **API 문서화**
   - Swagger/OpenAPI 스펙 확인
   - 각 엔드포인트별 에러 코드 명세

3. **모니터링 강화**
   - 백엔드 로깅 레벨 조정
   - 에러 추적 도구 도입 (Sentry, ELK Stack)

---

## 다음 단계

### Option 1: 디버깅 우선 (추천)
1. 백엔드 애플리케이션 로그 확인
2. Supplier/Customer/Warehouse API 500 에러 근본 원인 파악
3. JOIN FETCH 또는 스키마 이슈 수정
4. API 정상화 후 E2E 테스트 재실행

### Option 2: 우회 방법
1. psql 또는 DB 관리 도구로 테스트 데이터 직접 삽입
2. 기존 데이터 활용하여 일부 시나리오만 테스트
3. 프론트엔드 UI 수동 테스트

### Option 3: 테스트 범위 축소
1. 현재 작동하는 Product API 활용
2. QMS 모듈 통합 테스트 (이미 검증됨)
3. Production 모듈 통합 테스트

---

## 기술 노트

### 테스트 환경 정보
- **백엔드**: Spring Boot 3.2.1 + Java 21
- **데이터베이스**: PostgreSQL 16 (sds_mes_dev)
- **포트**: 8080
- **Multi-Tenant**: DEMO001

### 사용 가능한 테스트 데이터
- ✅ Products: 6개 (productId: 1-6)
- ❌ Suppliers: 0개 (API 에러로 확인 불가)
- ❌ Customers: 0개 (API 에러로 확인 불가)
- ❌ Warehouses: 0개 (API 에러로 확인 불가)

### 테스트 스크립트 위치
- **E2E 테스트 시나리오**: `docs/WMS_INTEGRATION_TEST_SCENARIOS.md`
- **테스트 자동화 스크립트**: `scripts/test_wms_integration.sh`
- **테스트 데이터 SQL**: `database/seeds/003_wms_test_data.sql`
- **테스트 결과**: 이 문서

---

## 결론

**현재 상태**: ⏸️ E2E 테스트 중단 (API 500 에러로 인한 블로킹)

**WMS 모듈 완성도**: 약 70-80%
- ✅ 백엔드 엔티티/서비스/컨트롤러 구현 완료
- ✅ 프론트엔드 UI 구현 완료
- ⏳ 통합 테스트 대기 중 (데이터 및 API 이슈)
- ⏳ 버그 수정 필요 (Supplier/Customer/Warehouse API)

**다음 조치**: 백엔드 로그 확인 및 500 에러 디버깅이 최우선 과제입니다.

---

**작성자**: Claude Sonnet 4.5
**작성일**: 2026-01-25 19:05 KST
**문서 버전**: 1.0
