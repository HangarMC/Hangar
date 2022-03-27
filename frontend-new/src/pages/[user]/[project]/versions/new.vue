<script lang="ts" setup>
import { useHead } from "@vueuse/head";
import { useSeo } from "~/composables/useSeo";
import { projectIconUrl } from "~/composables/useUrlHelper";
import { HangarProject } from "hangar-internal";
import { useRoute } from "vue-router";
import { useI18n } from "vue-i18n";

const route = useRoute();
const i18n = useI18n();
const props = defineProps<{
  project: HangarProject;
}>();

useHead(
  useSeo(
    i18n.t("version.new.title") + " | " + props.project.name,
    props.project.description,
    route,
    projectIconUrl(props.project.namespace.owner, props.project.namespace.slug)
  )
);
</script>

<template>
  <h1>new version</h1>
</template>

<route lang="yaml">
meta:
  requireProjectPerm: ["CREATE_VERSION"]
</route>
