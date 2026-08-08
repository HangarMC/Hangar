import { watch } from "vue";
import { setResponseHeader } from "h3";
import type { H3Event } from "h3";
import localeParser from "accept-language-parser";

export const useSettingsStore = defineStore("settings", () => {
  const themes = ["system", "light", "dark"];
  type ThemeMode = (typeof themes)[number];

  settingsLog("defineSettingsStore");
  const darkMode = ref<boolean>();
  const themeMode = ref<ThemeMode>("system");
  const systemDarkMode = ref(false);
  let watcherSetup = false;
  let systemThemeListenerSetup = false;

  function normalizeThemeMode(theme: string | undefined, fallback: ThemeMode): ThemeMode {
    return theme && themes.includes(theme) ? theme : fallback;
  }

  function updateDarkMode() {
    darkMode.value = themeMode.value === "system" ? systemDarkMode.value : themeMode.value === "dark";
  }

  function setThemeMode(theme: ThemeMode) {
    themeMode.value = theme;
    updateDarkMode();
  }

  function toggleDarkMode() {
    setThemeMode(darkMode.value ? "light" : "dark");
    settingsLog("toggleDarkMode", darkMode.value);
  }

  const authStore = useAuthStore();
  const i18n = useNuxtApp().$i18n;

  async function saveSettings() {
    const data = {
      theme: themeMode.value,
      language: useNuxtApp().$i18n.locale.value,
    };
    try {
      await useInternalApi("users/" + (authStore.user?.name || "anon") + "/settings/", "post", data);
      if (authStore.user) {
        authStore.user.theme = data.theme;
        authStore.user.language = data.language;
      }
    } catch (err) {
      settingsLog("cant save settings", err);
    }
  }

  function setupWatcher() {
    if (watcherSetup) return;
    watcherSetup = true;
    watch([themeMode, () => i18n.locale.value], async (newSettings) => {
      if (import.meta.env.SSR) return;
      if (newSettings[0] === authStore.user?.theme && newSettings[1] === authStore.user?.language) {
        settingsLog("settings did not change, not saving");
        return;
      }
      await saveSettings();
    });
  }

  async function loadSettingsServer(event: H3Event) {
    if (!import.meta.env.SSR) return;
    let newLocale: typeof i18n.locale.value;
    let newThemeMode: ThemeMode;
    if (authStore.user) {
      newLocale = (authStore.user.language || "en") as typeof i18n.locale.value;
      newThemeMode = normalizeThemeMode(authStore.user.theme, "light");
      settingsLog("user is logged in, locale = " + newLocale + ", darkMode = " + darkMode.value);
    } else {
      const acceptLanguageHeader = useRequestHeader("accept-language");
      if (acceptLanguageHeader) {
        const supportedLocales = useNuxtApp().$i18n.availableLocales;
        const pickedLocale = localeParser.pick(supportedLocales, acceptLanguageHeader);
        if (pickedLocale) {
          settingsLog("user is not logged in, picking from locale header, locale = " + pickedLocale, supportedLocales, acceptLanguageHeader);
          newLocale = pickedLocale;
        } else {
          settingsLog("user is not logged in and could not pick locale from header, using default...", supportedLocales, acceptLanguageHeader);
          newLocale = "en";
        }
      } else {
        settingsLog("using default locale cause there was no header...");
        newLocale = "en";
      }

      const cookie = useCookie("HANGAR_theme").value;
      if (cookie) {
        settingsLog("user is not logged in, using theme from cookie", cookie);
        newThemeMode = normalizeThemeMode(cookie, "system");
      } else {
        newThemeMode = "system";
      }
    }

    if (newThemeMode === "system") {
      setResponseHeader(event, "Accept-CH", "Sec-CH-Prefers-Color-Scheme");
      setResponseHeader(event, "Vary", "Sec-CH-Prefers-Color-Scheme");
      setResponseHeader(event, "Critical-CH", "Sec-CH-Prefers-Color-Scheme");
      systemDarkMode.value = useRequestHeader("sec-ch-prefers-color-scheme") === "dark";
    }

    await i18n.loadLocaleMessages(newLocale);
    i18n.locale.value = newLocale;
    themeMode.value = newThemeMode;
    updateDarkMode();
  }

  async function loadSettingsClient() {
    if (import.meta.env.SSR) return;
    setupWatcher();

    const mediaQuery = window.matchMedia("(prefers-color-scheme: dark)");
    systemDarkMode.value = mediaQuery.matches;
    if (!systemThemeListenerSetup) {
      systemThemeListenerSetup = true;
      mediaQuery.addEventListener("change", (event) => {
        systemDarkMode.value = event.matches;
        if (themeMode.value === "system") {
          updateDarkMode();
        }
      });
    }

    const cookie = useCookie("HANGAR_theme").value;
    let newThemeMode: ThemeMode;
    if (authStore.user?.theme) {
      newThemeMode = normalizeThemeMode(authStore.user.theme, "light");
      settingsLog("user is logged in, darkmode = " + darkMode.value);
    } else if (cookie) {
      newThemeMode = normalizeThemeMode(cookie, "system");
      settingsLog("user is not logged in, using cookie, darkmode = " + darkMode.value);
    } else {
      newThemeMode = "system";
      settingsLog("user is not logged in, using media query, darkmode = " + darkMode.value);
    }

    let newLocale = authStore.user?.language as typeof i18n.locale.value;
    if (newLocale) {
      settingsLog("user is logged in, language = " + newLocale);
    } else {
      newLocale = "en";
      settingsLog("user is not logged in, using default language");
    }

    await i18n.loadLocaleMessages(newLocale);
    i18n.locale.value = newLocale;
    themeMode.value = newThemeMode;
    updateDarkMode();
  }

  return {
    darkMode,
    themeMode,
    setThemeMode,
    toggleDarkMode,
    loadSettingsServer,
    loadSettingsClient,
  };
});
