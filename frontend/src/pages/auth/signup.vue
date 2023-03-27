<script lang="ts" setup>
import { useHead } from "@vueuse/head";
import { useRoute } from "vue-router";
import { useSeo } from "~/composables/useSeo";

const route = useRoute();

useHead(useSeo("Signup", null, route, null));
</script>

<template><div>todo signup</div></template>
