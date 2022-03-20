<script setup lang="ts">
const props = defineProps<{
  class?: string;
}>();
</script>

<template>
  <div :class="'max-w-1200px m-auto p-4 ' + (props.class || '')">
    <slot></slot>
  </div>
</template>
