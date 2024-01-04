<script lang="ts" setup>
import "rapidoc";
import { ref, watch } from "vue";
import { useSettingsStore } from "~/store/useSettingsStore";

const settings = useSettingsStore();

const dom = ref<HTMLElement>();

watch(dom, () => {
  if (!dom.value) {
    return;
  }

  dom.value?.addEventListener("spec-loaded", () => {
    const navBar = dom.value.shadowRoot?.querySelector<HTMLElement>(".nav-bar");
    if (navBar) {
      // set the height to auto so that max-content works
      navBar.style.height = "auto";
    }
  });
});
</script>

<template>
  <rapi-doc
    ref="dom"
    spec-url="/v3/api-docs"
    render-style="focused"
    allow-server-selection="false"
    allow-spec-url-load="false"
    allow-spec-file-load="false"
    show-header="false"
    show-method-in-nav-bar="as-colored-text"
    use-path-in-nav-bar="true"
    server-url="/"
    default-api-server="/"
    default-schema-tab="schema"
    schema-description-expanded="true"
    show-curl-before-try="true"
    heading-text="Hangar API"
    :theme="settings.darkMode ? 'dark' : 'light'"
  >
  </rapi-doc>
</template>

<style lang="scss">
rapi-doc {
  border-radius: 0.375rem;
  height: max-content;
}
</style>
