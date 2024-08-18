<script lang="ts" setup>
const props = withDefaults(
  defineProps<{
    animated?: boolean;
    delay?: boolean;
  }>(),
  {
    animated: true,
    delay: false,
  }
);

const show = ref(!props.delay);
if (import.meta.client && props.delay) {
  onMounted(() => {
    setTimeout(() => {
      show.value = true;
    }, 400);
  });
}
</script>

<template>
  <div v-show="show" class="skeleton rounded-md shadow-md" :class="animated && 'skeleton--animated'">
    <div class="skeleton__content">Loading...</div>
  </div>
</template>

<style lang="scss" scoped>
.skeleton {
  background-attachment: fixed;
  background-image: linear-gradient(90deg, var(--skeleton-color), var(--skeleton-color) 50%, var(--skeleton-color-shimmer) 75%, var(--skeleton-color));
  background-size: 150vw;

  --skeleton-color: #f4f3f1;
  --skeleton-color-shimmer: #e4e4e4;

  .dark & {
    --skeleton-color: #2d2d2d;
    --skeleton-color-shimmer: #3d3d3d;
  }

  &--animated {
    animation: skeleton 1s infinite;
  }

  &__content {
    opacity: 0;
    user-select: none;
  }
}

@keyframes skeleton {
  0% {
    background-position-x: 0;
  }
  100% {
    background-position-x: 150vw;
  }
}
</style>
