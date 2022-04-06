<script lang="ts" setup>
import { computed } from "vue";
import { FloatingLabel, inputClasses } from "~/composables/useInputHelper";
import ErrorTooltip from "~/components/design/ErrorTooltip.vue";

const emit = defineEmits<{
  (e: "update:modelValue", value?: string): void;
}>();
const value = computed({
  get: () => props.modelValue,
  set: (v) => emit("update:modelValue", v),
});
const props = defineProps<{
  modelValue?: string;
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
  <ErrorTooltip :error-messages="errorMessages" class="w-full">
    <label class="relative flex" :class="{ filled: modelValue, error: error }">
      <input v-model="value" type="text" :class="inputClasses" v-bind="$attrs" :maxlength="maxlength" />
      <span v-if="counter && maxlength" class="inline-flex items-center ml-2">{{ value?.length || 0 }}/{{ maxlength }}</span>
      <span v-else-if="counter">{{ value?.length || 0 }}</span>
      <floating-label :label="label" /> </label
  ></ErrorTooltip>
</template>
