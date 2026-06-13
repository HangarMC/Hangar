<script lang="ts" setup>
import { Tooltip } from "floating-vue";
import type { ErrorObject } from "@vuelidate/core";

const props = defineProps<{
  errorMessages?: (string | ErrorObject)[];
}>();

const formattedError = computed<string | Ref<string> | undefined>(() => {
  if (!props.errorMessages || props.errorMessages.length === 0) {
    // eslint-disable-next-line unicorn/no-useless-undefined
    return undefined;
  }
  return isErrorObject(props.errorMessages[0]) ? props.errorMessages[0].$message : props.errorMessages[0];
});
</script>

<template>
  <!-- hardcoding the id is meh, but else hydration breaks and it doesn't actually seem to be used for accessibility? -->
  <Tooltip
    v-bind="$attrs"
    :shown="Boolean(formattedError)"
    theme="error-tooltip"
    :triggers="[]"
    :delay="0"
    placement="bottom"
    class="text-center reset-popper"
    aria-id="tooltip"
    :container="false"
    handle-resize
  >
    <slot />
    <template #popper>
      {{ formattedError }}
    </template>
  </Tooltip>
</template>

<style>
.v-popper--theme-error-tooltip {
  transition-duration: 0s !important;
}

.v-popper--theme-error-tooltip .v-popper__inner {
  max-width: 700px;
  @apply max-w-2xl rounded-lg border border-red-600/70 bg-charcoal-500 px-4 py-2 text-center text-red-100 shadow-lg shadow-black/30;
  background-image: linear-gradient(rgb(127 29 29 / 25%), rgb(127 29 29 / 25%));
}

.v-popper--theme-error-tooltip .v-popper__arrow-outer {
  @apply border-red-600/70;
}

.v-popper--theme-error-tooltip .v-popper__arrow-inner {
  border-color: color-mix(in srgb, #7f1d1d 25%, var(--charcoal-500));
}
</style>
