<script lang="ts" setup>
import { useHead } from "@unhead/vue";
import { useI18n } from "vue-i18n";
import { useRoute } from "vue-router";
import { useSeo } from "~/composables/useSeo";

import { LazyApiDocs } from "#components";

const i18n = useI18n();
const route = useRoute();

const server = import.meta.env.SSR;
useHead(useSeo(i18n.t("apiDocs.title"), "API Docs for the Hangar REST API", route, null));
</script>

<template>
  <div class="my-auto mx-2">
    <LazyApiDocs render-style="focused" />
    <div v-show="server">
      <h1 class="text-3xl text-bold">Hangar API</h1>
      <h2 class="text-2xl">API Docs for the Hangar REST API</h2>
      <p>Loading...</p>
    </div>
  </div>
</template>
