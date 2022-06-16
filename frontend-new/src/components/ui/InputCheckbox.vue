<script setup lang="ts">
import { computed } from "vue";
import { useValidation } from "~/composables/useValidationHelpers";
import { ValidationRule } from "@vuelidate/core";

const emit = defineEmits<{
  (e: "update:modelValue", value: boolean | boolean[]): void;
}>();
const internalVal = computed({
  get: () => props.modelValue,
  set: (val) => emit("update:modelValue", val),
});
const props = defineProps<{
  modelValue: boolean | boolean[];
  label?: string;
  disabled?: boolean;
  errorMessages?: string[];
  rules?: ValidationRule<string | undefined>[];
}>();

const errorMessages = computed(() => props.errorMessages);
const { v, errors, hasError } = useValidation(props.label, props.rules, internalVal, errorMessages);
</script>

<template>
  <label class="customCheckbox group relative flex items-center select-none" :cursor="disabled ? 'auto' : 'pointer'">
    <input
      v-model="internalVal"
      type="checkbox"
      class="appearance-none h-4 w-4 bg-gray-300 mr-2 rounded-sm group-hover:bg-gray-400 !checked:bg-primary-400"
      dark="bg-gray-600 group-hover:bg-gray-500"
      :cursor="disabled ? 'auto' : 'pointer'"
      v-bind="$attrs"
      :disabled="disabled"
      @blur="v.$touch()"
    />
    <icon-mdi-check-bold class="absolute h-4 w-4 opacity-0 text-white" />
    <template v-if="props.label">{{ props.label }}</template>
  </label>
</template>

<style>
.customCheckbox input:checked ~ svg {
  @apply opacity-100;
}
</style>
