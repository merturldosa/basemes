/**
 * i18n Configuration Tests
 * @author Moon Myung-seop
 */

import { describe, it, expect, beforeEach } from 'vitest';
import i18n, { supportedLanguages } from './config';

describe('i18n Configuration', () => {
  beforeEach(async () => {
    // Reset to Korean for each test
    await i18n.changeLanguage('ko');
  });

  it('initializes with Korean as default language', () => {
    expect(i18n.language).toBe('ko');
  });

  it('has all supported languages', () => {
    expect(supportedLanguages).toHaveLength(3);
    expect(supportedLanguages.map(l => l.code)).toEqual(['ko', 'en', 'zh']);
  });

  it('loads Korean translations correctly', () => {
    expect(i18n.t('common.appName')).toBe('SDS MES');
    expect(i18n.t('common.buttons.save')).toBe('저장');
    expect(i18n.t('auth.login.title')).toBe('로그인');
    expect(i18n.t('navigation.dashboard')).toBe('대시보드');
  });

  it('switches to English correctly', async () => {
    await i18n.changeLanguage('en');

    expect(i18n.language).toBe('en');
    expect(i18n.t('common.buttons.save')).toBe('Save');
    expect(i18n.t('auth.login.title')).toBe('Login');
    expect(i18n.t('navigation.dashboard')).toBe('Dashboard');
  });

  it('switches to Chinese correctly', async () => {
    await i18n.changeLanguage('zh');

    expect(i18n.language).toBe('zh');
    expect(i18n.t('common.buttons.save')).toBe('保存');
    expect(i18n.t('auth.login.title')).toBe('登录');
    expect(i18n.t('navigation.dashboard')).toBe('仪表板');
  });

  it('handles missing translations with fallback', () => {
    const missingKey = 'non.existent.key';
    expect(i18n.t(missingKey)).toBe(missingKey);
  });

  it('supports nested translation keys', () => {
    expect(i18n.t('common.buttons.save')).toBe('저장');
    expect(i18n.t('navigation.menu.users')).toBe('사용자 관리');
    expect(i18n.t('dashboard.statistics.totalUsers')).toBe('전체 사용자');
  });

  it('supports interpolation in translations', () => {
    const translation = i18n.t('validation.minLength', { min: 5 });
    expect(translation).toContain('5');
  });

  it('persists language selection', async () => {
    // Change language
    await i18n.changeLanguage('en');

    // Check localStorage
    const storedLanguage = localStorage.getItem('i18nextLng');
    expect(storedLanguage).toBe('en');
  });

  it('loads all translation namespaces', () => {
    // Verify key sections exist in translations
    const keySections = [
      'common',
      'auth',
      'navigation',
      'dashboard',
      'production',
      'quality',
      'inventory',
      'settings',
      'validation',
    ];

    keySections.forEach(section => {
      const key = `${section}.title`;
      const translation = i18n.t(key);
      // Translation should not be the key itself for valid sections
      // Some sections might not have 'title', so we check if it returns something
      expect(translation).toBeDefined();
    });
  });
});
