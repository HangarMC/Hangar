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
}>();

function remove(t: string) {
  tags.value = tags.value.filter((v) => v != t);
}

function add() {
  if (tag.value) {
    tags.value.push(tag.value);
    tag.value = "";
  }
}
</script>

<template>
  <div>
    <span v-for="t in tags" :key="t" class="bg-gray-200 rounded-4xl px-2 py-1 mx-1 inline-flex items-center" dark="text-black">
      {{ t }}
      <button class="text-gray-400 ml-1 inline-flex" hover="text-gray-500" @click="remove(t)"><icon-mdi-close-circle /></button>
    </span>
    <!-- todo style the input -->
    <input v-model="tag" type="text" dark="text-black" @keydown.enter="add" />
  </div>
</template>
