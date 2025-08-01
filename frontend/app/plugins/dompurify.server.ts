import { JSDOM } from "jsdom";
import DOMPurify from "dompurify";

export default defineNuxtPlugin((nuxtApp) => {
  const window = new JSDOM("").window;
  const dompurify = DOMPurify(window);

  return {
    provide: {
      dompurify,
    },
  };
});
