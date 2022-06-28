import { createHead } from "@vueuse/head";
import { createPinia } from "pinia";
import { setupLayouts } from "virtual:generated-layouts";
import generatedRoutes from "virtual:generated-pages";
import viteSSR, { ClientOnly } from "vite-ssr";
import "windi.css";
import App from "~/App.vue";
import { installI18n } from "~/i18n";
import "./styles/main.css";
import { useBackendDataStore } from "~/store/backendData";
import devalue from "@nuxt/devalue";
import { settingsLog } from "~/lib/composables/useLog";
import * as domain from "~/composables/useDomain";

import "regenerator-runtime/runtime"; // popper needs this?

const routes = setupLayouts(generatedRoutes);
// we need to override the path on the error route to have the patch math
const errorRoute = routes.find((r) => r.path === "/error");
if (errorRoute) {
  errorRoute.path = "/:pathMatch(.*)*";
} else {
  console.error("No error route?!");
}

const options: Parameters<typeof viteSSR>["1"] = {
  routes,
  pageProps: {
    passToPage: false,
  },
  transformState(state) {
    return import.meta.env.SSR ? devalue(state) : state;
  },
};

export default viteSSR(App, options, async (ctx) => {
  const { app, initialState, initialRoute, request, response } = ctx;

  app.component(ClientOnly.name, ClientOnly);

  const d = domain.create(request, response);

  const head = createHead();
  const pinia = createPinia();
  app.use(pinia).use(head);
  domain.set("pinia", pinia);

  // install all modules under `modules/`
  for (const module of Object.values(import.meta.globEager("./modules/*.ts"))) {
    await module.install?.(ctx);
  }

  if (!import.meta.env.SSR) {
    pinia.state.value = initialState.pinia;
  }

  settingsLog("Load locale " + pinia.state.value.settings?.locale || "en");
  // Load default language async to avoid bundling all languages
  await installI18n(app, pinia.state.value.settings?.locale || "en");

  // really don't need to do stuff for such meta routes
  if (!initialRoute.fullPath.startsWith("/@vite")) {
    await useBackendDataStore().initBackendData();
  }

  if (import.meta.env.SSR) {
    initialState.pinia = pinia.state.value;
    request.ctx = ctx;
    request.pinia = pinia;
  }
  domain.exit(d);

  return { head };
});
