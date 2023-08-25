<script lang="ts" setup>
import { computed, watchPostEffect } from "vue";
import { useI18n } from "vue-i18n";
import { usePrismStore } from "~/store/prism";
import { parseMarkdown } from "~/composables/useMarked";
import { useDomPurify } from "~/composables/useDomPurify";

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

const renderedMarkdown = computed(() => useDomPurify(parseMarkdown(props.raw)));

watchPostEffect(async () => {
  if (!import.meta.env.SSR) {
    if (typeof renderedMarkdown.value?.includes === "function" && renderedMarkdown.value?.includes("<code")) {
      await usePrismStore().handlePrism();
    }
  }
});
</script>

<template>
  <div class="iframe-container prose max-w-full rounded markdown break-words" :class="{ 'p-4': !inline, inline: inline }">
    <!-- eslint-disable-next-line vue/no-v-html -->
    <div v-html="renderedMarkdown" />
  </div>
</template>

<style lang="scss">
@import "@/assets/css/markdown.scss";

.iframe-container iframe {
  max-width: 100%;
  max-height: 100%;
}
</style>
