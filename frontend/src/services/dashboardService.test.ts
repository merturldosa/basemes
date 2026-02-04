/**
 * Dashboard Service Tests
 * @author Moon Myung-seop
 */

import { describe, it, expect, beforeEach } from 'vitest';
import dashboardService from './dashboardService';
import { mockLocalStorage } from '@/test/test-utils';

// Mock localStorage and setup auth
beforeEach(() => {
  global.localStorage = mockLocalStorage() as Storage;
  localStorage.setItem('accessToken', 'mock-token');
});

describe('DashboardService', () => {
  describe('getStats', () => {
    it('should fetch dashboard statistics', async () => {
      const stats = await dashboardService.getStats();

      expect(stats).toBeDefined();
      expect(stats.totalUsers).toBeGreaterThanOrEqual(0);
      expect(stats.activeUsers).toBeGreaterThanOrEqual(0);
      expect(stats.totalRoles).toBeGreaterThanOrEqual(0);
      expect(stats.totalPermissions).toBeGreaterThanOrEqual(0);
      expect(stats.todayLogins).toBeGreaterThanOrEqual(0);
      expect(stats.activeSessions).toBeGreaterThanOrEqual(0);
    });

    it('should return valid stat structure', async () => {
      const stats = await dashboardService.getStats();

      expect(typeof stats.totalUsers).toBe('number');
      expect(typeof stats.activeUsers).toBe('number');
      expect(typeof stats.totalRoles).toBe('number');
      expect(typeof stats.totalPermissions).toBe('number');
      expect(typeof stats.todayLogins).toBe('number');
      expect(typeof stats.activeSessions).toBe('number');
    });
  });

  describe('getUserStats', () => {
    it('should fetch user statistics', async () => {
      const userStats = await dashboardService.getUserStats();

      expect(Array.isArray(userStats)).toBe(true);
      expect(userStats.length).toBeGreaterThan(0);
    });

    it('should return valid user stat structure', async () => {
      const userStats = await dashboardService.getUserStats();

      userStats.forEach((stat) => {
        expect(stat).toHaveProperty('status');
        expect(stat).toHaveProperty('displayName');
        expect(stat).toHaveProperty('count');
        expect(typeof stat.count).toBe('number');
      });
    });

    it('should include ACTIVE, INACTIVE, and LOCKED statuses', async () => {
      const userStats = await dashboardService.getUserStats();

      const statuses = userStats.map((s) => s.displayName);
      expect(statuses).toContain('활성');
      expect(statuses).toContain('비활성');
      expect(statuses).toContain('잠김');
    });
  });

  describe('getLoginTrend', () => {
    it('should fetch login trend data with default 7 days', async () => {
      const loginTrend = await dashboardService.getLoginTrend();

      expect(Array.isArray(loginTrend)).toBe(true);
      expect(loginTrend.length).toBe(7);
    });

    it('should fetch login trend data for 30 days', async () => {
      const loginTrend = await dashboardService.getLoginTrend(30);

      expect(Array.isArray(loginTrend)).toBe(true);
      expect(loginTrend.length).toBe(30);
    });

    it('should return valid login trend structure', async () => {
      const loginTrend = await dashboardService.getLoginTrend();

      loginTrend.forEach((trend) => {
        expect(trend).toHaveProperty('date');
        expect(trend).toHaveProperty('dateLabel');
        expect(trend).toHaveProperty('loginCount');
        expect(typeof trend.loginCount).toBe('number');
        expect(trend.loginCount).toBeGreaterThanOrEqual(0);
      });
    });

    it('should have correct date format', async () => {
      const loginTrend = await dashboardService.getLoginTrend();

      loginTrend.forEach((trend) => {
        // Check ISO date format
        expect(trend.date).toMatch(/^\d{4}-\d{2}-\d{2}$/);
        expect(trend.dateLabel).toBeTruthy();
      });
    });
  });

  describe('getRoleDistribution', () => {
    it('should fetch role distribution data', async () => {
      const roleDistribution = await dashboardService.getRoleDistribution();

      expect(Array.isArray(roleDistribution)).toBe(true);
      expect(roleDistribution.length).toBeGreaterThan(0);
    });

    it('should return valid role distribution structure', async () => {
      const roleDistribution = await dashboardService.getRoleDistribution();

      roleDistribution.forEach((role) => {
        expect(role).toHaveProperty('roleName');
        expect(role).toHaveProperty('userCount');
        expect(typeof role.roleName).toBe('string');
        expect(typeof role.userCount).toBe('number');
        expect(role.userCount).toBeGreaterThanOrEqual(0);
      });
    });

    it('should include common roles', async () => {
      const roleDistribution = await dashboardService.getRoleDistribution();

      const roleNames = roleDistribution.map((r) => r.roleName);
      expect(roleNames.length).toBeGreaterThan(0);
      // At least one role should exist
      expect(roleNames.some((name) => name.length > 0)).toBe(true);
    });
  });
});
