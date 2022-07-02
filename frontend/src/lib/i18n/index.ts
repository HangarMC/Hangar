import type { App } from "vue";
import { createI18n } from "vue-i18n";
import { DATE_FORMATS } from "./date-formats";
import { DEFAULT_LOCALE, SUPPORTED_LOCALES } from "./locales";
import { ref } from "vue";

export { DEFAULT_LOCALE, SUPPORTED_LOCALES, SUPPORTED_LANGUAGES, extractLocaleFromPath } from "./locales";

// This is a dynamic import so not all languages are bundled in frontend.
const messageImports = import.meta.glob("/src/locales/*.json");

function importLocale(locale: string) {
  console.log("import", locale);
  console.log("imports", messageImports);
  const [, importLoc] = Object.entries(messageImports).find(([key]) => key.includes(`/${locale}.`)) || [];

  return importLoc && importLoc();
}

export async function loadAsyncLanguage(i18n: any, locale = DEFAULT_LOCALE) {
  try {
    const result = await importLocale(locale);
    if (result) {
      i18n.setLocaleMessage(locale, result.default || result);
      i18n.locale.value = locale;
    }
  } catch (error) {
    console.error("loadAsyncLanguage error", error);
  }
}

export const I18n = ref();

export async function installI18n(app: App, locale = "") {
  locale = SUPPORTED_LOCALES.includes(locale) ? locale : DEFAULT_LOCALE;
  try {
    const defaultMessages = await importLocale(DEFAULT_LOCALE);
    const messages = await importLocale(locale);

    const i18n = createI18n({
      legacy: false,
      locale,
      fallbackLocale: DEFAULT_LOCALE,
      messages: {
        [locale]: messages?.default || messages,
        [DEFAULT_LOCALE]: defaultMessages?.default || defaultMessages,
      },
      datetimeFormats: DATE_FORMATS,
    });

    app.use(i18n);
    I18n.value = i18n;
  } catch (error) {
    console.log("installI18n error", error);

    // fallback to no messages
    const i18n = createI18n({
      legacy: false,
      locale: "en",
      fallbackLocale: DEFAULT_LOCALE,
      datetimeFormats: DATE_FORMATS,
    });
    app.use(i18n);
    I18n.value = i18n;
  }
}
