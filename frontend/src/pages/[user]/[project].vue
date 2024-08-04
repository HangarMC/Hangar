<script lang="ts" setup>
import type { HangarProject, HangarProjectPage, User } from "~/types/backend";
import { useDataLoader } from "~/composables/useDataLoader";

defineProps<{
  user?: User;
}>();

const { data: project } = useDataLoader<HangarProject>("project");

definePageMeta({
  dataLoader_project: true,
});

provide("updateProjectPages", function (pages: HangarProjectPage[]) {
  if (project.value) project.value.pages = pages;
});
</script>

<template>
  <ProjectHeader :project="project" />
  <ProjectNav :project="project" />
  <router-view v-slot="{ Component }">
    <Suspense>
      <div>
        <component :is="Component" v-model:project="project" :user="user" />
      </div>
      <template #fallback>
        <Delayed> Loading... </Delayed>
      </template>
    </Suspense>
  </router-view>
</template>
