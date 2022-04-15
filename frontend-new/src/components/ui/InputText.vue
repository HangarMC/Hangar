<script lang="ts" setup>
import { computed } from "vue";
import { FloatingLabel, inputClasses } from "~/composables/useInputHelper";
import ErrorTooltip from "~/components/design/ErrorTooltip.vue";
import { ValidationRule } from "@vuelidate/core";
import { useValidation } from "~/composables/useValidationHelpers";

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
  errorMessages?: string[];
  rules?: ValidationRule<string | undefined>[];
}>();

const { v, errors, hasError } = useValidation(props.label, props.rules, value, props.errorMessages);
</script>

<template>
  <ErrorTooltip :error-messages="errors" class="w-full">
    <label class="relative flex" :class="{ filled: modelValue, error: hasError }">
      <input v-model="value" type="text" :class="inputClasses" v-bind="$attrs" :maxlength="maxlength" @blur="v.$touch()" />
      <span v-if="counter && maxlength" class="inline-flex items-center ml-2">{{ value?.length || 0 }}/{{ maxlength }}</span>
      <span v-else-if="counter">{{ value?.length || 0 }}</span>
      <floating-label :label="label" /> </label
  ></ErrorTooltip>
</template>
