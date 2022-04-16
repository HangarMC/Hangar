<script lang="ts" setup>
import { computed, ref } from "vue";
import { useValidation } from "~/composables/useValidationHelpers";
import { ValidationRule } from "@vuelidate/core";
import InputWrapper from "~/components/ui/InputWrapper.vue";

const tag = ref<string>("");
const emit = defineEmits<{
  (e: "update:modelValue", tags: string[]): void;
}>();
const tags = computed({
  get: () => props.modelValue,
  set: (value) => emit("update:modelValue", value),
});
const props = defineProps<{
  modelValue: string[];
  label?: string;
  counter?: boolean;
  maxlength?: number;
  loading?: boolean;
  errorMessages?: string[];
  rules?: ValidationRule<string | undefined>[];
}>();

const { v, errors, hasError } = useValidation(props.label, props.rules, tags, props.errorMessages);

if (!tags.value) tags.value = [];

function remove(t: string) {
  tags.value = tags.value.filter((v) => v != t);
  v.value.$touch();
}

function add() {
  if (tag.value && (!props.maxlength || tags.value.length < props.maxlength)) {
    tags.value.push(tag.value);
    tag.value = "";
  }
}
</script>

<template>
  <InputWrapper
    v-slot="slotProps"
    :errors="errors"
    :has-error="hasError"
    :counter="counter"
    :maxlength="maxlength"
    :loading="loading || v.$pending"
    :label="label"
    :value="tags"
  >
    <span v-for="t in tags" :key="t" class="bg-primary-light-400 rounded-4xl px-1 py-1 mx-1 h-30px inline-flex items-center" dark="text-black">
      {{ t }}
      <button class="text-gray-400 ml-1 inline-flex pointer-events-auto" hover="text-gray-500" @click="remove(t)"><icon-mdi-close-circle /></button>
    </span>
    <input
      v-model="tag"
      type="text"
      class="pointer-events-auto outline-none bg-transparent flex-grow"
      :class="slotProps.class"
      @keydown.enter="add"
      @blur="v.$touch()"
    />
  </InputWrapper>
</template>
