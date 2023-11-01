<script lang="ts" setup>
import { provide } from "vue";
import type { User } from "hangar-api";
import type { RouteLocationNormalized } from "vue-router";
import { useRoute } from "vue-router";
import type { HangarProjectPage, HangarProject } from "hangar-internal";
import { useProject } from "~/composables/useApiHelper";
import { useErrorRedirect } from "~/composables/useErrorRedirect";
import ProjectHeader from "~/components/projects/ProjectHeader.vue";
import ProjectNav from "~/components/projects/ProjectNav.vue";
import { createError, navigateTo, onBeforeRouteUpdate, useInternalApi } from "#imports";
import Delayed from "~/components/design/Delayed.vue";

defineProps<{
  user: User;
}>();

const route = useRoute();
const project = await useProject(route.params.project as string);
await verify(route);

async function verify(to: RouteLocationNormalized) {
  if (!project?.value) {
    throw useErrorRedirect(to, 404, "Not found");
  } else if (to.params.project !== project.value?.namespace.slug) {
    const newPath = to.fullPath.replace(to.params.project as string, project.value!.namespace.slug);
    console.debug("Redirect to " + newPath + " from (" + to.fullPath + ")");
    await navigateTo(newPath);
    throw createError("dummy");
  }
}

onBeforeRouteUpdate(async (to, from) => {
  if (!to.params.project || !to.params.user) return;
  if (to.params.user === from.params.user && to.params.project === from.params.project) return;
  project.value = await useInternalApi<HangarProject>("projects/project/" + to.params.project);
  await verify(to);
});

provide("updateProjectPages", function (pages: HangarProjectPage[]) {
  if (project && project.value) project.value!.pages = pages;
});
</script>

<template>
  <div v-if="project">
    <ProjectHeader :project="project" />
    <ProjectNav :project="project" />
    <router-view v-slot="{ Component }">
      <Suspense>
        <component :is="Component" v-model:project="project" :user="user" />
        <template #fallback>
          <Delayed> Loading... </Delayed>
        </template>
      </Suspense>
    </router-view>
  </div>
</template>
