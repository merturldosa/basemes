# SDS MES Phase 2 배포 가이드

Phase 2: POP Enhancement 배포를 위한 자동화 스크립트 모음입니다.

## 📋 배포 개요

**배포 대상**: Phase 2 - POP Enhancement (완성도 72% → 85%)

**주요 기능**:
- 실시간 생산 진행 추적 (WorkProgressEntity)
- 작업 일시정지/재개 (PauseResumeEntity)
- POP 서비스 (10개 API 엔드포인트)
- SOP 운영자 뷰 (4개 API 엔드포인트)
- 모바일 최적화 인터페이스
- WebSocket 실시간 업데이트
- 오프라인 데이터 동기화

**성능 개선**:
- 데이터베이스 인덱스 20개 추가
- 쿼리 성능 5-10배 향상

---

## 🚀 빠른 시작

### 방법 1: 전체 자동 배포 (권장)

```bash
# 프로젝트 루트에서 실행
cd D:\prj\softice\prj\claude\SDMES

# 전체 배포 스크립트 실행
./deploy/05-deploy-all.sh
```

이 스크립트는 4개 단계를 순차적으로 실행합니다:
1. 환경 확인
2. 데이터베이스 마이그레이션
3. 백엔드 빌드
4. 프론트엔드 빌드

각 단계 사이에 확인 메시지가 표시되어 문제 발생 시 중단할 수 있습니다.

### 방법 2: 단계별 수동 배포

문제 해결이나 특정 단계만 실행하고 싶을 때 사용합니다.

```bash
# Step 1: 환경 확인
./deploy/01-check-environment.sh

# Step 2: 데이터베이스 마이그레이션
./deploy/02-migrate-database.sh

# Step 3: 백엔드 빌드
./deploy/03-build-backend.sh

# Step 4: 프론트엔드 빌드
./deploy/04-build-frontend.sh
```

---

## 📝 스크립트 상세 설명

### 01-check-environment.sh

**목적**: 필요한 도구와 환경이 모두 설치되어 있는지 확인

**확인 항목**:
- ✅ Java (JDK 17 이상)
- ✅ Maven (3.6 이상)
- ✅ Node.js (18 이상)
- ✅ npm (9 이상)
- ✅ Git
- ✅ PostgreSQL (15 이상)
- ✅ 데이터베이스 연결 상태
- ✅ Git 상태 및 최신 커밋

**실행 예시**:
```bash
./deploy/01-check-environment.sh
```

**출력 예시**:
```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
SDS MES Phase 2 - Environment Check
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

1. Checking Java...
✓ java is installed
openjdk 17.0.2 2022-01-18

2. Checking Maven...
✓ mvn is installed
Apache Maven 3.8.6

...
```

**오류 해결**:
- ❌ 도구가 설치되지 않은 경우 → 해당 도구 설치 후 재실행
- ⚠️ PostgreSQL 연결 실패 → PostgreSQL 서버 시작 확인

---

### 02-migrate-database.sh

**목적**: Phase 2 데이터베이스 마이그레이션 실행

**실행 내용**:
- V029: Work Progress 스키마 생성
  - `si_work_progress` 테이블
  - `si_pause_resume_history` 테이블
- V030: 성능 최적화 인덱스 20개 추가

**데이터베이스 설정**:

환경 변수로 설정 가능 (기본값 사용 가능):
```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=sds_mes_dev
export DB_USER=mes_admin
export DB_PASSWORD=mes_password_dev_2026
```

**실행 예시**:
```bash
# 기본 설정 사용
./deploy/02-migrate-database.sh

# 커스텀 설정 사용
DB_HOST=192.168.1.100 DB_NAME=sds_mes_prod ./deploy/02-migrate-database.sh
```

**마이그레이션 방법**:

스크립트는 두 가지 방법을 지원합니다:

1. **Flyway 사용 (권장)**:
   - Maven Flyway 플러그인 사용
   - 마이그레이션 이력 자동 관리
   - 롤백 지원

2. **직접 SQL 실행**:
   - psql로 SQL 파일 직접 실행
   - Flyway 없이 사용 가능

**검증**:

마이그레이션 완료 후 자동으로 검증:
- 테이블 생성 확인 (`si_work_progress`, `si_pause_resume_history`)
- 인덱스 생성 확인 (20개 인덱스)

**오류 해결**:
- ❌ 연결 실패 → PostgreSQL 서버 확인, 비밀번호 확인
- ❌ 마이그레이션 파일 없음 → Git에서 최신 코드 pull
- ❌ 이미 적용됨 → 정상 (Flyway가 중복 방지)

---

### 03-build-backend.sh

**목적**: Spring Boot 백엔드 애플리케이션 빌드

**실행 단계**:
1. `mvn clean` - 이전 빌드 결과 삭제
2. `mvn compile` - 소스 코드 컴파일
3. `mvn test` - 테스트 실행 (선택)
4. `mvn package` - JAR 파일 생성

**실행 예시**:
```bash
./deploy/03-build-backend.sh
```

**대화형 옵션**:
- "Run tests?" → y/n 선택
  - y: POPIntegrationTest 실행
  - n: 테스트 건너뛰기 (빠른 빌드)

**출력 파일**:
- `backend/target/sds-mes-0.0.1-SNAPSHOT.jar`

**JAR 실행 방법**:
```bash
# 방법 1: Maven으로 실행
cd backend
mvn spring-boot:run

# 방법 2: JAR 직접 실행
java -jar backend/target/sds-mes-0.0.1-SNAPSHOT.jar
```

**오류 해결**:
- ❌ 컴파일 에러 → 소스 코드 확인
- ❌ 테스트 실패 → 데이터베이스 연결 확인, 테스트 건너뛰기 옵션 사용
- ❌ JAR 파일 없음 → package 단계 로그 확인

---

### 04-build-frontend.sh

**목적**: React + Vite 프론트엔드 애플리케이션 빌드

**실행 단계**:
1. `npm install` - 의존성 설치/업데이트
2. `npm run lint` - 코드 품질 검사 (선택)
3. `npm run test:unit` - 단위 테스트 (선택)
4. `npm run build` - 프로덕션 빌드

**실행 예시**:
```bash
./deploy/04-build-frontend.sh
```

**대화형 옵션**:
- "Run linter?" → y/n 선택
- "Run tests?" → y/n 선택

**출력 디렉토리**:
- `frontend/dist/` - 빌드된 정적 파일

**개발 서버 실행**:
```bash
cd frontend
npm run dev
# http://localhost:5173 접속
```

**프로덕션 배포**:
```bash
# 빌드 결과 확인
cd frontend/dist
ls -la

# 웹 서버로 복사 (예: nginx)
cp -r dist/* /var/www/html/

# 또는 preview 서버로 테스트
npm run preview
```

**오류 해결**:
- ❌ 의존성 설치 실패 → `rm -rf node_modules package-lock.json` 후 재설치
- ❌ 빌드 에러 → TypeScript 타입 에러 확인
- ❌ Lint 에러 → `npm run lint -- --fix`로 자동 수정

---

### 05-deploy-all.sh

**목적**: 위 4개 스크립트를 순차적으로 실행하는 마스터 스크립트

**실행 순서**:
1. 01-check-environment.sh
2. 02-migrate-database.sh
3. 03-build-backend.sh
4. 04-build-frontend.sh

**특징**:
- 각 단계 사이에 확인 프롬프트
- 중간에 취소 가능
- 색상으로 구분된 출력 (파란색 헤더, 녹색 성공, 빨간색 에러)
- 완료 후 다음 단계 안내

**실행 예시**:
```bash
./deploy/05-deploy-all.sh
```

**출력 예시**:
```
╔════════════════════════════════════════════════════════╗
║                                                        ║
║      SDS MES Phase 2 - Complete Deployment          ║
║                                                        ║
╚════════════════════════════════════════════════════════╝

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Step 1/4: Environment Check
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

...

Continue with database migration? (y/n)
```

---

## 🔧 사전 준비 사항

### 1. PostgreSQL 데이터베이스 준비

**데이터베이스 생성**:
```sql
-- PostgreSQL에 접속
psql -U postgres

-- 데이터베이스 생성
CREATE DATABASE sds_mes_dev
    WITH ENCODING = 'UTF8'
         LC_COLLATE = 'en_US.UTF-8'
         LC_CTYPE = 'en_US.UTF-8';

-- 사용자 생성 (비밀번호는 application.yml과 일치해야 함)
CREATE USER mes_admin WITH PASSWORD 'mes_password_dev_2026';

-- 권한 부여
GRANT ALL PRIVILEGES ON DATABASE sds_mes_dev TO mes_admin;

-- 스키마 생성
\c sds_mes_dev
CREATE SCHEMA mes AUTHORIZATION mes_admin;
CREATE SCHEMA qms AUTHORIZATION mes_admin;
CREATE SCHEMA wms AUTHORIZATION mes_admin;
```

**연결 테스트**:
```bash
psql -h localhost -p 5432 -U mes_admin -d sds_mes_dev -c "SELECT version();"
```

### 2. 필수 도구 설치 확인

**Windows (Git Bash)**:
```bash
# Java
java -version
# openjdk version "17.0.2" 이상

# Maven
mvn --version
# Apache Maven 3.6 이상

# Node.js
node --version
# v18.0.0 이상

# npm
npm --version
# 9.0.0 이상
```

### 3. Git 최신 상태 확인

```bash
# 최신 코드 받기
git pull origin main

# 현재 브랜치 확인
git branch
# * main

# 최신 커밋 확인
git log --oneline -1
# Phase 2 완료 커밋이 보여야 함
```

---

## 📊 배포 체크리스트

배포 전에 다음 항목을 확인하세요:

### 환경 준비
- [ ] PostgreSQL 서버 실행 중
- [ ] 데이터베이스 `sds_mes_dev` 생성됨
- [ ] 사용자 `mes_admin` 생성 및 권한 부여됨
- [ ] Java 17 이상 설치됨
- [ ] Maven 3.6 이상 설치됨
- [ ] Node.js 18 이상 설치됨
- [ ] npm 9 이상 설치됨

### 코드 상태
- [ ] Git에서 최신 코드 pull 완료
- [ ] main 브랜치 체크아웃
- [ ] Phase 2 커밋 존재 확인

### 설정 파일
- [ ] `backend/src/main/resources/application.yml` 데이터베이스 설정 확인
- [ ] `frontend/.env` 파일 존재 및 API URL 확인

### 네트워크
- [ ] 포트 8080 (백엔드) 사용 가능
- [ ] 포트 5173 (프론트엔드 개발) 사용 가능
- [ ] 방화벽 설정 확인

---

## 🚨 문제 해결

### 일반적인 문제

#### 1. "command not found: mvn"
**원인**: Maven이 설치되지 않았거나 PATH에 없음

**해결**:
```bash
# Windows (chocolatey)
choco install maven

# 또는 수동 설치
# https://maven.apache.org/download.cgi
# 환경 변수 PATH에 추가
```

#### 2. "Cannot connect to database"
**원인**: PostgreSQL 서버가 실행되지 않았거나 설정 오류

**해결**:
```bash
# PostgreSQL 서비스 상태 확인
# Windows
net start postgresql-x64-15

# 연결 테스트
psql -h localhost -p 5432 -U mes_admin -d sds_mes_dev

# 비밀번호 확인 (application.yml과 일치해야 함)
```

#### 3. "Build failed: compilation error"
**원인**: 소스 코드에 컴파일 에러

**해결**:
```bash
# 에러 메시지 자세히 확인
cd backend
mvn clean compile

# 특정 파일 문제 확인
# 에러 메시지에서 파일명과 라인 번호 확인
```

#### 4. "npm install failed"
**원인**: 의존성 충돌 또는 네트워크 문제

**해결**:
```bash
# node_modules 삭제 후 재설치
cd frontend
rm -rf node_modules package-lock.json
npm install

# 레지스트리 변경 (중국/한국)
npm config set registry https://registry.npmmirror.com
```

#### 5. "Port 8080 already in use"
**원인**: 다른 애플리케이션이 8080 포트 사용 중

**해결**:
```bash
# 사용 중인 프로세스 확인
# Windows
netstat -ano | findstr :8080

# 프로세스 종료 (PID 확인 후)
taskkill /PID <PID> /F

# 또는 application.yml에서 포트 변경
server:
  port: 8081
```

### 데이터베이스 마이그레이션 문제

#### 1. "Migration file not found"
**원인**: Git에서 최신 마이그레이션 파일이 없음

**해결**:
```bash
# 최신 코드 pull
git pull origin main

# 마이그레이션 파일 확인
ls -la database/migrations/V029*
ls -la database/migrations/V030*
```

#### 2. "Migration already applied"
**원인**: 마이그레이션이 이미 적용됨 (정상)

**해결**:
- Flyway는 중복 적용을 자동으로 방지합니다
- 문제 없으면 계속 진행

#### 3. "Checksum mismatch"
**원인**: 이미 적용된 마이그레이션 파일이 수정됨

**해결**:
```bash
# Flyway 리페어 실행
cd backend
mvn flyway:repair

# 또는 수동으로 flyway_schema_history 테이블 확인
psql -U mes_admin -d sds_mes_dev
SELECT * FROM flyway_schema_history;
```

---

## 📚 추가 문서

배포 후 다음 문서를 참고하세요:

### 운영자 가이드
- **docs/POP_OPERATOR_QUICK_START.md**
  - POP 시스템 사용 방법 (한글)
  - 작업지시 스캔, 생산 수량 기록, 불량 보고 등
  - 운영자 교육용

### API 개발 문서
- **docs/POP_API_REFERENCE.md**
  - POP API 10개 엔드포인트 상세 설명
  - 요청/응답 예제
  - WebSocket 연결 방법
  - 개발자용

### 모바일 최적화
- **docs/POP_MOBILE_OPTIMIZATION_GUIDE.md**
  - PWA 설정 및 사용법
  - 터치 인터페이스 가이드라인
  - 오프라인 모드 구현
  - 성능 최적화 팁

### Phase 2 완료 보고서
- **docs/PHASE2_POP_ENHANCEMENT_COMPLETE.md**
  - Phase 2 구현 내역 전체
  - 아키텍처 설명
  - 테스트 결과
  - 알려진 이슈

---

## 🎯 배포 후 확인 사항

### 1. 백엔드 API 테스트

```bash
# 백엔드 서버 시작
cd backend
mvn spring-boot:run

# 새 터미널에서 API 테스트
curl http://localhost:8080/api/pop/work-orders/active
curl http://localhost:8080/actuator/health
```

### 2. 프론트엔드 접속 테스트

```bash
# 개발 서버 시작
cd frontend
npm run dev

# 브라우저에서 접속
# http://localhost:5173
# http://localhost:5173/pop/work-orders
```

### 3. 데이터베이스 확인

```sql
-- 테이블 생성 확인
SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'mes'
  AND table_name IN ('si_work_progress', 'si_pause_resume_history')
ORDER BY table_name;

-- 인덱스 확인
SELECT indexname
FROM pg_indexes
WHERE schemaname = 'mes'
  AND indexname LIKE 'idx_work_progress%'
ORDER BY indexname;
```

### 4. WebSocket 연결 테스트

브라우저 콘솔에서:
```javascript
const client = new StompJs.Client({
  brokerURL: 'ws://localhost:8080/ws',
  connectHeaders: { tenantId: 'tenant1' }
});

client.onConnect = () => {
  console.log('WebSocket connected');
  client.subscribe('/topic/work-progress/tenant1', (message) => {
    console.log('Received:', message.body);
  });
};

client.activate();
```

---

## 🔄 재배포

코드 변경 후 재배포:

### 백엔드만 재배포
```bash
./deploy/03-build-backend.sh
# 백엔드 서버 재시작
```

### 프론트엔드만 재배포
```bash
./deploy/04-build-frontend.sh
# 개발 서버는 자동 리로드
```

### 데이터베이스 스키마 변경
```bash
# 새 마이그레이션 파일 추가 후
./deploy/02-migrate-database.sh
```

---

## 📞 지원

문제가 해결되지 않으면:

1. **로그 확인**:
   - 백엔드: `backend/logs/application.log`
   - 프론트엔드: 브라우저 개발자 도구 콘솔

2. **이슈 보고**:
   - Git 저장소에 이슈 등록
   - 에러 메시지 전체 복사
   - 실행 환경 정보 포함

3. **연락처**:
   - 개발자: 문명섭 (Moon Myeong-seop)
   - 이메일: msmoon@softice.co.kr
   - 전화: 010-4882-2035

---

## 📋 버전 정보

- **Phase**: Phase 2 - POP Enhancement
- **작성일**: 2026-02-05
- **작성자**: Moon Myung-seop
- **버전**: 1.0.0

---

이 가이드를 따라 배포하시면 Phase 2 POP Enhancement 기능을 성공적으로 배포할 수 있습니다.

배포 성공을 기원합니다! 🚀
