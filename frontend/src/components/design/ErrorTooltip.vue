<script lang="ts" setup>
import { Tooltip } from "floating-vue";
import type { ErrorObject } from "@vuelidate/core";

const props = defineProps<{
  errorMessages?: (string | ErrorObject)[];
}>();

const formattedError = computed<string | Ref<string>>(() => {
  if (!props.errorMessages || props.errorMessages.length === 0) {
    return "";
  }
  return isErrorObject(props.errorMessages[0]) ? props.errorMessages[0].$message : props.errorMessages[0];
});

const hasError = computed<boolean>(() => {
  return props.errorMessages ? props.errorMessages.length > 0 : false;
});
</script>

<template>
  <!-- hardcoding the id is meh, but else hydration breaks and it doesn't actually seem to be used for accessibility? -->
  <Tooltip
    v-bind="$attrs"
    :shown="hasError"
    theme="error-tooltip"
    :triggers="[]"
    :delay="0"
    placement="bottom"
    class="text-center reset-popper"
    aria-id="tooltip"
    :container="false"
  >
    <slot />
    <template #popper>
      {{ formattedError || "error" }}
    </template>
  </Tooltip>
</template>

<style>
.v-popper--theme-error-tooltip .v-popper__inner {
  max-width: 700px;
  background-color: #d62e22;
  padding: 0.5rem;
  border-radius: 0.375rem;
  color: #fff;
}

.v-popper--theme-error-tooltip .v-popper__arrow-outer {
  border-color: #d62e22;
}
</style>
