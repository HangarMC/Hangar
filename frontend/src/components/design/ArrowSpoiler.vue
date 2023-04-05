<script setup lang="ts">
import { ref } from "#imports";

const props = defineProps<{
  open: boolean;
}>();
const open = ref(props.open);
</script>

<!-- TODO: Most of this is horrible -->
<template>
  <details :open="open">
    <summary class="inline-flex items-center">
      <slot name="title" />
    </summary>
    <slot name="content" />
  </details>
</template>

<style lang="scss" scoped>
summary {
  position: relative;
}

summary::marker {
  color: transparent;
}

summary::-webkit-details-marker {
  display: none;
}

summary::after {
  content: "▶";
  position: absolute;
  margin: auto;
  right: 0;
  transition: all 0.5s;
}

details[open] summary::after {
  transform: rotate(90deg);
}

details {
  height: auto;
  max-height: 1.5rem;
}

details[open] {
  max-height: 99rem;
  transition: all 2s ease;
}
</style>
