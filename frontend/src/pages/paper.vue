<script setup lang="ts">
import Homepage from "~/components/Homepage.vue";
import { Platform } from "~/types/backend";
</script>

<template>
  <Homepage :platform="Platform.PAPER" platformName="Paper" />
</template>
