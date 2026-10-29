<script lang="ts" setup>
defineProps<{ title: string; description?: string }>();
const slots = useSlots();
</script>

<template>
  <section>
    <div class="flex items-center gap-2">
      <span v-if="hasSlotContent(slots.icon)" class="flex-shrink-0 text-lg text-gray-secondary">
        <slot name="icon" />
      </span>
      <h3 class="min-w-0 text-lg font-bold">{{ title }}</h3>
    </div>
    <p v-if="description" class="mt-1 text-sm text-gray-secondary">{{ description }}</p>
    <div class="mt-3">
      <slot />
    </div>
  </section>
</template>
