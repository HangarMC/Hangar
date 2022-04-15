<script lang="ts" setup>
import { computed } from "vue";
import { FloatingLabel, inputClasses } from "~/composables/useInputHelper";
import ErrorTooltip from "~/components/design/ErrorTooltip.vue";
import { useValidation } from "~/composables/useValidationHelpers";
import { ValidationRule } from "@vuelidate/core";

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
  errorMessages?: string[];
  rules?: ValidationRule<string | undefined>[];
}>();

const { v, errors, hasError } = useValidation(props.label, props.rules, value, props.errorMessages);
</script>

<template>
  <ErrorTooltip :error-messages="errors" class="w-full">
    <label class="relative flex" :class="{ filled: modelValue, error: hasError }">
      <textarea v-model="value" :class="inputClasses" v-bind="$attrs" :maxlength="maxlength" @blur="v.$touch()" />
      <floating-label :label="label" />
    </label>
    <div v-if="counter" class="mt-1 mb-2">
      <span v-if="maxlength">{{ value?.length || 0 }}/{{ maxlength }}</span>
      <span v-else>{{ value?.length || 0 }}</span>
    </div>
  </ErrorTooltip>
</template>
