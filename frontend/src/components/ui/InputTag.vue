<script lang="ts" setup>
import { computed, ref, watch } from "vue";
import type { ValidationRule } from "@vuelidate/core";
import { useValidation } from "~/composables/useValidationHelpers";
import InputWrapper from "~/components/ui/InputWrapper.vue";

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
      const separatorIndex = commaIndex !== -1 ? commaIndex : remainingString.indexOf(" ");
      if (separatorIndex === -1) {
        // No more separators!
        tag.value = remainingString.substring(0, props.tagMaxlength);
        add();
        return;
      }

      tag.value = remainingString.substring(0, Math.min(separatorIndex, props.tagMaxlength));
      if (separatorIndex === remainingString.length - 1) {
        // The last character is a separator
        add();
        return;
      }

      // Add the current tag and check for more
      remainingString = remainingString.substring(separatorIndex + 1, remainingString.length);
      add();
    } while (remainingString.includes(",") || remainingString.includes(" "));

    if (remainingString.length !== 0) {
      // And the last one
      tag.value = remainingString.substring(0, props.tagMaxlength);
      add();
    }
  } else {
    tag.value = t.substring(0, props.tagMaxlength);
  }
});

function remove(t: string) {
  const index = tags.value.indexOf(t);
  if (index === -1) return;
  tags.value.splice(index, 1);
  v.value.$touch();
}

function add() {
  if (tag.value && (!props.maxlength || tags.value.length < props.maxlength)) {
    if (props.options) {
      if (!filteredOptions.value?.includes(tag.value)) {
        return;
      }
    }
    tags.value.push(tag.value);
    tag.value = "";
  }
}

const filteredOptions = computed(() => {
  if (props.options) {
    return props.options.filter((o) => !tags.value.includes(o));
  }
  return props.options;
});
</script>

<template>
  <InputWrapper
    v-slot="slotProps"
    :errors="errors"
    :messages="messages"
    :has-error="hasError"
    :counter="counter"
    :maxlength="maxlength"
    :loading="loading || v.$pending"
    :label="label"
    :value="tags"
    :no-error-tooltip="noErrorTooltip"
  >
    <div class="flex flex-wrap flex-grow gap-2 mt-2">
      <span v-for="t in tags" :key="t" class="bg-primary-light-400 rounded-xl px-1 py-1 h-30px inline-flex items-center dark:bg-gray-600">
        {{ t }}
        <span class="text-gray-400 ml-1 inline-flex pointer-events-auto cursor-pointer hover:text-gray-500" @click="remove(t)">
          <icon-mdi-close-circle />
        </span>
      </span>
      <template v-if="options">
        <input
          v-model="tag"
          type="text"
          v-bind="$attrs"
          :class="slotProps.class"
          :list="id"
          class="pointer-events-auto flex-grow !bg-gray-100 rounded-xl px-2 dark:(!bg-gray-500 text-white)"
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
      <div
        v-else
        class="pointer-events-auto relative flex flex-column items-center flex-grow !bg-gray-100 rounded-xl px-2 dark:(!bg-gray-500 text-white)"
        :class="slotProps.class"
      >
        <IconMdiSubdirectoryArrowLeft class="absolute right-2"> </IconMdiSubdirectoryArrowLeft>
        <input v-model="tag" type="text" :class="slotProps.class" @keydown.enter="add" @blur="v.$touch()" />
      </div>
    </div>
  </InputWrapper>
</template>
