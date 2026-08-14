import type { RouterHistory, RouterOptions } from "vue-router";

type RouterConfig = Partial<Omit<RouterOptions, "history" | "routes">> & {
  history?: (baseURL?: string) => RouterHistory;
  routes?: (_routes: RouterOptions["routes"]) => RouterOptions["routes"];
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

    // scrolling before the old page fades makes it visually jump; delays assume transitions.css
    const delay = savedPosition ? 250 : 100;
    if (window.matchMedia("(prefers-reduced-motion: reduce)").matches) {
      return savedPosition ?? { top: 0 };
    }
    return new Promise((resolve) => {
      setTimeout(() => resolve(savedPosition ?? { top: 0 }), delay);
    });
  },
} as RouterConfig;
