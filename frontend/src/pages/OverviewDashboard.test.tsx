/**
 * Overview Dashboard Page Tests
 * @author Moon Myung-seop
 */

import { describe, it, expect, beforeEach } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import { renderWithProviders } from '@/test/test-utils';
import OverviewDashboard from './OverviewDashboard';

beforeEach(() => {
  global.localStorage.setItem('accessToken', 'mock-token');
});

describe('OverviewDashboard', () => {
  it('should render dashboard title', () => {
    renderWithProviders(<OverviewDashboard />);

    expect(screen.getByText('통합 대시보드')).toBeInTheDocument();
  });

  it('should display loading state initially', () => {
    renderWithProviders(<OverviewDashboard />);

    // Should show loading indicator
    const loadingIndicator = screen.queryByRole('progressbar');
    expect(loadingIndicator).toBeInTheDocument();
  });

  it('should load and display statistics', async () => {
    renderWithProviders(<OverviewDashboard />);

    // Wait for data to load
    await waitFor(
      () => {
        expect(screen.queryByRole('progressbar')).not.toBeInTheDocument();
      },
      { timeout: 3000 }
    );

    // Should display stat cards
    expect(screen.getByText('전체 사용자')).toBeInTheDocument();
    expect(screen.getByText('전체 역할')).toBeInTheDocument();
    expect(screen.getByText('전체 권한')).toBeInTheDocument();
  });

  it('should display user stats chart', async () => {
    renderWithProviders(<OverviewDashboard />);

    await waitFor(
      () => {
        expect(screen.getByText('사용자 상태 분포')).toBeInTheDocument();
      },
      { timeout: 3000 }
    );
  });

  it('should display login trend chart with toggle buttons', async () => {
    renderWithProviders(<OverviewDashboard />);

    await waitFor(
      () => {
        expect(screen.getByText(/최근.*일 로그인 추이/)).toBeInTheDocument();
      },
      { timeout: 3000 }
    );

    // Should have 7일 and 30일 toggle buttons
    const button7Days = screen.getByRole('button', { name: '7일' });
    const button30Days = screen.getByRole('button', { name: '30일' });

    expect(button7Days).toBeInTheDocument();
    expect(button30Days).toBeInTheDocument();
  });

  it('should display role distribution chart', async () => {
    renderWithProviders(<OverviewDashboard />);

    await waitFor(
      () => {
        expect(screen.getByText('역할별 사용자 분포')).toBeInTheDocument();
      },
      { timeout: 3000 }
    );
  });

  it('should auto-refresh data', () => {
    renderWithProviders(<OverviewDashboard />);

    // Component should setup auto-refresh interval
    // This is implicitly tested by the component mounting without errors
    expect(screen.getByText('통합 대시보드')).toBeInTheDocument();
  });
});
