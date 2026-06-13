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
const open = ref(false);
const root = useTemplateRef<HTMLElement>("root");

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

function select(val: any) {
  internalVal.value = getValue(val) as T;
  open.value = false;
}

function close(event: MouseEvent) {
  if (!root.value?.contains(event.target as Node)) {
    open.value = false;
  }
}

onMounted(() => document.addEventListener("mousedown", close));
onBeforeUnmount(() => document.removeEventListener("mousedown", close));

const errorMessages = computed(() => props.errorMessages);
const { v, errors, hasError } = useValidation(props.label, props.rules, internalVal, errorMessages);
</script>

<template>
  <div ref="root" class="relative w-full">
    <label
      :class="[
        'relative flex h-10.5 w-full items-center rounded-lg border border-transparent bg-gray-100 outline-none',
        'hover:border-gray-300 focus-within:border-gray-400 dark:hover:border-gray-700 dark:focus-within:border-gray-600',
        'dark:bg-gray-800',
        'error:border-red-400',
        'transition-colors duration-200 ease',
        { 'border-red-400!': hasError },
      ]"
    >
      <input
        :id="id"
        v-model="internalVal"
        type="text"
        autocomplete="off"
        v-bind="$attrs"
        class="h-full min-w-0 grow bg-transparent px-3 outline-none"
        :placeholder="label"
        :disabled="disabled"
        @focus="open = true"
        @input="open = true"
        @blur="v.$touch()"
      />
    </label>

    <div
      v-if="open && Object.keys(values).length > 0"
      class="background-default absolute top-full right-0 left-0 z-100 mt-1 flex max-h-60 flex-col gap-1 overflow-y-auto rounded-lg border border-gray-800 p-1 shadow-lg shadow-charcoal-900"
    >
      <button
        v-for="val in values"
        :key="getValue(val)"
        type="button"
        class="w-full rounded-lg border border-transparent px-2 py-1 text-left font-semibold transition-all duration-250 hover:border-gray-300 hover:bg-gray-100 dark:hover:border-gray-700 dark:hover:bg-gray-800"
        :class="{ 'border-gray-300 bg-gray-100 dark:border-gray-700 dark:bg-gray-800': getValue(val) === internalVal }"
        @mousedown.prevent="select(val)"
      >
        {{ getText(val) }}
      </button>
    </div>
    <p v-for="message in errors" :key="String(message)" class="mt-1 text-xs text-red-400">
      {{ typeof message === "string" ? message : message.$message }}
    </p>
    <p v-for="message in messages" :key="message" class="mt-1 text-xs text-gray">
      {{ message }}
    </p>
  </div>
</template>
