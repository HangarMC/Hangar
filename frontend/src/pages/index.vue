<script setup lang="ts">
import { Menu, MenuButton, MenuItem, MenuItems } from "@headlessui/vue";
import { useI18n } from "vue-i18n";
import InputCheckbox from "~/lib/components/ui/InputCheckbox.vue";
import { useBackendDataStore } from "~/store/backendData";
import ProjectList from "~/components/projects/ProjectList.vue";
import { useProjects } from "~/composables/useApiHelper";
import { handleRequestError } from "~/composables/useErrorHandling";
import { useContext } from "vite-ssr/vue";
import Card from "~/lib/components/design/Card.vue";
import Container from "~/lib/components/design/Container.vue";
import { computed, isRef, ref, watch } from "vue";
import { useSeo } from "~/composables/useSeo";
import { useHead } from "@vueuse/head";
import { useRoute, useRouter } from "vue-router";
import { useApi } from "~/composables/useApi";
import { PaginatedResult, Project } from "hangar-api";
import Alert from "~/lib/components/design/Alert.vue";
import { Platform } from "~/types/enums";
import { toNumber } from "lodash-es";
import PlatformLogo from "~/components/logos/PlatformLogo.vue";
import InputRadio from "~/components/ui/InputRadio.vue";

const i18n = useI18n();
const route = useRoute();
const router = useRouter();
const ctx = useContext();

const backendData = useBackendDataStore();
const sorters = [
  { id: "-stars", label: i18n.t("project.sorting.mostStars") },
  { id: "-downloads", label: i18n.t("project.sorting.mostDownloads") },
  { id: "-newest", label: i18n.t("project.sorting.newest") },
  { id: "-updated", label: i18n.t("project.sorting.recentlyUpdated") },
  { id: "-recent_downloads", label: i18n.t("project.sorting.recentDownloads") },
];

const filters = ref({
  versions: [],
  categories: [],
  platform: null,
  licenses: [],
});

const query = ref<string>((route.query.q as string) || "");
const loggedOut = ref<boolean>("loggedOut" in route.query);
const projects = ref<PaginatedResult<Project> | null>();
const activeSorter = ref<string>("-updated");

const requestParams = computed(() => {
  const params: Record<string, any> = {
    limit: 25,
    offset: 0,
    version: filters.value.versions,
    category: filters.value.categories,
    platform: filters.value.platform !== null ? [filters.value.platform] : [],
    license: filters.value.licenses,
  };
  if (query.value) {
    params.q = query.value;
  }
  if (activeSorter.value) {
    params.sort = activeSorter.value;
  }
  return params;
});
const p = await useProjects(requestParams.value).catch((e) => handleRequestError(e, ctx, i18n));
if (p && p.value) {
  projects.value = p.value;
}

watch(filters, async () => updateProjects(), { deep: true });
watch(query, async () => updateProjects());
watch(activeSorter, async () => updateProjects());

async function updateProjects() {
  projects.value = await useApi<PaginatedResult<Project>>("projects", false, "get", requestParams.value);
}

function versions(platform: Platform) {
  return backendData.platforms
    ?.get(platform)
    ?.possibleVersions.sort((a, b) => toNumber(b.substring(2)) - toNumber(a.substring(2)))
    .map((k) => {
      return { version: k };
    });
}

const meta = useSeo("Home", null, route, null);
const script = {
  type: "application/ld+json",
  children: JSON.stringify({
    "@context": "https://schema.org",
    "@type": "WebSite",
    url: import.meta.env.HANGAR_PUBLIC_HOST,
    potentialAction: {
      "@type": "SearchAction",
      target: import.meta.env.HANGAR_PUBLIC_HOST + "/?q={search_term_string}",
      "query-input": "required name=search_term_string",
    },
  }),
};
if (isRef(meta.script)) {
  meta.script.value.push(script);
} else {
  meta.script = meta.script || [];
  meta.script.push(script);
}
useHead(meta);
</script>

<template>
  <Container class="flex flex-col items-center gap-4">
    <Alert v-if="loggedOut" type="success">{{ i18n.t("hangar.loggedOut") }}</Alert>
    <h2 class="text-3xl font-bold uppercase text-center my-4">{{ i18n.t("hangar.projectSearch.title") }}</h2>
    <!-- Search Bar -->
    <div class="relative rounded-md flex shadow-md w-full max-w-screen-md">
      <!-- Text Input -->
      <input
        v-model="query"
        class="rounded-l-md p-4 basis-full min-w-0"
        type="text"
        :placeholder="i18n.t('hangar.projectSearch.query', [projects?.pagination.count])"
      />
      <!-- Sorting Button -->
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
          <MenuItems class="absolute right-0 top-16 flex flex-col z-10 background-default filter drop-shadow-md rounded-md border-top-primary">
            <MenuItem v-for="sorter in sorters" :key="sorter.id" v-slot="{ active }">
              <button :class="{ 'bg-gradient-to-r from-[#004ee9] to-[#367aff] text-white': active }" class="p-2 text-left" @click="activeSorter = sorter.id">
                {{ sorter.label }}
              </button>
            </MenuItem>
          </MenuItems>
        </transition>
      </Menu>
    </div>
  </Container>
  <Container class="mt-5" lg="flex items-start gap-6">
    <!-- Projects -->
    <div class="w-full min-w-0 mb-5 flex flex-col gap-2" lg="mb-0">
      <ProjectList :projects="projects" />
    </div>
    <!-- Sidebar -->
    <Card accent class="min-w-300px flex flex-col gap-4">
      <div class="platforms">
        <h3 class="font-bold mb-1">{{ i18n.t("hangar.projectSearch.platforms") }}</h3>
        <div class="flex flex-col gap-1">
          <ul>
            <li v-for="platform in backendData.visiblePlatforms" :key="platform.enumName" class="inline-flex w-full">
              <InputRadio v-model="filters.platform" :value="platform.enumName" />
              <PlatformLogo :platform="platform.enumName" :size="24" class="mr-1" />
              {{ platform.name }}
            </li>
          </ul>
        </div>
      </div>
      <div v-if="filters.platform" class="versions">
        <h3 class="font-bold mb-1">{{ i18n.t("hangar.projectSearch.versions") }}</h3>
        <div class="flex flex-col gap-1 max-h-30 overflow-auto">
          <InputCheckbox
            v-for="version in versions(filters.platform)"
            :key="version.version"
            v-model="filters.versions"
            :value="version.version"
            :label="version.version"
          />
        </div>
      </div>
      <div class="categories">
        <h3 class="font-bold mb-1">{{ i18n.t("hangar.projectSearch.categories") }}</h3>
        <div class="flex flex-col gap-1">
          <InputCheckbox
            v-for="category in backendData.visibleCategories"
            :key="category.apiName"
            v-model="filters.categories"
            :value="category.apiName"
            :label="i18n.t(category.title)"
          />
        </div>
      </div>
      <div class="licenses">
        <h3 class="font-bold mb-1">{{ i18n.t("hangar.projectSearch.licenses") }}</h3>
        <div class="flex flex-col gap-1">
          <InputCheckbox v-for="license in backendData.licenses" :key="license" v-model="filters.licenses" :value="license" :label="license" />
        </div>
      </div>
    </Card>
  </Container>
</template>

<route lang="yaml">
meta:
  layout: wide
</route>
