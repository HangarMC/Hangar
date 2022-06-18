<script setup lang="ts">
import { computed } from "vue";
import { useValidation } from "~/composables/useValidationHelpers";
import { ValidationRule } from "@vuelidate/core";
import InputWrapper from "~/components/ui/InputWrapper.vue";

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
    loading?: boolean;
    errorMessages?: string[];
    rules?: ValidationRule<string | undefined>[];
  }>(),
  {
    modelValue: "",
    itemValue: "value",
    itemText: "text",
    label: "",
    loading: false,
    errorMessages: () => [],
    rules: () => [],
  }
);
const errorMessages = computed(() => props.errorMessages);
const { v, errors, hasError } = useValidation(props.label, props.rules, internalVal, errorMessages);
</script>

<template>
  <InputWrapper
    v-slot="slotProps"
    :errors="errors"
    :has-error="hasError"
    :loading="loading || v.$pending"
    :label="label"
    :value="internalVal"
    :disabled="disabled"
  >
    <select v-model="internalVal" :disabled="disabled" :class="slotProps.class" class="appearance-none" @blur="v.$touch()">
      <option v-for="val in values" :key="val[itemValue] || val" :value="val[itemValue] || val" class="dark:bg-[#191e28]">
        {{ val[itemText] || val }}
      </option>
    </select>
  </InputWrapper>
</template>
