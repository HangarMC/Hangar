<script setup lang="ts">
import type { AxiosError } from "axios";
import { NamedPermission, ReviewState, Tag, Visibility } from "#shared/types/backend";
import type { Platform, HangarProject } from "#shared/types/backend";

const i18n = useI18n();
const router = useRouter();
const notification = useNotificationStore();
const props = defineProps<{
  project?: HangarProject;
}>();

const authStore = useAuthStore();

const starred = computed(() => props.project?.userActions?.starred);
const watching = computed(() => props.project?.userActions?.watching);
const starredCount = computed(() => props.project?.stats?.stars);
const watchingCount = computed(() => props.project?.stats?.watchers);
const reported = computed(() => props.project?.userActions?.flagged);

const isLoggedIn = computed(() => authStore.authenticated && !!authStore.user);
const isOwn = computed(() => authStore.user?.name === props.project?.namespace?.owner);

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
  if (!isLoggedIn.value) return;
  starredChanged.value = !starredChanged.value;
  toggleState("star", "starred", "unstarred", hasStarred());
}

function toggleWatch() {
  if (!isLoggedIn.value) return;
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
    if (version?.reviewState !== ReviewState.Reviewed) {
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
  <Card class="project-hero !p-4 sm:!p-5 overflow-visible">
    <div class="flex gap-4 lt-sm:flex-col">
      <UserAvatar
        class="flex-shrink-0 lt-sm:hidden shadow-lg"
        :loading="!project"
        :username="project?.namespace?.owner"
        :to="'/' + project?.namespace?.owner + '/' + project?.name"
        :img-src="project?.avatarUrl"
        size="xl"
      />
      <div class="min-w-0 flex-grow overflow-hidden">
        <div class="inline-flex items-center gap-3">
          <UserAvatar
            class="!w-14 !h-14 sm:hidden shadow-lg"
            :loading="!project"
            :username="project?.namespace?.owner"
            :to="'/' + project?.namespace?.owner + '/' + project?.name"
            :img-src="project?.avatarUrl"
          />
          <template v-if="project">
            <div class="min-w-0">
              <div class="flex flex-wrap items-baseline gap-x-3">
                <NuxtLink :to="'/' + project.namespace.owner + '/' + project.name">
                  <h1 class="truncate text-2xl sm:text-3xl font-bold">{{ project.name }}</h1>
                </NuxtLink>
                <span class="text-sm text-gray">
                  by
                  <NuxtLink class="color-primary hover:underline" :to="'/' + project.namespace.owner">
                    {{ project.namespace.owner }}
                  </NuxtLink>
                </span>
              </div>
              <p class="mt-1 text-gray-600 dark:text-gray-300">{{ project.description }}</p>
            </div>
          </template>
          <Skeleton v-else />
        </div>
        <div v-if="project" class="mt-5 flex flex-wrap items-center gap-x-4 gap-y-2 text-sm">
          <span class="inline-flex items-center gap-1.5">
            <IconMdiShapeOutline class="color-primary" />
            {{ i18n.t("project.category." + project.category) }}
          </span>
          <span v-for="tag in project.settings.tags" :key="tag" class="inline-flex items-center gap-1.5 text-gray">
            <IconMdiPuzzleOutline v-if="tag === Tag.ADDON" />
            <IconMdiBookshelf v-else-if="tag === Tag.LIBRARY" />
            <IconMdiLeaf v-else />
            {{ i18n.t("project.settings.tags." + tag + ".title") }}
          </span>
        </div>
      </div>
      <div class="flex flex-col items-end justify-between gap-5 flex-shrink-0 lt-sm:items-stretch">
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
        <div class="flex justify-end">
          <Tooltip>
            <template #content>
              <span v-if="!isLoggedIn">{{ i18n.t("general.error.401") }}</span>
              <span v-else-if="isOwn">{{ i18n.t("project.info.stars", 0) }}</span>
              <span v-else-if="hasStarred()">{{ i18n.t("project.actions.unstar") }}</span>
              <span v-else>{{ i18n.t("project.actions.star") }}</span>
            </template>
            <Button button-type="secondary" size="medium" :disabled="!isLoggedIn" @click="toggleStar">
              <IconMdiStar v-if="hasStarred()" />
              <IconMdiStarOutline v-else />
              <span class="ml-2">{{ getStarredCount()?.toLocaleString("en-US") }}</span>
            </Button>
          </Tooltip>
          <!-- Tooltips mess with normal margins so this is a workaround -->
          <div class="px-1" />
          <Tooltip>
            <template #content>
              <span v-if="!isLoggedIn">{{ i18n.t("general.error.401") }}</span>
              <span v-else-if="isOwn">{{ i18n.t("project.info.watchers", 0) }}</span>
              <span v-else-if="isWatching()">{{ i18n.t("project.actions.unwatch") }}</span>
              <span v-else>{{ i18n.t("project.actions.watch") }}</span>
            </template>
            <Button button-type="secondary" size="medium" :disabled="!isLoggedIn" @click="toggleWatch">
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

<style scoped>
.project-hero {
  background: linear-gradient(105deg, color-mix(in srgb, var(--primary-500) 22%, transparent), transparent 62%), var(--charcoal-600);
}

.light .project-hero {
  background: linear-gradient(105deg, color-mix(in srgb, var(--primary-500) 16%, transparent), transparent 62%), var(--gray-50);
}
</style>
