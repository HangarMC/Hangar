<script lang="ts" setup>
import { useProjectVersionsInternal } from "~/composables/useApiHelper";
import { useContext } from "vite-ssr/vue";
import { useI18n } from "vue-i18n";
import { useRoute, useRouter } from "vue-router";
import { handleRequestError } from "~/composables/useErrorHandling";
import { HangarProject } from "hangar-internal";
import { useErrorRedirect } from "~/lib/composables/useErrorRedirect";
import { useHead } from "@vueuse/head";
import { useSeo } from "~/composables/useSeo";
import { projectIconUrl } from "~/composables/useUrlHelper";
import { Platform } from "~/types/enums";

const ctx = useContext();
const i18n = useI18n();
const route = useRoute();

const props = defineProps<{
  project: HangarProject;
}>();

const version = await useProjectVersionsInternal(route.params.user as string, route.params.project as string, route.params.version as string).catch((e) =>
  handleRequestError(e, ctx, i18n)
);
if (!version || !version.value) {
  await useRouter().replace(useErrorRedirect(route, 404, "Not found"));
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
  await (entry ? useRouter().replace({ path: `${path}/${entry.toLowerCase()}` }) : useRouter().replace(useErrorRedirect(route, 404, "Not found")));
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
