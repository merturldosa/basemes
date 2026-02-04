import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import LanguageDetector from 'i18next-browser-languagedetector';

// Import translation files
import ko from './locales/ko.json';
import en from './locales/en.json';
import zh from './locales/zh.json';

// Language resources
const resources = {
  ko: { translation: ko },
  en: { translation: en },
  zh: { translation: zh },
};

i18n
  // Detect user language
  .use(LanguageDetector)
  // Pass the i18n instance to react-i18next
  .use(initReactI18next)
  // Initialize i18next
  .init({
    resources,
    fallbackLng: 'ko', // Default language (Korean)
    debug: false, // Set to true for debugging

    // Language detection options
    detection: {
      // Order of language detection methods
      order: ['localStorage', 'navigator'],
      // Cache user language in localStorage
      caches: ['localStorage'],
      // localStorage key
      lookupLocalStorage: 'i18nextLng',
    },

    interpolation: {
      escapeValue: false, // React already escapes values
    },

    // Support for nested keys (e.g., "common.buttons.save")
    keySeparator: '.',

    // Namespace separator (not using namespaces, but good to define)
    nsSeparator: ':',

    // React specific options
    react: {
      useSuspense: false,
    },
  });

export default i18n;

// Export supported languages for language selector
export const supportedLanguages = [
  { code: 'ko', name: '한국어', nativeName: '한국어' },
  { code: 'en', name: 'English', nativeName: 'English' },
  { code: 'zh', name: '中文', nativeName: '中文' },
] as const;

export type SupportedLanguage = typeof supportedLanguages[number]['code'];
