<script setup lang="ts">
import { computed } from "vue";
import { useValidation } from "~/composables/useValidationHelpers";
import { ValidationRule } from "@vuelidate/core";

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
  errorMessages?: string[];
  rules?: ValidationRule<string | undefined>[];
}>();

const { v, errors, hasError } = useValidation(props.label, props.rules, internalVal, props.errorMessages);
</script>

<template>
  <label class="group relative cursor-pointer pl-30px customCheckboxContainer w-max">
    <template v-if="props.label">{{ props.label }}</template>
    <input v-model="internalVal" type="radio" class="hidden" v-bind="$attrs" @blur="v.$touch()" />
    <span
      class="absolute top-5px left-0 h-15px w-15px rounded-full bg-gray-300"
      after="absolute hidden content-DEFAULT top-1px left-5px border-solid w-6px h-12px border-r-3px border-b-3px border-white rounded-full"
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
