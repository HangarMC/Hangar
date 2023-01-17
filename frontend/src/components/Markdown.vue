<script lang="ts" setup>
import { computed, watchPostEffect } from "vue";
import { useI18n } from "vue-i18n";
import { usePrismStore } from "~/store/prism";
import { parseMarkdown } from "~/composables/useMarked";

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

const renderedMarkdown = computed(() => parseMarkdown(props.raw));

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
