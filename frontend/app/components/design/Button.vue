<script lang="ts" setup>
import type { RouteLocationRaw } from "vue-router";
import { NuxtLink } from "#components";

defineEmits<{
  click: [event: MouseEvent];
}>();
const props = withDefaults(
  defineProps<{
    disabled?: boolean;
    size?: "small" | "medium" | "large";
    buttonType?: "primary" | "secondary" | "red" | "transparent" | "borderless";
    loading?: boolean;
    to?: string | RouteLocationRaw | object;
    href?: string;
  }>(),
  {
    disabled: false,
    size: "small",
    buttonType: "primary",
    loading: false,
    to: undefined,
    href: undefined,
  }
);
const paddingClass = computed<string>(() => {
  switch (props.size) {
    case "large":
      return "p-3 space-x-1.2";

    case "medium":
      return "p-2 space-x-0.7";

    default:
      return "p-1 px-2 space-x-0.7";
  }
});

const classes = computed<string>(() => {
  const isTextButton = props.buttonType === "transparent" || props.buttonType === "borderless";
  const border = props.buttonType === "borderless" ? "border-1 border-transparent" : "border-1 border-gray-800";
  const interaction =
    props.buttonType === "borderless" ? "hover:border-gray-300 hover:bg-gray-100 dark:hover:border-gray-700 dark:hover:bg-gray-800" : "";
  const colors = isTextButton
    ? "text-black dark:text-white disabled:cursor-not-allowed disabled:text-gray-400"
    : "text-white disabled:(bg-gray-300 cursor-not-allowed) disabled:dark:(text-gray-500 bg-charcoal-600)";
  const loading = props.loading ? "!cursor-wait" : "cursor-pointer";

  return [
    "rounded-lg font-semibold inline-flex items-center justify-center transition-all duration-250",
    border,
    interaction,
    colors,
    paddingClass.value,
    `button-${props.buttonType}`,
    loading,
  ].join(" ");
});
</script>

<template>
  <component
    :is="to ? NuxtLink : href ? 'a' : 'button'"
    :class="classes"
    :style="
      buttonType === 'primary'
        ? {
            backgroundColor: 'color-mix(in srgb, var(--primary-500) 25%, transparent)',
            borderColor: 'var(--primary-500)',
          }
        : {}
    "
    :disabled="disabled || loading"
    :to="to"
    :href="href"
    v-bind="$attrs"
    @click="$emit('click', $event)"
  >
    <slot />
  </component>
</template>
