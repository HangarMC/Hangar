<script lang="ts" setup>
const props = withDefaults(
  defineProps<{
    raw?: string | null;
    inline?: boolean;
  }>(),
  {
    raw: undefined,
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
