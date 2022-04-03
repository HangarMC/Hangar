<script lang="ts" setup>
import { computed } from "vue";
import { FloatingLabel, inputClasses } from "~/composables/useInputHelper";

const emit = defineEmits<{
  (e: "update:modelValue", value: string): void;
}>();
const value = computed({
  get: () => props.modelValue,
  set: (v) => emit("update:modelValue", v),
});
const props = defineProps<{
  modelValue: string;
  label?: string;
  errorMessages?: string[];
  counter?: boolean;
  maxlength?: number;
}>();
// TODO proper validation
const error = computed<boolean>(() => {
  return props.errorMessages ? props.errorMessages.length > 0 : false;
});
</script>

<template>
  <label class="relative flex" :class="{ filled: modelValue, error: error }">
    <textarea v-model="value" :class="inputClasses" v-bind="$attrs" :maxlength="maxlength" />
    <floating-label :label="label" />
    <span v-if="counter && maxlength">{{ value?.length || 0 }}/{{ maxlength }}</span>
    <span v-else-if="counter">{{ value?.length || 0 }}</span>
  </label>
  <template v-if="errorMessages && errorMessages.length > 0">
    <span v-for="msg in errorMessages" :key="msg" class="text-red-500">{{ msg }}</span>
  </template>
</template>
