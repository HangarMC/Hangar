<script lang="ts" setup>
import "rapidoc";

const settings = useSettingsStore();

const dom = useTemplateRef<HTMLElement>("dom");

watch(dom, () => {
  if (!dom.value) {
    return;
  }

  dom.value?.addEventListener("spec-loaded", () => {
    const navBar = dom.value?.shadowRoot?.querySelector<HTMLElement>(".nav-bar");
    if (navBar) {
      // set the height to auto so that max-content works
      navBar.style.height = "auto";
    }
  });
});

const bgColor = computed(() => window.getComputedStyle(document.body).getPropertyValue(settings.darkMode ? "--gray-800" : "--gray-50"));
const primaryColor = computed(() => window.getComputedStyle(document.body).getPropertyValue("--primary-500"));
</script>

<template>
  <rapi-doc
    ref="dom"
    spec-url="/v3/api-docs/public"
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
    font-size="large"
    :bg-color="bgColor"
    :primary-color="primaryColor"
    regular-font="inherit"
    :text-color="settings.darkMode ? '#E0E6f0' : '#262626'"
  />
</template>

<style lang="scss">
rapi-doc {
  border-radius: 0.375rem;
  height: max-content;
}
</style>
