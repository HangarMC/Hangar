<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import { useRoute } from "vue-router";
import PageTitle from "~/components/design/PageTitle.vue";
import { useSeo } from "~/composables/useSeo";
import { useHead } from "@vueuse/head";

const i18n = useI18n();
const route = useRoute();

useHead(useSeo(i18n.t("pages.guidelinesTitle"), null, route, null));
</script>

<template>
  <PageTitle>{{ i18n.t("guidelines.title") }}</PageTitle>
</template>
