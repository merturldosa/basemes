/**
 * MSW API Mock Handlers
 * @author Moon Myung-seop
 */

import { http, HttpResponse } from 'msw';

const API_BASE_URL = 'http://localhost:8080/api';

export const handlers = [
  // Auth endpoints
  http.post(`${API_BASE_URL}/auth/login`, async ({ request }) => {
    const body = await request.json() as { username: string; password: string };

    if (body.username === 'admin' && body.password === 'admin123') {
      return HttpResponse.json({
        success: true,
        data: {
          token: 'mock-jwt-token',
          user: {
            id: 1,
            username: 'admin',
            email: 'admin@soice.co.kr',
            name: '관리자',
            status: 'ACTIVE',
            roles: ['ADMIN'],
          },
        },
        message: '로그인 성공',
      });
    }

    return HttpResponse.json(
      {
        success: false,
        message: '잘못된 사용자명 또는 비밀번호입니다.',
      },
      { status: 401 }
    );
  }),

  http.post(`${API_BASE_URL}/auth/logout`, () => {
    return HttpResponse.json({
      success: true,
      message: '로그아웃 성공',
    });
  }),

  http.get(`${API_BASE_URL}/auth/me`, ({ request }) => {
    const authHeader = request.headers.get('Authorization');

    if (authHeader && authHeader.startsWith('Bearer ')) {
      return HttpResponse.json({
        success: true,
        data: {
          id: 1,
          username: 'admin',
          email: 'admin@soice.co.kr',
          name: '관리자',
          status: 'ACTIVE',
          roles: ['ADMIN'],
        },
      });
    }

    return HttpResponse.json(
      {
        success: false,
        message: 'Unauthorized',
      },
      { status: 401 }
    );
  }),

  // Users endpoints
  http.get(`${API_BASE_URL}/users`, () => {
    return HttpResponse.json({
      success: true,
      data: {
        content: [
          {
            id: 1,
            username: 'admin',
            email: 'admin@soice.co.kr',
            name: '관리자',
            status: 'ACTIVE',
            roles: ['ADMIN'],
            createdAt: '2024-01-01T00:00:00Z',
          },
          {
            id: 2,
            username: 'user',
            email: 'user@soice.co.kr',
            name: '사용자',
            status: 'ACTIVE',
            roles: ['USER'],
            createdAt: '2024-01-02T00:00:00Z',
          },
        ],
        totalElements: 2,
        totalPages: 1,
        size: 20,
        number: 0,
      },
    });
  }),

  http.get(`${API_BASE_URL}/users/:id`, ({ params }) => {
    const { id } = params;

    return HttpResponse.json({
      success: true,
      data: {
        id: Number(id),
        username: 'testuser',
        email: 'test@soice.co.kr',
        name: '테스트 사용자',
        status: 'ACTIVE',
        roles: ['USER'],
        createdAt: '2024-01-01T00:00:00Z',
      },
    });
  }),

  // Dashboard endpoints
  http.get(`${API_BASE_URL}/dashboard/stats`, () => {
    return HttpResponse.json({
      success: true,
      data: {
        totalUsers: 100,
        activeUsers: 85,
        totalRoles: 5,
        totalPermissions: 50,
        todayLogins: 25,
        activeSessions: 15,
      },
    });
  }),

  http.get(`${API_BASE_URL}/dashboard/user-stats`, () => {
    return HttpResponse.json({
      success: true,
      data: [
        { status: 'ACTIVE', displayName: '활성', count: 85 },
        { status: 'INACTIVE', displayName: '비활성', count: 10 },
        { status: 'LOCKED', displayName: '잠김', count: 5 },
      ],
    });
  }),

  http.get(`${API_BASE_URL}/dashboard/login-trend`, () => {
    return HttpResponse.json({
      success: true,
      data: Array.from({ length: 7 }, (_, i) => ({
        date: new Date(Date.now() - (6 - i) * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
        dateLabel: new Date(Date.now() - (6 - i) * 24 * 60 * 60 * 1000).toLocaleDateString('ko-KR', { month: 'short', day: 'numeric' }),
        loginCount: Math.floor(Math.random() * 50) + 10,
      })),
    });
  }),

  http.get(`${API_BASE_URL}/dashboard/role-distribution`, () => {
    return HttpResponse.json({
      success: true,
      data: [
        { roleName: 'ADMIN', userCount: 5 },
        { roleName: 'MANAGER', userCount: 15 },
        { roleName: 'USER', userCount: 65 },
        { roleName: 'VIEWER', userCount: 15 },
      ],
    });
  }),
];
