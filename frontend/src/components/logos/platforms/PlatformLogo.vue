<script lang="ts" setup>
import { Platform } from "~/types/enums";
import PaperLogo from "~/components/logos/platforms/PaperLogo.vue";
import VelocityLogo from "~/components/logos/platforms/VelocityLogo.vue";
import WaterfallLogo from "~/components/logos/platforms/WaterfallLogo.vue";

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
