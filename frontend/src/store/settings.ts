export const useSettingsStore = defineStore("settings", () => {
  settingsLog("defineSettingsStore");
  const darkMode = ref(false);
  const locale = ref("en");

  const mobile: Ref<boolean> = ref(true); // True cause mobile first!!
  const mobileBreakPoint = 700;

  function toggleDarkMode() {
    darkMode.value = !unref(darkMode);
    settingsLog("toggleDarkMode", darkMode.value);
  }

  function enableDarkMode() {
    darkMode.value = true;
    settingsLog("enableDarkMode", darkMode.value);
  }

  function disableDarkMode() {
    darkMode.value = false;
    settingsLog("disableDarkMode", darkMode.value);
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
      await useInternalApi("users/" + (store.user?.name || "anon") + "/settings/", "post", data);
    } catch (e) {
      settingsLog("cant save settings", e);
    }
  }

  const { loadSettingsServer, loadSettingsClient } = useSettingsHelper(
    import.meta.env.SSR,
    userData,
    () => useCookie("HANGAR_theme").value,
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
