<script lang="ts" setup>
const i18n = useI18n();
const route = useRoute("api-docs");

const server = import.meta.env.SSR;
useSeo(computed(() => ({ title: i18n.t("apiDocs.title"), description: "API Docs for the Hangar REST API", route })));
</script>

<template>
  <div class="my-auto mx-2 dum">
    <LazyApiDocs />
    <div v-show="server">
      <h1 class="text-3xl text-bold">Hangar API</h1>
      <h2 class="text-2xl">API Docs for the Hangar REST API</h2>
      <p>Loading...</p>
    </div>
  </div>
</template>

<style scoped>
.dum {
  /* nooooo clue where the height 100vh is coming from */
  height: auto !important;
}
</style>
