<script lang="ts" setup>
import type { RouteLocationRaw } from "vue-router";
import { NuxtLink } from "#components";

defineEmits<{
  click: [event: MouseEvent];
}>();

const props = withDefaults(
  defineProps<{
    variant?: "solid" | "outline" | "ghost";
    /**
    `danger` is reserved for irreversible actions -- never for "back" or "cancel".
    */
    tone?: "primary" | "neutral" | "danger";
    size?: "sm" | "md" | "lg";
    /**
    Square button holding a single icon. Requires an `aria-label`.
    */
    iconOnly?: boolean;
    disabled?: boolean;
    loading?: boolean;
    to?: string | RouteLocationRaw | object;
    href?: string;
  }>(),
  {
    variant: "solid",
    tone: "primary",
    size: "md",
    iconOnly: false,
    disabled: false,
    loading: false,
    to: undefined,
    href: undefined,
  }
);

const isLink = computed<boolean>(() => Boolean(props.to || props.href));
const isDisabled = computed<boolean>(() => props.disabled || props.loading);

// Heights line up with the ~36px inputs rendered by InputWrapper.
const sizeClasses = {
  sm: { box: "h-8 px-3 text-sm", square: "h-8 w-8 text-sm", gap: "gap-1.5" },
  md: { box: "h-9 px-4 text-sm", square: "h-9 w-9 text-sm", gap: "gap-2" },
  lg: { box: "h-11 px-5 text-base", square: "h-11 w-11 text-base", gap: "gap-2" },
} as const;

const classes = computed(() => [
  "hangar-btn relative inline-flex items-center justify-center rounded-md font-semibold whitespace-nowrap transition-colors",
  props.iconOnly ? sizeClasses[props.size].square : sizeClasses[props.size].box,
  `btn-${props.variant}-${props.tone}`,
  isDisabled.value ? "opacity-50 cursor-not-allowed" : "cursor-pointer",
  // `disabled` does nothing on an anchor
  isDisabled.value && isLink.value ? "pointer-events-none" : "",
]);
</script>

<template>
  <component
    :is="to ? NuxtLink : href ? 'a' : 'button'"
    :class="classes"
    :disabled="isLink ? undefined : isDisabled"
    :aria-disabled="isLink && isDisabled ? 'true' : undefined"
    :aria-busy="loading ? 'true' : undefined"
    :tabindex="isLink && isDisabled ? -1 : undefined"
    :to="to"
    :href="href"
    v-bind="$attrs"
    @click="$emit('click', $event)"
  >
    <!-- Kept in flow while loading so the button never changes width mid-action. -->
    <span :class="['inline-flex items-center justify-center', sizeClasses[size].gap, loading ? 'invisible' : '']">
      <slot />
    </span>
    <span v-if="loading" class="absolute inset-0 flex items-center justify-center">
      <Spinner class="stroke-current" :diameter="1.15" :stroke="0.12" unit="em" />
    </span>
  </component>
</template>

<style scoped>
/* em-based, so call sites need no margin/size classes on icons. */
.hangar-btn :deep(svg) {
  width: 1.15em;
  height: 1.15em;
  flex-shrink: 0;
}

/* Written out rather than composed from utilities: `outline-2` only sets a width,
   which leaves outline-style at `none` and renders nothing. */
.hangar-btn:focus-visible {
  outline: 2px solid var(--primary-500);
  outline-offset: 2px;
}
</style>
