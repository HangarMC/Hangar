<script setup lang="ts">
import { computed } from "vue";

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
}>();
</script>

<!-- todo make fancy -->
<template>
  <label>
    <template v-if="label">{{ label }}</template>
    <select v-model="internalVal" :disabled="disabled">
      <option v-for="val in values" :key="val.value" :value="val.value">{{ val.text }}</option>
    </select>
  </label>
</template>
