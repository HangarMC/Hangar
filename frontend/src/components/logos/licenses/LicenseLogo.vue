<script lang="ts" setup>
withDefaults(
  defineProps<{
    license: string;
    size?: number;
  }>(),
  {
    size: 72,
  }
);
</script>
<template>
  <icon-mdi-help v-if="license === 'Unspecified'" v-bind="$attrs" :width="size" :height="size" />
  <MITLogo v-else-if="license === 'MIT'" v-bind="$attrs" :width="size" :height="size" />
  <icon-mdi-feather v-else-if="license === 'Apache 2.0'" v-bind="$attrs" :width="size" :height="size" />
  <GPLLogo v-else-if="license === 'GPL'" v-bind="$attrs" :width="size" :height="size" />
  <LGPLLogo v-else-if="license === 'LGPL'" v-bind="$attrs" :width="size" :height="size" />
  <AGPLLogo v-else-if="license === 'AGPL'" v-bind="$attrs" :width="size" :height="size" />
  <icon-mdi-gavel v-else-if="license === 'Other'" v-bind="$attrs" :width="size" :height="size" />
</template>
