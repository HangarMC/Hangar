import { installI18n } from "~/i18n";

export default defineNuxtPlugin(async (nuxtApp) => {
  // TODO load from settings
  settingsLog("Load locale " + "en");
  await installI18n(nuxtApp.vueApp, "en");
});
