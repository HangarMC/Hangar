<script lang="ts" setup>
import { computed } from "vue";
import { useValidation } from "~/composables/useValidationHelpers";
import { ValidationRule } from "@vuelidate/core";
import InputWrapper from "~/components/ui/InputWrapper.vue";

const emit = defineEmits<{
  (e: "update:modelValue", file: string): void;
}>();
const file = computed({
  get: () => props.modelValue,
  set: (value) => emit("update:modelValue", value),
});
const props = defineProps<{
  modelValue: string;
  label?: string;
  disabled?: boolean;
  showSize?: boolean;
  loading?: boolean;
  errorMessages?: string[];
  rules?: ValidationRule<string | undefined>[];
}>();

const { v, errors, hasError } = useValidation(props.label, props.rules, file, props.errorMessages);
</script>

<template>
  <InputWrapper v-slot="slotProps" :errors="errors" :has-error="hasError" :loading="loading || v.$pending" :label="label" :value="file">
    <!-- todo make fancy, implement functionality -->
    <input type="file" v-bind="$attrs" :disabled="disabled" :class="slotProps.class" />
  </InputWrapper>
</template>
