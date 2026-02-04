/**
 * Dashboard Service
 * 대시보드 API 서비스
 * @author Moon Myung-seop
 */

import apiClient from './api';

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

export interface LoginTrend {
  date: string;
  loginCount: number;
  dateLabel: string;
}

export interface RoleDistribution {
  roleCode: string;
  roleName: string;
  userCount: number;
}

class DashboardService {
  private basePath = '/dashboard';

  /**
   * 대시보드 통계 조회
   */
  async getStats(): Promise<DashboardStats> {
    const response = await apiClient.get<DashboardStats>(`${this.basePath}/stats`);
    return response.data;
  }

  /**
   * 사용자 상태별 통계
   */
  async getUserStats(): Promise<UserStats[]> {
    const response = await apiClient.get<UserStats[]>(`${this.basePath}/user-stats`);
    return response.data;
  }

  /**
   * 일별 로그인 추이 조회
   * @param days 조회할 일수 (기본 7일)
   */
  async getLoginTrend(days: number = 7): Promise<LoginTrend[]> {
    const response = await apiClient.get<LoginTrend[]>(`${this.basePath}/login-trend`, {
      params: { days }
    });
    return response.data;
  }

  /**
   * 역할별 사용자 분포
   */
  async getRoleDistribution(): Promise<RoleDistribution[]> {
    const response = await apiClient.get<RoleDistribution[]>(`${this.basePath}/role-distribution`);
    return response.data;
  }
}

export const dashboardService = new DashboardService();
export default dashboardService;
