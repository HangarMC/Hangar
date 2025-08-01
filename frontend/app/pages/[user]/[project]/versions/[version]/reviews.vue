<script lang="ts" setup>
import { ReviewAction, ReviewState } from "#shared/types/backend";
import type { Platform, HangarProject, HangarReview, HangarReviewMessage, Version } from "#shared/types/backend";
import { useReviews } from "~/composables/useData";

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
const hideClosed = ref<boolean>(false);
const message = ref<string>("");
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
  const lastMsg = currentUserReview.value!.messages[currentUserReview.value!.messages.length - 1];
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

function getReviewStateString(review: HangarReview): string {
  if (!review.messages) return "error";

  const lastMsg = review.messages.at(-1);
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

function getReviewStateColor(review: HangarReview): string {
  if (!review.messages) return "#D50000";

  const lastMsg = review.messages.at(-1);
  if (!lastMsg) return "#D50000";

  switch (lastMsg.action) {
    case ReviewAction.START:
    case ReviewAction.MESSAGE:
    case ReviewAction.REOPEN:
    case ReviewAction.UNDO_APPROVAL:
      return "#ffc801";
    case ReviewAction.STOP:
      return "#D50000";
    case ReviewAction.APPROVE:
      return "#69F0AE";
    case ReviewAction.PARTIALLY_APPROVE:
      return "#4CAF50";
  }

  return "#D50000";
}

function getReviewMessageColor(msg: HangarReviewMessage): string {
  switch (msg.action) {
    case ReviewAction.START:
    case ReviewAction.REOPEN:
      return "#ffc801";
    case ReviewAction.MESSAGE:
      return "";
    case ReviewAction.STOP:
      return "#FF5252";
    case ReviewAction.APPROVE:
    case ReviewAction.PARTIALLY_APPROVE:
      return "#69F0AE";
    case ReviewAction.UNDO_APPROVAL:
      return "#ff9100";
  }
}

function getLastUpdateDate(review: HangarReview): string {
  if (!review.messages) return "error";

  const lastMsg = review.messages.at(-1);
  if (!lastMsg) return "error";

  return lastMsg.createdAt;
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
    /* empty */
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
  <div v-if="version" class="mt-4">
    <div class="float-right inline-flex">
      <template v-if="!isReviewStateChecked">
        <Button size="large" :to="{ name: 'user-project', params: route.params }" exact>
          <IconMdiHome />
          {{ t("reviews.projectPage") }}
        </Button>
        <Button class="ml-1" size="large" :to="route.path.replace('/reviews', '/scan')">
          <IconMdiAlertDecagram class="mr-1" />
          {{ i18n.t("version.page.scans") }}
        </Button>
        <DownloadButton v-if="project" :project="project" :version="version" class="ml-1" />
      </template>
    </div>

    <h2 class="my-3 text-2xl">
      {{ t("reviews.title") }}
      <span class="text-base">
        {{ t("reviews.headline", [version.author, version.name]) }}
        <PrettyTime :time="version.createdAt" long />
      </span>
    </h2>
    <div class="my-1 flex space-x-2">
      <div v-if="!currentUserReview" class="flex-grow-0">
        <Button :loading="loadingValues.start" @click="startReview">
          <IconMdiPlay />
          {{ t("reviews.startReview") }}
        </Button>
      </div>
      <div class="flex-grow-0">
        <Button @click="refreshReviews">
          <IconMdiRefresh />
          {{ t("general.refresh") }}
        </Button>
      </div>
      <div class="flex items-center">
        <InputCheckbox v-model="hideClosed" :label="t('reviews.hideClosed')" />
      </div>
    </div>

    <Accordeon v-if="filteredReviews" :values="filteredReviews" class="mt-4">
      <template #header="{ entry: review }">
        <div class="flex">
          <div class="flex-grow items-center inline-flex">
            {{ t("reviews.presets.reviewTitle", { name: review.userName }) }}
            <Tag :name="t(`reviews.state.${getReviewStateString(review)}`)" :color="{ background: getReviewStateColor(review) }" class="ml-2" />
            <span class="text-xs ml-2 text-gray-400">
              {{ t("reviews.state.lastUpdate") }}
              <PrettyTime :time="getLastUpdateDate(review)" />
            </span>
          </div>
        </div>
      </template>
      <template #plainHeader="{ entry: review }">
        <div v-if="isCurrentReviewOpen && currentUserReview === review" class="space-x-1">
          <TextAreaModal :title="t('reviews.stopReview')" :label="t('general.message')" :submit="stopReview">
            <template #activator="slotProps">
              <Button size="small" color="error" v-on="slotProps.on">
                <IconMdiStop />
                {{ t("reviews.stopReview") }}
              </Button>
            </template>
          </TextAreaModal>

          <Button size="small" :loading="loadingValues.approvePartial" @click="approvePartial">
            <IconMdiCheckDecagramOutline />
            {{ t("reviews.approvePartial") }}
          </Button>
          <Button size="small" :loading="loadingValues.approve" @click="approve">
            <IconMdiCheckDecagram />
            {{ t("reviews.approve") }}
          </Button>
        </div>
        <div v-else-if="currentUserReview === review" class="text-right">
          <Button v-if="currentReviewLastAction === 'STOP'" size="small" button-type="secondary" :loading="loadingValues.reopen" @click="reopenReview">
            <IconMdiRefresh />
            {{ t("reviews.reopenReview") }}
          </Button>
          <Button
            v-else-if="currentReviewLastAction === 'APPROVE' || currentReviewLastAction === 'PARTIALLY_APPROVE'"
            size="small"
            color="error"
            :loading="loadingValues.undoApproval"
            @click="undoApproval"
          >
            <IconMdiUndo />
            {{ t("reviews.undoApproval") }}
          </Button>
        </div>
      </template>
      <template #entry="{ entry: review, index }">
        <ul class="py-1">
          <li v-for="(msg, mIndex) in review.messages" :key="`review-${index}-msg-${mIndex}`">
            <div :style="'color: ' + getReviewMessageColor(msg)" :class="{ 'ml-4': msg.action === ReviewAction.MESSAGE }">
              <span>{{ t(msg.message, msg.args) }}</span>
              <span class="text-xs ml-4 text-gray-400"><PrettyTime :time="msg.createdAt" long /></span>
            </div>
          </li>
          <li v-if="isCurrentReviewOpen && currentUserReview === review">
            <div class="w-full mt-1">
              <InputTextarea v-model.trim="message" class="mt-2" :label="t('reviews.reviewMessage')" :rows="3" @keydown.enter.prevent="" />
              <Button
                color="primary"
                :loading="loadingValues.send"
                class="mt-2 block w-full"
                :disabled="message.length === 0 || v.$invalid"
                @click="sendMessage"
              >
                <span class="inline-flex items-center gap-1 text-white">
                  <IconMdiSend />
                  {{ t("general.send") }}
                </span>
              </Button>
            </div>
          </li>
        </ul>
      </template>
    </Accordeon>

    <Alert v-if="!reviews?.length" type="info" class="mt-2">
      {{ t("reviews.notUnderReview") }}
    </Alert>
  </div>
</template>
