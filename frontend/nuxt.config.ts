import path from "node:path";
import VueI18nVitePlugin from "@intlify/unplugin-vue-i18n/vite";
import IconsResolver from "unplugin-icons/resolver";
import Icons from "unplugin-icons/vite";
import Components from "unplugin-vue-components/vite";
import type { ProxyOptions } from "@nuxt-alt/proxy";
import { defineNuxtConfig } from "nuxt/config";
import prettier from "./src/vite/prettier";
import unocss from "./unocss.config";

const backendHost = process.env.BACKEND_HOST || "http://localhost:8080";
const local = true; // set to false if backendData should be fetched from staging. You might need to hard reload (Ctrl+F5) the next page you're on when changing this value
const backendDataHost = process.env.BACKEND_DATA_HOST || (local ? "http://localhost:8080" : "https://hangar.papermc.dev");
const allowIndexing = process.env.HANGAR_ALLOW_INDEXING || "true";

// https://v3.nuxtjs.org/api/configuration/nuxt.config
export default defineNuxtConfig({
  imports: {
    autoImport: false,
  },
  srcDir: "src",
  runtimeConfig: {
    backendHost,
    public: {
      allowIndexing,
    },
  },
  unocss,
  modules: [
    "@unocss/nuxt",
    "@pinia/nuxt",
    "@nuxt-alt/proxy",
    "unplugin-icons/nuxt",
    // "@unlighthouse/nuxt",
    [
      "./src/module/backendData",
      {
        serverUrl: backendDataHost,
      },
    ],
  ],
  build: {
    transpile: ["vue-i18n"],
  },
  vite: {
    plugins: [
      // https://github.com/antfu/unplugin-vue-components
      Components({
        // we don't want to import components, just icons
        dirs: ["none"],
        // auto import icons
        resolvers: [
          // https://github.com/antfu/vite-plugin-icons
          IconsResolver({
            componentPrefix: "icon",
            enabledCollections: ["mdi"],
          }),
        ],
        dts: "types/generated/icons.d.ts",
      }),

      // https://github.com/antfu/unplugin-icons
      Icons({
        autoInstall: true,
      }),

      // https://github.com/intlify/bundle-tools/tree/main/packages/unplugin-vue-i18n
      VueI18nVitePlugin({
        runtimeOnly: false, // TODO figure out if using the message compiler and including all locales is better? maybe we can still treeshake locales with runtimeOnly?
        include: [path.resolve(__dirname, "src/locales/*.json")],
      }),

      // TODO fix this
      // EslintPlugin({
      //   fix: true,
      // }),
      prettier(),
    ],
    ssr: {
      // Workaround until they support native ESM
      noExternal: ["vue3-popper"],
    },
  },
  experimental: {
    writeEarlyHints: false,
    componentIslands: true,
  },
  typescript: {
    // typeCheck: "build", // TODO enable typechecking on build
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
      isCustomElement: (tag) => ["lottie-player"].includes(tag),
    },
  },
  proxy: {
    enableProxy: true,
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
