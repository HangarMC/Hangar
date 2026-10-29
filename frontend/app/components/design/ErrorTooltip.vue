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

/* child-scoped: a descendant selector also paints a nested container="false" popper red */
.v-popper--theme-error-tooltip > .v-popper__wrapper > .v-popper__inner {
  max-width: 24rem;
  padding: 0.4375rem 0.6875rem;
  border: 1px solid #fca5a5;
  border-radius: 0.375rem;
  background-color: #fef2f2;
  box-shadow:
    0 8px 20px -6px rgb(0 0 0 / 0.25),
    0 2px 6px -2px rgb(0 0 0 / 0.15);
  color: #b91c1c;
  font-size: 0.875rem;
  font-weight: 500;
  line-height: 1.4;
  text-align: left;
  text-wrap: pretty;
}

.dark .v-popper--theme-error-tooltip > .v-popper__wrapper > .v-popper__inner {
  border-color: #7f1d1d;
  background-color: #2a1416;
  box-shadow:
    0 8px 20px -6px rgb(0 0 0 / 0.5),
    0 2px 6px -2px rgb(0 0 0 / 0.35);
  color: #fca5a5;
}

.v-popper--theme-error-tooltip > .v-popper__wrapper > .v-popper__arrow-container .v-popper__arrow-outer {
  border-color: #fca5a5;
}

.v-popper--theme-error-tooltip > .v-popper__wrapper > .v-popper__arrow-container .v-popper__arrow-inner {
  border-color: #fef2f2;
}

.dark .v-popper--theme-error-tooltip > .v-popper__wrapper > .v-popper__arrow-container .v-popper__arrow-outer {
  border-color: #7f1d1d;
}

.dark .v-popper--theme-error-tooltip > .v-popper__wrapper > .v-popper__arrow-container .v-popper__arrow-inner {
  border-color: #2a1416;
}
</style>
