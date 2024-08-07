<script lang="ts" setup>
import type { RouteLocationNormalizedTyped } from "unplugin-vue-router";
import type { HangarProject, HangarProjectPage, User } from "~/types/backend";

defineProps<{
  user: User;
}>();

const route = useRoute("user-project");
// for some reason this page is triggered twice on soft navigation, need to short circuit the case where the params are empty
const project = route.params.project ? await useProject(route.params.project) : ref(null);
if ("project" in route.params) {
  await verify(route);
}

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
