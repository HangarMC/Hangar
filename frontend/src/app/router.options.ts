import { RouterConfig } from "@nuxt/schema";

export default {
  scrollBehavior(to, from, savedPosition) {
    if (to.path === from.path) return {};
    return savedPosition
      ? new Promise((resolve) => {
          setTimeout(() => {
            resolve(savedPosition);
          }, 250);
        })
      : { top: 0 };
  },
} as RouterConfig;
