<script lang="ts" setup>
import type { ErrorObject } from "@vuelidate/core";
import { ErrorTooltip } from "#components";

const props = defineProps<{
  errors?: (string | ErrorObject)[];
  messages?: string[];
  hasError: boolean;
  disabled?: boolean;
  label?: string;
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
  <component
    :is="noErrorTooltip ? 'div' : ErrorTooltip"
    :error-messages="(disabled && !readonly) || noErrorTooltip ? null : errors"
    class="w-full"
    :class="{ filled: value, error: hasError && !disabled }"
  >
    <label
      :class="[
        'relative flex w-full outline-none py-2 rounded-full',
        'focus-within:border-primary-500 focus-within:hover:border-primary-500',
        'dark:bg-gray-800',
        'error:border-red-400',
        'transition duration-200 ease',
      ]"
    >
      <slot class="outline-none flex-grow bg-transparent w-full py-0.5" />
      <span class="flex pl-2 self-center">
        <span v-if="counter && maxlength" class="inline-flex items-center ml-2">{{ value?.length || 0 }}/{{ maxlength }}</span>
        <span v-else-if="counter">{{ value?.length || 0 }}</span>
        <slot name="append" />
      </span>
      <span
        v-if="label"
        :class="[
          'absolute origin-top-left left-4 pointer-events-none',
          'input-hover:(opacity-100)',
          'input-focused:(transform scale-60 opacity-100 not-italic) filled:(transform scale-60 text-black-50 not-italic)',
          'opacity-60 error:(!text-red-400) input-focused:(text-primary-400)',
          'py-0.5 input-focused:(top-0) filled:(top-0)',
          'transition duration-250 ease',
        ]"
      >
        {{ label }}
      </span>
    </label>
    <span v-if="messages" class="text-small">
      <span v-for="message in messages" :key="message"> {{ message }}<br /> </span>
    </span>
    <span v-if="errors && noErrorTooltip" class="text-small text-red-400">
      <span v-for="message in errors" :key="getErrorMessage(message)"> {{ getErrorMessage(message) }}<br /> </span>
    </span>
  </component>
</template>

<style>
/* we have our own one */
input[type="password" i]::-ms-reveal {
  display: none;
}
</style>
