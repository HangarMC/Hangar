<script lang="ts" setup>
const props = defineProps<{
  to?: string | object;
  href?: string;
}>();
const classes = "color-primary font-bold hover:(underline)";
</script>

<template>
  <router-link v-if="to" :to="to" :class="classes" v-bind="$attrs" active-class="underline">
    <slot></slot>
  </router-link>
  <a v-else :href="href" :class="classes" v-bind="$attrs">
    <slot></slot>
  </a>
</template>
