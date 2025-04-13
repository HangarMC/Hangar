<script lang="ts" setup>
const props = withDefaults(
  defineProps<{
    to?: string | object;
    href?: string | null;
    activeUnderline?: boolean;
    disabled?: boolean;
    custom?: boolean;
  }>(),
  {
    activeUnderline: false,
    to: undefined,
    href: undefined,
    disabled: false,
    custom: false,
  }
);
const classes = computed<string>(() => (props.disabled ? "color-gray-400 cursor-not-allowed" : "color-primary hover:(underline)"));

defineSlots<{
  default(props: { classes?: string }): any;
}>();
</script>

<template>
  <slot v-if="custom" :classes />
  <NuxtLink v-else-if="to" :to="to" :class="classes" v-bind="$attrs" :active-class="props.activeUnderline ? 'underline' : ''">
    <slot />
  </NuxtLink>
  <a v-else-if="href" :href="sanitizeUrl(href)" :class="classes" v-bind="$attrs">
    <slot />
  </a>
  <span v-else :class="classes" v-bind="$attrs" cursor="pointer">
    <slot />
  </span>
</template>
