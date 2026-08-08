<script lang="ts" setup>
const props = withDefaults(
  defineProps<{
    type?: "success" | "info" | "warning" | "danger" | "neutral";
  }>(),
  {
    type: "info",
  }
);

const color = computed(() => {
  // tinted surface + matching border, consistent with the toast notifications
  return {
    success: "bg-green-50 dark:bg-green-500/10 border-green-500 text-green-900 dark:text-green-100",
    info: "bg-sky-50 dark:bg-sky-500/10 border-sky-500 text-sky-900 dark:text-sky-100",
    warning: "bg-amber-50 dark:bg-amber-500/10 border-amber-500 text-amber-900 dark:text-amber-100",
    danger: "bg-red-50 dark:bg-red-500/10 border-red-500 text-red-900 dark:text-red-100 font-semibold",
    neutral: "background-default border dark:border-gray-800",
  }[props.type];
});

const iconColor = computed(() => {
  return {
    success: "text-green-600 dark:text-green-400",
    info: "text-sky-600 dark:text-sky-400",
    warning: "text-amber-600 dark:text-amber-500",
    danger: "text-red-600 dark:text-red-400",
    neutral: "text-gray-secondary",
  }[props.type];
});
</script>

<template>
  <div :class="'flex flex-row items-center rounded-md p-4 border-l-6 border-solid ' + color" :role="props.type === 'danger' ? 'alert' : 'status'">
    <slot name="icon" :clazz="'mr-3 w-8 h-8 min-w-8 ' + iconColor">
      <IconMdiAlert v-if="props.type === 'danger'" class="mr-3 w-8 h-8 min-w-8" :class="iconColor" />
      <IconMdiAlertBox v-else-if="props.type === 'warning'" class="mr-3 w-8 h-8 min-w-8" :class="iconColor" />
      <IconMdiInformation v-else-if="props.type === 'info' || props.type === 'neutral'" class="mr-3 w-8 h-8 min-w-8" :class="iconColor" />
      <IconMdiTrophy v-else-if="props.type === 'success'" class="mr-3 w-8 h-8 min-w-8" :class="iconColor" />
    </slot>
    <slot />
  </div>
</template>
