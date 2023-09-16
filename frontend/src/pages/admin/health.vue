<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import { useRoute } from "vue-router";
import { useHead } from "@unhead/vue";
import { useHealthReport } from "~/composables/useApiHelper";
import Card from "~/components/design/Card.vue";
import Link from "~/components/design/Link.vue";
import PageTitle from "~/components/design/PageTitle.vue";
import { useSeo } from "~/composables/useSeo";
import { definePageMeta } from "#imports";

definePageMeta({
  globalPermsRequired: ["VIEW_HEALTH"],
});

const i18n = useI18n();
const route = useRoute();
const healthReport = await useHealthReport();

useHead(useSeo(i18n.t("health.title"), null, route, null));
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
        <template #header> {{ i18n.t("health.erroredJobs") }}</template>

        <ul class="max-h-xs overflow-auto">
          <li v-for="job in healthReport.erroredJobs" :key="job.jobType + new Date(job.lastUpdated).toISOString()">
            {{ i18n.t("health.jobText", [job.jobType, job.lastErrorDescriptor, i18n.d(job.lastUpdated, "time")]) }}
          </li>
          <li v-if="!healthReport.erroredJobs || healthReport.erroredJobs.length === 0">
            {{ i18n.t("health.empty") }}
          </li>
        </ul>
      </Card>
    </div>
  </div>
</template>
