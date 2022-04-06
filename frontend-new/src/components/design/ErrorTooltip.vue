<script lang="ts" setup>
import Popper from "vue3-popper";
import { computed } from "vue";

const props = defineProps<{
  errorMessages?: string[];
}>();

const formattedError = computed<string>(() => {
  if (!props.errorMessages || props.errorMessages.length === 0) {
    return "";
  }
  return props.errorMessages[0];
});

const hasError = computed<boolean>(() => {
  return props.errorMessages ? props.errorMessages.length > 0 : false;
});
</script>

<template>
  <ClientOnly>
    <Popper v-bind="$attrs" :show="hasError" arrow placement="bottom" :content="formattedError" class="text-center">
      <slot />
    </Popper>
  </ClientOnly>
</template>

<style scoped>
:deep(.popper) {
  background: #d62e22;
  padding: 0.4rem;
  border-radius: 0.375rem;
  color: #fff;
}
:deep(.popper #arrow::before),
:deep(.popper:hover),
:deep(.popper:hover > #arrow::before) {
  background: #d62e22;
}
</style>
