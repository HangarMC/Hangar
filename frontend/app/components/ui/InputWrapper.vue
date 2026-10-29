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
  /**
  Keeps the label raised even while empty, for inputs that render their own placeholder (`date` shows
  `dd.mm.yyyy`) which the resting label would otherwise sit on top of.
  */
  alwaysFilled?: boolean;
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
    :class="{ filled: alwaysFilled || value, error: hasError && !disabled }"
  >
    <label
      :class="[
        // 7px keeps single-line fields at h-11 like the buttons beside them, without capping textareas
        'input-field relative flex w-full outline-none px-2 py-[7px] border-1px rounded-md',
        'border-gray-300 dark:border-gray-600',
        'hover:border-gray-400 dark:hover:border-gray-500',
        'error:border-red-400',
        'transition duration-200 ease',
      ]"
    >
      <slot class="outline-none flex-grow bg-transparent w-full py-0.5" />
      <span class="flex pl-2 self-center">
        <span v-if="loading" class="w-[24px] h-[24px]"><Spinner class="stroke-gray-400" /></span>
        <span v-if="counter && maxlength" class="ml-2 inline-flex items-center text-gray-500 dark:text-gray-400">
          {{ value?.length || 0 }}/{{ maxlength }}
        </span>
        <span v-else-if="counter" class="text-gray-500 dark:text-gray-400">{{ value?.length || 0 }}</span>
        <slot name="append" />
      </span>
      <span
        v-if="label"
        :class="[
          'absolute origin-top-left left-2 pointer-events-none',
          'input-hover:(opacity-100)',
          'input-focused:(transform scale-60 opacity-100) filled:(transform scale-60 text-gray-secondary)',
          'opacity-70 error:(!text-red-400) input-focused:(text-primary-400)',
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

.input-field {
  background-color: var(--input-surface);
}

/* doubled class so focus wins over the hover border */
.input-field.input-field:focus-within {
  border-color: var(--primary-500);
  box-shadow: 0 0 0 2px color-mix(in srgb, var(--primary-500) 12%, transparent);
}

.dark .input-field.input-field:focus-within {
  border-color: var(--primary-300);
  box-shadow: 0 0 0 2px color-mix(in srgb, var(--primary-300) 12%, transparent);
}

.error .input-field.input-field:focus-within {
  border-color: #f87171;
  box-shadow: 0 0 0 2px rgb(248 113 113 / 0.12);
}
</style>
