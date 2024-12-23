import IconsResolver from "unplugin-icons/resolver";
// import EslintPlugin from "vite-plugin-eslint";
import Components from "unplugin-vue-components/vite";
import { defineNuxtConfig } from "nuxt/config";

// noinspection ES6PreferShortImport
import { loadLocales } from "./src/i18n/i18n-util";

const backendHost = process.env.BACKEND_HOST || "http://localhost:8080";
const local = true; // set to false if backendData should be fetched from staging. You might need to hard reload (Ctrl+F5) the next page you're on when changing this value
const backendDataHost = process.env.BACKEND_DATA_HOST || (local ? "http://localhost:8080" : "https://hangar.papermc.dev");
const allowIndexing = process.env.HANGAR_ALLOW_INDEXING || "true";
const sentryDSN = process.env.SENTRY_DSN || "https://801c6e3ec217457e94b8d360e861242d@o4504989579804672.ingest.sentry.io/4504989584850944";
const sentryEnvironment = process.env.SENTRY_ENV || "local";
const umamiWebsiteId = process.env.UMAMI_WEBSIDE_ID || "8530c6aa-8fb3-4421-8503-4e8de6bc19ef"; // hangar local;

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
    backendHost,
    public: {
      allowIndexing,
      umamiWebsiteId,
      sentry: {
        dsn: sentryDSN,
        environment: sentryEnvironment,
      },
    },
  },
  modules: [
    "@unocss/nuxt",
    "@pinia/nuxt",
    "unplugin-icons/nuxt",
    "@vueuse/nuxt",
    "@nuxt/eslint",
    "@nuxtjs/i18n",
    "@sentry/nuxt/module",
    "@nuxtjs/turnstile",
    [
      "unplugin-icons/nuxt",
      {
        autoInstall: true,
      },
    ],
    [
      "./src/module/backendData",
      {
        serverUrl: backendDataHost,
      },
    ],
    "./src/module/componentsFix",
  ],
  i18n: {
    vueI18n: "./src/i18n/i18n.config.ts",
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
    "/api/**": proxy("/api/**"),
    "/v3/**": proxy("/v3/**"),
  },
});

function cache() {
  return { headers: { "Cache-Control": "max-age=31536000, immutable" } };
}

function proxy(path: string) {
  return {
    proxy: (process.env.BACKEND_HOST || process.env.NITRO_BACKEND_HOST || "http://localhost:8080") + path,
  };
}
