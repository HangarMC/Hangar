import path from "node:path";
import VueI18n from "@intlify/vite-plugin-vue-i18n";
import IconsResolver from "unplugin-icons/resolver";
import Icons from "unplugin-icons/vite";
import Components from "unplugin-vue-components/vite";
import { ProxyOptions } from "@nuxtjs-alt/proxy";
import prettier from "./src/lib/plugins/prettier";

const backendHost = process.env.BACKEND_HOST || "http://localhost:8080";
const authHost = process.env.AUTH_HOST || "http://localhost:3001";

// https://v3.nuxtjs.org/api/configuration/nuxt.config
export default defineNuxtConfig({
  imports: {
    autoImport: false,
  },
  srcDir: "src",
  runtimeConfig: {
    authHost,
    backendHost,
  },
  modules: ["nuxt-windicss", "@pinia/nuxt", "@nuxtjs-alt/proxy", "unplugin-icons/nuxt"],
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

      // https://github.com/intlify/bundle-tools/tree/main/packages/vite-plugin-vue-i18n
      VueI18n({
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
  },
  proxy: {
    enableProxy: true,
    proxies: {
      "/api/": defineProxyBackend(),
      "/signup": defineProxyBackend(),
      "/login": defineProxyBackend(),
      "/logout": defineProxyBackend(),
      "/handle-logout": defineProxyBackend(),
      "/refresh": defineProxyBackend(),
      "/invalidate": defineProxyBackend(),
      "/v2/api-docs/": defineProxyBackend(),
      "/robots.txt": defineProxyBackend(),
      "/sitemap.xml": defineProxyBackend(),
      "/global-sitemap.xml": defineProxyBackend(),
      "/*/sitemap.xml": defineProxyBackend(),
      "/statusz": defineProxyBackend(),
      // auth
      "/avatar": defineProxyAuth(),
      "/oauth/logout": defineProxyAuth(),
    },
  },
});

function defineProxyAuth(): ProxyOptions {
  return {
    configure: (proxy, options) => {
      options.target = process.env.AUTH_HOST || process.env.NITRO_AUTH_HOST || "http://localhost:3001";
    },
    changeOrigin: true,
  };
}
function defineProxyBackend(): ProxyOptions {
  return {
    configure: (proxy, options) => {
      options.target = process.env.BACKEND_HOST || process.env.NITRO_BACKEND_HOST || "http://localhost:8080";
    },
    changeOrigin: true,
  };
}
