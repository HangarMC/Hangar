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
    <span v-for="t in tags" :key="t">{{ t }}<button @click="remove(t)">(x)</button></span>
    <input v-model="tag" type="text" @keydown.enter="add" />
  </div>
</template>
