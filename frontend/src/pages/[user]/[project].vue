<script lang="ts" setup>
import { PropType, provide } from "vue";
import { User } from "hangar-api";
import { useContext } from "vite-ssr/vue";
import { useI18n } from "vue-i18n";
import { useProject } from "~/composables/useApiHelper";
import { useRoute, useRouter } from "vue-router";
import { handleRequestError } from "~/composables/useErrorHandling";
import { useErrorRedirect } from "~/lib/composables/useErrorRedirect";
import ProjectHeader from "~/components/projects/ProjectHeader.vue";
import ProjectNav from "~/components/projects/ProjectNav.vue";
import { HangarProjectPage } from "hangar-internal";

defineProps({
  user: {
    type: Object as PropType<User>,
    required: true,
  },
});

const ctx = useContext();
const i18n = useI18n();
const route = useRoute();
const project = await useProject(route.params.user as string, route.params.project as string).catch((e) => handleRequestError(e, ctx, i18n));
if (!project || !project.value) {
  await useRouter().replace(useErrorRedirect(route, 404, "Not found"));
}

provide("updateProjectPages", function (pages: HangarProjectPage[]) {
  if (project && project.value) project.value.pages = pages;
});
</script>

<template>
  <div v-if="project">
    <ProjectHeader :project="project"></ProjectHeader>
    <ProjectNav :project="project"></ProjectNav>
    <router-view v-slot="{ Component }">
      <Suspense>
        <component :is="Component" v-model:project="project" :user="user" />
        <template #fallback> Loading... </template>
      </Suspense>
    </router-view>
  </div>
</template>
