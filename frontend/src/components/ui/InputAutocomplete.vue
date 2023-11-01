<script setup lang="ts">
import { computed, watch } from "vue";
import type { ValidationRule } from "@vuelidate/core";
import { useValidation } from "~/composables/useValidationHelpers";
import InputWrapper from "~/components/ui/InputWrapper.vue";
import type { Option } from "~/types/components/ui/InputAutocomplete";

const emit = defineEmits<{
  (e: "update:modelValue", value: object | string | boolean | number | null | undefined): void;
  (e: "search", value: object | string | boolean | number | null | undefined): void;
}>();
const internalVal = computed({
  get: () => props.modelValue,
  set: (val) => emit("update:modelValue", val),
});

const props = withDefaults(
  defineProps<{
    id: string;
    modelValue?: object | string | boolean | number | null;
    values: Option[] | Record<string, any> | string[];
    itemValue?: string | ((object: object) => string);
    itemText?: string | ((object: object) => string);
    disabled?: boolean;
    label?: string;
    loading?: boolean;
    messages?: string[];
    errorMessages?: string[];
    rules?: ValidationRule<string | undefined>[];
    noErrorTooltip?: boolean;
  }>(),
  {
    modelValue: "",
    itemValue: "value",
    itemText: "text",
    label: "",
    loading: false,
    messages: () => [],
    errorMessages: () => [],
    rules: () => [],
  }
);

function getValue(val: any) {
  if (typeof props.itemValue === "function") {
    return props.itemValue(val);
  } else if (val[props.itemValue]) {
    return val[props.itemValue];
  } else {
    return val as string;
  }
}

function getText(val: any) {
  if (typeof props.itemText === "function") {
    return props.itemText(val);
  } else if (val[props.itemText]) {
    return val[props.itemText];
  } else {
    return val as string;
  }
}

watch(internalVal, (val) => emit("search", val));

const errorMessages = computed(() => props.errorMessages);
const { v, errors, hasError } = useValidation(props.label, props.rules, internalVal, errorMessages);
</script>

<template>
  <InputWrapper
    :errors="errors"
    :messages="messages"
    :has-error="hasError"
    :loading="loading || v.$pending"
    :label="label"
    :value="internalVal"
    :disabled="disabled"
    :no-error-tooltip="noErrorTooltip"
  >
    <template #default="slotProps">
      <input v-model="internalVal" type="text" v-bind="$attrs" :class="slotProps.class" :list="id" :disabled="disabled" @blur="v.$touch()" />
      <datalist :id="id">
        <option v-for="val in values" :key="getValue(val)" :value="getValue(val)">
          {{ getText(val) }}
        </option>
      </datalist>
    </template>
    <template v-for="(_, name) in $slots" #[name]="slotData">
      <slot :name="name" v-bind="slotData || {}" />
    </template>
  </InputWrapper>
</template>
