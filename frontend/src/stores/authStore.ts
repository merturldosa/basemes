/**
 * Authentication Store
 * Zustand를 사용한 인증 상태 관리
 * @author Moon Myung-seop
 */

import { create } from 'zustand';
import { User, LoginRequest } from '@/types';
import authService from '@/services/authService';
import { getErrorMessage } from '@/utils/errorUtils';

interface AuthState {
  user: User | null;
  tenantId: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;

  // Actions
  login: (credentials: LoginRequest) => Promise<void>;
  logout: () => Promise<void>;
  setUser: (user: User | null) => void;
  clearError: () => void;
  initialize: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  tenantId: null,
  isAuthenticated: false,
  isLoading: false,
  error: null,

  login: async (credentials: LoginRequest) => {
    set({ isLoading: true, error: null });
    try {
      const response = await authService.login(credentials);
      set({
        user: response.user,
        tenantId: response.user?.tenantId || null,
        isAuthenticated: true,
        isLoading: false,
      });
    } catch (error) {
      set({
        error: getErrorMessage(error, '로그인에 실패했습니다.'),
        isLoading: false,
      });
      throw error;
    }
  },

  logout: async () => {
    set({ isLoading: true });
    try {
      await authService.logout();
    } finally {
      set({
        user: null,
        tenantId: null,
        isAuthenticated: false,
        isLoading: false,
      });
    }
  },

  setUser: (user: User | null) => {
    set({
      user,
      tenantId: user?.tenantId || null,
      isAuthenticated: !!user,
    });
  },

  clearError: () => {
    set({ error: null });
  },

  initialize: () => {
    const user = authService.getCurrentUser();
    const isAuthenticated = authService.isAuthenticated();
    set({ user, tenantId: user?.tenantId || null, isAuthenticated });
  },
}));
