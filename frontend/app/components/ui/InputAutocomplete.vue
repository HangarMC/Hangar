<script setup lang="ts" generic="T">
import type { ValidationRule } from "@vuelidate/core";
import type { Option } from "#shared/types/components/ui/InputAutocomplete";

const emit = defineEmits<{
  (e: "update:modelValue", value?: T): void;
  (e: "search", value?: T): void | Promise<void>;
}>();
const internalVal = computed({
  get: () => props.modelValue,
  set: (val) => emit("update:modelValue", val),
});

const props = withDefaults(
  defineProps<{
    id: string;
    modelValue?: T;
    values: Option<T>[] | Record<string, any> | string[];
    itemValue?: string | ((object: T) => string);
    itemText?: string | ((object: T) => string);
    disabled?: boolean;
    label?: string;
    loading?: boolean;
    messages?: string[];
    errorMessages?: string[];
    rules?: ValidationRule<string | undefined>[];
    noErrorTooltip?: boolean;
  }>(),
  {
    modelValue: undefined,
    itemValue: "value",
    itemText: "text",
    label: "",
    loading: false,
    messages: () => [],
    errorMessages: () => [],
    rules: () => [],
  }
);

function getValue(val: any) {
  if (typeof props.itemValue === "function") {
    return props.itemValue(val);
  } else if (val[props.itemValue]) {
    return val[props.itemValue];
  } else {
    return val as string;
  }
}

function getText(val: any) {
  if (typeof props.itemText === "function") {
    return props.itemText(val);
  } else if (val[props.itemText]) {
    return val[props.itemText];
  } else {
    return val as string;
  }
}

onMounted(() => {
  if (internalVal.value) {
    emit("search", internalVal.value);
  }
});

watch(internalVal, (val) => emit("search", val));

const errorMessages = computed(() => props.errorMessages);
const { v, errors, hasError } = useValidation(props.label, props.rules, internalVal, errorMessages);
</script>

<template>
  <InputWrapper
    :errors="errors"
    :messages="messages"
    :has-error="hasError"
    :loading="loading || v.$pending"
    :label="label"
    :value="internalVal"
    :disabled="disabled"
    :no-error-tooltip="noErrorTooltip"
  >
    <template #default="slotProps">
      <input v-model="internalVal" type="text" v-bind="$attrs" :class="slotProps.class" :list="id" :disabled="disabled" @blur="v.$touch()" />
      <datalist :id="id">
        <option v-for="val in values" :key="getValue(val)" :value="getValue(val)">
          {{ getText(val) }}
        </option>
      </datalist>
    </template>
    <!-- @vue-ignore -->
    <template v-for="(_, name) in $slots" #[name]="slotData">
      <slot :name="name" v-bind="slotData || {}" />
    </template>
  </InputWrapper>
</template>
