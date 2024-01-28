import DOMPurify from "dompurify";

export default defineNuxtPlugin((nuxtApp) => {
  const purify = DOMPurify();
  return {
    provide: {
      dompurify: purify,
    },
  };
});
