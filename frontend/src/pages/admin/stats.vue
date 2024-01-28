<script lang="ts" setup>
import { Line } from "vue-chartjs";
import { CategoryScale, Chart, type ChartData, Colors, Legend, LinearScale, LineController, LineElement, PointElement, Tooltip } from "chart.js";

definePageMeta({
  globalPermsRequired: ["VIEW_STATS"],
});

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

const data = ref<DayStats[] | void>(
  await useInternalApi<DayStats[]>("admin/stats", "get", {
    from: startDate.value,
    to: endDate.value,
  }).catch((e) => handleRequestError(e))
);

const labels = computed(() => data.value?.map((day) => i18n.d(fromISOString(day.day), "date")));

const pluginData = ref<ChartData<"line", number[], string>>({
  labels: labels.value,
  datasets: [
    {
      label: i18n.t("stats.reviews"),
      data: data.value?.map((day) => day.reviews) || [],
      tension: 0.2,
    },
    {
      label: i18n.t("stats.uploads"),
      data: data.value?.map((day) => day.uploads) || [],
      tension: 0.2,
    },
  ],
});

const downloadData = ref<ChartData<"line", number[], string>>({
  labels: labels.value,
  datasets: [
    {
      label: i18n.t("stats.totalDownloads"),
      data: data.value?.map((day) => day.totalDownloads) || [],
      tension: 0.2,
    },
  ],
});

const flagData = ref<ChartData<"line", number[], string>>({
  labels: labels.value,
  datasets: [
    {
      label: i18n.t("stats.openedFlags"),
      data: data.value?.map((day) => day.flagsOpened) || [],
      tension: 0.2,
    },
    {
      label: i18n.t("stats.closedFlags"),
      data: data.value?.map((day) => day.flagsClosed) || [],
      tension: 0.2,
    },
  ],
});

const options = {
  responsive: true,
};

Chart.register(CategoryScale, LinearScale, Tooltip, Legend, PointElement, LineElement, LineController, Colors);

useHead(useSeo(i18n.t("stats.title"), null, route, null));

watch(startDate, updateDate);
watch(endDate, updateDate);

async function updateDate() {
  data.value = (await useInternalApi<DayStats[]>("admin/stats", "get", {
    from: startDate.value,
    to: endDate.value,
  }).catch((e) => handleRequestError(e))) as DayStats[];
}
</script>

<template>
  <div>
    <PageTitle>{{ i18n.t("stats.title") }}</PageTitle>
    <InputDate v-model="startDate" />
    <InputDate v-model="endDate" />
    <Card class="mt-4">
      <template #header> {{ i18n.t("stats.plugins") }}</template>
      <Line :data="pluginData" :options="options"></Line>
    </Card>
    <Card class="mt-4">
      <template #header>{{ i18n.t("stats.downloads") }}</template>
      <Line :data="downloadData" :options="options"></Line>
    </Card>
    <Card class="mt-4">
      <template #header>{{ i18n.t("stats.flags") }}</template>
      <Line :data="flagData" :options="options"></Line>
    </Card>
  </div>
</template>
