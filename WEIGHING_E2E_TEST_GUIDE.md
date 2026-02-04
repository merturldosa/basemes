# Weighing API End-to-End Test Guide

## 개요

이 문서는 Weighing System (칭량 관리)의 완전한 End-to-End 테스트 가이드입니다.

**테스트 목표**: GMP 준수 칭량 시스템의 완전한 워크플로우 검증

## 사전 요구사항

### 1. 서버 실행 확인
```bash
# 서버가 실행 중인지 확인
curl http://localhost:8080/actuator/health
# 또는
netstat -ano | grep 8080
```

### 2. 테스트 데이터 준비

#### Option A: Database에 직접 테스트 데이터 삽입
```sql
-- PostgreSQL에 접속
psql -U postgres -d soice_mes_db

-- 테스트 tenant 확인 또는 생성
SELECT * FROM common.si_tenants WHERE tenant_id = 'tenant1';

-- 테스트 product 확인 또는 생성
SELECT * FROM common.si_products WHERE tenant_id = 'tenant1' LIMIT 5;

-- 테스트 users 확인
SELECT user_id, username, email FROM common.si_users WHERE tenant_id = 'tenant1' LIMIT 5;
```

#### Option B: API를 통해 테스트 데이터 생성
먼저 인증 토큰을 획득해야 합니다.

### 3. 인증 토큰 획득

```bash
# 로그인 API 호출 (실제 엔드포인트에 맞게 수정)
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }' | jq -r '.data.token')

echo "JWT Token: $TOKEN"
```

## 테스트 시나리오

### Scenario 1: 칭량 생성 워크플로우 (자동 계산 검증)

**목표**: 칭량 생성 시 자동 계산 및 번호 생성 검증

```bash
# 1. 칭량 생성 요청
curl -X POST http://localhost:8080/api/weighings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-ID: tenant1" \
  -d '{
    "weighingType": "INCOMING",
    "productId": 1,
    "tareWeight": 50.0,
    "grossWeight": 1050.0,
    "expectedWeight": 1000.0,
    "unit": "kg",
    "operatorUserId": 1,
    "tolerancePercentage": 2.0,
    "temperature": 22.5,
    "humidity": 45.0,
    "remarks": "Test weighing - initial"
  }' | jq .

# 2. 응답 검증 체크리스트
# ✓ HTTP Status: 201 Created
# ✓ weighingNo 형식: WG-YYYYMMDD-0001 (예: WG-20260204-0001)
# ✓ netWeight = 1000.0 (grossWeight - tareWeight = 1050.0 - 50.0)
# ✓ variance = 0.0 (netWeight - expectedWeight = 1000.0 - 1000.0)
# ✓ variancePercentage = 0.0000
# ✓ toleranceExceeded = false (variance 0% < tolerance 2%)
# ✓ verificationStatus = "PENDING"
# ✓ temperature = 22.5
# ✓ humidity = 45.0
```

**Database 검증**:
```sql
SELECT
    weighing_no,
    weighing_type,
    tare_weight,
    gross_weight,
    net_weight,
    expected_weight,
    variance,
    variance_percentage,
    tolerance_exceeded,
    verification_status,
    created_at
FROM wms.si_weighings
ORDER BY created_at DESC
LIMIT 1;
```

### Scenario 2: 이중 검증 워크플로우 (GMP 준수)

**목표**: 다른 사용자에 의한 칭량 검증 워크플로우 테스트

```bash
# 1. 위 Scenario 1에서 생성한 weighingId 사용
WEIGHING_ID=1  # 실제 생성된 ID로 변경

# 2. 검증 요청 (다른 사용자로)
curl -X POST http://localhost:8080/api/weighings/$WEIGHING_ID/verify \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-ID: tenant1" \
  -d '{
    "verifierUserId": 2,
    "action": "VERIFY",
    "remarks": "Verified - weights are accurate"
  }' | jq .

# 3. 응답 검증 체크리스트
# ✓ HTTP Status: 200 OK
# ✓ verificationStatus = "VERIFIED" (PENDING → VERIFIED 변경)
# ✓ verifierUserId = 2 (operatorUserId와 다름)
# ✓ verificationDate가 현재 시간으로 설정됨
# ✓ verifierUsername과 verifierName이 설정됨
```

**Database 검증**:
```sql
SELECT
    weighing_no,
    verification_status,
    operator_user_id,
    verifier_user_id,
    verification_date,
    remarks
FROM wms.si_weighings
WHERE weighing_id = 1;
```

### Scenario 3: 자가 검증 방지 (GMP 준수)

**목표**: 동일 사용자의 자가 검증 방지 검증

```bash
# 1. 새로운 칭량 생성 (operatorUserId=1)
WEIGHING_RESPONSE=$(curl -s -X POST http://localhost:8080/api/weighings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-ID: tenant1" \
  -d '{
    "weighingType": "PRODUCTION",
    "productId": 1,
    "tareWeight": 100.0,
    "grossWeight": 1100.0,
    "expectedWeight": 1000.0,
    "unit": "kg",
    "operatorUserId": 1,
    "tolerancePercentage": 2.0
  }')

WEIGHING_ID=$(echo $WEIGHING_RESPONSE | jq -r '.data.weighingId')

# 2. 동일 사용자로 검증 시도 (verifierUserId=1, 실패해야 함)
curl -X POST http://localhost:8080/api/weighings/$WEIGHING_ID/verify \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-ID: tenant1" \
  -d '{
    "verifierUserId": 1,
    "action": "VERIFY",
    "remarks": "Attempting self-verification"
  }' | jq .

# 3. 응답 검증 체크리스트
# ✓ HTTP Status: 400 Bad Request
# ✓ Error message: "Self-verification is not allowed for GMP compliance"
# ✓ verificationStatus = "PENDING" (변경되지 않음)
```

### Scenario 4: 허용 오차 초과 감지

**목표**: 허용 오차를 초과한 칭량의 자동 플래깅 검증

```bash
# 1. 높은 variance를 가진 칭량 생성
curl -X POST http://localhost:8080/api/weighings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-ID: tenant1" \
  -d '{
    "weighingType": "INCOMING",
    "productId": 1,
    "tareWeight": 50.0,
    "grossWeight": 1100.0,
    "expectedWeight": 1000.0,
    "unit": "kg",
    "operatorUserId": 1,
    "tolerancePercentage": 2.0,
    "remarks": "High variance test"
  }' | jq .

# 2. 응답 검증 체크리스트
# ✓ HTTP Status: 201 Created
# ✓ netWeight = 1050.0 (1100.0 - 50.0)
# ✓ variance = 50.0 (1050.0 - 1000.0)
# ✓ variancePercentage = 5.0 (50/1000 * 100)
# ✓ toleranceExceeded = true (5% > 2% tolerance)

# 3. 허용 오차 초과 목록 조회
curl -X GET http://localhost:8080/api/weighings/tolerance-exceeded \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-ID: tenant1" | jq .

# 4. 응답 검증 체크리스트
# ✓ HTTP Status: 200 OK
# ✓ 방금 생성한 칭량이 목록에 포함됨
# ✓ 모든 항목의 toleranceExceeded = true
```

### Scenario 5: 검증 대기 큐

**목표**: 검증 대기 중인 칭량 목록 관리 검증

```bash
# 1. 여러 개의 칭량 생성 (3개)
for i in {1..3}; do
  curl -s -X POST http://localhost:8080/api/weighings \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -H "X-Tenant-ID: tenant1" \
    -d "{
      \"weighingType\": \"INCOMING\",
      \"productId\": 1,
      \"tareWeight\": 50.0,
      \"grossWeight\": 1050.0,
      \"expectedWeight\": 1000.0,
      \"unit\": \"kg\",
      \"operatorUserId\": 1,
      \"tolerancePercentage\": 2.0,
      \"remarks\": \"Batch test $i\"
    }" > /dev/null
  echo "Created weighing $i"
done

# 2. 검증 대기 목록 조회
curl -X GET http://localhost:8080/api/weighings/pending-verification \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-ID: tenant1" | jq .

# 3. 응답 검증 체크리스트
# ✓ HTTP Status: 200 OK
# ✓ 3개 이상의 항목이 반환됨
# ✓ 모든 항목의 verificationStatus = "PENDING"
# ✓ weighingNo 순서대로 정렬됨

# 4. 하나의 칭량 검증
FIRST_WEIGHING_ID=$(curl -s -X GET http://localhost:8080/api/weighings/pending-verification \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-ID: tenant1" | jq -r '.data[0].weighingId')

curl -X POST http://localhost:8080/api/weighings/$FIRST_WEIGHING_ID/verify \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-ID: tenant1" \
  -d '{
    "verifierUserId": 2,
    "action": "VERIFY",
    "remarks": "Verified in batch"
  }' | jq .

# 5. 검증 대기 목록 재조회 (항목이 하나 줄어야 함)
curl -X GET http://localhost:8080/api/weighings/pending-verification \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-ID: tenant1" | jq -r '.data | length'

# 6. 응답 검증 체크리스트
# ✓ 검증 대기 항목 수가 1개 감소함
```

### Scenario 6: 참조 링크

**목표**: 다른 문서(불출, 작업지시 등)와의 연결 검증

```bash
# 1. Material Request 참조로 칭량 생성
curl -X POST http://localhost:8080/api/weighings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-ID: tenant1" \
  -d '{
    "weighingType": "OUTGOING",
    "referenceType": "MATERIAL_REQUEST",
    "referenceId": 100,
    "productId": 1,
    "tareWeight": 50.0,
    "grossWeight": 1050.0,
    "expectedWeight": 1000.0,
    "unit": "kg",
    "operatorUserId": 1,
    "tolerancePercentage": 2.0,
    "remarks": "Material request weighing"
  }' | jq .

# 2. 참조 문서별 칭량 조회
curl -X GET "http://localhost:8080/api/weighings/reference/MATERIAL_REQUEST/100" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-ID: tenant1" | jq .

# 3. 응답 검증 체크리스트
# ✓ HTTP Status: 200 OK
# ✓ referenceType = "MATERIAL_REQUEST"
# ✓ referenceId = 100
# ✓ 해당 참조를 가진 칭량만 반환됨

# 4. Work Order 참조로 칭량 생성
curl -X POST http://localhost:8080/api/weighings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-ID: tenant1" \
  -d '{
    "weighingType": "PRODUCTION",
    "referenceType": "WORK_ORDER",
    "referenceId": 200,
    "productId": 1,
    "tareWeight": 100.0,
    "grossWeight": 2100.0,
    "expectedWeight": 2000.0,
    "unit": "kg",
    "operatorUserId": 1,
    "tolerancePercentage": 2.0,
    "remarks": "Work order weighing"
  }' | jq .

# 5. Work Order 참조 칭량 조회
curl -X GET "http://localhost:8080/api/weighings/reference/WORK_ORDER/200" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-ID: tenant1" | jq .
```

### Scenario 7: 칭량 수정 (자동 재계산)

**목표**: 칭량 수정 시 자동 재계산 검증

```bash
# 1. 칭량 생성
CREATE_RESPONSE=$(curl -s -X POST http://localhost:8080/api/weighings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-ID: tenant1" \
  -d '{
    "weighingType": "INCOMING",
    "productId": 1,
    "tareWeight": 50.0,
    "grossWeight": 1050.0,
    "expectedWeight": 1000.0,
    "unit": "kg",
    "operatorUserId": 1,
    "tolerancePercentage": 2.0
  }')

WEIGHING_ID=$(echo $CREATE_RESPONSE | jq -r '.data.weighingId')

# 2. 칭량 수정 (무게 변경)
curl -X PUT http://localhost:8080/api/weighings/$WEIGHING_ID \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-ID: tenant1" \
  -d '{
    "tareWeight": 60.0,
    "grossWeight": 1060.0,
    "remarks": "Updated weights after recalibration"
  }' | jq .

# 3. 응답 검증 체크리스트
# ✓ HTTP Status: 200 OK
# ✓ tareWeight = 60.0 (updated)
# ✓ grossWeight = 1060.0 (updated)
# ✓ netWeight = 1000.0 (recalculated: 1060.0 - 60.0)
# ✓ variance = 0.0 (recalculated: 1000.0 - 1000.0)
# ✓ variancePercentage = 0.0000 (recalculated)
# ✓ remarks updated
```

### Scenario 8: 칭량 거부

**목표**: 칭량 거부 워크플로우 검증

```bash
# 1. 칭량 생성
CREATE_RESPONSE=$(curl -s -X POST http://localhost:8080/api/weighings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-ID: tenant1" \
  -d '{
    "weighingType": "INCOMING",
    "productId": 1,
    "tareWeight": 50.0,
    "grossWeight": 1050.0,
    "expectedWeight": 1000.0,
    "unit": "kg",
    "operatorUserId": 1,
    "tolerancePercentage": 2.0
  }')

WEIGHING_ID=$(echo $CREATE_RESPONSE | jq -r '.data.weighingId')

# 2. 칭량 거부
curl -X POST http://localhost:8080/api/weighings/$WEIGHING_ID/verify \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-ID: tenant1" \
  -d '{
    "verifierUserId": 2,
    "action": "REJECT",
    "remarks": "Rejected - scale not calibrated"
  }' | jq .

# 3. 응답 검증 체크리스트
# ✓ HTTP Status: 200 OK
# ✓ verificationStatus = "REJECTED" (PENDING → REJECTED 변경)
# ✓ verifierUserId = 2
# ✓ verificationDate가 설정됨
# ✓ remarks에 거부 사유 저장됨
```

## Database 종합 검증

```sql
-- 1. 전체 칭량 현황
SELECT
    verification_status,
    COUNT(*) as count,
    SUM(CASE WHEN tolerance_exceeded THEN 1 ELSE 0 END) as exceeded_count
FROM wms.si_weighings
WHERE tenant_id = 'tenant1'
GROUP BY verification_status;

-- 2. 최근 칭량 목록 (상세)
SELECT
    weighing_id,
    weighing_no,
    weighing_type,
    reference_type,
    reference_id,
    tare_weight,
    gross_weight,
    net_weight,
    expected_weight,
    variance,
    variance_percentage,
    tolerance_exceeded,
    verification_status,
    operator_user_id,
    verifier_user_id,
    created_at
FROM wms.si_weighings
WHERE tenant_id = 'tenant1'
ORDER BY created_at DESC
LIMIT 20;

-- 3. 허용 오차 초과 칭량
SELECT
    weighing_no,
    net_weight,
    expected_weight,
    variance,
    variance_percentage,
    tolerance_percentage,
    verification_status
FROM wms.si_weighings
WHERE tenant_id = 'tenant1'
  AND tolerance_exceeded = true
ORDER BY created_at DESC;

-- 4. 검증 대기 칭량
SELECT
    weighing_no,
    weighing_type,
    net_weight,
    created_at,
    EXTRACT(HOUR FROM (NOW() - created_at)) as hours_pending
FROM wms.si_weighings
WHERE tenant_id = 'tenant1'
  AND verification_status = 'PENDING'
ORDER BY created_at ASC;

-- 5. 자가 검증 체크 (있으면 안 됨)
SELECT
    weighing_no,
    operator_user_id,
    verifier_user_id,
    verification_status
FROM wms.si_weighings
WHERE tenant_id = 'tenant1'
  AND verification_status = 'VERIFIED'
  AND operator_user_id = verifier_user_id;
-- 결과: 0 rows (자가 검증이 방지되었으면 성공)
```

## 성능 테스트 (선택적)

```bash
# 대량 칭량 생성 (100개)
for i in {1..100}; do
  curl -s -X POST http://localhost:8080/api/weighings \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -H "X-Tenant-ID: tenant1" \
    -d "{
      \"weighingType\": \"INCOMING\",
      \"productId\": 1,
      \"tareWeight\": 50.0,
      \"grossWeight\": $((1000 + RANDOM % 200)).0,
      \"expectedWeight\": 1000.0,
      \"unit\": \"kg\",
      \"operatorUserId\": 1,
      \"tolerancePercentage\": 2.0,
      \"remarks\": \"Performance test $i\"
    }" > /dev/null

  if [ $((i % 10)) -eq 0 ]; then
    echo "Created $i weighings..."
  fi
done

echo "Performance test completed. Created 100 weighings."

# 목록 조회 성능 측정
time curl -s -X GET http://localhost:8080/api/weighings \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-ID: tenant1" > /dev/null
```

## 오류 시나리오 테스트

### 1. 필수 필드 누락
```bash
curl -X POST http://localhost:8080/api/weighings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-ID: tenant1" \
  -d '{
    "weighingType": "INCOMING",
    "tareWeight": 50.0,
    "grossWeight": 1050.0
  }' | jq .

# 예상 응답: 400 Bad Request
# Error: "Product ID is required"
```

### 2. 잘못된 무게 (gross < tare)
```bash
curl -X POST http://localhost:8080/api/weighings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-ID: tenant1" \
  -d '{
    "weighingType": "INCOMING",
    "productId": 1,
    "tareWeight": 1000.0,
    "grossWeight": 500.0,
    "expectedWeight": 1000.0,
    "unit": "kg",
    "operatorUserId": 1
  }' | jq .

# 예상 응답: 400 Bad Request
# Error: "Gross weight must be greater than or equal to tare weight"
```

### 3. 존재하지 않는 칭량 ID
```bash
curl -X GET http://localhost:8080/api/weighings/999999 \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-ID: tenant1" | jq .

# 예상 응답: 404 Not Found
# Error: "Weighing not found"
```

### 4. 이미 검증된 칭량 수정 시도
```bash
# (이미 VERIFIED 상태인 칭량 ID 사용)
curl -X PUT http://localhost:8080/api/weighings/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-ID: tenant1" \
  -d '{
    "tareWeight": 60.0,
    "grossWeight": 1060.0
  }' | jq .

# 예상 응답: 400 Bad Request
# Error: "Cannot update verified weighing"
```

## 테스트 결과 체크리스트

- [ ] Scenario 1: 칭량 생성 및 자동 계산 ✓
- [ ] Scenario 2: 이중 검증 워크플로우 ✓
- [ ] Scenario 3: 자가 검증 방지 ✓
- [ ] Scenario 4: 허용 오차 초과 감지 ✓
- [ ] Scenario 5: 검증 대기 큐 관리 ✓
- [ ] Scenario 6: 참조 링크 (다형성) ✓
- [ ] Scenario 7: 칭량 수정 및 재계산 ✓
- [ ] Scenario 8: 칭량 거부 워크플로우 ✓
- [ ] Database 검증 ✓
- [ ] 오류 시나리오 ✓
- [ ] 성능 테스트 (선택적)

## 다음 단계

테스트 완료 후:

1. **문제 발견 시**:
   - 로그 확인: `tail -f logs/application.log`
   - 데이터베이스 확인
   - 관련 서비스 코드 리뷰

2. **성공 시**:
   - Day 3 완료로 표시
   - Day 4 또는 Module 2로 진행
   - 테스트 결과 문서화

3. **추가 테스트**:
   - Multi-tenant 격리 테스트
   - 동시성 테스트
   - Scale 연동 테스트 (하드웨어 있는 경우)
