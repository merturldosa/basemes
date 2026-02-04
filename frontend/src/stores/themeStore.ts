/**
 * Theme Store
 * 테마 상태 관리
 * @author Moon Myung-seop
 */

import { create } from 'zustand';
import { Theme as ThemeType } from '@/types';

type ThemeMode = 'light' | 'dark';

interface ThemeState {
  currentTheme: string;
  themeData: ThemeType | null;
  mode: ThemeMode;

  // Actions
  setTheme: (themeCode: string) => void;
  setThemeData: (theme: ThemeType) => void;
  toggleMode: () => void;
  setMode: (mode: ThemeMode) => void;
}

export const useThemeStore = create<ThemeState>((set, get) => ({
  currentTheme: localStorage.getItem('themeCode') || 'DEFAULT',
  themeData: null,
  mode: (localStorage.getItem('themeMode') as ThemeMode) || 'light',

  setTheme: (themeCode: string) => {
    localStorage.setItem('themeCode', themeCode);
    set({ currentTheme: themeCode });
  },

  setThemeData: (theme: ThemeType) => {
    set({ themeData: theme, currentTheme: theme.themeCode });
    localStorage.setItem('themeCode', theme.themeCode);
  },

  toggleMode: () => {
    const newMode = get().mode === 'light' ? 'dark' : 'light';
    localStorage.setItem('themeMode', newMode);
    set({ mode: newMode });
  },

  setMode: (mode: ThemeMode) => {
    localStorage.setItem('themeMode', mode);
    set({ mode });
  },
}));
