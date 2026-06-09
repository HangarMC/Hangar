<script lang="ts" setup>
import { useHealthReport } from "~/composables/useData";

definePageMeta({
  globalPermsRequired: ["ViewHealth"],
});

const i18n = useI18n();
const route = useRoute("admin-health");
const { healthReport, healthReportRefresh } = useHealthReport();

async function retryJob(jobId: number) {
  try {
    await useInternalApi("health/retry/" + jobId, "POST");
    useNotificationStore().success(i18n.t("health.jobRetryScheduled"));
  } catch (err) {
    handleRequestError(err);
  }
}

async function queue() {
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
  }
}

useSeo(computed(() => ({ title: i18n.t("health.title"), route })));
</script>

<template>
  <div>
    <PageTitle>{{ i18n.t("health.title") }}</PageTitle>
    <template v-if="healthReport?.finished">
      <div class="mb-4">
        Generated <PrettyTime long short-relative :time="healthReport.finished.generatedAt" />
        <Button class="ml-2" @click="queue">Queue new</Button>
      </div>
      <div class="grid gap-8 grid-cols-1 md:grid-cols-2">
        <Card>
          <template #header> {{ i18n.t("health.missingFileProjects") }} ({{ healthReport.finished.missingFiles?.length }})</template>

          <ul class="max-h-xs overflow-auto">
            <li v-for="project in healthReport.finished.missingFiles" :key="project.namespace.slug + project.namespace.owner">
              <Link :to="'/' + project.namespace.owner + '/' + project.namespace.slug + '/versions/' + project.versionString">
                {{ project.namespace.owner + "/" + project.namespace.slug + "/" + project.versionString + " (" + project.platforms + ")" }}
              </Link>
            </li>
            <li v-if="!healthReport.finished.missingFiles?.length">
              {{ i18n.t("health.empty") }}
            </li>
          </ul>
        </Card>
        <Card>
          <template #header> {{ i18n.t("health.staleProjects") }} ({{ healthReport.finished.staleProjects?.length }})</template>

          <ul class="max-h-xs overflow-auto">
            <li v-for="project in healthReport.finished.staleProjects" :key="project.namespace.slug + project.namespace.owner">
              <Link :to="'/' + project.namespace.owner + '/' + project.namespace.slug">
                {{ project.namespace.owner + "/" + project.namespace.slug }} (<PrettyTime :time="project.lastUpdated" />)
              </Link>
            </li>
            <li v-if="!healthReport.finished.staleProjects?.length">
              {{ i18n.t("health.empty") }}
            </li>
          </ul>
        </Card>
        <Card>
          <template #header> {{ i18n.t("health.notPublicProjects") }} ({{ healthReport.finished.nonPublicProjects?.length }})</template>

          <ul class="max-h-xs overflow-auto">
            <li v-for="project in healthReport.finished.nonPublicProjects" :key="project.namespace.slug + project.namespace.owner">
              <Link :to="'/' + project.namespace.owner + '/' + project.namespace.slug">
                {{ project.namespace.owner + "/" + project.namespace.slug }}
              </Link>
            </li>
            <li v-if="!healthReport.finished.nonPublicProjects?.length">
              {{ i18n.t("health.empty") }}
            </li>
          </ul>
        </Card>
        <Card>
          <template #header> {{ i18n.t("health.erroredJobs") }} ({{ healthReport.finished.erroredJobs?.length }})</template>

          <ul class="max-h-xs overflow-auto">
            <li v-for="job in healthReport.finished.erroredJobs" :key="job.jobType + new Date(job.lastUpdated).toISOString()">
              <details>
                <summary>
                  {{ i18n.t("health.jobText", [job.jobType, job.lastErrorDescriptor, i18n.d(job.lastUpdated, "time")]) }}
                </summary>
                <div class="flex flex-col">
                  <span>{{ job.lastError }}</span>
                  <Button @click="retryJob(job.id)">Retry</Button>
                </div>
              </details>
            </li>
            <li v-if="!healthReport.finished.erroredJobs?.length">
              {{ i18n.t("health.empty") }}
            </li>
          </ul>
        </Card>
        <Card>
          <template #header> {{ i18n.t("health.fileSizes") }} ({{ healthReport.finished.fileSizes?.length }})</template>

          <ul class="max-h-xs overflow-auto">
            <li v-for="project in healthReport.finished.fileSizes" :key="project.namespace.owner + project.namespace.slug" class="flex gap-4">
              <Link class="grow-1" :to="'/' + project.namespace.owner + '/' + project.namespace.slug">
                {{ project.namespace.owner + "/" + project.namespace.slug }}
              </Link>
              <span>{{ project.fileCount }} files</span>
              <span>{{ formatSize(project.totalSize) }}</span>
            </li>
            <li v-if="!healthReport.finished.fileSizes?.length">
              {{ i18n.t("health.empty") }}
            </li>
          </ul>
        </Card>
      </div>
    </template>
    <div v-else-if="healthReport?.pending">
      Report is being generated...<br />
      <PrettyTime long short-relative :time="healthReport.pending.queuedAt" /> by {{ healthReport.pending.queuedBy }}<br />
      Status: {{ healthReport.pending.status }}
    </div>
    <div v-else>
      No report available yet.
      <Button @click="queue">Queue</Button>
    </div>
  </div>
</template>
