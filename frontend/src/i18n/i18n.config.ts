export default defineI18nConfig(() => ({
  legacy: false,
  locale: "en",
  fallbackLocale: "en",
  datetimeFormats: {
    en: {
      date: {
        day: "numeric",
        month: "long",
        year: "numeric",
      },
      time: {
        day: "numeric",
        month: "2-digit",
        year: "numeric",
        hour: "numeric",
        minute: "numeric",
      },
      datetime: {
        day: "numeric",
        month: "long",
        year: "numeric",
        hour: "numeric",
        minute: "numeric",
      },
      shortweektime: {
        weekday: "short",
        hour: "numeric",
        minute: "numeric",
      },
      clock: {
        hour: "numeric",
        minute: "numeric",
      },
    },
  },
}));
