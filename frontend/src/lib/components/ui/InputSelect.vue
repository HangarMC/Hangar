<script setup lang="ts">
import { computed } from "vue";
import { type ValidationRule } from "@vuelidate/core";
import { useValidation } from "~/lib/composables/useValidationHelpers";
import InputWrapper from "~/lib/components/ui/InputWrapper.vue";
import { useI18n } from "vue-i18n";

const i18n = useI18n();

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
    messages?: string[];
    errorMessages?: string[];
    rules?: ValidationRule<string | undefined>[];
    noErrorTooltip?: boolean;
    i18nTextValues?: boolean;
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
    i18nTextValues: false,
  }
);
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
      <select v-model="internalVal" :disabled="disabled" :class="slotProps.class" class="appearance-none" @blur="v.$touch()">
        <option v-for="val in values" :key="val[itemValue] || val" :value="val[itemValue] || val" class="dark:bg-[#191e28]">
          {{ i18nTextValues ? i18n.t(val[itemText] || val) : val[itemText] || val }}
        </option>
      </select>
    </template>
    <template v-for="(_, name) in $slots" #[name]="slotData">
      <slot :name="name" v-bind="slotData || {}" />
    </template>
  </InputWrapper>
</template>
