import type { NuxtApp } from "nuxt/app";
import DOMPurify from "dompurify";
import { defineNuxtPlugin } from "#imports";

export default defineNuxtPlugin((nuxtApp: NuxtApp) => {
  const purify = DOMPurify();
  return {
    provide: {
      dompurify: purify,
    },
  };
});
