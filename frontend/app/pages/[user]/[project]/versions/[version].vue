<script lang="ts" setup>
import type { HangarProject, Platform } from "#shared/types/backend";
import { useDataLoader } from "~/composables/useDataLoader";

defineProps<{
  project?: HangarProject;
}>();

const { data: version } = useDataLoader("version");

definePageMeta({
  dataLoader_version: true,
});

const versionPlatforms = computed<Set<Platform>>(() => {
  const result = new Set<Platform>();
  if (version?.value) {
    for (const platformKey in version.value.platformDependencies) {
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
