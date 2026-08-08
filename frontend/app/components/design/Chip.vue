<script setup lang="ts">
withDefaults(
  defineProps<{
    tone?: "neutral" | "amber" | "green" | "red" | "primary";
  }>(),
  {
    tone: "neutral",
  }
);

const toneClasses = {
  neutral: "chip-neutral text-gray-700 dark:text-gray-300",
  amber: "bg-amber-500/15 text-amber-700 dark:text-amber-300",
  green: "bg-green-500/15 text-green-700 dark:text-green-300",
  red: "bg-red-500/15 text-red-700 dark:text-red-300",
  primary: "chip-primary color-primary",
} as const;
</script>

<template>
  <span class="hangar-chip inline-flex flex-shrink-0 items-center gap-1 rounded-full px-2 py-0.5 text-xs font-semibold leading-5" :class="toneClasses[tone]">
    <slot />
  </span>
</template>

<style scoped>
.chip-neutral {
  background-color: color-mix(in srgb, var(--gray-500) 15%, transparent);
}

.chip-primary {
  background-color: color-mix(in srgb, var(--primary-500) 15%, transparent);
}

.hangar-chip :deep(svg) {
  width: 1.1em;
  height: 1.1em;
  flex-shrink: 0;
}
</style>
