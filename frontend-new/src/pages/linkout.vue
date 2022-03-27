<script lang="ts" setup>
import { useRoute } from "vue-router";
import { useSeo } from "~/composables/useSeo";
import { useHead } from "@vueuse/head";

const route = useRoute();
const target = route.query.remoteUrl;

useHead(useSeo("Linkout", null, route, null));
</script>
<template>
  <div>
    Hur dur danger ahead!<br />
    <a class="cursor-pointer" @click="$router.back()">Back to safety</a><br />
    <a :href="target" target="_self" rel="noopener noreferrer">Go ahead</a>
  </div>
</template>
