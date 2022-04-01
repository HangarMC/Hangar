<script lang="ts" setup>
import { onMounted } from "vue";
import { SwaggerConfigs, SwaggerUIBundle } from "swagger-ui-dist";
import { useHead } from "@vueuse/head";
import { useSeo } from "~/composables/useSeo";
import { useI18n } from "vue-i18n";
import { useRoute } from "vue-router";
import { useAuthStore } from "~/store/auth";

declare global {
  interface Window {
    ui: SwaggerUIBundle;
  }
}

const i18n = useI18n();
const route = useRoute();
const authStore = useAuthStore();

onMounted(() => {
  window.ui = SwaggerUIBundle({
    url: "/v2/api-docs/",
    dom_id: "#swagger-ui",
    deepLinking: true,
    presets: [SwaggerUIBundle.presets.apis, SwaggerUIBundle.SwaggerUIStandalonePreset],
    plugins: [SwaggerUIBundle.plugins.DownloadUrl],
    layout: "BaseLayout",
    requestInterceptor: (req) => {
      if (!req.loadSpec) {
        req.headers.authorization = "HangarAuth " + authStore.token;
        if (req.url.startsWith("http://localhost:8080")) {
          req.url = req.url.replace("http://localhost:8080", "http://localhost:3333");
        }
      }
      return req;
    },
  } as SwaggerConfigs);
});

useHead(useSeo(i18n.t("apiDocs.title"), null, route, null));
</script>

<template>
  <div class="bg-gray-100 dark:(bg-gray-200) rounded-md my-auto mx-2 py-1" lg="w-2/3 min-w-2/3 max-w-2/3">
    <div id="swagger-ui" />
  </div>
</template>

<style lang="scss">
@import "swagger-ui-dist/swagger-ui.css";

.swagger-ui {
  .topbar .download-url-wrapper,
  .info hgroup.main a {
    display: none;
  }
  .wrapper .info {
    background-color: unset !important;
    border-color: unset !important;
    margin: 2rem 0;
    .title small pre {
      background-color: unset;
      border: unset;
    }
    .description h2 {
      padding-top: 1.5rem;
      margin: 1.5rem 0 0;
      border-top: 3px solid #333333;
    }
    .scheme-container {
      border-top: 1px solid rgba(0, 0, 0, 0.15);
    }
    .markdown {
      min-height: 0;
    }
  }
}
.model-container,
.responses-inner {
  overflow-x: auto;
}
</style>
