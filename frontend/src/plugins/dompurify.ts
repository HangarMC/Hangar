import { NuxtApp } from "nuxt/app";
import VueDOMPurifyHTML from "vue-dompurify-html";
import { defineNuxtPlugin } from "#imports";

export default defineNuxtPlugin((nuxtApp: NuxtApp) => {
  nuxtApp.vueApp.use(VueDOMPurifyHTML);
});
