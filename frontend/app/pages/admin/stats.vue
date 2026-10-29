<script lang="ts" setup>
import { Line } from "vue-chartjs";
import { CategoryScale, Chart, Filler, Legend, LinearScale, LineController, LineElement, PointElement, Tooltip } from "chart.js";
import type { ChartData, ChartOptions } from "chart.js";
import type { Component } from "vue";
import IconMdiDownloadOutline from "~icons/mdi/download-outline";
import IconMdiEyeOutline from "~icons/mdi/eye-outline";
import IconMdiPackageVariantClosed from "~icons/mdi/package-variant-closed";
import IconMdiFolderPlusOutline from "~icons/mdi/folder-plus-outline";
import IconMdiAccountPlusOutline from "~icons/mdi/account-plus-outline";
import IconMdiClipboardCheckOutline from "~icons/mdi/clipboard-check-outline";
import IconMdiAccountGroupOutline from "~icons/mdi/account-group-outline";
import IconMdiFolderMultipleOutline from "~icons/mdi/folder-multiple-outline";
import IconMdiPackageVariant from "~icons/mdi/package-variant";
import { Platform } from "#shared/types/backend";
import type { DayStats } from "#shared/types/backend";

definePageMeta({
  globalPermsRequired: ["ViewStats"],
});

Chart.register(CategoryScale, LinearScale, Tooltip, Legend, PointElement, LineElement, LineController, Filler);

const i18n = useI18n();
const route = useRoute("admin-stats");
const settingsStore = useSettingsStore();
const globalData = useGlobalData();

const PRESETS = { week: 7, month: 30, quarter: 90, year: 365 } as const;
type Preset = keyof typeof PRESETS | "custom";

const today = new Date();
const endDate = ref(toISODateString(today));
const startDate = ref(toISODateString(shiftDays(today, -(PRESETS.month - 1))));

function shiftDays(date: Date, days: number): Date {
  const shifted = new Date(date);
  shifted.setDate(shifted.getDate() + days);
  return shifted;
}

const dayCount = computed(() => {
  const span = (fromISOString(endDate.value).getTime() - fromISOString(startDate.value).getTime()) / 86_400_000;
  return Math.max(1, Math.round(span) + 1);
});

const activePreset = computed<Preset>(() => {
  if (endDate.value !== toISODateString(new Date())) return "custom";
  const match = Object.entries(PRESETS).find(([, days]) => days === dayCount.value);
  return match ? (match[0] as Preset) : "custom";
});

const presetOptions = computed(() => {
  const options = Object.keys(PRESETS).map((key) => ({ value: key as Preset, label: i18n.t(`stats.range.${key}`) }));
  if (activePreset.value === "custom") options.push({ value: "custom", label: i18n.t("stats.range.custom") });
  return options;
});

function selectPreset(preset: Preset) {
  if (preset === "custom") return;
  const now = new Date();
  endDate.value = toISODateString(now);
  startDate.value = toISODateString(shiftDays(now, -(PRESETS[preset] - 1)));
}

const range = computed(() => ({ from: startDate.value, to: endDate.value }));
const previousRange = computed(() => {
  const previousEnd = shiftDays(fromISOString(startDate.value), -1);
  return { from: toISODateString(shiftDays(previousEnd, -(dayCount.value - 1))), to: toISODateString(previousEnd) };
});

const { adminStats } = useAdminStats(() => range.value);
const { adminStats: previousStats } = useAdminStats(() => previousRange.value);
const { adminStatsSummary } = useAdminStatsSummary(() => range.value);

const days = computed(() => adminStats.value || []);
const labels = computed(() => days.value.map((day) => i18n.d(fromISOString(day.day), "date")));
const rangeLabel = computed(() => `${i18n.d(fromISOString(startDate.value), "date")} – ${i18n.d(fromISOString(endDate.value), "date")}`);

type MetricKey = "downloads" | "views" | "uploads" | "newProjects" | "newUsers" | "reviews";

const METRICS: { key: MetricKey; label: string; icon: Component }[] = [
  { key: "downloads", label: "stats.downloads", icon: IconMdiDownloadOutline },
  { key: "views", label: "stats.views", icon: IconMdiEyeOutline },
  { key: "uploads", label: "stats.uploads", icon: IconMdiPackageVariantClosed },
  { key: "newProjects", label: "stats.newProjects", icon: IconMdiFolderPlusOutline },
  { key: "newUsers", label: "stats.newUsers", icon: IconMdiAccountPlusOutline },
  { key: "reviews", label: "stats.reviews", icon: IconMdiClipboardCheckOutline },
];

function sumOf(source: DayStats[] | null | undefined, key: keyof DayStats) {
  return (source || []).reduce((total, day) => total + (day[key] as number), 0);
}

const periodTiles = computed(() =>
  METRICS.map((metric) => {
    const value = sumOf(days.value, metric.key);
    const previous = sumOf(previousStats.value, metric.key);
    return { ...metric, value, change: previous > 0 ? ((value - previous) / previous) * 100 : undefined };
  })
);

const totalTiles = computed(() => {
  const totals = adminStatsSummary.value?.totals;
  return [
    { label: "stats.users", value: totals?.users, icon: IconMdiAccountGroupOutline },
    { label: "stats.projects", value: totals?.projects, icon: IconMdiFolderMultipleOutline },
    { label: "stats.versions", value: totals?.versions, icon: IconMdiPackageVariant },
    { label: "stats.downloads", value: totals?.downloads, icon: IconMdiDownloadOutline },
    { label: "stats.views", value: totals?.views, icon: IconMdiEyeOutline },
  ];
});

// Slots 1-3 of the validated categorical palette, stepped per mode; index is the series identity, never its rank.
const SERIES = { light: ["#2a78d6", "#eb6834", "#1baf7a"], dark: ["#3987e5", "#d95926", "#199e70"] };
const palette = computed(() => (settingsStore.darkMode ? SERIES.dark : SERIES.light));
const axisInk = computed(() => (settingsStore.darkMode ? "#9ca3af" : "#6b7280"));
const gridInk = computed(() => (settingsStore.darkMode ? "rgba(148, 163, 184, 0.16)" : "rgba(100, 116, 139, 0.16)"));

const PLATFORM_ORDER = [Platform.PAPER, Platform.WATERFALL, Platform.VELOCITY];

const platformSplit = computed(() => {
  const rows = adminStatsSummary.value?.platformDownloads || [];
  const total = rows.reduce((sum, row) => sum + row.downloads, 0);
  return rows.map((row) => ({
    platform: row.platform,
    name: globalData.value?.platforms?.find((p) => p.enumName === row.platform)?.name || row.platform,
    downloads: row.downloads,
    share: total > 0 ? (row.downloads / total) * 100 : 0,
    color: palette.value[Math.max(0, PLATFORM_ORDER.indexOf(row.platform))],
  }));
});

const totalPlatformDownloads = computed(() => platformSplit.value.reduce((total, row) => total + row.downloads, 0));
const topProjects = computed(() => adminStatsSummary.value?.topProjects || []);
const topDownloads = computed(() => topProjects.value[0]?.downloads || 0);

// a zero bar stays empty; anything non-zero keeps a 1% sliver so it never disappears
function barWidth(value: number, max: number) {
  if (value <= 0 || max <= 0) return 0;
  return Math.max((value / max) * 100, 1);
}

type SeriesKey = MetricKey | "flagsOpened" | "flagsClosed";

const SERIES_LABELS: Partial<Record<SeriesKey, string>> = { flagsOpened: "openedFlags", flagsClosed: "closedFlags" };

function series(key: SeriesKey, slot: number, fill = false) {
  const color = palette.value[slot];
  return {
    label: i18n.t(`stats.${SERIES_LABELS[key] || key}`),
    data: days.value.map((day) => day[key]),
    borderColor: color,
    backgroundColor: fill ? `${color}24` : color,
    pointBackgroundColor: color,
    fill,
  };
}

const downloadData = computed<ChartData<"line", number[], string>>(() => ({ labels: labels.value, datasets: [series("downloads", 0, true)] }));
const viewData = computed<ChartData<"line", number[], string>>(() => ({ labels: labels.value, datasets: [series("views", 1, true)] }));
const growthData = computed<ChartData<"line", number[], string>>(() => ({
  labels: labels.value,
  datasets: [series("uploads", 0), series("newProjects", 1), series("newUsers", 2)],
}));
const moderationData = computed<ChartData<"line", number[], string>>(() => ({
  labels: labels.value,
  datasets: [series("reviews", 0), series("flagsOpened", 1), series("flagsClosed", 2)],
}));

function options(legend: boolean): ChartOptions<"line"> {
  return {
    responsive: true,
    maintainAspectRatio: false,
    interaction: { mode: "index", intersect: false },
    elements: { line: { borderWidth: 2, tension: 0.35 }, point: { radius: 0, hoverRadius: 4, hoverBorderWidth: 2, hitRadius: 16 } },
    plugins: {
      legend: {
        display: legend,
        position: "bottom",
        labels: { color: axisInk.value, usePointStyle: true, pointStyle: "circle", boxWidth: 8, boxHeight: 8, padding: 18 },
      },
      tooltip: { usePointStyle: true, boxPadding: 4, padding: 10 },
    },
    scales: {
      x: { grid: { display: false }, border: { display: false }, ticks: { color: axisInk.value, maxTicksLimit: 8, autoSkip: true, maxRotation: 0 } },
      y: { beginAtZero: true, grid: { color: gridInk.value }, border: { display: false }, ticks: { color: axisInk.value, maxTicksLimit: 5, precision: 0 } },
    },
  };
}

const singleOptions = computed(() => options(false));
const multiOptions = computed(() => options(true));

function hasValues(data: ChartData<"line", number[], string>) {
  return data.datasets.some((dataset) => dataset.data.some((value) => value > 0));
}

function formatNumber(value?: number) {
  return (value ?? 0).toLocaleString("en-US");
}

function formatChange(change: number) {
  return `${change > 0 ? "+" : ""}${change.toFixed(Math.abs(change) < 10 ? 1 : 0)}%`;
}

useSeo(computed(() => ({ title: i18n.t("stats.title"), route })));
</script>

<template>
  <div class="flex flex-col gap-4">
    <div class="flex flex-col gap-3 lg:flex-row lg:items-end lg:justify-between">
      <div>
        <h1 class="text-3xl font-bold">{{ i18n.t("stats.title") }}</h1>
        <p class="mt-1 text-gray-secondary">{{ rangeLabel }} &middot; {{ i18n.t("stats.dayCount", dayCount) }}</p>
      </div>
      <div class="flex flex-wrap items-end gap-2">
        <SegmentedControl :model-value="activePreset" :options="presetOptions" :aria-label="i18n.t('stats.period')" @update:model-value="selectPreset" />
        <div class="w-40"><InputDate v-model="startDate" :label="i18n.t('stats.from')" /></div>
        <div class="w-40"><InputDate v-model="endDate" :label="i18n.t('stats.to')" /></div>
      </div>
    </div>

    <Card flat padding="none">
      <div class="flex items-center gap-2 border-b border-gray-300 px-4 py-3 dark:border-gray-700">
        <h2 class="flex-grow text-lg font-bold">{{ i18n.t("stats.needsAttention") }}</h2>
      </div>
      <div class="grid gap-px background-card sm:grid-cols-2">
        <NuxtLink
          to="/admin/approval/versions"
          class="flex items-center gap-3 px-4 py-3 background-default transition-colors hover:background-card"
          :class="{ 'opacity-70': !adminStatsSummary?.totals.pendingReviews }"
        >
          <div
            class="h-9 w-9 flex flex-shrink-0 items-center justify-center rounded-lg text-lg"
            :class="adminStatsSummary?.totals.pendingReviews ? 'bg-amber-500/15 text-amber-500' : 'bg-lime-500/15 text-lime-500'"
          >
            <IconMdiClipboardTextClockOutline />
          </div>
          <span class="min-w-0 flex-1 truncate">{{ i18n.t("stats.pendingReviews") }}</span>
          <span class="text-xl font-bold tabular-nums">{{ formatNumber(adminStatsSummary?.totals.pendingReviews) }}</span>
        </NuxtLink>
        <NuxtLink
          to="/admin/flags"
          class="flex items-center gap-3 px-4 py-3 background-default transition-colors hover:background-card"
          :class="{ 'opacity-70': !adminStatsSummary?.totals.openFlags }"
        >
          <div
            class="h-9 w-9 flex flex-shrink-0 items-center justify-center rounded-lg text-lg"
            :class="adminStatsSummary?.totals.openFlags ? 'bg-red-500/15 text-red-500' : 'bg-lime-500/15 text-lime-500'"
          >
            <IconMdiFlagOutline />
          </div>
          <span class="min-w-0 flex-1 truncate">{{ i18n.t("stats.openFlags") }}</span>
          <span class="text-xl font-bold tabular-nums">{{ formatNumber(adminStatsSummary?.totals.openFlags) }}</span>
        </NuxtLink>
      </div>
    </Card>

    <section>
      <h2 class="mb-2 text-xs font-semibold text-gray-secondary uppercase tracking-wide">
        {{ i18n.t("stats.period") }} &middot; {{ i18n.t("stats.vsPrevious", { days: i18n.t("stats.dayCount", dayCount) }) }}
      </h2>
      <div class="grid gap-3 grid-cols-2 md:grid-cols-3 xl:grid-cols-6">
        <Card v-for="tile in periodTiles" :key="tile.key" flat padding="sm">
          <div class="flex items-center gap-1.5 text-xs font-semibold text-gray-secondary uppercase tracking-wide">
            <component :is="tile.icon" class="flex-shrink-0" />
            <span class="min-w-0 truncate" :title="i18n.t(tile.label)">{{ i18n.t(tile.label) }}</span>
          </div>
          <div class="mt-1 text-2xl font-bold tabular-nums">{{ formatNumber(tile.value) }}</div>
          <div class="mt-0.5 flex items-center gap-1 text-xs tabular-nums">
            <template v-if="tile.change === undefined">
              <span class="text-gray-secondary">&mdash;</span>
            </template>
            <template v-else>
              <IconMdiArrowUp v-if="tile.change > 0" class="text-lime-600 dark:text-lime-400" />
              <IconMdiArrowDown v-else-if="tile.change < 0" class="text-red-600 dark:text-red-400" />
              <span :class="tile.change > 0 ? 'text-lime-600 dark:text-lime-400' : tile.change < 0 ? 'text-red-600 dark:text-red-400' : 'text-gray-secondary'">
                {{ formatChange(tile.change) }}
              </span>
            </template>
          </div>
        </Card>
      </div>
    </section>

    <div class="grid gap-4 xl:grid-cols-2">
      <Card flat padding="none">
        <div class="flex items-center gap-2 border-b border-gray-300 px-4 py-3 dark:border-gray-700">
          <h2 class="flex-grow text-lg font-bold">{{ i18n.t("stats.downloadsChart") }}</h2>
          <Chip tone="neutral" class="tabular-nums">{{ formatNumber(sumOf(days, "downloads")) }}</Chip>
        </div>
        <div class="h-64 p-4">
          <Line v-if="hasValues(downloadData)" :data="downloadData" :options="singleOptions" />
          <p v-else class="h-full flex items-center justify-center text-gray-secondary">{{ i18n.t("stats.noData") }}</p>
        </div>
      </Card>

      <Card flat padding="none">
        <div class="flex items-center gap-2 border-b border-gray-300 px-4 py-3 dark:border-gray-700">
          <h2 class="flex-grow text-lg font-bold">{{ i18n.t("stats.viewsChart") }}</h2>
          <Chip tone="neutral" class="tabular-nums">{{ formatNumber(sumOf(days, "views")) }}</Chip>
        </div>
        <div class="h-64 p-4">
          <Line v-if="hasValues(viewData)" :data="viewData" :options="singleOptions" />
          <p v-else class="h-full flex items-center justify-center text-gray-secondary">{{ i18n.t("stats.noData") }}</p>
        </div>
      </Card>

      <Card flat padding="none">
        <div class="flex items-center gap-2 border-b border-gray-300 px-4 py-3 dark:border-gray-700">
          <h2 class="flex-grow text-lg font-bold">{{ i18n.t("stats.growthChart") }}</h2>
        </div>
        <div class="h-64 p-4">
          <Line v-if="hasValues(growthData)" :data="growthData" :options="multiOptions" />
          <p v-else class="h-full flex items-center justify-center text-gray-secondary">{{ i18n.t("stats.noData") }}</p>
        </div>
      </Card>

      <Card flat padding="none">
        <div class="flex items-center gap-2 border-b border-gray-300 px-4 py-3 dark:border-gray-700">
          <h2 class="flex-grow text-lg font-bold">{{ i18n.t("stats.moderationChart") }}</h2>
        </div>
        <div class="h-64 p-4">
          <Line v-if="hasValues(moderationData)" :data="moderationData" :options="multiOptions" />
          <p v-else class="h-full flex items-center justify-center text-gray-secondary">{{ i18n.t("stats.noData") }}</p>
        </div>
      </Card>
    </div>

    <div class="grid items-start gap-4 xl:grid-cols-2">
      <Card flat padding="none">
        <div class="flex items-center gap-2 border-b border-gray-300 px-4 py-3 dark:border-gray-700">
          <h2 class="flex-grow text-lg font-bold">{{ i18n.t("stats.platformSplit") }}</h2>
        </div>
        <ul v-if="platformSplit.length > 0" class="flex flex-col gap-4 p-4">
          <li v-for="platform in platformSplit" :key="platform.platform">
            <div class="mb-1.5 flex items-baseline gap-2">
              <PlatformLogo :platform="platform.platform" :size="16" class="flex-shrink-0 self-center" />
              <span class="min-w-0 flex-1 truncate font-semibold">{{ platform.name }}</span>
              <span class="text-sm font-semibold tabular-nums">{{ formatNumber(platform.downloads) }}</span>
              <span class="w-12 text-right text-xs text-gray-secondary tabular-nums">{{ platform.share.toFixed(1) }}%</span>
            </div>
            <div class="h-2 overflow-hidden rounded-full background-card">
              <div
                class="h-full rounded-full"
                :style="{ width: `${barWidth(platform.downloads, totalPlatformDownloads)}%`, backgroundColor: platform.color }"
              />
            </div>
          </li>
        </ul>
        <p v-else class="px-4 py-10 text-center text-gray-secondary">{{ i18n.t("stats.noData") }}</p>
      </Card>

      <Card flat padding="none">
        <div class="flex items-center gap-2 border-b border-gray-300 px-4 py-3 dark:border-gray-700">
          <h2 class="flex-grow text-lg font-bold">{{ i18n.t("stats.topProjects") }}</h2>
        </div>
        <ol v-if="topProjects.length > 0" class="divide-y divide-gray-300 dark:divide-gray-700">
          <li v-for="(project, index) in topProjects" :key="project.namespace.owner + '/' + project.namespace.slug" class="flex items-center gap-3 px-4 py-2.5">
            <span class="w-5 flex-shrink-0 text-right text-sm font-bold text-gray-secondary tabular-nums">{{ index + 1 }}</span>
            <div class="min-w-0 flex-1">
              <Link :to="'/' + project.namespace.owner + '/' + project.namespace.slug">{{ project.namespace.owner }}/{{ project.namespace.slug }}</Link>
              <div class="mt-1 h-1.5 overflow-hidden rounded-full background-card">
                <div class="h-full rounded-full" :style="{ width: `${barWidth(project.downloads, topDownloads)}%`, backgroundColor: palette[0] }" />
              </div>
            </div>
            <div class="flex-shrink-0 text-right">
              <div class="text-sm font-semibold tabular-nums">{{ formatNumber(project.downloads) }}</div>
              <div class="inline-flex items-center gap-1 text-xs text-gray-secondary tabular-nums">
                <IconMdiEyeOutline />
                {{ formatNumber(project.views) }}
              </div>
            </div>
          </li>
        </ol>
        <p v-else class="px-4 py-10 text-center text-gray-secondary">{{ i18n.t("stats.noData") }}</p>
      </Card>
    </div>

    <section>
      <h2 class="mb-2 text-xs font-semibold text-gray-secondary uppercase tracking-wide">{{ i18n.t("stats.allTime") }}</h2>
      <div class="grid gap-3 grid-cols-2 md:grid-cols-3 xl:grid-cols-5">
        <Card v-for="tile in totalTiles" :key="tile.label" flat padding="sm" class="flex items-center gap-3">
          <div class="accent-soft h-9 w-9 flex flex-shrink-0 items-center justify-center rounded-lg text-lg color-primary">
            <component :is="tile.icon" />
          </div>
          <div class="min-w-0">
            <div class="truncate text-xs font-semibold text-gray-secondary uppercase tracking-wide">{{ i18n.t(tile.label) }}</div>
            <div class="text-xl font-bold tabular-nums">{{ formatNumber(tile.value) }}</div>
          </div>
        </Card>
      </div>
    </section>
  </div>
</template>

<style scoped>
.accent-soft {
  background-color: color-mix(in srgb, var(--primary-500) 15%, transparent);
}
</style>
