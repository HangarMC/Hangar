import { watch } from "vue";
import { setResponseHeader } from "h3";
import type { H3Event } from "h3";
import localeParser from "accept-language-parser";

export const useSettingsStore = defineStore("settings", () => {
  settingsLog("defineSettingsStore");
  const darkMode = ref<boolean>();

  function toggleDarkMode() {
    darkMode.value = !unref(darkMode);
    settingsLog("toggleDarkMode", darkMode.value);
  }

  const authStore = useAuthStore();
  const i18n = useNuxtApp().$i18n;

  async function saveSettings() {
    const data = {
      theme: darkMode.value ? "dark" : "light",
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
    watch([darkMode, () => i18n.locale.value], async (newSettings, oldSettings) => {
      if (import.meta.env.SSR) return;
      if (newSettings[0] === (authStore.user?.theme === "dark") && newSettings[1] === authStore.user?.language) {
        settingsLog("settings did not change, not saving");
        return;
      }
      await saveSettings();
    });
  }

  async function loadSettingsServer(event: H3Event) {
    if (!import.meta.env.SSR) return;
    let newLocale: typeof i18n.locale.value;
    let newDarkMode: boolean;
    if (authStore.user) {
      newLocale = (authStore.user.language || "en") as typeof i18n.locale.value;
      newDarkMode = (authStore.user.theme || "light") === "dark";
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
        newDarkMode = cookie === "dark";
      } else {
        setResponseHeader(event, "Accept-CH", "Sec-CH-Prefers-Color-Scheme");
        setResponseHeader(event, "Vary", "Sec-CH-Prefers-Color-Scheme");
        setResponseHeader(event, "Critical-CH", "Sec-CH-Prefers-Color-Scheme");
        const themeHeader = useRequestHeader("sec-ch-prefers-color-scheme");
        if (themeHeader) {
          settingsLog("user is not logged in, using theme from header", themeHeader);
          newDarkMode = themeHeader === "dark";
        } else {
          settingsLog("user is not logged in and we got no theme header, using default...", themeHeader);
          newDarkMode = false;
        }
      }
    }

    await i18n.loadLocaleMessages(newLocale);
    i18n.locale.value = newLocale;
    darkMode.value = newDarkMode;
  }

  async function loadSettingsClient() {
    if (import.meta.env.SSR) return;
    setupWatcher();

    const cookie = useCookie("HANGAR_theme").value;
    let newDarkMode;
    if (authStore.user?.theme) {
      newDarkMode = authStore.user?.theme === "dark";
      settingsLog("user is logged in, darkmode = " + darkMode.value);
    } else if (cookie) {
      newDarkMode = cookie === "dark";
      settingsLog("user is not logged in, using cookie, darkmode = " + darkMode.value);
    } else {
      newDarkMode = window.matchMedia("(prefers-color-scheme: dark)").matches;
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
    darkMode.value = newDarkMode;
  }

  return {
    darkMode,
    toggleDarkMode,
    loadSettingsServer,
    loadSettingsClient,
  };
});
