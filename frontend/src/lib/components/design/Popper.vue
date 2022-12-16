<script lang="ts" setup>
import Popper from "vue3-popper";
</script>

<template>
  <Popper v-bind="$attrs">
    <slot />
    <template #content="{ close }">
      <slot name="content" :close="close" />
    </template>
  </Popper>
</template>

<style scoped>
:deep(.popper) {
  padding: unset !important;
  border-radius: unset !important;
  border-width: unset !important;
  box-shadow: unset !important;
  background: unset !important;
  transition: unset !important;
  color: unset !important;
}
</style>
