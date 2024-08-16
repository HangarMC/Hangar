<script setup lang="ts">
const route = useRoute();

const markdown = ref("# Your input  \nEnter your *markdown* here!");

useSeo(computed(() => ({ title: "Markdown Editor", route })));
</script>

<template>
  <div>
    <PageTitle>Markdown Editor</PageTitle>

    <ClientOnly>
      <MarkdownEditor :saveable="false" :cancellable="false" :deletable="false" :raw="markdown" :editing="true" />
    </ClientOnly>
  </div>
</template>
