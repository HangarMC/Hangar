<script lang="ts" setup>
import { computed } from "vue";
import { sanitizeUrl } from "~/composables/useUrlHelper";

const props = withDefaults(
  defineProps<{
    to?: string | object;
    href?: string | null;
    activeUnderline?: boolean;
    disabled?: boolean;
  }>(),
  {
    activeUnderline: false,
    to: undefined,
    href: undefined,
    disabled: false,
  }
);
const classes = computed<string>(() => "font-bold " + (props.disabled ? "color-gray-400 cursor-not-allowed" : "color-primary hover:(underline)"));
</script>

<template>
  <NuxtLink v-if="to" :to="to" :class="classes" v-bind="$attrs" :active-class="props.activeUnderline ? 'underline' : ''">
    <slot></slot>
  </NuxtLink>
  <a v-else-if="href" :href="sanitizeUrl(href)" :class="classes" v-bind="$attrs">
    <slot></slot>
  </a>
  <span v-else :class="classes" v-bind="$attrs" cursor="pointer">
    <slot></slot>
  </span>
</template>
