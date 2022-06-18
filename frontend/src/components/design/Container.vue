<script setup lang="ts">
const props = defineProps<{
  class?: string;
}>();
</script>

<template>
  <div :class="'max-w-screen-xl mx-auto p-4 ' + (props.class || '')">
    <slot></slot>
  </div>
</template>
