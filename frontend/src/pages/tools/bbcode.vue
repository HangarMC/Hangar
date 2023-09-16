<script setup lang="ts">
import { ref } from "vue";
import { useI18n } from "vue-i18n";
import { useHead } from "@unhead/vue";
import { useRoute } from "vue-router";
import Button from "~/components/design/Button.vue";
import Markdown from "~/components/Markdown.vue";
import { useInternalApi } from "~/composables/useApi";
import { handleRequestError } from "~/composables/useErrorHandling";
import PageTitle from "~/components/design/PageTitle.vue";
import { useSeo } from "~/composables/useSeo";
import InputTextarea from "~/components/ui/InputTextarea.vue";

const i18n = useI18n();
const route = useRoute();

const input = ref("");
const output = ref("");
const loading = ref(false);

useHead(useSeo("BBCode Converter", null, route, null));

async function convertBBCode() {
  loading.value = true;
  output.value = await useInternalApi<string>("pages/convert-bbcode", "post", {
    content: input.value,
  }).catch<any>((e) => handleRequestError(e));
  loading.value = false;
}
</script>

<template>
  <div>
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
  </div>
</template>
