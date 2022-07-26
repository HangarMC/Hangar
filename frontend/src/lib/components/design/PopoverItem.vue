<script setup lang="ts">
import { computed } from "vue";

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
    return "router-link";
  } else if (props.href) {
    return "a";
  } else {
    return "p";
  }
});

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
    :class="'px-4 py-2 font-semibold hover:(bg-gray-100 dark:bg-gray-700) ' + (disabled ? 'cursor-not-allowed text-opacity-50' : 'cursor-pointer')"
    v-bind="attrs"
  >
    <slot></slot>
  </component>
</template>
