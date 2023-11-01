<script lang="ts" setup>
import { computed, ref } from "vue";
import type { ValidationRule } from "@vuelidate/core";
import { useValidation } from "~/composables/useValidationHelpers";
import InputWrapper from "~/components/ui/InputWrapper.vue";

const emit = defineEmits<{
  (e: "update:modelValue", value?: string | null): void;
}>();
const value = computed({
  get: () => props.modelValue,
  set: (val) => emit("update:modelValue", val),
});
const props = defineProps<{
  modelValue?: string | null;
  label?: string;
  counter?: boolean;
  maxlength?: number;
  loading?: boolean;
  messages?: string[];
  errorMessages?: string[];
  disabled?: boolean;
  rules?: ValidationRule<string | undefined>[];
  noErrorTooltip?: boolean;
}>();

const errorMessages = computed(() => props.errorMessages);
const { v, errors, hasError } = useValidation(props.label, props.rules, value, errorMessages);

const show = ref(false);

defineExpose({ validation: v });
</script>

<template>
  <InputWrapper
    :errors="errors"
    :messages="messages"
    :has-error="hasError"
    :counter="counter"
    :maxlength="maxlength"
    :loading="loading || v.$pending"
    :label="label"
    :value="value"
    :disabled="disabled"
    :no-error-tooltip="noErrorTooltip"
  >
    <template #default="slotProps">
      <input
        v-model="value"
        :type="show ? 'text' : 'password'"
        v-bind="$attrs"
        :maxlength="maxlength"
        :class="slotProps.class"
        :disabled="disabled"
        @input="v.$touch"
      />
    </template>
    <template #append>
      <IconMdiEye v-if="show" @click="show = false" />
      <IconMdiEyeOff v-else @click="show = true" />
    </template>
    <template v-for="(_, name) in $slots" #[name]="slotData">
      <slot :name="name" v-bind="slotData || {}" />
    </template>
  </InputWrapper>
</template>
