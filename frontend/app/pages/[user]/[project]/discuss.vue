<script lang="ts" setup>
import type { HangarProject } from "#shared/types/backend";

const route = useRoute("user-project-discuss");
const props = defineProps<{
  project?: HangarProject;
}>();

useSeo(computed(() => ({ title: "Discuss | " + props.project?.name, route, description: props.project?.description, image: props.project?.avatarUrl })));
</script>

<template>
  <h1>discuss</h1>
</template>
