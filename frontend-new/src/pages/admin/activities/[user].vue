<script lang="ts" setup>
import { useInternalApi } from "~/composables/useApi";
import { useRoute } from "vue-router";
import { handleRequestError } from "~/composables/useErrorHandling";
import { useI18n } from "vue-i18n";
import { useContext } from "vite-ssr/vue";
import { FlagActivity, ReviewActivity } from "hangar-internal";
import PageTitle from "~/components/design/PageTitle.vue";
import Card from "~/components/design/Card.vue";
import Table from "~/components/design/Table.vue";
import Link from "~/components/design/Link.vue";
import Alert from "~/components/design/Alert.vue";
import { useHead } from "@vueuse/head";
import { useSeo } from "~/composables/useSeo";

const route = useRoute();
const i18n = useI18n();
const ctx = useContext();
const flagActivities = await useInternalApi<FlagActivity[]>(`admin/activity/${route.params.user}/flags`).catch((e) => handleRequestError(e, ctx, i18n));
const reviewActivities = await useInternalApi<ReviewActivity[]>(`admin/activity/${route.params.user}/reviews`).catch((e) => handleRequestError(e, ctx, i18n));

useHead(useSeo(i18n.t("userActivity.title", [route.params.user]) + route.params.constructor, null, route, null));

function getRouteParams(activity: ReviewActivity) {
  return {
    user: activity.namespace.owner,
    project: activity.namespace.slug,
    version: activity.versionString,
    platform: activity.platforms[0].toLowerCase(),
  };
}
</script>

<template>
  <PageTitle>{{ i18n.t("userActivity.title", [route.params.user]) }}</PageTitle>
  <div class="grid grid-cols-2 gap-4">
    <Card>
      <template #header>{{ i18n.t("userActivity.reviews") }}</template>

      <Table v-if="reviewActivities.length">
        <tbody>
          <tr v-for="(activity, idx) in reviewActivities" :key="`review-${idx}`">
            <td>{{ i18n.t("userActivity.reviewApproved") }}</td>
            <td>{{ activity.endedAt ? i18n.d(activity.endedAt, "time") : "" }}</td>
            <td>
              {{ `${activity.namespace.owner}/${activity.namespace.slug}/${activity.versionString}: ${activity.platforms[0].toLowerCase()}` }}
            </td>
            <td>
              <Link
                :to="{
                  name: 'user-project-versions-version-platform-reviews',
                  params: getRouteParams(activity),
                }"
              >
                <IconMdiListStatus class="float-left"></IconMdiListStatus>
                {{ i18n.t("version.page.reviewLogs") }}
              </Link>
            </td>
          </tr>
        </tbody>
      </Table>
      <Alert v-else>
        {{ i18n.t("health.empty") }}
      </Alert>
    </Card>
    <Card>
      <template #header>{{ i18n.t("userActivity.flags") }}</template>

      <Table v-if="flagActivities.length">
        <tbody>
          <tr v-for="(activity, idx) in flagActivities" :key="`flag-${idx}`">
            <td>{{ i18n.t("userActivity.flagResolved") }}</td>
            <td>{{ activity.resolvedAt ? i18n.d(activity.resolvedAt, "time") : "" }}</td>
            <td>
              <Link :to="`/${activity.namespace.owner}/${activity.namespace.slug}`">
                {{ `${activity.namespace.owner}/${activity.namespace.slug}` }}
              </Link>
            </td>
          </tr>
        </tbody>
      </Table>
      <Alert v-else>
        {{ i18n.t("health.empty") }}
      </Alert>
    </Card>
  </div>
</template>

<route lang="yaml">
meta:
  requireGlobalPerm: ["REVIEWER"]
</route>
