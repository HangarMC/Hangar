<script lang="ts" setup>
import { useContext } from "vite-ssr/vue";
import { useI18n } from "vue-i18n";
import { useRoute } from "vue-router";
import { handleRequestError } from "~/composables/useErrorHandling";
import { ref, watch } from "vue";
import { fromISOString, toISODateString } from "~/lib/composables/useDate";
import { useInternalApi } from "~/composables/useApi";
import Chart from "~/components/Chart.vue";
import Chartist, { IChartistSeriesData, ILineChartOptions } from "chartist";
import PageTitle from "~/lib/components/design/PageTitle.vue";
import Card from "~/lib/components/design/Card.vue";
import InputDate from "~/lib/components/ui/InputDate.vue";
import { useHead } from "@vueuse/head";
import { useSeo } from "~/composables/useSeo";

const ctx = useContext();
const i18n = useI18n();
const route = useRoute();

const now = new Date();
const oneMonthBefore = new Date(now.getFullYear(), now.getMonth() - 1, now.getDate());
const startDate = ref<string>(toISODateString(oneMonthBefore));
const endDate = ref<string>(toISODateString(now));

let data: DayStats[] = (await useInternalApi<DayStats[]>("admin/stats", true, "get", {
  from: startDate.value,
  to: endDate.value,
}).catch((e) => handleRequestError(e, ctx, i18n))) as DayStats[];

let reviews: DayStat[] = [];
let uploads: DayStat[] = [];
let totalDownloads: DayStat[] = [];
let unsafeDownloads: DayStat[] = [];
let openedFlags: DayStat[] = [];
let closedFlags: DayStat[] = [];
for (const statDay of data) {
  const day = fromISOString(statDay.day);
  reviews.push({ x: day, y: statDay.reviews });
  uploads.push({ x: day, y: statDay.uploads });
  totalDownloads.push({ x: day, y: statDay.totalDownloads });
  unsafeDownloads.push({ x: day, y: statDay.unsafeDownloads });
  openedFlags.push({ x: day, y: statDay.flagsOpened });
  closedFlags.push({ x: day, y: statDay.flagsClosed });
}

const pluginData = ref({
  series: [
    {
      name: i18n.t("stats.reviews") as string,
      data: reviews,
    },
    {
      name: i18n.t("stats.uploads") as string,
      data: uploads,
    },
  ],
});

const downloadData = ref({
  series: [
    {
      name: i18n.t("stats.totalDownloads") as string,
      data: totalDownloads,
    },
    {
      name: i18n.t("stats.unsafeDownloads") as string,
      data: unsafeDownloads,
    },
  ],
});

const flagData = ref({
  series: [
    {
      name: i18n.t("stats.openedFlags") as string,
      data: openedFlags,
    },
    {
      name: i18n.t("stats.closedFlags") as string,
      data: closedFlags,
    },
  ],
});

const options: ILineChartOptions = {
  axisX: {
    type: Chartist.FixedScaleAxis,
    divisor: 5,
    labelInterpolationFnc: (value: string | Date) => {
      return i18n.d(value, "date");
    },
  },
  plugins: [Chartist.plugins.legend()],
};

useHead(useSeo(i18n.t("stats.title"), null, route, null));

watch(startDate, updateDate);
watch(endDate, updateDate);

async function updateDate() {
  console.log("update", startDate, endDate);
  data = (await useInternalApi<DayStats[]>("admin/stats", true, "get", {
    from: startDate.value,
    to: endDate.value,
  }).catch((e) => handleRequestError(e, ctx, i18n))) as DayStats[];
  if (!data) {
    return;
  }
  reviews = [];
  uploads = [];
  totalDownloads = [];
  unsafeDownloads = [];
  openedFlags = [];
  closedFlags = [];
  for (const statDay of data) {
    const day = fromISOString(statDay.day);
    reviews.push({ x: day, y: statDay.reviews });
    uploads.push({ x: day, y: statDay.uploads });
    totalDownloads.push({ x: day, y: statDay.totalDownloads });
    unsafeDownloads.push({ x: day, y: statDay.unsafeDownloads });
    openedFlags.push({ x: day, y: statDay.flagsOpened });
    closedFlags.push({ x: day, y: statDay.flagsClosed });
  }
  (pluginData.value.series[0] as IChartistSeriesData).data = reviews;
  (pluginData.value.series[1] as IChartistSeriesData).data = uploads;
  (downloadData.value.series[0] as IChartistSeriesData).data = totalDownloads;
  (downloadData.value.series[1] as IChartistSeriesData).data = unsafeDownloads;
  (flagData.value.series[0] as IChartistSeriesData).data = openedFlags;
  (flagData.value.series[1] as IChartistSeriesData).data = closedFlags;
}

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
  unsafeDownloads: number;
  uploads: number;
}
</script>

<template>
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
</template>

<route lang="yaml">
meta:
  requireGlobalPerm: ["VIEW_STATS"]
</route>
