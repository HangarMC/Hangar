<script lang="ts" setup>
import { hasSlotContent } from "~/composables/useSlot";

const props = defineProps<{
  class?: string;
}>();
</script>

<template>
  <div :class="'bg-white border-top-primary shadow-soft rounded-md p-4 ' + (props.class || '')" dark="bg-background-dark-90">
    <div v-if="hasSlotContent($slots.header)" class="text-xl font-bold mb-2">
      <slot name="header"></slot>
    </div>
    <slot name="default"></slot>
    <div v-if="hasSlotContent($slots.footer)" class="mt-2">
      <slot name="footer"></slot>
    </div>
  </div>
</template>
