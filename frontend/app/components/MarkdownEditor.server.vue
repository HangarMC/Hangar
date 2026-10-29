<script lang="ts" setup>
import type { ValidationRule } from "@vuelidate/core";

const i18n = useI18n();

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
      <div class="absolute top-2 right-0 z-10 flex gap-1">
        <Button variant="outline" tone="neutral" size="sm" icon-only :title="i18n.t('general.edit')" :aria-label="i18n.t('general.edit')">
          <IconMdiPencil />
        </Button>
      </div>
    </div>
    <Markdown :raw="raw" :show-toc="false" />
  </div>
</template>
