<script lang="ts" setup>
import { NamedPermission, Platform, ReviewState } from "~/types/enums";
import { HangarProject, HangarVersion, IPlatform } from "hangar-internal";
import { computed, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useI18n } from "vue-i18n";
import { useContext } from "vite-ssr/vue";
import { useBackendDataStore } from "~/store/backendData";
import { useInternalApi } from "~/composables/useApi";
import { handleRequestError } from "~/composables/useErrorHandling";
import { Tag } from "hangar-api";
import { useErrorRedirect } from "~/composables/useErrorRedirect";
import TagComponent from "~/components/Tag.vue";
import { hasPerms } from "~/composables/usePerm";
import Button from "~/components/design/Button.vue";
import Alert from "~/components/design/Alert.vue";
import MarkdownEditor from "~/components/MarkdownEditor.vue";
import Markdown from "~/components/Markdown.vue";
import Card from "~/components/design/Card.vue";
import Link from "~/components/design/Link.vue";
import { useHead } from "@vueuse/head";
import { useSeo } from "~/composables/useSeo";
import { projectIconUrl } from "~/composables/useUrlHelper";
import { useNotificationStore } from "~/store/notification";
import DropdownButton from "~/components/design/DropdownButton.vue";
import DropdownItem from "~/components/design/DropdownItem.vue";
import PlatformVersionEditModal from "~/components/modals/PlatformVersionEditModal.vue";

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
const channel = computed<Tag | null>(() => projectVersion.value?.tags?.find((t) => t?.name === "Channel") || null);
const approvalTooltip = computed<string>(() =>
  projectVersion.value?.reviewState === ReviewState.PARTIALLY_REVIEWED ? i18n.t("version.page.partiallyApproved") : i18n.t("version.page.approved")
);
const platformTag = computed<Tag | null>(() => projectVersion.value?.tags.find((t) => t?.name === platform.value?.name) || null);
const requiresConfirmation = computed<boolean>(() => projectVersion.value?.externalUrl !== null || projectVersion.value?.reviewState !== ReviewState.REVIEWED);
const editingPage = ref(false);

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
    // this.$refs.editor.loading.save = false; // TODO
    handleRequestError(err, ctx, i18n, "page.new.error.save");
  }
}

async function setRecommended() {
  //this.loading.recommend = true;
  try {
    await useInternalApi(`versions/version/${props.project.id}/${projectVersion.value?.id}/${platform.value?.enumName}/recommend`, true, "post");
    notification.success(i18n.t("version.success.recommended", [platform.value?.name]));
    // this.$nuxt.refresh(); // TODO
  } catch (e) {
    handleRequestError(e, ctx, i18n);
  }
  // this.loading.recommend = false;
}

async function deleteVersion(comment: string) {
  try {
    await useInternalApi(`versions/version/${props.project.id}/${projectVersion.value?.id}/delete`, true, "post", {
      content: comment,
    });
    notification.success(i18n.t("version.success.softDelete"));
    // this.$nuxt.refresh();  // TODO
  } catch (e) {
    handleRequestError(e, ctx, i18n);
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
    handleRequestError(e, ctx, i18n);
  }
}

async function restoreVersion() {
  try {
    await useInternalApi(`versions/version/${props.project.id}/${projectVersion.value?.id}/restore`, true, "post");
    notification.success(i18n.t("version.success.restore"));
    // this.$nuxt.refresh(); // TODO
  } catch (e) {
    handleRequestError(e, ctx, i18n);
  }
}
</script>

<template>
  <div v-if="projectVersion" class="flex">
    <div>
      <h1 class="text-3xl">
        {{ projectVersion.name }}
        <DropdownButton v-if="versionPlatforms.size > 1" class="text-2xl inline" :name="platform?.name">
          <DropdownItem v-for="plat in versionPlatforms" :key="plat" :to="plat.toLowerCase()">{{ backendData.platforms?.get(plat)?.name }}</DropdownItem>
        </DropdownButton>
      </h1>
      <TagComponent :tag="channel" :short-form="true" />
      <h2>{{ i18n.t("version.page.subheader", [projectVersion.author, i18n.d(projectVersion.createdAt, "date")]) }}</h2>
    </div>
    <div class="flex-right">
      <h2>
        <em v-if="hasPerms(NamedPermission.REVIEWER) && projectVersion.approvedBy" class="mr-1">
          {{ i18n.t("version.page.adminMsg", [projectVersion.approvedBy, i18n.d(projectVersion.createdAt, "date")]) }}
        </em>
        <IconMdiDiamondStone v-if="projectVersion.recommended.includes(platform?.enumName)" :title="i18n.t('version.page.recommended')" />
        <IconMdiCheckCircleOutline v-if="isReviewStateChecked" :title="approvalTooltip" />
      </h2>

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

      <!-- todo set recommended -->
      <!-- todo delete -->
      <!-- todo download -->
      <!-- todo admin actions -->
    </div>
  </div>

  <div v-if="projectVersion" class="flex flex-wrap md:flex-nowrap gap-4">
    <section class="basis-full md:basis-8/12 flex-grow">
      <Alert v-if="requiresConfirmation" class="mb-8" type="info">{{ i18n.t("version.page.unsafeWarning") }}</Alert>
      <Card>
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

    <section class="basis-full md:basis-4/12 flex-grow space-y-4">
      <Card>
        <template #header>
          {{ i18n.t("version.page.platform") }}
          <PlatformVersionEditModal :project="project" :versions="versions" />
        </template>

        <!-- todo platform icon -->
        {{ platform?.name }}
        {{ platformTag.data }}
      </Card>

      <Card>
        <template #header>
          {{ i18n.t("version.page.dependencies") }}
          <!-- todo DependencyEditModal -->
          <!-- <DependencyEditModal :project="project" :versions="versions" /> -->
        </template>

        <ul>
          <li v-for="dep in projectVersion.pluginDependencies[platform?.name.toUpperCase()]" :key="dep.name">
            <Link
              :href="dep.externalUrl || undefined"
              :target="dep.externalUrl ? '_blank' : undefined"
              :to="!!dep.namespace ? { name: 'user-project', params: { user: dep.namespace.owner, project: dep.namespace.slug } } : undefined"
            >
              {{ dep.name }}
              <small v-if="dep.required">({{ i18n.t("general.required") }})</small>
            </Link>
          </li>
        </ul>
      </Card>
    </section>
  </div>
</template>
