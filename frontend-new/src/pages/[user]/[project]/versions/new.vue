<script lang="ts" setup>
import { useHead } from "@vueuse/head";
import { useSeo } from "~/composables/useSeo";
import { projectIconUrl } from "~/composables/useUrlHelper";
import { HangarProject } from "hangar-internal";
import { useRoute } from "vue-router";
import { useI18n } from "vue-i18n";
import Steps, { Step } from "~/components/design/Steps.vue";
import { ref } from "vue";
import Card from "~/components/design/Card.vue";

const route = useRoute();
const i18n = useI18n();
const props = defineProps<{
  project: HangarProject;
}>();

const selectedStep = ref("test");
const steps: Step[] = [
  { value: "test", header: "Test" },
  { value: "test2", header: "Test2" },
];

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
  <Steps v-model="selectedStep" :steps="steps">
    <template #test> test </template>
    <template #test2> test2 </template>
  </Steps>
</template>

<route lang="yaml">
meta:
  requireProjectPerm: ["CREATE_VERSION"]
</route>
