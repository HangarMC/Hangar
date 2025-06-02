<script lang="ts" setup>
import type { ValidationRule } from "@vuelidate/core";

const emit = defineEmits<{
  (e: "update:modelValue", value?: string | null): void;
}>();
const value = computed({
  get: () => props.modelValue,
  set: (val) => emit("update:modelValue", val),
});
const props = defineProps<{
  modelValue?: string | null;
  label?: string;
  counter?: boolean;
  maxlength?: number;
  loading?: boolean;
  messages?: string[];
  errorMessages?: string[];
  disabled?: boolean;
  rules?: ValidationRule<string | undefined>[];
  noErrorTooltip?: boolean;
  readonly?: boolean;
}>();

const errorMessages = computed(() => props.errorMessages);
const { v, errors, hasError } = useValidation(props.label, props.rules, value, errorMessages, false, props.readonly);

defineExpose({ validation: v });
</script>

<template>
  <InputWrapper
    :errors="errors"
    :messages="messages"
    :has-error="hasError"
    :counter="counter"
    :maxlength="maxlength"
    :loading="loading || v.$pending"
    :label="label"
    :value="value"
    :disabled="disabled"
    :readonly="readonly"
    :no-error-tooltip="noErrorTooltip"
  >
    <template #default="slotProps">
      <input v-model="value" class="ml-4 focus:outline-none" type="text" v-bind="$attrs" :maxlength="maxlength" :class="slotProps.class" :disabled :readonly @input="v.$touch" />
    </template>
    <!-- @vue-ignore -->
    <template v-for="(_, name) in $slots" #[name]="slotData">
      <slot :name="name" v-bind="slotData || {}" />
    </template>
  </InputWrapper>
</template>
