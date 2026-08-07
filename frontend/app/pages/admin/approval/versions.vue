<script lang="ts" setup>
import { ReviewAction } from "#shared/types/backend";
import type { HangarReviewQueueEntry, Review } from "#shared/types/backend";

definePageMeta({
  globalPermsRequired: ["Reviewer"],
});

const i18n = useI18n();
const route = useRoute("admin-approval-versions");
const { versionApprovals } = useVersionApprovals();

const actions = {
  ongoing: [ReviewAction.START, ReviewAction.MESSAGE, ReviewAction.UNDO_APPROVAL, ReviewAction.REOPEN],
  stopped: [ReviewAction.STOP],
  approved: [ReviewAction.APPROVE, ReviewAction.PARTIALLY_APPROVE],
};

const notStarted = computed(() => versionApprovals.value?.notStarted ?? []);
const underReview = computed(() => versionApprovals.value?.underReview ?? []);
const expanded = ref<Record<string, boolean>>({});

useSeo(computed(() => ({ title: i18n.t("versionApproval.title"), route })));

// TODO There's no actual endpoint with filters
// function getRouteParams(entry: HangarReviewQueueEntry) {
//   return {
//     user: entry.namespace.owner,
//     project: entry.namespace.slug,
//     version: entry.versionString,
//     platform: entry.platforms[0].toLowerCase(),
//   };
// }

function isOngoing(review: Review) {
  return actions.ongoing.includes(review.lastAction);
}

function isStopped(review: Review) {
  return actions.stopped.includes(review.lastAction);
}

function isApproved(review: Review) {
  return actions.approved.includes(review.lastAction);
}

function getOngoingCount(entry: HangarReviewQueueEntry) {
  return getCount(entry, ...actions.ongoing);
}

function getStoppedCount(entry: HangarReviewQueueEntry) {
  return getCount(entry, ...actions.stopped);
}

function getApprovedCount(entry: HangarReviewQueueEntry) {
  return getCount(entry, ...actions.approved);
}

function getCount(entry: HangarReviewQueueEntry, ..._actions: ReviewAction[]) {
  let count = 0;
  for (const review of entry.reviews) {
    if (_actions.includes(review.lastAction)) {
      count++;
    }
  }
  return count;
}

function projectUrl(entry: HangarReviewQueueEntry) {
  return `/${entry.namespace.owner}/${entry.namespace.slug}`;
}

function versionUrl(entry: HangarReviewQueueEntry) {
  return `${projectUrl(entry)}/versions/${entry.versionString}`;
}

function reviewUrl(entry: HangarReviewQueueEntry) {
  return `${versionUrl(entry)}/reviews`;
}
</script>

<template>
  <div>
    <div class="mb-5 flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
      <div>
        <h1 class="text-3xl font-bold">{{ i18n.t("versionApproval.title") }}</h1>
        <p class="mt-1 text-gray-secondary">{{ i18n.t("versionApproval.subtitle") }}</p>
      </div>
      <div class="flex flex-wrap gap-2">
        <Button variant="outline" tone="neutral" to="/admin/approval/projects">
          <IconMdiFolderAlertOutline />
          {{ i18n.t("projectApproval.title") }}
        </Button>
      </div>
    </div>

    <div class="flex flex-col gap-4">
      <Card flat padding="none">
        <div class="flex items-center gap-2 border-b border-gray-300 px-4 py-3 dark:border-gray-700">
          <h2 class="flex-grow text-lg font-bold">{{ i18n.t("versionApproval.approvalQueue") }}</h2>
          <span class="text-sm text-gray-secondary tabular-nums">{{ notStarted.length }}</span>
        </div>

        <ul v-if="notStarted.length > 0" class="divide-y divide-gray-300 dark:divide-gray-700">
          <Pagination :items="notStarted" :items-per-page="15">
            <template #default="{ item }">
              <li class="flex flex-col gap-3 px-4 py-3 sm:flex-row sm:items-center">
                <div class="h-9 w-9 flex flex-shrink-0 items-center justify-center rounded-lg bg-amber-500/15 text-lg text-amber-500">
                  <IconMdiClockOutline />
                </div>
                <div class="min-w-0 flex-1">
                  <div class="flex flex-wrap items-center gap-2">
                    <NuxtLink :to="projectUrl(item)" class="font-semibold">{{ item.namespace.owner }}/{{ item.namespace.slug }}</NuxtLink>
                    <Tag :color="{ background: item.channelColor }" :name="item.channelName" />
                  </div>
                  <div class="mt-0.5 flex flex-wrap items-center gap-x-2 gap-y-1 text-xs text-gray-secondary">
                    <NuxtLink :to="versionUrl(item)" class="tabular-nums">{{ item.versionString }}</NuxtLink>
                    <PlatformLogo v-for="platform in item.platforms" :key="platform" :platform="platform" :size="14" class="flex-shrink-0" />
                    <span>&middot; {{ item.versionAuthor }}</span>
                    <span>&middot; {{ lastUpdated(new Date(item.versionCreatedAt)) }}</span>
                  </div>
                </div>
                <Button :to="reviewUrl(item)" size="sm" class="flex-shrink-0 self-end sm:self-center">
                  <IconMdiPlay />
                  {{ i18n.t("version.page.reviewStart") }}
                </Button>
              </li>
            </template>
            <template #pagination="{ page, pages, updatePage }">
              <li class="p-3">
                <PaginationButtons :page="page" :pages="pages" @update:page="updatePage" />
              </li>
            </template>
          </Pagination>
        </ul>
        <div v-else class="flex flex-col items-center px-4 py-10 text-center">
          <div class="mb-3 h-12 w-12 flex items-center justify-center rounded-full background-card text-xl text-gray-secondary">
            <IconMdiCheckDecagramOutline />
          </div>
          <p class="text-gray-secondary">{{ i18n.t("versionApproval.queueClear") }}</p>
        </div>
      </Card>

      <Card flat padding="none">
        <div class="flex items-center gap-2 border-b border-gray-300 px-4 py-3 dark:border-gray-700">
          <h2 class="flex-grow text-lg font-bold">{{ i18n.t("versionApproval.inReview") }}</h2>
          <span class="text-sm text-gray-secondary tabular-nums">{{ underReview.length }}</span>
        </div>

        <ul v-if="underReview.length > 0" class="divide-y divide-gray-300 dark:divide-gray-700">
          <Pagination :items="underReview" :items-per-page="15">
            <template #default="{ item }">
              <li class="px-4 py-3">
                <div class="flex flex-col gap-3 sm:flex-row sm:items-center">
                  <div class="h-9 w-9 flex flex-shrink-0 items-center justify-center rounded-lg bg-sky-500/15 text-lg text-sky-500">
                    <IconMdiEyeOutline />
                  </div>
                  <div class="min-w-0 flex-1">
                    <div class="flex flex-wrap items-center gap-2">
                      <NuxtLink :to="projectUrl(item)" class="font-semibold">{{ item.namespace.owner }}/{{ item.namespace.slug }}</NuxtLink>
                      <Tag :color="{ background: item.channelColor }" :name="item.channelName" />
                    </div>
                    <div class="mt-0.5 flex flex-wrap items-center gap-x-2 gap-y-1 text-xs text-gray-secondary">
                      <NuxtLink :to="versionUrl(item)" class="tabular-nums">{{ item.versionString }}</NuxtLink>
                      <PlatformLogo v-for="platform in item.platforms" :key="platform" :platform="platform" :size="14" class="flex-shrink-0" />
                      <span>&middot; {{ item.versionAuthor }}</span>
                      <span>&middot; {{ lastUpdated(new Date(item.versionCreatedAt)) }}</span>
                    </div>
                    <div class="mt-1.5 flex flex-wrap gap-1.5">
                      <Chip v-if="getOngoingCount(item)" tone="amber">{{ i18n.t("versionApproval.statuses.ongoing", [getOngoingCount(item)]) }}</Chip>
                      <Chip v-if="getStoppedCount(item)" tone="red">{{ i18n.t("versionApproval.statuses.stopped", [getStoppedCount(item)]) }}</Chip>
                      <Chip v-if="getApprovedCount(item)" tone="green">{{ i18n.t("versionApproval.statuses.approved", [getApprovedCount(item)]) }}</Chip>
                    </div>
                  </div>
                  <div class="flex flex-shrink-0 gap-2 self-end sm:self-center">
                    <Button :to="reviewUrl(item)" variant="outline" tone="neutral" size="sm">
                      <IconMdiFormatListChecks />
                      {{ i18n.t("reviews.title") }}
                    </Button>
                    <Button
                      variant="ghost"
                      tone="neutral"
                      size="sm"
                      icon-only
                      :title="i18n.t('versionApproval.toggleReviewers')"
                      :aria-label="i18n.t('versionApproval.toggleReviewers')"
                      :aria-expanded="expanded[item.versionId] ? 'true' : 'false'"
                      @click="expanded[item.versionId] = !expanded[item.versionId]"
                    >
                      <IconMdiChevronDown class="transition-transform" :class="expanded[item.versionId] ? 'rotate-180' : ''" />
                    </Button>
                  </div>
                </div>

                <ul v-if="expanded[item.versionId]" class="mt-3 flex flex-col gap-1 border-l-2 border-gray-300 pl-3 text-sm sm:ml-12 dark:border-gray-700">
                  <li v-for="entry in item.reviews" :key="entry.reviewerName" class="flex flex-wrap items-center gap-x-2">
                    <span
                      class="font-semibold"
                      :class="{ 'text-amber-500': isOngoing(entry), 'text-red-500': isStopped(entry), 'text-lime-500': isApproved(entry) }"
                      >{{ entry.reviewerName }}</span
                    >
                    <span class="text-xs text-gray-secondary">{{ i18n.t("versionApproval.started", [i18n.d(entry.reviewStarted, "time")]) }}</span>
                    <span v-if="entry.reviewEnded" class="text-xs text-gray-secondary">
                      &middot; {{ i18n.t("versionApproval.ended", [i18n.d(entry.reviewEnded, "time")]) }}
                    </span>
                  </li>
                </ul>
              </li>
            </template>
            <template #pagination="{ page, pages, updatePage }">
              <li class="p-3">
                <PaginationButtons :page="page" :pages="pages" @update:page="updatePage" />
              </li>
            </template>
          </Pagination>
        </ul>
        <div v-else class="flex flex-col items-center px-4 py-10 text-center">
          <div class="mb-3 h-12 w-12 flex items-center justify-center rounded-full background-card text-xl text-gray-secondary">
            <IconMdiEyeOutline />
          </div>
          <p class="text-gray-secondary">{{ i18n.t("versionApproval.noneInReview") }}</p>
        </div>
      </Card>
    </div>
  </div>
</template>
