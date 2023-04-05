<script lang="ts" setup>
defineProps<{
  show: boolean;
}>();
</script>

<template>
  <div>
    <transition appear>
      <div v-if="show">
        <slot v-if="show" />
      </div>
    </transition>
  </div>
</template>

<style lang="scss">
.v-enter-active,
.v-leave-active {
  transition: opacity 0.3s ease;
}

.v-enter-from,
.v-leave-to {
  opacity: 0;
}
</style>
