<script setup lang="ts">
import "./lib/styles/main.css";
import { useHead } from "@vueuse/head";
import { computed } from "vue";
import { useSettingsStore } from "~/store/useSettingsStore";
import { settingsLog } from "~/lib/composables/useLog";
import { useAuthStore } from "~/store/auth";
import { useBackendDataStore } from "~/store/backendData";
import "regenerator-runtime/runtime"; // popper needs this?

// keep in sync with error.vue, cause reasons
const authStore = useAuthStore();
const settingsStore = useSettingsStore();
settingsStore.loadSettingsClient();
settingsStore.setupMobile();
await useBackendDataStore().initBackendData();
settingsLog("render for user", authStore.user?.name, "with darkmode", settingsStore.darkMode);
useHead({
  htmlAttrs: {
    class: computed(() => (settingsStore.darkMode ? "dark" : "light")),
    lang: "en", // TODO load from user locale
  },
  bodyAttrs: {
    class: "background-body text-[#262626] dark:text-[#E0E6f0]",
  },
});
</script>

<template>
  <NuxtLayout>
    <NuxtPage />
  </NuxtLayout>
</template>
