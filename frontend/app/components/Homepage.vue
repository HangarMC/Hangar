<script lang="ts" setup>
import { Menu, MenuButton, MenuItem, MenuItems } from "@headlessui/vue";
import { isEqual } from "lodash-es";
import { Platform, Tag } from "#shared/types/backend";
import type { Category } from "#shared/types/backend";
import type { LocationQueryValue } from "#vue-router";

const props = defineProps<{
  platform?: Platform;
  platformName?: string;
  index?: boolean;
}>();

const i18n = useI18n();
const route = useRoute("index");
const router = useRouter();

const sorters = [
  { id: "-stars", label: i18n.t("project.sorting.mostStars") },
  { id: "-recent_downloads", label: i18n.t("project.sorting.recentDownloads") },
  { id: "-downloads", label: i18n.t("project.sorting.mostDownloads") },
  { id: "-updated", label: i18n.t("project.sorting.recentlyUpdated") },
  { id: "-newest", label: i18n.t("project.sorting.newest") },
];

const toArray = (input: LocationQueryValue | LocationQueryValue[] | undefined): string[] =>
  Array.isArray(input) ? (input as string[]) : input ? [input!] : [];

function filtersFromQuery() {
  return {
    versions: toArray(route.query.version),
    categories: toArray(route.query.category),
    // the request sends platform as a list, so it can come back off the url as a one-element array
    platform: props.platform ?? (toArray(route.query.platform)[0] as Platform | undefined),
    tags: toArray(route.query.tag),
  };
}
const sorterFromQuery = () => (route.query.sort as string) || "-stars";
const pageFromQuery = () => (route.query.page ? Number(route.query.page) : 0);
const queryFromQuery = () => (route.query.query as string) || "";

const filters = ref(filtersFromQuery());
const activeSorter = ref<string>(sorterFromQuery());
const page = ref(pageFromQuery());
const query = ref<string>(queryFromQuery());
const versionQuery = ref("");
const openFilterSections = reactive({
  platforms: true,
  tags: true,
  categories: true,
});
// Mobile only: the sidebar sits above the results, so it starts collapsed behind a "Filters" toggle.
const mobileFiltersOpen = ref(false);
const activeFilterCount = computed(
  () => (filters.value.platform ? 1 : 0) + filters.value.versions.length + filters.value.tags.length + filters.value.categories.length
);

const filteredPlatformVersions = computed(() => {
  if (!filters.value.platform) return [];
  const versions = usePlatformVersions(filters.value.platform);
  const search = versionQuery.value.trim().toLowerCase();
  if (!search) return versions;
  return versions.flatMap((version) => {
    if (version.version.toLowerCase().includes(search)) return [version];
    const subVersions = version.subVersions.filter((subVersion) => subVersion.toLowerCase().includes(search));
    return subVersions.length > 0 ? [{ ...version, subVersions }] : [];
  });
});

const requestParams = computed(() => {
  const limit = 10;
  const params: ReturnType<Parameters<typeof useProjects>[0]> = {
    limit,
    offset: page.value * limit,
    version: filters.value.versions,
    category: filters.value.categories,
    platform: filters.value.platform ? [filters.value.platform] : [],
    tag: filters.value.tags,
  };
  if (query.value) {
    params.query = query.value;
  }
  if (activeSorter.value) {
    params.sort = activeSorter.value;
  }

  return params;
});
const { projects } = useProjects(() => requestParams.value, router);

// if somebody set page too high, lets reset it back
watch(projects, () => {
  if (projects.value && projects.value.pagination?.offset !== 0 && projects.value.pagination?.offset > projects.value.pagination?.count) {
    page.value = 0;
  }
});
// the route component is reused across query changes, so a link back to "/" has to reset the state it holds
watch(
  () => route.query,
  () => {
    const incoming = filtersFromQuery();
    if (isEqual(incoming, filters.value) && sorterFromQuery() === activeSorter.value && pageFromQuery() === page.value && queryFromQuery() === query.value) {
      return;
    }
    filters.value = incoming;
    activeSorter.value = sorterFromQuery();
    page.value = pageFromQuery();
    query.value = queryFromQuery();
    versionQuery.value = "";
  }
);

function togglePlatform(platform: Platform) {
  updatePlatform(filters.value.platform === platform ? undefined : platform);
}

// Reassign, don't mutate: `requestParams` only reads `filters.value.<key>`, so an in-place
// push/splice leaves that property unchanged and the (non-deep) params watcher never refetches.
function toggleFilter(key: "tags" | "categories", value: string) {
  const list = filters.value[key];
  filters.value[key] = list.includes(value) ? list.filter((v) => v !== value) : [...list, value];
}

function updatePlatform(platform?: Platform) {
  filters.value.platform = platform;
  versionQuery.value = platform ? versionQuery.value : "";

  const allowed = platform ? usePlatformVersions(platform) : [];
  const kept = platform ? filters.value.versions.filter((v) => allowed.some((a) => a.version === v)) : [];
  if (kept.length !== filters.value.versions.length) {
    filters.value.versions = kept;
  }
}

const config = useRuntimeConfig();
const pageChangeScrollAnchor = useTemplateRef<HTMLElement>("pageChangeScrollAnchor");
const ssr = import.meta.server;

useSeo(
  computed(() => ({
    title: `Hangar - The best place to download ${props.index ? "Minecraft" : props.platformName} plugins`,
    description: `Hangar allows you to find and download the best ${props.index ? "Minecraft" : props.platformName} plugins for your Minecraft server`,
    route,
    additionalScripts: [
      {
        type: "application/ld+json",
        textContent: JSON.stringify({
          "@context": "https://schema.org",
          "@type": "WebSite",
          url: config.public.host,
          potentialAction: {
            "@type": "SearchAction",
            target: config.public.host + "/?q={search_term_string}",
            "query-input": "required name=search_term_string",
          },
        }),
        key: "website",
      },
    ],
    manualTitle: true,
  }))
);
</script>

<template>
  <div>
    <Container class="flex flex-col items-center gap-4">
      <template v-if="index">
        <h1 ref="pageChangeScrollAnchor" class="text-3xl font-bold uppercase text-center mt-4 flex flex-col w-full" data-allow-mismatch>
          <template v-if="ssr">
            Find your favorite <strong class="highlight bg-gradient-to-r from-primary-500 to-primary-400 text-transparent">Paper plugins</strong>
          </template>
          <template v-else>
            Find your favorite
            <div class="h-[36px] overflow-hidden relative">
              <span class="flex flex-col absolute w-full anim">
                <strong class="highlight bg-gradient-to-r from-primary-500 to-primary-400 text-transparent">Paper plugins</strong>
                <strong class="highlight bg-gradient-to-r from-primary-500 to-primary-400 text-transparent">Velocity plugins</strong>
                <strong class="highlight bg-gradient-to-r from-primary-500 to-primary-400 text-transparent">Waterfall plugins</strong>
              </span>
            </div>
          </template>
        </h1>
        <div class="text-1xl text-center mb-2">
          Hangar allows you to find and download the best Paper plugins, Velocity plugins or Waterfall plugins for your Minecraft server
        </div>
      </template>
      <template v-else>
        <h1 ref="pageChangeScrollAnchor" class="text-3xl font-bold uppercase text-center mt-4">
          Find your favorite
          <strong class="highlight bg-gradient-to-r from-primary-500 to-primary-400 text-transparent"> {{ platformName }} plugins </strong>
        </h1>
        <div class="text-1xl text-center mb-2">Hangar allows you to find and download the best {{ platformName }} plugins for your Minecraft server</div>
      </template>
      <div v-if="!index" class="text-center -mt-2">
        Looking for other platforms?
        <div class="flex gap-3 mt-2 mb-2">
          <Button v-if="platform != Platform.PAPER" to="/paper">
            Download Paper plugins <PlatformLogo :platform="Platform.PAPER" :size="24" class="ml-1" />
          </Button>
          <Button v-if="platform != Platform.VELOCITY" to="/velocity">
            Download Velocity Plugins <PlatformLogo :platform="Platform.VELOCITY" :size="24" class="ml-1" />
          </Button>
          <Button v-if="platform != Platform.WATERFALL" to="/waterfall">
            Download Waterfall plugins <PlatformLogo :platform="Platform.WATERFALL" :size="24" class="ml-1" />
          </Button>
        </div>
      </div>
      <!-- Search Bar -->
      <div class="relative rounded-md flex shadow-md w-full max-w-screen-md">
        <!-- Text Input -->
        <div class="relative flex basis-full items-center min-w-0">
          <IconMdiMagnify class="pointer-events-none absolute left-4 text-xl text-gray-500 dark:text-gray-400" />
          <input
            v-model="query"
            name="query"
            class="rounded-l-md md:rounded-md w-full p-4 pl-11 min-w-0 dark:bg-gray-700"
            type="text"
            autocomplete="off"
            :placeholder="i18n.t('hangar.projectSearch.query', [projects?.pagination.count])"
            v-on="useTracking('homepage-search', { platformName })"
          />
        </div>
        <div class="md:hidden flex">
          <Menu as="div">
            <MenuButton
              id="sort-button"
              class="bg-gradient-to-r from-primary-500 to-primary-400 rounded-r-md text-left font-semibold flex items-center gap-2 text-white p-2 h-full"
            >
              <span class="whitespace-nowrap">{{ i18n.t("hangar.projectSearch.sortBy") }}</span>
              <icon-mdi-sort-variant class="text-xl pointer-events-none" />
            </MenuButton>
            <transition
              enter-active-class="transition duration-100 ease-out"
              enter-from-class="transform scale-95 opacity-0"
              enter-to-class="transform scale-100 opacity-100"
              leave-active-class="transition duration-75 ease-out"
              leave-from-class="transform scale-100 opacity-100"
              leave-to-class="transform scale-95 opacity-0"
            >
              <MenuItems
                class="absolute right-0 top-15 flex flex-col z-10 background-default filter shadow-default drop-shadow-md rounded border-top-primary border-t-3"
              >
                <MenuItem v-for="sorter in sorters" :key="sorter.id" v-slot="{ active }">
                  <button
                    :class="{
                      'bg-gray-100 dark:bg-gray-700': active,
                      'bg-gradient-to-r from-primary-500 to-primary-400 text-white': activeSorter === sorter.id,
                    }"
                    class="px-4 py-2 text-left"
                    @click="activeSorter = sorter.id"
                  >
                    {{ sorter.label }}
                  </button>
                </MenuItem>
              </MenuItems>
            </transition>
          </Menu>
        </div>
      </div>
      <div class="justify-center inline-flex gap-1 lt-md:hidden">
        <div v-for="sorter in sorters" :key="sorter.id">
          <button
            :class="{ 'bg-gradient-to-r from-primary-500 to-primary-400 text-white': activeSorter === sorter.id }"
            class="rounded-lg py-2 px-4 hover:(bg-gray-300 dark:bg-gray-700)"
            @click="activeSorter = sorter.id"
          >
            {{ sorter.label }}
          </button>
        </div>
      </div>
    </Container>
    <Container class="flex flex-col items-stretch gap-4 lg:flex-row lg:items-start lg:gap-6">
      <!-- Projects -->
      <div class="w-full min-w-0 mb-5 flex flex-col gap-2 lg:mb-0 lg:order-first">
        <ProjectList :projects="projects" :loading="!projects" :reset-anchor="pageChangeScrollAnchor" @update:page="(newPage: number) => (page = newPage)" />
      </div>
      <!-- Sidebar -->
      <aside class="order-first flex flex-col gap-3 lg:order-last lg:min-w-300px lg:max-w-300px">
        <Button variant="outline" tone="neutral" class="lg:hidden" @click="mobileFiltersOpen = !mobileFiltersOpen">
          <IconMdiFilterVariant />
          {{ i18n.t("hangar.projectSearch.filters") }}
          <span v-if="activeFilterCount" class="rounded-full bg-primary-500 px-1.5 text-xs text-white">{{ activeFilterCount }}</span>
        </Button>

        <div :class="['flex flex-col gap-3', mobileFiltersOpen ? '' : 'lt-lg:hidden']">
          <Card class="!p-3">
            <button
              type="button"
              class="mb-1 flex w-full items-center gap-2 text-left font-bold text-lg"
              :aria-expanded="openFilterSections.platforms"
              @click="openFilterSections.platforms = !openFilterSections.platforms"
            >
              {{ platform ? i18n.t("hangar.projectSearch.versions." + filters.platform) : i18n.t("hangar.projectSearch.platforms") }}
              <IconMdiChevronDown
                class="ml-auto flex-shrink-0 text-gray-secondary transition-transform"
                :class="{ '-rotate-90': !openFilterSections.platforms }"
              />
            </button>

            <div v-show="openFilterSections.platforms">
              <div v-if="!platform" class="flex flex-col gap-0.5">
                <FilterOption
                  v-for="visiblePlatform in useVisiblePlatforms"
                  :key="visiblePlatform.enumName"
                  :label="visiblePlatform.name"
                  :selected="filters.platform === visiblePlatform.enumName"
                  @toggle="togglePlatform(visiblePlatform.enumName)"
                >
                  <PlatformLogo :platform="visiblePlatform.enumName" :size="18" class="flex-shrink-0" />
                </FilterOption>
              </div>

              <div v-if="filters.platform" :class="{ 'mt-4 border-t border-gray-300 pt-4 dark:border-gray-700': !platform }">
                <h4 v-if="!platform" class="mb-2 font-semibold">{{ i18n.t("hangar.projectSearch.versions." + filters.platform) }}</h4>
                <div class="relative mb-2">
                  <IconMdiMagnify class="pointer-events-none absolute left-2.5 top-2.5 text-gray-secondary" />
                  <input
                    v-model="versionQuery"
                    type="search"
                    class="w-full rounded-md background-card py-2 pl-8 pr-3 text-sm outline-none focus:(ring-2 ring-primary-500)"
                    :placeholder="i18n.t('hangar.projectSearch.searchVersions')"
                  />
                </div>
                <div class="max-h-52 overflow-auto pr-1">
                  <VersionSelector
                    v-if="filteredPlatformVersions.length > 0"
                    v-model="filters.versions"
                    :versions="filteredPlatformVersions"
                    expand="none"
                    col
                    compact
                  />
                  <p v-else class="py-2 text-sm text-gray-secondary">{{ i18n.t("hangar.projectSearch.noFilterResults") }}</p>
                </div>
              </div>
            </div>
          </Card>

          <Card class="!p-3">
            <button
              type="button"
              class="mb-1 flex w-full items-center gap-2 text-left font-bold text-lg"
              :aria-expanded="openFilterSections.tags"
              @click="openFilterSections.tags = !openFilterSections.tags"
            >
              {{ i18n.t("hangar.projectSearch.tags") }}
              <IconMdiChevronDown class="ml-auto flex-shrink-0 text-gray-secondary transition-transform" :class="{ '-rotate-90': !openFilterSections.tags }" />
            </button>
            <div v-show="openFilterSections.tags" class="flex flex-col gap-0.5">
              <FilterOption
                v-for="tag in Object.values(Tag)"
                :key="tag"
                :label="i18n.t('project.settings.tags.' + tag + '.title')"
                :selected="filters.tags.includes(tag)"
                @toggle="toggleFilter('tags', tag)"
              >
                <IconMdiPuzzleOutline v-if="tag === Tag.ADDON" class="flex-shrink-0" />
                <IconMdiBookshelf v-else-if="tag === Tag.LIBRARY" class="flex-shrink-0" />
                <IconMdiLeaf v-else-if="tag === Tag.SUPPORTS_FOLIA" class="flex-shrink-0" />
              </FilterOption>
            </div>
          </Card>

          <Card class="!p-3">
            <button
              type="button"
              class="mb-1 flex w-full items-center gap-2 text-left font-bold text-lg"
              :aria-expanded="openFilterSections.categories"
              @click="openFilterSections.categories = !openFilterSections.categories"
            >
              {{ i18n.t("hangar.projectSearch.categories") }}
              <IconMdiChevronDown
                class="ml-auto flex-shrink-0 text-gray-secondary transition-transform"
                :class="{ '-rotate-90': !openFilterSections.categories }"
              />
            </button>
            <div v-show="openFilterSections.categories">
              <div class="flex flex-col gap-0.5">
                <FilterOption
                  v-for="category in useVisibleCategories"
                  :key="category.apiName"
                  :label="i18n.t(category.title)"
                  :selected="filters.categories.includes(category.apiName)"
                  @toggle="toggleFilter('categories', category.apiName)"
                >
                  <CategoryLogo :category="category.apiName as Category" :size="18" class="flex-shrink-0" />
                </FilterOption>
              </div>
            </div>
          </Card>
        </div>
      </aside>
    </Container>
    <h2 class="text-2xl text-center font-bold mt-8">Frequently asked Questions about Hangar (FAQ)</h2>
    <div class="md:(ml-15 mr-15)">
      <Card class="mt-4" itemscope itemprop="mainEntity" itemtype="https://schema.org/Question">
        <h3 class="text-lg font-bold mb-1" itemprop="name">What is Hangar?</h3>
        <div itemscope itemprop="acceptedAnswer" itemtype="https://schema.org/Answer">
          <div itemprop="text">
            Hangar is the best place to download {{ platformName }} plugins. Created by the <Link href="https://papermc.io/team">PaperMC Team</Link>, we took
            great care that you can find the newest and best {{ platformName }} plugins.
          </div>
        </div>
      </Card>
      <Card class="mt-4" itemscope itemprop="mainEntity" itemtype="https://schema.org/Question">
        <h3 class="text-lg font-bold mb-1" itemprop="name">How do I download {{ platformName }} plugins from Hangar?</h3>
        <div itemscope itemprop="acceptedAnswer" itemtype="https://schema.org/Answer">
          <div itemprop="text">
            To download {{ platformName }} plugins, simply use the search on this page to find the plugin you are looking for and download the
            {{ platformName }} plugin from the resource page. The main download button will always provide the latest release version.
          </div>
        </div>
      </Card>
      <Card class="mt-4" itemscope itemprop="mainEntity" itemtype="https://schema.org/Question">
        <h3 class="text-lg font-bold mb-1" itemprop="name">Can I automate uploading {{ platformName }} plugins to Hangar?</h3>
        <div itemscope itemprop="acceptedAnswer" itemtype="https://schema.org/Answer">
          <div itemprop="text">Yes! Simply use the <Link href="https://github.com/HangarMC/hangar-publish-plugin">Hangar publish plugin for Gradle</Link>.</div>
        </div>
      </Card>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.anim {
  animation: anim 5s infinite;
}
.highlight {
  background-clip: text;
}

@keyframes anim {
  0%,
  30% {
    top: 0;
  }
  40%,
  60% {
    top: -36px;
  }
  70%,
  90% {
    top: -72px;
  }
  100% {
    top: 0;
  }
}
</style>
