<script lang="ts" setup>
import { computed } from "vue";

const props = withDefaults(
  defineProps<{
    type?: "success" | "info" | "warning" | "danger" | "neutral";
  }>(),
  {
    type: "info",
  }
);

const color = computed(() => {
  // map type to bg-color
  return {
    success: "bg-green-500 border-green-600",
    info: "bg-primary-400 border-primary-500",
    warning: "bg-yellow-500 border-yellow-600",
    danger: "bg-red-500 font-semibold border-red-600",
    neutral: "background-default border dark:border-gray-800",
  }[props.type];
});
</script>

<template>
  <div :class="'flex flex-row items-center rounded-md p-4 text-white border-l-6 border-solid ' + color">
    <slot name="icon" clazz="mr-3 w-8 h-8 min-w-8">
      <IconMdiAlert v-if="props.type === 'danger'" class="mr-3 w-8 h-8 min-w-8" />
      <IconMdiAlertBox v-else-if="props.type === 'warning'" class="mr-3 w-8 h-8 min-w-8" />
      <IconMdiInformation v-else-if="props.type === 'info' || props.type === 'neutral'" class="mr-3 w-8 h-8 min-w-8" />
      <IconMdiTrophy v-else-if="props.type === 'success'" class="mr-3 w-8 h-8 min-w-8" />
    </slot>
    <slot></slot>
  </div>
</template>
