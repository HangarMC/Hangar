<script setup lang="ts">
import { useI18n } from "vue-i18n";
import { useHead } from "@vueuse/head";
import { useRoute } from "vue-router";
import PageTitle from "~/lib/components/design/PageTitle.vue";
import { useSeo } from "~/composables/useSeo";

const i18n = useI18n();
const route = useRoute();

useHead(useSeo("Import projects", null, route, null));
</script>

<template>
  <div>
    <PageTitle>Import projects</PageTitle>
  </div>
</template>
