<script lang="ts" setup>
import type { ValidationRule } from "@vuelidate/core";

withDefaults(
  defineProps<{
    raw?: string | null;
    editing: boolean;
    deletable: boolean;
    cancellable: boolean;
    saveable: boolean;
    maxlength?: number;
    errorMessages?: string[];
    rules?: ValidationRule<string | undefined>[];
    noPaddingTop?: boolean;
    maxHeight?: string;
    label?: string;
  }>(),
  {
    raw: undefined,
    maxlength: 30_000,
    errorMessages: undefined,
    rules: undefined,
    noPaddingTop: false,
    maxHeight: "500px",
    label: undefined,
  }
);
</script>

<template>
  <div class="markdown-editor relative">
    <slot name="title" />
    <div class="absolute top-2 right-2 z-2">
      <Button button-type="secondary" class="!h-9 !w-9 !p-0" aria-label="Edit content">
        <IconMdiPencil />
      </Button>
    </div>
    <Markdown :raw="raw" />
  </div>
</template>

<style scoped>
.markdown-editor :deep(.markdown) {
  margin-top: 0;
  padding: 1rem;
}

.markdown-editor :deep(.markdown > div > :first-child) {
  margin-top: 0;
}

.markdown-editor :deep(.markdown > div > :last-child) {
  margin-bottom: 0;
}
</style>
