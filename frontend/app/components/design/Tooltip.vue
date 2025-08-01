<script lang="ts" setup>
import { Tooltip } from "floating-vue";

withDefaults(
  defineProps<{
    hover?: boolean;
    show?: boolean;
  }>(),
  {
    hover: true,
    show: undefined,
  }
);
</script>

<template>
  <!-- hardcoding the id is meh, but else hydration breaks and it doesn't actually seem to be used for accessibility? -->
  <Tooltip :triggers="hover ? ['hover'] : []" :delay="{ show: 200, hide: 100 }" :shown="show" aria-id="tooltip">
    <slot />
    <template #popper>
      <slot name="content" />
    </template>
  </Tooltip>
</template>

<style>
.v-popper--theme-tooltip {
  display: inline-block;
}

.v-popper--theme-tooltip .v-popper__inner {
  max-width: 700px;
  @apply max-w-2xl text-white bg-charcoal-500 border border-gray-700 rounded-full py-2 px-4;
}

.v-popper--theme-tooltip .v-popper__arrow-outer {
  @apply border-gray-700;
}
</style>
