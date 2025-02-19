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
    buttonType?: "primary" | "secondary" | "red" | "transparent";
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
  const button = " button-" + props.buttonType;
  const loading = props.loading ? " !cursor-wait" : " cursor-pointer";
  return (
    "rounded-md font-semibold h-min inline-flex items-center justify-center " +
    (props.buttonType === "transparent"
      ? "text-black dark:text-white disabled:cursor-not-allowed disabled:text-gray-400 "
      : "text-white disabled:(bg-gray-300 cursor-not-allowed) disabled:dark:(text-gray-500 bg-gray-700) ") +
    paddingClass.value +
    button +
    loading
  );
});
</script>

<template>
  <component
    :is="to ? NuxtLink : href ? 'a' : 'button'"
    :class="classes"
    :disabled="disabled || loading"
    :to="to"
    :href="href"
    v-bind="$attrs"
    @click="$emit('click', $event)"
  >
    <slot />
    <span v-if="loading" class="pl-1"><Spinner class="stroke-gray-400" :diameter="1" :stroke="0.01" unit="rem" /></span>
  </component>
</template>
