<script setup lang="ts">
import { computed, ref, Ref } from "vue";
import { useI18n } from "vue-i18n";
import UserAvatar from "~/components/UserAvatar.vue";
import Button from "~/lib/components/design/Button.vue";
import Card from "~/lib/components/design/Card.vue";
import { projectIconUrl } from "~/composables/useUrlHelper";
import { HangarProject } from "hangar-internal";
import { useInternalApi } from "~/composables/useApi";
import { handleRequestError } from "~/composables/useErrorHandling";
import { useContext } from "vite-ssr/vue";
import Tooltip from "~/lib/components/design/Tooltip.vue";
import { useAuthStore } from "~/store/auth";
import { useNotificationStore } from "~/store/notification";
import FlagModal from "~/components/modals/FlagModal.vue";
import Alert from "~/lib/components/design/Alert.vue";
import { hasPerms } from "~/composables/usePerm";
import { NamedPermission, Platform, ReviewState, Visibility } from "~/types/enums";
import Markdown from "~/components/Markdown.vue";
import { AxiosError } from "axios";
import { useRouter } from "vue-router";
import DownloadButton from "~/components/projects/DownloadButton.vue";

const ctx = useContext();
const i18n = useI18n();
const router = useRouter();
const notification = useNotificationStore();
const props = defineProps<{
  project: HangarProject;
}>();

const user = useAuthStore().user;
const slug = computed(() => props.project.namespace.owner + "/" + props.project.name);
const imageUrl = computed(() => {
  return projectIconUrl(props.project.namespace.owner, props.project.name);
});

const starred = ref(props.project.userActions.starred);
const watching = ref(props.project.userActions.watching);
const starredCount = ref(props.project.stats.stars);
const watchingCount = ref(props.project.stats.watchers);

const isOwn = computed(() => !user || user.name === props.project.namespace.owner);

function toggleState(route: string, completedKey: string, revokedKey: string, value: Ref<boolean>, count: Ref<number>) {
  useInternalApi(`projects/project/${props.project.id}/${route}/${!value.value}`, true, "post")
    .then(() => {
      value.value = !value.value;
      if (value.value) {
        count.value++;
      } else {
        count.value--;
      }

      notification.success(i18n.t("project.actions." + (value.value ? completedKey : revokedKey)));
    })
    .catch((err) => handleRequestError(err, ctx, i18n, i18n.t(`project.error.${route}`)));
}

function toggleStar() {
  toggleState("star", "starred", "unstarred", starred, starredCount);
}

function toggleWatch() {
  toggleState("watch", "watched", "unwatched", watching, watchingCount);
}

async function sendForApproval() {
  try {
    await useInternalApi(`projects/visibility/${props.project.id}/sendforapproval`, true, "post");
    notification.success(i18n.t("projectApproval.sendForApproval"));
    await router.go(0);
  } catch (e) {
    handleRequestError(e as AxiosError, ctx, i18n);
  }
}

function requiresConfirmation(): boolean {
  for (const platform in props.project.mainChannelVersions) {
    const version = props.project.mainChannelVersions[platform as Platform];
    if (version.externalUrl !== null || version.reviewState !== ReviewState.REVIEWED) {
      return true;
    }
  }
  return false;
}
</script>

<template>
  <div v-if="project.visibility !== Visibility.PUBLIC" class="mb-4">
    <Alert v-if="project.visibility === Visibility.NEEDS_CHANGES" type="danger">
      <div>
        <div class="text-bold">{{ i18n.t("visibility.notice." + project.visibility) }}</div>
        <Markdown :raw="project.lastVisibilityChangeComment || 'Unknown'" class="mt-2" inline />
        <div v-if="hasPerms(NamedPermission.EDIT_PAGE)">
          <Button @click="sendForApproval">{{ i18n.t("project.sendForApproval") }}</Button>
        </div>
      </div>
    </Alert>
    <Alert v-else-if="project.visibility === Visibility.SOFT_DELETE" type="danger">
      {{ i18n.t("visibility.notice." + project.visibility, [project.lastVisibilityChangeUserName]) }}
    </Alert>
    <Alert v-else type="danger">
      {{ i18n.t("visibility.notice." + project.visibility) }}
      <Markdown v-if="project.lastVisibilityChangeComment" :raw="project.lastVisibilityChangeComment" inline />
    </Alert>
  </div>
  <Card accent>
    <div class="flex <sm:flex-col">
      <UserAvatar
        class="flex-shrink-0 mr-4 <sm:hidden"
        :username="project.namespace.owner"
        :to="'/' + project.namespace.owner + '/' + project.name"
        :img-src="imageUrl"
      />
      <div class="flex-grow sm:mr-4 <sm:mb-4 overflow-clip overflow-hidden">
        <div class="text-2xl <sm:text-lg pb-1 inline-flex space-x-1.2 items-center">
          <UserAvatar
            class="!w-8 !h-8 sm:hidden"
            :username="project.namespace.owner"
            :to="'/' + project.namespace.owner + '/' + project.name"
            :img-src="imageUrl"
          />
          <router-link class="!sm:ml-0" :to="'/' + project.namespace.owner">{{ project.namespace.owner }}</router-link>
          <span class="text-gray-500 dark:text-gray-400"> / </span>
          <h1 class="font-semibold">{{ project.name }}</h1>
        </div>
        <p>{{ project.description }}</p>
      </div>
      <div class="flex flex-col justify-around <sm:items-center space-y-2 items-end justify-between flex-shrink-0">
        <span v-if="Object.keys(project.mainChannelVersions).length !== 0" class="inline-flex items-center">
          <Tooltip v-if="requiresConfirmation()" :content="i18n.t('project.info.lastVersionUnreviewed')">
            <IconMdiAlertCircleOutline class="mr-2 text-2xl" />
          </Tooltip>
          <DownloadButton :project="project" />
        </span>
        <div class="flex">
          <Tooltip>
            <template #content>
              <span v-if="isOwn">{{ i18n.t("project.info.stars", 0) }}</span>
              <span v-else-if="starred">{{ i18n.t("project.actions.unstar") }}</span>
              <span v-else>{{ i18n.t("project.actions.star") }}</span>
            </template>
            <Button button-type="secondary" size="small" :disabled="isOwn" @click="toggleStar">
              <IconMdiStar v-if="starred" />
              <IconMdiStarOutline v-else />
              <span class="ml-2">{{ starredCount }}</span>
            </Button>
          </Tooltip>
          <!-- Tooltips mess with normal margins so this is a workaround -->
          <div class="px-1"></div>
          <Tooltip>
            <template #content>
              <span v-if="isOwn">{{ i18n.t("project.info.watchers", 0) }}</span>
              <span v-else-if="starred">{{ i18n.t("project.actions.unwatch") }}</span>
              <span v-else>{{ i18n.t("project.actions.watch") }}</span>
            </template>
            <Button button-type="secondary" size="small" :disabled="isOwn" @click="toggleWatch">
              <IconMdiBell v-if="watching" />
              <IconMdiBellOutline v-else />
              <span class="ml-2">{{ watchingCount }}</span>
            </Button>
          </Tooltip>
          <div class="px-1"></div>
          <FlagModal :project="project" :disabled="isOwn" />
        </div>
      </div>
    </div>
  </Card>
</template>
