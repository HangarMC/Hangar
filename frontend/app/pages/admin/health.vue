<script lang="ts" setup>
import { useHealthReport } from "~/composables/useData";

definePageMeta({
  globalPermsRequired: ["ViewHealth"],
});

const i18n = useI18n();
const route = useRoute("admin-health");
const { healthReport, healthReportRefresh } = useHealthReport();

const queueing = ref(false);

const report = computed(() => healthReport.value?.finished);
const missingFiles = computed(() => report.value?.missingFiles || []);
const staleProjects = computed(() => report.value?.staleProjects || []);
const nonPublicProjects = computed(() => report.value?.nonPublicProjects || []);
const erroredJobs = computed(() => report.value?.erroredJobs || []);
const fileSizes = computed(() => report.value?.fileSizes || []);
const totalFileSize = computed(() => fileSizes.value.reduce((total, project) => total + project.totalSize, 0));

async function retryJob(jobId: number) {
  try {
    await useInternalApi("health/retry/" + jobId, "POST");
    useNotificationStore().success(i18n.t("health.jobRetryScheduled"));
  } catch (err) {
    handleRequestError(err);
  }
}

async function queue() {
  queueing.value = true;
  try {
    await useInternalApi("health/queue", "POST");
    useNotificationStore().success(i18n.t("health.reportQueued"));
    await healthReportRefresh();
    while (!healthReport.value?.finished) {
      await new Promise((resolve) => setTimeout(resolve, 1000));
      await healthReportRefresh();
    }
  } catch (err) {
    handleRequestError(err);
  } finally {
    queueing.value = false;
  }
}

useSeo(computed(() => ({ title: i18n.t("health.title"), route })));
</script>

<template>
  <div>
    <div class="mb-5 flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
      <div>
        <h1 class="text-3xl font-bold">{{ i18n.t("health.title") }}</h1>
        <p v-if="report" class="mt-1 text-gray-secondary">Generated <PrettyTime long short-relative :time="report.generatedAt" /></p>
      </div>
      <div v-if="report" class="flex flex-wrap gap-2">
        <Button variant="outline" tone="neutral" :loading="queueing" @click="queue">
          <IconMdiRefresh />
          Queue new report
        </Button>
      </div>
    </div>

    <div v-if="report" class="grid gap-4 lg:grid-cols-2">
      <Card flat padding="none">
        <div class="flex items-center gap-2 border-b border-gray-300 px-4 py-3 dark:border-gray-700">
          <h2 class="flex-grow text-lg font-bold">{{ i18n.t("health.missingFileProjects") }}</h2>
          <Chip :tone="missingFiles.length > 0 ? 'amber' : 'green'" class="tabular-nums">{{ missingFiles.length }}</Chip>
        </div>
        <ul v-if="missingFiles.length > 0" class="max-h-xs overflow-auto divide-y divide-gray-300 dark:divide-gray-700">
          <li
            v-for="project in missingFiles"
            :key="project.namespace.slug + project.namespace.owner + project.versionString"
            class="flex items-center gap-3 px-4 py-3"
          >
            <div class="h-9 w-9 flex flex-shrink-0 items-center justify-center rounded-lg bg-amber-500/15 text-lg text-amber-500">
              <IconMdiFileRemoveOutline />
            </div>
            <div class="min-w-0 flex-1">
              <Link :to="'/' + project.namespace.owner + '/' + project.namespace.slug + '/versions/' + project.versionString">
                {{ project.namespace.owner }}/{{ project.namespace.slug }}
              </Link>
              <div class="mt-0.5 flex flex-wrap items-center gap-x-2 text-xs text-gray-secondary">
                <span class="tabular-nums">{{ project.versionString }}</span>
                <span>&middot; {{ project.platforms.join(", ") }}</span>
              </div>
            </div>
          </li>
        </ul>
        <div v-else class="flex flex-col items-center px-4 py-10 text-center">
          <div class="mb-3 h-12 w-12 flex items-center justify-center rounded-full bg-lime-500/15 text-xl text-lime-500">
            <IconMdiCheckCircleOutline />
          </div>
          <p class="text-gray-secondary">{{ i18n.t("health.empty") }}</p>
        </div>
      </Card>

      <Card flat padding="none">
        <div class="flex items-center gap-2 border-b border-gray-300 px-4 py-3 dark:border-gray-700">
          <h2 class="flex-grow text-lg font-bold">{{ i18n.t("health.staleProjects") }}</h2>
          <Chip :tone="staleProjects.length > 0 ? 'amber' : 'green'" class="tabular-nums">{{ staleProjects.length }}</Chip>
        </div>
        <ul v-if="staleProjects.length > 0" class="max-h-xs overflow-auto divide-y divide-gray-300 dark:divide-gray-700">
          <li v-for="project in staleProjects" :key="project.namespace.slug + project.namespace.owner" class="flex items-center gap-3 px-4 py-3">
            <div class="h-9 w-9 flex flex-shrink-0 items-center justify-center rounded-lg bg-amber-500/15 text-lg text-amber-500">
              <IconMdiClockAlertOutline />
            </div>
            <div class="min-w-0 flex-1">
              <Link :to="'/' + project.namespace.owner + '/' + project.namespace.slug"> {{ project.namespace.owner }}/{{ project.namespace.slug }} </Link>
              <div class="mt-0.5 text-xs text-gray-secondary">{{ lastUpdated(new Date(project.lastUpdated)) }}</div>
            </div>
          </li>
        </ul>
        <div v-else class="flex flex-col items-center px-4 py-10 text-center">
          <div class="mb-3 h-12 w-12 flex items-center justify-center rounded-full bg-lime-500/15 text-xl text-lime-500">
            <IconMdiCheckCircleOutline />
          </div>
          <p class="text-gray-secondary">{{ i18n.t("health.empty") }}</p>
        </div>
      </Card>

      <Card flat padding="none">
        <div class="flex items-center gap-2 border-b border-gray-300 px-4 py-3 dark:border-gray-700">
          <h2 class="flex-grow text-lg font-bold">{{ i18n.t("health.notPublicProjects") }}</h2>
          <Chip :tone="nonPublicProjects.length > 0 ? 'amber' : 'green'" class="tabular-nums">{{ nonPublicProjects.length }}</Chip>
        </div>
        <ul v-if="nonPublicProjects.length > 0" class="max-h-xs overflow-auto divide-y divide-gray-300 dark:divide-gray-700">
          <li v-for="project in nonPublicProjects" :key="project.namespace.slug + project.namespace.owner" class="flex items-center gap-3 px-4 py-3">
            <div class="h-9 w-9 flex flex-shrink-0 items-center justify-center rounded-lg bg-amber-500/15 text-lg text-amber-500">
              <IconMdiEyeOffOutline />
            </div>
            <div class="min-w-0 flex-1">
              <Link :to="'/' + project.namespace.owner + '/' + project.namespace.slug"> {{ project.namespace.owner }}/{{ project.namespace.slug }} </Link>
              <div class="mt-0.5 text-xs text-gray-secondary">{{ project.visibility }}</div>
            </div>
          </li>
        </ul>
        <div v-else class="flex flex-col items-center px-4 py-10 text-center">
          <div class="mb-3 h-12 w-12 flex items-center justify-center rounded-full bg-lime-500/15 text-xl text-lime-500">
            <IconMdiCheckCircleOutline />
          </div>
          <p class="text-gray-secondary">{{ i18n.t("health.empty") }}</p>
        </div>
      </Card>

      <Card flat padding="none">
        <div class="flex items-center gap-2 border-b border-gray-300 px-4 py-3 dark:border-gray-700">
          <h2 class="flex-grow text-lg font-bold">{{ i18n.t("health.erroredJobs") }}</h2>
          <Chip :tone="erroredJobs.length > 0 ? 'red' : 'green'" class="tabular-nums">{{ erroredJobs.length }}</Chip>
        </div>
        <ul v-if="erroredJobs.length > 0" class="max-h-xs overflow-auto divide-y divide-gray-300 dark:divide-gray-700">
          <li v-for="job in erroredJobs" :key="job.jobType + new Date(job.lastUpdated).toISOString()" class="px-4 py-3">
            <details>
              <summary class="flex list-none cursor-pointer items-center gap-3">
                <div class="h-9 w-9 flex flex-shrink-0 items-center justify-center rounded-lg bg-red-500/15 text-lg text-red-500">
                  <IconMdiAlertCircleOutline />
                </div>
                <div class="min-w-0 flex-1">
                  <div class="font-semibold">{{ job.jobType }}</div>
                  <div class="mt-0.5 text-xs text-gray-secondary">{{ job.lastErrorDescriptor }} &middot; {{ i18n.d(job.lastUpdated, "time") }}</div>
                </div>
                <IconMdiChevronDown class="details-chevron flex-shrink-0 text-gray-secondary transition-transform" />
              </summary>
              <div class="mt-3 flex flex-col items-start gap-2 pl-12">
                <pre class="max-w-full overflow-auto rounded-md background-card p-2 text-xs">{{ job.lastError }}</pre>
                <Button variant="outline" tone="neutral" size="sm" @click="retryJob(job.id)">
                  <IconMdiRefresh />
                  {{ i18n.t("general.retry") }}
                </Button>
              </div>
            </details>
          </li>
        </ul>
        <div v-else class="flex flex-col items-center px-4 py-10 text-center">
          <div class="mb-3 h-12 w-12 flex items-center justify-center rounded-full bg-lime-500/15 text-xl text-lime-500">
            <IconMdiCheckCircleOutline />
          </div>
          <p class="text-gray-secondary">{{ i18n.t("health.empty") }}</p>
        </div>
      </Card>

      <Card flat padding="none" class="lg:col-span-2">
        <div class="flex items-center gap-2 border-b border-gray-300 px-4 py-3 dark:border-gray-700">
          <h2 class="flex-grow text-lg font-bold">{{ i18n.t("health.fileSizes") }}</h2>
          <span class="text-sm text-gray-secondary tabular-nums">{{ formatSize(totalFileSize) }}</span>
          <Chip tone="neutral" class="tabular-nums">{{ fileSizes.length }}</Chip>
        </div>
        <ul v-if="fileSizes.length > 0" class="max-h-xs overflow-auto divide-y divide-gray-300 dark:divide-gray-700">
          <li v-for="project in fileSizes" :key="project.namespace.owner + project.namespace.slug" class="flex items-center gap-3 px-4 py-3">
            <div class="h-9 w-9 flex flex-shrink-0 items-center justify-center rounded-lg bg-sky-500/15 text-lg text-sky-500">
              <IconMdiHarddisk />
            </div>
            <div class="min-w-0 flex-1">
              <Link :to="'/' + project.namespace.owner + '/' + project.namespace.slug"> {{ project.namespace.owner }}/{{ project.namespace.slug }} </Link>
              <div class="mt-0.5 text-xs text-gray-secondary tabular-nums">{{ project.fileCount.toLocaleString("en-US") }} files</div>
            </div>
            <span class="flex-shrink-0 text-sm font-semibold tabular-nums">{{ formatSize(project.totalSize) }}</span>
          </li>
        </ul>
        <div v-else class="flex flex-col items-center px-4 py-10 text-center">
          <div class="mb-3 h-12 w-12 flex items-center justify-center rounded-full bg-lime-500/15 text-xl text-lime-500">
            <IconMdiCheckCircleOutline />
          </div>
          <p class="text-gray-secondary">{{ i18n.t("health.empty") }}</p>
        </div>
      </Card>
    </div>

    <Card v-else-if="healthReport?.pending" flat class="flex flex-col items-center py-10 text-center">
      <div class="mb-3 h-12 w-12 flex items-center justify-center rounded-full bg-sky-500/15 text-xl text-sky-500">
        <IconMdiTimerSand />
      </div>
      <h2 class="text-lg font-bold">Report is being generated…</h2>
      <p class="mt-1 text-gray-secondary">
        Queued <PrettyTime long short-relative :time="healthReport.pending.queuedAt" /> by {{ healthReport.pending.queuedBy }}
      </p>
      <Chip tone="neutral" class="mt-3">{{ healthReport.pending.status }}</Chip>
    </Card>

    <Card v-else flat class="flex flex-col items-center py-10 text-center">
      <div class="mb-3 h-12 w-12 flex items-center justify-center rounded-full background-card text-xl text-gray-secondary">
        <IconMdiHeartPulse />
      </div>
      <h2 class="text-lg font-bold">No report available yet</h2>
      <p class="mt-1 max-w-md text-gray-secondary">Queue a report to check for missing files, stale projects, hidden projects and failed jobs.</p>
      <Button class="mt-4" :loading="queueing" @click="queue">Queue report</Button>
    </Card>
  </div>
</template>

<style scoped>
details[open] .details-chevron {
  transform: rotate(180deg);
}
</style>
