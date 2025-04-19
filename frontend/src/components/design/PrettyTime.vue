<script lang="ts" setup>
import type { DateTimeFormatOptions } from "@intlify/core-base";

const i18n = useI18n();

const props = defineProps<{
  time?: string | number | Date;
  long?: boolean;
  shortRelative?: boolean;
}>();

const date = computed(() => {
  if (!props.time) return;
  return typeof props.time === "object" ? props.time : new Date(props.time);
});

const options = computed<DateTimeFormatOptions & { prefix?: string }>(() => {
  if (props.shortRelative && date.value) {
    const today: Date = new Date();
    const todayTime = today.getTime();
    const dateTime = date.value.getTime();
    const todayDays = Math.floor(todayTime / (1000 * 60 * 60 * 24));
    const dateDays = Math.floor(dateTime / (1000 * 60 * 60 * 24));
    // eslint-disable-next-line unicorn/prefer-switch
    if (todayDays === dateDays) {
      return { prefix: i18n.t("general.today"), hour: "numeric", minute: "numeric" };
    } else if (todayDays === dateDays + 1) {
      return { prefix: i18n.t("general.yesterday"), hour: "numeric", minute: "numeric" };
    } else if (todayDays === dateDays - 1) {
      return { prefix: i18n.t("general.tomorrow"), hour: "numeric", minute: "numeric" };
    } else if (todayDays - dateDays < 7) {
      return { weekday: "short", hour: "numeric", minute: "numeric" };
    } else {
      return { day: "numeric", month: "long", year: "numeric" };
    }
  } else if (props.long) {
    return { day: "numeric", month: "long", year: "numeric", hour: "numeric", minute: "numeric" };
  } else {
    return { day: "numeric", month: "long", year: "numeric" };
  }
});

const formattedDate = computed(() => {
  const formatter = new Intl.DateTimeFormat(i18n.locale.value, options.value);
  let formatted = formatter.format(date.value);
  formatted = formatted.replace(/ at/, ","); // ???
  const prefix = options.value.prefix ? options.value.prefix + " " : "";
  return prefix + formatted;
});
const isoDate = computed(() => date.value?.toISOString());
</script>

<template>
  <time :datetime="isoDate">{{ formattedDate }}</time>
</template>
