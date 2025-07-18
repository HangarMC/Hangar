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
  <div class="relative">
    <slot name="title" />
    <div class="flex">
      <div class="absolute top-2 right-0 space-x-1">
        <Button>
          <IconMdiPencil />
        </Button>
      </div>
    </div>
    <Markdown :raw="raw" />>
  </div>
</template>
