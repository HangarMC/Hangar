<script lang="ts" setup>
import type { HangarProject, Platform, Version } from "#shared/types/backend";
import { NamedPermission, Visibility } from "#shared/types/backend";

const i18n = useI18n();
const router = useRouter();
const route = useRoute("user-project-versions");
const globalData = useGlobalData();

const toArray = <T,>(input: unknown): T => (Array.isArray(input) ? input : (input ? [input] : [])) as T;
const filter = reactive({
  channels: toArray<string[]>(route.query.channel),
  platforms: toArray<Platform[]>(route.query.platform),
});

const props = defineProps<{
  project?: HangarProject;
}>();

const pageChangeScrollAnchor = useTemplateRef("pageChangeScrollAnchor");
const page = ref(route.query.page ? Number(route.query.page) : 0);
const requestParams = computed(() => {
  const limit = 25;
  return {
    limit,
    offset: page.value * limit,
    channel: filter.channels,
    platform: filter.platforms,
  };
});

const { channels } = useProjectChannels(() => route.params.project);
const { versions, versionsStatus } = useProjectVersions(
  () => ({
    project: route.params.project,
    data: { ...requestParams.value, includeHiddenChannels: filter.channels?.length > 0 },
  }),
  router
);

useSeo(
  computed(() => ({
    title: "Versions | " + props.project?.name,
    route,
    description: `Download ${versions.value?.pagination?.count} ${props.project?.name} versions. ${props.project?.stats?.downloads} total downloads. Last updated on ${lastUpdated(new Date(versions.value?.result?.[0]?.createdAt || 0), i18n)}`,
    image: props.project?.avatarUrl,
  }))
);

const openFilterSections = reactive({ channels: true, platforms: true });

function toggleChannel(name: string) {
  filter.channels = filter.channels.includes(name) ? filter.channels.filter((c) => c !== name) : [...filter.channels, name];
}

function togglePlatformFilter(platform: Platform) {
  filter.platforms = filter.platforms.includes(platform) ? filter.platforms.filter((p) => p !== platform) : [...filter.platforms, platform];
}

function getRowClasses(version: Version): string {
  if (version.visibility === Visibility.SoftDelete) return "bg-red-500/5";
  return version.visibility === Visibility.Public ? "" : "bg-amber-500/5";
}

function getVisibilityTitle(visibility: Visibility) {
  const value = useBackendData.visibilities.find((v) => v.name === visibility);
  return value ? i18n.t(value.title) : undefined;
}
</script>

<template>
  <div ref="pageChangeScrollAnchor" class="flex flex-wrap md:flex-nowrap gap-4">
    <section class="basis-full md:basis-11/15 flex-grow">
      <template v-if="versionsStatus === 'loading' && !versions?.result?.length">
        <Skeleton class="mb-2 h-[52px]" delay />
        <Skeleton class="mb-2 h-[52px]" delay />
      </template>
      <Alert v-else-if="!versions?.result?.length" type="info"> {{ i18n.t("version.page.noVersions") }} </Alert>
      <Card v-else flat padding="none">
        <div
          class="version-columns hidden gap-x-3 border-b border-gray-300 px-4 py-2 text-xs text-gray-secondary font-semibold uppercase tracking-wide lg:grid dark:border-gray-700"
        >
          <span :aria-label="i18n.t('version.page.channel')" />
          <span class="min-w-0">{{ i18n.t("version.page.version") }}</span>
          <span class="min-w-0">{{ i18n.t("version.page.platforms") }}</span>
          <span class="min-w-0">{{ i18n.t("version.page.published") }}</span>
          <span class="min-w-0">{{ i18n.t("version.page.downloads") }}</span>
          <span />
        </div>
        <ul class="divide-y divide-gray-300 dark:divide-gray-700">
          <Pagination :items="versions.result" :server-pagination="versions.pagination" :reset-anchor="pageChangeScrollAnchor" @update:page="(p) => (page = p)">
            <template #pagination="{ page: current, pages, updatePage }">
              <li class="p-3">
                <PaginationButtons :page="current" :pages="pages" @update:page="updatePage" />
              </li>
            </template>
            <template #default="{ item }">
              <li
                class="version-columns relative flex flex-wrap items-center gap-x-3 gap-y-2 px-4 py-2.5 transition-colors hover:background-card lg:grid"
                :class="getRowClasses(item)"
              >
                <ChannelTile :channel="item.channel" />

                <div class="min-w-0 flex flex-1 items-center gap-2">
                  <h3 class="min-w-0 truncate font-semibold">
                    <NuxtLink
                      :to="`/${project?.namespace?.owner}/${project?.namespace?.slug}/versions/${item.name}`"
                      class="after:(absolute inset-0 content-empty)"
                    >
                      {{ item.name }}
                    </NuxtLink>
                  </h3>
                  <IconMdiCancel v-if="item.visibility === Visibility.SoftDelete" class="flex-shrink-0 text-red-500" />
                  <span v-else-if="item.visibility !== Visibility.Public" class="flex-shrink-0 inline-flex items-center gap-1 text-xs text-gray-secondary">
                    {{ getVisibilityTitle(item.visibility) }}
                    <IconMdiEyeOff />
                  </span>
                </div>

                <!-- below lg this drops under the title as a two-column block; on lg the wrappers
                     dissolve (display:contents) so the parts land in the row's own grid columns -->
                <div
                  class="order-2 basis-full grid grid-cols-2 gap-x-4 gap-y-1 border-t border-gray-300 pt-2 text-sm lg:(order-none basis-auto contents) dark:border-gray-700"
                >
                  <div class="min-w-0 flex flex-col gap-1 lg:(flex-row items-center gap-3 overflow-hidden)">
                    <span v-for="(v, p) in item?.platformDependenciesFormatted" :key="p" class="inline-flex items-center gap-1" :title="v.join(', ')">
                      <PlatformLogo :platform="p as unknown as Platform" :size="16" class="flex-shrink-0" />
                      <span class="tabular-nums">{{ collapseRanges(v) }}</span>
                    </span>
                  </div>

                  <div class="flex flex-col gap-1 lg:contents">
                    <span class="min-w-0 inline-flex items-center gap-1.5 truncate">
                      <IconMdiCalendar class="flex-shrink-0 lg:hidden" />
                      {{ i18n.d(item.createdAt, "date") }}
                    </span>
                    <span class="inline-flex flex-shrink-0 items-center gap-1.5">
                      <IconMdiDownload class="flex-shrink-0" />
                      <span class="tabular-nums">{{ item.stats.totalDownloads.toLocaleString("en-US") }}</span>
                    </span>
                  </div>
                </div>

                <DownloadButton
                  v-if="project"
                  :project="project"
                  :version="item"
                  small
                  :show-versions="false"
                  :show-single-platform="false"
                  class="relative z-1 order-1 flex-shrink-0 justify-end lg:order-none"
                />
              </li>
            </template>
          </Pagination>
        </ul>
      </Card>
    </section>

    <section class="basis-full md:basis-4/15 md:max-w-300px flex flex-col gap-3">
      <Button v-if="hasPerms(NamedPermission.CreateVersion)" :to="route.path + '/new'" class="w-full">
        <IconMdiPlus />
        {{ i18n.t("version.new.uploadNew") }}
      </Button>

      <Card class="!p-3">
        <div class="mb-1 flex items-center gap-2">
          <button
            type="button"
            class="flex-1 text-left font-bold text-lg"
            :aria-expanded="openFilterSections.channels"
            @click="openFilterSections.channels = !openFilterSections.channels"
          >
            {{ i18n.t("version.channels") }}
          </button>
          <Button
            v-if="project && hasPerms(NamedPermission.EditChannels)"
            :to="`/${project.namespace.owner}/${project.name}/settings/channels`"
            variant="ghost"
            tone="neutral"
            size="sm"
            icon-only
            :title="i18n.t('general.edit')"
            :aria-label="i18n.t('general.edit')"
          >
            <IconMdiPencil />
          </Button>
          <button
            type="button"
            class="flex-shrink-0 rounded p-0.5 text-gray-secondary hover:color-primary"
            :aria-label="i18n.t('version.channels')"
            :aria-expanded="openFilterSections.channels"
            @click="openFilterSections.channels = !openFilterSections.channels"
          >
            <IconMdiChevronDown class="transition-transform" :class="{ '-rotate-90': !openFilterSections.channels }" />
          </button>
        </div>
        <div v-show="openFilterSections.channels">
          <div class="flex flex-col gap-0.5">
            <FilterOption
              v-for="channel in channels"
              :key="channel.name"
              :label="channel.name"
              :selected="filter.channels.includes(channel.name)"
              @toggle="toggleChannel(channel.name)"
            >
              <span class="h-3 w-3 flex-shrink-0 rounded-sm" :style="{ backgroundColor: channel.color }" />
            </FilterOption>
          </div>
        </div>
      </Card>

      <Card class="!p-3">
        <button
          type="button"
          class="mb-1 flex w-full items-center gap-2 text-left font-bold text-lg"
          :aria-expanded="openFilterSections.platforms"
          @click="openFilterSections.platforms = !openFilterSections.platforms"
        >
          {{ i18n.t("version.platforms") }}
          <IconMdiChevronDown class="ml-auto flex-shrink-0 text-gray-secondary transition-transform" :class="{ '-rotate-90': !openFilterSections.platforms }" />
        </button>
        <div v-show="openFilterSections.platforms">
          <div class="flex flex-col gap-0.5">
            <FilterOption
              v-for="platform in globalData?.platforms"
              :key="platform.name"
              :label="platform.name"
              :selected="filter.platforms.includes(platform.enumName)"
              @toggle="togglePlatformFilter(platform.enumName)"
            >
              <PlatformLogo :platform="platform.enumName" :size="18" class="flex-shrink-0" />
            </FilterOption>
          </div>
        </div>
      </Card>
    </section>
  </div>
</template>

<style scoped>
/* one template shared by the header and every row, so the columns line up without hand-matched widths */
@media (min-width: 1024px) {
  .version-columns {
    grid-template-columns: 2rem minmax(0, 1fr) minmax(0, 1fr) minmax(0, 1fr) 4rem 4.5rem;
    align-items: center;
  }
}
</style>
