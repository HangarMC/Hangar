<script setup lang="ts">
import type { AxiosError } from "axios";
import { NamedPermission, ReviewState, Visibility } from "~/types/backend";
import type { Platform, HangarProject } from "~/types/backend";

const i18n = useI18n();
const router = useRouter();
const notification = useNotificationStore();
const props = defineProps<{
  project?: HangarProject;
}>();

const user = useAuthStore().user;

const starred = computed(() => props.project?.userActions?.starred);
const watching = computed(() => props.project?.userActions?.watching);
const starredCount = computed(() => props.project?.stats?.stars);
const watchingCount = computed(() => props.project?.stats?.watchers);
const reported = computed(() => props.project?.userActions?.flagged);

const isOwn = computed(() => !user || user.name === props.project?.namespace?.owner);

const starredChanged = ref(false);
const watchingChanged = ref(false);

function toggleState(route: string, completedKey: string, revokedKey: string, value?: boolean) {
  useInternalApi(`projects/project/${props.project?.id}/${route}/${value}`, "post")
    .then(() => {
      notification.success(i18n.t("project.actions." + (value ? completedKey : revokedKey)));
    })
    .catch((err) => handleRequestError(err, i18n.t(`project.error.${route}`)));
}

function toggleStar() {
  starredChanged.value = !starredChanged.value;
  toggleState("star", "starred", "unstarred", hasStarred());
}

function toggleWatch() {
  watchingChanged.value = !watchingChanged.value;
  toggleState("watch", "watched", "unwatched", isWatching());
}

function hasStarred() {
  return starredChanged.value ? !starred.value : starred.value;
}

function isWatching() {
  return watchingChanged.value ? !watching.value : watching.value;
}

function getStarredCount() {
  if (starredCount.value === undefined) {
    return 0;
  }
  if (starredChanged.value) {
    return starred.value ? starredCount.value - 1 : starredCount.value + 1;
  } else {
    return starredCount.value;
  }
}

function getWatchingCount() {
  if (watchingCount.value === undefined) {
    return 0;
  }
  if (watchingChanged.value) {
    return watching.value ? watchingCount.value - 1 : watchingCount.value + 1;
  } else {
    return watchingCount.value;
  }
}

async function sendForApproval() {
  try {
    await useInternalApi(`projects/visibility/${props.project?.id}/sendforapproval`, "post");
    notification.success(i18n.t("projectApproval.sendForApproval"));
    await router.go(0);
  } catch (err) {
    handleRequestError(err as AxiosError);
  }
}

enum ConfirmationType {
  REQUIRED = "version.page.unsafeWarning",
  EXTERNAL_URL = "version.page.unsafeWarningExternal",
  NO = "",
}

function requiresConfirmation(): ConfirmationType {
  for (const platform in props.project?.mainChannelVersions) {
    const version = props.project.mainChannelVersions[platform as Platform];
    if (version.reviewState !== ReviewState.Reviewed) {
      return ConfirmationType.REQUIRED;
    }

    const download = version.downloads[platform as Platform];
    if (download && download.externalUrl !== null) {
      return ConfirmationType.EXTERNAL_URL;
    }
  }
  return ConfirmationType.NO;
}
</script>

<template>
  <div v-if="project && project.visibility !== Visibility.Public" class="mb-4">
    <Alert v-if="project.visibility === Visibility.NeedsChanges" type="danger">
      <div>
        <div class="text-bold">{{ i18n.t("visibility.notice." + project.visibility) }}</div>
        <Markdown :raw="project.lastVisibilityChangeComment || 'Unknown'" class="mt-2" inline />
        <div v-if="hasPerms(NamedPermission.EditPage)">
          <Button @click="sendForApproval">{{ i18n.t("project.sendForApproval") }}</Button>
        </div>
      </div>
    </Alert>
    <Alert v-else-if="project.visibility === Visibility.SoftDelete" type="danger">
      {{ i18n.t("visibility.notice." + project.visibility, [project.lastVisibilityChangeUserName]) }}
    </Alert>
    <Alert v-else type="danger">
      {{ i18n.t("visibility.notice." + project.visibility) }}
      <Markdown v-if="project.lastVisibilityChangeComment" :raw="project.lastVisibilityChangeComment" inline />
    </Alert>
  </div>
  <Card accent>
    <div class="flex lt-sm:flex-col">
      <UserAvatar
        class="flex-shrink-0 mr-3 lt-sm:hidden"
        :loading="!project"
        :username="project?.namespace?.owner"
        :to="'/' + project?.namespace?.owner + '/' + project?.name"
        :img-src="project?.avatarUrl"
      />
      <div class="flex-grow sm:mr-4 lt-sm:mb-4 overflow-clip overflow-hidden">
        <div class="text-2xl lt-sm:text-lg pb-1 inline-flex space-x-0.3 items-center">
          <UserAvatar
            class="!w-8 !h-8 sm:hidden"
            :loading="!project"
            :username="project?.namespace?.owner"
            :to="'/' + project?.namespace?.owner + '/' + project?.name"
            :img-src="project?.avatarUrl"
          />
          <template v-if="project">
            <NuxtLink class="!sm:ml-0 px-1 rounded hover:bg-gray-400/25 hover:dark:bg-gray-500/25" :to="'/' + project.namespace.owner">
              {{ project.namespace.owner }}
            </NuxtLink>
            <span class="text-gray-500 dark:text-gray-400"> / </span>
            <NuxtLink class="px-1 rounded hover:bg-gray-400/25 hover:dark:bg-gray-500/25" :to="'/' + project.namespace.owner + '/' + project.name">
              <h1 class="font-semibold">{{ project.name }}</h1>
            </NuxtLink>
          </template>
          <Skeleton v-else />
        </div>
        <p v-if="project" class="sm:ml-1">{{ project.description }}</p>
        <Skeleton v-else />
      </div>
      <div class="flex flex-col justify-around lt-sm:items-center space-y-2 items-end justify-between flex-shrink-0">
        <span v-if="project?.mainChannelVersions" class="inline-flex items-center">
          <Tooltip v-if="requiresConfirmation() !== ConfirmationType.NO">
            <template #content>
              {{ i18n.t(requiresConfirmation()) }}
            </template>
            <div class="mr-2 text-2xl">
              <IconMdiAlert v-if="requiresConfirmation() === ConfirmationType.EXTERNAL_URL" />
              <IconMdiProgressQuestion v-else class="text-gray-400" />
            </div>
          </Tooltip>
          <DownloadButton :project="project" />
        </span>
        <div class="flex">
          <Tooltip>
            <template #content>
              <span v-if="isOwn">{{ i18n.t("project.info.stars", 0) }}</span>
              <span v-else-if="hasStarred()">{{ i18n.t("project.actions.unstar") }}</span>
              <span v-else>{{ i18n.t("project.actions.star") }}</span>
            </template>
            <Button button-type="secondary" size="small" @click="toggleStar">
              <IconMdiStar v-if="hasStarred()" />
              <IconMdiStarOutline v-else />
              <span class="ml-2">{{ getStarredCount()?.toLocaleString("en-US") }}</span>
            </Button>
          </Tooltip>
          <!-- Tooltips mess with normal margins so this is a workaround -->
          <div class="px-1" />
          <Tooltip>
            <template #content>
              <span v-if="isOwn">{{ i18n.t("project.info.watchers", 0) }}</span>
              <span v-else-if="isWatching()">{{ i18n.t("project.actions.unwatch") }}</span>
              <span v-else>{{ i18n.t("project.actions.watch") }}</span>
            </template>
            <Button button-type="secondary" size="small" @click="toggleWatch">
              <IconMdiBell v-if="isWatching()" />
              <IconMdiBellOutline v-else />
              <span class="ml-2">{{ getWatchingCount()?.toLocaleString("en-US") }}</span>
            </Button>
          </Tooltip>
          <div class="px-1" />
          <FlagModal v-if="project" :project="project" :disabled="isOwn" :open-report="reported" @reported="reported = true" />
        </div>
      </div>
    </div>
  </Card>
</template>
