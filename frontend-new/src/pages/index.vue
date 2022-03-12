<script setup lang="ts">
import { Menu, MenuButton, MenuItem, MenuItems } from "@headlessui/vue";
import { useI18n } from "vue-i18n";
import LabeledCheckbox from "~/components/LabeledCheckbox.vue";
import { useBackendDataStore } from "~/store/backendData";
import ProjectList from "~/components/ProjectList.vue";
import { useProjects } from "~/composables/useApiHelper";
import { handleRequestError } from "~/composables/useErrorHandling";
import { useContext } from "vite-ssr/vue";

const i18n = useI18n();

// TODO: versions, categories, platforms and licences should be all loaded from backend eventually (see internal.BackendDataController)
const backendData = useBackendDataStore();
const sorters = [
  { id: "stars", label: i18n.t("project.sorting.mostStars") },
  { id: "downloads", label: i18n.t("project.sorting.mostDownloads") },
  { id: "views", label: i18n.t("project.sorting.mostViews") },
  { id: "newest", label: i18n.t("project.sorting.newest") },
  { id: "updated", label: i18n.t("project.sorting.recentlyUpdated") },
];

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

const ctx = useContext();
const projects = await useProjects().catch((e) => handleRequestError(e, ctx, i18n));
</script>

<template>
  <div class="flex flex-col items-center pt-10">
    <h2 class="text-3xl font-bold uppercase">Find your favorite plugins</h2>
    <!-- Search Bar & Sorting button -->
    <div class="flex flex-row mt-6 rounded-md big-box-shadow">
      <!-- Search Bar -->
      <input
        class="rounded-l-md p-3 w-[80vw] max-w-800px focus-visible:(border-white) text-black"
        type="text"
        placeholder="Search in 1 projects, proudly made by the community..."
      />
      <!-- Sorting Button -->
      <div class="rounded-r-md w-100px bg-gradient-to-r from-[#004ee9] to-[#367aff]">
        <Menu>
          <MenuButton class="rounded-r-md h-1/1 text-left font-semibold flex flex-row items-center gap-2 text-white p-2">
            <span>Sort by</span>
            <icon-mdi-sort-variant class="text-[1.2em] pointer-events-none overflow-hidden" />
          </MenuButton>
          <transition
            enter-active-class="transition duration-100 ease-out"
            enter-from-class="transform scale-95 opacity-0"
            enter-to-class="transform scale-100 opacity-100"
            leave-active-class="transition duration-75 ease-out"
            leave-from-class="transform scale-100 opacity-100"
            leave-to-class="transform scale-95 opacity-0"
          >
            <MenuItems class="absolute flex flex-col z-10 background-header shadow1 rounded-md border-top-primary">
              <MenuItem v-for="sorter in sorters" :key="sorter.id" v-slot="{ active }">
                <button :class="{ 'bg-gradient-to-r from-[#004ee9] to-[#367aff] text-white': active }" class="p-2 text-left">
                  {{ sorter.label }}
                </button>
              </MenuItem>
            </MenuItems>
          </transition>
        </Menu>
      </div>
    </div>
  </div>
  <div class="p-4 mt-5 w-screen max-w-1200px flex justify-around m-auto flex-col gap-y-6" lg="flex-row gap-x-6 gap-y-0 ">
    <!-- Projects -->
    <div class="min-h-800px bg-gray-200 rounded-md" lg="w-2/3 min-w-2/3 max-w-2/3">
      <ProjectList :projects="projects" />
    </div>
    <!-- Sidebar -->
    <div class="flex flex-col gap-4 bg-white border-top-primary shadow-soft rounded-md min-w-300px min-h-800px p-4" dark="bg-background-dark-90">
      <div class="versions">
        <h3 class="font-bold">Minecraft versions</h3>
        <div class="flex flex-col gap-2 max-h-30 overflow-auto">
          <LabeledCheckbox v-for="version in versions" :key="version.version" :label="version.version" />
        </div>
      </div>
      <hr />
      <div class="categories">
        <h3 class="font-bold">Categories</h3>
        <div class="flex flex-col gap-2">
          <LabeledCheckbox v-for="category in backendData.visibleCategories" :key="category.apiName" :label="category.title" />
        </div>
      </div>
      <hr />
      <div class="platforms">
        <h3 class="font-bold">Platforms</h3>
        <div class="flex flex-col gap-2">
          <LabeledCheckbox v-for="platform in backendData.visiblePlatforms" :key="platform.enumName" :label="platform.name" />
        </div>
      </div>
      <hr />
      <div class="licenses">
        <h3 class="font-bold">Licenses</h3>
        <div class="flex flex-col gap-2">
          <LabeledCheckbox v-for="license in backendData.licenses" :key="license" :label="license" />
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="css" scoped>
.big-box-shadow {
  box-shadow: 0 0 10px 0 #004ee99e;
}
</style>
