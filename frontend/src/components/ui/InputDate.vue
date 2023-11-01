<script lang="ts" setup>
import { computed } from "vue";
import type { ValidationRule } from "@vuelidate/core";
import { useValidation } from "~/composables/useValidationHelpers";
import InputWrapper from "~/components/ui/InputWrapper.vue";

const emit = defineEmits<{
  (e: "update:modelValue", date: string): void;
}>();
const date = computed({
  get: () => props.modelValue,
  set: (value) => emit("update:modelValue", value),
});
const props = defineProps<{
  modelValue: string;
  label?: string;
  disabled?: boolean;
  loading?: boolean;
  messages?: string[];
  errorMessages?: string[];
  rules?: ValidationRule<string | undefined>[];
  noErrorTooltip?: boolean;
}>();

const errorMessages = computed(() => props.errorMessages);
const { v, errors, hasError } = useValidation(props.label, props.rules, date, errorMessages);
</script>

<template>
  <InputWrapper
    :errors="errors"
    :messages="messages"
    :has-error="hasError"
    :loading="loading || v.$pending"
    :label="label"
    :value="date"
    :disabled="disabled"
    :no-error-tooltip="noErrorTooltip"
  >
    <template #default="slotProps">
      <!-- todo make fancy -->
      <input v-model="date" type="date" v-bind="$attrs" :disabled="disabled" :class="slotProps.class" />
    </template>
    <template v-for="(_, name) in $slots" #[name]="slotData">
      <slot :name="name" v-bind="slotData || {}" />
    </template>
  </InputWrapper>
</template>
