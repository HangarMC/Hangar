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
const route = useRoute();

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

watchPostEffect(async () => {
  if (!import.meta.env.SSR && typeof renderedMarkdown.value?.html.includes === "function" && renderedMarkdown.value?.html.includes("<code")) {
    await usePrismStore().handlePrism();
  }
});
</script>

<template>
  <div
    v-if="!inline && !route.params.project && (renderedMarkdown.headings?.length || 0) > 0"
    class="flex items-center border-b px-4 py-3 dark:border-gray-800"
  >
    <DropdownButton :button-arrow="false" button-size="medium" button-type="transparent" placement="bottom-start">
      <template #button-label>
        <IconMdiFormatListBulleted />
      </template>
      <template #default="{ close }">
        <div class="flex max-h-lg min-w-56 max-w-lg flex-col gap-1 overflow-y-auto px-2 py-1.5">
          <!-- eslint-disable vue/no-v-html -->
          <a
            v-for="heading in renderedMarkdown.headings"
            :key="heading.id"
            class="rounded-lg border border-transparent px-3 py-2 font-semibold decoration-none transition-all duration-250 hover:border-gray-300 hover:bg-gray-100 dark:hover:border-gray-700 dark:hover:bg-gray-800"
            :class="'toc-' + heading.level"
            :href="`#${heading.id}`"
            @click="close"
            v-html="heading.text"
          />
          <!-- eslint-enable vue/no-v-html -->
        </div>
      </template>
    </DropdownButton>
  </div>
  <div class="iframe-container prose dark:prose-invert max-w-full rounded markdown break-words" :class="{ 'p-5': !inline, inline: inline }">
    <!-- eslint-disable-next-line vue/no-v-html -->
    <div v-html="renderedMarkdown.html" />
  </div>
</template>

<style lang="scss">
@use "@/assets/css/markdown.scss";

.iframe-container iframe {
  max-width: 100%;
  max-height: 100%;
}
</style>

<style lang="scss" scoped>
.toc-1 {
  font-weight: 700;
}
.toc-2 {
  padding-left: 2rem;
}
.toc-3 {
  padding-left: 3rem;
}
.toc-4 {
  padding-left: 4rem;
}
</style>
