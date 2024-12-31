<script setup lang="ts" generic="T">
import type { ValidationRule } from "@vuelidate/core";
import type { Option } from "~/types/components/ui/InputSelect";

const i18n = useI18n();

const emit = defineEmits<{
  (e: "update:modelValue", value?: T): void;
}>();
const internalVal = computed({
  get: () => props.modelValue,
  set: (val) => emit("update:modelValue", val),
});

const props = withDefaults(
  defineProps<{
    modelValue?: T;
    values: Option<T>[] | Record<string, any> | string[] | object[];
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
    modelValue: undefined,
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
      <select v-model="internalVal" v-bind="$attrs" :disabled="disabled" :class="slotProps.class" class="appearance-none" @blur="v.$touch()">
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
    <!-- @vue-ignore -->
    <template v-for="(_, name) in $slots" #[name]="slotData">
      <slot :name="name" v-bind="slotData || {}" />
    </template>
  </InputWrapper>
</template>
