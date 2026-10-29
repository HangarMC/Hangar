<script lang="ts" setup>
import { Tooltip } from "floating-vue";

const root = useTemplateRef<{ $el?: HTMLElement }>("root");

// floating-vue marks the popper aria-hidden while an activated item still holds focus, so hand it back to the trigger
function restoreFocus() {
  const active = document.activeElement as HTMLElement | null;
  if (!active?.closest(".v-popper__popper")) return;

  active.blur();
  const trigger = root.value?.$el?.querySelector<HTMLElement>('button, [href], [tabindex]:not([tabindex="-1"])');
  trigger?.focus({ preventScroll: true });
}
</script>

<template>
  <!-- hardcoding the id is meh, but else hydration breaks and it doesn't actually seem to be used for accessibility? -->
  <Tooltip
    ref="root"
    v-bind="$attrs"
    theme="none"
    :triggers="['click']"
    :delay="0"
    auto-hide
    auto-boundary-max-size
    aria-id="tooltip"
    class="w-fit"
    @hide="restoreFocus"
  >
    <template #default="{ shown }">
      <slot :shown="shown" />
    </template>
    <template #popper="{ hide }">
      <slot name="content" :close="hide" />
    </template>
  </Tooltip>
</template>

<style>
.v-popper--theme-none .v-popper__inner,
.v-popper--theme-none .v-popper__inner > * {
  display: flex;
  flex-direction: column;
  min-height: 0;
  overflow: hidden;
}
.v-popper--theme-none .v-popper__arrow-container {
  display: none;
}
</style>
