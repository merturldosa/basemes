# SoIce MES Frontend

React + TypeScript + Vite 기반의 모던 프론트엔드 애플리케이션

## 기술 스택

- **React 18**: UI 라이브러리
- **TypeScript**: 타입 안전성
- **Vite 5**: 빠른 빌드 도구
- **Material-UI 5**: UI 컴포넌트 라이브러리
- **Zustand**: 상태 관리
- **Axios**: HTTP 클라이언트
- **React Router 6**: 라우팅
- **Vitest**: 테스트 프레임워크
- **React Testing Library**: 컴포넌트 테스팅
- **MSW**: API Mocking

## 시작하기

### 의존성 설치

```bash
npm install
```

### 개발 서버 실행

```bash
npm run dev
```

브라우저에서 http://localhost:3000 접속

### 빌드

```bash
npm run build
```

빌드 결과물은 `dist/` 폴더에 생성됩니다.

### 프리뷰

```bash
npm run preview
```

### 테스트

```bash
# 테스트 실행 (watch 모드)
npm run test

# 테스트 1회 실행
npm run test:run

# 테스트 UI
npm run test:ui

# 커버리지 리포트
npm run test:coverage
```

## 프로젝트 구조

```
src/
├── components/        # 재사용 가능한 컴포넌트
│   └── layout/       # 레이아웃 컴포넌트
├── pages/            # 페이지 컴포넌트
├── services/         # API 서비스
│   ├── api.ts       # Axios 클라이언트
│   └── authService.ts
├── stores/           # Zustand 스토어
│   ├── authStore.ts
│   └── themeStore.ts
├── test/             # 테스트 유틸리티
│   ├── mocks/       # MSW API mocks
│   │   ├── handlers.ts
│   │   └── server.ts
│   ├── setup.ts     # 테스트 전역 설정
│   └── test-utils.tsx # 테스트 헬퍼 함수
├── themes/           # MUI 테마 설정
│   └── themeConfig.ts
├── types/            # TypeScript 타입 정의
│   └── index.ts
├── App.tsx           # 메인 앱 컴포넌트
└── main.tsx          # 진입점
```

## 환경 변수

`.env.development`:
```
VITE_API_BASE_URL=http://localhost:8080/api
```

`.env.production`:
```
VITE_API_BASE_URL=/api
```

## 주요 기능

### 인증
- JWT 토큰 기반 인증
- 자동 토큰 갱신
- Protected Routes

### 테마 시스템
- 5가지 산업별 프리셋 테마
- 실시간 테마 전환
- Material-UI 테마 커스터마이징

### 상태 관리
- Zustand를 사용한 간결한 상태 관리
- Auth Store: 인증 상태
- Theme Store: 테마 상태

### API 통신
- Axios 인터셉터로 자동 토큰 주입
- 에러 핸들링
- 토큰 갱신 로직

## 코딩 스타일

- ESLint 설정 준수
- TypeScript strict 모드
- Functional Components + Hooks
- 명명 규칙:
  - 컴포넌트: PascalCase
  - 함수/변수: camelCase
  - 상수: UPPER_SNAKE_CASE

## 라우트

- `/login` - 로그인 페이지
- `/` - 대시보드 (Protected)
- `/users` - 사용자 관리 (Protected)
- `/roles` - 역할 관리 (Protected)
- `/permissions` - 권한 관리 (Protected)
- `/audit-logs` - 감사 로그 (Protected)
- `/themes` - 테마 설정 (Protected)

## 개발 가이드

### 새 페이지 추가

1. `src/pages/` 에 페이지 컴포넌트 생성
2. `src/App.tsx` 에 라우트 추가
3. 필요시 사이드바 메뉴 항목 추가 (`DashboardLayout.tsx`)

### API 서비스 추가

1. `src/services/` 에 서비스 파일 생성
2. `apiClient` 사용하여 API 호출
3. TypeScript 타입 정의 (`src/types/`)

### 새 스토어 추가

```typescript
import { create } from 'zustand';

interface MyStore {
  data: any;
  setData: (data: any) => void;
}

export const useMyStore = create<MyStore>((set) => ({
  data: null,
  setData: (data) => set({ data }),
}));
```

## 테스팅

### 테스트 작성

모든 새로운 기능은 테스트와 함께 작성되어야 합니다.

```typescript
// LoginPage.test.tsx
import { describe, it, expect } from 'vitest';
import { screen } from '@testing-library/react';
import { renderWithProviders, userEvent } from '@/test/test-utils';
import LoginPage from './LoginPage';

describe('LoginPage', () => {
  it('should render login form', () => {
    renderWithProviders(<LoginPage />);

    expect(screen.getByLabelText(/사용자명/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /로그인/i })).toBeInTheDocument();
  });

  it('should handle login', async () => {
    renderWithProviders(<LoginPage />);
    const user = userEvent.setup();

    await user.type(screen.getByLabelText(/사용자명/i), 'admin');
    await user.type(screen.getByLabelText(/비밀번호/i), 'admin123');
    await user.click(screen.getByRole('button', { name: /로그인/i }));

    // Assert login success
  });
});
```

### API Mocking

테스트에서 MSW를 사용하여 API를 모킹합니다:

```typescript
// src/test/mocks/handlers.ts
import { http, HttpResponse } from 'msw';

export const handlers = [
  http.get('/api/users', () => {
    return HttpResponse.json({
      success: true,
      data: [{ id: 1, name: 'Test User' }],
    });
  }),
];
```

### 커버리지 목표

- **Statements**: 60%+
- **Branches**: 50%+
- **Functions**: 60%+
- **Lines**: 60%+

## 문제 해결

### 포트 충돌
기본 포트(3000)가 사용 중이면 `vite.config.ts`에서 변경:

```typescript
server: {
  port: 3001,
}
```

### API 연결 실패
백엔드 서버가 실행 중인지 확인하고 환경 변수의 API URL을 확인하세요.

## 라이선스

Proprietary - (주)소프트아이스
