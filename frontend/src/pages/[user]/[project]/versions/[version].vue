<script lang="ts" setup>
import { useProjectVersionsInternal } from "~/composables/useApiHelper";
import { useContext } from "vite-ssr/vue";
import { useI18n } from "vue-i18n";
import { useRoute, useRouter } from "vue-router";
import { handleRequestError } from "~/composables/useErrorHandling";
import { HangarProject, HangarVersion } from "hangar-internal";
import { Platform } from "~/types/enums";
import { useErrorRedirect } from "~/lib/composables/useErrorRedirect";
import { useHead } from "@vueuse/head";
import { useSeo } from "~/composables/useSeo";
import { projectIconUrl } from "~/composables/useUrlHelper";
import Link from "~/lib/components/design/Link.vue";

const ctx = useContext();
const i18n = useI18n();
const route = useRoute();

const props = defineProps<{
  project: HangarProject;
}>();

const versions = await useProjectVersionsInternal(route.params.user as string, route.params.project as string, route.params.version as string).catch((e) =>
  handleRequestError(e, ctx, i18n)
);
if (!versions || !versions.value) {
  await useRouter().push(useErrorRedirect(route, 404, "Not found"));
}

const versionMap: Map<Platform, HangarVersion> = new Map<Platform, HangarVersion>();
const versionPlatforms: Set<Platform> = new Set<Platform>();
if (versions && versions.value) {
  for (const version of versions.value) {
    for (const platformKey in version.platformDependencies) {
      versionPlatforms.add(platformKey as Platform);
      versionMap.set(platformKey as Platform, version);
    }
  }
}

if (!route.params.platform) {
  let path = route.path;
  if (path.endsWith("/")) {
    path = path.substring(0, path.length - 1);
  }
  const entry = versionMap.keys().next();
  await (entry.value ? useRouter().replace({ path: `${path}/${entry.value.toLowerCase()}` }) : useRouter().push(useErrorRedirect(route, 404, "Not found")));
}

useHead(
  useSeo(
    (props.project.name + " " + route.params.version) as string,
    props.project.description,
    route,
    projectIconUrl(props.project.namespace.owner, props.project.namespace.slug)
  )
);
</script>

<template>
  <router-view v-slot="{ Component }">
    <Suspense>
      <component :is="Component" :project="project" :versions="versionMap" :version-platforms="versionPlatforms" />
      <template #fallback> Loading... </template>
    </Suspense>
  </router-view>
</template>
