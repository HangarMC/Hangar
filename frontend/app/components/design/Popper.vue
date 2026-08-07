<script lang="ts" setup>
import { Tooltip } from "floating-vue";
</script>

<template>
  <!-- hardcoding the id is meh, but else hydration breaks and it doesn't actually seem to be used for accessibility? -->
  <Tooltip v-bind="$attrs" theme="none" :triggers="['click']" :delay="0" auto-hide auto-boundary-max-size aria-id="tooltip" class="w-fit">
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
