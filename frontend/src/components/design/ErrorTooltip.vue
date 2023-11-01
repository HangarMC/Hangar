<script lang="ts" setup>
import Popper from "vue3-popper";
import type { ErrorObject } from "@vuelidate/core";
import { computed, type Ref } from "vue";
import { isErrorObject } from "~/composables/useValidationHelpers";
import { onErrorCaptured } from "#imports";
import { popperLog } from "~/composables/useLog";

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

onErrorCaptured((err) => {
  if (err.stack?.includes("popper")) {
    popperLog("Captured popper error", err);
    return false;
  }
});
</script>

<template>
  <Popper v-bind="$attrs" :show="hasError" arrow placement="bottom" class="text-center reset-popper">
    <slot />
    <template #content>
      {{ formattedError || "error" }}
    </template>
  </Popper>
</template>

<style scoped>
:deep(.popper) {
  padding: 0.4rem;
  border-radius: 0.375rem;
  color: #fff;
}

:deep(.popper),
:deep(.popper #arrow::before),
:deep(.popper:hover),
:deep(.popper:hover > #arrow::before) {
  background: #d62e22 !important;
}

.reset-popper {
  border: unset !important;
  margin: unset !important;
}
</style>
