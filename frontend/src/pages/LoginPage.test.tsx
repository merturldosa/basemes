/**
 * Login Page Tests
 * @author Moon Myung-seop
 */

import { describe, it, expect, beforeEach, vi } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import { renderWithProviders, userEvent } from '@/test/test-utils';
import LoginPage from './LoginPage';
import { useAuthStore } from '@/stores/authStore';

const mockNavigate = vi.fn();

// Mock react-router-dom
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

beforeEach(() => {
  mockNavigate.mockClear();
  useAuthStore.setState({
    user: null,
    isAuthenticated: false,
    isLoading: false,
    error: null,
  });
});

describe('LoginPage', () => {
  it('should render login form', () => {
    renderWithProviders(<LoginPage />);

    expect(screen.getByText('SDS MES')).toBeInTheDocument();
    expect(screen.getByText('Manufacturing Execution System')).toBeInTheDocument();
    expect(screen.getByLabelText(/테넌트 ID/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/사용자명/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/비밀번호/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /로그인/i })).toBeInTheDocument();
  });

  it('should handle form input changes', async () => {
    renderWithProviders(<LoginPage />);
    const user = userEvent.setup();

    const tenantInput = screen.getByLabelText(/테넌트 ID/i);
    const usernameInput = screen.getByLabelText(/사용자명/i);
    const passwordInput = screen.getByLabelText(/비밀번호/i);

    await user.type(tenantInput, 'default');
    await user.type(usernameInput, 'admin');
    await user.type(passwordInput, 'admin123');

    expect(tenantInput).toHaveValue('default');
    expect(usernameInput).toHaveValue('admin');
    expect(passwordInput).toHaveValue('admin123');
  });

  it('should toggle password visibility', async () => {
    renderWithProviders(<LoginPage />);
    const user = userEvent.setup();

    const passwordInput = screen.getByLabelText(/비밀번호/i) as HTMLInputElement;
    const toggleButton = screen.getByRole('button', { name: /toggle password visibility/i });

    // Initially password should be hidden
    expect(passwordInput.type).toBe('password');

    // Click toggle button
    await user.click(toggleButton);
    await waitFor(() => {
      expect(passwordInput.type).toBe('text');
    });

    // Click again to hide
    await user.click(toggleButton);
    await waitFor(() => {
      expect(passwordInput.type).toBe('password');
    });
  });

  it('should submit login form with valid credentials', async () => {
    renderWithProviders(<LoginPage />);
    const user = userEvent.setup();

    // Fill form
    await user.type(screen.getByLabelText(/테넌트 ID/i), 'default');
    await user.type(screen.getByLabelText(/사용자명/i), 'admin');
    await user.type(screen.getByLabelText(/비밀번호/i), 'admin123');

    // Submit form
    const submitButton = screen.getByRole('button', { name: /로그인/i });
    await user.click(submitButton);

    // Should navigate to home page on successful login
    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith('/');
    });
  });

  it('should display error message on login failure', async () => {
    renderWithProviders(<LoginPage />);
    const user = userEvent.setup();

    // Fill form with invalid credentials
    await user.type(screen.getByLabelText(/테넌트 ID/i), 'default');
    await user.type(screen.getByLabelText(/사용자명/i), 'invalid');
    await user.type(screen.getByLabelText(/비밀번호/i), 'wrong');

    // Submit form
    const submitButton = screen.getByRole('button', { name: /로그인/i });
    await user.click(submitButton);

    // Should display error message
    await waitFor(() => {
      const errorMessage = screen.queryByRole('alert');
      expect(errorMessage).toBeInTheDocument();
    });

    // Should NOT navigate
    expect(mockNavigate).not.toHaveBeenCalled();
  });

  it('should disable form inputs while loading', async () => {
    // Set loading state
    useAuthStore.setState({ isLoading: true });

    renderWithProviders(<LoginPage />);

    const tenantInput = screen.getByLabelText(/테넌트 ID/i);
    const usernameInput = screen.getByLabelText(/사용자명/i);
    const passwordInput = screen.getByLabelText(/비밀번호/i);
    const submitButton = screen.getByRole('button', { name: /로그인/i });

    expect(tenantInput).toBeDisabled();
    expect(usernameInput).toBeDisabled();
    expect(passwordInput).toBeDisabled();
    expect(submitButton).toBeDisabled();
  });

  it('should show loading indicator when submitting', async () => {
    useAuthStore.setState({ isLoading: true });

    renderWithProviders(<LoginPage />);

    // Should show loading indicator (CircularProgress)
    const loadingIndicator = screen.getByRole('progressbar');
    expect(loadingIndicator).toBeInTheDocument();
  });

  it('should clear error when user types', async () => {
    // Set initial error
    useAuthStore.setState({ error: 'Login failed' });

    renderWithProviders(<LoginPage />);
    const user = userEvent.setup();

    // Error should be visible
    expect(screen.getByRole('alert')).toBeInTheDocument();

    // Type in username field
    await user.type(screen.getByLabelText(/사용자명/i), 'a');

    // Error should be cleared
    await waitFor(() => {
      expect(screen.queryByRole('alert')).not.toBeInTheDocument();
    });
  });

  it('should require all fields', async () => {
    renderWithProviders(<LoginPage />);
    const user = userEvent.setup();

    const submitButton = screen.getByRole('button', { name: /로그인/i });

    // Try to submit without filling fields
    await user.click(submitButton);

    // HTML5 validation should prevent submission
    // Fields should be marked as required
    const tenantInput = screen.getByLabelText(/테넌트 ID/i);
    const usernameInput = screen.getByLabelText(/사용자명/i);
    const passwordInput = screen.getByLabelText(/비밀번호/i);

    expect(tenantInput).toBeRequired();
    expect(usernameInput).toBeRequired();
    expect(passwordInput).toBeRequired();
  });
});
