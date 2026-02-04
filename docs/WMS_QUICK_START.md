# WMS 모듈 빠른 시작 가이드

## ⚡ 5분 안에 시작하기

### 1. Backend 실행 (2분)

```bash
# 1. 터미널 열기 (Git Bash 또는 PowerShell)
cd D:\prj\softice\prj\claude\SoIceMES\backend

# 2. 컴파일 및 실행 (jar 파일 잠금 문제가 있다면)
# 방법 A: Maven 직접 실행
mvn spring-boot:run

# 방법 B: 기존 실행 중인 프로세스 종료 후 재실행
# taskkill /F /IM java.exe
# mvn clean package -DskipTests
# java -jar target/soice-mes-backend-0.1.0-SNAPSHOT.jar
```

**확인**:
- Console에 "Started SoiceMesBackendApplication" 메시지 표시
- 포트 8080 리스닝 확인
- Swagger UI: http://localhost:8080/swagger-ui/index.html

### 2. Frontend 실행 (1분)

```bash
# 1. 새 터미널 열기
cd D:\prj\softice\prj\claude\SoIceMES\frontend

# 2. 의존성 설치 (최초 1회만)
npm install

# 3. 개발 서버 실행
npm run dev
```

**확인**:
- "Local: http://localhost:5173" 메시지 표시
- 브라우저에서 http://localhost:5173 접속

### 3. 로그인 (30초)

**기본 계정**:
- ID: `admin`
- PW: `admin123`

### 4. WMS 메뉴 접근 (30초)

**메뉴 경로**:
1. **창고 관리**: 재고 관리 > 창고 마스터
2. **입하 관리**: 입고 관리 > 입하 관리
3. **재고 현황**: 재고 관리 > 재고 현황

---

## 🧪 간단한 테스트

### Test 1: 창고 생성 (1분)

1. **창고 마스터** 페이지 이동
2. **"창고 생성"** 버튼 클릭
3. 입력:
   - 창고 코드: `TEST-WH-001`
   - 창고명: `테스트 원자재 창고`
   - 창고 유형: `원자재`
   - 담당자: 선택
4. **"생성"** 버튼 클릭
5. 목록에서 생성된 창고 확인

### Test 2: 입하 생성 (2분)

**사전 조건**:
- 창고 1개 이상 존재
- 제품 1개 이상 존재
- 공급업체 1개 이상 존재

**단계**:
1. **입하 관리** 페이지 이동
2. **"신규 입하"** 버튼 클릭
3. 입력:
   - 입하번호: 자동 생성 (비워둠)
   - 입하일자: 오늘
   - 창고: 선택
   - 입하유형: `구매`
4. **항목 추가** (향후 구현)
5. **"생성"** 버튼 클릭

### Test 3: 재고 현황 조회 (30초)

1. **재고 현황** 페이지 이동
2. 필터 선택:
   - 창고: 전체 또는 특정 창고
   - 제품: 전체 또는 특정 제품
3. 재고 목록 확인:
   - 가용 수량
   - 예약 수량
   - LOT 번호
   - 마지막 트랜잭션

---

## 🐛 문제 해결

### 문제 1: Backend 실행 실패 - "Port 8080 already in use"

**해결**:
```bash
# Windows
netstat -ano | findstr :8080
taskkill /F /PID [PID번호]

# Linux/Mac
lsof -i :8080
kill -9 [PID]
```

### 문제 2: Frontend 실행 실패 - "EADDRINUSE: address already in use"

**해결**:
```bash
# 포트 5173 사용 중인 프로세스 종료
netstat -ano | findstr :5173
taskkill /F /PID [PID번호]
```

### 문제 3: "Tenant not found" 에러

**원인**: 테넌트 데이터 없음

**해결**:
```sql
-- PostgreSQL 접속 후 실행
INSERT INTO common.si_tenants (tenant_id, tenant_name, is_active, created_at, updated_at)
VALUES ('TENANT001', '테스트회사', true, NOW(), NOW());
```

### 문제 4: "User not found" 에러

**원인**: 사용자 데이터 없음

**해결**:
```sql
INSERT INTO common.si_users (tenant_id, username, password, email, is_active, created_at, updated_at)
VALUES ('TENANT001', 'admin', '$2a$10$...', 'admin@test.com', true, NOW(), NOW());
```

### 문제 5: CORS 에러

**원인**: Backend CORS 설정 없음

**해결**: `application.yml` 수정
```yaml
spring:
  web:
    cors:
      allowed-origins:
        - http://localhost:5173
        - http://127.0.0.1:5173
      allowed-methods:
        - GET
        - POST
        - PUT
        - DELETE
        - PATCH
      allowed-headers: "*"
      allow-credentials: true
```

### 문제 6: 빌드 실패 - "Failed to delete target"

**원인**: jar 파일 실행 중

**해결**:
```bash
# 실행 중인 Java 프로세스 종료
taskkill /F /IM java.exe

# 또는 target 폴더 직접 삭제
rm -rf target

# 다시 빌드
mvn clean package -DskipTests
```

---

## 📚 추가 리소스

### 문서
- [통합 테스트 가이드](./WMS_INTEGRATION_TEST_GUIDE.md)
- [API 엔드포인트 명세](./WMS_API_ENDPOINTS.md)
- [개발 진행 상황](./DEVELOPMENT_PROGRESS.md)

### API 테스트
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- Postman Collection: (작성 예정)

### 데이터베이스
- PostgreSQL: `localhost:5432`
- Database: `soice_mes`
- User: (환경 설정 확인)

---

## ✅ 체크리스트

**실행 전**:
- [ ] PostgreSQL 실행 중
- [ ] Database `soice_mes` 존재
- [ ] 필수 스키마 생성 (Flyway 마이그레이션 확인)
- [ ] 포트 8080, 5173 사용 가능

**실행 후**:
- [ ] Backend: http://localhost:8080/swagger-ui/index.html 접속 가능
- [ ] Frontend: http://localhost:5173 접속 가능
- [ ] 로그인 성공
- [ ] WMS 메뉴 표시

**기능 확인**:
- [ ] 창고 CRUD 정상 동작
- [ ] 입하 생성 정상 동작
- [ ] 재고 현황 조회 정상 동작
- [ ] 재고 예약/해제 정상 동작

---

## 🎯 다음 단계

1. **테스트 데이터 준비**
   - 창고 5개 (각 타입별)
   - 제품/자재 10개
   - 공급업체 3개

2. **통합 테스트 실행**
   - [WMS_INTEGRATION_TEST_GUIDE.md](./WMS_INTEGRATION_TEST_GUIDE.md) 참조
   - Test #1: QMS 통합
   - Test #2: Production 통합
   - Test #3: Shipping 통합

3. **추가 개발**
   - 바코드/QR 코드 스캔
   - 실사 관리
   - 모바일 UI
   - 재고 분석 대시보드

---

**작성일**: 2026-01-24
**버전**: 1.0
