<script lang="ts" setup>
import { onMounted } from "vue";
import { useHead } from "@vueuse/head";
import { useSeo } from "~/composables/useSeo";
import { useI18n } from "vue-i18n";
import { useRoute } from "vue-router";
import { useAuthStore } from "~/store/auth";

const i18n = useI18n();
const route = useRoute();
const authStore = useAuthStore();

onMounted(() => {
  const swaggerUiVersion = "4.13.0";

  const bundle = document.createElement("script");
  bundle.src = `https://unpkg.com/swagger-ui-dist@${swaggerUiVersion}/swagger-ui-bundle.js`;
  bundle.crossOrigin = "";
  document.body.append(bundle);

  const preset = document.createElement("script");
  preset.src = `https://unpkg.com/swagger-ui-dist@${swaggerUiVersion}/swagger-ui-standalone-preset.js`;
  preset.crossOrigin = "";
  document.body.append(preset);

  const script = document.createElement("script");
  // language=JavaScript
  script.innerHTML = `
  window.ui = SwaggerUIBundle({
    url: "/v2/api-docs/",
    dom_id: "#swagger-ui",
    deepLinking: true,
    presets: [SwaggerUIBundle.presets.apis, SwaggerUIBundle.SwaggerUIStandalonePreset],
    plugins: [SwaggerUIBundle.plugins.DownloadUrl],
    layout: "BaseLayout",
    requestInterceptor: (req) => {
      if (!req.loadSpec) {
        if (req.url.startsWith("http://localhost:8080")) {
          req.url = req.url.replace("http://localhost:8080", "http://localhost:3333");
        }
      }
      return req;
    }
  });
  `;

  bundle.onload = () => document.body.append(script);
});

useHead(useSeo(i18n.t("apiDocs.title"), "API Docs for the Hangar REST API", route, null));
</script>

<template>
  <div class="bg-gray-100 dark:(bg-gray-200) rounded-md my-auto mx-2 py-1" lg="w-2/3 min-w-2/3 max-w-2/3">
    <div id="swagger-ui">
      <h1 class="text-3xl text-bold">Hangar API</h1>
      <h2 class="text-2xl">API Docs for the Hangar REST API</h2>
      <p>Loading...</p>
    </div>
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
