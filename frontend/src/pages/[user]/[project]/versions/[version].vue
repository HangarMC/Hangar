<script lang="ts" setup>
import { computed } from "vue";
import type { HangarProject, HangarVersion } from "hangar-internal";
import type { Platform } from "~/types/enums";

const route = useRoute();

defineProps<{
  project: HangarProject;
}>();

const version = await useProjectVersionsInternal(route.params.project as string, route.params.version as string);
verify();

function verify() {
  if (!version?.value) {
    throw useErrorRedirect(route, 404, "Not found");
  }
}

onBeforeRouteUpdate(async (to, from) => {
  if (!to.params.version || !to.params.project || !to.params.user) return;
  if (to.params.user === from.params.user && to.params.project === from.params.project && to.params.version === from.params.version) return;
  version.value = await useInternalApi<HangarVersion>(`versions/version/${to.params.user}/${to.params.project}/versions/${to.params.version}`);
  await verify();
});

const versionPlatforms = computed<Set<Platform>>(() => {
  const result = new Set<Platform>();
  if (version?.value) {
    for (const platformKey in version.value!.platformDependencies) {
      result.add(platformKey as Platform);
    }
  }
  return result;
});
</script>

<template>
  <router-view v-slot="{ Component }">
    <Suspense>
      <div>
        <component :is="Component" :project="project" :version="version" :version-platforms="versionPlatforms" />
      </div>
      <template #fallback> <Delayed> Loading... </Delayed> </template>
    </Suspense>
  </router-view>
</template>
