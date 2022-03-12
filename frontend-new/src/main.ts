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

const routes = setupLayouts(generatedRoutes);

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
  // install all modules under `modules/`
  Object.values(import.meta.globEager("./modules/*.ts")).map((i) => i.install?.(ctx));

  const { app, initialState, initialRoute } = ctx;

  app.component(ClientOnly.name, ClientOnly);

  const head = createHead();
  const pinia = createPinia();
  app.use(pinia).use(head);

  // Load language async to avoid bundling all languages
  // TODO would be cool to load user locale here I guess?
  await installI18n(app, "en");

  if (!import.meta.env.SSR) {
    pinia.state.value = initialState.pinia;
  }

  // really don't need to do stuff for such meta routes
  if (!initialRoute.fullPath.startsWith("/@vite")) {
    await useBackendDataStore().initBackendData();
  }

  if (import.meta.env.SSR) {
    initialState.pinia = pinia.state.value;
  }

  return { head };
});
