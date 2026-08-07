<script lang="ts" setup>
const props = withDefaults(
  defineProps<{
    diameter?: number;
    stroke?: number;
    unit?: string;
  }>(),
  {
    diameter: 24,
    stroke: 2,
    unit: "px",
  }
);

const size = computed(() => props.diameter + props.unit);
// the viewBox is a fixed 24 units, so scale the requested stroke into that space
const strokeWidth = computed(() => (props.stroke * 24) / props.diameter);
</script>

<template>
  <svg class="hangar-spinner" :style="{ width: size, height: size }" viewBox="0 0 24 24" fill="none" stroke="currentColor" aria-hidden="true" focusable="false">
    <circle cx="12" cy="12" r="10" :stroke-width="strokeWidth" stroke-linecap="round" stroke-dasharray="15.71 62.83" />
  </svg>
</template>

<style scoped>
.hangar-spinner {
  display: inline-block;
  animation: hangar-spin 0.7s linear infinite;
}

@keyframes hangar-spin {
  to {
    transform: rotate(360deg);
  }
}

@media (prefers-reduced-motion: reduce) {
  .hangar-spinner {
    animation-duration: 1.6s;
  }
}
</style>
