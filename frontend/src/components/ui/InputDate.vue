<script lang="ts" setup>
import type { ValidationRule } from "@vuelidate/core";

const emit = defineEmits<{
  (e: "update:modelValue", date: string): void;
}>();
const date = computed({
  get: () => {
    if (props.time && props.modelValue) {
      // we need to cut off seconds and millis from iso format...
      const split = props.modelValue.split(":");
      return split[0] + ":" + split[1];
    }
    return props.modelValue;
  },
  set: (value) => {
    let valueToEmit = value;
    if (props.time && value) {
      // add seconds and millies back...
      valueToEmit += ":00.000Z";
    }
    emit("update:modelValue", valueToEmit);
  },
});
const props = defineProps<{
  modelValue: string;
  label?: string;
  disabled?: boolean;
  loading?: boolean;
  messages?: string[];
  errorMessages?: string[];
  rules?: ValidationRule<string | undefined>[];
  noErrorTooltip?: boolean;
  time?: boolean;
}>();

const errorMessages = computed(() => props.errorMessages);
const { v, errors, hasError } = useValidation(props.label, props.rules, date, errorMessages);
</script>

<template>
  <InputWrapper
    :errors="errors"
    :messages="messages"
    :has-error="hasError"
    :loading="loading || v.$pending"
    :label="label"
    :value="date"
    :disabled="disabled"
    :no-error-tooltip="noErrorTooltip"
  >
    <template #default="slotProps">
      <!-- todo make fancy -->
      <input v-model="date" :type="time ? 'datetime-local' : 'date'" v-bind="$attrs" :disabled="disabled" :class="slotProps.class" />
    </template>
    <!-- @vue-ignore -->
    <template v-for="(_, name) in $slots" #[name]="slotData">
      <slot :name="name" v-bind="slotData || {}" />
    </template>
  </InputWrapper>
</template>
