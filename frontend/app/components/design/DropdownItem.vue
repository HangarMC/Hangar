<script setup lang="ts">
import { NuxtLink } from "#components";

const props = withDefaults(
  defineProps<{
    to?: string;
    href?: string;
    disabled?: boolean;
  }>(),
  {
    to: undefined,
    href: undefined,
    disabled: false,
  }
);

const type = computed(() => {
  if (props.disabled) {
    return "p";
  }
  if (props.to) {
    return NuxtLink;
  } else if (props.href) {
    return "a";
  } else {
    return "p";
  }
});

// we can't pass href as undefined, else links aren't middle clickable, so we gotta use this computed...
const attrs = computed(() => {
  if (props.to) {
    return {
      to: props.to,
    };
  } else if (props.href) {
    return {
      href: props.href,
    };
  } else {
    return {};
  }
});
</script>

<template>
  <component
    :is="type"
    :class="
      'whitespace-nowrap rounded-lg border border-transparent px-2 py-1 font-semibold decoration-none transition-all duration-250 hover:border-gray-300 hover:bg-gray-100 dark:hover:border-gray-700 dark:hover:bg-gray-800 ' +
      (disabled ? 'cursor-not-allowed text-opacity-50' : 'cursor-pointer')
    "
    v-bind="attrs"
  >
    <slot />
  </component>
</template>
