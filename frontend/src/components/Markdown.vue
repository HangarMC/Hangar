<script lang="ts" setup>
import { computed, watchPostEffect } from "vue";
import { useI18n } from "vue-i18n";
import { marked } from "marked";
import { usePrismStore } from "~/store/prism";

const i18n = useI18n();
const props = withDefaults(
  defineProps<{
    raw: string;
    inline?: boolean;
  }>(),
  {
    inline: false,
  }
);

const renderer = {
  heading(text: string, level: number) {
    const escapedText = text.toLowerCase().replaceAll(/\W+/g, "-");
    return `
            <h${level}>
              ${text}
              <a id="${escapedText}" class="headeranchor" href="#${escapedText}">
                <span class="header-link"></span>
                <svg class="ml-2 text-xl" preserveAspectRatio="xMidYMid meet" viewBox="0 0 24 24" width="1.2em" height="2.2em"><path fill="currentColor" d="M10.59 13.41c.41.39.41 1.03 0 1.42c-.39.39-1.03.39-1.42 0a5.003 5.003 0 0 1 0-7.07l3.54-3.54a5.003 5.003 0 0 1 7.07 0a5.003 5.003 0 0 1 0 7.07l-1.49 1.49c.01-.82-.12-1.64-.4-2.42l.47-.48a2.982 2.982 0 0 0 0-4.24a2.982 2.982 0 0 0-4.24 0l-3.53 3.53a2.982 2.982 0 0 0 0 4.24m2.82-4.24c.39-.39 1.03-.39 1.42 0a5.003 5.003 0 0 1 0 7.07l-3.54 3.54a5.003 5.003 0 0 1-7.07 0a5.003 5.003 0 0 1 0-7.07l1.49-1.49c-.01.82.12 1.64.4 2.43l-.47.47a2.982 2.982 0 0 0 0 4.24a2.982 2.982 0 0 0 4.24 0l3.53-3.53a2.982 2.982 0 0 0 0-4.24a.973.973 0 0 1 0-1.42Z"></path></svg>
              </a>
            </h${level}>`;
  },
};
marked.use({ renderer });

const renderedMarkdown = computed(() => marked.parse(props.raw));

watchPostEffect(async () => {
  if (!import.meta.env.SSR) {
    if (typeof renderedMarkdown.value?.includes === "function" && renderedMarkdown.value?.includes("<code")) {
      await usePrismStore().handlePrism();
    }
  }
});
</script>

<template>
  <div class="prose max-w-full rounded markdown break-words" :class="{ 'p-4': !inline, inline: inline }">
    <div v-dompurify-html="renderedMarkdown" />
  </div>
</template>

<style lang="scss">
@import "@/lib/assets/css/markdown.scss";
</style>
