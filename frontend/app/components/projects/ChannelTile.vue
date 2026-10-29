<script setup lang="ts">
withDefaults(
  defineProps<{
    channel: { name: string; color: string; description?: string };
    size?: "sm" | "md";
  }>(),
  { size: "sm" }
);
</script>

<template>
  <Tooltip class="flex-shrink-0">
    <template #content>
      {{ channel.name }}<template v-if="channel.description"> — {{ channel.description }}</template>
    </template>
    <span
      class="channel-tile flex items-center justify-center rounded-md font-bold"
      :class="size === 'md' ? 'h-10 w-10 text-lg' : 'h-8 w-8 text-sm'"
      :style="{ '--channel': channel.color }"
      :aria-label="channel.name"
    >
      {{ channel.name.charAt(0).toUpperCase() }}
    </span>
  </Tooltip>
</template>

<style>
/* Tinted rather than solid: the raw channel colour as text on its own tint only reached 2.4:1 in light mode. */
.channel-tile {
  background-color: color-mix(in srgb, var(--channel) 16%, transparent);
  color: color-mix(in srgb, var(--channel) 72%, black);
}

.dark .channel-tile {
  color: color-mix(in srgb, var(--channel) 82%, white);
}
</style>
