<script setup lang="ts">
import { computed, useAttrs } from 'vue';

const emit = defineEmits<{
  (e: "update:modelValue", value?: string): void;
}>();

const props = defineProps<{
  modelValue?: string;
  label?: string;
}>();

const internalVal = computed({
  get: () => props.modelValue,
  set: (val) => emit("update:modelValue", val),
});

const attrs = useAttrs() as Record<string, any>;

const isChecked = computed(
  () => String(internalVal.value) === String(attrs.value)
);

// uses InputGroup for validation
const { v } = useValidation(props.label, undefined, internalVal);
</script>

<template>
  <label class="relative cursor-pointer flex items-center w-full">
    <input
      v-model="internalVal"
      type="radio"
      class="peer appearance-none"
      v-bind="$attrs"
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
      <span v-if="props.label" class="ml-1">{{ props.label }}</span>
    </span>
  </label>
</template>
