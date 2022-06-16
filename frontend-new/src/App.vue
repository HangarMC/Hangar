<script setup lang="ts">
import { useSettingsStore } from "~/store/settings";
import { useHead } from "@vueuse/head";

const settingsStore = useSettingsStore();
settingsStore.loadSettingsClient();
useHead({
  htmlAttrs: {
    class: settingsStore.darkMode ? "dark" : "light",
  },
});
</script>

<template>
  <router-view v-slot="{ Component }">
    <Suspense>
      <component :is="Component" />
      <template #fallback> Loading... </template>
    </Suspense>
  </router-view>
</template>
