<script setup lang="ts">
const route = useRoute();

const input = ref("");
const output = ref("");
const loading = ref(false);
const copied = ref(false);

useSeo(computed(() => ({ title: "BBCode Converter", route })));

async function convertBBCode() {
  loading.value = true;
  output.value = await useInternalApi<string>("pages/convert-bbcode", "post", {
    content: input.value,
  }).catch<any>((err) => handleRequestError(err));
  loading.value = false;
}

function reset() {
  input.value = "";
  output.value = "";
}

function copy(event: any) {
  const clipboardData = event.clipboardData || event.originalEvent?.clipboardData || navigator.clipboard;
  clipboardData.writeText(output.value);
  copied.value = true;
  setTimeout(() => (copied.value = false), 2000);
}
</script>

<template>
  <div>
    <div class="mb-5 flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
      <div>
        <h1 class="text-3xl font-bold">BBCode to Markdown Converter</h1>
        <p class="mt-1 text-gray-secondary">Paste BBCode below and convert it to Markdown you can use on Hangar pages.</p>
      </div>
      <div class="flex flex-wrap gap-2">
        <Button variant="outline" tone="neutral" :disabled="!input && !output" @click="reset">
          <IconMdiBroom />
          Reset
        </Button>
        <Button :loading="loading" :disabled="!input" @click="convertBBCode">
          <IconMdiSwapHorizontal />
          Convert
        </Button>
      </div>
    </div>

    <div class="grid gap-4 lg:grid-cols-2">
      <section class="min-w-0">
        <div class="mb-2 h-8 flex items-center gap-2">
          <h2 id="bbcode-input-label" class="flex-grow text-lg font-bold">BBCode input</h2>
          <span class="text-sm text-gray-secondary tabular-nums">{{ input.length }}</span>
        </div>
        <InputTextarea v-model="input" class="w-full rounded-lg" :loading="loading" :min-rows="14" aria-labelledby="bbcode-input-label" />
      </section>

      <section class="min-w-0">
        <div class="mb-2 h-8 flex items-center gap-2">
          <h2 id="markdown-output-label" class="flex-grow text-lg font-bold">Markdown output</h2>
          <Tooltip :hover="false" :show="copied">
            <template #content>Copied to clipboard!</template>
            <Button variant="outline" tone="neutral" size="sm" :disabled="!output" @click="copy">
              <IconMdiContentCopy />
              Copy
            </Button>
          </Tooltip>
        </div>
        <InputTextarea v-model="output" class="w-full rounded-lg" :min-rows="14" aria-labelledby="markdown-output-label" />
      </section>
    </div>

    <section class="mt-6">
      <h2 class="mb-2 text-lg font-bold">Markdown preview</h2>
      <Card flat>
        <Markdown v-if="output" :raw="output" />
        <p v-else class="py-6 text-center text-gray-secondary">Convert some BBCode to see the rendered result here.</p>
      </Card>
    </section>
  </div>
</template>
