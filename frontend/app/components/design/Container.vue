<script setup lang="ts">
const props = defineProps<{
  class?: string;
}>();
</script>

<template>
  <div :class="'max-w-screen-2xl mx-auto p-4 ' + (props.class || '')">
    <slot />
  </div>
</template>
