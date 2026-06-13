<script lang="ts" setup>
import type { ValidationRule } from "@vuelidate/core";

const tag = ref<string>("");
const emit = defineEmits<{
  (e: "update:modelValue", tags: string[]): void;
}>();
const tags = computed({
  get: () => props.modelValue,
  set: (value) => emit("update:modelValue", value),
});
const props = withDefaults(
  defineProps<{
    modelValue: string[];
    label?: string;
    counter?: boolean;
    maxlength?: number;
    loading?: boolean;
    messages?: string[];
    errorMessages?: string[];
    rules?: ValidationRule<string[] | undefined>[];
    tagMaxlength?: number;
    options?: string[];
    noErrorTooltip?: boolean;
  }>(),
  {
    tagMaxlength: 20,
    options: undefined,
    label: undefined,
    maxlength: undefined,
    messages: undefined,
    errorMessages: undefined,
    rules: undefined,
  }
);

const id = Math.random() + "";

const errorMessages = computed(() => props.errorMessages);
const { v, errors, hasError } = useValidation(props.label, props.rules, tags, errorMessages);

if (!tags.value) tags.value = [];

watch(tag, (t) => {
  // comma and space act like enter, didn't find a nicer way to implement this...
  if (t.includes(",") || t.includes(" ")) {
    let remainingString = t;
    do {
      const commaIndex = remainingString.indexOf(",");
      const separatorIndex = commaIndex === -1 ? remainingString.indexOf(" ") : commaIndex;
      if (separatorIndex === -1) {
        // No more separators!
        tag.value = remainingString.slice(0, Math.max(0, props.tagMaxlength));
        add();
        return;
      }

      tag.value = remainingString.slice(0, Math.max(0, Math.min(separatorIndex, props.tagMaxlength)));
      if (separatorIndex === remainingString.length - 1) {
        // The last character is a separator
        add();
        return;
      }

      // Add the current tag and check for more
      // eslint-disable-next-line unicorn/prefer-string-slice
      remainingString = remainingString.substring(separatorIndex + 1, remainingString.length);
      add();
    } while (remainingString.includes(",") || remainingString.includes(" "));

    if (remainingString.length > 0) {
      // And the last one
      tag.value = remainingString.slice(0, Math.max(0, props.tagMaxlength));
      add();
    }
  } else {
    tag.value = t.slice(0, Math.max(0, props.tagMaxlength));
  }
});

function remove(t: string) {
  const index = tags.value.indexOf(t);
  if (index === -1) return;
  tags.value.splice(index, 1);
  v.value.$touch();
}

function add() {
  const value = tag.value.trim();
  if (!value || (props.maxlength && tags.value.length >= props.maxlength)) return;
  if (tags.value.some((existingTag) => existingTag.toLocaleLowerCase() === value.toLocaleLowerCase())) {
    tag.value = "";
    v.value.$touch();
    return;
  }
  if (props.options && !filteredOptions.value?.includes(value)) return;

  tags.value.push(value);
  tag.value = "";
  v.value.$touch();
}

const filteredOptions = computed(() => {
  if (props.options) {
    return props.options.filter((o) => !tags.value.includes(o));
  }
  return props.options;
});
</script>

<template>
  <InputWrapper :errors="errors" :messages="messages" :has-error="hasError" :loading="loading || v.$pending" :value="tags" :no-error-tooltip="noErrorTooltip">
    <template #default>
      <div class="flex min-w-0 flex-grow flex-wrap items-center gap-1 px-1.5 py-0">
        <span
          v-for="t in tags"
          :key="t"
          class="inline-flex h-6.5 items-center rounded-md border border-primary-500/50 py-0 pl-2 pr-1 text-sm"
          :style="{ backgroundColor: 'color-mix(in srgb, var(--primary-500) 20%, transparent)' }"
        >
          {{ t }}
          <button type="button" class="ml-0.5 inline-flex h-5 w-5 flex-shrink-0 cursor-pointer items-center justify-center text-gray-400" @click="remove(t)">
            <span class="relative block h-3 w-3" aria-hidden="true">
              <span class="absolute top-1/2 left-0 block h-px w-3 -translate-y-1/2 rotate-45 bg-current" />
              <span class="absolute top-1/2 left-0 block h-px w-3 -translate-y-1/2 -rotate-45 bg-current" />
            </span>
          </button>
        </span>
        <template v-if="options">
          <input
            v-model="tag"
            type="text"
            v-bind="$attrs"
            :placeholder="$attrs.placeholder?.toString() || label"
            :list="id"
            class="min-w-24 flex-grow bg-transparent py-1 outline-none"
            @blur="v.$touch()"
            @keydown.enter="add"
            @change="add"
          />
          <datalist :id="id">
            <option v-for="val in filteredOptions" :key="val" :value="val">
              {{ val }}
            </option>
          </datalist>
        </template>
        <input
          v-else
          v-model="tag"
          type="text"
          v-bind="$attrs"
          :placeholder="$attrs.placeholder?.toString() || label"
          class="min-w-24 flex-grow bg-transparent py-1 outline-none"
          @keydown.enter.prevent="add"
          @blur="v.$touch()"
        />
      </div>
    </template>
    <template v-if="counter" #append>
      <span class="pr-3 text-sm text-gray">{{ tags.length }}/{{ maxlength }}</span>
    </template>
  </InputWrapper>
</template>
