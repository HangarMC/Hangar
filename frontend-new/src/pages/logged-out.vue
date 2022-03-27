<script lang="ts" setup>
import { useSeo } from "~/composables/useSeo";
import { useRoute } from "vue-router";
import { useHead } from "@vueuse/head";

const route = useRoute();

useHead(useSeo("Logged Out", null, route, null));
</script>

<template>
  <h1>Logged out</h1>
</template>

<route lang="yaml">
meta:
  requireNotLoggedIn: true
</route>
