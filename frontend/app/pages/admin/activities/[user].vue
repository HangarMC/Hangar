<script lang="ts" setup>
import type { FlagActivity, ReviewActivity } from "#shared/types/backend";

definePageMeta({
  globalPermsRequired: ["Reviewer"],
});

const route = useRoute("admin-activities-user");
const i18n = useI18n();
const flagActivities = await useInternalApi<FlagActivity[]>(`admin/activity/${route.params.user}/flags`).catch((err) => handleRequestError(err));
const reviewActivities = await useInternalApi<ReviewActivity[]>(`admin/activity/${route.params.user}/reviews`).catch((err) => handleRequestError(err));

useSeo(computed(() => ({ title: i18n.t("userActivity.title", [route.params.user]) + route.params.constructor, route })));

function getRouteParams(activity: ReviewActivity) {
  return {
    user: activity.namespace.owner,
    project: activity.namespace.slug,
    version: activity.versionString,
    platform: activity.platforms[0]?.toLowerCase(),
  };
}
</script>

<template>
  <div>
    <PageTitle>{{ i18n.t("userActivity.title", [route.params.user]) }}</PageTitle>
    <div class="grid grid-cols-2 gap-4">
      <Card>
        <template #header>{{ i18n.t("userActivity.reviews") }}</template>

        <Table v-if="reviewActivities && reviewActivities.length > 0">
          <tbody>
            <tr v-for="(activity, idx) in reviewActivities" :key="`review-${idx}`">
              <td>{{ i18n.t("userActivity.reviewApproved") }}</td>
              <td>{{ activity.endedAt ? i18n.d(activity.endedAt, "time") : "" }}</td>
              <td>
                {{ `${activity.namespace.owner}/${activity.namespace.slug}/${activity.versionString}: ${activity.platforms[0]?.toLowerCase()}` }}
              </td>
              <td>
                <Link
                  :to="{
                    name: 'user-project-versions-version-platform-reviews',
                    params: getRouteParams(activity),
                  }"
                >
                  <IconMdiListStatus class="float-left" />
                  {{ i18n.t("version.page.reviewLogs") }}
                </Link>
              </td>
            </tr>
          </tbody>
        </Table>
        <Alert v-else type="success">
          {{ i18n.t("health.empty") }}
        </Alert>
      </Card>
      <Card>
        <template #header>{{ i18n.t("userActivity.flags") }}</template>

        <Table v-if="flagActivities && flagActivities.length > 0">
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
        <Alert v-else type="success">
          {{ i18n.t("health.empty") }}
        </Alert>
      </Card>
    </div>
  </div>
</template>
