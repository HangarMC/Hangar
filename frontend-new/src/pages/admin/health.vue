<script lang="ts" setup>
import { useContext } from "vite-ssr/vue";
import { useI18n } from "vue-i18n";
import { useRoute } from "vue-router";
import { useHealthReport } from "~/composables/useApiHelper";
import { handleRequestError } from "~/composables/useErrorHandling";
import { prettyDateTime } from "~/composables/useDate";
import Card from "~/components/design/Card.vue";
import Link from "~/components/design/Link.vue";
import PageTitle from "~/components/design/PageTitle.vue";

const ctx = useContext();
const i18n = useI18n();
const { params } = useRoute();
const healthReport = await useHealthReport().catch((e) => handleRequestError(e, ctx, i18n));
</script>

<template>
  <PageTitle>{{ i18n.t("health.title") }}</PageTitle>
  <div class="grid gap-8 grid-cols-1 md:grid-cols-2">
    <Card>
      <template #header> {{ i18n.t("health.noTopicProject") }}</template>

      <ul class="max-h-xs overflow-auto">
        <li v-for="project in healthReport.noTopicProjects" :key="project.namespace.slug + project.namespace.owner">
          <Link :to="'/' + project.namespace.owner + '/' + project.namespace.slug">
            {{ project.namespace.owner + "/" + project.namespace.slug }}
          </Link>
        </li>
        <li v-if="!healthReport.noTopicProjects || healthReport.noTopicProjects.length === 0">
          {{ i18n.t("health.empty") }}
        </li>
      </ul>
    </Card>
    <Card>
      <template #header> {{ i18n.t("health.erroredJobs") }}</template>

      <ul class="max-h-xs overflow-auto">
        <li v-for="job in healthReport.erroredJobs" :key="job.jobType + new Date(job.lastUpdated).toISOString()">
          {{ i18n.t("health.jobText", [job.jobType, job.lastErrorDescriptor, prettyDateTime(job.lastUpdated)]) }}
        </li>
        <li v-if="!healthReport.erroredJobs || healthReport.erroredJobs.length === 0">
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
            <strong>{{ project.namespace.owner + "/" + project.namespace.slug }}</strong>
            <small class="ml-1">{{ i18n.t("visibility.name." + project.visibility) }}</small>
          </Link>
        </li>
        <li v-if="!healthReport.nonPublicProjects || healthReport.nonPublicProjects.length === 0">
          {{ i18n.t("health.empty") }}
        </li>
      </ul>
    </Card>
    <Card>
      <template #header>{{ i18n.t("health.noPlatform") }}</template>

      <ul>
        <li>TODO: Implementation</li>
        <!--TODO idek what this is for?-->
        <!--<li v-if="!healthReport.noPlatform || healthReport.noPlatform.length === 0">{{ i18n.t('health.empty') }}</li>-->
      </ul>
    </Card>
    <Card>
      <template #header> {{ i18n.t("health.missingFileProjects") }}</template>

      <ul class="max-h-xs overflow-auto">
        <li v-for="project in healthReport.missingFiles" :key="project.namespace.slug + project.namespace.owner">
          <Link :to="'/' + project.namespace.owner + '/' + project.namespace.slug">
            {{ project.namespace.owner + "/" + project.namespace.slug }}
          </Link>
        </li>
        <li v-if="!healthReport.missingFiles || healthReport.missingFiles.length === 0">
          {{ i18n.t("health.empty") }}
        </li>
      </ul>
    </Card>
  </div>
</template>

<route lang="yaml">
meta:
  requireGlobalPerm: ["VIEW_HEALTH"]
</route>
