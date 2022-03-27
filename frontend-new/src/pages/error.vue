<script lang="ts" setup>
import { useRoute } from "vue-router";
import { useHead } from "@vueuse/head";
import { useSeo } from "~/composables/useSeo";

const route = useRoute();
useHead(useSeo((route.params.status || 404) + " " + (route.params.msg || "Not found"), null, route, null));
</script>

<template>
  <h1>Error: {{ route.params.status || 404 }}</h1>
  <h2>{{ route.params.msg || "Not found" }}</h2>
</template>

<route lang="yaml">
layout: error
</route>
