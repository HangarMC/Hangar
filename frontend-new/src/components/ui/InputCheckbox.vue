<script setup lang="ts">
import { computed } from "vue";

const emit = defineEmits<{
  (e: "update:modelValue", value: boolean | boolean[]): void;
}>();
const internalVal = computed({
  get: () => props.modelValue,
  set: (v) => emit("update:modelValue", v),
});
const props = defineProps<{
  modelValue: boolean | boolean[];
  label?: string;
  disabled?: boolean;
}>();
</script>

<template>
  <label class="group relative pl-30px customCheckboxContainer w-max" :cursor="disabled ? 'auto' : 'pointer'">
    <template v-if="props.label">{{ props.label }}</template>
    <input v-model="internalVal" type="checkbox" class="hidden" v-bind="$attrs" :disabled="disabled" />
    <span
      class="absolute top-5px left-0 h-15px w-15px rounded bg-gray-300"
      after="absolute hidden content-DEFAULT top-1px left-5px border-solid w-6px h-12px border-r-3px border-b-3px border-white"
      group-hover="bg-gray-400"
    />
  </label>
</template>

<style>
/*This is needed, because you cannot have more than one parent group in tailwind/windi*/
.customCheckboxContainer input:checked ~ span {
  background-color: #004ee9 !important;
}

/*The tailwind/windi utility class rotate-45 is BROKEN*/
.customCheckboxContainer input:checked ~ span:after {
  display: block;
  transform: rotate(45deg);
}
</style>
