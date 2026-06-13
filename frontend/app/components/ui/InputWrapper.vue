<script lang="ts" setup>
import type { ErrorObject } from "@vuelidate/core";

const props = defineProps<{
  errors?: (string | ErrorObject)[];
  messages?: string[];
  hasError: boolean;
  disabled?: boolean;
  counter?: boolean;
  maxlength?: number;
  loading?: boolean;
  value: any;
  noErrorTooltip?: boolean;
  readonly?: boolean;
}>();

function getErrorMessage(message: NonNullable<typeof props.errors>[number]) {
  return isErrorObject(message) ? unref(message.$message) : message;
}
</script>

<template>
  <div class="w-full" :class="{ filled: value, error: hasError && !disabled }">
    <label
      :class="[
        'relative flex w-full rounded-lg border border-transparent bg-gray-100 py-2 outline-none',
        'hover:border-gray-300 focus-within:border-gray-400 dark:hover:border-gray-700 dark:focus-within:border-gray-600',
        'dark:bg-gray-800',
        'error:border-red-400',
        'transition-colors duration-200 ease',
      ]"
    >
      <slot class="outline-none flex-grow bg-transparent w-full py-0.5" />
      <span class="flex pl-2 self-center">
        <span v-if="counter && maxlength" class="inline-flex items-center ml-2">{{ value?.length || 0 }}/{{ maxlength }}</span>
        <span v-else-if="counter">{{ value?.length || 0 }}</span>
        <slot name="append" />
      </span>
    </label>
    <span v-if="messages" class="text-small">
      <span v-for="message in messages" :key="message"> {{ message }}<br /> </span>
    </span>
    <span v-if="errors && !disabled" class="mt-1 block text-sm text-red-400">
      <span v-for="message in errors" :key="getErrorMessage(message)"> {{ getErrorMessage(message) }}<br /> </span>
    </span>
  </div>
</template>

<style>
/* we have our own one */
input[type="password" i]::-ms-reveal {
  display: none;
}

input,
select,
textarea {
  border: 0;
  box-shadow: none;
  outline: 0;
}

input:focus,
input:focus-visible,
select:focus,
select:focus-visible,
textarea:focus,
textarea:focus-visible {
  border: 0;
  box-shadow: none;
  outline: 0;
}
</style>
