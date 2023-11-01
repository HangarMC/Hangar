<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import { useRoute } from "vue-router";
import { ref, watch } from "vue";
import type { LineChartData, LineChartOptions } from "chartist";
import { FixedScaleAxis } from "chartist";
import { useHead } from "@unhead/vue";
import { handleRequestError } from "~/composables/useErrorHandling";
import { fromISOString, toISODateString } from "~/composables/useDate";
import { useInternalApi } from "~/composables/useApi";
import Chart from "~/components/Chart.vue";
import PageTitle from "~/components/design/PageTitle.vue";
import Card from "~/components/design/Card.vue";
import InputDate from "~/components/ui/InputDate.vue";
import { useSeo } from "~/composables/useSeo";
import { definePageMeta } from "#imports";

definePageMeta({
  globalPermsRequired: ["VIEW_STATS"],
});

interface DayStat {
  x: Date;
  y: number;
}

interface DayStats {
  day: string;
  flagsClosed: number;
  flagsOpened: number;
  reviews: number;
  totalDownloads: number;
  uploads: number;
}

const i18n = useI18n();
const route = useRoute();

const now = new Date();
const oneMonthBefore = new Date(now.getFullYear(), now.getMonth() - 1, now.getDate());
const startDate = ref<string>(toISODateString(oneMonthBefore));
const endDate = ref<string>(toISODateString(now));

let data: DayStats[] = (await useInternalApi<DayStats[]>("admin/stats", "get", {
  from: startDate.value,
  to: endDate.value,
}).catch((e) => handleRequestError(e))) as DayStats[];

let reviews: DayStat[] = [];
let uploads: DayStat[] = [];
let totalDownloads: DayStat[] = [];
let openedFlags: DayStat[] = [];
let closedFlags: DayStat[] = [];
for (const statDay of data) {
  const day = fromISOString(statDay.day);
  reviews.push({ x: day, y: statDay.reviews });
  uploads.push({ x: day, y: statDay.uploads });
  totalDownloads.push({ x: day, y: statDay.totalDownloads });
  openedFlags.push({ x: day, y: statDay.flagsOpened });
  closedFlags.push({ x: day, y: statDay.flagsClosed });
}

const pluginData = ref<LineChartData>({
  labels: [i18n.t("stats.reviews"), i18n.t("stats.uploads")],
  series: [reviews, uploads],
});

const downloadData = ref<LineChartData>({
  labels: [i18n.t("stats.totalDownloads")],
  series: [totalDownloads],
});

const flagData = ref<LineChartData>({
  labels: [i18n.t("stats.openedFlags"), i18n.t("stats.closedFlags")],
  series: [openedFlags, closedFlags],
});

const options: LineChartOptions = {
  axisX: {
    type: FixedScaleAxis,
    divisor: 5,
    labelInterpolationFnc: (value: string | Date) => {
      return i18n.d(value, "date");
    },
  },
  // plugins: [Chartist.plugins.legend()],
};

useHead(useSeo(i18n.t("stats.title"), null, route, null));

watch(startDate, updateDate);
watch(endDate, updateDate);

async function updateDate() {
  data = (await useInternalApi<DayStats[]>("admin/stats", "get", {
    from: startDate.value,
    to: endDate.value,
  }).catch((e) => handleRequestError(e))) as DayStats[];
  if (!data) {
    return;
  }
  reviews = [];
  uploads = [];
  totalDownloads = [];
  openedFlags = [];
  closedFlags = [];
  for (const statDay of data) {
    const day = fromISOString(statDay.day);
    reviews.push({ x: day, y: statDay.reviews });
    uploads.push({ x: day, y: statDay.uploads });
    totalDownloads.push({ x: day, y: statDay.totalDownloads });
    openedFlags.push({ x: day, y: statDay.flagsOpened });
    closedFlags.push({ x: day, y: statDay.flagsClosed });
  }
  pluginData.value.series[0] = reviews;
  pluginData.value.series[1] = uploads;
  downloadData.value.series[0] = totalDownloads;
  flagData.value.series[0] = openedFlags;
  flagData.value.series[1] = closedFlags;
}
</script>

<template>
  <div>
    <PageTitle>{{ i18n.t("stats.title") }}</PageTitle>
    <InputDate v-model="startDate" />
    <InputDate v-model="endDate" />
    <Card class="mt-4">
      <template #header> {{ i18n.t("stats.plugins") }}</template>
      <client-only>
        <Chart id="stats" :data="pluginData" :options="options" bar-type="Line" />
      </client-only>
    </Card>
    <Card class="mt-4">
      <template #header>{{ i18n.t("stats.downloads") }}</template>
      <client-only>
        <Chart id="downloads" :data="downloadData" :options="options" bar-type="Line" />
      </client-only>
    </Card>
    <Card class="mt-4">
      <template #header>{{ i18n.t("stats.flags") }}</template>
      <client-only>
        <Chart id="flags" :data="flagData" :options="options" bar-type="Line" />
      </client-only>
    </Card>
  </div>
</template>
