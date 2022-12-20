<script lang="ts" setup>
import { type ValidationRule } from "@vuelidate/core";
import { computed } from "vue";
import { useValidation } from "~/lib/composables/useValidationHelpers";
import ErrorTooltip from "~/lib/components/design/ErrorTooltip.vue";

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

const errorMessages = computed(() => props.errorMessages);
const { v, errors } = useValidation(props.label, props.rules, value, errorMessages, true);
</script>

<template>
  <div>
    <label v-if="label" class="block" @click="v.$validate()">{{ label }}</label>
    <ErrorTooltip :error-messages="errors" class="w-full">
      <slot />
    </ErrorTooltip>
  </div>
</template>
