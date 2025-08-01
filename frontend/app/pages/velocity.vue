<script setup lang="ts">
import Homepage from "~/components/Homepage.vue";
import { Platform } from "#shared/types/backend";
</script>

<template>
  <Homepage :platform="Platform.VELOCITY" platform-name="Velocity" />
</template>
