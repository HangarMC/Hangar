import { defineStore } from "pinia";
import type { Ref } from "vue";
import { computed, ref, unref } from "vue";
import { useAuthStore } from "~/store/auth";
import { settingsLog } from "~/lib/composables/useLog";
import { useCookies } from "~/composables/useCookies";
import { useSettingsHelper } from "~/lib/composables/useSettingsHelper";
import { useInternalApi } from "~/composables/useApi";

export const useSettingsStore = defineStore("settings", () => {
  settingsLog("defineSettingsStore");
  const darkMode: Ref<boolean> = ref(false);
  const locale: Ref<string> = ref("en");

  const mobile: Ref<boolean> = ref(true); // True cause mobile first!!
  const mobileBreakPoint = 700;

  function toggleDarkMode() {
    darkMode.value = !unref(darkMode);
    settingsLog("darkmode", darkMode.value);
  }

  function enableDarkMode() {
    darkMode.value = true;
    settingsLog("darkmode", darkMode.value);
  }

  function disableDarkMode() {
    darkMode.value = false;
    settingsLog("darkmode", darkMode.value);
  }

  function toggleMobile() {
    mobile.value = !unref(mobile);
  }

  function enableMobile() {
    mobile.value = true;
  }

  function disableMobile() {
    mobile.value = false;
  }

  const authStore = useAuthStore();
  const userData = computed(() => {
    return {
      hasUser: Boolean(authStore.user),
      theme: authStore.user?.theme,
      language: authStore.user?.language,
    };
  });

  async function saveSettings() {
    const store = useAuthStore();
    const data = {
      theme: darkMode.value ? "dark" : "light",
      language: locale.value,
    };
    try {
      await (store.user?.id
        ? useInternalApi("users/" + store.user?.name + "/settings/", true, "post", data)
        : useInternalApi("users/anon/settings/", false, "post", data));
    } catch (e) {
      settingsLog("cant save settings", e);
    }
  }

  const { loadSettingsServer, loadSettingsClient } = useSettingsHelper(
    import.meta.env.SSR,
    userData,
    () => useCookies().get("HANGAR_theme"),
    (loc) => (locale.value = loc),
    (dark) => (darkMode.value = dark),
    saveSettings,
    darkMode
  );

  function setupMobile() {
    if (import.meta.env.SSR) return;
    // For checking if on mobile or not
    if (innerWidth <= mobileBreakPoint && !mobile.value) {
      enableMobile();
    } else if (innerWidth > mobileBreakPoint && mobile) {
      disableMobile();
    }
    addEventListener("resize", () => {
      if (innerWidth <= mobileBreakPoint && !mobile.value) {
        enableMobile();
      } else if (innerWidth > mobileBreakPoint && mobile) {
        disableMobile();
      }
    });
  }

  return {
    darkMode,
    toggleDarkMode,
    enableDarkMode,
    disableDarkMode,
    mobile,
    toggleMobile,
    enableMobile,
    disableMobile,
    mobileBreakPoint,
    loadSettingsServer,
    loadSettingsClient,
    setupMobile,
    locale,
  };
});
