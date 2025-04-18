export function lastUpdated(date: Date | string, i18n?: ReturnType<typeof useI18n>): string {
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
  } else if (todayDays === dateDays + 1) {
    return lang.t("general.yesterday") + " " + lang.d(date, "clock");
  } else if (todayDays - dateDays < 7) {
    return lang.d(date, "shortweektime");
  } else {
    return lang.d(date, "date");
  }
}
