<script lang="ts" setup>
import { computed } from "vue";

const emit = defineEmits<{
  (e: "update:modelValue", date: string): void;
}>();
const date = computed({
  get: () => props.modelValue,
  set: (value) => emit("update:modelValue", value),
});
const props = defineProps<{
  modelValue: string;
}>();
</script>

<template>
  <!-- todo make fancy -->
  <input v-model="date" type="date" />
</template>
