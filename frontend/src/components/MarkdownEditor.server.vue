<script lang="ts" setup>
import type { ValidationRule } from "@vuelidate/core";
import Markdown from "~/components/Markdown.vue";
import Button from "~/components/design/Button.vue";

const props = withDefaults(
  defineProps<{
    raw: string;
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
    maxlength: 30_000,
    errorMessages: undefined,
    rules: undefined,
    noPaddingTop: false,
    maxHeight: "500px",
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
