# SDS MES - Quick Start Deployment

> **快速部署指南** | **Quick Deployment Guide**
> 5분 안에 시스템을 시작하세요

## 🚀 최단 경로 (Fastest Way)

### Docker로 전체 시스템 실행 (권장)

```bash
# 1. 저장소 클론
git clone https://github.com/your-org/SDMES.git
cd SDMES

# 2. 모든 서비스 시작 (한 번의 명령으로!)
docker-compose up -d

# 3. 완료! 아래 URL로 접속
# Frontend: http://localhost
# Backend API: http://localhost/api
# Swagger UI: http://localhost/api/swagger-ui.html
```

### 모니터링 포함 실행

```bash
docker-compose -f docker-compose.yml -f monitoring/docker-compose.monitoring.yml up -d

# 추가 접속 URL:
# Prometheus: http://localhost:9090
# Grafana: http://localhost:3001 (admin/admin123)
```

---

## 📋 기본 계정

**초기 관리자 계정** (시스템 첫 실행 시 자동 생성):
- Username: `admin`
- Password: `admin123`

⚠️ **보안**: 프로덕션 환경에서는 반드시 비밀번호를 변경하세요!

---

## 🛠️ 개발 모드 (Hot Reload)

```bash
# 코드 변경 시 자동 재시작
docker-compose -f docker-compose.yml -f docker-compose.dev.yml up
```

---

## 🔧 유용한 명령어

```bash
# 로그 확인
docker-compose logs -f

# 특정 서비스 로그만 확인
docker-compose logs -f backend
docker-compose logs -f frontend

# 서비스 상태 확인
docker-compose ps

# 모든 서비스 중지
docker-compose down

# 데이터 포함 완전 삭제 (주의!)
docker-compose down -v

# 이미지 재빌드
docker-compose build --no-cache
```

---

## 🔍 문제 해결

### 포트 충돌 발생 시

```bash
# 사용 중인 포트 확인
netstat -ano | findstr :80
netstat -ano | findstr :8080
netstat -ano | findstr :5432

# docker-compose.yml에서 포트 변경
ports:
  - "8080:80"  # 80 대신 8080 사용
```

### 데이터베이스 초기화 실패 시

```bash
# 기존 데이터 삭제 후 재시작
docker-compose down -v
docker-compose up -d postgres
# 30초 대기
docker-compose up -d
```

### 백엔드 연결 안 됨

```bash
# 백엔드 로그 확인
docker-compose logs backend

# 데이터베이스 연결 확인
docker-compose exec postgres psql -U sds_admin -d sds_mes -c "SELECT 1"

# 백엔드 재시작
docker-compose restart backend
```

---

## 📚 다음 단계

1. **데이터 입력**: 시스템에 로그인하여 기본 설정 시작
2. **사용자 추가**: 관리 → 사용자 관리에서 팀원 추가
3. **권한 설정**: 역할 및 권한 설정
4. **프로덕션 배포**: [전체 배포 가이드](docs/DEPLOYMENT_GUIDE.md) 참조

---

## 🆘 지원

- 📧 Email: msmoon@softice.co.kr
- 📞 Phone: 010-4882-2035
- 📖 Full Guide: [DEPLOYMENT_GUIDE.md](docs/DEPLOYMENT_GUIDE.md)

---

**즐거운 제조 관리 되세요! 🏭✨**
