<script lang="ts" setup>
import type { ValidationRule } from "@vuelidate/core";
import { computed } from "vue";
import { useValidation } from "~/composables/useValidationHelpers";
import ErrorTooltip from "~/components/design/ErrorTooltip.vue";

const emit = defineEmits<{
  (e: "update:modelValue", value?: string[] | boolean[] | number[] | object[]): void;
}>();
const value = computed({
  get: () => props.modelValue,
  set: (val) => emit("update:modelValue", val),
});
const props = withDefaults(
  defineProps<{
    modelValue?: string[] | boolean[] | number[] | object[];
    label?: string;
    errorMessages?: string[];
    rules?: ValidationRule<string | undefined>[];
    silentErrors?: boolean;
    fullWidth?: boolean;
  }>(),
  {
    modelValue: undefined,
    label: undefined,
    errorMessages: undefined,
    rules: undefined,
    silentErrors: true,
    fullWidth: false,
  }
);

const errorMessages = computed(() => props.errorMessages);
const { v, errors } = useValidation(props.label, props.rules, value, errorMessages, props.silentErrors);
</script>

<template>
  <label v-if="label" class="block mb-2" v-bind="$attrs" @click="v.$validate()">{{ label }}</label>
  <div :class="fullWidth ? 'w-full' : 'w-min'">
    <ErrorTooltip :error-messages="errors" class="w-full">
      <slot />
    </ErrorTooltip>
  </div>
</template>
