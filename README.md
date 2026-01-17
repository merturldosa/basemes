# SoIce MES (Manufacturing Execution System)

<div align="center">

![Version](https://img.shields.io/badge/version-0.1.0--alpha-blue)
![Java](https://img.shields.io/badge/Java-21_LTS-orange)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2+-green)
![React](https://img.shields.io/badge/React-18-61DAFB)
![License](https://img.shields.io/badge/license-Proprietary-red)

**차세대 Base MES 플랫폼** - 산업별 맞춤형 제조 실행 시스템

</div>

---

## 📋 프로젝트 개요

SoIce MES는 **(주)소프트아이스**가 개발하는 차세대 Base MES 플랫폼입니다.
의료기기, 화학, 전자 등 다양한 제조 산업에 **신속하게 커스터마이징** 가능한 설정 기반 아키텍처를 제공합니다.

### 핵심 특징

- ⚙️ **Configuration-Driven**: 코드 수정 없이 산업별 맞춤 설정
- 🏢 **Multi-Tenant**: 하나의 플랫폼으로 여러 고객사 운영
- 🎨 **Dynamic UI**: 메타데이터 기반 화면 자동 생성
- 🔄 **Workflow Engine**: BPMN 기반 비주얼 프로세스 설계
- 🤖 **AI/IoT Ready**: Industry 4.0 대응 스마트 팩토리
- 🌐 **Multi-Language**: 다국어 지원 (한국어, 영어, 중국어 등)

---

## 🏗️ 아키텍처

```
┌─────────────────────────────────────────┐
│     Customer Configuration Layer         │  ← 산업별 템플릿
│  (의료기기 | 화학 | 전자 | 자동차부품)   │
└────────────────┬────────────────────────┘
                 │
┌────────────────┴────────────────────────┐
│      Base MES Core Platform              │
│  ┌──────┬──────┬──────┬──────┬──────┐  │
│  │ MES  │ QMS  │ WMS  │ EMS  │COMMON│  │
│  │생산  │품질  │창고  │설비  │공통  │  │
│  └──────┴──────┴──────┴──────┴──────┘  │
└────────────────┬────────────────────────┘
                 │
┌────────────────┴────────────────────────┐
│   Infrastructure & Data Layer            │
│  PostgreSQL | Redis | TimescaleDB       │
└─────────────────────────────────────────┘
```

---

## 🛠️ 기술 스택

### Backend
- **Language**: Java 21 LTS
- **Framework**: Spring Boot 3.2+
- **ORM**: JPA (Hibernate) + QueryDSL
- **Security**: Spring Security + JWT
- **API**: REST + Swagger/OpenAPI

### Frontend
- **Framework**: React 18 + TypeScript
- **UI Library**: Material-UI v5
- **State Management**: Redux Toolkit + RTK Query
- **Charts**: Apache ECharts
- **Build Tool**: Vite

### Database
- **Primary DB**: PostgreSQL 16
- **Time-Series**: TimescaleDB
- **Cache**: Redis 7.x
- **Search**: Elasticsearch (선택)

### DevOps
- **Container**: Docker + Docker Compose
- **CI/CD**: GitHub Actions / GitLab CI
- **Monitoring**: Prometheus + Grafana
- **Logging**: ELK Stack

---

## 📂 프로젝트 구조

```
SoIceMES/
├── backend/                    # Spring Boot 백엔드
│   ├── src/
│   ├── pom.xml
│   └── README.md
├── frontend/                   # React 프론트엔드
│   ├── src/
│   ├── package.json
│   └── README.md
├── database/                   # 데이터베이스
│   ├── migrations/            # DB 마이그레이션 스크립트
│   ├── seeds/                 # 초기 데이터
│   └── schema/                # 스키마 문서
├── docker/                     # Docker 설정
│   ├── docker-compose.yml
│   ├── postgres/
│   └── redis/
├── docs/                       # 프로젝트 문서
│   ├── architecture/          # 아키텍처 설계
│   ├── api/                   # API 명세서
│   └── user-guide/            # 사용자 가이드
├── scripts/                    # 유틸리티 스크립트
├── conversation_logs/          # Claude 대화 로그
├── .github/workflows/          # CI/CD 워크플로우
├── .gitignore
├── README.md
└── CLAUDE.md                   # Claude Code 가이드
```

---

## 🚀 시작하기

### 사전 요구사항

- **Java**: 21 LTS 이상
- **Node.js**: 18 LTS 이상
- **Docker**: 20.10 이상
- **Git**: 2.30 이상

### 개발 환경 설정

```bash
# 1. 저장소 클론
git clone <repository-url>
cd SoIceMES

# 2. Docker 컨테이너 시작 (PostgreSQL, Redis)
cd docker
docker-compose up -d

# 3. Backend 실행
cd ../backend
./mvnw spring-boot:run

# 4. Frontend 실행
cd ../frontend
npm install
npm run dev
```

### 환경 변수 설정

```bash
# backend/.env.local
DB_HOST=localhost
DB_PORT=5432
DB_NAME=soice_mes
DB_USER=mes_user
DB_PASSWORD=<your-password>
JWT_SECRET=<your-secret>

# frontend/.env.local
VITE_API_BASE_URL=http://localhost:8080/api
```

---

## 📊 주요 모듈

### 1. 생산관리 (MES)
- 생산 계획 및 스케줄링
- 작업 지시 관리
- 실시간 공정 모니터링
- 생산 실적 집계

### 2. 품질관리 (QMS)
- 검사 기준 관리
- 수입/공정/출하 검사
- 부적합품 관리
- 통계적 품질 분석

### 3. 창고관리 (WMS)
- 재고 현황 관리
- 입출고 관리
- 로케이션 관리
- 재고 조사

### 4. 설비관리 (EMS)
- 설비 대장 관리
- 예방 보전
- 고장 이력 관리
- 가동률 분석

### 5. 시험관리 (LIMS)
- 시험 항목 관리
- 시험 결과 입력
- CoA 발행
- 안정성 시험

### 6. 공통 관리 (COMMON)
- 사용자/권한 관리
- 코드 관리
- Audit Trail
- 대시보드

---

## 🎯 개발 로드맵

### Phase 1: 기반 구축 (현재)
- [x] 프로젝트 초기 설정
- [x] Git 저장소 초기화
- [ ] Docker 개발 환경 구축
- [ ] 데이터베이스 스키마 설계
- [ ] Backend 기본 구조 생성
- [ ] Frontend 기본 구조 생성

### Phase 2: 핵심 기능 개발
- [ ] 인증/권한 시스템 (Multi-tenant)
- [ ] Configuration Engine
- [ ] Dynamic UI Framework
- [ ] 첫 번째 모듈: 공통 관리

### Phase 3: 산업 템플릿
- [ ] 의료기기 템플릿 (i-sens 기준)
- [ ] 화학 산업 템플릿
- [ ] 전자 산업 템플릿

---

## 📖 문서

- [아키텍처 설계](./docs/architecture/README.md)
- [API 명세서](./docs/api/README.md)
- [데이터베이스 스키마](./database/schema/README.md)
- [개발 가이드](./docs/development-guide.md)
- [배포 가이드](./docs/deployment-guide.md)

---

## 🤝 기여

이 프로젝트는 **(주)소프트아이스**의 소유입니다.

---

## 👥 팀

**개발사**: (주)소프트아이스
**개발자**: 문명섭 (Moon Myung-seop)
**이메일**: msmoon@softice.co.kr
**전화**: 010-4882-2035

---

## 📄 라이선스

Proprietary - All rights reserved by SoftIce Co., Ltd.

---

## 🔖 버전 히스토리

### v0.1.0-alpha (2026-01-17)
- 프로젝트 초기 설정
- Git 저장소 초기화
- 기본 폴더 구조 생성
- 기술 스택 확정

---

<div align="center">

**Made with ❤️ by SoftIce Co., Ltd.**

</div>
