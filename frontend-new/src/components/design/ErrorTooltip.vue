<script lang="ts" setup>
import Popper from "vue3-popper";
import { ErrorObject } from "@vuelidate/core";
import { computed, Ref } from "vue";
import { isErrorObject } from "~/composables/useValidationHelpers";

const props = defineProps<{
  errorMessages?: string[] | ErrorObject[];
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
  <!-- todo this throws hydration errors -->
  <Popper v-bind="$attrs" :show="hasError" arrow placement="bottom" :content="formattedError" class="text-center">
    <slot />
  </Popper>
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
