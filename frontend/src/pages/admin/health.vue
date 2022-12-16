<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import { useRoute } from "vue-router";
import { useHead } from "@vueuse/head";
import { useHealthReport } from "~/composables/useApiHelper";
import { handleRequestError } from "~/composables/useErrorHandling";
import Card from "~/lib/components/design/Card.vue";
import Link from "~/lib/components/design/Link.vue";
import PageTitle from "~/lib/components/design/PageTitle.vue";
import { useSeo } from "~/composables/useSeo";
import { definePageMeta } from "#imports";

definePageMeta({
  globalPermsRequired: ["VIEW_HEALTH"],
});

const i18n = useI18n();
const route = useRoute();
const healthReport = await useHealthReport().catch((e) => handleRequestError(e, i18n));

useHead(useSeo(i18n.t("health.title"), null, route, null));
</script>

<template>
  <div>
    <PageTitle>{{ i18n.t("health.title") }}</PageTitle>
    <div class="grid gap-8 grid-cols-1 md:grid-cols-2">
      <Card v-if="healthReport">
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
      <Card v-if="healthReport">
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
      <Card v-if="healthReport">
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
      <Card v-if="healthReport">
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
      <Card v-if="healthReport">
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
  </div>
</template>
