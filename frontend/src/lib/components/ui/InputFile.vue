<script lang="ts" setup>
import { computed, ref, watch } from "vue";
import { ValidationRule } from "@vuelidate/core";
import { useValidation } from "~/lib/composables/useValidationHelpers";
import InputWrapper from "~/lib/components/ui/InputWrapper.vue";

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
  messages?: string[];
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

// check if we got reset from the outside
const input = ref();
watch(file, (newVal) => {
  if (!newVal) {
    input.value.value = null;
  }
});
</script>

<template>
  <InputWrapper
    v-slot="slotProps"
    :errors="errors"
    :messages="messages"
    :has-error="hasError"
    :loading="loading || v.$pending"
    :label="label"
    :value="file"
    :disabled="disabled"
  >
    <!-- todo make button fancy -->
    <input ref="input" type="file" v-bind="$attrs" :disabled="disabled" :class="slotProps.class" @change="onFileChange" />
  </InputWrapper>
</template>
