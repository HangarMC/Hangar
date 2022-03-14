<script lang="ts" setup>
import { useRoute } from "vue-router";

const route = useRoute();
</script>

<template>
  <h1>Error: {{ route.params.status || 404 }}</h1>
  <h2>{{ route.params.msg || "Not found" }}</h2>
</template>

<route lang="yaml">
layout: error
</route>
