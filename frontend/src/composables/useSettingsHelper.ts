import localeParser from "accept-language-parser";
import { type ComputedRef, type Ref, watch } from "vue";
import type { H3Event } from "h3";

export function useSettingsHelper(
  ssr: boolean,
  userData: ComputedRef<{ hasUser: boolean; theme?: string; language?: string }>,
  getThemeCookie: () => string | undefined | null,
  setLocale: (locale: string) => void,
  setTheme: (dark: boolean) => void,
  saveSettings: () => Promise<void>,
  darkMode: Ref<boolean>
) {
  function loadSettingsServer(event: H3Event) {
    if (!ssr) return;
    let newLocale;
    let theme;
    if (userData.value.hasUser) {
      newLocale = userData.value.language || "en";
      theme = userData.value.theme || "white";
      settingsLog("user is logged in, locale = " + newLocale + ", theme = " + theme);
    } else {
      const acceptLanguageHeader = useRequestHeader("accept-language");
      if (acceptLanguageHeader) {
        const supportedLocales = useNuxtApp().$i18n.availableLocales;
        console.log("supported locales", supportedLocales);
        const pickedLocale = localeParser.pick(supportedLocales, acceptLanguageHeader);
        if (!pickedLocale) {
          settingsLog("user is not logged in and could not pick locale from header, using default...", supportedLocales, acceptLanguageHeader);
          newLocale = "en";
        } else {
          settingsLog("user is not logged in, picking from locale header, locale = " + pickedLocale, supportedLocales, acceptLanguageHeader);
          newLocale = pickedLocale;
        }
      } else {
        settingsLog("using default locale cause there was no header...");
        newLocale = "en";
      }

      const cookie = getThemeCookie();
      if (cookie) {
        settingsLog("user is not logged in, using theme from cookie", cookie);
        theme = cookie === "dark" ? "dark" : "light";
      } else {
        setResponseHeader(event, "Accept-CH", "Sec-CH-Prefers-Color-Scheme");
        setResponseHeader(event, "Vary", "Sec-CH-Prefers-Color-Scheme");
        setResponseHeader(event, "Critical-CH", "Sec-CH-Prefers-Color-Scheme");
        const themeHeader = useRequestHeader("sec-ch-prefers-color-scheme");
        if (themeHeader) {
          settingsLog("user is not logged in, using theme from header", themeHeader);
          theme = themeHeader === "dark" ? "dark" : "light";
        } else {
          settingsLog("user is not logged in and we got no theme header, using default...", themeHeader);
        }
      }
    }

    setLocale(newLocale);
    setTheme(theme === "dark");
  }

  watch(darkMode, async (newMode) => {
    if (ssr) return;
    await saveSettings();
  });

  function loadSettingsClient() {
    if (ssr) return;
    let darkMode: boolean;

    const cookie = getThemeCookie();
    if (userData.value && userData.value.theme) {
      darkMode = userData.value.theme === "dark";
      settingsLog("user is logged in, darkmode = " + darkMode);
    } else if (cookie) {
      darkMode = cookie === "dark";
      settingsLog("user is not logged in, using cookie, darkmode = " + darkMode);
    } else {
      darkMode = window.matchMedia("(prefers-color-scheme: dark)").matches;
      settingsLog("user is not logged in, using media query, darkmode = " + darkMode);
    }

    setTheme(darkMode);
  }

  return { loadSettingsServer, loadSettingsClient };
}
