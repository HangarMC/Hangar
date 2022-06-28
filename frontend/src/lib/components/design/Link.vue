<script lang="ts" setup>
import { computed } from "vue";

const props = withDefaults(
  defineProps<{
    to?: string | object;
    href?: string;
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
  <router-link v-if="to" :to="to" :class="classes" v-bind="$attrs" :active-class="props.activeUnderline ? 'underline' : ''">
    <slot></slot>
  </router-link>
  <a v-else :href="href" :class="classes" v-bind="$attrs">
    <slot></slot>
  </a>
</template>
