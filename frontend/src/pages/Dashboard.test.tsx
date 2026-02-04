/**
 * Dashboard Page Tests
 * @author Moon Myung-seop
 */

import { describe, it, expect, beforeEach } from 'vitest';
import { screen } from '@testing-library/react';
import { renderWithProviders } from '@/test/test-utils';
import Dashboard from './Dashboard';
import { useAuthStore } from '@/stores/authStore';

beforeEach(() => {
  // Set authenticated user
  useAuthStore.setState({
    user: {
      id: 1,
      username: 'admin',
      email: 'admin@soice.co.kr',
      name: '관리자',
      status: 'ACTIVE',
      roles: ['ADMIN'],
    } as any,
    isAuthenticated: true,
    isLoading: false,
    error: null,
  });
});

describe('Dashboard', () => {
  it('should render dashboard title', () => {
    renderWithProviders(<Dashboard />);

    expect(screen.getByText('실시간 대시보드')).toBeInTheDocument();
  });

  it('should display welcome message', () => {
    renderWithProviders(<Dashboard />);

    expect(screen.getByText(/환영합니다/i)).toBeInTheDocument();
  });

  it('should show quick action buttons', () => {
    renderWithProviders(<Dashboard />);

    // Quick action buttons should be present
    const buttons = screen.getAllByRole('button');
    expect(buttons.length).toBeGreaterThan(0);
  });

  it('should display user information', () => {
    renderWithProviders(<Dashboard />);

    // Should show user name or username
    const userName = screen.queryByText('관리자') || screen.queryByText('admin');
    expect(userName).toBeInTheDocument();
  });
});
