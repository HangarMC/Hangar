<script lang="ts" setup>
const props = withDefaults(
  defineProps<{
    accent?: boolean;
    alternateBackground?: boolean;
    flat?: boolean;
    padding?: "none" | "sm" | "md";
  }>(),
  {
    accent: false,
    alternateBackground: false,
    flat: false,
    padding: "md",
  }
);

const clazz = computed(() => {
  return {
    "background-default": !props.alternateBackground,
    "background-card": props.alternateBackground,
    border: true,
    "dark:border-gray-800": true,
    "!border-top-primary": props.accent,
    "shadow-md": !props.flat,
    "rounded-md": true,
    "p-4": props.padding === "md",
    "p-3": props.padding === "sm",
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
