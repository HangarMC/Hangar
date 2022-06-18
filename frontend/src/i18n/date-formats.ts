import { SUPPORTED_LOCALES } from "./locales";

const DEFAULT_FORMAT = {
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
};

export const DATE_FORMATS = Object.freeze({
  ...SUPPORTED_LOCALES.reduce((acc, l) => ({ ...acc, [l]: DEFAULT_FORMAT }), {}),
  // Overwrite formats here for specific locales
});
