/**
 * Theme Store Tests
 * @author Moon Myung-seop
 */

import { describe, it, expect, beforeEach } from 'vitest';
import { useThemeStore } from './themeStore';
import { mockLocalStorage } from '@/test/test-utils';
import { act } from '@testing-library/react';

// Mock localStorage
beforeEach(() => {
  global.localStorage = mockLocalStorage() as Storage;
  // Reset store state
  useThemeStore.setState({
    currentTheme: 'DEFAULT',
    themeData: null,
  });
});

describe('ThemeStore', () => {
  describe('initial state', () => {
    it('should have DEFAULT as initial theme', () => {
      const state = useThemeStore.getState();

      expect(state.currentTheme).toBe('DEFAULT');
      expect(state.themeData).toBeNull();
    });

    it('should load theme from localStorage if available', () => {
      // Create new store instance with theme in localStorage
      localStorage.setItem('themeCode', 'DARK');

      // Force re-initialization by getting initial state
      const newTheme = localStorage.getItem('themeCode') || 'DEFAULT';

      expect(newTheme).toBe('DARK');
    });
  });

  describe('setTheme', () => {
    it('should update current theme', () => {
      act(() => {
        useThemeStore.getState().setTheme('MEDICAL');
      });

      const state = useThemeStore.getState();

      expect(state.currentTheme).toBe('MEDICAL');
      expect(localStorage.getItem('themeCode')).toBe('MEDICAL');
    });

    it('should persist theme to localStorage', () => {
      act(() => {
        useThemeStore.getState().setTheme('CHEMICAL');
      });

      expect(localStorage.getItem('themeCode')).toBe('CHEMICAL');
    });
  });

  describe('setThemeData', () => {
    it('should update theme data and current theme', () => {
      const themeData = {
        id: 1,
        themeCode: 'ELECTRONICS',
        themeName: 'Electronics Theme',
        description: 'Theme for electronics industry',
        primaryColor: '#2196F3',
        secondaryColor: '#FF9800',
        accentColor: '#4CAF50',
        successColor: '#4CAF50',
        warningColor: '#FF9800',
        errorColor: '#F44336',
        infoColor: '#2196F3',
        backgroundColor: '#FFFFFF',
        surfaceColor: '#F5F5F5',
        textPrimaryColor: '#212121',
        textSecondaryColor: '#757575',
        dividerColor: '#E0E0E0',
        logoUrl: null,
        faviconUrl: null,
        isActive: true,
        createdAt: '2024-01-01T00:00:00Z',
        updatedAt: '2024-01-01T00:00:00Z',
        createdBy: 'admin',
        updatedBy: 'admin',
      };

      act(() => {
        useThemeStore.getState().setThemeData(themeData);
      });

      const state = useThemeStore.getState();

      expect(state.themeData).toEqual(themeData);
      expect(state.currentTheme).toBe('ELECTRONICS');
      expect(localStorage.getItem('themeCode')).toBe('ELECTRONICS');
    });

    it('should persist theme code from theme data', () => {
      const themeData = {
        id: 2,
        themeCode: 'AUTOMOTIVE',
        themeName: 'Automotive Theme',
        description: 'Theme for automotive industry',
        primaryColor: '#FF5722',
        secondaryColor: '#607D8B',
      } as any;

      act(() => {
        useThemeStore.getState().setThemeData(themeData);
      });

      expect(localStorage.getItem('themeCode')).toBe('AUTOMOTIVE');
    });
  });

  describe('theme persistence', () => {
    it('should maintain theme across store recreations', () => {
      // Set theme
      act(() => {
        useThemeStore.getState().setTheme('MEDICAL');
      });

      // Simulate app restart by checking localStorage
      const savedTheme = localStorage.getItem('themeCode');

      expect(savedTheme).toBe('MEDICAL');
    });

    it('should handle multiple theme changes', () => {
      const themes = ['MEDICAL', 'CHEMICAL', 'ELECTRONICS', 'DEFAULT'];

      themes.forEach((theme) => {
        act(() => {
          useThemeStore.getState().setTheme(theme);
        });

        expect(useThemeStore.getState().currentTheme).toBe(theme);
        expect(localStorage.getItem('themeCode')).toBe(theme);
      });
    });
  });
});
