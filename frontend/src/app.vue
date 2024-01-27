<script setup lang="ts">
import "./assets/css/main.css";
// eslint-disable-next-line import/no-unresolved
import "uno.css";
import { useHead } from "@unhead/vue";
import { computed } from "vue";
import { useSettingsStore } from "~/store/useSettingsStore";
import { settingsLog } from "~/composables/useLog";
import { useAuthStore } from "~/store/auth";
import { onErrorCaptured, transformAxiosError, useAccentColor, useRuntimeConfig } from "#imports";

// popper needs this?
import "regenerator-runtime/runtime";

// keep in sync with error.vue, cause reasons
const runtimeConfig = useRuntimeConfig();
const authStore = useAuthStore();
const settingsStore = useSettingsStore();
settingsStore.loadSettingsClient();
settingsStore.setupMobile();
useAccentColor();
settingsLog("render for user", authStore.user?.name, "with darkmode", settingsStore.darkMode);

// for some dum reason useHead will not always replace the "light" from server side with "dark" from client side so we just force it.
if (process.client) {
  document.documentElement.classList.remove("light", "dark");
  document.documentElement.classList.add(settingsStore.darkMode ? "dark" : "light");
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
  if (err?.data?.logErrorMessage === false) {
    return;
  }
  console.log("captured", transformAxiosError(err)); // TODO error handling
});
</script>

<template>
  <div>
    <NuxtLayout>
      <NuxtPage />
    </NuxtLayout>
  </div>
</template>
