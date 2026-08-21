<script lang="ts" setup>
defineProps<{ label: string; value: number | string }>();
</script>

<template>
  <div class="stat-tile min-w-0 flex flex-1 basis-24 flex-col items-center gap-0.5 rounded-md border border-gray-300 px-2 py-2.5 dark:border-gray-700">
    <span class="text-lg font-bold tabular-nums">{{ typeof value === "number" ? value.toLocaleString("en-US") : value }}</span>
    <span class="max-w-full min-w-0 inline-flex items-center gap-1 text-xs text-gray-secondary">
      <span class="flex-shrink-0 inline-flex"><slot name="icon" /></span>
      <span class="min-w-0 truncate" :title="label">{{ label }}</span>
    </span>
  </div>
</template>

<style scoped>
.stat-tile {
  background-color: var(--surface-sunken);
}
</style>
