<script lang="ts" setup>
import Popper from "vue3-popper";
import { onErrorCaptured } from "#imports";
import { popperLog } from "~/composables/useLog";

withDefaults(
  defineProps<{
    hover?: boolean;
    show?: boolean;
  }>(),
  {
    hover: true,
    show: undefined,
  }
);

onErrorCaptured((err) => {
  if (err.stack?.includes("popper")) {
    popperLog("Captured popper error", err);
    return false;
  }
});
</script>

<template>
  <Popper :hover="hover" open-delay="200" close-delay="100" :show="show">
    <slot />
    <template #content>
      <slot name="content" />
    </template>
  </Popper>
</template>

<style>
.popper {
  max-width: 700px;
  --popper-theme-background-color: #464646;
  --popper-theme-background-color-hover: #464646;
  --popper-theme-padding: 0.5rem;
  --popper-theme-border-radius: 0.375rem;
  --popper-theme-text-color: #fff;
}
</style>
