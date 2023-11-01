<script setup lang="ts">
import { computed } from "vue";
import type { ValidationRule } from "@vuelidate/core";
import { useI18n } from "vue-i18n";
import { useValidation } from "~/composables/useValidationHelpers";
import InputWrapper from "~/components/ui/InputWrapper.vue";
import type { Option } from "~/types/components/ui/InputSelect";

const i18n = useI18n();

const emit = defineEmits<{
  (e: "update:modelValue", value: object | string | boolean | number | null | undefined): void;
}>();
const internalVal = computed({
  get: () => props.modelValue,
  set: (val) => emit("update:modelValue", val),
});

const props = withDefaults(
  defineProps<{
    modelValue?: object | string | boolean | number | null;
    values: Option[] | Record<string, any> | string[] | object[];
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
        <option
          v-for="val in values"
          :key="val[itemValue] || val"
          :value="val[itemValue] || val"
          class="dark:bg-[#191e28]"
          :selected="internalVal === val[itemValue] || internalVal === val"
        >
          {{ i18nTextValues ? i18n.t(val[itemText] || val) : val[itemText] || val }}
        </option>
      </select>
      <IconMdiMenuDown class="absolute flex right-2 self-center -z-index-1" />
    </template>
    <template v-for="(_, name) in $slots" #[name]="slotData">
      <slot :name="name" v-bind="slotData || {}" />
    </template>
  </InputWrapper>
</template>
