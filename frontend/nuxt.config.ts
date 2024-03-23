import path from "node:path";
import VueI18nVitePlugin from "@intlify/unplugin-vue-i18n/vite";
import IconsResolver from "unplugin-icons/resolver";
// import EslintPlugin from "vite-plugin-eslint";
import Components from "unplugin-vue-components/vite";
import type { ProxyOptions } from "@nuxt-alt/proxy";
import { defineNuxtConfig } from "nuxt/config";

const backendHost = process.env.BACKEND_HOST || "http://localhost:8080";
const local = true; // set to false if backendData should be fetched from staging. You might need to hard reload (Ctrl+F5) the next page you're on when changing this value
const backendDataHost = process.env.BACKEND_DATA_HOST || (local ? "http://localhost:8080" : "https://hangar.papermc.dev");
const allowIndexing = process.env.HANGAR_ALLOW_INDEXING || "true";

// https://v3.nuxtjs.org/api/configuration/nuxt.config
export default defineNuxtConfig({
  telemetry: false,
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
        from: "vue-i18n",
        imports: ["useI18n", "createI18n"],
      },
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
    },
  },
  modules: [
    "@unocss/nuxt",
    "@pinia/nuxt",
    "@nuxt-alt/proxy",
    "unplugin-icons/nuxt",
    "@vueuse/nuxt",
    [
      "unplugin-icons/nuxt",
      {
        autoInstall: true,
      },
    ],
    // "@unlighthouse/nuxt",
    [
      "./src/module/backendData",
      {
        serverUrl: backendDataHost,
      },
    ],
    "./src/module/componentsFix",
  ],
  build: {
    transpile: ["vue-i18n"],
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

      // https://github.com/intlify/bundle-tools/tree/main/packages/unplugin-vue-i18n
      VueI18nVitePlugin({
        runtimeOnly: false, // TODO figure out if using the message compiler and including all locales is better? maybe we can still treeshake locales with runtimeOnly?
        include: [path.resolve(__dirname, "src/locales/*.json")],
      }),

      // TODO this seems slow as fuck, wtf
      // EslintPlugin({
      //  fix: true,
      // }),
    ],
    ssr: {
      // Workaround until they support native ESM
      noExternal: ["vue3-popper"],
    },
  },
  experimental: {
    writeEarlyHints: false,
    componentIslands: true,
    asyncContext: true,
    typedPages: true,
  },
  typescript: {
    // typeCheck: "build", // TODO enable typechecking on build
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
    preset: "node-cluster",
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
  proxy: {
    proxies: {
      // for performance, these should be mirrored in ingress
      "/api/": defineProxyBackend(),
      "/signup": defineProxyBackend(),
      "/login": defineProxyBackend(),
      "/logout": defineProxyBackend(),
      "/handle-logout": defineProxyBackend(),
      "/refresh": defineProxyBackend(),
      "/invalidate": defineProxyBackend(),
      "/v3/api-docs": defineProxyBackend(),
      "/robots.txt": defineProxyBackend(),
      "/sitemap.xml": defineProxyBackend(),
      "/global-sitemap.xml": defineProxyBackend(),
      "^/.*/sitemap.xml": defineProxyBackend(),
      "/statusz": defineProxyBackend(),
    },
  },
  // cache statics for a year
  routeRules: {
    "/_nuxt/**": { headers: { "Cache-Control": "max-age=31536000, immutable" } },
    "/**/*.js": { headers: { "Cache-Control": "max-age=31536000, immutable" } },
    "/**/*.mjs": { headers: { "Cache-Control": "max-age=31536000, immutable" } },
    "/**/*.css": { headers: { "Cache-Control": "max-age=31536000, immutable" } },
    "/**/*.json": { headers: { "Cache-Control": "max-age=31536000, immutable" } },
    "/**/*.xml": { headers: { "Cache-Control": "max-age=31536000, immutable" } },
    "/**/*.svg": { headers: { "Cache-Control": "max-age=31536000, immutable" } },
  },
});

function defineProxyBackend(): ProxyOptions {
  return {
    configure: (proxy, options) => {
      options.target = process.env.BACKEND_HOST || process.env.NITRO_BACKEND_HOST || "http://localhost:8080";
    },
    changeOrigin: true,
  };
}
