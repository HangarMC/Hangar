<script lang="ts" setup>
import { useHead } from "@vueuse/head";
import { useSeo } from "~/composables/useSeo";
import { useRoute } from "vue-router";
import { Platform } from "~/types/enums";
import { HangarProject, HangarVersion } from "hangar-internal";
import { projectIconUrl } from "~/composables/useUrlHelper";

const route = useRoute();

const props = defineProps<{
  versions: Map<Platform, HangarVersion>;
  project: HangarProject;
}>();

useHead(
  useSeo("Reviews | " + props.project.name, props.project.description, route, projectIconUrl(props.project.namespace.owner, props.project.namespace.slug))
);
</script>

<template>
  <!-- todo reviews page -->
  <h1>reviews</h1>
</template>

<route lang="yaml">
meta:
  requireGlobalPerm: ["REVIEWER"]
</route>
