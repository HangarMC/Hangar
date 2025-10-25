<script setup lang="ts" generic="T">
import { computed, useAttrs } from "vue";

const emit = defineEmits<{
  (e: "update:modelValue", value?: T): void;
}>();
const internalVal = computed({
  get: () => props.modelValue,
  set: (val) => emit("update:modelValue", val),
});
const props = defineProps<{
  modelValue?: T;
  label?: string;
  disabled?: boolean;
  value?: string;
}>();

const isChecked = computed(() => {
  const val = internalVal.value as any;
  if (Array.isArray(val)) {
    return props.value !== undefined && val.includes(props.value);
  }
  if (props.value !== undefined) {
    return String(val) === String(props.value);
  }
  return Boolean(val);
});

// uses InputGroup for validation
const { v } = useValidation(props.label, undefined, internalVal);
</script>

<template>
  <label class="w-full flex items-center select-none relative" :class="{
           'cursor-pointer': !disabled,
         }">
    <input
      v-model="internalVal"
      v-bind="$attrs"
      type="checkbox"
      class="appearance-none peer"
      :disabled="disabled"
      :value="value"
      @blur="v.$touch()"
    />

    <IconMdiCheck class="absolute hidden peer-checked:block top-2.25 right-3.5 z-10" />
    <span
      :style="isChecked ? {
        backgroundColor: 'color-mix(in srgb, var(--primary-500) 25%, transparent)',
        borderColor: 'var(--primary-500)'
      } : {}"
      class="flex items-center rounded-xl border w-full border-transparent py-1.5 transition-all duration-250
              peer-hover:bg-gray-800 peer-hover:border-gray-700 peer-hover:scale-[1.015] z-0"
    >
      <slot />
      <slot name="label">
        <span v-if="props.label" class="ml-4">{{ props.label }}</span>
      </slot>
    </span>
  </label>
</template>
