<script lang="ts" setup>
const props = withDefaults(
  defineProps<{
    to?: string | object;
    href?: string;
    activeUnderline?: boolean;
  }>(),
  {
    activeUnderline: false,
    to: undefined,
    href: undefined,
  }
);
const classes = "color-primary font-bold hover:(underline)";
</script>

<template>
  <router-link v-if="to" :to="to" :class="classes" v-bind="$attrs" :active-class="props.activeUnderline ? 'underline' : ''">
    <slot></slot>
  </router-link>
  <a v-else :href="href" :class="classes" v-bind="$attrs">
    <slot></slot>
  </a>
</template>
