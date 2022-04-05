<script lang="ts" setup>
import { computed, ref } from "vue";
import { FloatingLabel, inputClasses } from "~/composables/useInputHelper";

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
  errorMessages?: string[];
  counter?: boolean;
  maxlength?: number;
}>();

// TODO proper validation
const error = computed<boolean>(() => {
  return props.errorMessages ? props.errorMessages.length > 0 : false;
});

if (!tags.value) tags.value = [];

function remove(t: string) {
  tags.value = tags.value.filter((v) => v != t);
}

function add() {
  if (tag.value && (!props.maxlength || tags.value.length < props.maxlength)) {
    tags.value.push(tag.value);
    tag.value = "";
  }
}
</script>

<template>
  <div class="relative flex items-center pointer-events-none" :class="{ filled: (modelValue && modelValue.length) || tag, error: error }">
    <div :class="inputClasses">
      <span v-for="t in tags" :key="t" class="bg-primary-light-400 rounded-4xl px-1 py-1 mx-1 h-30px inline-flex items-center" dark="text-black">
        {{ t }}
        <button class="text-gray-400 ml-1 inline-flex pointer-events-auto" hover="text-gray-500" @click="remove(t)"><icon-mdi-close-circle /></button>
      </span>
      <input v-model="tag" type="text" class="pointer-events-auto outline-none bg-transparent" @keydown.enter="add" />
    </div>
    <floating-label :label="label" />
    <span v-if="counter && maxlength">{{ tags?.length || 0 }}/{{ maxlength }}</span>
    <span v-else-if="counter">{{ tags?.length || 0 }}</span>
  </div>
  <template v-if="errorMessages && errorMessages.length > 0">
    <span v-for="msg in errorMessages" :key="msg" class="text-red-500">{{ msg }}</span>
  </template>
</template>
