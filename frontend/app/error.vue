<script lang="ts" setup>
import "./assets/css/main.css";
import type { NuxtError } from "nuxt/app";
import type { HangarNuxtError } from "#shared/types/components/error";
import type { Composer } from "vue-i18n";
import { getActivePinia } from "pinia";

const props = defineProps<{
  error: NuxtError<HangarNuxtError>;
}>();

const route = useRoute();
const pinia = getActivePinia();
const useAppLayout = ref(pinia !== undefined);
let settingsStore: ReturnType<typeof useSettingsStore> | undefined;

if (pinia) {
  try {
    const authStore = useAuthStore(pinia);
    settingsStore = useSettingsStore(pinia);
    await settingsStore.loadSettingsClient();
    settingsLog("render error for user", authStore.user?.name, "with darkmode", settingsStore.darkMode);
  } catch (err) {
    useAppLayout.value = false;
    console.log("cant load themed error page?!", err);
  }
}

useAccentColor();

// custom
let i18n: Composer | undefined;
try {
  i18n = useI18n();
} catch (err) {
  console.log("cant load i18n?!", err);
}

const auth = useAuth;
const animationFailed = ref(false);

const statusCode = computed(() => {
  return Number(props.error.statusCode || 500);
});

const text = computed(() => {
  switch (statusCode.value) {
    case 404:
      return i18n?.t("error.404") || "404";
    case 401:
      return i18n?.t("error.401") || "401";
    case 403:
      return i18n?.t("error.403") || "403";
    default:
      return props.error.message || "An error occurred";
  }
});

const title = computed(() => {
  switch (statusCode.value) {
    case 404:
      return i18n?.t("error.404") || "404";
    case 401:
      return i18n?.t("error.401") || "401";
    case 403:
      return i18n?.t("error.403") || "403";
    default:
      return i18n?.t("error.unknown") || "unknown error";
  }
});

let errorData = props.error?.data;
if (typeof errorData === "string") {
  try {
    errorData = JSON.parse(errorData);
  } catch {
    errorData = undefined;
  }
}
if (errorData?.logErrorMessage !== false) {
  console.log("render error page", text.value, title.value);
}

useHead({
  title,
  htmlAttrs: {
    class: computed(() => (settingsStore?.darkMode ? "dark" : "light")),
    lang: "en",
  },
  bodyAttrs: { class: "background-body text-[#262626] dark:text-[#E0E6f0]" },
  meta: [{ name: "robots", content: "noindex,nofollow" }], // never index error page
});
</script>

<template>
  <NuxtErrorBoundary v-if="useAppLayout">
    <NuxtLayout>
      <div class="flex min-h-50vh flex-col items-center justify-center">
        <template v-if="statusCode === 404">
          <Lottie v-if="!animationFailed" src="https://assets9.lottiefiles.com/temp/lf20_dzWAyu.json" @error="animationFailed = true" />
          <template v-else>
            <h1 class="text-4xl font-bold">{{ title }}</h1>
            <h2 class="text-xl font-bold">{{ text }}</h2>
          </template>
        </template>
        <template v-else>
          <h1 class="text-4xl font-bold">{{ title }}</h1>
          <h2 class="text-xl font-bold">{{ text }}</h2>
          <Button v-if="statusCode === 401" class="mt-2" :to="auth.loginUrl(route.fullPath)">Login</Button>
        </template>
      </div>
    </NuxtLayout>
    <template #error>
      <main class="flex min-h-screen flex-col items-center justify-center gap-3 px-4 text-center">
        <a href="/" class="text-2xl font-bold text-primary">Hangar</a>
        <div>
          <h1 class="text-4xl font-bold">{{ title }}</h1>
          <h2 class="mt-2 text-xl font-bold">{{ text }}</h2>
        </div>
        <a v-if="statusCode === 401" class="rounded bg-primary px-4 py-2 text-white" :href="auth.loginUrl(route.fullPath)">Login</a>
      </main>
    </template>
  </NuxtErrorBoundary>
  <main v-else class="flex min-h-screen flex-col items-center justify-center gap-3 px-4 text-center">
    <a href="/" class="text-2xl font-bold text-primary">Hangar</a>
    <div>
      <h1 class="text-4xl font-bold">{{ title }}</h1>
      <h2 class="mt-2 text-xl font-bold">{{ text }}</h2>
    </div>
    <a v-if="statusCode === 401" class="rounded bg-primary px-4 py-2 text-white" :href="auth.loginUrl(route.fullPath)">Login</a>
  </main>
</template>
