# Changelog

All notable changes to the SDS MES project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Planned
- Docker 기반 개발 환경 구축
- 데이터베이스 스키마 설계
- Backend 기본 구조 생성
- Frontend 기본 구조 생성
- 인증/권한 시스템 (Multi-tenant)

---

## [0.1.0-alpha] - 2026-01-17

### Added
- 프로젝트 초기 설정 및 구조 생성
- Git 저장소 초기화
- 기본 폴더 구조 (`backend/`, `frontend/`, `database/`, `docker/`, `docs/`, `scripts/`)
- 대화 로그 자동 저장 시스템 (`conversation_logs/`)
- `.gitignore` 파일 (Java, Node.js, Docker, 보안 파일)
- `README.md` - 프로젝트 개요 및 시작 가이드
- `CLAUDE.md` - Claude Code 개발 가이드라인
- `LICENSE` - Proprietary 라이선스
- `CHANGELOG.md` - 변경 이력 문서

### Decisions
- **기술 스택 확정**:
  - Backend: Java 21 LTS + Spring Boot 3.2+
  - Frontend: React 18 + TypeScript
  - Database: PostgreSQL 16 + Redis 7.x
  - DevOps: Docker + Docker Compose
- **아키텍처 방향**: Configuration-Driven Base MES Platform
- **Multi-Tenant 전략**: Shared Database with tenant_id 컬럼
- **개발자 정보**: Moon Myung-seop (msmoon@softice.co.kr)

### Notes
- 화면설계서 374페이지 분석 완료
- Base MES 플랫폼 전환 전략 수립
- 개발 로드맵 수립 (Phase 1-3)

---

## Legend

- `Added`: 새로운 기능
- `Changed`: 기존 기능 변경
- `Deprecated`: 곧 제거될 기능
- `Removed`: 제거된 기능
- `Fixed`: 버그 수정
- `Security`: 보안 관련 수정
- `Decisions`: 주요 결정 사항
- `Notes`: 기타 참고 사항
