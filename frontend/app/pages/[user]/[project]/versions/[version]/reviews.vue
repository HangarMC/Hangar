<script lang="ts" setup>
import { titleCase } from "scule";
import { ReviewAction, ReviewState } from "#shared/types/backend";
import type { Platform, HangarProject, HangarReview, Version } from "#shared/types/backend";
import { useJarScans, useReviews } from "~/composables/useData";

definePageMeta({
  globalPermsRequired: ["Reviewer"],
});

const route = useRoute("user-project-versions-version-reviews");
const authStore = useAuthStore();
const i18n = useI18n();
const t = i18n.t;
const v = useVuelidate();

const props = defineProps<{
  version?: Version;
  project?: HangarProject;
  versionPlatforms: Set<Platform>;
}>();

const { reviews, refreshReviews } = useReviews(() => props.version?.id as unknown as string);
const { jarScans } = useJarScans(() => props.version?.id as unknown as string);
const hideClosed = ref<boolean>(false);
const message = ref<string>("");
const expanded = ref<Record<number, boolean>>({});
const loadingValues = reactive({
  start: false,
  send: false,
  reopen: false,
  approve: false,
  approvePartial: false,
  undoApproval: false,
});

const currentUser = computed(() => authStore.user!);

const currentUserReview = computed<HangarReview | undefined>(() => {
  if (!currentUser.value) return;
  return reviews.value?.find((r) => r.userId === currentUser.value.id);
});

const isCurrentReviewOpen = computed<boolean>(() => {
  return !currentUserReview.value?.endedAt;
});

const currentReviewLastAction = computed<ReviewAction>(() => {
  const lastMsg = currentUserReview.value!.messages.at(-1);
  return lastMsg!.action;
});

const filteredReviews = computed<HangarReview[] | undefined>(() => {
  if (hideClosed.value) {
    return reviews.value?.filter((r) => !r.endedAt);
  }
  return reviews.value;
});

const isReviewStateChecked = computed<boolean>(() => {
  return props.version?.reviewState === ReviewState.PartiallyReviewed || props.version?.reviewState === ReviewState.Reviewed;
});

const scanPath = computed<string>(() => route.path.replace("/reviews", "/scan"));

const artifacts = computed(() =>
  [...props.versionPlatforms].map((platform) => ({
    platform,
    externalUrl: props.version?.downloads?.[platform]?.externalUrl,
    fileInfo: props.version?.downloads?.[platform]?.fileInfo,
    scan: jarScans.value?.find((s) => s.platform === platform),
  }))
);

const externalCount = computed<number>(() => artifacts.value.filter((a) => a.externalUrl).length);
const unscannedCount = computed<number>(() => artifacts.value.filter((a) => !a.externalUrl && !a.scan).length);

function severityTone(severity: string): "neutral" | "amber" | "green" | "red" {
  switch (severity) {
    case "HIGHEST":
    case "HIGH":
      return "red";
    case "MEDIUM":
      return "amber";
    case "LOW":
    case "LOWEST":
      return "green";
    default:
      return "neutral";
  }
}

function externalHost(url: string): string {
  try {
    return new URL(url).hostname;
  } catch {
    return url;
  }
}

function isExpanded(review: HangarReview): boolean {
  if (review.userId in expanded.value) return expanded.value[review.userId]!;
  return currentUserReview.value === review;
}

function toggleExpanded(review: HangarReview) {
  expanded.value[review.userId] = !isExpanded(review);
}

function getReviewStateString(review: HangarReview): "ongoing" | "stopped" | "approved" | "partiallyApproved" | "error" {
  const lastMsg = review.messages?.at(-1);
  if (!lastMsg) return "error";

  switch (lastMsg.action) {
    case ReviewAction.START:
    case ReviewAction.MESSAGE:
    case ReviewAction.REOPEN:
    case ReviewAction.UNDO_APPROVAL:
      return "ongoing";
    case ReviewAction.STOP:
      return "stopped";
    case ReviewAction.APPROVE:
      return "approved";
    case ReviewAction.PARTIALLY_APPROVE:
      return "partiallyApproved";
  }

  return "error";
}

function getReviewPuckClasses(review: HangarReview): string {
  switch (getReviewStateString(review)) {
    case "ongoing":
      return "bg-amber-500/15 text-amber-500";
    case "stopped":
      return "bg-red-500/15 text-red-500";
    case "approved":
    case "partiallyApproved":
      return "bg-lime-500/15 text-lime-500";
    default:
      return "bg-red-500/15 text-red-500";
  }
}

function getReviewChipTone(review: HangarReview): "amber" | "red" | "green" {
  switch (getReviewStateString(review)) {
    case "ongoing":
      return "amber";
    case "stopped":
      return "red";
    case "approved":
    case "partiallyApproved":
      return "green";
    default:
      return "red";
  }
}

function getMessageActionClasses(action: ReviewAction): string {
  switch (action) {
    case ReviewAction.START:
    case ReviewAction.REOPEN:
      return "text-amber-500";
    case ReviewAction.STOP:
    case ReviewAction.UNDO_APPROVAL:
      return "text-red-500";
    case ReviewAction.APPROVE:
    case ReviewAction.PARTIALLY_APPROVE:
      return "text-lime-500";
    default:
      return "";
  }
}

function getLastUpdateDate(review: HangarReview): string {
  return review.messages?.at(-1)?.createdAt ?? review.createdAt;
}

function startReview() {
  const args = {
    name: currentUser.value.name,
  };
  loadingValues.start = true;
  sendReviewRequest(
    "start",
    { name: currentUser.value.name },
    ReviewAction.START,
    () => {
      reviews.value?.push({
        userName: currentUser.value.name,
        userId: currentUser.value.id,
        createdAt: new Date().toISOString(),
        endedAt: undefined,
        messages: [
          {
            message: "reviews.presets.start",
            args,
            action: ReviewAction.START,
            createdAt: new Date().toISOString(),
          },
        ],
      });
    },
    () => {
      loadingValues.start = false;
    }
  );
}

function sendMessage() {
  if (!isCurrentReviewOpen.value || message.value.length === 0) {
    return;
  }

  loadingValues.send = true;
  sendReviewRequest(
    "message",
    { msg: message.value },
    ReviewAction.MESSAGE,
    () => {
      if (document.activeElement instanceof HTMLElement) {
        document.activeElement.blur();
        v.value.$reset();
      }
      message.value = "";
    },
    () => {
      loadingValues.send = false;
    }
  );
}

function stopReview(userMsg: string) {
  if (!isCurrentReviewOpen.value || !currentUserReview.value) {
    return;
  }

  const review = currentUserReview.value;
  const args = {
    name: currentUserReview.value.userName,
    msg: userMsg,
  };
  return sendReviewRequest("stop", args, ReviewAction.STOP, () => (review.endedAt = new Date().toISOString()));
}

function reopenReview() {
  if (isCurrentReviewOpen.value || !currentUserReview.value) {
    return;
  }

  const review = currentUserReview.value;
  loadingValues.reopen = true;
  sendReviewRequest(
    "reopen",
    { name: review.userName },
    ReviewAction.REOPEN,
    () => (review.endedAt = undefined),
    () => (loadingValues.reopen = false)
  );
}

function approve() {
  if (!isCurrentReviewOpen.value || !currentUserReview.value) {
    return;
  }

  const review = currentUserReview.value;
  loadingValues.approve = true;
  sendReviewRequest(
    "approve",
    { name: review.userName },
    ReviewAction.APPROVE,
    () => (review.endedAt = new Date().toISOString()),
    () => (loadingValues.approve = false)
  );
}

function approvePartial() {
  if (!isCurrentReviewOpen.value || !currentUserReview.value) {
    return;
  }

  const review = currentUserReview.value;
  loadingValues.approvePartial = true;
  sendReviewRequest(
    "approvePartial",
    { name: review.userName },
    ReviewAction.PARTIALLY_APPROVE,
    () => (review.endedAt = new Date().toISOString()),
    () => (loadingValues.approvePartial = false)
  );
}

function undoApproval() {
  if (isCurrentReviewOpen.value) {
    return;
  }

  loadingValues.undoApproval = true;
  sendReviewRequest(
    "undoApproval",
    { name: currentUser.value.name },
    ReviewAction.UNDO_APPROVAL,
    () => reviews.value && (reviews.value.find((r) => r.userId === currentUser.value.id)!.endedAt = undefined),
    () => (loadingValues.undoApproval = false)
  );
}

function sendReviewRequest(
  urlPath: string,
  args: Record<string, string>,
  action: ReviewAction,
  then: () => void,
  final: () => void = () => {
    /*
    empty
    */
  }
): Promise<void> {
  const msg = `reviews.presets.${urlPath}`;
  return useInternalApi(`reviews/${props.version?.id}/reviews/${urlPath}`, "post", { message: msg, args })
    .then(() => {
      if (currentUserReview.value) {
        currentUserReview.value.messages.push({
          action,
          createdAt: new Date().toISOString(),
          message: msg,
          args,
        });
      }
      then();
      refreshReviews();
    })
    .catch((err) => handleRequestError(err))
    .finally(final);
}

useSeo(computed(() => ({ title: "Reviews | " + props.project?.name, route, description: props.project?.description, image: props.project?.avatarUrl })));
</script>

<template>
  <div v-if="version" class="mt-4 flex flex-col gap-4">
    <div class="flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
      <div>
        <h1 class="text-3xl font-bold">{{ t("reviews.title") }}</h1>
        <p class="mt-1 text-gray-secondary">
          {{ t("reviews.headline", [version.author, version.name]) }}
          <PrettyTime :time="version.createdAt" long />
        </p>
      </div>
      <div v-if="!isReviewStateChecked" class="flex flex-wrap gap-2">
        <Button variant="outline" tone="neutral" :to="{ name: 'user-project', params: route.params }" exact>
          <IconMdiHome />
          {{ t("reviews.projectPage") }}
        </Button>
        <DownloadButton v-if="project" :project="project" :version="version" small />
      </div>
    </div>

    <Card v-if="artifacts.length > 0" flat padding="none">
      <div class="flex flex-wrap items-center gap-2 border-b border-gray-300 px-4 py-3 dark:border-gray-700">
        <h2 class="flex-grow text-lg font-bold">{{ t("reviews.artifacts.title") }}</h2>
        <Chip v-if="externalCount > 0" tone="amber">
          <IconMdiOpenInNew />
          {{ t("reviews.artifacts.hasExternal", [externalCount], externalCount) }}
        </Chip>
        <Chip v-if="unscannedCount > 0" tone="amber">
          <IconMdiShieldOffOutline />
          {{ t("reviews.artifacts.hasUnscanned", [unscannedCount], unscannedCount) }}
        </Chip>
        <Button size="sm" variant="outline" tone="neutral" :to="scanPath">
          <IconMdiAlertDecagramOutline />
          {{ t("reviews.artifacts.scanDetails") }}
        </Button>
      </div>

      <ul class="divide-y divide-gray-300 dark:divide-gray-700">
        <li v-for="artifact in artifacts" :key="artifact.platform" class="flex flex-col gap-2 px-4 py-3 sm:flex-row sm:items-center sm:gap-3">
          <div class="flex flex-shrink-0 items-center gap-2 sm:w-32">
            <PlatformLogo :platform="artifact.platform" :size="20" class="flex-shrink-0" />
            <span class="font-semibold">{{ usePlatformName(artifact.platform) }}</span>
          </div>

          <div class="min-w-0 flex flex-1 items-center gap-2">
            <template v-if="artifact.externalUrl">
              <Chip tone="amber">
                <IconMdiOpenInNew />
                {{ t("reviews.artifacts.external") }}
              </Chip>
              <a
                :href="linkout(artifact.externalUrl)"
                target="_blank"
                rel="noopener noreferrer"
                class="min-w-0 truncate text-sm text-gray-secondary hover:underline"
                :title="artifact.externalUrl"
              >
                {{ externalHost(artifact.externalUrl) }}
              </a>
            </template>
            <template v-else-if="artifact.fileInfo">
              <IconMdiFileOutline class="flex-shrink-0 text-gray-secondary" />
              <span class="min-w-0 truncate text-sm">{{ artifact.fileInfo.name }}</span>
              <span class="flex-shrink-0 text-xs text-gray-secondary tabular-nums">{{ formatSize(artifact.fileInfo.sizeBytes) }}</span>
            </template>
          </div>

          <div class="flex flex-shrink-0 flex-wrap items-center gap-2 sm:justify-end">
            <Chip v-if="artifact.externalUrl">
              <IconMdiShieldOffOutline />
              {{ t("reviews.artifacts.notScannable") }}
            </Chip>
            <template v-else-if="artifact.scan">
              <Chip :tone="severityTone(artifact.scan.highestSeverity)">
                <IconMdiShieldAlertOutline v-if="severityTone(artifact.scan.highestSeverity) === 'red'" />
                <IconMdiShieldCheckOutline v-else />
                {{ t("reviews.artifacts.severity", [titleCase(artifact.scan.highestSeverity.toLowerCase())]) }}
              </Chip>
              <span class="text-xs text-gray-secondary"><PrettyTime :time="artifact.scan.createdAt" short-relative /></span>
            </template>
            <Chip v-else tone="amber">
              <IconMdiShieldOffOutline />
              {{ t("reviews.artifacts.notScanned") }}
            </Chip>
          </div>
        </li>
      </ul>
    </Card>

    <div class="flex flex-wrap items-center gap-2">
      <Button v-if="!currentUserReview" :loading="loadingValues.start" @click="startReview">
        <IconMdiPlay />
        {{ t("reviews.startReview") }}
      </Button>
      <Button variant="outline" tone="neutral" @click="refreshReviews">
        <IconMdiRefresh />
        {{ t("general.refresh") }}
      </Button>
      <InputCheckbox v-model="hideClosed" class="ml-auto" :label="t('reviews.hideClosed')" />
    </div>

    <Card flat padding="none">
      <div class="flex items-center gap-2 border-b border-gray-300 px-4 py-3 dark:border-gray-700">
        <h2 class="flex-grow text-lg font-bold">{{ t("reviews.reviewers") }}</h2>
        <span class="text-sm text-gray-secondary tabular-nums">{{ filteredReviews?.length ?? 0 }}</span>
      </div>

      <ul v-if="filteredReviews && filteredReviews.length > 0" class="divide-y divide-gray-300 dark:divide-gray-700">
        <li v-for="review in filteredReviews" :key="review.userId" class="px-4 py-3">
          <div class="flex flex-col gap-3 sm:flex-row sm:items-center">
            <div class="h-9 w-9 flex flex-shrink-0 items-center justify-center rounded-lg text-lg" :class="getReviewPuckClasses(review)">
              <IconMdiStop v-if="getReviewStateString(review) === 'stopped'" />
              <IconMdiCheckDecagram v-else-if="getReviewStateString(review) === 'approved' || getReviewStateString(review) === 'partiallyApproved'" />
              <IconMdiEyeOutline v-else />
            </div>
            <div class="min-w-0 flex-1">
              <div class="flex flex-wrap items-center gap-2">
                <span class="font-semibold">{{ t("reviews.presets.reviewTitle", { name: review.userName }) }}</span>
                <Chip :tone="getReviewChipTone(review)">{{ t(`reviews.state.${getReviewStateString(review)}`) }}</Chip>
              </div>
              <div class="mt-0.5 text-xs text-gray-secondary">
                {{ t("reviews.state.lastUpdate") }}
                <PrettyTime :time="getLastUpdateDate(review)" />
              </div>
            </div>
            <div class="flex flex-shrink-0 flex-wrap gap-2 self-end sm:self-center">
              <template v-if="isCurrentReviewOpen && currentUserReview === review">
                <TextAreaModal :title="t('reviews.stopReview')" :label="t('general.message')" submit-tone="danger" :submit="stopReview">
                  <template #activator="slotProps">
                    <Button size="sm" variant="outline" tone="danger" v-on="slotProps.on">
                      <IconMdiStop />
                      {{ t("reviews.stopReview") }}
                    </Button>
                  </template>
                </TextAreaModal>
                <Button size="sm" variant="outline" tone="neutral" :loading="loadingValues.approvePartial" @click="approvePartial">
                  <IconMdiCheckDecagramOutline />
                  {{ t("reviews.approvePartial") }}
                </Button>
                <Button size="sm" :loading="loadingValues.approve" @click="approve">
                  <IconMdiCheckDecagram />
                  {{ t("reviews.approve") }}
                </Button>
              </template>
              <template v-else-if="currentUserReview === review">
                <Button
                  v-if="currentReviewLastAction === 'STOP'"
                  size="sm"
                  variant="outline"
                  tone="neutral"
                  :loading="loadingValues.reopen"
                  @click="reopenReview"
                >
                  <IconMdiRefresh />
                  {{ t("reviews.reopenReview") }}
                </Button>
                <Button
                  v-else-if="currentReviewLastAction === 'APPROVE' || currentReviewLastAction === 'PARTIALLY_APPROVE'"
                  size="sm"
                  variant="outline"
                  tone="danger"
                  :loading="loadingValues.undoApproval"
                  @click="undoApproval"
                >
                  <IconMdiUndo />
                  {{ t("reviews.undoApproval") }}
                </Button>
              </template>
              <Button
                variant="ghost"
                tone="neutral"
                size="sm"
                icon-only
                :title="t('reviews.toggleLog')"
                :aria-label="t('reviews.toggleLog')"
                :aria-expanded="isExpanded(review) ? 'true' : 'false'"
                @click="toggleExpanded(review)"
              >
                <IconMdiChevronDown class="transition-transform" :class="isExpanded(review) ? 'rotate-180' : ''" />
              </Button>
            </div>
          </div>

          <div v-if="isExpanded(review)" class="mt-3 sm:ml-12">
            <Pagination :items="review.messages" :items-per-page="20">
              <template #default="{ item: msg }">
                <div class="flex flex-wrap items-start gap-x-2 gap-y-0.5 border-l-2 border-gray-300 py-1 pl-3 dark:border-gray-700">
                  <span class="mt-0.5 flex-shrink-0" :class="getMessageActionClasses(msg.action)">
                    <IconMdiPlay v-if="msg.action === ReviewAction.START" />
                    <IconMdiRefresh v-else-if="msg.action === ReviewAction.REOPEN" />
                    <IconMdiStop v-else-if="msg.action === ReviewAction.STOP" />
                    <IconMdiCheckDecagram v-else-if="msg.action === ReviewAction.APPROVE" />
                    <IconMdiCheckDecagramOutline v-else-if="msg.action === ReviewAction.PARTIALLY_APPROVE" />
                    <IconMdiUndo v-else-if="msg.action === ReviewAction.UNDO_APPROVAL" />
                    <IconMdiMessageOutline v-else />
                  </span>
                  <span class="min-w-0 flex-1 text-sm" :class="getMessageActionClasses(msg.action)">{{ t(msg.message, msg.args) }}</span>
                  <span class="flex-shrink-0 text-xs text-gray-secondary tabular-nums"><PrettyTime :time="msg.createdAt" long /></span>
                </div>
              </template>
              <template #pagination="{ page, pages, updatePage }">
                <div class="border-l-2 border-gray-300 py-2 pl-3 dark:border-gray-700">
                  <PaginationButtons :page="page" :pages="pages" @update:page="updatePage" />
                </div>
              </template>
            </Pagination>

            <div v-if="isCurrentReviewOpen && currentUserReview === review" class="mt-2 border-l-2 border-gray-300 pl-3 dark:border-gray-700">
              <InputTextarea v-model.trim="message" :label="t('reviews.reviewMessage')" :rows="3" @keydown.enter.prevent="" />
              <Button :loading="loadingValues.send" class="mt-2 w-full" :disabled="message.length === 0 || v.$invalid" @click="sendMessage">
                <IconMdiSend />
                {{ t("general.send") }}
              </Button>
            </div>
          </div>
        </li>
      </ul>
      <div v-else class="flex flex-col items-center px-4 py-10 text-center">
        <div class="mb-3 h-12 w-12 flex items-center justify-center rounded-full background-card text-xl text-gray-secondary">
          <IconMdiEyeOutline />
        </div>
        <p class="text-gray-secondary">{{ reviews?.length ? t("reviews.hiddenByFilter") : t("reviews.notUnderReview") }}</p>
      </div>
    </Card>
  </div>
</template>
