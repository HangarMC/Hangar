<script setup lang="ts" generic="T">
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

// uses InputGroup for validation
const { v } = useValidation(props.label, undefined, internalVal);
</script>

<template>
  <label class="customCheckbox group relative flex items-center select-none" :cursor="disabled ? 'auto' : 'pointer'">
    <input
      v-model="internalVal"
      v-bind="$attrs"
      type="checkbox"
      class="appearance-none h-4 w-4 bg-gray-300 mr-2 rounded-sm shrink-0 group-hover:bg-gray-400 !checked:bg-primary-500"
      dark="bg-gray-600 group-hover:bg-gray-500"
      :cursor="disabled ? 'auto' : 'pointer'"
      :disabled="disabled"
      :value="value"
      @blur="v.$touch()"
    />
    <icon-mdi-check-bold class="absolute h-4 w-4 opacity-0 text-white" />
    <slot />
    <slot name="label">
      <template v-if="props.label">{{ props.label }}</template>
    </slot>
  </label>
</template>

<style>
.customCheckbox input:checked ~ svg {
  @apply opacity-100;
}
</style>
