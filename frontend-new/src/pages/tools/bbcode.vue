<script setup lang="ts">
import { ref } from "vue";
import Button from "~/components/design/Button.vue";
import Markdown from "~/components/Markdown.vue";
import { useInternalApi } from "~/composables/useApi";
import { handleRequestError } from "~/composables/useErrorHandling";
import { useContext } from "vite-ssr/vue";
import { useI18n } from "vue-i18n";
import PageTitle from "~/components/design/PageTitle.vue";
import { useHead } from "@vueuse/head";
import { useSeo } from "~/composables/useSeo";
import { useRoute } from "vue-router";

const ctx = useContext();
const i18n = useI18n();
const route = useRoute();

const input = ref("");
const output = ref("");
const loading = ref(false);

useHead(useSeo("BBCode Converter", null, route, null));

async function convertBBCode() {
  loading.value = true;
  output.value = await useInternalApi<string>("pages/convert-bbcode", false, "post", {
    content: input.value,
  }).catch<any>((e) => handleRequestError(e, ctx, i18n));
  loading.value = false;
}
</script>

<template>
  <PageTitle>BBCode to Markdown Converter</PageTitle>

  <h2 class="text-lg">Enter your BBCode below:</h2>
  <textarea v-model="input" class="rounded-lg w-full min-h-50"></textarea>

  <Button class="w-full mb-4 text-lg font-medium" @click="convertBBCode"> Convert </Button>

  <h2 class="text-lg">Markdown output</h2>
  <textarea v-model="output" class="mb-2 rounded-lg w-full min-h-50"></textarea>

  <h2 class="text-lg">Markdown preview</h2>
  <Markdown :raw="output"></Markdown>
</template>
