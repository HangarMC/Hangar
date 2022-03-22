<script setup lang="ts">
import { computed } from "vue";
import { MenuItem } from "@headlessui/vue";

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
</script>

<template>
  <MenuItem>
    <component
      :is="type"
      :class="
        'px-4 py-2 font-semibold hover:(bg-background-light-10 dark:bg-background-dark-90) ' +
        (disabled ? 'cursor-not-allowed text-opacity-50' : 'cursor-pointer')
      "
      :href="href"
      :to="to"
    >
      <slot></slot>
    </component>
  </MenuItem>
</template>
