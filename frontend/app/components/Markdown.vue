<script lang="ts" setup>
const props = withDefaults(
  defineProps<{
    raw?: string | null;
    inline?: boolean;
    /**
    Off when a wrapper (MarkdownEditor) renders the overview in its own control cluster.
    */
    showToc?: boolean;
  }>(),
  {
    raw: undefined,
    inline: false,
    showToc: true,
  }
);

const emit = defineEmits<{
  headings: [headings: { id: string; text?: string; level: number }[]];
}>();

const renderedMarkdown = computed(() => {
  const { html, headings } = parseMarkdown(props.raw);
  return {
    html: useDomPurify(html),
    headings: headings
      ?.map((heading) => ({
        ...heading,
        text: stripAllHtml(heading.text),
      }))
      .filter((heading) => heading.text.trim().length > 0),
  };
});

watchEffect(() => emit("headings", renderedMarkdown.value.headings || []));

watchPostEffect(async () => {
  if (!import.meta.env.SSR && typeof renderedMarkdown.value?.html.includes === "function" && renderedMarkdown.value?.html.includes("<code")) {
    await usePrismStore().handlePrism();
  }
});
</script>

<template>
  <div class="relative">
    <!-- Sits in the prose padding rather than above it, so the card no longer opens with an empty band. -->
    <MarkdownToc
      v-if="showToc && (renderedMarkdown.headings?.length || 0) > 0"
      :headings="renderedMarkdown.headings || []"
      class="absolute right-2 top-2 z-1"
    />
    <div class="iframe-container prose dark:prose-invert max-w-full rounded markdown break-words" :class="{ 'p-4': !inline, inline: inline }">
      <!-- eslint-disable-next-line vue/no-v-html -->
      <div v-html="renderedMarkdown.html" />
    </div>
  </div>
</template>

<style lang="scss">
@use "@/assets/css/markdown.scss";

.iframe-container iframe {
  max-width: 100%;
  max-height: 100%;
}
</style>
