<script setup lang="ts">
import { computed } from "vue";
import { FloatingLabel, inputClasses } from "~/composables/useInputHelper";
import ErrorTooltip from "~/components/design/ErrorTooltip.vue";
import { useValidation } from "~/composables/useValidationHelpers";
import { ValidationRule } from "@vuelidate/core";

const emit = defineEmits<{
  (e: "update:modelValue", value: object | string | boolean | number | null | undefined): void;
}>();
const internalVal = computed({
  get: () => props.modelValue,
  set: (val) => emit("update:modelValue", val),
});

export interface Option {
  value: object | string | boolean | number;
  text: string;
}

const props = withDefaults(
  defineProps<{
    modelValue: object | string | boolean | number | null;
    values: Option[] | Record<string, any> | string[];
    itemValue?: string;
    itemText?: string;
    disabled?: boolean;
    label?: string;
    errorMessages?: string[];
    rules?: ValidationRule<string | undefined>[];
  }>(),
  {
    modelValue: "",
    itemValue: "value",
    itemText: "text",
    label: "",
    errorMessages: () => [],
    rules: () => [],
  }
);

const { v, errors, hasError } = useValidation(props.label, props.rules, internalVal, props.errorMessages);
</script>

<template>
  <ErrorTooltip :error-messages="errors" class="w-full">
    <label class="relative flex" :class="{ filled: internalVal, error: hasError }">
      <select v-model="internalVal" :disabled="disabled" :class="inputClasses" @blur="v.$touch()">
        <option v-for="val in values" :key="val[itemValue] || val" :value="val[itemValue] || val" class="dark:bg-[#191e28]">
          {{ val[itemText] || val }}
        </option>
      </select>
      <floating-label :label="label" />
    </label>
  </ErrorTooltip>
</template>
