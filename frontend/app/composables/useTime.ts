import type { Composer } from "vue-i18n";

export function lastUpdated(date: Date | string, i18n?: Composer): string {
  // some dum linter hates me
  const lang = (i18n ?? useI18n())!;
  date = new Date(date);
  const today: Date = new Date();
  const todayTime = today.getTime();
  const dateTime = date.getTime();
  const todayDays = Math.floor(todayTime / (1000 * 60 * 60 * 24));
  const dateDays = Math.floor(dateTime / (1000 * 60 * 60 * 24));
  if (todayDays === dateDays) {
    return lang.t("general.today") + " " + lang.d(date, "clock");
  }
  if (todayDays === dateDays + 1) {
    return lang.t("general.yesterday") + " " + lang.d(date, "clock");
  }
  return todayDays - dateDays < 7 ? lang.d(date, "shortweektime") : lang.d(date, "date");
}
