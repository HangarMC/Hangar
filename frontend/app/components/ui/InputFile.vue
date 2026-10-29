<script lang="ts" setup>
import type { ValidationRule } from "@vuelidate/core";

const emit = defineEmits<{
  (e: "update:modelValue", file?: File | null): void;
}>();
const file = computed({
  get: () => props.modelValue,
  set: (value) => emit("update:modelValue", value),
});
const props = defineProps<{
  modelValue?: File | null;
  label?: string;
  disabled?: boolean;
  showSize?: boolean;
  loading?: boolean;
  messages?: string[];
  errorMessages?: string[];
  rules?: ValidationRule<string | undefined>[];
  noErrorTooltip?: boolean;
  accept?: string;
}>();

const i18n = useI18n();
const errorMessages = computed(() => props.errorMessages);
const { v, errors, hasError } = useValidation(props.label, props.rules, file, errorMessages);

function getErrorMessage(message: (typeof errors.value)[number]) {
  return isErrorObject(message) ? unref(message.$message) : message;
}

const input = useTemplateRef("input");
const dragging = ref(false);

function pick(files?: FileList | null) {
  const picked = files?.[0];
  if (picked) file.value = picked;
}

function onFileChange(e: Event) {
  pick((e.target as HTMLInputElement).files);
}

function onDrop(e: DragEvent) {
  dragging.value = false;
  if (props.disabled) return;
  pick(e.dataTransfer?.files);
}

function clear() {
  file.value = undefined;
}

// check if we got reset from the outside
watch(file, (newVal) => {
  if (!newVal && input.value) {
    input.value.value = "";
  }
});
</script>

<template>
  <div>
    <button
      type="button"
      class="w-full flex items-center gap-3 rounded-md border-2 border-dashed px-3 py-4 text-left transition-colors"
      :class="[
        hasError && !disabled ? 'border-red-500' : dragging ? 'border-primary-500 background-card' : 'border-gray-300 dark:border-gray-700',
        disabled ? 'cursor-not-allowed opacity-60' : 'cursor-pointer hover:background-card',
      ]"
      :disabled="disabled"
      @click="input?.click()"
      @dragover.prevent="dragging = !disabled"
      @dragleave.prevent="dragging = false"
      @drop.prevent="onDrop"
    >
      <Spinner v-if="loading || v.$pending" class="h-6 w-6 flex-shrink-0 stroke-gray-400" />
      <IconMdiFileCheckOutline v-else-if="file" class="flex-shrink-0 text-2xl color-primary" />
      <IconMdiTrayArrowUp v-else class="flex-shrink-0 text-2xl text-gray-secondary" />

      <span class="min-w-0 flex-1">
        <span class="block truncate font-semibold">{{ file?.name ?? (label || i18n.t("general.file.choose")) }}</span>
        <span class="block truncate text-sm text-gray-secondary">
          <template v-if="file && showSize">{{ formatSize(file.size) }}</template>
          <template v-else>{{ i18n.t("general.file.dropHint") }}</template>
        </span>
      </span>

      <span
        v-if="file && !disabled"
        class="flex-shrink-0 rounded p-1 text-gray-secondary hover:color-primary"
        :title="i18n.t('general.file.clear')"
        :aria-label="i18n.t('general.file.clear')"
        @click.stop="clear"
      >
        <IconMdiClose />
      </span>

      <input ref="input" type="file" class="hidden" v-bind="$attrs" :accept="accept" :disabled="disabled" @change="onFileChange" />
    </button>

    <p v-for="message in messages" :key="message" class="mt-1 text-sm text-gray-secondary">{{ message }}</p>
    <p v-for="error in errors" :key="getErrorMessage(error)" class="mt-1 text-sm text-red-400">{{ getErrorMessage(error) }}</p>
  </div>
</template>
