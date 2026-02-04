/**
 * LanguageSelector Component Tests
 * @author Moon Myung-seop
 */

import { describe, it, expect, vi } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { renderWithProviders } from '@/test/test-utils';
import LanguageSelector from './LanguageSelector';

describe('LanguageSelector', () => {
  it('renders language selector button', () => {
    renderWithProviders(<LanguageSelector />);

    const languageButton = screen.getByRole('button');
    expect(languageButton).toBeInTheDocument();
  });

  it('opens language menu on button click', async () => {
    const user = userEvent.setup();
    renderWithProviders(<LanguageSelector />);

    const languageButton = screen.getByRole('button');
    await user.click(languageButton);

    // Check if menu items are visible
    await waitFor(() => {
      expect(screen.getByText('한국어')).toBeInTheDocument();
      expect(screen.getByText('English')).toBeInTheDocument();
      expect(screen.getByText('中文')).toBeInTheDocument();
    });
  });

  it('changes language when menu item is clicked', async () => {
    const user = userEvent.setup();
    renderWithProviders(<LanguageSelector />);

    // Open language menu
    const languageButton = screen.getByRole('button');
    await user.click(languageButton);

    // Click on English option
    const englishOption = await screen.findByText('English');
    await user.click(englishOption);

    // Menu should close after selection
    await waitFor(() => {
      expect(screen.queryByText('한국어')).not.toBeInTheDocument();
    });
  });

  it('shows check mark for current language', async () => {
    const user = userEvent.setup();
    renderWithProviders(<LanguageSelector />);

    // Open language menu
    const languageButton = screen.getByRole('button');
    await user.click(languageButton);

    // Korean should be selected by default (check mark should be present)
    const menuItems = await screen.findAllByRole('menuitem');
    expect(menuItems.length).toBe(3);

    // First item (Korean) should have check icon
    const koreanItem = menuItems[0];
    expect(koreanItem).toHaveAttribute('aria-selected', 'true');
  });

  it('closes menu when clicking outside', async () => {
    const user = userEvent.setup();
    renderWithProviders(<LanguageSelector />);

    // Open language menu
    const languageButton = screen.getByRole('button');
    await user.click(languageButton);

    // Verify menu is open
    expect(await screen.findByText('한국어')).toBeInTheDocument();

    // Click outside (press Escape)
    await user.keyboard('{Escape}');

    // Menu should close
    await waitFor(() => {
      expect(screen.queryByText('한국어')).not.toBeInTheDocument();
    });
  });
});
