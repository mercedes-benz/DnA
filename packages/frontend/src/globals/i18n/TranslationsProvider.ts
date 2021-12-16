import { LANGUAGES, SessionStorageKeys } from '../Enums';
import { Translations } from './Translations.i18n';

export const getCurrentLanguage = () => {
  return sessionStorage.getItem(SessionStorageKeys.LANGUAGE) || 'en';
};

export const isEN = () => {
  return getCurrentLanguage() === 'en';
};

export const getTranslatedLabel = (key: string, language?: LANGUAGES): string => {
  const lang = language || getCurrentLanguage();

  if (Translations[lang] && Translations[lang][key]) {
    return Translations[lang][key];
  }
  return `key-not-found [${key}]`;
};

export const getTranslatedLabelOrKey = (key: string, language?: LANGUAGES): string => {
  const lang = language || getCurrentLanguage();
  if (Translations[lang] && Translations[lang][key]) {
    return Translations[lang][key];
  }
  return key;
};

export const setCurrentLanguage = (lang: string) => {
  sessionStorage.setItem(SessionStorageKeys.LANGUAGE, lang);
};
