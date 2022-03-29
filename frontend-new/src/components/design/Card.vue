<script lang="ts" setup>
import { hasSlotContent } from "~/composables/useSlot";
import { computed } from "vue";

const props = withDefaults(
  defineProps<{
    accent?: boolean;
  }>(),
  {
    accent: false,
  }
);

const clazz = computed(() => {
  return {
    "bg-white": true,
    "dark:bg-background-dark-90": true,
    border: true,
    "dark:border-neutral-800": true,
    "!border-top-primary": props.accent,
    "shadow-xl": true,
    "rounded-md": true,
    "p-4": true,
    "overflow-auto": true,
  };
});
</script>

<template>
  <div :class="clazz">
    <div v-if="hasSlotContent($slots.header)" class="text-xl font-bold mb-2">
      <slot name="header"></slot>
    </div>
    <slot name="default"></slot>
    <div v-if="hasSlotContent($slots.footer)" class="mt-2">
      <slot name="footer"></slot>
    </div>
  </div>
</template>
