<script lang="ts" setup>
import type { Platform, HangarProject, HangarVersion, JarScanResult } from "~/types/backend";

definePageMeta({
  globalPermsRequired: ["Reviewer"],
});

const props = defineProps<{
  version?: HangarVersion;
  project?: HangarProject;
  versionPlatforms: Set<Platform>;
}>();

const route = useRoute("user-project-versions-version-scan");

const results = ref<JarScanResult[]>([]);
watch(
  () => props.version,
  async () => {
    results.value = [];
    for (const platform of props.versionPlatforms) {
      if (!props.version?.downloads?.[platform]?.fileInfo) {
        continue;
      }

      const result = await useInternalApi<JarScanResult>(`jarscanning/result/${platform}/${props.version?.id}`);
      if (result) {
        results.value.push(result);
      }
    }
  }
);

async function scan() {
  for (const platform of props.versionPlatforms) {
    if (!props.version?.downloads?.[platform]?.fileInfo) {
      continue;
    }

    await useInternalApi(`jarscanning/scan/${platform}/${props.version?.id}`, "POST");
  }
  await useNotificationStore().success("Scheduled scan");
}

useHead(useSeo("Scan | " + props.project?.name, props.project?.description, route, props.project?.avatarUrl));
</script>

<template>
  <div v-if="version" class="mt-4">
    <div v-for="result in results" :key="result.id" class="mb-4">
      <h3 class="text-2xl inline-flex items-center space-x-1">
        <IconMdiInformation class="mr-1" />
        Results for {{ version.name }} {{ result.platform }} (last scanned: <PrettyTime :time="result.createdAt" short-relative />)
      </h3>
      <div v-for="(line, idx) in result.entries" :key="idx">
        {{ line }}
        <hr class="my-1" />
      </div>
    </div>
    <Button @click="scan">Scan</Button>
  </div>
</template>
