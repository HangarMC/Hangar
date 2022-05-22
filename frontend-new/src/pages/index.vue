<script setup lang="ts">
import { Menu, MenuButton, MenuItem, MenuItems } from "@headlessui/vue";
import { useI18n } from "vue-i18n";
import InputCheckbox from "~/components/ui/InputCheckbox.vue";
import { useBackendDataStore } from "~/store/backendData";
import ProjectList from "~/components/projects/ProjectList.vue";
import { useProjects } from "~/composables/useApiHelper";
import { handleRequestError } from "~/composables/useErrorHandling";
import { useContext } from "vite-ssr/vue";
import Card from "~/components/design/Card.vue";
import Container from "~/components/design/Container.vue";
import { computed, isRef, ref, watch } from "vue";
import { useSeo } from "~/composables/useSeo";
import { useHead } from "@vueuse/head";
import { useRoute, useRouter } from "vue-router";
import { useApi } from "~/composables/useApi";
import { PaginatedResult, Project } from "hangar-api";
import Alert from "~/components/design/Alert.vue";

const i18n = useI18n();
const route = useRoute();
const router = useRouter();
const ctx = useContext();

const backendData = useBackendDataStore();
const sorters = [
  { id: "stars", label: i18n.t("project.sorting.mostStars") },
  { id: "downloads", label: i18n.t("project.sorting.mostDownloads") },
  { id: "views", label: i18n.t("project.sorting.mostViews") },
  { id: "newest", label: i18n.t("project.sorting.newest") },
  { id: "updated", label: i18n.t("project.sorting.recentlyUpdated") },
];

// todo versions need to be extracted from the platforms
const versions = [
  { version: "1.18.1" },
  { version: "1.18" },
  { version: "1.17.1" },
  { version: "1.17" },
  { version: "1.16.5" },
  { version: "1.16.4" },
  { version: "1.16.3" },
  { version: "1.16.2" },
  { version: "1.16.1" },
  { version: "1.16" },
];

const filters = ref({
  versions: [],
  categories: [],
  platforms: [],
  licences: [],
});

const query = ref<string>((route.query.q as string) || "");
const loggedOut = ref<boolean>("loggedOut" in route.query);
const projects = ref<PaginatedResult<Project> | null>();

const requestParams = computed(() => {
  // TODO add filters for licence and version
  // TODO implement sorting, see https://github.com/HangarMC/Hangar/pull/597
  const params: Record<string, any> = { limit: 25, offset: 0, category: filters.value.categories, platform: filters.value.platforms };
  if (query.value && query.value) {
    params.q = query.value;
  }
  return params;
});
const p = await useProjects(requestParams.value).catch((e) => handleRequestError(e, ctx, i18n));
if (p && p.value) {
  projects.value = p.value;
}

watch(filters, async () => updateProjects(), { deep: true });
watch(query, async () => {
  await updateProjects();
});

async function updateProjects() {
  projects.value = await useApi<PaginatedResult<Project>>("projects", false, "get", requestParams.value);
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
    <Alert v-if="loggedOut" type="success">You have been logged out!</Alert>
    <h2 class="text-3xl font-bold uppercase text-center my-4">{{ i18n.t("hangar.projectSearch.title") }}</h2>
    <!-- Search Bar -->
    <div class="relative rounded-md flex shadow-lg w-full max-w-screen-md">
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
          <MenuItems class="absolute right-0 top-16 flex flex-col z-10 background-header drop-shadow-md rounded-md border-top-primary">
            <MenuItem v-for="sorter in sorters" :key="sorter.id" v-slot="{ active }">
              <button :class="{ 'bg-gradient-to-r from-[#004ee9] to-[#367aff] text-white': active }" class="p-2 text-left">
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
    <div class="w-full min-w-0 mb-5" lg="mb-0">
      <ProjectList :projects="projects" />
    </div>
    <!-- Sidebar -->
    <Card accent class="min-w-300px flex flex-col gap-4">
      <div class="versions">
        <h3 class="font-bold mb-1">Minecraft versions</h3>
        <div class="flex flex-col gap-1 max-h-30 overflow-auto">
          <InputCheckbox v-for="version in versions" :key="version.version" v-model="filters.versions" :value="version.version" :label="version.version" />
        </div>
      </div>
      <div class="categories">
        <h3 class="font-bold mb-1">Categories</h3>
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
      <div class="platforms">
        <h3 class="font-bold mb-1">Platforms</h3>
        <div class="flex flex-col gap-1">
          <InputCheckbox
            v-for="platform in backendData.visiblePlatforms"
            :key="platform.enumName"
            v-model="filters.platforms"
            :value="platform.enumName"
            :label="platform.name"
          />
        </div>
      </div>
      <div class="licenses">
        <h3 class="font-bold mb-1">Licenses</h3>
        <div class="flex flex-col gap-1">
          <InputCheckbox v-for="license in backendData.licenses" :key="license" v-model="filters.licences" :value="license" :label="license" />
        </div>
      </div>
    </Card>
  </Container>
</template>

<route lang="yaml">
meta:
  layout: wide
</route>
