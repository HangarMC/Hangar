<script lang="ts" setup>
import Button from "~/lib/components/design/Button.vue";
import { computed } from "vue";

const props = defineProps<{
  page: number;
  pages: number;
}>();

const emit = defineEmits<{
  (e: "update:page", page: number): void;
}>();
const page = computed({
  get: () => props.page,
  set: (value) => emit("update:page", value),
});

const options = computed<number[]>(() => {
  const result: number[] = [];
  if (props.pages < 5) {
    for (let i = 1; i <= props.pages; i++) {
      result.push(i);
    }
  } else {
    let low = page.value - 1;
    let high = page.value + 3;
    while (low < 1) {
      low++;
      high++;
    }
    while (high > props.pages) {
      low--;
      high--;
    }
    for (let i = low; i <= high; i++) {
      result.push(i);
    }
  }
  return result;
});

function gotoPage(pageNo: number) {
  page.value = pageNo;
}
</script>

<template>
  <div class="flex gap-2 justify-center">
    <Button :disabled="page <= 0" @click="gotoPage(0)">&lt;&lt;</Button>
    <Button :disabled="page <= 0" @click="gotoPage(page - 1)">&lt;</Button>
    <Button v-for="index in options" :key="index" :disabled="page === index - 1" @click="gotoPage(index - 1)">{{ index }}</Button>
    <Button :disabled="page >= pages - 1" @click="gotoPage(page + 1)">&gt;</Button>
    <Button :disabled="page >= pages - 1" @click="gotoPage(pages - 1)">&gt;&gt;</Button>
  </div>
</template>
