<script setup lang="ts">
import { computed } from "vue";
import { useValidation } from "~/lib/composables/useValidationHelpers";
import { type ValidationRule } from "@vuelidate/core";

const emit = defineEmits<{
  (e: "update:modelValue", value?: string): void;
}>();
const internalVal = computed({
  get: () => props.modelValue,
  set: (val) => emit("update:modelValue", val),
});
const props = defineProps<{
  modelValue?: string;
  label?: string;
  errorMessages?: string[];
  rules?: ValidationRule<string | undefined>[];
}>();

const errorMessages = computed(() => props.errorMessages);
const { v, errors, hasError } = useValidation(props.label, props.rules, internalVal, errorMessages);
</script>

<template>
  <label class="customRadioButton group relative cursor-pointer flex items-center select-none">
    <input
      v-model="internalVal"
      type="radio"
      class="appearance-none h-4 w-4 bg-gray-300 mr-2 rounded-full group-hover:bg-gray-400 !checked:bg-primary-400"
      dark="bg-gray-600 group-hover:bg-gray-500"
      v-bind="$attrs"
      @blur="v.$touch()"
    />
    <icon-mdi-circle class="absolute h-4 w-4 p-1 opacity-0 text-white" />
    <slot></slot>
    <template v-if="props.label">{{ props.label }}</template>
  </label>
</template>

<style>
.customRadioButton input:checked ~ svg {
  @apply opacity-100;
}
</style>
