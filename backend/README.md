# SDS MES Backend

Spring Boot 기반 Base MES 플랫폼 백엔드 API 서버

---

## 기술 스택

- **Java**: 21 LTS
- **Framework**: Spring Boot 3.2.1
- **Build Tool**: Maven
- **Database**: PostgreSQL 16
- **Cache**: Redis 7
- **ORM**: JPA (Hibernate) + QueryDSL
- **Security**: Spring Security + JWT
- **API Docs**: SpringDoc OpenAPI 3 (Swagger)

---

## 프로젝트 구조

```
backend/
├── src/
│   ├── main/
│   │   ├── java/kr/co/softice/mes/
│   │   │   ├── SoIceMesApplication.java
│   │   │   ├── common/              # 공통 모듈
│   │   │   │   ├── config/          # 설정 클래스
│   │   │   │   ├── dto/             # 공통 DTO
│   │   │   │   ├── exception/       # 예외 처리
│   │   │   │   ├── security/        # 보안 관련
│   │   │   │   └── util/            # 유틸리티
│   │   │   ├── domain/              # 도메인 모델
│   │   │   │   ├── entity/          # JPA Entity
│   │   │   │   ├── repository/      # Repository
│   │   │   │   └── service/         # Service
│   │   │   └── api/                 # API Layer
│   │   │       └── controller/      # REST Controller
│   │   └── resources/
│   │       ├── application.yml      # 설정 파일
│   │       └── application-{profile}.yml
│   └── test/                        # 테스트 코드
└── pom.xml                          # Maven 설정
```

---

## 시작하기

### 사전 요구사항

- Java 21 LTS
- Maven 3.8+
- Docker (PostgreSQL, Redis 실행용)

### 1. 데이터베이스 시작

```bash
cd ../docker
docker-compose up -d
```

### 2. 애플리케이션 실행

```bash
# Maven Wrapper 사용 (권장)
./mvnw spring-boot:run

# 또는 Maven 직접 사용
mvn spring-boot:run
```

### 3. 접속 확인

- **API Base URL**: http://localhost:8080/api
- **Swagger UI**: http://localhost:8080/api/swagger-ui.html
- **API Docs**: http://localhost:8080/api/v3/api-docs
- **Actuator**: http://localhost:8080/api/actuator/health

### 4. Health Check

```bash
# 기본 Health Check
curl http://localhost:8080/api/health

# 상세 Health Check
curl http://localhost:8080/api/health/detail
```

---

## 빌드

### Development 빌드

```bash
mvn clean package
```

### Production 빌드

```bash
mvn clean package -P production
```

### 빌드 결과

```
target/
└── sds-mes-backend-0.1.0-SNAPSHOT.jar
```

---

## 테스트

### 전체 테스트 실행

```bash
mvn test
```

### 특정 테스트 실행

```bash
mvn test -Dtest=HealthControllerTest
```

### 테스트 커버리지

```bash
mvn test jacoco:report
```

---

## 환경 설정

### application.yml

```yaml
spring:
  profiles:
    active: local  # local, dev, staging, production

app:
  jwt:
    secret: YOUR_JWT_SECRET_KEY
  tenant:
    default-tenant: softice
```

### 환경별 프로파일

- `application-local.yml`: 로컬 개발
- `application-dev.yml`: 개발 서버
- `application-staging.yml`: 스테이징 서버
- `application-production.yml`: 운영 서버

---

## API 문서

### Swagger UI

애플리케이션 실행 후 다음 URL 접속:

http://localhost:8080/api/swagger-ui.html

### OpenAPI 3.0 스펙

http://localhost:8080/api/v3/api-docs

---

## 주요 기능

### 현재 구현됨

- [x] Spring Boot 3.2 기본 구조
- [x] PostgreSQL 연동
- [x] Redis 연동
- [x] CORS 설정
- [x] Swagger/OpenAPI 문서화
- [x] Health Check API

### 구현 예정

- [ ] Multi-tenant 지원
- [ ] JWT 인증/인가
- [ ] JPA Entity (User, Role, Permission 등)
- [ ] RBAC (Role-Based Access Control)
- [ ] Audit Trail
- [ ] 파일 업로드/다운로드
- [ ] WebSocket (실시간 알림)

---

## 개발 가이드

### 코딩 컨벤션

- Java 21 기능 활용 (Records, Pattern Matching, Text Blocks)
- Lombok 사용으로 Boilerplate 코드 최소화
- MapStruct로 DTO 매핑 자동화
- QueryDSL로 Type-safe 쿼리 작성

### 패키지 구조 원칙

```
kr.co.softice.mes
├── common          # 전체 애플리케이션 공통
├── domain          # 도메인 모델 (Entity, Repository, Service)
└── api             # API Layer (Controller, DTO)
```

### 네이밍 규칙

- **Entity**: `User`, `Role`, `Tenant`
- **Repository**: `UserRepository`, `RoleRepository`
- **Service**: `UserService`, `AuthService`
- **Controller**: `UserController`, `AuthController`
- **DTO**: `UserDto`, `LoginRequest`, `LoginResponse`

---

## 트러블슈팅

### 포트 충돌

```bash
# 8080 포트 사용 중인 프로세스 확인
netstat -ano | findstr :8080

# 프로세스 종료
taskkill /PID <프로세스ID> /F
```

### 데이터베이스 연결 오류

```bash
# Docker 컨테이너 상태 확인
docker ps

# PostgreSQL 로그 확인
docker logs sds-mes-postgres
```

---

## 라이센스

Proprietary - All rights reserved by SoftIce Co., Ltd.

---

## 문의

**개발자**: Moon Myung-seop
**이메일**: msmoon@softice.co.kr
**회사**: (주)스마트도킹스테이션
