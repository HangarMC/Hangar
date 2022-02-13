import "windi.css";
import "./styles/main.css";
import viteSSR, { ClientOnly } from "vite-ssr";
import { createHead } from "@vueuse/head";
import generatedRoutes from "virtual:generated-pages";
import { setupLayouts } from "virtual:generated-layouts";
import { createPinia } from "pinia";
import App from "~/App.vue";
import { installI18n } from "~/i18n";

const routes = setupLayouts(generatedRoutes);

const options: Parameters<typeof viteSSR>["1"] = {
  routes,
  pageProps: {
    passToPage: false,
  },
};

export default viteSSR(App, options, async (ctx) => {
  // install all modules under `modules/`
  Object.values(import.meta.globEager("./modules/*.ts")).map((i) => i.install?.(ctx));

  const { app, initialState } = ctx;

  app.component(ClientOnly.name, ClientOnly);

  const head = createHead();
  const pinia = createPinia();
  app.use(pinia).use(head);

  // Load language async to avoid bundling all languages
  // TODO would be cool to load user locale here I guess?
  await installI18n(app, "en");

  if (import.meta.env.SSR) {
    initialState.pinia = pinia.state.value;
  } else {
    pinia.state.value = initialState.pinia;
  }

  return { head };
});
