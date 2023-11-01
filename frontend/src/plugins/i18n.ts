import type { NuxtApp } from "nuxt/app";
import { defineNuxtPlugin } from "#imports";
import { installI18n } from "~/i18n";
import { settingsLog } from "~/composables/useLog";

export default defineNuxtPlugin(async (nuxtApp: NuxtApp) => {
  // TODO load from settings
  settingsLog("Load locale " + "en");
  await installI18n(nuxtApp.vueApp, "en");
});
