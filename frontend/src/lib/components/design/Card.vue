<script lang="ts" setup>
import { hasSlotContent } from "~/lib/composables/useSlot";
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
    "background-default": true,
    border: true,
    "dark:border-gray-800": true,
    "!border-top-primary": props.accent,
    "shadow-md": true,
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
