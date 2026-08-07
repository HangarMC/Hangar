<script lang="ts" setup>
import { NamedPermission } from "#shared/types/backend";
import type { FlagActivity, ReviewActivity } from "#shared/types/backend";

definePageMeta({
  globalPermsRequired: ["Reviewer"],
});

const route = useRoute("admin-activities-user");
const i18n = useI18n();
const flagActivities = await useInternalApi<FlagActivity[]>(`admin/activity/${route.params.user}/flags`).catch((err) => handleRequestError(err));
const reviewActivities = await useInternalApi<ReviewActivity[]>(`admin/activity/${route.params.user}/reviews`).catch((err) => handleRequestError(err));

const reviewCount = computed(() => reviewActivities?.length ?? 0);
const flagCount = computed(() => flagActivities?.length ?? 0);

useSeo(computed(() => ({ title: i18n.t("userActivity.title", [route.params.user]), route })));

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
    <div class="mb-5 flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
      <div>
        <h1 class="text-3xl font-bold">{{ i18n.t("userActivity.title", [route.params.user]) }}</h1>
        <p class="mt-1 text-gray-secondary">{{ i18n.t("userActivity.subtitle") }}</p>
      </div>
      <div class="flex flex-wrap gap-2">
        <Button variant="outline" tone="neutral" :to="'/' + route.params.user">
          <IconMdiAccount />
          {{ i18n.t("userActivity.viewProfile") }}
        </Button>
        <Button v-if="hasPerms(NamedPermission.EditAllUserSettings)" variant="outline" tone="neutral" :to="'/admin/user/' + route.params.user">
          <IconMdiAccountCog />
          {{ i18n.t("userAdmin.title") }}
        </Button>
      </div>
    </div>

    <div class="grid gap-4 lg:grid-cols-2">
      <Card flat padding="none">
        <div class="flex items-center gap-2 border-b border-gray-300 px-4 py-3 dark:border-gray-700">
          <h2 class="flex-grow text-lg font-bold">{{ i18n.t("userActivity.reviews") }}</h2>
          <span class="text-sm text-gray-secondary tabular-nums">{{ reviewCount }}</span>
        </div>

        <ul v-if="reviewCount" class="divide-y divide-gray-300 dark:divide-gray-700">
          <li v-for="(activity, idx) in reviewActivities" :key="`review-${idx}`" class="flex items-center gap-3 px-4 py-3">
            <div class="h-9 w-9 flex flex-shrink-0 items-center justify-center rounded-lg bg-lime-500/15 text-lg text-lime-500">
              <IconMdiCheckDecagram />
            </div>
            <div class="min-w-0 flex-1">
              <Link :to="{ name: 'user-project-versions-version-platform-reviews', params: getRouteParams(activity) }" class="font-semibold">
                {{ activity.namespace.owner }}/{{ activity.namespace.slug }}
              </Link>
              <div class="mt-0.5 flex flex-wrap items-center gap-x-2 text-xs text-gray-secondary">
                <span class="tabular-nums">{{ activity.versionString }}</span>
                <PlatformLogo v-if="activity.platforms[0]" :platform="activity.platforms[0]" :size="14" class="flex-shrink-0" />
                <span v-if="activity.endedAt">&middot; {{ lastUpdated(new Date(activity.endedAt)) }}</span>
              </div>
            </div>
          </li>
        </ul>
        <div v-else class="flex flex-col items-center px-4 py-10 text-center">
          <div class="mb-3 h-12 w-12 flex items-center justify-center rounded-full background-card text-xl text-gray-secondary">
            <IconMdiCheckDecagramOutline />
          </div>
          <p class="text-gray-secondary">{{ i18n.t("userActivity.noReviews") }}</p>
        </div>
      </Card>

      <Card flat padding="none">
        <div class="flex items-center gap-2 border-b border-gray-300 px-4 py-3 dark:border-gray-700">
          <h2 class="flex-grow text-lg font-bold">{{ i18n.t("userActivity.flags") }}</h2>
          <span class="text-sm text-gray-secondary tabular-nums">{{ flagCount }}</span>
        </div>

        <ul v-if="flagCount" class="divide-y divide-gray-300 dark:divide-gray-700">
          <li v-for="(activity, idx) in flagActivities" :key="`flag-${idx}`" class="flex items-center gap-3 px-4 py-3">
            <div class="h-9 w-9 flex flex-shrink-0 items-center justify-center rounded-lg bg-amber-500/15 text-lg text-amber-500">
              <IconMdiFlag />
            </div>
            <div class="min-w-0 flex-1">
              <NuxtLink :to="`/${activity.namespace.owner}/${activity.namespace.slug}`" class="font-semibold">
                {{ activity.namespace.owner }}/{{ activity.namespace.slug }}
              </NuxtLink>
              <div class="mt-0.5 text-xs text-gray-secondary">
                {{ i18n.t("userActivity.flagResolved") }}
                <template v-if="activity.resolvedAt">&middot; {{ lastUpdated(new Date(activity.resolvedAt)) }}</template>
              </div>
            </div>
          </li>
        </ul>
        <div v-else class="flex flex-col items-center px-4 py-10 text-center">
          <div class="mb-3 h-12 w-12 flex items-center justify-center rounded-full background-card text-xl text-gray-secondary">
            <IconMdiFlagOutline />
          </div>
          <p class="text-gray-secondary">{{ i18n.t("userActivity.noFlags") }}</p>
        </div>
      </Card>
    </div>
  </div>
</template>
