<script lang="ts" setup>
import { Menu, MenuButton, MenuItem, MenuItems } from "@headlessui/vue";
import type { Category, PlatformVersion } from "~/types/backend";
import { Platform, Tag } from "~/types/backend";
import CollapsibleCard from "~/components/design/CollapsibleCard.vue";

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

const toArray = (input: (string | null)[] | string | null): string[] => (Array.isArray(input) ? (input as string[]) : (input ? [input!] : []));
const showAllVersions = ref(false);
const filters = ref({
  versions: toArray(route.query.version),
  categories: toArray(route.query.category),
  platform: (route.query.platform || undefined) as Platform | undefined,
  tags: toArray(route.query.tag),
});

if (props.platform) {
  filters.value.platform = props.platform;
}

const activeSorter = ref<string>((route.query.sort as string) || "-stars");
const page = ref(route.query.page ? Number(route.query.page) : 0);
const query = ref<string>((route.query.query as string) || "");

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
    params.limit = 20;
  }

  return params;
});
const { projects, refreshProjects } = useProjects(() => requestParams.value, router);

// if somebody set page too high, lets reset it back
watch(projects, () => {
  if (projects.value && projects.value.pagination?.offset !== 0 && projects.value.pagination?.offset > projects.value.pagination?.count) {
    page.value = 0;
  }
});
// for some reason the watch in useProjects doesn't work for filters 🤷‍♂️
watch(
  () => filters.value.versions,
  () => refreshProjects(),
  { deep: true }
);

function versions(platform: Platform): PlatformVersion[] {
  const platformData = useBackendData.platforms?.get(platform);
  if (!platformData) {
    return [];
  }

  return platformData.platformVersions;
}

function updatePlatform(platform: any) {
  filters.value.platform = platform;

  const allowedVersions = versions(platform);
  filters.value.versions = filters.value.versions.filter((existingVersion) =>
    allowedVersions.some((allowedVersion) => allowedVersion.version === existingVersion)
  );
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
        children: JSON.stringify({
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

const versionSearch = ref('');

const categorySearch = ref('');
const filteredCategories = computed(() => {
  return useVisibleCategories.value.filter((category) =>
    category.title.toLowerCase().includes(categorySearch.value.toLowerCase())
  );
});
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
        <input
          v-model="query"
          name="query"
          class="rounded-l-md md:rounded-md p-4 basis-full min-w-0 dark:bg-gray-700"
          type="text"
          :placeholder="i18n.t('hangar.projectSearch.query', [projects?.pagination.count])"
          v-on="useTracking('homepage-search', { platformName })"
        />
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
    <Container lg="flex items-start gap-4">
      <!-- Projects -->
      <div class="w-full min-w-0 mb-5 flex flex-col gap-4 lg:mb-0">
        <div class="flex justify-between">
          <h2 class="font-bold text-3xl">Projects</h2>
          <DropdownButton :button-arrow="true" button-size="medium" button-type="secondary">
            <template #button-label>
              <span class="font-medium ml-2">{{ i18n.t("hangar.projectSearch.sortBy") }}</span>:&nbsp;<span>{{ sorters.find(s => s.id === activeSorter)!.label }}</span>
            </template>
            <template #default="{ close }">
              <div class="w-max flex flex-col gap-1 max-h-lg max-w-lg overflow-x-auto py-1.5">
                <!-- eslint-disable vue/no-v-html -->
                <a
                  v-for="sorter in sorters"
                  :key="sorter.id"
                  class="mx-2 px-4 py-1.5 font-semibold hover:bg-gray-100 hover:dark:bg-gray-700 rounded-full cursor-pointer decoration-none transition-all duration-250 hover:scale-[1.01]"
                  @click="() => { activeSorter = sorter.id; close(); }"
                  v-html="sorter.label"
                />
                <!-- eslint-enable vue/no-v-html -->
              </div>
            </template>
          </DropdownButton>
        </div>
        <ProjectList :projects="projects" :loading="!projects" :reset-anchor="pageChangeScrollAnchor" @update:page="(newPage) => (page = newPage)" />
      </div>
      <!-- Sidebar -->
      <div class="flex flex-col gap-4">

        <!-- Platform Filter -->
        <CollapsibleCard class="min-w-300px flex flex-col gap-1">
          <template #title>
            {{ i18n.t('hangar.projectSearch.platforms') }}
            <Transition name="collapse">
              <div v-if="!platform" class="flex items-center justify-between w-full h-full">
                <span />
                <Tooltip>
                  <button
                    v-if="filters.platform"
                    class="flex items-center rounded-full border border-transparent p-1 transition-all duration-250
                            hover:bg-red-900 hover:scale-[1.015]"
                    cursor="pointer"
                    @click="() => {
                      filters.platform = undefined;
                      filters.versions = [];
                    }">
                    <IconMdiBroom class="text-sm" />
                  </button>
                  <template #content>
                    {{ i18n.t('hangar.projectSearch.clear') }}
                  </template>
                </Tooltip>
              </div>
            </Transition>
          </template>
          <div class="flex flex-col">
            <ul>
              <li v-for="visiblePlatform in useVisiblePlatforms" :key="visiblePlatform.enumName" class="inline-flex w-full mt-1">
                <InputRadio
                  :label="visiblePlatform.name"
                  :model-value="filters.platform"
                  :value="visiblePlatform.enumName"
                  @update:model-value="updatePlatform"
                >
                  <PlatformLogo :platform="visiblePlatform.enumName" :size="20" class="ml-3 mr-1" />
                </InputRadio>
              </li>
            </ul>
          </div>
        </CollapsibleCard>

        <!-- Version Filter -->
        <Transition name="collapse">
          <CollapsibleCard v-if="filters.platform" class="min-w-300px flex flex-col gap-1" :title="i18n.t('hangar.projectSearch.versions.' + filters.platform)">
            <template #title>
              <span  class="text-nowrap">{{ i18n.t("hangar.projectSearch.versions." + filters.platform) }}</span>
              <Transition name="collapse">
                <div v-if="filters.versions.length > 0" class="flex items-center justify-between w-full h-full">
                  <span />
                  <Tooltip>
                    <button
                      v-if="filters.versions"
                      class="flex items-center rounded-full border border-transparent py-1 px-1 transition-all duration-250
                            hover:bg-red-900 hover:scale-[1.015]"
                      cursor="pointer"
                      @click="() => {
                        filters.versions = [];
                        versionSearch = '';
                      }"
                    >
                      <IconMdiBroom class="text-sm" />
                    </button>
                    <template #content>
                      {{ i18n.t('hangar.projectSearch.clear') }}
                    </template>
                  </Tooltip>
                </div>
              </Transition>
            </template>
            <!-- Version Search -->
            <div class="relative hover:scale-[1.015] transition-all duration-250">
              <input
                v-model="versionSearch"
                name="versionSearch"
                class="rounded-full px-9 py-2 w-full dark:bg-gray-700 my-1"
                type="text"
                :placeholder="i18n.t('hangar.projectSearch.searchVersion')"
              />
              <IconMdiMagnify class="absolute top-3.75 left-3 text-gray-500" />
              <button v-if="versionSearch.length > 0" class="transition-all duration-250" @click="versionSearch = ''">
                <IconMdiClose class="absolute top-3.75 right-3 text-gray-500 hover:text-white" />
              </button>
            </div>
            <div class="relative">
              <div class="h-40 -px-1 overflow-y-auto overflow-x-hidden">
                <VersionSelector v-model="filters.versions" :version-search-query="versionSearch" :show-all-versions="showAllVersions" :versions="versions(filters.platform)" :open="false" col />
              </div>
              <!-- Gradient Overlay -->
              <div class="absolute inset-x-0 bottom-0 w-full h-8
                bg-gradient-to-b from-transparent to-charcoal-600
                pointer-events-none" />
              <div class="absolute inset-x-0 top-0 w-full h-3
                bg-gradient-to-b to-transparent from-charcoal-600
                pointer-events-none" />
            </div>
            <Transition name="collapse">
              <div v-if="filters.platform === Platform.PAPER" class="mt-2 pt-2 border-t border-gray-800">
                <InputCheckbox v-model:model-value="showAllVersions">
                  <span class="ml-4">{{ i18n.t("hangar.projectSearch.showAllVersions") }}</span>
                </InputCheckbox>
              </div>
            </Transition>
          </CollapsibleCard>
        </Transition>

        <!-- Tags Filter -->
        <CollapsibleCard class="min-w-300px flex flex-col gap-1">
          <template #title>
            {{ i18n.t('hangar.projectSearch.tags') }}
            <Transition name="collapse">
              <div v-if="filters.tags.length > 0" class="flex items-center justify-between w-full h-full">
                <span />
                <Tooltip>
                  <button
                    v-if="filters.tags"
                    class="flex items-center rounded-full border border-transparent py-1 px-1 transition-all duration-250
                            hover:bg-red-900 hover:scale-[1.015]"
                    cursor="pointer"
                    @click="filters.tags = []"
                  >
                    <IconMdiBroom class="text-sm" />
                  </button>
                  <template #content>
                    {{ i18n.t('hangar.projectSearch.clear') }}
                  </template>
                </Tooltip>
              </div>
            </Transition>
          </template>
          <div class="flex flex-col gap-1 mt-1">
            <InputCheckbox v-for="tag in Object.values(Tag)" :key="tag" v-model="filters.tags" :value="tag">
              <template #label>
                <IconMdiPuzzleOutline v-if="tag === Tag.ADDON" class="ml-3 mr-1"/>
                <IconMdiBookshelf v-else-if="tag === Tag.LIBRARY" class="ml-3 mr-1"/>
                <IconMdiLeaf v-else-if="tag === Tag.SUPPORTS_FOLIA" class="ml-3 mr-1"/>
                <span class="ml-1">{{ i18n.t("project.settings.tags." + tag + ".title") }}</span>
              </template>
            </InputCheckbox>
          </div>
        </CollapsibleCard>

        <!-- Categories Filter -->
        <CollapsibleCard class="min-w-300px flex flex-col gap-1">
          <template #title>
            {{ i18n.t("hangar.projectSearch.categories") }}
            <Transition name="collapse">
              <div v-if="filters.categories.length > 0" class="flex items-center justify-between w-full h-full">
                <span />
                <Tooltip>
                  <button
                    v-if="filters.tags"
                    class="text-sm flex items-center rounded-full border border-transparent p-1 transition-all duration-250
                            hover:bg-red-900 hover:scale-[1.015]"
                    cursor="pointer"
                    @click="filters.categories = []"
                  >
                    <IconMdiBroom class="text-sm" />
                  </button>
                  <template #content>
                    {{ i18n.t('hangar.projectSearch.clear') }}
                  </template>
                </Tooltip>
              </div>
            </Transition>
          </template>
          <!-- Category Search -->
          <div class="relative hover:scale-[1.015] transition-all duration-250">
            <input
              v-model="categorySearch"
              name="categorySearch"
              class="rounded-full px-9 py-2 w-full dark:bg-gray-700 my-1"
              type="text"
              :placeholder="i18n.t('hangar.projectSearch.searchCategory')"
            />
            <IconMdiMagnify class="absolute top-3.75 left-3 text-gray-500" />
            <button v-if="categorySearch.length > 0" class="transition-all duration-250" @click="categorySearch = ''">
              <IconMdiClose class="absolute top-3.75 right-3 text-gray-500 hover:text-white" />
            </button>
          </div>
          <div class="relative h-60 flex flex-col">
            <template v-if="filteredCategories.length === 0">
              <span class="text-center text-gray-400 my-auto">{{ i18n.t("hangar.projectSearch.noCategories") }}</span>
            </template>
            <div v-else class="flex flex-col gap-1 mt-1 h-60 -px-1 overflow-y-auto overflow-x-hidden pt-2 pb-3">
              <template v-for="category in filteredCategories" :key="category.apiName">
                <div class="mr-4 ml-1">
                  <InputCheckbox
                    v-model="filters.categories"
                    :value="category.apiName"
                    :label="i18n.t(category.title)"
                  >
                    <CategoryLogo :category="category.apiName as Category" :size="22" class="ml-3 mr-1" />
                  </InputCheckbox>
                </div>
              </template>

            </div>
            <!-- Gradient Overlay -->
            <div class="absolute inset-x-0 bottom-0 w-full h-8
                bg-gradient-to-b from-transparent to-charcoal-600
                pointer-events-none" />
            <div class="absolute inset-x-0 top-0 w-full h-3
                bg-gradient-to-b to-transparent from-charcoal-600
                pointer-events-none" />
          </div>

        </CollapsibleCard>
      </div>
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
