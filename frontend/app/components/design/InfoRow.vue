<script lang="ts" setup>
defineProps<{ label: string }>();
</script>

<template>
  <div class="flex items-center gap-3">
    <span class="flex-shrink-0 text-xl text-gray-secondary">
      <slot name="icon" />
    </span>
    <div class="min-w-0 flex-1">
      <div class="text-xs text-gray-secondary">{{ label }}</div>
      <div class="min-w-0 truncate font-semibold">
        <slot />
      </div>
    </div>
  </div>
</template>
