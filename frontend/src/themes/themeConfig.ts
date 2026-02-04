/**
 * Theme Configuration
 * 산업별 테마 설정
 * @author Moon Myung-seop
 */

import { createTheme, ThemeOptions } from '@mui/material/styles';

// 화학 제조업 테마
export const chemicalTheme: ThemeOptions = {
  palette: {
    mode: 'light',
    primary: {
      main: '#0d47a1', // Deep Blue
      light: '#5472d3',
      dark: '#002171',
    },
    secondary: {
      main: '#1976d2', // Blue
      light: '#63a4ff',
      dark: '#004ba0',
    },
    success: {
      main: '#388e3c',
    },
    warning: {
      main: '#f57c00',
    },
    error: {
      main: '#d32f2f',
    },
    background: {
      default: '#fafafa',
      paper: '#ffffff',
    },
  },
  typography: {
    fontFamily: '"Noto Sans KR", "Roboto", "Helvetica", "Arial", sans-serif',
  },
};

// 전자/전기 제조업 테마
export const electronicsTheme: ThemeOptions = {
  palette: {
    mode: 'light',
    primary: {
      main: '#1565c0', // Tech Blue
      light: '#5e92f3',
      dark: '#003c8f',
    },
    secondary: {
      main: '#00838f', // Cyan
      light: '#4fb3bf',
      dark: '#005662',
    },
    success: {
      main: '#2e7d32',
    },
    warning: {
      main: '#ef6c00',
    },
    error: {
      main: '#c62828',
    },
    background: {
      default: '#f5f5f5',
      paper: '#ffffff',
    },
  },
  typography: {
    fontFamily: '"Noto Sans KR", "Roboto", "Helvetica", "Arial", sans-serif',
  },
};

// 의료기기 제조업 테마
export const medicalTheme: ThemeOptions = {
  palette: {
    mode: 'light',
    primary: {
      main: '#00695c', // Medical Teal
      light: '#439889',
      dark: '#003d33',
    },
    secondary: {
      main: '#0097a7', // Cyan
      light: '#56c8d8',
      dark: '#006978',
    },
    success: {
      main: '#2e7d32',
    },
    warning: {
      main: '#f57c00',
    },
    error: {
      main: '#c62828',
    },
    background: {
      default: '#f0f4f3',
      paper: '#ffffff',
    },
  },
  typography: {
    fontFamily: '"Noto Sans KR", "Roboto", "Helvetica", "Arial", sans-serif',
  },
};

// 식품/음료 제조업 테마
export const foodTheme: ThemeOptions = {
  palette: {
    mode: 'light',
    primary: {
      main: '#558b2f', // Green
      light: '#85bb5c',
      dark: '#255d00',
    },
    secondary: {
      main: '#689f38', // Light Green
      light: '#99d066',
      dark: '#387002',
    },
    success: {
      main: '#388e3c',
    },
    warning: {
      main: '#f57c00',
    },
    error: {
      main: '#d32f2f',
    },
    background: {
      default: '#f1f8e9',
      paper: '#ffffff',
    },
  },
  typography: {
    fontFamily: '"Noto Sans KR", "Roboto", "Helvetica", "Arial", sans-serif',
  },
};

// 기본 테마
export const defaultTheme: ThemeOptions = {
  palette: {
    mode: 'light',
    primary: {
      main: '#1976d2', // Material Blue
      light: '#63a4ff',
      dark: '#004ba0',
    },
    secondary: {
      main: '#dc004e', // Material Pink
      light: '#ff5c8d',
      dark: '#9a0036',
    },
    success: {
      main: '#4caf50',
    },
    warning: {
      main: '#ff9800',
    },
    error: {
      main: '#f44336',
    },
    background: {
      default: '#fafafa',
      paper: '#ffffff',
    },
  },
  typography: {
    fontFamily: '"Noto Sans KR", "Roboto", "Helvetica", "Arial", sans-serif',
  },
};

// 테마 맵
export const themeMap: Record<string, ThemeOptions> = {
  CHEMICAL: chemicalTheme,
  ELECTRONICS: electronicsTheme,
  MEDICAL: medicalTheme,
  FOOD: foodTheme,
  DEFAULT: defaultTheme,
  GENERAL: defaultTheme,
};

// 다크 모드 테마 생성 함수
const createDarkTheme = (baseTheme: ThemeOptions): ThemeOptions => {
  return {
    ...baseTheme,
    palette: {
      ...baseTheme.palette,
      mode: 'dark',
      background: {
        default: '#121212',
        paper: '#1e1e1e',
      },
      text: {
        primary: '#ffffff',
        secondary: 'rgba(255, 255, 255, 0.7)',
      },
    },
  };
};

// 테마 생성 함수
export const createMesTheme = (themeCode: string = 'DEFAULT', mode: 'light' | 'dark' = 'light') => {
  let themeConfig = themeMap[themeCode] || defaultTheme;

  // 다크 모드인 경우 다크 테마로 변환
  if (mode === 'dark') {
    themeConfig = createDarkTheme(themeConfig);
  }

  return createTheme(themeConfig);
};
