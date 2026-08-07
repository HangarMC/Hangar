<script lang="ts" setup>
import { Line } from "vue-chartjs";
import { CategoryScale, Chart, Colors, Legend, LinearScale, LineController, LineElement, PointElement, Tooltip } from "chart.js";
import type { ChartData } from "chart.js";

definePageMeta({
  globalPermsRequired: ["ViewStats"],
});

const i18n = useI18n();
const route = useRoute("admin-stats");

const now = new Date();
const oneMonthBefore = new Date(now.getFullYear(), now.getMonth() - 1, now.getDate());
const startDate = ref<string>(toISODateString(oneMonthBefore));
const endDate = ref<string>(toISODateString(now));

const { adminStats } = useAdminStats(() => ({ from: startDate.value, to: endDate.value }));

const labels = computed(() => adminStats.value?.map((day) => i18n.d(fromISOString(day.day), "date")));

const rangeLabel = computed(() => {
  if (!startDate.value || !endDate.value) return "";
  return `${i18n.d(fromISOString(startDate.value), "date")} – ${i18n.d(fromISOString(endDate.value), "date")}`;
});

const tiles = computed(() => {
  const days = adminStats.value || [];
  const sum = (get: (day: (typeof days)[number]) => number) => days.reduce((total, day) => total + get(day), 0);
  return [
    { label: i18n.t("stats.uploads"), value: sum((day) => day.uploads) },
    { label: i18n.t("stats.reviews"), value: sum((day) => day.reviews) },
    { label: i18n.t("stats.totalDownloads"), value: sum((day) => day.totalDownloads) },
    { label: i18n.t("stats.openedFlags"), value: sum((day) => day.flagsOpened) },
    { label: i18n.t("stats.closedFlags"), value: sum((day) => day.flagsClosed) },
  ];
});

const pluginData = computed<ChartData<"line", number[], string>>(() => ({
  labels: labels.value,
  datasets: [
    {
      label: i18n.t("stats.reviews"),
      data: adminStats.value?.map((day) => day.reviews) || [],
      tension: 0.2,
    },
    {
      label: i18n.t("stats.uploads"),
      data: adminStats.value?.map((day) => day.uploads) || [],
      tension: 0.2,
    },
  ],
}));

const downloadData = computed<ChartData<"line", number[], string>>(() => ({
  labels: labels.value,
  datasets: [
    {
      label: i18n.t("stats.totalDownloads"),
      data: adminStats.value?.map((day) => day.totalDownloads) || [],
      tension: 0.2,
    },
  ],
}));

const flagData = computed<ChartData<"line", number[], string>>(() => ({
  labels: labels.value,
  datasets: [
    {
      label: i18n.t("stats.openedFlags"),
      data: adminStats.value?.map((day) => day.flagsOpened) || [],
      tension: 0.2,
    },
    {
      label: i18n.t("stats.closedFlags"),
      data: adminStats.value?.map((day) => day.flagsClosed) || [],
      tension: 0.2,
    },
  ],
}));

const options = {
  responsive: true,
};

Chart.register(CategoryScale, LinearScale, Tooltip, Legend, PointElement, LineElement, LineController, Colors);

useSeo(computed(() => ({ title: i18n.t("stats.title"), route })));
</script>

<template>
  <div>
    <div class="mb-5 flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
      <div>
        <h1 class="text-3xl font-bold">{{ i18n.t("stats.title") }}</h1>
        <p v-if="rangeLabel" class="mt-1 text-gray-secondary">{{ rangeLabel }}</p>
      </div>
      <div class="flex flex-wrap items-end gap-2">
        <InputDate v-model="startDate" label="From" />
        <InputDate v-model="endDate" label="To" />
      </div>
    </div>

    <div class="grid mb-4 gap-3 grid-cols-2 lg:grid-cols-5">
      <Card v-for="tile in tiles" :key="tile.label" flat padding="sm">
        <div class="truncate text-xs font-semibold text-gray-secondary uppercase tracking-wide" :title="tile.label">{{ tile.label }}</div>
        <div class="mt-1 text-2xl font-bold tabular-nums">{{ tile.value.toLocaleString("en-US") }}</div>
      </Card>
    </div>

    <div class="flex flex-col gap-4">
      <Card flat padding="none">
        <div class="flex items-center gap-2 border-b border-gray-300 px-4 py-3 dark:border-gray-700">
          <h2 class="flex-grow text-lg font-bold">{{ i18n.t("stats.plugins") }}</h2>
        </div>
        <div class="p-4">
          <Line :data="pluginData" :options="options" />
        </div>
      </Card>
      <Card flat padding="none">
        <div class="flex items-center gap-2 border-b border-gray-300 px-4 py-3 dark:border-gray-700">
          <h2 class="flex-grow text-lg font-bold">{{ i18n.t("stats.downloads") }}</h2>
        </div>
        <div class="p-4">
          <Line :data="downloadData" :options="options" />
        </div>
      </Card>
      <Card flat padding="none">
        <div class="flex items-center gap-2 border-b border-gray-300 px-4 py-3 dark:border-gray-700">
          <h2 class="flex-grow text-lg font-bold">{{ i18n.t("stats.flags") }}</h2>
        </div>
        <div class="p-4">
          <Line :data="flagData" :options="options" />
        </div>
      </Card>
    </div>
  </div>
</template>
