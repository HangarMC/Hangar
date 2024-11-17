import DOMPurify from "dompurify";

export default defineNuxtPlugin((nuxtApp) => {
  const purify = DOMPurify(window);
  return {
    provide: {
      dompurify: purify,
    },
  };
});
