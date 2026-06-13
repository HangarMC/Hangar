<script lang="ts" setup>
import type { HangarProject, Platform, Version } from "#shared/types/backend";
import { NamedPermission, Visibility } from "#shared/types/backend";

const i18n = useI18n();
const router = useRouter();
const route = useRoute("user-project-versions");
const globalData = useGlobalData();

function toArray<T>(input: unknown): T {
  if (Array.isArray(input)) return input as T;
  return (input ? [input] : []) as T;
}
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
  const limit = 7;
  return {
    limit,
    offset: page.value * limit,
    channel: filter.channels.length ? filter.channels : undefined,
    platform: filter.platforms.length ? filter.platforms : undefined,
  };
});

const { channels } = useProjectChannels(() => route.params.project);
const { versions, versionsStatus } = useProjectVersions(
  () => ({
    project: route.params.project,
    data: { ...requestParams.value, includeHiddenChannels: true },
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

function getBorderClasses(version: Version): string {
  if (version.visibility === Visibility.SoftDelete) {
    return "!border-red-500 border-1px";
  }
  return version.visibility === Visibility.Public ? "!border-gray-200 !dark:border-gray-800 border-1px" : "";
}

function getVisibilityTitle(visibility: Visibility) {
  const value = useBackendData.visibilities.find((v) => v.name === visibility);
  return value ? i18n.t(value.title) : undefined;
}
</script>

<template>
  <div ref="pageChangeScrollAnchor" class="grid grid-cols-1 items-start gap-4 lg:grid-cols-[minmax(0,1fr)_280px] xl:grid-cols-[minmax(0,1fr)_300px]">
    <section class="min-w-0">
      <Card class="!p-0 overflow-hidden">
        <div
          class="hidden grid-cols-[minmax(180px,1fr)_minmax(180px,1fr)_140px_100px_56px] items-center gap-4 border-b px-4 py-3 text-xs font-semibold text-gray dark:border-gray-800 md:grid"
        >
          <span>Version</span>
          <span>{{ i18n.t("version.platforms") }}</span>
          <span>Date</span>
          <span>Downloads</span>
          <span aria-hidden="true" />
        </div>
        <ul>
          <template v-if="versionsStatus === 'loading'">
            <li class="border-b p-4 dark:border-gray-800"><Skeleton class="h-[68px]" delay /></li>
            <li class="p-4"><Skeleton class="h-[68px]" delay /></li>
          </template>
          <li v-else-if="!versions?.result?.length" class="px-4 py-10 text-center text-gray">
            {{ i18n.t("version.page.noVersions") }}
          </li>
          <Pagination
            v-else
            :items="versions.result"
            :server-pagination="versions.pagination"
            :reset-anchor="pageChangeScrollAnchor"
            @update:page="(p) => (page = p)"
          >
            <template #default="{ item }">
              <li
                :class="getBorderClasses(item)"
                class="group relative border-b border-x-0 border-t-0 transition-colors last:border-b-0 hover:background-card dark:border-gray-800"
              >
                <NuxtLink
                  :to="`/${project?.namespace?.owner}/${project?.namespace?.slug}/versions/${item.name}`"
                  class="absolute inset-0 z-0"
                  :aria-label="`View version ${item.name}`"
                />
                <div
                  class="pointer-events-none relative z-1 grid min-w-0 grid-cols-[minmax(0,1fr)_56px] items-center gap-x-3 gap-y-3 px-4 py-3 md:grid-cols-[minmax(180px,1fr)_minmax(180px,1fr)_140px_100px_56px] md:gap-4"
                >
                  <div class="min-w-0">
                    <div class="flex min-w-0 items-center gap-2">
                      <span
                        class="background-default inline-flex h-7 w-7 flex-shrink-0 items-center justify-center rounded-md border text-sm font-semibold uppercase"
                        :style="{ borderColor: item.channel.color, color: item.channel.color }"
                        :title="item.channel.name"
                      >
                        {{ item.channel.name.charAt(0) }}
                      </span>
                      <span class="min-w-0 truncate text-base font-bold leading-tight">{{ item.name }}</span>
                      <IconMdiCancel v-if="item.visibility === Visibility.SoftDelete" class="flex-shrink-0" />
                      <span v-else-if="item.visibility !== Visibility.Public" class="inline-flex flex-shrink-0 items-center text-xs text-gray">
                        {{ getVisibilityTitle(item.visibility) }}
                        <IconMdiEyeOff class="ml-1" />
                      </span>
                    </div>
                  </div>

                  <div class="col-span-2 flex min-w-0 flex-wrap gap-1.5 md:col-span-1">
                    <span v-for="(v, p) in item?.platformDependenciesFormatted" :key="p" class="inline-flex min-w-0 items-center gap-1.5 text-xs text-gray">
                      <PlatformLogo :platform="p as unknown as Platform" :size="15" class="flex-shrink-0" />
                      <span class="truncate">{{ v.join(", ") }}</span>
                    </span>
                  </div>

                  <span class="inline-flex items-center gap-1.5 text-xs text-gray">
                    <IconMdiCalendarOutline class="flex-shrink-0" />
                    {{ i18n.d(item.createdAt, "date") }}
                  </span>
                  <span class="inline-flex items-center gap-1.5 text-xs text-gray">
                    <IconMdiDownloadOutline class="flex-shrink-0" />
                    {{ item.stats.totalDownloads.toLocaleString("en-US") }}
                  </span>
                  <div class="pointer-events-auto col-start-2 row-start-1 flex justify-end md:col-start-auto md:row-start-auto">
                    <DownloadButton
                      v-if="project"
                      :project="project"
                      :version="item"
                      small
                      :show-versions="false"
                      :show-single-platform="false"
                      fixed-width
                      dropdown-placement="bottom-start"
                    />
                  </div>
                </div>
              </li>
            </template>
            <template #pagination="{ page: currentPage, updatePage, pages }">
              <li class="px-4 py-3">
                <PaginationButtons :page="currentPage" :pages="pages" @update:page="updatePage" />
              </li>
            </template>
          </Pagination>
        </ul>
      </Card>
    </section>

    <aside class="space-y-4 lg:sticky lg:top-4">
      <div v-if="hasPerms(NamedPermission.CreateVersion)">
        <NuxtLink :to="route.path + '/new'">
          <Button size="large" class="w-full">
            <IconMdiUpload class="mr-1" />
            {{ i18n.t("version.new.uploadNew") }}
          </Button>
        </NuxtLink>
      </div>

      <Card class="!p-0 overflow-hidden">
        <template #header>
          <div class="flex items-center gap-2 px-4 pt-3.5 pb-1">
            <h2 class="flex-grow">{{ i18n.t("version.channels") }}</h2>
            <Tooltip v-if="filter.channels.length">
              <button
                class="flex items-center rounded-full border border-transparent p-1 transition-all duration-250 hover:border-red-600 hover:bg-red-900/50"
                @click="filter.channels = []"
              >
                <IconMdiBroom class="text-sm" />
              </button>
              <template #content>{{ i18n.t("hangar.projectSearch.clear") }}</template>
            </Tooltip>
            <Link v-if="project && hasPerms(NamedPermission.EditChannels)" :to="`/${project.namespace.owner}/${project.name}/channels`">
              <Button size="small" class="text-sm"><IconMdiPencil /></Button>
            </Link>
          </div>
        </template>

        <ul class="space-y-1 px-2 pt-1 pb-2">
          <li v-for="channel in channels" :key="channel.name">
            <InputCheckbox v-model="filter.channels" :value="channel.name">
              <span class="ml-3 h-3 w-3 flex-shrink-0 rounded-full" :style="{ backgroundColor: channel.color }" />
              <span class="ml-2 min-w-0 truncate font-semibold">{{ channel.name }}</span>
            </InputCheckbox>
          </li>
        </ul>
      </Card>

      <Card class="!p-0 overflow-hidden">
        <template #header>
          <div class="flex items-center gap-2 px-4 pt-3.5 pb-1">
            <h2 class="flex-grow">{{ i18n.t("version.platforms") }}</h2>
            <Tooltip v-if="filter.platforms.length">
              <button
                class="flex items-center rounded-full border border-transparent p-1 transition-all duration-250 hover:border-red-600 hover:bg-red-900/50"
                @click="filter.platforms = []"
              >
                <IconMdiBroom class="text-sm" />
              </button>
              <template #content>{{ i18n.t("hangar.projectSearch.clear") }}</template>
            </Tooltip>
          </div>
        </template>

        <ul class="space-y-1 px-2 pt-1 pb-2">
          <li v-for="platform in globalData?.platforms" :key="platform.name">
            <InputCheckbox v-model="filter.platforms" :value="platform.enumName">
              <PlatformLogo :platform="platform.enumName" :size="21" class="ml-3 flex-shrink-0" />
              <span class="ml-2 min-w-0 truncate font-semibold">{{ platform.name }}</span>
            </InputCheckbox>
          </li>
        </ul>
      </Card>
    </aside>
  </div>
</template>
