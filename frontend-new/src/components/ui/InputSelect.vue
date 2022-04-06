<script setup lang="ts">
import { computed } from "vue";
import { FloatingLabel, inputClasses } from "~/composables/useInputHelper";
import ErrorTooltip from "~/components/design/ErrorTooltip.vue";

const emit = defineEmits<{
  (e: "update:modelValue", value: object | string | boolean | number | null | undefined): void;
}>();
const internalVal = computed({
  get: () => props.modelValue,
  set: (v) => emit("update:modelValue", v),
});

export interface Option {
  value: object | string | boolean | number;
  text: string;
}

const props = withDefaults(
  defineProps<{
    modelValue: object | string | boolean | number | null;
    values: Option[] | Record<string, any> | string[];
    itemValue?: string;
    itemText?: string;
    disabled?: boolean;
    label?: string;
    errorMessages?: string[];
  }>(),
  {
    modelValue: "",
    itemValue: "value",
    itemText: "text",
    label: "",
    errorMessages: () => [],
  }
);

// TODO proper validation
const error = computed<boolean>(() => {
  return props.errorMessages ? props.errorMessages.length > 0 : false;
});
</script>

<template>
  <ErrorTooltip :error-messages="errorMessages" class="w-full">
    <label class="relative flex" :class="{ filled: internalVal, error: error }">
      <select v-model="internalVal" :disabled="disabled" :class="inputClasses">
        <option v-for="val in values" :key="val[itemValue] || val" :value="val[itemValue] || val" class="dark:bg-[#191e28]">
          {{ val[itemText] || val }}
        </option>
      </select>
      <floating-label :label="label" />
    </label>
  </ErrorTooltip>
</template>
