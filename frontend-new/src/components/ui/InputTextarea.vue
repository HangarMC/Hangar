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
  <label class="flex">
    <span v-if="label" class="block">{{ label }}</span>
    <textarea v-model="value" class="ml-2 flex-grow flex-shrink" v-bind="$attrs" />
  </label>
</template>
