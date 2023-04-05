<script lang="ts" setup>
import { computed } from "vue";
import { hasSlotContent } from "~/composables/useSlot";

const props = withDefaults(
  defineProps<{
    accent?: boolean;
    alternateBackground?: boolean;
  }>(),
  {
    accent: false,
    alternateBackground: false,
  }
);

const clazz = computed(() => {
  return {
    "background-default": !props.alternateBackground,
    "background-card": props.alternateBackground,
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
      <slot name="header" />
    </div>
    <slot name="default" />
    <div v-if="hasSlotContent($slots.footer)" class="mt-2">
      <slot name="footer" />
    </div>
  </div>
</template>
