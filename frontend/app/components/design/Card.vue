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
    "border-gray-300": true,
    "dark:border-gray-700": true,
    "!border-top-primary": props.accent,
    "card-raised": !props.flat,
    "rounded-lg": true,
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

<style scoped>
/* shadow-md composited to fully transparent in dark mode, so the elevation is spelled out here */
.card-raised {
  box-shadow:
    0 1px 2px rgb(0 0 0 / 0.04),
    0 4px 12px -6px rgb(0 0 0 / 0.1);
}

.dark .card-raised {
  box-shadow:
    0 1px 2px rgb(0 0 0 / 0.3),
    0 6px 16px -8px rgb(0 0 0 / 0.5);
}
</style>
