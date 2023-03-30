import { RouterConfig } from "@nuxt/schema";

export default {
  scrollBehavior(to, from, savedPosition) {
    if (to.path === from.path) {
      return {};
    }

    const anchorIndex = to.fullPath.indexOf("#");
    if (anchorIndex !== -1) {
      return {};
    }

    return savedPosition
      ? new Promise((resolve) => {
          setTimeout(() => {
            resolve(savedPosition);
          }, 250);
        })
      : { top: 0 };
  },
} as RouterConfig;
