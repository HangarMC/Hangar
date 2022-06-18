import { useI18n } from "vue-i18n";

export function lastUpdated(date: Date): string {
  const i18n = useI18n();
  date = new Date(date);
  const today: Date = new Date();
  const todayTime = today.getTime();
  const dateTime = date.getTime();
  const todayDays = Math.floor(todayTime / (1000 * 60 * 60 * 24));
  const dateDays = Math.floor(dateTime / (1000 * 60 * 60 * 24));
  if (todayDays === dateDays) {
    return i18n.t("general.today") + " " + i18n.d(date, "clock");
  } else if (todayDays === dateDays + 1) {
    return i18n.t("general.yesterday") + " " + i18n.d(date, "clock");
  } else if (todayDays - dateDays < 7) {
    return i18n.d(date, "shortweektime");
  } else {
    return i18n.d(date, "date");
  }
}
