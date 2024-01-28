<script lang="ts" setup>
import type { User } from "hangar-api";
import type { HangarProjectPage, HangarProject } from "hangar-internal";
import type { RouteLocationNormalizedTyped } from "unplugin-vue-router";

defineProps<{
  user: User;
}>();

const route = useRoute("/:user()/:project()");
const project = await useProject(route.params.project);
await verify(route);

async function verify(to: RouteLocationNormalizedTyped<any>) {
  if (!project?.value) {
    throw useErrorRedirect(to, 404, "Not found");
  } else if (to.params.project !== project.value?.namespace.slug) {
    const newPath = to.fullPath.replace(to.params.project as string, project.value!.namespace.slug);
    console.debug("Redirect to " + newPath + " from (" + to.fullPath + ")");
    await navigateTo(newPath);
    useDummyError();
  }
}

onBeforeRouteUpdate(async (to, from) => {
  if (!("project" in to.params) || !to.params.project || !to.params.user) return;
  if ("project" in from.params && to.params.user === from.params.user && to.params.project === from.params.project) return;
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
        <div>
          <component :is="Component" v-model:project="project" :user="user" />
        </div>
        <template #fallback>
          <Delayed> Loading... </Delayed>
        </template>
      </Suspense>
    </router-view>
  </div>
</template>
