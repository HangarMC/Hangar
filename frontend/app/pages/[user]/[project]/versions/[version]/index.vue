<script lang="ts" setup>
import type { AxiosError } from "axios";
import { titleCase } from "scule";
import { ReviewState, PinnedStatus, NamedPermission, Visibility } from "#shared/types/backend";
import type { Platform, HangarProject, Version, User } from "#shared/types/backend";

const route = useRoute("user-project-versions-version");
const i18n = useI18n();
const router = useRouter();
const notification = useNotificationStore();
const config = useRuntimeConfig();

const props = defineProps<{
  version?: Version;
  project?: HangarProject;
  versionPlatforms: Set<Platform>;
  user?: User;
}>();

const isReviewStateChecked = computed<boolean>(
  () => props.version?.reviewState === ReviewState.PartiallyReviewed || props.version?.reviewState === ReviewState.Reviewed
);
const isUnderReview = computed<boolean>(() => props.version?.reviewState === ReviewState.UnderReview);
const currentVisibility = computed(() => useBackendData.visibilities.find((v) => (v.name as Visibility) === props.version?.visibility));
const editingPage = ref(false);
// eslint-disable-next-line vue/return-in-computed-property
const confirmationWarningKey = computed<string | undefined>(() => {
  if (props.version?.reviewState !== ReviewState.Reviewed) {
    return "version.page.unsafeWarning";
  }
  for (const platform in props.version?.downloads) {
    if (props.version.downloads[platform as Platform]?.externalUrl) {
      return "version.page.unsafeWarningExternal";
    }
  }
  return;
});
const platformsWithDependencies = computed(() => {
  const platforms = [];
  for (const platform of props.versionPlatforms) {
    if ((props.version && props.version.pluginDependencies[platform]) || hasPerms(NamedPermission.EditVersion)) {
      platforms.push(platform);
    }
  }
  return platforms;
});

function sortedDependencies(platform: Platform) {
  if (props.version && props.version.pluginDependencies[platform]) {
    return props.version.pluginDependencies[platform].toSorted((a, b) => Number(b.required) - Number(a.required));
  }
  return [];
}

const supportsString = computed(() => {
  const result = [];
  for (const platform in props.version?.platformDependenciesFormatted) {
    result.push(titleCase(platform.toLowerCase()) + " " + props.version?.platformDependenciesFormatted[platform]?.join(", "));
  }
  return result.join(", ");
});

useSeo(
  computed(() => ({
    title: `${props.project?.name} ${props.version?.name}`,
    route,
    description: `Download ${props.project?.name} ${props.version?.name} on Hangar.
  Supports ${supportsString.value}.
  Published on ${props.version && i18n.d(new Date(props.version.createdAt), "date")}.
  ${props.version?.stats?.totalDownloads} downloads.`,
    image: props.project?.avatarUrl,
    additionalScripts: [
      {
        type: "application/ld+json",
        textContent: JSON.stringify({
          "@context": "https://schema.org",
          "@type": "WebContent",
          about: {
            "@type": "WebContent",
            name: props.project?.name,
            url: config.public.host + "/" + props.project?.namespace?.owner + "/" + props.project?.namespace?.slug,
            description: props.project?.description,
          },
          author: {
            "@type": "Person",
            name: props.project?.namespace.owner,
            url: config.public.host + "/" + props.project?.namespace?.owner,
          },
          name: props.project?.name + " " + props.version?.name,
          datePublished: props.version?.createdAt,
          dateCreated: props.version?.createdAt,
          version: props.version?.name,
          url: config.public.host + route.path,
        }),
        key: "version",
      },
    ],
  }))
);

async function savePage(content: string) {
  if (!props.version) return;
  try {
    await useInternalApi(`versions/version/${props.project?.id}/${props.version.id}/saveDescription`, "post", {
      content,
    });
    // this is fine (tm)
    // eslint-disable-next-line vue/no-mutating-props
    props.version.description = content;
    editingPage.value = false;
  } catch (err) {
    handleRequestError(err, "page.new.error.save");
  }
}

async function setPinned(value: boolean) {
  if (!props.version) return;
  try {
    await useInternalApi(`versions/version/${props.project?.id}/${props.version.id}/pinned?value=${value}`, "post");
    props.version!.pinnedStatus = value ? PinnedStatus.VERSION : PinnedStatus.NONE;
    notification.success(i18n.t(`version.page.pinned.request.${value}`));
  } catch (err) {
    handleRequestError(err as AxiosError);
  }
}

async function deleteVersion(comment: string) {
  if (!props.version) return;
  try {
    await useInternalApi(`versions/version/${props.project?.id}/${props.version.id}/delete`, "post", {
      content: comment,
    });
    notification.success(i18n.t("version.success.softDelete"));
    await router.replace(`/${route.params.user}/${route.params.project}/versions`);
  } catch (err) {
    handleRequestError(err as AxiosError);
  }
}

async function hardDeleteVersion(comment: string) {
  if (!props.version) return;
  try {
    await useInternalApi(`versions/version/${props.project?.id}/${props.version.id}/hardDelete`, "post", {
      content: comment,
    });
    notification.success(i18n.t("version.success.hardDelete"));
    await router.push({
      name: "user-project-versions",
      params: {
        ...route.params,
      },
    });
  } catch (err) {
    handleRequestError(err as AxiosError);
  }
}

async function restoreVersion() {
  if (!props.version) return;
  try {
    await useInternalApi(`versions/version/${props.project?.id}/${props.version.id}/restore`, "post");
    notification.success(i18n.t("version.success.restore"));
    await router.replace(`/${route.params.user}/${route.params.project}/versions`);
  } catch (err) {
    handleRequestError(err as AxiosError);
  }
}
</script>

<template>
  <div v-if="version" class="grid grid-cols-1 gap-4 lg:grid-cols-[minmax(0,1fr)_344px] lg:items-start">
    <section class="min-w-0">
      <div class="flex flex-wrap items-center gap-3">
        <ChannelTile :channel="version.channel" size="md" />

        <h1 class="min-w-0 flex-1 truncate text-2xl font-bold">{{ version.name }}</h1>

        <div class="flex flex-shrink-0 items-center gap-2">
          <Tooltip v-if="confirmationWarningKey">
            <template #content>
              {{ i18n.t(confirmationWarningKey) }}
            </template>
            <div class="text-2xl">
              <IconMdiAlert v-if="confirmationWarningKey === 'version.page.unsafeWarningExternal'" />
              <IconMdiProgressQuestion v-else class="text-gray-400" />
            </div>
          </Tooltip>
          <DownloadButton v-if="version && project" :version="version" :project="project" :show-single-platform="false" :show-versions="false" show-file-size />
        </div>
      </div>

      <Card class="relative mt-4 pb-0 overflow-clip overflow-hidden">
        <ClientOnly v-if="hasPerms(NamedPermission.EditVersion)">
          <MarkdownEditor
            v-model:editing="editingPage"
            :raw="version.description"
            :deletable="false"
            :cancellable="true"
            :saveable="true"
            :rules="[required()]"
            @save="savePage"
          />
          <template #fallback>
            <Markdown :raw="version.description" />
          </template>
        </ClientOnly>
        <Markdown v-else :raw="version.description" />
      </Card>
    </section>

    <section class="min-w-0 flex flex-col gap-4">
      <Card v-if="hasPerms(NamedPermission.DeleteVersion) || hasPerms(NamedPermission.ViewLogs) || hasPerms(NamedPermission.Reviewer)">
        <template #header>
          <h2>{{ i18n.t("version.page.manage") }}</h2>
        </template>

        <p class="inline-flex items-center gap-1.5 text-sm">
          <IconMdiInformation class="flex-shrink-0 text-gray-secondary" />
          {{ i18n.t("version.page.visibility", [i18n.t(currentVisibility?.title || "")]) }}
        </p>

        <div class="mt-3 flex flex-wrap gap-2">
          <Tooltip>
            <template #content>
              <span v-if="version?.pinnedStatus === PinnedStatus.CHANNEL">{{ i18n.t("version.page.pinned.tooltip.channel") }}</span>
              <span v-else>{{ i18n.t(`version.page.pinned.tooltip.${version?.pinnedStatus?.toLowerCase()}`) }}</span>
            </template>
            <Button
              variant="outline"
              tone="neutral"
              size="sm"
              :disabled="version?.pinnedStatus === PinnedStatus.CHANNEL"
              @click="setPinned(version?.pinnedStatus === PinnedStatus.NONE)"
            >
              <IconMdiPinOff v-if="version?.pinnedStatus !== PinnedStatus.NONE" />
              <IconMdiPin v-else />
              {{ i18n.t(`version.page.pinned.button.${version?.pinnedStatus?.toLowerCase()}`) }}
            </Button>
          </Tooltip>

          <Button v-if="hasPerms(NamedPermission.ViewLogs)" variant="outline" tone="neutral" size="sm" @click="router.push('/admin/log')">
            <IconMdiFileDocument />
            {{ i18n.t("version.page.userAdminLogs") }}
          </Button>

          <template v-if="hasPerms(NamedPermission.Reviewer)">
            <Button variant="outline" tone="neutral" size="sm" :to="route.path + '/reviews'">
              <IconMdiListStatus v-if="isReviewStateChecked || isUnderReview" />
              <IconMdiPlay v-else />
              {{ isReviewStateChecked || isUnderReview ? i18n.t("version.page.reviewLogs") : i18n.t("version.page.reviewStart") }}
            </Button>
            <Button variant="outline" tone="neutral" size="sm" :to="route.path + '/scan'">
              <IconMdiAlertDecagram />
              {{ i18n.t("version.page.scans") }}
            </Button>
          </template>
        </div>

        <div
          v-if="hasPerms(NamedPermission.DeleteVersion) || hasPerms(NamedPermission.HardDeleteVersion) || hasPerms(NamedPermission.Reviewer)"
          class="mt-3 flex flex-wrap gap-2 border-t border-gray-300 pt-3 dark:border-gray-700"
        >
          <Button
            v-if="hasPerms(NamedPermission.Reviewer) && version.visibility === Visibility.SoftDelete"
            variant="outline"
            tone="neutral"
            size="sm"
            @click="restoreVersion"
          >
            <IconMdiRestore />
            {{ i18n.t("version.page.restore") }}
          </Button>
          <TextAreaModal
            v-if="hasPerms(NamedPermission.DeleteVersion) && version.visibility !== Visibility.SoftDelete"
            :title="i18n.t('version.page.delete')"
            :label="i18n.t('general.comment')"
            :submit="deleteVersion"
            :submit-label="i18n.t('version.page.delete')"
            submit-tone="danger"
            require-input
          >
            <template #activator="{ on }">
              <Button variant="outline" tone="danger" size="sm" v-on="on">{{ i18n.t("version.page.delete") }}</Button>
            </template>
          </TextAreaModal>
          <TextAreaModal
            v-if="hasPerms(NamedPermission.HardDeleteVersion)"
            :title="i18n.t('version.page.hardDelete')"
            :label="i18n.t('general.comment')"
            :submit="hardDeleteVersion"
            :submit-label="i18n.t('version.page.hardDelete')"
            submit-tone="danger"
            require-input
          >
            <template #activator="{ on }">
              <Button variant="outline" tone="danger" size="sm" v-on="on">{{ i18n.t("version.page.hardDelete") }}</Button>
            </template>
          </TextAreaModal>
        </div>
      </Card>

      <Card>
        <template #header>
          <h2>{{ i18n.t("project.info.title") }}</h2>
        </template>

        <dl class="flex flex-col gap-1.5">
          <div class="flex items-baseline justify-between gap-3">
            <dt class="inline-flex min-w-0 items-center gap-1.5 truncate text-gray-secondary">
              <IconMdiAccount class="flex-shrink-0" />
              {{ i18n.t("version.page.author") }}
            </dt>
            <dd class="min-w-0 truncate text-right font-semibold">
              <Link :to="'/' + version.author">{{ version.author }}</Link>
            </dd>
          </div>
          <div class="flex items-baseline justify-between gap-3">
            <dt class="inline-flex min-w-0 items-center gap-1.5 truncate text-gray-secondary">
              <IconMdiCalendar class="flex-shrink-0" />
              {{ i18n.t("project.info.publishDate") }}
            </dt>
            <dd class="flex-shrink-0 whitespace-nowrap text-right font-semibold tabular-nums">{{ i18n.d(version.createdAt, "date") }}</dd>
          </div>
          <div class="flex items-baseline justify-between gap-3">
            <dt class="inline-flex min-w-0 items-center gap-1.5 truncate text-gray-secondary">
              <IconMdiDownload class="flex-shrink-0" />
              {{ i18n.t(hasPerms(NamedPermission.IsSubjectMember) ? "project.info.totalTotalDownloads" : "project.info.totalDownloads", 0) }}
            </dt>
            <dd class="flex-shrink-0 whitespace-nowrap text-right font-semibold tabular-nums">{{ version.stats.totalDownloads.toLocaleString("en-US") }}</dd>
          </div>

          <template v-if="hasPerms(NamedPermission.IsSubjectMember)">
            <div
              v-for="platform in Object.keys(version.stats.platformDownloads)"
              :key="platform"
              class="flex items-baseline justify-between gap-3 border-t border-gray-300 pt-1.5 first:border-t-0 first:pt-0 dark:border-gray-700"
            >
              <dt class="inline-flex min-w-0 flex-shrink items-center gap-1.5 text-gray-secondary">
                <PlatformLogo :platform="platform as Platform" :size="16" class="flex-shrink-0" />
                <span class="truncate">{{ usePlatformName(platform as Platform) }}</span>
              </dt>
              <dd class="flex-shrink-0 whitespace-nowrap text-right font-semibold tabular-nums">
                {{ version.stats.platformDownloads[platform]?.toLocaleString("en-US") }}
              </dd>
            </div>
          </template>
        </dl>
      </Card>

      <Card>
        <template #header>
          <h2>{{ i18n.t("version.page.platforms") }}</h2>
        </template>

        <div v-for="platform in versionPlatforms" :key="platform" class="flex items-center gap-2 py-1">
          <PlatformLogo :platform="platform" :size="20" class="flex-shrink-0" />
          <div class="min-w-0 flex-1">
            <div class="truncate font-semibold">{{ usePlatformName(platform) }}</div>
            <div class="truncate text-sm text-gray-secondary tabular-nums" :title="version?.platformDependenciesFormatted[platform]?.join(', ')">
              {{ collapseRanges(version?.platformDependenciesFormatted[platform]) }}
            </div>
          </div>
          <PlatformVersionEditModal
            v-if="project && version && hasPerms(NamedPermission.EditVersion)"
            :project="project"
            :version="version"
            :platform="usePlatformData(platform)!"
          />
        </div>
      </Card>

      <Card v-if="hasPerms(NamedPermission.EditVersion) || platformsWithDependencies.length > 0">
        <template #header>
          <h2>{{ i18n.t("version.page.dependencies") }}</h2>
        </template>

        <div
          v-for="platform in platformsWithDependencies"
          :key="platform"
          class="border-t border-gray-300 py-2 first:border-t-0 first:pt-0 dark:border-gray-700"
        >
          <div class="flex items-center gap-2">
            <PlatformLogo :platform="platform" :size="20" class="flex-shrink-0" />
            <span class="min-w-0 flex-1 truncate font-semibold">{{ usePlatformName(platform) }}</span>
            <DependencyEditModal v-if="project && version" :project="project" :version="version" :platform="usePlatformData(platform)!" />
          </div>
          <ul v-if="sortedDependencies(platform).length > 0" class="mt-1.5 flex flex-col gap-1">
            <li v-for="dep in sortedDependencies(platform)" :key="dep.name" class="flex items-center gap-2 text-sm">
              <Link
                :href="dep.externalUrl || '/api/internal/projects/project-redirect/' + (dep.projectId ?? dep.name)"
                :target="dep.externalUrl ? '_blank' : undefined"
                class="min-w-0 inline-flex items-center gap-1 truncate font-normal"
              >
                {{ dep.name }}
                <IconMdiOpenInNew v-if="dep.externalUrl" class="flex-shrink-0 text-xs" />
              </Link>
              <span v-if="!dep.required" class="ml-auto flex-shrink-0 rounded background-card px-1.5 py-0.5 text-xs font-semibold text-gray-secondary">
                {{ i18n.t("general.optional") }}
              </span>
            </li>
          </ul>
          <p v-else class="mt-1 text-sm text-gray-secondary">{{ i18n.t("version.page.noDependencies") }}</p>
        </div>
      </Card>
    </section>
  </div>
</template>
