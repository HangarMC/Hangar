<script lang="ts" setup>
import type { NuxtError } from "nuxt/app";
import type { HangarNuxtError } from "~/types/components/error";

const props = defineProps<{
  error: NuxtError<HangarNuxtError>;
}>();

const route = useRoute();

if (!(props.error?.data?.dummyError === true)) {
  // keep in sync with app.vue, cause reasons
  const authStore = useAuthStore();
  const settingsStore = useSettingsStore();
  await settingsStore.loadSettingsClient();
  useAccentColor();
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
let i18n: ReturnType<typeof useI18n>;
try {
  i18n = useI18n();
} catch (err) {
  console.log("cant load i18n?!", err);
}

const auth = useAuth;

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
      return props.error.message;
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

if (
  (typeof props.error?.data === "string" && JSON.parse(props.error?.data).logErrorMessage !== false) ||
  (typeof props.error?.data !== "string" && props.error?.data?.logErrorMessage !== false)
) {
  console.log("render error page", text.value, title.value);
}
try {
  useSeo(computed(() => ({ title: title.value, route })));
} catch (err) {
  console.log("seo error?!", err);
}
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
        <Button v-if="statusCode === 401" class="mt-2" :to="auth.loginUrl(route.fullPath)">Login</Button>
      </template>
    </div>
  </NuxtLayout>
</template>
