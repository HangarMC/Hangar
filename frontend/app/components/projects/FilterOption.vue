<script setup lang="ts">
defineProps<{
  label: string;
  selected: boolean;
}>();

defineEmits<{
  toggle: [];
}>();
</script>

<template>
  <button
    type="button"
    class="flex w-full items-center gap-2 rounded-md border px-2 py-0.5 text-left transition-colors"
    :class="selected ? 'border-gray-400 background-card font-semibold dark:border-gray-600' : 'border-transparent hover:background-card'"
    :aria-pressed="selected"
    @click="$emit('toggle')"
  >
    <slot />
    <span class="min-w-0 truncate">{{ label }}</span>
  </button>
</template>
