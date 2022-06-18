<script lang="ts" setup>
import { useRoute } from "vue-router";
import { useHead } from "@vueuse/head";
import { useSeo } from "~/composables/useSeo";
import Lottie from "~/components/Lottie.vue";

const route = useRoute();
useHead(useSeo((route.params.status || 404) + " " + (route.params.msg || "Not found"), null, route, null));
</script>

<template>
  <div class="flex flex-col items-center justify-center min-h-50vh">
    <template v-if="(route.params.status || '404') === '404'">
      <Lottie src="https://assets9.lottiefiles.com/temp/lf20_dzWAyu.json" />
    </template>
    <template v-else>
      <h1 class="text-4xl font-bold">Error: {{ route.params.status || 404 }}</h1>
      <h2 class="text-xl font-bold">{{ route.params.msg || "Not found" }}</h2>
    </template>
  </div>
</template>

<route lang="yaml">
layout: error
</route>
