<script lang="ts" setup>
import { type PropType, provide, watch } from "vue";
import { User } from "hangar-api";
import { useI18n } from "vue-i18n";
import { useRoute } from "vue-router";
import { HangarProjectPage, HangarProject } from "hangar-internal";
import { useProject } from "~/composables/useApiHelper";
import { useErrorRedirect } from "~/composables/useErrorRedirect";
import ProjectHeader from "~/components/projects/ProjectHeader.vue";
import ProjectNav from "~/components/projects/ProjectNav.vue";
import { createError, navigateTo, useInternalApi } from "#imports";
import Delayed from "~/components/design/Delayed.vue";

defineProps({
  user: {
    type: Object as PropType<User>,
    required: true,
  },
});

const i18n = useI18n();
const route = useRoute();
const project = await useProject(route.params.user as string, route.params.project as string);
await verify();

async function verify() {
  if (!project?.value) {
    throw useErrorRedirect(route, 404, "Not found");
  } else if (route.params.project !== project.value?.namespace.slug) {
    const newPath = route.fullPath.replace(route.params.project as string, project.value!.namespace.slug);
    console.debug("Redirect to " + newPath + " from (" + route.fullPath + ")");
    await navigateTo(newPath);
    throw createError("dummy");
  }
}

watch(
  () => route.params.project,
  async () => {
    project.value = await useInternalApi<HangarProject>("projects/project/" + route.params.user + "/" + route.params.project);
    await verify();
  }
);

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
