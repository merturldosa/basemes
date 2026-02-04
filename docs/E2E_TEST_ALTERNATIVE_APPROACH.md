# E2E 테스트 대안 접근 방법

**작성일**: 2026-01-25 20:00 KST
**상태**: 제안
**우선순위**: 높음

---

## 현재 상황 요약

### 문제점
1. **백엔드 빌드 실패**: 100개 이상의 컴파일 에러 발생
   - ErrorCode enum 값 누락 (RESOURCE_NOT_FOUND, INVALID_OPERATION 등) - ✅ 부분 수정
   - 다수의 메서드/필드 불일치
   - Phase 8 (Common Module) API 변경이 전체 코드베이스에 반영되지 않음

2. **E2E 테스트 중단**: 백엔드 재빌드 없이는 WMS API 테스트 불가

### 이미 시도한 조치
- ✅ ResponseDTO → ApiResponse 일괄 변경
- ✅ TenantContextHolder → TenantContext 일괄 변경
- ✅ import 경로 수정 (.inventory 서브패키지)
- ✅ ErrorCode enum에 일반 에러 코드 추가
- ⏳ 100+ 개별 컴파일 에러 수정 (미완료)

---

## 대안 1: 기존 실행 중인 백엔드 활용 ⭐ (추천)

### 현황
- 백엔드가 이미 실행 중이었음 (포트 8080)
- Product API는 정상 작동 확인 (200 OK, 6개 제품 조회 성공)
- Supplier/Customer/Warehouse API만 500 에러

### 제안
1. **기존 실행 중이던 JAR 파일 재시작**
   ```bash
   # 실행 중이던 버전으로 복구
   cd backend
   java -jar target/soice-mes-backend-0.1.0-SNAPSHOT.jar
   ```

2. **500 에러 원인 파악**
   - 로그 파일 확인: `backend/logs/soice-mes-backend.log`
   - 실제 Exception 스택트레이스 분석
   - LazyInitializationException인지 다른 문제인지 확인

3. **간단한 수정으로 해결 가능성**
   - Repository JOIN FETCH는 이미 구현되어 있음
   - Service도 올바른 메서드 사용 중
   - 실행 시점 설정 문제일 가능성

### 장점
- ✅ 즉시 테스트 재개 가능
- ✅ 빌드 에러 수정 불필요
- ✅ 실제 운영 환경과 동일

### 단점
- ⚠️ 근본적인 빌드 문제 미해결

---

## 대안 2: Git 이전 커밋으로 복귀

### 제안
빌드가 성공했던 마지막 커밋을 찾아 체크아웃

```bash
# 빌드 성공했던 커밋 찾기
git log --oneline --all | head -20

# 특정 커밋으로 이동 (예시)
git checkout 30faeb0

# 빌드 시도
cd backend
mvn clean package -DskipTests
```

### 장점
- ✅ 확실히 빌드 가능한 상태
- ✅ 안정적인 테스트 환경

### 단점
- ⚠️ 최신 변경사항 손실
- ⚠️ Phase 8 기능 사용 불가

---

## 대안 3: 최소 기능 빌드

### 제안
WMS 테스트에 필수적인 모듈만 포함하고 나머지 제외

```bash
# 이미 제외한 파일들
- PhysicalInventory* (완료)

# 추가 제외 후보
- WorkingHoursController/Service
- DisposalController/Service
- CommonCodeController/Service (일부)
```

### 장점
- ✅ WMS 핵심 기능 테스트 가능
- ✅ 빌드 시간 단축

### 단점
- ⚠️ 전체 시스템 테스트 불가
- ⚠️ 임시 방편

---

## 대안 4: 단계적 빌드 에러 수정 (장기)

### 제안
IDE (IntelliJ IDEA)를 사용하여 체계적으로 수정

1. **IntelliJ에서 프로젝트 열기**
2. **모든 컴파일 에러 목록 확인**
3. **우선순위별 수정**:
   - Priority 1: ErrorCode 관련 (공통)
   - Priority 2: API 변경 관련 (Builder 메서드)
   - Priority 3: Entity 필드 불일치

4. **자동화 도구 활용**:
   - IntelliJ "Optimize Imports"
   - IntelliJ "Code Inspection"
   - Find/Replace All

### 예상 시간
- 3-5시간

### 장점
- ✅ 근본적인 문제 해결
- ✅ 향후 빌드 안정성 확보

### 단점
- ⚠️ 시간 소요 큼
- ⚠️ 즉시 테스트 불가

---

## 대안 5: 프론트엔드 UI 수동 테스트

### 제안
백엔드 빌드를 포기하고, 프론트엔드에서 직접 테스트

1. **프론트엔드 개발 서버 실행**
   ```bash
   cd frontend
   npm run dev
   ```

2. **브라우저에서 수동 테스트**
   - 창고 관리 페이지
   - 재고 현황 페이지
   - 입하 관리 페이지

3. **API 에러 발생 시 브라우저 콘솔 확인**

### 장점
- ✅ UI/UX 검증 가능
- ✅ 사용자 관점 테스트

### 단점
- ⚠️ 자동화 불가
- ⚠️ 재현성 낮음

---

## 권장 접근 순서

### 1단계: 대안 1 (기존 백엔드 활용) - 즉시 실행
```bash
# 1. 기존 JAR 파일 확인
ls -lh backend/target/*.jar

# 2. 백엔드 재시작
cd backend
java -jar target/soice-mes-backend-0.1.0-SNAPSHOT.jar &

# 3. 5초 대기 후 테스트
sleep 5
curl http://localhost:8080/api/warehouses \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-ID: DEMO001"

# 4. 에러 발생 시 로그 확인
tail -100 logs/soice-mes-backend.log
```

### 2단계: 500 에러 간단 수정 (30분 이내)
로그에서 발견된 실제 에러만 타겟 수정

### 3단계: E2E 테스트 실행
WMS 통합 테스트 스크립트 실행

### 4단계 (선택): 대안 4 (장기 수정)
시간 여유가 있을 때 빌드 에러 전체 수정

---

## 즉시 실행 가능한 명령어

### Option A: 기존 백엔드 재시작
```bash
cd backend
# 기존 프로세스 종료 (이미 완료)
# JAR 파일 실행
java -jar target/soice-mes-backend-0.1.0-SNAPSHOT.jar > ../backend_restart.log 2>&1 &
echo $! > backend.pid
sleep 10

# 테스트
TOKEN=$(curl -s -X POST \
  -H "Content-Type: application/json" \
  -d '{"tenantId":"DEMO001","username":"admin","password":"admin123"}' \
  http://localhost:8080/api/auth/login | grep -o '"accessToken":"[^"]*"' | sed 's/"accessToken":"\(.*\)"/\1/')

curl -s -X GET \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-ID: DEMO001" \
  http://localhost:8080/api/warehouses
```

### Option B: 이전 커밋으로 복귀 후 빌드
```bash
git log --oneline | head -10
# 빌드 성공 커밋 선택 후
git checkout [COMMIT_HASH]
cd backend && mvn clean package -DskipTests
```

---

## 결론

**최우선 권장**: 대안 1 (기존 백엔드 활용)

**이유**:
1. 즉시 실행 가능
2. Product API는 이미 작동 확인
3. Warehouse/Supplier/Customer API만 수정하면 됨
4. 실제 에러 로그 확인 후 타겟 수정 가능
5. 전체 빌드 불필요

**다음 단계**:
1. 기존 JAR 재시작 (5분)
2. 실제 에러 로그 분석 (10분)
3. 간단한 수정 (30분 이내 예상)
4. E2E 테스트 재개

**장기 계획**:
- 빌드 에러 전체 수정은 별도 이슈로 분리
- CI/CD 파이프라인 구축하여 빌드 실패 조기 발견

---

**작성자**: Claude Sonnet 4.5
**작성일**: 2026-01-25 20:00 KST
**문서 버전**: 1.0
