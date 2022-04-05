<script setup lang="ts">
import { computed } from "vue";
import { FloatingLabel, inputClasses } from "~/composables/useInputHelper";

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

const props = defineProps<{
  modelValue: object | string | boolean | number | null | undefined;
  values: Option[];
  disabled?: boolean;
  label?: string;
  errorMessages?: string[];
}>();

// TODO proper validation
const error = computed<boolean>(() => {
  return props.errorMessages ? props.errorMessages.length > 0 : false;
});
</script>

<!-- todo make fancy -->
<template>
  <label class="relative flex" :class="{ filled: modelValue, error: error }">
    <select v-model="internalVal" :disabled="disabled" :class="inputClasses">
      <option v-for="val in values" :key="val.value" :value="val.value" class="dark:bg-[#191e28]">
        {{ val.text }}
      </option>
    </select>
    <floating-label :label="label" />
  </label>
  <template v-if="errorMessages && errorMessages.length > 0">
    <span v-for="msg in errorMessages" :key="msg" class="text-red-500">{{ msg }}</span>
  </template>
</template>
