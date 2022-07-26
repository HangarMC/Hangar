<script lang="ts" setup>
import Popper from "vue3-popper";
import { ref } from "vue";

const popper = ref();
function close() {
  popper.value.close();
}

defineExpose({ close });
</script>

<template>
  <Popper v-bind="$attrs" ref="popper">
    <slot />
    <template #content="{ close }">
      <slot name="content" :close="close" />
    </template>
  </Popper>
</template>

<style scoped>
:deep(.popper) {
  padding: unset;
  border-radius: unset;
  border-width: unset;
  box-shadow: unset;
  background: unset;
  transition: unset;
  color: unset;
}
</style>
