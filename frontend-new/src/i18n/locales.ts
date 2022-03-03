export const SUPPORTED_LANGUAGES = [
  {
    locale: "en",
    name: "English",
    default: true,
  },
  {
    locale: "de",
  },
  {
    locale: "es",
  },
  // { TODO figure out why this breaks
  //   locale: "fr",
  // },
  {
    locale: "it",
  },
  {
    locale: "nl",
  },
  // {
  //   locale: "tr",
  // },
  {
    locale: "zh_hans",
  },
  {
    locale: "zh_hant",
  },
];

export const SUPPORTED_LOCALES = SUPPORTED_LANGUAGES.map((l) => l.locale);

export const DEFAULT_LANGUAGE = SUPPORTED_LANGUAGES.find((l) => l.default);

export const DEFAULT_LOCALE = DEFAULT_LANGUAGE?.locale as string;

export function extractLocaleFromPath(path = "") {
  const maybeLocale = path.split("/")[1];
  return SUPPORTED_LOCALES.includes(maybeLocale) ? maybeLocale : DEFAULT_LOCALE;
}
