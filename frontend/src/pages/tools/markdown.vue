<script setup lang="ts">
import { ref } from "vue";
import { useI18n } from "vue-i18n";
import { useHead } from "@unhead/vue";
import { useRoute } from "vue-router";
import PageTitle from "~/components/design/PageTitle.vue";
import { useSeo } from "~/composables/useSeo";

const i18n = useI18n();
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
