<script lang="ts" setup>
import { ValidationRule } from "@vuelidate/core";
import { useValidation } from "~/composables/useValidationHelpers";
import ErrorTooltip from "~/components/design/ErrorTooltip.vue";
import { computed } from "vue";

const emit = defineEmits<{
  (e: "update:modelValue", value?: string[] | boolean[] | number[] | object[]): void;
}>();
const value = computed({
  get: () => props.modelValue,
  set: (val) => emit("update:modelValue", val),
});
const props = defineProps<{
  modelValue?: string[] | boolean[] | number[] | object[];
  label?: string;
  errorMessages?: string[];
  rules?: ValidationRule<string | undefined>[];
}>();

const { v, errors, hasError } = useValidation(props.label, props.rules, value, props.errorMessages, true);
</script>

<template>
  <div>
    <label v-if="label" class="block" @click="v.$validate()">{{ label }}</label>
    <ErrorTooltip :error-messages="errors" class="w-full">
      <slot />
    </ErrorTooltip>
  </div>
</template>
