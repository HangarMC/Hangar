import { createI18n } from 'vue-i18n';
import en from '@/assets/locales/en.json';

// TODO languages

export function setupI18n(locale = 'en') {
    const i18n = createI18n({
        locale,
        fallbackLocale: 'en',
        messages: {
            en,
        },
    });
    setI18nLanguage(i18n, locale);
    return i18n;
}

export function setI18nLanguage(i18n, locale) {
    i18n.global.locale = locale;
    document.querySelector('html').setAttribute('lang', locale);
}
