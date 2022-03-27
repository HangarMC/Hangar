<script lang="ts" setup>
import { useHead } from "@vueuse/head";
import { useSeo } from "~/composables/useSeo";
import { useRoute } from "vue-router";
import { useI18n } from "vue-i18n";

const route = useRoute();
const i18n = useI18n();

useHead(useSeo(i18n.t("organization.new.title"), null, route, null));
</script>

<template>
  <h1>new org</h1>
</template>

<route lang="yaml">
meta:
  requireLoggedIn: true
</route>
