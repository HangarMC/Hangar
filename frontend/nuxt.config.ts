import IconsResolver from "unplugin-icons/resolver";
// import EslintPlugin from "vite-plugin-eslint";
import Components from "unplugin-vue-components/vite";
import { defineNuxtConfig } from "nuxt/config";
import devtoolsJson from "vite-plugin-devtools-json";

// noinspection ES6PreferShortImport
import { loadLocales } from "./src/i18n/i18n-util";

// https://v3.nuxtjs.org/api/configuration/nuxt.config
export default defineNuxtConfig({
  telemetry: false,
  compatibilityDate: "2024-07-06",
  components: [
    {
      path: "~/components",
      pathPrefix: false,
    },
  ],
  imports: {
    dirs: ["store"],
    presets: [
      {
        from: "@vuelidate/core",
        imports: ["useVuelidate"],
      },
    ],
  },
  app: {
    pageTransition: {
      name: "page",
      mode: "in-out",
    },
  },
  srcDir: "src",
  runtimeConfig: {
    backendHost: "",
    public: {
      host: "",
      allowIndexing: "",
      umamiWebsiteId: "",
      sentry: {
        dsn: "",
        environment: "",
        tracePropagationTargets: [
          "http://localhost:3333",
          "https://hangar.papermc.dev",
          "https://hangar.papermc.io",
          "http://hangar-backend:8080",
          "http://localhost:8080",
        ],
        debug: false,
        tracesSampleRate: 1,
      },
    },
  },
  modules: [
    "@unocss/nuxt",
    "@pinia/nuxt",
    "@vueuse/nuxt",
    "@nuxt/eslint",
    "@nuxtjs/i18n",
    "@sentry/nuxt/module",
    "@nuxtjs/turnstile",
    "floating-vue/nuxt",
    [
      "unplugin-icons/nuxt",
      {
        autoInstall: true,
      },
    ],
    [
      "./src/module/backendData",
      {
        serverUrl: process.env.BACKEND_DATA_HOST,
      },
    ],
    "./src/module/componentsFix",
  ],
  i18n: {
    vueI18n: "../src/i18n/i18n.config.ts",
    strategy: "no_prefix",
    lazy: true,
    langDir: "../src/i18n/locales/processed",
    defaultLocale: "en",
    locales: loadLocales(),
    detectBrowserLanguage: false,
    compilation: {
      strictMessage: false,
    },
    bundle: {
      runtimeOnly: true,
      dropMessageCompiler: true,
      optimizeTranslationDirective: false,
    },
  },
  vite: {
    plugins: [
      // https://github.com/antfu/unplugin-vue-components
      Components({
        // auto import icons
        resolvers: [
          // https://github.com/antfu/vite-plugin-icons
          IconsResolver({
            prefix: "icon",
            enabledCollections: ["mdi"],
          }),
        ],
        dts: "types/generated/icons.d.ts",
      }),
      // https://github.com/ChromeDevTools/vite-plugin-devtools-json
      devtoolsJson(),
    ],
    ssr: {
      // Workaround until they support native ESM
      noExternal: ["vue3-popper"],
    },
    css: {
      preprocessorOptions: {
        scss: {
          silenceDeprecations: ["legacy-js-api"],
        },
      },
    },
  },
  experimental: {
    writeEarlyHints: false,
    componentIslands: true,
    asyncContext: true,
    typedPages: true,
  },
  typescript: {
    typeCheck: "build",
    tsConfig: {
      include: ["./types/typed-router.d.ts"],
      compilerOptions: {
        types: ["bun"],
        strictNullChecks: true,
        noUnusedLocals: true,
      },
    },
  },
  devtools: {
    enabled: true,

    timeline: {
      enabled: true,
    },
  },
  nitro: {
    preset: "bun",
    compressPublicAssets: true,
    timing: false,
  },
  sourcemap: {
    server: true,
    client: true,
  },
  vue: {
    compilerOptions: {
      isCustomElement: (tag) => ["lottie-player", "rapi-doc"].includes(tag),
    },
  },
  // cache statics for a year
  routeRules: {
    "/_nuxt/**": cache(),
    "/**/*.js": cache(),
    "/**/*.mjs": cache(),
    "/**/*.css": cache(),
    "/**/*.json": cache(),
    "/**/*.xml": cache(),
    "/**/*.svg": cache(),
  },
});

function cache() {
  return { headers: { "Cache-Control": "max-age=31536000, immutable" } };
}
