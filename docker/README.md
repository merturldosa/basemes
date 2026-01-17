# Docker 개발 환경 가이드

SoIce MES 프로젝트의 Docker 기반 개발 환경 설정 및 사용 가이드입니다.

---

## 📋 서비스 구성

| 서비스 | 포트 | 용도 | 접속 정보 |
|--------|------|------|-----------|
| **PostgreSQL 16** | 5432 | Main Database | `mes_admin` / `mes_password_dev_2026` |
| **Redis 7** | 6379 | Cache & Session | (Password 없음) |
| **PgAdmin 4** | 5050 | DB Management UI | `msmoon@softice.co.kr` / `admin_password_2026` |

---

## 🚀 빠른 시작

### 1. Docker 서비스 시작

```bash
cd docker
docker-compose up -d
```

### 2. 서비스 상태 확인

```bash
docker-compose ps
```

**예상 출력:**
```
NAME                    STATUS              PORTS
soice-mes-postgres      running             0.0.0.0:5432->5432/tcp
soice-mes-redis         running             0.0.0.0:6379->6379/tcp
soice-mes-pgadmin       running             0.0.0.0:5050->80/tcp
```

### 3. 로그 확인

```bash
# 전체 로그
docker-compose logs -f

# 특정 서비스 로그
docker-compose logs -f postgres
docker-compose logs -f redis
```

### 4. 서비스 중지

```bash
# 컨테이너 중지 (데이터 유지)
docker-compose stop

# 컨테이너 중지 및 삭제 (데이터 유지)
docker-compose down

# 컨테이너 및 볼륨 삭제 (⚠️ 데이터 삭제)
docker-compose down -v
```

---

## 🗄️ PostgreSQL 사용법

### A. 커맨드라인 접속

```bash
# Docker 컨테이너 내부 접속
docker exec -it soice-mes-postgres psql -U mes_admin -d soice_mes_dev

# 호스트에서 직접 접속 (psql 설치 필요)
psql -h localhost -p 5432 -U mes_admin -d soice_mes_dev
```

### B. PgAdmin 웹 UI 접속

1. 브라우저에서 `http://localhost:5050` 접속
2. 로그인:
   - Email: `msmoon@softice.co.kr`
   - Password: `admin_password_2026`
3. 좌측 "Servers" → "SoIce MES Development" 자동 연결

### C. 데이터베이스 구조

**스키마:**
- `common` - 공통 관리 (사용자, 권한, 코드)
- `mes` - 생산관리
- `qms` - 품질관리
- `wms` - 창고관리
- `ems` - 설비관리
- `lims` - 시험관리
- `audit` - 감사 추적

**사용자:**
- `mes_admin` - 관리자 (모든 권한)
- `mes_app` - 애플리케이션 사용자 (CRUD 권한)
- `mes_readonly` - 읽기 전용 사용자

### D. 백업 및 복원

**백업:**
```bash
# 전체 데이터베이스 백업
docker exec soice-mes-postgres pg_dump -U mes_admin soice_mes_dev > ./postgres/backups/backup_$(date +%Y%m%d_%H%M%S).sql

# 특정 스키마만 백업
docker exec soice-mes-postgres pg_dump -U mes_admin -n mes soice_mes_dev > ./postgres/backups/mes_backup.sql
```

**복원:**
```bash
# 백업 파일 복원
docker exec -i soice-mes-postgres psql -U mes_admin -d soice_mes_dev < ./postgres/backups/backup_20260117.sql
```

---

## 🔴 Redis 사용법

### A. 커맨드라인 접속

```bash
# Docker 컨테이너 내부 접속
docker exec -it soice-mes-redis redis-cli

# 호스트에서 직접 접속 (redis-cli 설치 필요)
redis-cli -h localhost -p 6379
```

### B. 기본 명령어

```redis
# 연결 테스트
PING

# 현재 DB 정보
INFO

# 모든 키 조회 (개발 환경만!)
KEYS *

# 특정 키 조회
GET my_key

# 키 삭제
DEL my_key

# DB 전환
SELECT 1

# 전체 DB 삭제 (⚠️ 주의!)
FLUSHALL
```

### C. DB 구분

- **DB 0**: Session Storage (사용자 세션)
- **DB 1**: Application Cache (애플리케이션 캐시)
- **DB 2**: Real-time Data (실시간 데이터)
- **DB 3-15**: Reserved (예약)

---

## ⚙️ 환경 변수 설정

### Backend (.env)

```env
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=soice_mes_dev
DB_USER=mes_app
DB_PASSWORD=mes_app_password_dev_2026

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_DB=0

# JWT
JWT_SECRET=your_jwt_secret_key_here
JWT_EXPIRATION=86400
```

### Frontend (.env)

```env
VITE_API_BASE_URL=http://localhost:8080/api
VITE_WS_URL=ws://localhost:8080/ws
```

---

## 🔧 트러블슈팅

### 문제: 포트가 이미 사용 중

**오류:**
```
Error: Bind for 0.0.0.0:5432 failed: port is already allocated
```

**해결:**
```bash
# 포트 사용 확인 (Windows)
netstat -ano | findstr :5432

# 프로세스 종료
taskkill /PID <프로세스ID> /F

# 또는 docker-compose.yml에서 포트 변경
ports:
  - "15432:5432"  # 호스트 포트를 15432로 변경
```

### 문제: 볼륨 권한 오류

**오류:**
```
Permission denied: '/var/lib/postgresql/data'
```

**해결:**
```bash
# 기존 볼륨 삭제 후 재생성
docker-compose down -v
docker-compose up -d
```

### 문제: 초기화 스크립트 미실행

**증상:**
- 스키마가 생성되지 않음
- Extension이 설치되지 않음

**해결:**
```bash
# 볼륨 삭제 후 재시작 (데이터 삭제됨!)
docker-compose down -v
docker-compose up -d

# 로그 확인
docker-compose logs postgres | grep "init-database.sql"
```

---

## 📊 성능 튜닝

### PostgreSQL 성능 설정

현재 설정 (docker-compose.yml):
```yaml
max_connections=200
shared_buffers=256MB
effective_cache_size=1GB
work_mem=4MB
```

**메모리 증가 시:**
```yaml
# 8GB RAM 시스템
shared_buffers=2GB
effective_cache_size=6GB
work_mem=16MB
```

### Redis 성능 설정

```conf
# redis.conf
maxmemory 512mb          # 메모리 제한
maxmemory-policy allkeys-lru  # LRU 정책
```

---

## 🔒 보안 고려사항

### ⚠️ 개발 환경 설정 (현재)

- PostgreSQL: 비밀번호 간단 (`mes_password_dev_2026`)
- Redis: 비밀번호 없음
- PgAdmin: 간단한 비밀번호
- 모든 서비스: localhost 바인딩

### ✅ 운영 환경 필수 변경사항

1. **강력한 비밀번호 사용**
   ```bash
   # 랜덤 비밀번호 생성
   openssl rand -base64 32
   ```

2. **Redis 비밀번호 설정**
   ```conf
   # redis.conf
   requirepass <strong_password>
   ```

3. **네트워크 격리**
   - 외부 접속 차단
   - VPN/방화벽 설정

4. **SSL/TLS 활성화**
   - PostgreSQL SSL 인증서
   - Redis TLS 설정

---

## 📚 참고 자료

- [PostgreSQL 16 Documentation](https://www.postgresql.org/docs/16/)
- [Redis 7 Documentation](https://redis.io/docs/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [PgAdmin Documentation](https://www.pgadmin.org/docs/)

---

## 🆘 지원

문제가 발생하면 다음을 확인하세요:

1. Docker 버전: `docker --version` (20.10 이상)
2. Docker Compose 버전: `docker-compose --version` (1.29 이상)
3. 로그 확인: `docker-compose logs`
4. 컨테이너 상태: `docker-compose ps`

**문의:** msmoon@softice.co.kr
