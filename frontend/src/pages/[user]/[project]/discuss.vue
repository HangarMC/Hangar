<script lang="ts" setup>
import type { HangarProject } from "hangar-internal";

const route = useRoute();
const props = defineProps<{
  project: HangarProject;
}>();

useHead(useSeo("Discuss | " + props.project.name, props.project.description, route, props.project.avatarUrl));
</script>

<template>
  <h1>discuss</h1>
</template>
