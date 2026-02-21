/**
 * Authentication Service Tests
 * @author Moon Myung-seop
 */

import { describe, it, expect, beforeEach } from 'vitest';
import authService from './authService';
import { mockLocalStorage } from '@/test/test-utils';

// Mock localStorage
beforeEach(() => {
  global.localStorage = mockLocalStorage() as Storage;
});

describe('AuthService', () => {
  describe('login', () => {
    it('should login successfully and store tokens', async () => {
      const credentials = {
        username: 'admin',
        password: 'admin123',
        tenantId: 'default',
      };

      const response = await authService.login(credentials);

      expect(response).toBeDefined();
      expect(response.token).toBeDefined();
      expect(response.user).toBeDefined();
      expect(response.user.username).toBe('admin');

      // Check if tokens are stored
      expect(localStorage.getItem('accessToken')).toBeTruthy();
      expect(localStorage.getItem('user')).toBeTruthy();
    });

    it('should throw error for invalid credentials', async () => {
      const credentials = {
        username: 'invalid',
        password: 'wrong',
        tenantId: 'default',
      };

      await expect(authService.login(credentials)).rejects.toThrow();
    });
  });

  describe('logout', () => {
    it('should clear local storage on logout', async () => {
      // Set some data in localStorage
      localStorage.setItem('accessToken', 'test-token');
      localStorage.setItem('refreshToken', 'test-refresh');
      localStorage.setItem('user', JSON.stringify({ id: 1, username: 'test' }));
      localStorage.setItem('tenantId', 'default');

      await authService.logout();

      expect(localStorage.getItem('accessToken')).toBeNull();
      expect(localStorage.getItem('refreshToken')).toBeNull();
      expect(localStorage.getItem('user')).toBeNull();
      expect(localStorage.getItem('tenantId')).toBeNull();
    });
  });

  describe('getCurrentUser', () => {
    it('should return user from localStorage', () => {
      const user = { id: 1, username: 'testuser', email: 'test@example.com' };
      localStorage.setItem('user', JSON.stringify(user));

      const currentUser = authService.getCurrentUser();

      expect(currentUser).toEqual(user);
    });

    it('should return null when no user in localStorage', () => {
      const currentUser = authService.getCurrentUser();

      expect(currentUser).toBeNull();
    });

    it('should return null for invalid JSON', () => {
      localStorage.setItem('user', 'invalid-json');

      const currentUser = authService.getCurrentUser();

      expect(currentUser).toBeNull();
    });
  });

  describe('isAuthenticated', () => {
    it('should return true when token exists', () => {
      localStorage.setItem('accessToken', 'test-token');

      expect(authService.isAuthenticated()).toBe(true);
    });

    it('should return false when no token', () => {
      expect(authService.isAuthenticated()).toBe(false);
    });
  });

  describe('getAccessToken', () => {
    it('should return access token from localStorage', () => {
      const token = 'test-access-token';
      localStorage.setItem('accessToken', token);

      expect(authService.getAccessToken()).toBe(token);
    });

    it('should return null when no token', () => {
      expect(authService.getAccessToken()).toBeNull();
    });
  });
});
