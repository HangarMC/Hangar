<script lang="ts" setup>
import { useHealthReport } from "~/composables/useData";

definePageMeta({
  globalPermsRequired: ["ViewHealth"],
});

const i18n = useI18n();
const route = useRoute("admin-health");
const { healthReport } = useHealthReport();

async function retryJob(jobId: string) {
  try {
    await useInternalApi("admin/health/retry/" + jobId, "POST");
    useNotificationStore().success(i18n.t("health.jobRetryScheduled"));
  } catch (err) {
    handleRequestError(err);
  }
}

useSeo(computed(() => ({ title: i18n.t("health.title"), route })));
</script>

<template>
  <div>
    <PageTitle>{{ i18n.t("health.title") }}</PageTitle>
    <div v-if="healthReport" class="grid gap-8 grid-cols-1 md:grid-cols-2">
      <Card>
        <template #header> {{ i18n.t("health.missingFileProjects") }}</template>

        <ul class="max-h-xs overflow-auto">
          <li v-for="project in healthReport.missingFiles" :key="project.namespace.slug + project.namespace.owner">
            <Link :to="'/' + project.namespace.owner + '/' + project.namespace.slug + '/versions/' + project.versionString">
              {{ project.namespace.owner + "/" + project.namespace.slug + "/" + project.versionString + " (" + project.platforms + ")" }}
            </Link>
          </li>
          <li v-if="!healthReport.missingFiles || healthReport.missingFiles.length === 0">
            {{ i18n.t("health.empty") }}
          </li>
        </ul>
      </Card>
      <Card>
        <template #header> {{ i18n.t("health.staleProjects") }}</template>

        <ul class="max-h-xs overflow-auto">
          <li v-for="project in healthReport.staleProjects" :key="project.namespace.slug + project.namespace.owner">
            <Link :to="'/' + project.namespace.owner + '/' + project.namespace.slug">
              {{ project.namespace.owner + "/" + project.namespace.slug }}
            </Link>
          </li>
          <li v-if="!healthReport.staleProjects || healthReport.staleProjects.length === 0">
            {{ i18n.t("health.empty") }}
          </li>
        </ul>
      </Card>
      <Card>
        <template #header> {{ i18n.t("health.notPublicProjects") }}</template>

        <ul class="max-h-xs overflow-auto">
          <li v-for="project in healthReport.nonPublicProjects" :key="project.namespace.slug + project.namespace.owner">
            <Link :to="'/' + project.namespace.owner + '/' + project.namespace.slug">
              {{ project.namespace.owner + "/" + project.namespace.slug }}
            </Link>
          </li>
          <li v-if="!healthReport.staleProjects || healthReport.staleProjects.length === 0">
            {{ i18n.t("health.empty") }}
          </li>
        </ul>
      </Card>
      <Card>
        <template #header> {{ i18n.t("health.erroredJobs") }}</template>

        <ul class="max-h-xs overflow-auto">
          <li v-for="job in healthReport.erroredJobs" :key="job.jobType + new Date(job.lastUpdated).toISOString()">
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
          <li v-if="!healthReport.erroredJobs || healthReport.erroredJobs.length === 0">
            {{ i18n.t("health.empty") }}
          </li>
        </ul>
      </Card>
    </div>
  </div>
</template>
