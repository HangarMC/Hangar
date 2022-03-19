<script lang="ts" setup>
import { useContext } from "vite-ssr/vue";
import { useI18n } from "vue-i18n";
import { useRoute } from "vue-router";
import { useHealthReport } from "~/composables/useApiHelper";
import { handleRequestError } from "~/composables/useErrorHandling";
import { prettyDateTime } from "~/composables/useDate";

const ctx = useContext();
const i18n = useI18n();
const { params } = useRoute();
const healthReport = await useHealthReport().catch((e) => handleRequestError(e, ctx, i18n));
</script>

<template>
  <h1>{{ i18n.t("health.title") }}</h1>
  <div class="flex flex-wrap">
    <div class="basis-12/12 md:basis-6/12 p-4">
      <div class="bg-gray-200 rounded h-full p-2">
        <h2>{{ i18n.t("health.noTopicProject") }}</h2>
        <ul>
          <li v-for="project in healthReport.noTopicProjects" :key="project.namespace.slug + project.namespace.owner">
            <router-link :to="'/' + project.namespace.owner + '/' + project.namespace.slug">
              <strong>{{ project.namespace.owner + "/" + project.namespace.slug }}</strong>
            </router-link>
          </li>
          <li v-if="!healthReport.noTopicProjects || healthReport.noTopicProjects.length === 0">
            {{ i18n.t("health.empty") }}
          </li>
        </ul>
      </div>
    </div>
    <div class="basis-12/12 md:basis-6/12 p-4">
      <div class="bg-gray-200 rounded h-full p-2">
        <h2>{{ i18n.t("health.erroredJobs") }}</h2>
        <ul>
          <li v-for="job in healthReport.erroredJobs" :key="job.jobType + new Date(job.lastUpdated).toISOString()">
            {{ i18n.t("health.jobText", [job.jobType, job.lastErrorDescriptor, prettyDateTime(job.lastUpdated)]) }}
          </li>
          <li v-if="!healthReport.erroredJobs || healthReport.erroredJobs.length === 0">
            {{ i18n.t("health.empty") }}
          </li>
        </ul>
      </div>
    </div>
    <div class="basis-12/12 md:basis-6/12 p-4">
      <div class="bg-gray-200 rounded h-full p-2">
        <h2>{{ i18n.t("health.staleProjects") }}</h2>
        <ul>
          <li v-for="project in healthReport.staleProjects" :key="project.namespace.slug + project.namespace.owner">
            <router-link :to="'/' + project.namespace.owner + '/' + project.namespace.slug">
              <strong>{{ project.namespace.owner + "/" + project.namespace.slug }}</strong>
            </router-link>
          </li>
          <li v-if="!healthReport.staleProjects || healthReport.staleProjects.length === 0">
            {{ i18n.t("health.empty") }}
          </li>
        </ul>
      </div>
    </div>
    <div class="basis-12/12 md:basis-6/12 p-4">
      <div class="bg-gray-200 rounded h-full p-2">
        <h2>{{ i18n.t("health.notPublicProjects") }}</h2>
        <ul>
          <li v-for="project in healthReport.nonPublicProjects" :key="project.namespace.slug + project.namespace.owner">
            <router-link :to="'/' + project.namespace.owner + '/' + project.namespace.slug">
              <strong>{{ project.namespace.owner + "/" + project.namespace.slug }}</strong>
              <small>{{ i18n.t("visibility.name." + project.visibility) }}</small>
            </router-link>
          </li>
          <li v-if="!healthReport.nonPublicProjects || healthReport.nonPublicProjects.length === 0">
            {{ i18n.t("health.empty") }}
          </li>
        </ul>
      </div>
    </div>
    <div class="basis-12/12 md:basis-6/12 p-4">
      <div class="bg-gray-200 rounded h-full p-2">
        <h2>{{ i18n.t("health.noPlatform") }}</h2>
        <ul>
          <li>TODO: Implementation</li>
          <!--TODO idek what this is for?-->
          <!--<li v-if="!healthReport.noPlatform || healthReport.noPlatform.length === 0">{{ i18n.t('health.empty') }}</li>-->
        </ul>
      </div>
    </div>
    <div class="basis-12/12 md:basis-6/12 p-4">
      <div class="bg-gray-200 rounded h-full p-2">
        <h2>{{ i18n.t("health.missingFileProjects") }}</h2>
        <ul>
          <li v-for="project in healthReport.missingFiles" :key="project.namespace.slug + project.namespace.owner">
            <router-link :to="'/' + project.namespace.owner + '/' + project.namespace.slug">
              <strong>{{ project.namespace.owner + "/" + project.namespace.slug }}</strong>
            </router-link>
          </li>
          <li v-if="!healthReport.missingFiles || healthReport.missingFiles.length === 0">
            {{ i18n.t("health.empty") }}
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>

<route lang="yaml">
meta:
  requireGlobalPerm: ["VIEW_HEALTH"]
</route>
