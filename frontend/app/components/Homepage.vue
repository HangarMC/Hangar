<script lang="ts" setup>
import { Menu, MenuButton, MenuItem, MenuItems } from "@headlessui/vue";
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

function updatePlatform(platform: any) {
  filters.value.platform = platform;

  const allowedVersion = usePlatformVersions(platform);
  filters.value.versions = filters.value.versions.filter((existingVersion) => {
    return allowedVersion.find((allowedNewVersion) => allowedNewVersion.version === existingVersion);
  });
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
        <input
          v-model="query"
          name="query"
          class="rounded-l-md md:rounded-md p-4 basis-full min-w-0 dark:bg-gray-700"
          type="text"
          :placeholder="i18n.t('hangar.projectSearch.query', [projects?.pagination.count])"
          v-on="useTracking('homepage-search', { platformName })"
        />
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
    <Container lg="flex items-start gap-6">
      <!-- Projects -->
      <div class="w-full min-w-0 mb-5 flex flex-col gap-2 lg:mb-0">
        <h2 class="font-bold text-2xl lg:(absolute -mt-11)">Projects</h2>
        <ProjectList :projects="projects" :loading="!projects" :reset-anchor="pageChangeScrollAnchor" @update:page="(newPage: number) => (page = newPage)" />
      </div>
      <!-- Sidebar -->
      <Card accent class="min-w-300px flex flex-col gap-4">
        <h2 class="font-bold text-xl -mb-2">Filters</h2>
        <div v-if="!platform" class="platforms">
          <h3 class="font-bold mb-1">
            {{ i18n.t("hangar.projectSearch.platforms") }}
            <span
              v-if="filters.platform"
              class="font-normal text-sm hover:(underline) text-gray-600 dark:text-gray-400"
              cursor="pointer"
              @click="filters.platform = undefined"
            >
              {{ i18n.t("hangar.projectSearch.clear") }}
            </span>
          </h3>
          <div class="flex flex-col gap-1">
            <ul>
              <li v-for="visiblePlatform in useVisiblePlatforms" :key="visiblePlatform.enumName" class="inline-flex w-full">
                <InputRadio
                  :label="visiblePlatform.name"
                  :model-value="filters.platform"
                  :value="visiblePlatform.enumName"
                  @update:model-value="updatePlatform"
                >
                  <PlatformLogo :platform="visiblePlatform.enumName" :size="24" class="mr-1" />
                </InputRadio>
              </li>
            </ul>
          </div>
        </div>
        <div v-if="filters.platform" class="versions">
          <h3 class="font-bold mb-1">{{ i18n.t("hangar.projectSearch.versions." + filters.platform) }}</h3>
          <div class="max-h-40 overflow-auto">
            <VersionSelector v-model="filters.versions" :versions="usePlatformVersions(filters.platform)" :open="false" col />
          </div>
        </div>
        <div class="tags">
          <h3 class="font-bold mb-1">{{ i18n.t("hangar.projectSearch.tags") }}</h3>
          <div class="flex flex-col gap-1">
            <InputCheckbox v-for="tag in Object.values(Tag)" :key="tag" v-model="filters.tags" :value="tag">
              <template #label>
                <IconMdiPuzzleOutline v-if="tag === Tag.ADDON" />
                <IconMdiBookshelf v-else-if="tag === Tag.LIBRARY" />
                <IconMdiLeaf v-else-if="tag === Tag.SUPPORTS_FOLIA" />
                <span class="ml-1">{{ i18n.t("project.settings.tags." + tag + ".title") }}</span>
              </template>
            </InputCheckbox>
          </div>
        </div>
        <div class="categories">
          <h3 class="font-bold mb-1">{{ i18n.t("hangar.projectSearch.categories") }}</h3>
          <div class="flex flex-col gap-1">
            <InputCheckbox
              v-for="category in useVisibleCategories"
              :key="category.apiName"
              v-model="filters.categories"
              :value="category.apiName"
              :label="i18n.t(category.title)"
            >
              <CategoryLogo :category="category.apiName as Category" :size="22" class="mr-1" />
            </InputCheckbox>
          </div>
        </div>
      </Card>
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
