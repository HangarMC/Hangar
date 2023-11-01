<script setup lang="ts">
import { Menu, MenuButton, MenuItem, MenuItems } from "@headlessui/vue";
import { useI18n } from "vue-i18n";
import type { Ref } from "vue";
import { computed, isRef, ref, watch } from "vue";
import { useHead } from "@unhead/vue";
import { useRoute, useRouter } from "vue-router";
import type { PaginatedResult, Project } from "hangar-api";
import type { PlatformVersion } from "hangar-internal";
import { watchDebounced } from "@vueuse/core";
import InputCheckbox from "~/components/ui/InputCheckbox.vue";
import { useBackendData, useVisibleCategories, useVisiblePlatforms } from "~/store/backendData";
import ProjectList from "~/components/projects/ProjectList.vue";
import { useProjects } from "~/composables/useApiHelper";
import Card from "~/components/design/Card.vue";
import Container from "~/components/design/Container.vue";
import { useSeo } from "~/composables/useSeo";
import { useApi } from "~/composables/useApi";
import Alert from "~/components/design/Alert.vue";
import type { Platform } from "~/types/enums";
import { Tag } from "~/types/enums";
import InputRadio from "~/components/ui/InputRadio.vue";
import PlatformLogo from "~/components/logos/platforms/PlatformLogo.vue";
import CategoryLogo from "~/components/logos/categories/CategoryLogo.vue";
import { useConfig } from "~/composables/useConfig";
import VersionSelector from "~/components/VersionSelector.vue";

const i18n = useI18n();
const route = useRoute();
const router = useRouter();

const sorters = [
  { id: "-stars", label: i18n.t("project.sorting.mostStars") },
  { id: "-recent_downloads", label: i18n.t("project.sorting.recentDownloads") },
  { id: "-downloads", label: i18n.t("project.sorting.mostDownloads") },
  { id: "-updated", label: i18n.t("project.sorting.recentlyUpdated") },
  { id: "-newest", label: i18n.t("project.sorting.newest") },
];

const toArray = (input: unknown) => (Array.isArray(input) ? input : input ? [input] : []);
const filters = ref({
  versions: toArray(route.query.version),
  categories: toArray(route.query.category),
  platform: (route.query.platform || null) as string | null,
  tags: toArray(route.query.tag),
});

const activeSorter = ref<string>((route.query.sort as string) || "-stars");
const page = ref(route.query.page ? Number(route.query.page) : 0);
const query = ref<string>((route.query.q as string) || "");
const loggedOut = ref<boolean>("loggedOut" in route.query);
const projects = ref<PaginatedResult<Project> | null>();

const requestParams = computed(() => {
  const limit = 10;
  const params: Record<string, any> = {
    limit,
    offset: page.value * limit,
    version: filters.value.versions,
    category: filters.value.categories,
    platform: filters.value.platform !== null ? [filters.value.platform] : [],
    tag: filters.value.tags,
  };
  if (query.value) {
    params.q = query.value;
  }
  if (activeSorter.value) {
    params.sort = activeSorter.value;
  }

  return params;
});
const p = await useProjects(requestParams.value);
if (p && p.value) {
  projects.value = p.value;
  await checkOffsetLargerCount();
}

const projectList = ref();
function resetPage() {
  projectList.value.updatePage(0);
}

watch(filters, resetPage, { deep: true });
watch(query, resetPage);
watch(activeSorter, resetPage);
watchDebounced(
  requestParams,
  async () => {
    // dont want limit in url, its hardcoded in frontend
    // offset we dont want, we set page instead
    const { limit, offset, ...paramsWithoutLimit } = requestParams.value;
    // set the request params
    await router.replace({ query: { page: page.value, ...paramsWithoutLimit } });
    // do the update
    return updateProjects();
  },
  { deep: true, debounce: 250 }
);

async function updateProjects() {
  projects.value = await useApi<PaginatedResult<Project>>("projects", "get", requestParams.value);
  await checkOffsetLargerCount();
}

// if somebody set page too high, lets reset it back
async function checkOffsetLargerCount() {
  if (projects.value && projects.value.pagination?.offset !== 0 && projects.value.pagination?.offset > projects.value.pagination?.count) {
    page.value = 0;
    await updateProjects();
  }
}

function versions(platform: Platform): PlatformVersion[] {
  const platformData = useBackendData.platforms?.get(platform);
  if (!platformData) {
    return [];
  }

  return platformData.possibleVersions;
}

function updatePlatform(platform: any) {
  filters.value.platform = platform;

  const allowedVersion: PlatformVersion[] = versions(platform);
  filters.value.versions = filters.value.versions.filter((existingVersion) => {
    return allowedVersion.find((allowedNewVersion) => allowedNewVersion === existingVersion);
  });
}

const meta = useSeo("Home", null, route, null);
const config = useConfig();
const script = {
  type: "application/ld+json",
  children: JSON.stringify({
    "@context": "https://schema.org",
    "@type": "WebSite",
    url: config.publicHost,
    potentialAction: {
      "@type": "SearchAction",
      target: config.publicHost + "/?q={search_term_string}",
      "query-input": "required name=search_term_string",
    },
  }),
};
if (isRef(meta.script)) {
  meta.script.value.push(script);
} else {
  meta.script = (meta.script || []) as [];
  meta.script.push(script);
}

const pageChangeScrollAnchor: Ref<Element | null> = ref(null);

useHead(meta);
</script>

<template>
  <div>
    <Container class="flex flex-col items-center gap-4">
      <h1 ref="pageChangeScrollAnchor" class="text-3xl font-bold uppercase text-center mt-4">{{ i18n.t("hangar.projectSearch.title") }}</h1>
      <h2 class="text-1xl text-center mb-2">{{ i18n.t("hangar.projectSearch.subTitle") }}</h2>
      <!-- Search Bar -->
      <div class="relative rounded-md flex shadow-md w-full max-w-screen-md">
        <!-- Text Input -->
        <input
          v-model="query"
          class="rounded-l-md md:rounded-md p-4 basis-full min-w-0 dark:bg-gray-700"
          type="text"
          :placeholder="i18n.t('hangar.projectSearch.query', [projects?.pagination.count])"
        />
        <div class="md:hidden flex">
          <Menu>
            <MenuButton class="bg-gradient-to-r from-[#004ee9] to-[#367aff] rounded-r-md text-left font-semibold flex items-center gap-2 text-white p-2">
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
                    :class="{ 'bg-gray-100 dark:bg-gray-700': active, 'bg-gradient-to-r from-[#004ee9] to-[#367aff] text-white': activeSorter === sorter.id }"
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
            :class="{ 'bg-gradient-to-r from-[#004ee9] to-[#367aff] text-white': activeSorter === sorter.id }"
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
      <div v-if="projects" class="w-full min-w-0 mb-5 flex flex-col gap-2 lg:mb-0">
        <ProjectList ref="projectList" :projects="projects" :reset-anchor="pageChangeScrollAnchor" @update:page="(newPage) => (page = newPage)" />
      </div>
      <!-- Sidebar -->
      <Card accent class="min-w-300px flex flex-col gap-4">
        <div class="platforms">
          <h4 class="font-bold mb-1">
            {{ i18n.t("hangar.projectSearch.platforms") }}
            <span
              v-if="filters.platform"
              class="font-normal text-sm hover:(underline) text-gray-600 dark:text-gray-400"
              cursor="pointer"
              @click="filters.platform = null"
            >
              {{ i18n.t("hangar.projectSearch.clear") }}
            </span>
          </h4>
          <div class="flex flex-col gap-1">
            <ul>
              <li v-for="platform in useVisiblePlatforms" :key="platform.enumName" class="inline-flex w-full">
                <InputRadio :label="platform.name" :model-value="filters.platform" :value="platform.enumName" @update:model-value="updatePlatform">
                  <PlatformLogo :platform="platform.enumName" :size="24" class="mr-1" />
                </InputRadio>
              </li>
            </ul>
          </div>
        </div>
        <div v-if="filters.platform" class="versions">
          <h4 class="font-bold mb-1">{{ i18n.t("hangar.projectSearch.versions." + filters.platform) }}</h4>
          <div class="max-h-40 overflow-auto">
            <VersionSelector v-model="filters.versions" :versions="versions(filters.platform)" :open="false" col />
          </div>
        </div>
        <div class="tags">
          <h4 class="font-bold mb-1">{{ i18n.t("hangar.projectSearch.tags") }}</h4>
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
          <h4 class="font-bold mb-1">{{ i18n.t("hangar.projectSearch.categories") }}</h4>
          <div class="flex flex-col gap-1">
            <InputCheckbox
              v-for="category in useVisibleCategories"
              :key="category.apiName"
              v-model="filters.categories"
              :value="category.apiName"
              :label="i18n.t(category.title)"
            >
              <CategoryLogo :category="category.apiName" :size="22" class="mr-1" />
            </InputCheckbox>
          </div>
        </div>
      </Card>
    </Container>
  </div>
</template>
