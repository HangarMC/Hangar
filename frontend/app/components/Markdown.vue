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
  <div v-if="(renderedMarkdown.headings?.length || 0) > 0" class="mb-4 relative">
    <DropdownButton :button-arrow="false" button-size="small" class="absolute top-2 left-0">
      <template #button-label>
        <IconMdiFormatListBulleted />
      </template>
      <template #default="{ close }">
        <div class="w-max flex flex-col max-h-lg max-w-lg overflow-x-auto">
          <!-- eslint-disable vue/no-v-html -->
          <a
            v-for="heading in renderedMarkdown.headings"
            :key="heading.id"
            class="px-4 py-2 font-semibold hover:bg-gray-100 hover:dark:bg-gray-700 cursor-pointer decoration-none"
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
  <div class="iframe-container prose max-w-full rounded markdown break-words" :class="{ 'p-4': !inline, inline: inline }">
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
