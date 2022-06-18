<script lang="ts" setup>
import { computed } from "vue";

const props = withDefaults(
  defineProps<{
    type: "success" | "info" | "warning" | "danger";
  }>(),
  {
    type: "info",
  }
);

const color = computed(() => {
  // map type to bg-color
  return {
    success: "bg-green-500",
    info: "bg-blue-500",
    warning: "bg-yellow-500",
    danger: "bg-red-500",
  }[props.type];
});
</script>

<template>
  <div :class="'rounded p-4 ' + color">
    <slot></slot>
  </div>
</template>
