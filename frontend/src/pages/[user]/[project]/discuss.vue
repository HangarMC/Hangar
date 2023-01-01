<script lang="ts" setup>
import { useHead } from "@vueuse/head";
import { useRoute } from "vue-router";
import { HangarProject } from "hangar-internal";
import { useSeo } from "~/composables/useSeo";

const route = useRoute();
const props = defineProps<{
  project: HangarProject;
}>();

useHead(useSeo("Discuss | " + props.project.name, props.project.description, route, props.project.avatarUrl));
</script>

<template>
  <h1>discuss</h1>
</template>
