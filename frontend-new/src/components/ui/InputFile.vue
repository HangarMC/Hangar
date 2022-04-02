<script lang="ts" setup>
import { computed } from "vue";

const emit = defineEmits<{
  (e: "update:modelValue", file: string): void;
}>();
const file = computed({
  get: () => props.modelValue,
  set: (value) => emit("update:modelValue", value),
});
const props = defineProps<{
  modelValue: string;
  showSize?: boolean;
}>();
</script>

<template>
  <!-- todo make fancy, implement functionality -->
  <input type="file" v-bind="$attrs" />
</template>
