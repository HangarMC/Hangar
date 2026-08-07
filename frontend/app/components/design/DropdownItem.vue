<script setup lang="ts">
import { NuxtLink } from "#components";

const props = withDefaults(
  defineProps<{
    to?: string;
    href?: string;
    disabled?: boolean;
    selected?: boolean;
    tone?: "neutral" | "danger";
  }>(),
  {
    to: undefined,
    href: undefined,
    disabled: false,
    selected: false,
    tone: "neutral",
  }
);

const type = computed(() => {
  if (props.disabled) {
    return "span";
  }
  if (props.to) {
    return NuxtLink;
  } else if (props.href) {
    return "a";
  } else {
    return "button";
  }
});

// we can't pass href as undefined, else links aren't middle clickable, so we gotta use this computed...
const attrs = computed(() => {
  if (props.disabled) {
    return {};
  } else if (props.to) {
    return { to: props.to };
  } else if (props.href) {
    return { href: props.href };
  } else {
    return { type: "button" };
  }
});
</script>

<template>
  <component
    :is="type"
    class="dropdown-item w-full flex flex-shrink-0 items-center gap-2 rounded px-2.5 py-1.5 text-left text-sm font-medium transition-colors"
    :class="[
      disabled ? 'cursor-not-allowed text-gray-secondary' : 'cursor-pointer hover:background-card',
      tone === 'danger' ? 'text-red-600 dark:text-red-400' : '',
      selected ? 'background-card' : '',
    ]"
    v-bind="attrs"
  >
    <slot />
    <IconMdiCheck v-if="selected" class="ml-auto flex-shrink-0 color-primary" />
  </component>
</template>

<style scoped>
.dropdown-item:focus-visible {
  outline: 2px solid var(--primary-500);
  outline-offset: -2px;
}
</style>
