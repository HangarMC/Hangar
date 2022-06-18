<script lang="ts" setup>
import { computed } from "vue";
import { useValidation } from "~/composables/useValidationHelpers";
import { ValidationRule } from "@vuelidate/core";
import InputWrapper from "~/components/ui/InputWrapper.vue";

const emit = defineEmits<{
  (e: "update:modelValue", file: File): void;
}>();
const file = computed({
  get: () => props.modelValue,
  set: (value) => emit("update:modelValue", value),
});
const props = defineProps<{
  modelValue: File;
  label?: string;
  disabled?: boolean;
  showSize?: boolean;
  loading?: boolean;
  errorMessages?: string[];
  rules?: ValidationRule<string | undefined>[];
}>();

const errorMessages = computed(() => props.errorMessages);
const { v, errors, hasError } = useValidation(props.label, props.rules, file, errorMessages);

function onFileChange(e: InputEvent) {
  const files = (e.target as HTMLInputElement).files || e.dataTransfer?.files;
  if (!files?.length) return;
  file.value = files[0];
}
</script>

<template>
  <InputWrapper v-slot="slotProps" :errors="errors" :has-error="hasError" :loading="loading || v.$pending" :label="label" :value="file" :disabled="disabled">
    <!-- todo make button fancy -->
    <input type="file" v-bind="$attrs" :disabled="disabled" :class="slotProps.class" @change="onFileChange" />
  </InputWrapper>
</template>
