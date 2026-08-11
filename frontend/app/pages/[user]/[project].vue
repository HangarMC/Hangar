<script lang="ts" setup>
import type { HangarOrganization, HangarProjectPage, User } from "#shared/types/backend";
import { useDataLoader } from "~/composables/useDataLoader";

defineProps<{
  user?: User;
  organization?: HangarOrganization;
}>();

const { data: project } = useDataLoader("project");

definePageMeta({
  dataLoader_project: true,
});

provide("updateProjectPages", function (pages: HangarProjectPage[]) {
  if (project.value) project.value.pages = pages;
});
</script>

<template>
  <div>
    <ProjectHeader :project="project" />
    <ProjectNav :project="project" />
    <router-view v-slot="{ Component }">
      <Suspense>
        <div>
          <component :is="Component" v-model:project="project" />
        </div>
        <template #fallback>
          <Delayed> Loading... </Delayed>
        </template>
      </Suspense>
    </router-view>
  </div>
</template>
