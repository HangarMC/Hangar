<script lang="ts" setup>
import { HangarVersion, HangarProject, JarScanResult } from "hangar-internal";
import { definePageMeta, ref, useHead, useInternalApi, useJarScan, useRoute, useSeo } from "#imports";
import { Platform } from "~/types/enums";
import PrettyTime from "~/lib/components/design/PrettyTime.vue";
import { useNotificationStore } from "~/lib/store/notification";
import Button from "~/lib/components/design/Button.vue";

definePageMeta({
  globalPermsRequired: ["REVIEWER"],
});

const props = defineProps<{
  version: HangarVersion;
  project: HangarProject;
  versionPlatforms: Set<Platform>;
}>();

const route = useRoute();

const results = ref<JarScanResult[]>([]);
for (const platform of props.versionPlatforms) {
  const result = await useJarScan(props.version.id, platform);
  if (result.value) {
    results.value.push(result.value);
  }
}

async function scan() {
  for (const platform of props.versionPlatforms) {
    await useInternalApi(`jarscanning/scan/${platform}/${props.version.id}`, "POST");
  }
  await useNotificationStore.success("Scheduled scan");
}

useHead(useSeo("Scan | " + props.project.name, props.project.description, route, props.project.avatarUrl));
</script>

<template>
  <div v-if="version" class="mt-4">
    <div v-for="result in results" :key="result.id" class="mb-4">
      <h3 class="text-xl">Results for {{ version.name }} {{ result.platform }} (last scanned at <PrettyTime :time="result.createdAt" short-relative />)</h3>
      <div v-for="(file, idx) in result.data" :key="idx">
        <div v-for="(line, idx2) in file" :key="idx2">
          {{ line }}
        </div>
        <hr class="my-1" />
      </div>
    </div>
    <Button @click="scan">Scan</Button>
  </div>
</template>
