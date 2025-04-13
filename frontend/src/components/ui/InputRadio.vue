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
  <label class="customRadioButton relative cursor-pointer flex items-center select-none w-full bg-[linear-gradient(270deg,_transparent_-10%,_#004EE9_350%)] rounded-lg py-1.5 border border-gray-800">
    <input
      v-model="internalVal"
      type="radio"
      class="peer appearance-none"
      v-bind="$attrs"
      @blur="v.$touch()"
    />

    <slot />
    <template v-if="props.label">{{ props.label }}</template>
  </label>
</template>

<style>
.customRadioButton input:checked ~ svg {
  @apply opacity-100;
}
</style>
