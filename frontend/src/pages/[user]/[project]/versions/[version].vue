<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import { useRoute, useRouter } from "vue-router";
import { HangarProject } from "hangar-internal";
import { handleRequestError } from "~/composables/useErrorHandling";
import { useProjectVersionsInternal } from "~/composables/useApiHelper";
import { useErrorRedirect } from "~/lib/composables/useErrorRedirect";
import { Platform } from "~/types/enums";

const i18n = useI18n();
const route = useRoute();

const props = defineProps<{
  project: HangarProject;
}>();

const version = await useProjectVersionsInternal(route.params.user as string, route.params.project as string, route.params.version as string).catch((e) =>
  handleRequestError(e)
);
if (!version || !version.value) {
  throw useErrorRedirect(route, 404, "Not found");
}

const versionPlatforms: Set<Platform> = new Set<Platform>();
if (version && version.value) {
  for (const platformKey in version.value.platformDependencies) {
    versionPlatforms.add(platformKey as Platform);
  }
}

if (!route.params.platform) {
  let path = route.path;
  if (path.endsWith("/")) {
    path = path.substring(0, path.length - 1);
  }
  const [entry] = versionPlatforms;
  if (!entry) {
    throw useErrorRedirect(route, 404, "Not found");
  }
  await useRouter().replace({ path: `${path}/${entry.toLowerCase()}` });
}
</script>

<template>
  <router-view v-slot="{ Component }">
    <Suspense>
      <component :is="Component" :project="project" :version="version" :version-platforms="versionPlatforms" />
      <template #fallback> Loading... </template>
    </Suspense>
  </router-view>
</template>
