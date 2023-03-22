<script setup lang="ts">
import "./lib/styles/main.css";
// eslint-disable-next-line import/no-unresolved
import "uno.css";
import { useHead } from "@vueuse/head";
import { computed, ref } from "vue";
import { RouteLocationNormalized } from "vue-router";
import { useSettingsStore } from "~/store/useSettingsStore";
import { settingsLog } from "~/lib/composables/useLog";
import { useAuthStore } from "~/store/auth";
import { onErrorCaptured, transformAxiosError, useRoute, useRouter, useRuntimeConfig, watch } from "#imports";

// popper needs this?
import "regenerator-runtime/runtime";

const route = useRoute();
console.log("app.vue");

// keep in sync with error.vue, cause reasons
const runtimeConfig = useRuntimeConfig();
const authStore = useAuthStore();
const settingsStore = useSettingsStore();
settingsStore.loadSettingsClient();
settingsStore.setupMobile();
settingsLog("render for user", authStore.user?.name, "with darkmode", settingsStore.darkMode);

// for some dum reason useHead will not always replace the "light" from server side with "dark" from client side so we just force it.
if (process.client) {
  document.documentElement.className = settingsStore.darkMode ? "dark" : "light";
}

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
  if (err?.message === "dummy") {
    return;
  }
  console.log("captured", transformAxiosError(err)); // TODO error handling
});

const pageKey = ref("");
const router = useRouter();
router.beforeEach((route) => {
  updatePageKey(route);
});
updatePageKey(route);
function updatePageKey(route: RouteLocationNormalized) {
  console.log("update page key", pageKey.value);
  if (route?.params?.user && route?.params?.project) {
    pageKey.value = route.params.user + "-" + route.params.project;
  } else if (route?.params?.user) {
    pageKey.value = route.params.user + "";
  } else {
    pageKey.value = route.path;
  }
  console.log("new key", pageKey.value);
}
</script>

<template>
  <NuxtLayout>
    {{ pageKey }}
    <NuxtPage />
  </NuxtLayout>
</template>
