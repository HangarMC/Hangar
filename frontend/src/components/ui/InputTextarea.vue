<script lang="ts" setup>
import { computed } from "vue";
import type { ValidationRule } from "@vuelidate/core";
import { useValidation } from "~/composables/useValidationHelpers";
import InputWrapper from "~/components/ui/InputWrapper.vue";

const emit = defineEmits<{
  (e: "update:modelValue", value: string): void;
}>();
const value = computed({
  get: () => props.modelValue,
  set: (val) => emit("update:modelValue", val),
});
const props = defineProps<{
  modelValue: string;
  label?: string;
  counter?: boolean;
  maxlength?: number;
  loading?: boolean;
  messages?: string[];
  errorMessages?: string[];
  disabled?: boolean;
  rules?: ValidationRule<string | undefined>[];
  noErrorTooltip?: boolean;
  minRows?: number;
  maxRows?: number;
  extraRows?: number;
}>();

const rows = computed(() => {
  return Math.max(props.minRows || 3, Math.min(props.maxRows || 100, value.value?.split(/\r\n|\r|\n/g).length + (props.extraRows || 0)));
});

const errorMessages = computed(() => props.errorMessages);
const { v, errors, hasError } = useValidation(props.label, props.rules, value, errorMessages);
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
    :no-error-tooltip="noErrorTooltip"
  >
    <template #default="slotProps">
      <textarea v-model="value" v-bind="$attrs" :maxlength="maxlength" :rows="rows" :class="slotProps.class" :disabled="disabled" @blur="v.$touch()" />
    </template>
    <template v-for="(_, name) in $slots" #[name]="slotData">
      <slot :name="name" v-bind="slotData || {}" />
    </template>
  </InputWrapper>
</template>

<style>
textarea {
  resize: none;
}
</style>
