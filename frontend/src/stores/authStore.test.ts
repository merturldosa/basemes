/**
 * Authentication Store Tests
 * @author Moon Myung-seop
 */

import { describe, it, expect, beforeEach } from 'vitest';
import { useAuthStore } from './authStore';
import { mockLocalStorage } from '@/test/test-utils';
import { act } from '@testing-library/react';

// Mock localStorage
beforeEach(() => {
  global.localStorage = mockLocalStorage() as Storage;
  // Reset store state
  useAuthStore.setState({
    user: null,
    isAuthenticated: false,
    isLoading: false,
    error: null,
  });
});

describe('AuthStore', () => {
  describe('initial state', () => {
    it('should have correct initial state', () => {
      const state = useAuthStore.getState();

      expect(state.user).toBeNull();
      expect(state.isAuthenticated).toBe(false);
      expect(state.isLoading).toBe(false);
      expect(state.error).toBeNull();
    });
  });

  describe('login', () => {
    it('should login successfully', async () => {
      const credentials = {
        username: 'admin',
        password: 'admin123',
        tenantId: 'default',
      };

      await act(async () => {
        await useAuthStore.getState().login(credentials);
      });

      const state = useAuthStore.getState();

      expect(state.isAuthenticated).toBe(true);
      expect(state.user).toBeDefined();
      expect(state.user?.username).toBe('admin');
      expect(state.isLoading).toBe(false);
      expect(state.error).toBeNull();
    });

    it('should set error on login failure', async () => {
      const credentials = {
        username: 'invalid',
        password: 'wrong',
        tenantId: 'default',
      };

      try {
        await act(async () => {
          await useAuthStore.getState().login(credentials);
        });
      } catch (error) {
        // Expected to throw
      }

      const state = useAuthStore.getState();

      expect(state.isAuthenticated).toBe(false);
      expect(state.user).toBeNull();
      expect(state.isLoading).toBe(false);
      expect(state.error).toBeTruthy();
    });

    it('should set loading state during login', async () => {
      const credentials = {
        username: 'admin',
        password: 'admin123',
        tenantId: 'default',
      };

      // Start login (don't await)
      const loginPromise = useAuthStore.getState().login(credentials);

      // Check loading state immediately
      const loadingState = useAuthStore.getState();
      expect(loadingState.isLoading).toBe(true);

      // Wait for login to complete
      await act(async () => {
        await loginPromise;
      });

      // Check final state
      const finalState = useAuthStore.getState();
      expect(finalState.isLoading).toBe(false);
    });
  });

  describe('logout', () => {
    it('should logout and clear user data', async () => {
      // Set authenticated state
      useAuthStore.setState({
        user: { id: 1, username: 'test', email: 'test@example.com' } as any,
        isAuthenticated: true,
      });

      await act(async () => {
        await useAuthStore.getState().logout();
      });

      const state = useAuthStore.getState();

      expect(state.user).toBeNull();
      expect(state.isAuthenticated).toBe(false);
      expect(state.isLoading).toBe(false);
    });
  });

  describe('setUser', () => {
    it('should set user and update authentication status', () => {
      const user = {
        id: 1,
        username: 'testuser',
        email: 'test@example.com',
      } as any;

      act(() => {
        useAuthStore.getState().setUser(user);
      });

      const state = useAuthStore.getState();

      expect(state.user).toEqual(user);
      expect(state.isAuthenticated).toBe(true);
    });

    it('should clear user when set to null', () => {
      // Set user first
      useAuthStore.setState({
        user: { id: 1, username: 'test' } as any,
        isAuthenticated: true,
      });

      act(() => {
        useAuthStore.getState().setUser(null);
      });

      const state = useAuthStore.getState();

      expect(state.user).toBeNull();
      expect(state.isAuthenticated).toBe(false);
    });
  });

  describe('clearError', () => {
    it('should clear error message', () => {
      // Set error first
      useAuthStore.setState({ error: 'Test error' });

      act(() => {
        useAuthStore.getState().clearError();
      });

      const state = useAuthStore.getState();

      expect(state.error).toBeNull();
    });
  });

  describe('initialize', () => {
    it('should initialize from localStorage', () => {
      const user = { id: 1, username: 'test', email: 'test@example.com' };
      localStorage.setItem('user', JSON.stringify(user));
      localStorage.setItem('accessToken', 'test-token');

      act(() => {
        useAuthStore.getState().initialize();
      });

      const state = useAuthStore.getState();

      expect(state.user).toEqual(user);
      expect(state.isAuthenticated).toBe(true);
    });

    it('should not set user when no data in localStorage', () => {
      act(() => {
        useAuthStore.getState().initialize();
      });

      const state = useAuthStore.getState();

      expect(state.user).toBeNull();
      expect(state.isAuthenticated).toBe(false);
    });
  });
});
