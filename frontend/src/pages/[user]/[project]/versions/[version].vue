<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import { useRoute } from "vue-router";
import { HangarProject } from "hangar-internal";
import { useProjectVersionsInternal } from "~/composables/useApiHelper";
import { useErrorRedirect } from "~/composables/useErrorRedirect";
import { Platform } from "~/types/enums";
import Delayed from "~/components/design/Delayed.vue";

const i18n = useI18n();
const route = useRoute();

const props = defineProps<{
  project: HangarProject;
}>();

const version = await useProjectVersionsInternal(route.params.user as string, route.params.project as string, route.params.version as string);
if (!version?.value) {
  throw useErrorRedirect(route, 404, "Not found");
}

const versionPlatforms: Set<Platform> = new Set<Platform>();
if (version && version.value) {
  for (const platformKey in version.value.platformDependencies) {
    versionPlatforms.add(platformKey as Platform);
  }
}
</script>

<template>
  <router-view v-slot="{ Component }">
    <Suspense>
      <component :is="Component" :project="project" :version="version" :version-platforms="versionPlatforms" />
      <template #fallback> <Delayed> Loading... </Delayed> </template>
    </Suspense>
  </router-view>
</template>
