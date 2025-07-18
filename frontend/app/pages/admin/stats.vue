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
    <PageTitle>{{ i18n.t("stats.title") }}</PageTitle>
    <InputDate v-model="startDate" />
    <InputDate v-model="endDate" />
    <Card class="mt-4">
      <template #header> {{ i18n.t("stats.plugins") }}</template>
      <Line :data="pluginData" :options="options" />
    </Card>
    <Card class="mt-4">
      <template #header>{{ i18n.t("stats.downloads") }}</template>
      <Line :data="downloadData" :options="options" />
    </Card>
    <Card class="mt-4">
      <template #header>{{ i18n.t("stats.flags") }}</template>
      <Line :data="flagData" :options="options" />
    </Card>
  </div>
</template>
