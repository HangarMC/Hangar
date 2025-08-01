<script lang="ts" setup>
import type { Platform, HangarProject, Version } from "#shared/types/backend";
import type { AxiosError } from "axios";

definePageMeta({
  globalPermsRequired: ["Reviewer"],
});

const props = defineProps<{
  version?: Version;
  project?: HangarProject;
  versionPlatforms: Set<Platform>;
}>();

const route = useRoute("user-project-versions-version-scan");

const { jarScans } = useJarScans(() => props.version?.id as unknown as string);

async function doScan() {
  for (const platform of props.versionPlatforms) {
    if (!props.version?.downloads?.[platform]?.fileInfo) {
      continue;
    }

    try {
      await useInternalApi(`jarscanning/scan/${props.version?.id}/${platform}`, "POST");
      useNotificationStore().success("Scheduled scan for " + platform);
    } catch (err) {
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-expect-error
      useNotificationStore().error("Failed to schedule scan for " + platform + ": " + (err as AxiosError)?.response?.data?.message || err);
    }
  }
}

useSeo(
  computed(() => ({
    title: "Scan | " + props.project?.name,
    route,
    description: props.project?.description,
    image: props.project?.avatarUrl,
  }))
);
</script>

<template>
  <div v-if="version" class="mt-4">
    <div v-for="scan in jarScans" :key="scan.id" class="mb-4">
      <h3 class="text-2xl inline-flex items-center space-x-1">
        <IconMdiInformation class="mr-1" />
        Results for {{ version.name }} {{ scan.platform }} (last scanned: <PrettyTime :time="scan.createdAt" short-relative />)
      </h3>
      <div v-for="(line, idx) in scan.entries" :key="idx">
        {{ line }}
        <hr class="my-1" />
      </div>
    </div>
    <Button @click="doScan">Scan</Button>
  </div>
</template>
