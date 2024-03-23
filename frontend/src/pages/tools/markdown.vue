<script setup lang="ts">
const route = useRoute();

const markdown = ref("# Your input  \nEnter your *markdown* here!");

useHead(useSeo("Markdown Editor", null, route, null));
</script>

<template>
  <div>
    <PageTitle>Markdown Editor</PageTitle>

    <ClientOnly>
      <MarkdownEditor :saveable="false" :cancellable="false" :deletable="false" :raw="markdown" :editing="true" />
    </ClientOnly>
  </div>
</template>
