<script lang="ts" setup>
import type { AxiosError } from "axios";
import { titleCase } from "scule";
import { ReviewState, PinnedStatus, NamedPermission, Visibility } from "~/types/backend";
import type { Platform, HangarProject, Version, User } from "~/types/backend";

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
    if (props.version.downloads[platform as Platform].externalUrl) {
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
    return [...props.version.pluginDependencies[platform]].sort((a, b) => Number(b.required) - Number(a.required));
  }
  return [];
}

const supportsString = computed(() => {
  const result = [];
  for (const platform in props.version?.platformDependenciesFormatted) {
    result.push(titleCase(platform.toLowerCase()) + " " + props.version?.platformDependenciesFormatted[platform].join(", "));
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
  <div v-if="version" class="flex lt-md:flex-col flex-wrap lg:flex-nowrap gap-4 firefox-hack">
    <section class="max-w-full basis-full lg:basis-11/15 overflow-clip">
      <div class="flex gap-2 justify-between">
        <div>
          <h2 class="text-3xl sm:inline-flex items-center gap-x-1">
            <Tag
              class="mr-1"
              :name="version.channel.name"
              :color="{ background: version.channel.color }"
              :short-form="true"
              :tooltip="version.channel.description"
            />
            {{ version.name }}
          </h2>
          <h3>
            <span class="inline-flex lt-md:flex-wrap">
              {{ i18n.t("version.page.subheader", [version.author, lastUpdated(new Date(version.createdAt)), project?.name, version.name]) }}
            </span>
          </h3>
        </div>
        <div class="inline-flex items-center flex-grow space-x-2">
          <div class="flex-grow" />
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
    <section class="basis-full lg:basis-4/15 flex-grow space-y-4">
      <Card v-if="hasPerms(NamedPermission.DeleteVersion) || hasPerms(NamedPermission.ViewLogs) || hasPerms(NamedPermission.Reviewer)">
        <template #header>
          <h2>{{ i18n.t("version.page.manage") }}</h2>
        </template>

        <span class="inline-flex items-center">
          <IconMdiInformation class="mr-1" />
          {{ i18n.t("version.page.visibility", [i18n.t(currentVisibility?.title || "")]) }}
        </span>

        <div class="flex gap-2 flex-wrap mt-2">
          <Tooltip>
            <template #content>
              <span v-if="version?.pinnedStatus === PinnedStatus.CHANNEL">{{ i18n.t("version.page.pinned.tooltip.channel") }}</span>
              <span v-else>{{ i18n.t(`version.page.pinned.tooltip.${version?.pinnedStatus?.toLowerCase()}`) }}</span>
            </template>
            <Button size="small" :disabled="version?.pinnedStatus === PinnedStatus.CHANNEL" @click="setPinned(version?.pinnedStatus === PinnedStatus.NONE)">
              <IconMdiPinOff v-if="version?.pinnedStatus !== PinnedStatus.NONE" class="mr-1" />
              <IconMdiPin v-else class="mr-1" />
              {{ i18n.t(`version.page.pinned.button.${version?.pinnedStatus?.toLowerCase()}`) }}
            </Button>
          </Tooltip>

          <!--todo route for user action log, with filtering-->
          <Button v-if="hasPerms(NamedPermission.ViewLogs)" @click="router.push('/admin/log')">
            <IconMdiFileDocument />
            {{ i18n.t("version.page.userAdminLogs") }}
          </Button>

          <template v-if="hasPerms(NamedPermission.Reviewer)">
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

          <Button v-if="hasPerms(NamedPermission.Reviewer) && version.visibility === Visibility.SoftDelete" @click="restoreVersion">
            {{ i18n.t("version.page.restore") }}
          </Button>
          <TextAreaModal
            v-if="hasPerms(NamedPermission.DeleteVersion) && version.visibility !== Visibility.SoftDelete"
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
            v-if="hasPerms(NamedPermission.HardDeleteVersion)"
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
            <h2 class="flex-grow">{{ i18n.t("project.info.title") }}</h2>
          </div>
        </template>

        <table class="w-full">
          <tbody>
            <tr>
              <th class="text-left">{{ i18n.t("project.info.publishDate") }}</th>
              <td v-if="version" class="text-right">{{ i18n.d(version.createdAt, "date") }}</td>
              <td v-else><Skeleton /></td>
            </tr>
            <tr>
              <th class="text-left">
                {{ i18n.t(hasPerms(NamedPermission.IsSubjectMember) ? "project.info.totalTotalDownloads" : "project.info.totalDownloads", 0) }}
              </th>
              <td class="text-right">
                {{ version.stats.totalDownloads.toLocaleString("en-US") }}
              </td>
            </tr>
            <!-- Only show per platform downloads to project members, otherwise not too relevant and only adding to height -->
            <tr v-for="platform in version && hasPerms(NamedPermission.IsSubjectMember) ? Object.keys(version.stats.platformDownloads) : []" :key="platform">
              <th class="text-left inline-flex">
                <PlatformLogo :platform="platform as Platform" :size="24" class="mr-1" />
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
            <h2 class="flex-grow">{{ i18n.t("version.page.platforms") }}</h2>
          </div>
        </template>

        <div v-for="platform in versionPlatforms" :key="platform" class="flex items-center mb-1">
          <PlatformLogo :platform="platform" :size="24" class="mr-1 flex-shrink-0" />
          {{ useBackendData.platforms.get(platform)?.name }}
          ({{ version?.platformDependenciesFormatted[platform].join(", ") }})
          <span class="flex-grow" />
          <PlatformVersionEditModal
            v-if="project && version && hasPerms(NamedPermission.EditVersion)"
            :project="project"
            :version="version"
            :platform="useBackendData.platforms.get(platform)!"
          />
        </div>
      </Card>

      <Card v-if="hasPerms(NamedPermission.EditVersion) || platformsWithDependencies.length > 0">
        <template #header>
          <div class="inline-flex w-full">
            <h2 class="flex-grow">{{ i18n.t("version.page.dependencies") }}</h2>
          </div>
        </template>

        <div v-for="platform in platformsWithDependencies" :key="platform" class="py-1">
          <Spoiler :with-line="version?.pluginDependencies[platform] !== undefined" always-open>
            <template #title>
              <div class="flex gap-1 w-full">
                <PlatformLogo :platform="platform" :size="24" class="flex-shrink-0" />
                {{ useBackendData.platforms.get(platform)?.name }}
                <span class="flex-grow" />
                <DependencyEditModal v-if="project && version" :project="project" :version="version" :platform="useBackendData.platforms.get(platform)!" />
              </div>
            </template>
            <template #content>
              <div>
                <ul>
                  <li v-for="dep in sortedDependencies(platform)" :key="dep.name">
                    <Link
                      :href="dep.externalUrl || '/api/internal/projects/project-redirect/' + dep.name"
                      :target="dep.externalUrl ? '_blank' : undefined"
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

<style lang="scss" scoped>
/* firefox doesn't seem to respect flex-basis properly, so we add a max width, but we need to subtract the gap... */
@media (min-width: 1024px) {
  .firefox-hack {
    > :first-child {
      flex-basis: 73.3333333333%;
      max-width: calc(73.3333333333% - 0.5rem);
    }

    > :last-child {
      flex-basis: 26.6666666667%;
      max-width: calc(26.6666666667% - 0.5rem);
    }
  }
}
</style>
