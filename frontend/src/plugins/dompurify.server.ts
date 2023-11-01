import type { NuxtApp } from "nuxt/app";
import { JSDOM } from "jsdom";
import DOMPurify from "dompurify";
import { defineNuxtPlugin } from "#imports";

export default defineNuxtPlugin((nuxtApp: NuxtApp) => {
  const window = new JSDOM("").window;
  const dompurify = DOMPurify(window);

  return {
    provide: {
      dompurify,
    },
  };
});
