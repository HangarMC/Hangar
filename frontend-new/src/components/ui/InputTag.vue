<script lang="ts" setup>
import { computed, ref, watch } from "vue";
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

const tagMaxlength = 20;

const errorMessages = computed(() => props.errorMessages);
const { v, errors, hasError } = useValidation(props.label, props.rules, tags, errorMessages);

if (!tags.value) tags.value = [];

watch(tag, (t) => {
  tag.value = t.replace(" ", "").substring(0, tagMaxlength);
});

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
    <div class="flex flex-wrap flex-grow gap-2 mt-2">
      <span v-for="t in tags" :key="t" class="bg-primary-light-400 rounded-4xl px-1 py-1 h-30px inline-flex items-center" dark="bg-gray-600">
        {{ t }}
        <span class="text-gray-400 ml-1 inline-flex pointer-events-auto cursor-pointer" hover="text-gray-500" @click="remove(t)">
          <icon-mdi-close-circle />
        </span>
      </span>
      <input
        v-model="tag"
        type="text"
        class="pointer-events-auto flex-grow !bg-gray-100 rounded-xl px-2"
        dark="!bg-gray-500 text-white"
        :class="slotProps.class"
        :maxlength="tagMaxlength"
        @keydown.enter="add"
        @blur="v.$touch()"
      />
    </div>
  </InputWrapper>
</template>
