<script lang="ts" setup>
import { NamedPermission, Platform, ReviewState, Visibility, PinnedStatus } from "~/types/enums";
import { HangarProject, HangarVersion, IPlatform } from "hangar-internal";
import { computed, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useI18n } from "vue-i18n";
import { useContext } from "vite-ssr/vue";
import { useBackendDataStore } from "~/store/backendData";
import { lastUpdated } from "~/lib/composables/useTime";
import { useInternalApi } from "~/composables/useApi";
import { handleRequestError } from "~/composables/useErrorHandling";
import { User } from "hangar-api";
import { useErrorRedirect } from "~/lib/composables/useErrorRedirect";
import TagComponent from "~/components/Tag.vue";
import { hasPerms } from "~/composables/usePerm";
import Button from "~/lib/components/design/Button.vue";
import MarkdownEditor from "~/components/MarkdownEditor.vue";
import Markdown from "~/components/Markdown.vue";
import Card from "~/lib/components/design/Card.vue";
import Link from "~/lib/components/design/Link.vue";
import { useHead } from "@vueuse/head";
import { useSeo } from "~/composables/useSeo";
import { projectIconUrl } from "~/composables/useUrlHelper";
import { useNotificationStore } from "~/store/notification";
import DropdownButton from "~/lib/components/design/DropdownButton.vue";
import DropdownItem from "~/lib/components/design/DropdownItem.vue";
import PlatformVersionEditModal from "~/components/modals/PlatformVersionEditModal.vue";
import { AxiosError } from "axios";
import Tooltip from "~/lib/components/design/Tooltip.vue";
import DownloadButton from "~/components/projects/DownloadButton.vue";
import PlatformLogo from "~/components/logos/PlatformLogo.vue";
import TextAreaModal from "~/components/modals/TextAreaModal.vue";
import DependencyEditModal from "~/components/modals/DependencyEditModal.vue";
import { formatVersionNumbers } from "~/composables/useVersionHelper";
import filesize from "filesize";

const route = useRoute();
const i18n = useI18n();
const ctx = useContext();
const router = useRouter();
const backendData = useBackendDataStore();
const notification = useNotificationStore();

const props = defineProps<{
  versions: Map<Platform, HangarVersion>;
  project: HangarProject;
  versionPlatforms: Set<Platform>;
  user: User;
  //platform: string;
  version: string;
}>();

const p: Platform = ((route.params.platform as string) || "").toUpperCase() as Platform;
const projectVersion = computed<HangarVersion | undefined>(() => props.versions.get(p));
if (!projectVersion.value) {
  await useRouter().push(useErrorRedirect(route, 404, "Not found"));
}
const platform = computed<IPlatform | undefined>(() => backendData.platforms?.get(p));
const isReviewStateChecked = computed<boolean>(
  () => projectVersion.value?.reviewState === ReviewState.PARTIALLY_REVIEWED || projectVersion.value?.reviewState === ReviewState.REVIEWED
);
const isUnderReview = computed<boolean>(() => projectVersion.value?.reviewState === ReviewState.UNDER_REVIEW);
const approvalTooltip = computed<string>(() =>
  projectVersion.value?.reviewState === ReviewState.PARTIALLY_REVIEWED ? i18n.t("version.page.partiallyApproved") : i18n.t("version.page.approved")
);
const currentVisibility = computed(() => backendData.visibilities.find((v) => v.name === projectVersion.value?.visibility));
const editingPage = ref(false);

const sortedDependencies = computed(() => {
  if (platform.value && projectVersion.value && projectVersion.value.pluginDependencies[p]) {
    return [...projectVersion.value.pluginDependencies[p]].sort((a, b) => Number(b.required) - Number(a.required));
  }
  return [];
});

useHead(
  useSeo(
    props.project?.name + " " + projectVersion.value?.name,
    props.project.description,
    route,
    projectIconUrl(props.project.namespace.owner, props.project.namespace.slug)
  )
);

async function savePage(content: string) {
  try {
    await useInternalApi(`versions/version/${props.project.id}/${projectVersion.value?.id}/saveDescription`, true, "post", {
      content,
    });
    if (projectVersion.value) {
      projectVersion.value.description = content;
    }
    editingPage.value = false;
  } catch (err) {
    handleRequestError(err as AxiosError, ctx, i18n, "page.new.error.save");
  }
}

async function setPinned(value: boolean) {
  try {
    await useInternalApi(`versions/version/${props.project.id}/${projectVersion.value?.id}/pinned?value=${value}`, true, "post");
    notification.success(i18n.t(`version.page.pinned.request.${value}`));
    router.go(0);
  } catch (e) {
    handleRequestError(e as AxiosError, ctx, i18n);
  }
}

async function deleteVersion(comment: string) {
  try {
    await useInternalApi(`versions/version/${props.project.id}/${projectVersion.value?.id}/delete`, true, "post", {
      content: comment,
    });
    notification.success(i18n.t("version.success.softDelete"));
    router.go(0);
  } catch (e) {
    handleRequestError(e as AxiosError, ctx, i18n);
  }
}

async function hardDeleteVersion(comment: string) {
  try {
    await useInternalApi(`versions/version/${props.project.id}/${projectVersion.value?.id}/hardDelete`, true, "post", {
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
    handleRequestError(e as AxiosError, ctx, i18n);
  }
}

async function restoreVersion() {
  try {
    await useInternalApi(`versions/version/${props.project.id}/${projectVersion.value?.id}/restore`, true, "post");
    notification.success(i18n.t("version.success.restore"));
    router.go(0);
  } catch (e) {
    handleRequestError(e as AxiosError, ctx, i18n);
  }
}
</script>

<template>
  <div v-if="projectVersion" class="flex <sm:flex-col flex-wrap md:flex-nowrap gap-4">
    <section class="basis-full md:basis-9/12 flex-grow overflow-auto">
      <div class="flex flex-wrap gap-2 justify-between">
        <div>
          <h1 class="text-3xl sm:inline-flex items-center">
            <TagComponent class="mr-1" :name="projectVersion.channel.name" :color="{ background: projectVersion.channel.color }" :short-form="true" />
            {{ projectVersion.name }}
            <Tooltip v-if="isReviewStateChecked" :content="approvalTooltip" class="text-base">
              <IconMdiCheckCircleOutline class="text-2xl ml-1" />
            </Tooltip>
          </h1>
          <h2>
            <span class="inline-flex <sm:flex-wrap ml-1">
              {{ i18n.t("version.page.subheader", [projectVersion.author, lastUpdated(new Date(projectVersion.createdAt))]) }}
              <span v-if="projectVersion.fileInfo?.sizeBytes" class="inline-flex items-center sm:ml-3">
                <IconMdiFile class="mr-1" />
                {{ filesize(projectVersion.fileInfo.sizeBytes) }}
              </span>
            </span>
          </h2>
          <em v-if="hasPerms(NamedPermission.REVIEWER) && projectVersion.approvedBy" class="text-lg ml-1">
            {{ i18n.t("version.page.adminMsg", [projectVersion.approvedBy, i18n.d(projectVersion.createdAt, "date")]) }}
          </em>
        </div>
        <div class="inline-flex items-center flex-grow">
          <div class="flex-grow"></div>
          <DropdownButton v-if="versionPlatforms.size > 1" class="inline" :name="platform?.name" button-size="large">
            <DropdownItem v-for="plat in versionPlatforms" :key="plat" :to="plat.toLowerCase()">{{ backendData.platforms?.get(plat)?.name }}</DropdownItem>
          </DropdownButton>

          <DownloadButton :version="projectVersion" :project="project" :platform="p" class="ml-2" />
        </div>
      </div>

      <Card class="relative mt-4 overflow-clip overflow-hidden">
        <MarkdownEditor
          v-if="hasPerms(NamedPermission.EDIT_VERSION)"
          ref="editor"
          v-model:editing="editingPage"
          :raw="projectVersion.description"
          :deletable="false"
          :cancellable="true"
          :saveable="true"
          @save="savePage"
        />
        <Markdown v-else :raw="projectVersion.description" />
      </Card>
    </section>
    <section class="basis-full md:basis-3/12 flex-grow space-y-4">
      <Card v-if="hasPerms(NamedPermission.DELETE_VERSION) || hasPerms(NamedPermission.VIEW_LOGS) || hasPerms(NamedPermission.REVIEWER)">
        <template #header>{{ i18n.t("version.page.manage") }}</template>

        <span class="inline-flex items-center">
          <IconMdiInformation class="mr-1" />
          {{ i18n.t("version.page.visibility", [i18n.t(currentVisibility.title)]) }}
        </span>

        <div class="flex gap-2 flex-wrap mt-2">
          <Tooltip>
            <template #content>
              <span v-if="projectVersion.pinnedStatus === PinnedStatus.CHANNEL">{{ i18n.t("version.page.pinned.tooltip.channel") }}</span>
              <span v-else>{{ i18n.t(`version.page.pinned.tooltip.${projectVersion.pinnedStatus.toLowerCase()}`) }}</span>
            </template>
            <Button
              size="small"
              :disabled="projectVersion.pinnedStatus === PinnedStatus.CHANNEL"
              @click="setPinned(projectVersion.pinnedStatus === PinnedStatus.NONE)"
            >
              <IconMdiPinOff v-if="projectVersion.pinnedStatus !== PinnedStatus.NONE" class="mr-1" />
              <IconMdiPin v-else class="mr-1" />
              {{ i18n.t(`version.page.pinned.button.${projectVersion.pinnedStatus.toLowerCase()}`) }}
            </Button>
          </Tooltip>

          <!--todo route for user action log, with filtering-->
          <Button v-if="hasPerms(NamedPermission.VIEW_LOGS)" @click="router.push('/admin/log')">
            {{ i18n.t("version.page.userAdminLogs") }}
          </Button>

          <template v-if="hasPerms(NamedPermission.REVIEWER)">
            <Button v-if="isReviewStateChecked" color="success" :to="route.path + '/reviews'">
              <IconMdiListStatus />
              {{ i18n.t("version.page.reviewLogs") }}
            </Button>
            <Button v-else-if="isUnderReview" color="info" :to="route.path + '/reviews'">
              <IconMdiListStatus />
              {{ i18n.t("version.page.reviewLogs") }}
            </Button>
            <Button v-else color="success" :to="route.path + '/reviews'">
              <IconMdiPlay />
              {{ i18n.t("version.page.reviewStart") }}
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
            <span class="flex-grow">{{ i18n.t("version.page.platform") }}</span>
            <PlatformVersionEditModal v-if="hasPerms(NamedPermission.EDIT_VERSION)" :project="project" :versions="versions" />
          </div>
        </template>

        <div class="flex items-center">
          <PlatformLogo :platform="platform?.enumName" :size="24" class="mr-1" />
          {{ platform?.name }}
          {{ formatVersionNumbers(projectVersion?.platformDependencies[platform?.enumName]) }}
        </div>
      </Card>

      <Card v-if="projectVersion.pluginDependencies[platform?.name.toUpperCase()] || hasPerms(NamedPermission.EDIT_VERSION)">
        <template #header>
          <div class="inline-flex w-full">
            <span class="flex-grow">{{ i18n.t("version.page.dependencies") }}</span>
            <DependencyEditModal :project="project" :versions="versions" />
          </div>
        </template>

        <ul>
          <li v-for="dep in sortedDependencies" :key="dep.name">
            <Link
              :href="dep.externalUrl || undefined"
              :target="dep.externalUrl ? '_blank' : undefined"
              :to="!!dep.namespace ? { name: 'user-project', params: { user: dep.namespace.owner, project: dep.namespace.slug } } : undefined"
            >
              {{ dep.name }}
              <small v-if="!dep.required">({{ i18n.t("general.optional") }})</small>
            </Link>
          </li>
        </ul>
      </Card>
    </section>
  </div>
</template>
