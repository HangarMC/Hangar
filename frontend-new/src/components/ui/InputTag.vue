<script lang="ts" setup>
import { computed, ref } from "vue";

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
}>();

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
  <label>
    <span v-if="label">{{ label }}</span>
    <span v-for="t in tags" :key="t" class="bg-gray-200 rounded-4xl px-2 py-1 mx-1 inline-flex items-center" dark="text-black">
      {{ t }}
      <button class="text-gray-400 ml-1 inline-flex" hover="text-gray-500" @click="remove(t)"><icon-mdi-close-circle /></button>
    </span>
    <!-- todo style the input -->
    <input v-model="tag" type="text" dark="text-black" @keydown.enter="add" />
    <span v-if="counter && maxlength">{{ tags?.length || 0 }}/{{ maxlength }}</span>
    <span v-else-if="counter">{{ tags?.length || 0 }}</span>
  </label>
</template>
