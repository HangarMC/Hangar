<script lang="ts" setup>
import type { HangarProject, HangarVersion } from "hangar-internal";
import { computed, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useI18n } from "vue-i18n";
import type { User } from "hangar-api";
import { useHead } from "@unhead/vue";
import type { AxiosError } from "axios";
import type { Platform } from "~/types/enums";
import { NamedPermission, ReviewState, Visibility, PinnedStatus } from "~/types/enums";
import { useBackendData } from "~/store/backendData";
import { lastUpdated } from "~/composables/useTime";
import { useInternalApi } from "~/composables/useApi";
import { handleRequestError } from "~/composables/useErrorHandling";
import { useErrorRedirect } from "~/composables/useErrorRedirect";
import TagComponent from "~/components/Tag.vue";
import { hasPerms } from "~/composables/usePerm";
import Button from "~/components/design/Button.vue";
import { required } from "~/composables/useValidationHelpers";

import { MarkdownEditor } from "#components";
import Markdown from "~/components/Markdown.vue";
import Card from "~/components/design/Card.vue";
import Link from "~/components/design/Link.vue";
import { useSeo } from "~/composables/useSeo";
import { useNotificationStore } from "~/store/notification";
import Tooltip from "~/components/design/Tooltip.vue";
import DownloadButton from "~/components/projects/DownloadButton.vue";
import PlatformLogo from "~/components/logos/platforms/PlatformLogo.vue";
import TextAreaModal from "~/components/modals/TextAreaModal.vue";
import DependencyEditModal from "~/components/modals/DependencyEditModal.vue";
import Spoiler from "~/components/design/Spoiler.vue";
import PlatformVersionEditModal from "~/components/modals/PlatformVersionEditModal.vue";

const route = useRoute();
const i18n = useI18n();
const router = useRouter();
const notification = useNotificationStore();

const props = defineProps<{
  version: HangarVersion;
  project: HangarProject;
  versionPlatforms: Set<Platform>;
  user?: User;
}>();

const projectVersion = computed<HangarVersion | undefined>(() => props.version);
if (!projectVersion.value) {
  throw useErrorRedirect(route, 404, "Not found");
}
const isReviewStateChecked = computed<boolean>(
  () => projectVersion.value?.reviewState === ReviewState.PARTIALLY_REVIEWED || projectVersion.value?.reviewState === ReviewState.REVIEWED
);
const isUnderReview = computed<boolean>(() => projectVersion.value?.reviewState === ReviewState.UNDER_REVIEW);
const currentVisibility = computed(() => useBackendData.visibilities.find((v) => v.name === projectVersion.value?.visibility));
const editingPage = ref(false);
const confirmationWarningKey = computed<string | null>(() => {
  if (projectVersion.value?.reviewState !== ReviewState.REVIEWED) {
    return "version.page.unsafeWarning";
  }
  for (const platform in projectVersion.value?.downloads) {
    if (projectVersion.value.downloads[platform as Platform].externalUrl !== null) {
      return "version.page.unsafeWarningExternal";
    }
  }
  return null;
});
const platformsWithDependencies = computed(() => {
  const platforms = [];
  for (const platform of props.versionPlatforms) {
    if ((projectVersion.value && projectVersion.value.pluginDependencies[platform]) || hasPerms(NamedPermission.EDIT_VERSION)) {
      platforms.push(platform);
    }
  }
  return platforms;
});

function sortedDependencies(platform: Platform) {
  if (projectVersion.value && projectVersion.value.pluginDependencies[platform]) {
    return [...projectVersion.value.pluginDependencies[platform]].sort((a, b) => Number(b.required) - Number(a.required));
  }
  return [];
}

useHead(useSeo(props.project?.name + " " + projectVersion.value?.name, props.project.description, route, props.project.avatarUrl));

async function savePage(content: string) {
  try {
    await useInternalApi(`versions/version/${props.project.id}/${projectVersion.value?.id}/saveDescription`, "post", {
      content,
    });
    if (projectVersion.value) {
      projectVersion.value.description = content;
    }
    editingPage.value = false;
  } catch (err) {
    handleRequestError(err, "page.new.error.save");
  }
}

const pinnedStatus = ref(projectVersion.value?.pinnedStatus);

async function setPinned(value: boolean) {
  try {
    await useInternalApi(`versions/version/${props.project.id}/${projectVersion.value?.id}/pinned?value=${value}`, "post");
    pinnedStatus.value = value ? PinnedStatus.VERSION : PinnedStatus.NONE;
    notification.success(i18n.t(`version.page.pinned.request.${value}`));
  } catch (e) {
    handleRequestError(e as AxiosError);
  }
}

async function deleteVersion(comment: string) {
  try {
    await useInternalApi(`versions/version/${props.project.id}/${projectVersion.value?.id}/delete`, "post", {
      content: comment,
    });
    notification.success(i18n.t("version.success.softDelete"));
    await router.replace(`/${route.params.user}/${route.params.project}/versions`);
  } catch (e) {
    handleRequestError(e as AxiosError);
  }
}

async function hardDeleteVersion(comment: string) {
  try {
    await useInternalApi(`versions/version/${props.project.id}/${projectVersion.value?.id}/hardDelete`, "post", {
      content: comment,
    });
    notification.success(i18n.t("version.success.hardDelete"));
    await router.push({
      name: "user-project-versions",
      params: {
        ...route.params,
      },
    });
  } catch (e) {
    handleRequestError(e as AxiosError);
  }
}

async function restoreVersion() {
  try {
    await useInternalApi(`versions/version/${props.project.id}/${projectVersion.value?.id}/restore`, "post");
    notification.success(i18n.t("version.success.restore"));
    await router.replace(`/${route.params.user}/${route.params.project}/versions`);
  } catch (e) {
    handleRequestError(e as AxiosError);
  }
}
</script>

<template>
  <div v-if="projectVersion" class="flex lt-md:flex-col flex-wrap lg:flex-nowrap gap-4">
    <section class="basis-full lg:basis-11/15 overflow-clip">
      <div class="flex gap-2 justify-between">
        <div>
          <h2 class="text-3xl sm:inline-flex items-center gap-x-1">
            <TagComponent
              class="mr-1"
              :name="projectVersion.channel.name"
              :color="{ background: projectVersion.channel.color }"
              :short-form="true"
              :tooltip="projectVersion.channel.description"
            />
            {{ projectVersion.name }}
          </h2>
          <h3>
            <span class="inline-flex lt-md:flex-wrap">
              {{ i18n.t("version.page.subheader", [projectVersion.author, lastUpdated(new Date(projectVersion.createdAt))]) }}
            </span>
          </h3>
          <em v-if="hasPerms(NamedPermission.REVIEWER) && projectVersion.approvedBy">
            {{ i18n.t("version.page.adminMsg", [projectVersion.approvedBy, i18n.d(projectVersion.createdAt, "date")]) }}
          </em>
        </div>
        <div class="inline-flex items-center flex-grow space-x-2">
          <div class="flex-grow" />
          <Tooltip v-if="confirmationWarningKey" :content="i18n.t(confirmationWarningKey)">
            <div class="text-2xl">
              <IconMdiAlert v-if="confirmationWarningKey === 'version.page.unsafeWarningExternal'" />
              <IconMdiProgressQuestion v-else class="text-gray-400" />
            </div>
          </Tooltip>
          <DownloadButton :version="projectVersion" :project="project" :show-single-platform="false" :show-versions="false" show-file-size />
        </div>
      </div>

      <Card class="relative mt-4 pb-0 overflow-clip overflow-hidden">
        <ClientOnly v-if="hasPerms(NamedPermission.EDIT_VERSION)">
          <MarkdownEditor
            ref="editor"
            v-model:editing="editingPage"
            :raw="projectVersion.description"
            :deletable="false"
            :cancellable="true"
            :saveable="true"
            :rules="[required()]"
            @save="savePage"
          />
          <template #fallback>
            <Markdown :raw="projectVersion.description" />
          </template>
        </ClientOnly>
        <Markdown v-else :raw="projectVersion.description" />
      </Card>
    </section>
    <section class="basis-full lg:basis-4/15 flex-grow space-y-4">
      <Card v-if="hasPerms(NamedPermission.DELETE_VERSION) || hasPerms(NamedPermission.VIEW_LOGS) || hasPerms(NamedPermission.REVIEWER)">
        <template #header>
          <h3>{{ i18n.t("version.page.manage") }}</h3>
        </template>

        <span class="inline-flex items-center">
          <IconMdiInformation class="mr-1" />
          {{ i18n.t("version.page.visibility", [i18n.t(currentVisibility.title)]) }}
        </span>

        <div class="flex gap-2 flex-wrap mt-2">
          <Tooltip>
            <template #content>
              <span v-if="pinnedStatus === PinnedStatus.CHANNEL">{{ i18n.t("version.page.pinned.tooltip.channel") }}</span>
              <span v-else>{{ i18n.t(`version.page.pinned.tooltip.${pinnedStatus.toLowerCase()}`) }}</span>
            </template>
            <Button size="small" :disabled="pinnedStatus === PinnedStatus.CHANNEL" @click="setPinned(pinnedStatus === PinnedStatus.NONE)">
              <IconMdiPinOff v-if="pinnedStatus !== PinnedStatus.NONE" class="mr-1" />
              <IconMdiPin v-else class="mr-1" />
              {{ i18n.t(`version.page.pinned.button.${pinnedStatus.toLowerCase()}`) }}
            </Button>
          </Tooltip>

          <!--todo route for user action log, with filtering-->
          <Button v-if="hasPerms(NamedPermission.VIEW_LOGS)" @click="router.push('/admin/log')">
            <IconMdiFileDocument />
            {{ i18n.t("version.page.userAdminLogs") }}
          </Button>

          <template v-if="hasPerms(NamedPermission.REVIEWER)">
            <Button v-if="isReviewStateChecked" :to="route.path + '/reviews'">
              <IconMdiListStatus />
              {{ i18n.t("version.page.reviewLogs") }}
            </Button>
            <Button v-else-if="isUnderReview" :to="route.path + '/reviews'">
              <IconMdiListStatus />
              {{ i18n.t("version.page.reviewLogs") }}
            </Button>
            <Button v-else :to="route.path + '/reviews'">
              <IconMdiPlay />
              {{ i18n.t("version.page.reviewStart") }}
            </Button>
            <Button :to="route.path + '/scan'">
              <IconMdiAlertDecagram />
              {{ i18n.t("version.page.scans") }}
            </Button>
          </template>

          <Button v-if="hasPerms(NamedPermission.REVIEWER) && projectVersion.visibility === Visibility.SOFT_DELETE" @click="restoreVersion">
            {{ i18n.t("version.page.restore") }}
          </Button>
          <TextAreaModal
            v-if="hasPerms(NamedPermission.DELETE_VERSION) && projectVersion.visibility !== Visibility.SOFT_DELETE"
            :title="i18n.t('version.page.delete')"
            :label="i18n.t('general.comment')"
            :submit="deleteVersion"
            require-input
          >
            <template #activator="{ on }">
              <Button button-type="red" v-on="on">{{ i18n.t("version.page.delete") }}</Button>
            </template>
          </TextAreaModal>
          <TextAreaModal
            v-if="hasPerms(NamedPermission.HARD_DELETE_VERSION)"
            :title="i18n.t('version.page.hardDelete')"
            :label="i18n.t('general.comment')"
            :submit="hardDeleteVersion"
            require-input
          >
            <template #activator="{ on }">
              <Button button-type="red" v-on="on">{{ i18n.t("version.page.hardDelete") }}</Button>
            </template>
          </TextAreaModal>
        </div>
      </Card>

      <Card>
        <template #header>
          <div class="inline-flex w-full">
            <h3 class="flex-grow">{{ i18n.t("project.info.title") }}</h3>
          </div>
        </template>

        <table class="w-full">
          <tbody>
            <tr>
              <th class="text-left">{{ i18n.t("project.info.publishDate") }}</th>
              <td class="text-right">{{ i18n.d(version.createdAt, "date") }}</td>
            </tr>
            <tr>
              <th class="text-left">
                {{ i18n.t(hasPerms(NamedPermission.IS_SUBJECT_MEMBER) ? "project.info.totalTotalDownloads" : "project.info.totalDownloads", 0) }}
              </th>
              <td class="text-right">
                {{ version.stats.totalDownloads.toLocaleString("en-US") }}
              </td>
            </tr>
            <!-- Only show per platform downloads to project members, otherwise not too relevant and only adding to height -->
            <tr v-for="platform in hasPerms(NamedPermission.IS_SUBJECT_MEMBER) ? Object.keys(version.stats.platformDownloads) : []" :key="platform">
              <th class="text-left inline-flex">
                <PlatformLogo :platform="platform" :size="24" class="mr-1" />
                {{ i18n.t("project.info.totalDownloads", 0) }}
              </th>
              <td class="text-right">
                {{ version.stats.platformDownloads[platform].toLocaleString("en-US") }}
              </td>
            </tr>
          </tbody>
        </table>
      </Card>

      <Card>
        <template #header>
          <div class="inline-flex w-full">
            <h3 class="flex-grow">{{ i18n.t("version.page.platforms") }}</h3>
          </div>
        </template>

        <div v-for="platform in versionPlatforms" :key="platform" class="flex items-center mb-1">
          <PlatformLogo :platform="platform" :size="24" class="mr-1 flex-shrink-0" />
          {{ useBackendData.platforms.get(platform).name }}
          ({{ projectVersion?.platformDependenciesFormatted[platform] }})
          <span class="flex-grow" />
          <PlatformVersionEditModal
            v-if="hasPerms(NamedPermission.EDIT_VERSION)"
            :project="project"
            :version="version"
            :platform="useBackendData.platforms.get(platform)"
          />
        </div>
      </Card>

      <Card v-if="hasPerms(NamedPermission.EDIT_VERSION) || platformsWithDependencies.length !== 0">
        <template #header>
          <div class="inline-flex w-full">
            <h3 class="flex-grow">{{ i18n.t("version.page.dependencies") }}</h3>
          </div>
        </template>

        <div v-for="platform in platformsWithDependencies" :key="platform" class="py-1">
          <Spoiler :with-line="projectVersion?.pluginDependencies[platform] !== undefined" open>
            <template #title>
              <div class="flex gap-1 w-full">
                <PlatformLogo :platform="platform" :size="24" class="flex-shrink-0" />
                {{ useBackendData.platforms.get(platform).name }}
                <span class="flex-grow" />
                <DependencyEditModal :project="project" :version="version" :platform="useBackendData.platforms.get(platform)" />
              </div>
            </template>
            <template #content>
              <div>
                <ul>
                  <li v-for="dep in sortedDependencies(platform)" :key="dep.name">
                    <Link
                      :href="dep.externalUrl || undefined"
                      :target="dep.externalUrl ? '_blank' : undefined"
                      :to="!!dep.namespace ? { name: 'user-project', params: { user: dep.namespace.owner, project: dep.namespace.slug } } : undefined"
                      class="font-normal ml-1"
                    >
                      {{ dep.name }}
                      <small v-if="!dep.required">({{ i18n.t("general.optional") }})</small>
                    </Link>
                  </li>
                </ul>
              </div>
            </template>
          </Spoiler>
        </div>
      </Card>
    </section>
  </div>
</template>
