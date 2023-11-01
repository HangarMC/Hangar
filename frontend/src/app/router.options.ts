// import { RouterOptions } from "@nuxt/schema";
import type { RouterHistory, RouterOptions } from "vue-router";

type RouterConfig = Partial<Omit<RouterOptions, "history" | "routes">> & {
  history?: (baseURL?: string) => RouterHistory;
  routes?: (_routes: RouterOptions$1["routes"]) => RouterOptions$1["routes"];
  hashMode?: boolean;
};

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
