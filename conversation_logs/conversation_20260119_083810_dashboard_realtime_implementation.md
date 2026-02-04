# 대화 로그 - 대시보드 실시간 데이터 연동 구현
**날짜**: 2026-01-19
**세션**: Dashboard Real-time Data Integration
**작성자**: Claude (Sonnet 4.5)

---

## 세션 요약

이 세션에서는 대시보드에 실시간 데이터 연동 기능을 구현했습니다:
1. 백엔드 통계 API 개발
2. Apache ECharts 통합
3. 실시간 데이터 fetch 및 자동 새로고침
4. 반응형 차트 및 위젯

---

## 구현 완료 항목

### 1. 백엔드 통계 API

#### 새로운 DTO 클래스
**DashboardStatsResponse.java**
```java
@Data
@Builder
public class DashboardStatsResponse {
    private Long totalUsers;        // 전체 사용자 수
    private Long activeUsers;       // 활성 사용자 수
    private Long totalRoles;        // 전체 역할 수
    private Long totalPermissions;  // 전체 권한 수
    private Long todayLogins;       // 오늘 로그인 수
    private Long activeSessions;    // 현재 활성 세션 수 (최근 30분)
}
```

**UserStatsResponse.java**
```java
@Data
@Builder
public class UserStatsResponse {
    private String status;       // active, inactive, locked
    private Long count;          // 해당 상태의 사용자 수
    private String displayName;  // 표시 이름
}
```

#### DashboardService 구현
**파일**: `backend/src/main/java/kr/co/softice/mes/domain/service/DashboardService.java`

**주요 메서드**:
- `getDashboardStats(String tenantId)`: 전체 통계 조회
- `getUserStats(String tenantId)`: 사용자 상태별 통계

**로직**:
```java
// 활성 세션 수 = 최근 30분 이내 로그인한 사용자
LocalDateTime recentLoginTime = LocalDateTime.now().minusMinutes(30);
long activeSessions = userRepository.countByTenantAndLastLoginAtAfter(tenant, recentLoginTime);

// 오늘 로그인 수
LocalDateTime todayStart = LocalDate.now().atStartOfDay();
long todayLogins = userRepository.countByTenantAndLastLoginAtAfter(tenant, todayStart);
```

#### DashboardController 구현
**파일**: `backend/src/main/java/kr/co/softice/mes/api/controller/DashboardController.java`

**API 엔드포인트**:
- `GET /api/dashboard/stats`: 대시보드 통계 조회
- `GET /api/dashboard/user-stats`: 사용자 상태별 통계

**권한**: `@PreAuthorize("isAuthenticated()")` - 로그인한 사용자만 접근 가능

#### Repository 확장

**UserRepository 추가 메서드**:
```java
long countByTenant(TenantEntity tenant);
long countByTenantAndStatus(TenantEntity tenant, String status);
long countByTenantAndLastLoginAtAfter(TenantEntity tenant, LocalDateTime time);
```

**RoleRepository 추가 메서드**:
```java
long countByTenant(TenantEntity tenant);
```

### 2. 프론트엔드 구현

#### Apache ECharts 설치
```bash
npm install echarts echarts-for-react
```

**설치된 패키지**:
- `echarts`: Apache ECharts 라이브러리
- `echarts-for-react`: React 통합 컴포넌트

#### Dashboard Service 구현
**파일**: `frontend/src/services/dashboardService.ts`

**TypeScript 인터페이스**:
```typescript
export interface DashboardStats {
  totalUsers: number;
  activeUsers: number;
  totalRoles: number;
  totalPermissions: number;
  todayLogins: number;
  activeSessions: number;
}

export interface UserStats {
  status: string;
  count: number;
  displayName: string;
}
```

**메서드**:
- `getStats()`: 대시보드 통계 조회
- `getUserStats()`: 사용자 상태별 통계

#### Dashboard 페이지 리뉴얼
**파일**: `frontend/src/pages/Dashboard.tsx` (완전 재작성)

**주요 기능**:

1. **실시간 데이터 로딩**
```typescript
const loadDashboardData = async () => {
  const [statsData, userStatsData] = await Promise.all([
    dashboardService.getStats(),
    dashboardService.getUserStats(),
  ]);
  setStats(statsData);
  setUserStats(userStatsData);
};
```

2. **자동 새로고침 (30초마다)**
```typescript
useEffect(() => {
  loadDashboardData();
  const interval = setInterval(loadDashboardData, 30000);
  return () => clearInterval(interval);
}, []);
```

3. **6개의 통계 카드**
- 전체 사용자 (primary)
- 활성 사용자 (success) + 비율 표시
- 역할 (secondary)
- 권한 (info)
- 오늘 로그인 (warning)
- 활성 세션 (error) + "최근 30분" 표시

4. **StatCard 컴포넌트 개선**
```typescript
<Paper sx={{
  transition: 'transform 0.2s, box-shadow 0.2s',
  '&:hover': {
    transform: 'translateY(-4px)',
    boxShadow: 4,
  },
}}>
```
- 호버 효과로 카드 상승
- 숫자 천 단위 구분자 표시 (`toLocaleString()`)
- 선택적 subtitle 지원

5. **파이 차트 (사용자 상태별 분포)**
```typescript
const getUserStatusChartOption = () => ({
  title: { text: '사용자 상태별 분포' },
  series: [{
    type: 'pie',
    radius: ['40%', '70%'],  // 도넛 차트
    data: userStats.map(stat => ({
      value: stat.count,
      name: stat.displayName,
      itemStyle: { color: colors[stat.status] }
    }))
  }]
});
```

**차트 특징**:
- 도넛 형태 파이 차트
- 상태별 색상: 활성(녹색), 비활성(회색), 잠김(빨강)
- 반응형 SVG 렌더링
- 툴팁으로 상세 정보 표시

6. **시스템 상태 패널**
- 활성화율 계산
- 금일 로그인율 계산
- 평균 역할당 사용자 수

7. **에러 처리 및 로딩 상태**
```typescript
if (loading && !stats) {
  return <CircularProgress />;
}

if (error) {
  return <Alert severity="error">{error}</Alert>;
}
```

### 3. 반응형 레이아웃

**Grid 브레이크포인트**:
```typescript
<Grid item xs={12} sm={6} md={4} lg={2}>
```
- xs: 모바일 (12칸 전체)
- sm: 태블릿 (6칸 = 2열)
- md: 데스크톱 (4칸 = 3열)
- lg: 대형 화면 (2칸 = 6열)

**차트 레이아웃**:
```typescript
<Grid item xs={12} md={6}>  // 모바일 전체, 데스크톱 절반
```

---

## 파일 변경 사항

### Backend (신규 파일)
1. `backend/src/main/java/kr/co/softice/mes/common/dto/dashboard/DashboardStatsResponse.java` (NEW)
2. `backend/src/main/java/kr/co/softice/mes/common/dto/dashboard/UserStatsResponse.java` (NEW)
3. `backend/src/main/java/kr/co/softice/mes/domain/service/DashboardService.java` (NEW)
4. `backend/src/main/java/kr/co/softice/mes/api/controller/DashboardController.java` (NEW)

### Backend (수정 파일)
1. `backend/src/main/java/kr/co/softice/mes/domain/repository/UserRepository.java`
   - `countByTenant()` 추가
   - `countByTenantAndStatus()` 추가
   - `countByTenantAndLastLoginAtAfter()` 추가
   - `LocalDateTime` import 추가

2. `backend/src/main/java/kr/co/softice/mes/domain/repository/RoleRepository.java`
   - `countByTenant()` 추가

### Frontend (신규 파일)
1. `frontend/src/services/dashboardService.ts` (NEW)

### Frontend (수정 파일)
1. `frontend/src/pages/Dashboard.tsx` (완전 재작성)
   - 실시간 데이터 로딩
   - Apache ECharts 차트
   - 자동 새로고침
   - 반응형 레이아웃

### Frontend (패키지)
- `echarts`: ^5.5.0
- `echarts-for-react`: ^3.0.2

---

## API 엔드포인트 추가

### GET /api/dashboard/stats
**Request**:
```
Headers:
  Authorization: Bearer {JWT_TOKEN}
  X-Tenant-ID: softice
```

**Response**:
```json
{
  "success": true,
  "message": "대시보드 통계 조회 성공",
  "data": {
    "totalUsers": 5,
    "activeUsers": 5,
    "totalRoles": 8,
    "totalPermissions": 24,
    "todayLogins": 1,
    "activeSessions": 1
  }
}
```

### GET /api/dashboard/user-stats
**Request**:
```
Headers:
  Authorization: Bearer {JWT_TOKEN}
  X-Tenant-ID: softice
```

**Response**:
```json
{
  "success": true,
  "message": "사용자 통계 조회 성공",
  "data": [
    { "status": "active", "count": 5, "displayName": "활성" },
    { "status": "inactive", "count": 0, "displayName": "비활성" },
    { "status": "locked", "count": 0, "displayName": "잠김" }
  ]
}
```

---

## 기술적 특징

### 1. 실시간 데이터 갱신
- **초기 로드**: 컴포넌트 마운트 시 즉시 데이터 로드
- **자동 갱신**: 30초마다 `setInterval`로 자동 갱신
- **클린업**: 컴포넌트 언마운트 시 interval 정리

### 2. 성능 최적화
- **병렬 요청**: `Promise.all`로 여러 API 동시 호출
- **SVG 렌더링**: ECharts를 SVG로 렌더링하여 성능 최적화
- **조건부 렌더링**: 로딩/에러 상태에 따른 효율적인 렌더링

### 3. 사용자 경험 (UX)
- **로딩 인디케이터**: CircularProgress로 로딩 상태 표시
- **에러 알림**: Alert 컴포넌트로 명확한 에러 메시지
- **호버 효과**: 카드 호버 시 상승 애니메이션
- **반응형**: 모든 화면 크기에 최적화된 레이아웃

### 4. 데이터 시각화
- **도넛 차트**: 중앙 공간 활용으로 정보 밀도 향상
- **색상 코딩**: 상태별 직관적인 색상 사용
- **툴팁**: 마우스 호버로 상세 정보 제공

---

## 구현 로직 설명

### 활성 세션 계산
```java
// 최근 30분 이내 로그인한 사용자를 활성 세션으로 간주
LocalDateTime recentLoginTime = LocalDateTime.now().minusMinutes(30);
long activeSessions = userRepository.countByTenantAndLastLoginAtAfter(tenant, recentLoginTime);
```

**근거**: 일반적인 세션 타임아웃은 30분이므로, 최근 30분 이내 로그인한 사용자를 현재 활성 상태로 판단

### 오늘 로그인 수 계산
```java
// 오늘 00:00부터 현재까지 로그인한 사용자 수
LocalDateTime todayStart = LocalDate.now().atStartOfDay();
long todayLogins = userRepository.countByTenantAndLastLoginAtAfter(tenant, todayStart);
```

**구현 방식**: UserEntity의 `lastLoginAt` 필드를 활용하여 집계

### 차트 데이터 매핑
```typescript
data: userStats.map((stat) => ({
  value: stat.count,
  name: stat.displayName,
  itemStyle: {
    color: colors[stat.status]
  },
}))
```

**색상 매핑**:
- `active` → `#4caf50` (녹색)
- `inactive` → `#9e9e9e` (회색)
- `locked` → `#f44336` (빨강)

---

## 테스트 시나리오

### 1. 기본 기능 테스트
- [x] 대시보드 로딩 시 통계 카드 표시
- [x] 파이 차트 정상 렌더링
- [x] 30초 후 자동 갱신 확인
- [x] 로딩 인디케이터 표시

### 2. 반응형 테스트
- [x] 모바일 (xs): 카드 1열 배치
- [x] 태블릿 (sm): 카드 2열 배치
- [x] 데스크톱 (md): 카드 3열 배치
- [x] 대형 화면 (lg): 카드 6열 배치

### 3. 에러 처리
- [x] API 실패 시 Alert 표시
- [x] 빈 데이터 처리
- [x] 네트워크 오류 처리

### 4. 데이터 정확성
- [x] 전체 사용자 수 = DB count
- [x] 활성화율 계산 정확성
- [x] 차트 데이터와 통계 일치

---

## 다음 단계 제안

### 1. 추가 차트
- [ ] 일별 로그인 추이 (Line Chart)
- [ ] 역할별 사용자 분포 (Bar Chart)
- [ ] 월별 활동 히트맵 (Heatmap)

### 2. 고급 기능
- [ ] 날짜 범위 선택 필터
- [ ] 차트 데이터 CSV 내보내기
- [ ] 대시보드 위젯 커스터마이징
- [ ] 실시간 알림 배지

### 3. 성능 개선
- [ ] 차트 lazy loading
- [ ] 데이터 캐싱 (React Query)
- [ ] WebSocket 실시간 업데이트

### 4. MES 모듈 통계
- [ ] 생산 현황 위젯
- [ ] 품질 지표 위젯
- [ ] 재고 현황 위젯

---

## 완료 상태

| 항목 | 상태 |
|------|------|
| 대시보드 요구사항 분석 | ✅ |
| 백엔드 통계 API 구현 | ✅ |
| 백엔드 빌드 및 재시작 | ✅ |
| Apache ECharts 설치 | ✅ |
| 대시보드 위젯 컴포넌트 구현 | ✅ |
| 실시간 데이터 연동 | ✅ |
| 자동 새로고침 (30초) | ✅ |
| 반응형 레이아웃 최적화 | ✅ |

---

**작업 완료 시간**: 2026-01-19 08:38
**담당**: Claude Sonnet 4.5
**총 구현 시간**: 약 50분
**추가된 코드**:
- Backend: 4개 신규 파일, 2개 수정 파일
- Frontend: 1개 신규 파일, 1개 전체 재작성
- 총 라인 수: 약 600+ lines
