<script setup lang="ts">
const emit = defineEmits<{
  (e: "update:modelValue", value?: string): void;
}>();
const internalVal = computed({
  get: () => props.modelValue,
  set: (val) => emit("update:modelValue", val),
});
const props = defineProps<{
  modelValue?: string;
  label?: string;
}>();

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
      class="flex items-center rounded-full border w-full border-transparent py-1.5 transition-all duration-250
             peer-checked:bg-primary-500 peer-hover:bg-gray-700 peer-hover:scale-[1.015] z-0"
    >
      <slot />
      <span v-if="props.label" class="ml-1">{{ props.label }}</span>
    </span>
  </label>
</template>
