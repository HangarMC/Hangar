<script lang="ts" setup>
import type { AxiosError } from "axios";
import { titleCase } from "scule";
import { ReviewState, PinnedStatus, NamedPermission, Visibility } from "#shared/types/backend";
import type { Platform, HangarProject, Version, User } from "#shared/types/backend";

const route = useRoute("user-project-versions-version");
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
const visibilityLabel = computed(() => titleCase((props.version?.visibility || "unknown").toLowerCase()));
const formattedCreatedAt = computed(() =>
  props.version ? new Intl.DateTimeFormat("en", { dateStyle: "medium" }).format(new Date(props.version.createdAt)) : ""
);
const editingPage = ref(false);
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
  Published on ${formattedCreatedAt.value}.
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
    notification.success("Saved!");
  } catch (err) {
    handleRequestError(err, "page.new.error.save");
  }
}

async function setPinned(value: boolean) {
  if (!props.version) return;
  try {
    await useInternalApi(`versions/version/${props.project?.id}/${props.version.id}/pinned?value=${value}`, "post");
    props.version!.pinnedStatus = value ? PinnedStatus.VERSION : PinnedStatus.NONE;
    notification.success(value ? "Successfully pinned this version" : "Successfully unpinned this version");
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
    notification.success("Version deleted");
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
    notification.success("Version permanently deleted");
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
    notification.success("Version restored");
    await router.replace(`/${route.params.user}/${route.params.project}/versions`);
  } catch (err) {
    handleRequestError(err as AxiosError);
  }
}
</script>

<template>
  <div v-if="version" class="grid grid-cols-1 items-start gap-4 lg:grid-cols-[minmax(0,1fr)_300px] xl:grid-cols-[minmax(0,1fr)_320px]">
    <section class="min-w-0 space-y-4">
      <Card class="!p-4 overflow-visible">
        <div class="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
          <div class="min-w-0">
            <div class="flex flex-wrap items-center gap-2">
              <Tag :name="version.channel.name" :color="{ background: version.channel.color }" :tooltip="version.channel.description" />
              <span v-if="version.visibility !== Visibility.Public" class="inline-flex items-center gap-1 text-sm text-gray">
                <IconMdiEyeOff />
                {{ visibilityLabel }}
              </span>
            </div>
            <h1 class="mt-2 break-words text-2xl font-bold sm:text-3xl">{{ version.name }}</h1>
            <p class="mt-1 text-sm text-gray">
              Released by
              <NuxtLink :to="`/${version.author}`" class="color-primary hover:underline">{{ version.author }}</NuxtLink>
              {{ lastUpdated(new Date(version.createdAt)) }}
            </p>
          </div>
          <div class="flex flex-shrink-0 items-center gap-2">
            <Tooltip v-if="confirmationWarningKey">
              <template #content>
                {{
                  confirmationWarningKey === "version.page.unsafeWarningExternal"
                    ? "External downloads are not under our control and may not be safe to use"
                    : "This version has not been reviewed by staff and may not be safe to download"
                }}
              </template>
              <div class="text-2xl">
                <IconMdiAlert v-if="confirmationWarningKey === 'version.page.unsafeWarningExternal'" />
                <IconMdiProgressQuestion v-else class="text-gray-400" />
              </div>
            </Tooltip>
            <DownloadButton
              v-if="version && project"
              :version="version"
              :project="project"
              :show-single-platform="false"
              :show-versions="false"
              show-file-size
            />
          </div>
        </div>
      </Card>

      <Card class="relative !p-0 overflow-hidden">
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
    <aside class="space-y-4 self-start lg:sticky lg:top-4">
      <Card class="!p-0 overflow-hidden">
        <template #header>
          <div class="flex items-center gap-2 px-4 pt-3.5 pb-1">
            <h2>Version info</h2>
          </div>
        </template>
        <div class="px-4 pt-1 pb-3">
          <div class="flex items-center gap-3 py-1.5">
            <IconMdiCalendarOutline class="flex-shrink-0 text-lg text-gray" />
            <div class="min-w-0">
              <div class="text-xs text-gray">Published</div>
              <div class="font-semibold">{{ formattedCreatedAt }}</div>
            </div>
          </div>
          <div class="flex items-center gap-3 py-1.5">
            <IconMdiAccountOutline class="flex-shrink-0 text-lg text-gray" />
            <div class="min-w-0">
              <div class="text-xs text-gray">Author</div>
              <NuxtLink :to="`/${version.author}`" class="truncate font-semibold color-primary hover:underline">{{ version.author }}</NuxtLink>
            </div>
          </div>
        </div>
        <div
          class="grid gap-2 border-t px-3 py-3 dark:border-gray-800"
          :class="hasPerms(NamedPermission.IsSubjectMember) && Object.keys(version.stats.platformDownloads).length > 0 ? 'grid-cols-2' : 'grid-cols-1'"
        >
          <div class="flex min-h-17 flex-col rounded-lg bg-gray-100 p-1.5 text-center dark:bg-charcoal-500">
            <div class="flex flex-1 items-center justify-center font-semibold">
              {{ version.stats.totalDownloads.toLocaleString("en-US") }}
            </div>
            <div class="flex items-center justify-center gap-1 text-[0.65rem] text-gray">
              <IconMdiDownloadOutline />
              <span>Downloads</span>
            </div>
          </div>
          <div
            v-for="platform in hasPerms(NamedPermission.IsSubjectMember) ? Object.keys(version.stats.platformDownloads) : []"
            :key="platform"
            class="flex min-h-17 flex-col rounded-lg bg-gray-100 p-1.5 text-center dark:bg-charcoal-500"
          >
            <div class="flex flex-1 items-center justify-center font-semibold">
              {{ version.stats.platformDownloads[platform]?.toLocaleString("en-US") }}
            </div>
            <div class="flex items-center justify-center gap-1 text-[0.65rem] text-gray">
              <PlatformLogo :platform="platform as Platform" :size="13" />
              <span class="truncate">{{ usePlatformName(platform as Platform) }}</span>
            </div>
          </div>
        </div>
      </Card>

      <Card class="!p-0 overflow-hidden">
        <template #header>
          <div class="flex items-center gap-2 px-4 pt-3.5 pb-1">
            <h2>Platforms</h2>
          </div>
        </template>
        <div class="flex flex-col gap-2 px-3 pt-1 pb-3">
          <div
            v-for="platform in versionPlatforms"
            :key="platform"
            class="flex min-w-0 items-center gap-3 rounded-lg border border-gray-200 bg-gray-100/60 px-3 py-2.5 dark:border-gray-800 dark:bg-charcoal-500/60"
          >
            <PlatformLogo :platform="platform" :size="22" class="flex-shrink-0" />
            <div class="min-w-0 flex-grow">
              <div class="font-semibold">{{ usePlatformName(platform) }}</div>
              <div class="truncate text-xs text-gray" :title="version.platformDependenciesFormatted[platform]?.join(', ')">
                {{ version.platformDependenciesFormatted[platform]?.join(", ") }}
              </div>
            </div>
            <PlatformVersionEditModal
              v-if="project && hasPerms(NamedPermission.EditVersion)"
              :project="project"
              :version="version"
              :platform="usePlatformData(platform)!"
            />
          </div>
        </div>
      </Card>

      <Card v-if="hasPerms(NamedPermission.EditVersion) || platformsWithDependencies.length > 0" class="!p-0 overflow-hidden">
        <template #header>
          <div class="flex items-center gap-2 px-4 pt-3.5 pb-1">
            <h2>Dependencies</h2>
          </div>
        </template>
        <div class="flex flex-col gap-3 px-3 pt-1 pb-3">
          <div
            v-for="platform in platformsWithDependencies"
            :key="platform"
            class="rounded-lg border border-gray-200 bg-gray-100/60 dark:border-gray-800 dark:bg-charcoal-500/60"
          >
            <div class="flex items-center gap-2 px-3 py-2">
              <PlatformLogo :platform="platform" :size="20" />
              <span class="flex-grow font-semibold">{{ usePlatformName(platform) }}</span>
              <DependencyEditModal v-if="project" :project="project" :version="version" :platform="usePlatformData(platform)!" />
            </div>
            <ul v-if="sortedDependencies(platform).length > 0" class="border-t px-3 py-2 dark:border-gray-800">
              <li v-for="dep in sortedDependencies(platform)" :key="dep.name" class="flex items-center gap-2 py-1 text-sm">
                <IconMdiLinkVariant class="flex-shrink-0 text-gray" />
                <Link
                  :href="dep.externalUrl || '/api/internal/projects/project-redirect/' + dep.name"
                  :target="dep.externalUrl ? '_blank' : undefined"
                  class="min-w-0 flex-grow truncate font-semibold"
                >
                  {{ dep.name }}
                </Link>
                <span v-if="!dep.required" class="flex-shrink-0 text-xs text-gray">Optional</span>
              </li>
            </ul>
            <div v-else class="border-t px-3 py-3 text-sm text-gray dark:border-gray-800">No dependencies</div>
          </div>
        </div>
      </Card>

      <Card
        v-if="hasPerms(NamedPermission.DeleteVersion) || hasPerms(NamedPermission.ViewLogs) || hasPerms(NamedPermission.Reviewer)"
        class="!p-0 overflow-hidden"
      >
        <template #header>
          <div class="flex items-center gap-2 px-4 pt-3.5 pb-1">
            <h2>Management</h2>
          </div>
        </template>
        <div class="px-3 pt-1 pb-3">
          <div class="mb-3 flex items-center gap-2 rounded-lg bg-gray-100 px-3 py-2 text-sm dark:bg-charcoal-500">
            <IconMdiInformation class="text-gray" />
            Visibility: {{ visibilityLabel }}
          </div>
          <div class="grid grid-cols-1 gap-2">
            <Tooltip>
              <template #content>
                <span v-if="version.pinnedStatus === PinnedStatus.CHANNEL">This version is pinned through its channel</span>
                <span v-else-if="version.pinnedStatus === PinnedStatus.VERSION">Remove this pinned version from the project page</span>
                <span v-else>Pin this version to the project page</span>
              </template>
              <Button
                button-type="secondary"
                size="medium"
                class="w-full"
                :disabled="version.pinnedStatus === PinnedStatus.CHANNEL"
                @click="setPinned(version.pinnedStatus === PinnedStatus.NONE)"
              >
                <IconMdiPinOff v-if="version.pinnedStatus !== PinnedStatus.NONE" class="mr-1" />
                <IconMdiPin v-else class="mr-1" />
                {{ version.pinnedStatus === PinnedStatus.NONE ? "Pin" : "Unpin" }}
              </Button>
            </Tooltip>
            <Button v-if="hasPerms(NamedPermission.ViewLogs)" button-type="secondary" size="medium" class="w-full" @click="router.push('/admin/log')">
              <IconMdiFileDocument class="mr-1" />
              User logs
            </Button>
            <template v-if="hasPerms(NamedPermission.Reviewer)">
              <Button button-type="secondary" size="medium" class="w-full" :to="route.path + '/reviews'">
                <IconMdiListStatus v-if="isReviewStateChecked || isUnderReview" class="mr-1" />
                <IconMdiPlay v-else class="mr-1" />
                {{ isReviewStateChecked || isUnderReview ? "Review logs" : "Review" }}
              </Button>
              <Button button-type="secondary" size="medium" class="w-full" :to="route.path + '/scan'">
                <IconMdiAlertDecagram class="mr-1" />
                Scans
              </Button>
              <Button v-if="version.visibility === Visibility.SoftDelete" size="medium" class="w-full" @click="restoreVersion"> Restore </Button>
            </template>
            <TextAreaModal
              v-if="hasPerms(NamedPermission.DeleteVersion) && version.visibility !== Visibility.SoftDelete"
              title="Delete version"
              label="Reason for deletion"
              description="This will delete the version and make its downloads unavailable."
              confirmation-text="Confirm"
              submit-label="Delete"
              :submit="deleteVersion"
              require-input
              destructive
            >
              <template #activator="{ on }">
                <Button button-type="secondary" size="medium" class="w-full !border-red-600 !bg-red-900/50 text-white hover:!bg-red-900/70" v-on="on">
                  Delete
                </Button>
              </template>
            </TextAreaModal>
            <TextAreaModal
              v-if="hasPerms(NamedPermission.HardDeleteVersion)"
              title="Permanently delete version"
              label="Reason for permanent deletion"
              description="This permanently removes the version and all of its files. This action is irreversible."
              confirmation-text="Confirm"
              submit-label="Delete forever"
              :submit="hardDeleteVersion"
              require-input
              destructive
            >
              <template #activator="{ on }">
                <Button button-type="secondary" size="medium" class="w-full !border-red-600 !bg-red-900/50 text-white hover:!bg-red-900/70" v-on="on">
                  Delete forever
                </Button>
              </template>
            </TextAreaModal>
          </div>
        </div>
      </Card>
    </aside>
  </div>
</template>
