<script lang="ts" setup>
interface Color {
  foreground?: string;
  background?: string;
}

const props = defineProps<{
  name?: string;
  color?: Color;
  tooltip?: string;
}>();

const settingsStore = useSettingsStore();

const label = computed(() => props.color?.foreground ?? readableAccent(props.color?.background, settingsStore.darkMode ?? false));
</script>

<template>
  <span
    class="role-tag inline-flex flex-shrink-0 items-center gap-1 rounded-full border px-2 py-0.5 text-xs font-semibold leading-5"
    :class="color?.background ? 'border-tinted' : 'border-gray-300 text-gray-secondary dark:border-gray-600'"
    :style="color?.background ? { '--tag-color': color.background, color: label } : undefined"
    :title="tooltip"
  >
    {{ name }}
  </span>
</template>

<style scoped>
.border-tinted {
  background-color: color-mix(in srgb, var(--tag-color) 15%, transparent);
  border-color: color-mix(in srgb, var(--tag-color) 40%, transparent);
}
</style>
