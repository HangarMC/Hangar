<script setup lang="ts">
import "./assets/css/main.css";
import type { HangarNuxtError } from "#shared/types/components/error";
import { identify } from "~/composables/useTracking";

// keep in sync with error.vue, cause reasons
const runtimeConfig = useRuntimeConfig();
const authStore = useAuthStore();
const settingsStore = useSettingsStore();
await settingsStore.loadSettingsClient();
useAccentColor();
settingsLog("render for user", authStore.user?.name, "with darkmode", settingsStore.darkMode);
identify();

// for some dum reason useHead will not always replace the "light" from server side with "dark" from client side so we just force it.
if (import.meta.client) {
  document.documentElement.classList.remove("light", "dark");
  document.documentElement.classList.add(settingsStore.darkMode ? "dark" : "light");

  useDebug();

  onNuxtReady(() => nextTick(() => (window.hangarLoaded = true)));
}

useHead({
  htmlAttrs: {
    class: computed(() => (settingsStore.darkMode ? "dark" : "light")),
    lang: useI18n().locale,
  },
  bodyAttrs: {
    class: "background-body text-[#262626] dark:text-[#E0E6f0]",
  },
  meta: [{ name: "robots", content: runtimeConfig.public.allowIndexing === "false" ? "noindex,nofollow" : "index,follow" }],
  script: [
    runtimeConfig.public.umamiWebsiteId && {
      src: "https://trk.papermc.io/api/init",
      defer: true,
      "data-website-id": runtimeConfig.public.umamiWebsiteId,
    },
  ],
});

onErrorCaptured((err) => {
  if (isNuxtError<HangarNuxtError>(err) && err?.data?.logErrorMessage === false) {
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
