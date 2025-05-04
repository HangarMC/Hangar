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
  <label class="w-full flex items-center select-none" :cursor="disabled ? 'auto' : 'pointer'">
    <input
      v-model="internalVal"
      v-bind="$attrs"
      type="checkbox"
      class="appearance-none peer"
      :cursor="disabled ? 'auto' : 'pointer'"
      :disabled="disabled"
      :value="value"
      @blur="v.$touch()"
    />
    <span
      class="flex items-center rounded-full border w-full border-transparent py-1.5 transition-all duration-250
             peer-checked:bg-blue-600 peer-hover:bg-gray-700 peer-hover:scale-[1.015]"
    >
      <slot />
      <slot name="label">
        <span v-if="props.label" class="ml-1">{{ props.label }}</span>
      </slot>
    </span>
  </label>
</template>
