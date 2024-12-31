<script lang="ts" setup>
import Popper from "vue3-popper";

onErrorCaptured((err) => {
  if (err.stack?.includes("popper")) {
    popperLog("Captured popper error", err);
    return false;
  }
});
</script>

<template>
  <Popper v-bind="$attrs" data-allow-mismatch="style">
    <slot />
    <!-- todo replace popper with floating-vue -->
    <!-- @vue-ignore -->
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
