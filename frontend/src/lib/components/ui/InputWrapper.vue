<script lang="ts" setup>
import ErrorTooltip from "~/lib/components/design/ErrorTooltip.vue";
import Spinner from "~/lib/components/design/Spinner.vue";

const props = defineProps<{
  errors?: string[];
  hasError: boolean;
  disabled?: boolean;
  label?: string;
  counter?: boolean;
  maxlength?: number;
  loading?: boolean;
  value: unknown;
}>();
</script>

<template>
  <ErrorTooltip :error-messages="disabled ? null : errors" class="w-full" :class="{ filled: value, error: hasError && !disabled }">
    <label
      :class="[
        'relative flex w-full outline-none p-2 border-1px rounded',
        'border-gray-500',
        'focus:border-primary-400',
        'error:border-red-400',
        'transition duration-200 ease',
      ]"
    >
      <slot class="outline-none flex-grow bg-transparent w-full"></slot>
      <span class="flex pl-2">
        <span v-if="loading" class="w-[24px] h-[24px]"><Spinner class="stroke-gray-400" /></span>
        <span v-if="counter && maxlength" class="inline-flex items-center ml-2">{{ value?.length || 0 }}/{{ maxlength }}</span>
        <span v-else-if="counter">{{ value?.length || 0 }}</span>
      </span>
      <span
        v-if="label"
        :class="[
          'absolute origin-top-left left-2 italic pointer-events-none',
          'input-focused:(transform scale-62 opacity-100 not-italic) filled:(transform scale-62 text-black-50 not-italic)',
          'opacity-80 error:(!text-red-400) input-focused:(text-primary-300)',
          'top-10px input-focused:(top-0) filled:(top-0)',
          'transition duration-250 ease',
        ]"
      >
        {{ label }}
      </span>
    </label>
  </ErrorTooltip>
</template>
