<script setup lang="ts">
import { ref } from "vue";
import Button from "~/lib/components/design/Button.vue";
import Markdown from "~/components/Markdown.vue";
import { useInternalApi } from "~/composables/useApi";
import { handleRequestError } from "~/composables/useErrorHandling";
import { useContext } from "vite-ssr/vue";
import { useI18n } from "vue-i18n";
import PageTitle from "~/lib/components/design/PageTitle.vue";
import { useHead } from "@vueuse/head";
import { useSeo } from "~/composables/useSeo";
import { useRoute } from "vue-router";
import InputTextarea from "~/lib/components/ui/InputTextarea.vue";

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

  <h2 class="text-lg mb-2">Enter your BBCode below:</h2>
  <InputTextarea v-model="input" class="rounded-lg w-full min-h-50" :loading="loading" />

  <div class="text-center mt-4">
    <Button class="w-min mb-4 text-lg font-medium" size="large" :loading="loading" @click="convertBBCode"> Convert </Button>
  </div>

  <h2 class="text-lg mb-2">Markdown output</h2>
  <InputTextarea v-model="output" class="mb-2 rounded-lg w-full min-h-50" />

  <h2 class="text-lg my-2">Markdown preview</h2>
  <Markdown :raw="output"></Markdown>
</template>
