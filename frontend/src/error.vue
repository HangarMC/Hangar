<script lang="ts" setup>
import { useRoute } from "vue-router";
import { useHead } from "@vueuse/head";
import { computed } from "vue";
import { NuxtError } from "nuxt/app";
import { useI18n } from "vue-i18n";
import { useSeo } from "~/composables/useSeo";
import Lottie from "~/components/Lottie.vue";
import { useAuthStore } from "~/store/auth";
import { useSettingsStore } from "~/store/useSettingsStore";
import { settingsLog } from "~/lib/composables/useLog";

const props = defineProps<{
  error: NuxtError;
}>();

if (props.error?.message !== "dummy") {
  // keep in sync with app.vue, cause reasons
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
    meta: [{ name: "robots", content: "noindex,nofollow" }], // never index error page
  });
}

// custom
const { t } = useI18n();

const statusCode = computed(() => {
  return Number(props.error.statusCode || 500);
});

const text = computed(() => {
  switch (statusCode.value) {
    case 404:
      return t("error.404");
    case 401:
      return t("error.401");
    case 403:
      return t("error.403");
    default:
      return props.error.message;
  }
});

const title = computed(() => {
  switch (statusCode.value) {
    case 404:
      return t("error.404");
    case 401:
      return t("error.401");
    case 403:
      return t("error.403");
    default:
      return t("error.unknown");
  }
});

if (props.error?.message !== "dummy") {
  console.log("error", text.value, title.value, props.error);
}
useHead(useSeo(title.value, null, useRoute(), null));
</script>

<template>
  <NuxtLayout>
    <div class="flex flex-col items-center justify-center min-h-50vh">
      <template v-if="statusCode === 404">
        <ClientOnly>
          <Lottie src="https://assets9.lottiefiles.com/temp/lf20_dzWAyu.json" />
          <template #fallback>
            <h1 class="text-4xl font-bold">{{ title }}</h1>
            <h2 class="text-xl font-bold">{{ text }}</h2>
          </template>
        </ClientOnly>
      </template>
      <template v-else>
        <h1 class="text-4xl font-bold">{{ title }}</h1>
        <h2 class="text-xl font-bold">{{ text }}</h2>
      </template>
    </div>
  </NuxtLayout>
</template>
