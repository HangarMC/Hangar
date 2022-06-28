<script setup lang="ts">
import { computed, watch } from "vue";
import { useValidation } from "~/lib/composables/useValidationHelpers";
import { ValidationRule } from "@vuelidate/core";
import InputWrapper from "~/lib/components/ui/InputWrapper.vue";

const emit = defineEmits<{
  (e: "update:modelValue", value: object | string | boolean | number | null | undefined): void;
  (e: "search", value: object | string | boolean | number | null | undefined): void;
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
    id: string;
    modelValue?: object | string | boolean | number | null;
    values?: Option[] | Record<string, any> | string[];
    itemValue?: string | ((object: object) => string);
    itemText?: string | ((object: object) => string);
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

function getValue(val: Record<string, string>) {
  if (typeof props.itemValue === "function") {
    return props.itemValue(val);
  } else if (val[props.itemValue]) {
    return val[props.itemValue];
  } else {
    return val;
  }
}

function getText(val: Record<string, string>) {
  if (typeof props.itemText === "function") {
    return props.itemText(val);
  } else if (val[props.itemText]) {
    return val[props.itemText];
  } else {
    return val;
  }
}

watch(internalVal, (val) => emit("search", val));

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
    <input v-model="internalVal" type="text" v-bind="$attrs" :class="slotProps.class" :list="id" :disabled="disabled" @blur="v.$touch()" />
    <datalist :id="id">
      <option v-for="val in values" :key="getValue(val)" :value="getValue(val)">
        {{ getText(val) }}
      </option>
    </datalist>
  </InputWrapper>
</template>
