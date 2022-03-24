<script lang="ts" setup>
import { computed } from "vue";

const emit = defineEmits<{
  (e: "update:modelValue", value: string): void;
}>();
const value = computed({
  get: () => props.modelValue,
  set: (v) => emit("update:modelValue", v),
});
const props = defineProps<{
  modelValue: string;
  label?: string;
}>();
</script>

<template>
  <!-- todo make fancy -->
  <label>
    <template v-if="label">{{ label }}</template>
    <input v-model="value" type="text" :class="'w-full' + (label ? ' ml-2' : '')" v-bind="$attrs" />
  </label>
</template>
