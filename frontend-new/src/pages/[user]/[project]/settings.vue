<script lang="ts" setup>
import { useHead } from "@vueuse/head";
import { useSeo } from "~/composables/useSeo";
import { projectIconUrl } from "~/composables/useUrlHelper";
import { useRoute } from "vue-router";
import { HangarProject } from "hangar-internal";
import { useI18n } from "vue-i18n";

const route = useRoute();
const i18n = useI18n();
const props = defineProps<{
  project: HangarProject;
}>();

useHead(
  useSeo(
    i18n.t("project.settings.title") + " | " + props.project.name,
    props.project.description,
    route,
    projectIconUrl(props.project.namespace.owner, props.project.namespace.slug)
  )
);
</script>

<template>
  <h1>settings</h1>
</template>

<route lang="yaml">
meta:
  requireProjectPerm: ["EDIT_SUBJECT_SETTINGS"]
</route>
