<script setup lang="ts">
import "./lib/styles/main.css";
// eslint-disable-next-line import/no-unresolved
import "uno.css";
import { useHead } from "@vueuse/head";
import { computed } from "vue";
import { useSettingsStore } from "~/store/useSettingsStore";
import { settingsLog } from "~/lib/composables/useLog";
import { useAuthStore } from "~/store/auth";
import { onErrorCaptured, transformAxiosError, useRoute, useRuntimeConfig } from "#imports";

// popper needs this?
import "regenerator-runtime/runtime";

const route = useRoute();

// keep in sync with error.vue, cause reasons
const runtimeConfig = useRuntimeConfig();
const authStore = useAuthStore();
const settingsStore = useSettingsStore();
settingsStore.loadSettingsClient();
settingsStore.setupMobile();
settingsLog("render for user", authStore.user?.name, "with darkmode", settingsStore.darkMode);
useHead({
  htmlAttrs: {
    class: computed(() => (settingsStore.darkMode ? "dark" : "light")),
    lang: "en", // TODO load from user locale
  },
  bodyAttrs: {
    class: "background-body text-[#262626] dark:text-[#E0E6f0]",
  },
  meta: [{ name: "robots", content: runtimeConfig.public.allowIndexing === "false" ? "noindex,nofollow" : "index,follow" }],
});

onErrorCaptured((err) => {
  console.log("captured", transformAxiosError(err)); // TODO error handling
});

const pageKey = computed(() => {
  if (route?.params?.user && route?.params?.project) {
    return route.params.user + "-" + route.params.project;
  } else if (route?.params?.user) {
    return route.params.user;
  } else {
    return route.path;
  }
});
</script>

<template>
  <NuxtLayout>
    <NuxtPage :page-key="pageKey" />
  </NuxtLayout>
</template>
