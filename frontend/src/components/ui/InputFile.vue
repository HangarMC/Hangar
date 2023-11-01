<script lang="ts" setup>
import { computed, ref, watch } from "vue";
import type { ValidationRule } from "@vuelidate/core";
import { useValidation } from "~/composables/useValidationHelpers";
import InputWrapper from "~/components/ui/InputWrapper.vue";

const emit = defineEmits<{
  (e: "update:modelValue", file: File | null | undefined): void;
}>();
const file = computed({
  get: () => props.modelValue,
  set: (value) => emit("update:modelValue", value),
});
const props = defineProps<{
  modelValue?: File | null;
  label?: string;
  disabled?: boolean;
  showSize?: boolean;
  loading?: boolean;
  messages?: string[];
  errorMessages?: string[];
  rules?: ValidationRule<string | undefined>[];
  noErrorTooltip?: boolean;
}>();

const errorMessages = computed(() => props.errorMessages);
const { v, errors, hasError } = useValidation(props.label, props.rules, file, errorMessages);

function onFileChange(e: Event) {
  const files = (e.target as HTMLInputElement).files || (e as InputEvent).dataTransfer?.files;
  if (!files?.length) return;
  file.value = files[0];
}

// check if we got reset from the outside
const input = ref();
watch(file, (newVal) => {
  if (!newVal) {
    input.value.value = null;
  }
});
</script>

<template>
  <InputWrapper
    :errors="errors"
    :messages="messages"
    :has-error="hasError"
    :loading="loading || v.$pending"
    :label="label"
    :value="file"
    :disabled="disabled"
    :no-error-tooltip="noErrorTooltip"
  >
    <template #default="slotProps">
      <!-- todo make button fancy -->
      <input ref="input" type="file" v-bind="$attrs" :disabled="disabled" :class="slotProps.class" @change="onFileChange" />
    </template>
    <template v-for="(_, name) in $slots" #[name]="slotData">
      <slot :name="name" v-bind="slotData || {}" />
    </template>
  </InputWrapper>
</template>
