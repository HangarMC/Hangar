<script lang="ts" setup>
import { computed } from "vue";
import { ValidationRule } from "@vuelidate/core";
import { useValidation } from "~/composables/useValidationHelpers";
import InputWrapper from "~/components/ui/InputWrapper.vue";

const emit = defineEmits<{
  (e: "update:modelValue", value?: string): void;
}>();
const value = computed({
  get: () => props.modelValue,
  set: (val) => emit("update:modelValue", val),
});
const props = defineProps<{
  modelValue?: string;
  label?: string;
  counter?: boolean;
  maxlength?: number;
  loading?: boolean;
  errorMessages?: string[];
  rules?: ValidationRule<string | undefined>[];
}>();

const { v, errors, hasError } = useValidation(props.label, props.rules, value, props.errorMessages);

defineExpose({ v });
</script>

<template>
  <InputWrapper
    v-slot="slotProps"
    :errors="errors"
    :has-error="hasError"
    :counter="counter"
    :maxlength="maxlength"
    :loading="loading || v.$pending"
    :label="label"
    :value="value"
  >
    <input v-model="value" type="text" v-bind="$attrs" :maxlength="maxlength" :class="slotProps.class" @blur="v.$touch()" />
  </InputWrapper>
</template>
