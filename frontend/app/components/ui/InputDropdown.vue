<script setup lang="ts" generic="T">
import type { ValidationRule } from "@vuelidate/core";
import type { Option } from "#shared/types/components/ui/InputSelect";

const i18n = useI18n();

const emit = defineEmits<{
  (e: "update:modelValue", value?: T): void;
}>();
const internalVal = computed({
  get: () => props.modelValue,
  set: (val) => emit("update:modelValue", val),
});

const props = withDefaults(
  defineProps<{
    modelValue?: T;
    values: Option<T>[] | Record<string, any> | string[] | object[];
    itemValue?: string;
    itemText?: string;
    label?: string;
    placeholder?: string;
    errorMessages?: string[];
    rules?: ValidationRule<string | undefined>[];
    i18nTextValues?: boolean;
    buttonSize?: "sm" | "md" | "lg";
  }>(),
  {
    modelValue: undefined,
    itemValue: "value",
    itemText: "text",
    label: "",
    placeholder: undefined,
    errorMessages: () => [],
    rules: () => [],
    i18nTextValues: false,
    buttonSize: "lg",
  }
);

const errorMessages = computed(() => props.errorMessages);
const { v, errors } = useValidation(props.label, props.rules, internalVal, errorMessages);

const options = computed(() => (Array.isArray(props.values) ? props.values : Object.values(props.values)) as any[]);

function valueOf(val: any) {
  return val?.[props.itemValue] ?? val;
}

function textOf(val: any) {
  const text = val?.[props.itemText] ?? val;
  return props.i18nTextValues ? i18n.t(text) : text;
}

const selectedText = computed(() => {
  const match = options.value.find((val) => valueOf(val) === internalVal.value);
  return match === undefined ? undefined : textOf(match);
});

function select(val: any) {
  internalVal.value = valueOf(val);
  v.value.$touch();
}
</script>

<template>
  <div>
    <label v-if="label" class="mb-2 block">{{ label }}</label>
    <ErrorTooltip :error-messages="errors">
      <DropdownButton button-variant="outline" button-tone="neutral" :button-size="buttonSize">
        <template #button-label>
          <span class="truncate" :class="{ 'text-gray-secondary': selectedText === undefined }">
            {{ selectedText ?? placeholder ?? i18n.t("general.select") }}
          </span>
        </template>
        <template #default="{ close }">
          <DropdownItem
            v-for="val in options"
            :key="valueOf(val)"
            :selected="valueOf(val) === internalVal"
            @click="
              select(val);
              close();
            "
          >
            {{ textOf(val) }}
          </DropdownItem>
        </template>
      </DropdownButton>
    </ErrorTooltip>
  </div>
</template>
