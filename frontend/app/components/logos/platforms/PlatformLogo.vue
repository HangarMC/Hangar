<script lang="ts" setup>
import { Platform } from "#shared/types/backend";

withDefaults(
  defineProps<{
    platform: Platform;
    size?: number;
  }>(),
  {
    size: 72,
  }
);
</script>
<template>
  <PaperLogo v-if="platform === Platform.PAPER" v-bind="$attrs" :width="size" :height="size" />
  <VelocityLogo v-else-if="platform === Platform.VELOCITY" v-bind="$attrs" :width="size" :height="size" />
  <WaterfallLogo v-else-if="platform === Platform.WATERFALL" v-bind="$attrs" :width="size" :height="size" />
</template>
