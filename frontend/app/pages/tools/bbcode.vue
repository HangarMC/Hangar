<script setup lang="ts">
const route = useRoute();

const input = ref("");
const output = ref("");
const loading = ref(false);

useSeo(computed(() => ({ title: "BBCode Converter", route })));

async function convertBBCode() {
  loading.value = true;
  output.value = await useInternalApi<string>("pages/convert-bbcode", "post", {
    content: input.value,
  }).catch<any>((err) => handleRequestError(err));
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
    <Markdown :raw="output" />
  </div>
</template>
