import type { App } from "vue";
import { createI18n } from "vue-i18n";
import { ref } from "vue";
import { DATE_FORMATS } from "./date-formats";
import { DEFAULT_LOCALE, SUPPORTED_LOCALES } from "./locales";
import { langLog } from "~/composables/useLog";

export { DEFAULT_LOCALE, SUPPORTED_LOCALES, SUPPORTED_LANGUAGES, extractLocaleFromPath } from "./locales";

// This is a dynamic import so not all languages are bundled in frontend.
let messageImports = import.meta.glob("/src/i18n/locales/*.json");
// todo hack for nuxt since glob doesn't seem to work?
langLog("loaded message imports", messageImports);
if (!messageImports || Object.keys(messageImports).length === 0) {
  messageImports = { "/src/i18n/locales/en.json": async () => await import("./locales/en.json") };
  langLog("fallback to fake english import");
}

function importLocale(locale: string): Promise<{ default: any }> | undefined {
  langLog("import locale", locale);
  const [, importLoc] = Object.entries(messageImports).find(([key]) => key.includes(`/${locale}.`)) || [];
  langLog("found", importLoc);
  return importLoc && (importLoc() as Promise<{ default: any }> | undefined);
}

export const I18n = ref();

export async function installI18n(app: App, locale = "") {
  locale = SUPPORTED_LOCALES.includes(locale) ? locale : DEFAULT_LOCALE;
  try {
    const defaultMessages = await importLocale(DEFAULT_LOCALE);
    const messages = await importLocale(locale);

    langLog("create i18n");
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
    I18n.value = i18n.global;
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
