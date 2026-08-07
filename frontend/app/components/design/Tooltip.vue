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
  /* the popper is teleported to <body>, so pin the type down rather than inheriting whatever is there */
  max-width: 24rem;
  padding: 0.375rem 0.625rem;
  border-radius: 0.375rem;
  border: 1px solid #3f3f46;
  background-color: #27272a;
  box-shadow:
    0 8px 20px -6px rgb(0 0 0 / 0.45),
    0 2px 6px -2px rgb(0 0 0 / 0.3);
  color: #fafafa;
  font-size: 0.875rem;
  font-weight: 500;
  line-height: 1.35;
  text-align: left;
  text-wrap: pretty;
}

.dark .v-popper--theme-tooltip .v-popper__inner {
  border-color: #52525b;
  background-color: #3f3f46;
}

.v-popper--theme-tooltip .v-popper__arrow-outer {
  border-color: #3f3f46;
}

.v-popper--theme-tooltip .v-popper__arrow-inner {
  border-color: #27272a;
}

.dark .v-popper--theme-tooltip .v-popper__arrow-outer {
  border-color: #52525b;
}

.dark .v-popper--theme-tooltip .v-popper__arrow-inner {
  border-color: #3f3f46;
}
</style>
