<script lang="ts" setup>
import { computed } from "vue";
import { useValidation } from "~/composables/useValidationHelpers";
import { ValidationRule } from "@vuelidate/core";

const emit = defineEmits<{
  (e: "update:modelValue", date: string): void;
}>();
const date = computed({
  get: () => props.modelValue,
  set: (value) => emit("update:modelValue", value),
});
const props = defineProps<{
  modelValue: string;
  label?: string;
  disabled?: boolean;
  errorMessages?: string[];
  rules?: ValidationRule<string | undefined>[];
}>();

const { v, errors, hasError } = useValidation(props.label, props.rules, date, props.errorMessages);
</script>

<template>
  <!-- todo make fancy -->
  <input v-model="date" type="date" v-bind="$attrs" :disabled="disabled" />
</template>
