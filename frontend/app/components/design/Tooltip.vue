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
  /* teleported to <body>, so pin the type down rather than inheriting whatever is there */
  max-width: 24rem;
  padding: 0.4375rem 0.6875rem;
  border-radius: 0.375rem;
  border: 1px solid var(--gray-300);
  background-color: var(--gray-50);
  box-shadow:
    0 8px 20px -6px rgb(0 0 0 / 0.25),
    0 2px 6px -2px rgb(0 0 0 / 0.15);
  color: var(--gray-900);
  font-size: 0.9375rem;
  font-weight: 500;
  line-height: 1.4;
  text-align: left;
  text-wrap: pretty;
}

.dark .v-popper--theme-tooltip .v-popper__inner {
  border-color: var(--gray-700);
  background-color: var(--gray-800);
  box-shadow:
    0 8px 20px -6px rgb(0 0 0 / 0.5),
    0 2px 6px -2px rgb(0 0 0 / 0.35);
  color: var(--gray-50);
}

.v-popper--theme-tooltip .v-popper__arrow-outer {
  border-color: var(--gray-300);
}

.v-popper--theme-tooltip .v-popper__arrow-inner {
  border-color: var(--gray-50);
}

.dark .v-popper--theme-tooltip .v-popper__arrow-outer {
  border-color: var(--gray-700);
}

.dark .v-popper--theme-tooltip .v-popper__arrow-inner {
  border-color: var(--gray-800);
}
</style>
