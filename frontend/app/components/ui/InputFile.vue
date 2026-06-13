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
}>();

const errorMessages = computed(() => props.errorMessages);
const { v, errors, hasError } = useValidation(props.label, props.rules, file, errorMessages);

function onFileChange(e: Event) {
  const files = (e.target as HTMLInputElement).files || (e as InputEvent).dataTransfer?.files;
  if (!files?.length) return;
  file.value = files[0];
}

// check if we got reset from the outside
const input = useTemplateRef("input");
watch(file, (newVal) => {
  if (!newVal) {
    input.value!.value = "";
  }
});
</script>

<template>
  <div>
    <label v-if="label" class="mb-1 block text-sm font-semibold">{{ label }}</label>
    <InputWrapper
      :errors="errors"
      :messages="messages"
      :has-error="hasError"
      :loading="loading || v.$pending"
      :value="file"
      :disabled="disabled"
      :no-error-tooltip="noErrorTooltip"
    >
      <template #default="slotProps">
        <input ref="input" type="file" v-bind="$attrs" :disabled="disabled" :class="slotProps.class" @change="onFileChange" />
      </template>
      <!-- @vue-ignore -->
      <template v-for="(_, name) in $slots" #[name]="slotData">
        <slot :name="name" v-bind="slotData || {}" />
      </template>
    </InputWrapper>
  </div>
</template>
