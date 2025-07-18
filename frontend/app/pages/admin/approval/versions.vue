<script lang="ts" setup>
import type { Header } from "#shared/types/components/SortableTable";
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

const underReviewHeaders = [
  { title: i18n.t("versionApproval.project") as string, name: "project", sortable: false },
  { title: i18n.t("versionApproval.version") as string, name: "version", sortable: false },
  { title: i18n.t("versionApproval.queuedBy") as string, name: "queuedBy", sortable: true },
  { title: i18n.t("versionApproval.status") as string, name: "status", sortable: true },
] as const satisfies Header<string>[];

const notStartedHeaders = [
  { title: i18n.t("versionApproval.project") as string, name: "project", sortable: false },
  { title: i18n.t("versionApproval.date") as string, name: "date", sortable: true },
  { title: i18n.t("versionApproval.version") as string, name: "version", sortable: false },
  { title: i18n.t("versionApproval.queuedBy") as string, name: "queuedBy", sortable: true },
  { title: "", name: "startBtn", sortable: false },
] as const satisfies Header<string>[];

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
</script>

<template>
  <div>
    <Card>
      <template #header>{{ i18n.t("versionApproval.approvalQueue") }}</template>

      <SortableTable v-if="versionApprovals" :headers="notStartedHeaders" :items="versionApprovals.notStarted">
        <template #project="{ item }">
          <Link :to="`/${item.namespace.owner}/${item.namespace.slug}`">
            {{ `${item.namespace.owner}/${item.namespace.slug}` }}
          </Link>
        </template>
        <template #date="{ item }">
          <span class="start-date">{{ i18n.d(item.versionCreatedAt, "time") }}</span>
        </template>
        <template #version="{ item }">
          <Link :to="`/${item.namespace.owner}/${item.namespace.slug}/versions/${item.versionString}`">
            <Tag :color="{ background: item.channelColor }" :name="item.channelName" :data="item.versionString" />
            {{ item.versionString }}
          </Link>
        </template>
        <template #queuedBy="{ item }">
          <Link :to="`/${item.versionAuthor}`">
            {{ item.versionAuthor }}
          </Link>
        </template>
        <template #startBtn="{ item }">
          <Link :to="`/${item.namespace.owner}/${item.namespace.slug}/versions/${item.versionString}/reviews`">
            <Button>
              <IconMdiPlay />
              {{ i18n.t("version.page.reviewStart") }}
            </Button>
          </Link>
        </template>
      </SortableTable>
    </Card>

    <Card class="mt-4">
      <template #header>{{ i18n.t("versionApproval.inReview") }}</template>

      <SortableTable v-if="versionApprovals" :headers="underReviewHeaders" :items="versionApprovals.underReview" expandable>
        <template #project="{ item }">
          <Link :to="`/${item.namespace.owner}/${item.namespace.slug}`">
            {{ `${item.namespace.owner}/${item.namespace.slug}` }}
          </Link>
        </template>
        <template #version="{ item }">
          <Tag :color="{ background: item.channelColor }" :name="item.channelName" :data="item.versionString" />
        </template>
        <template #queuedBy="{ item }">
          <Link :to="`/${item.versionAuthor}`">
            {{ item.versionAuthor }}
          </Link>
          <br />
          <small>{{ i18n.d(item.versionCreatedAt, "time") }}</small>
        </template>
        <template #status="{ item }">
          <span class="text-yellow-400">
            {{ i18n.t("versionApproval.statuses.ongoing", [getOngoingCount(item)]) }}
          </span>
          <br />
          <span class="text-red-400">
            {{ i18n.t("versionApproval.statuses.stopped", [getStoppedCount(item)]) }}
          </span>
          <br />
          <span class="text-green-400"> {{ i18n.t("versionApproval.statuses.approved", [getApprovedCount(item)]) }}</span>
        </template>
        <template #expanded-item="{ item, headers }">
          <td :colspan="headers.length">
            <ul>
              <li v-for="entry in item.reviews" :key="entry.reviewerName" class="ml-4">
                <span
                  class="font-bold mr-2"
                  :class="{ 'text-yellow-400': isOngoing(entry), 'text-red-400': isStopped(entry), 'text-green-400': isApproved(entry) }"
                  >{{ entry.reviewerName }}</span
                >
                <span>{{ i18n.t("versionApproval.started", [i18n.d(entry.reviewStarted, "time")]) }}</span>
                <span v-if="entry.reviewEnded" class="ml-4" :class="{ 'text-red-400': isStopped(entry), 'text-green-400': isApproved(entry) }">{{
                  i18n.t("versionApproval.ended", [i18n.d(entry.reviewEnded, "time")])
                }}</span>
              </li>
            </ul>
          </td>
        </template>
      </SortableTable>
    </Card>
  </div>
</template>
