<script lang="ts" setup>
import { computed } from "vue";
import Button from "~/lib/components/design/Button.vue";

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
    <Button :disabled="page <= 0" aria-label="First page" @click="gotoPage(0)">&lt;&lt;</Button>
    <Button :disabled="page <= 0" aria-label="Prev page" @click="gotoPage(page - 1)">&lt;</Button>
    <Button v-for="index in options" :key="index" :disabled="page === index - 1" :aria-label="'Page ' + index" @click="gotoPage(index - 1)">{{ index }}</Button>
    <Button :disabled="page >= pages - 1" aria-label="Next page" @click="gotoPage(page + 1)">&gt;</Button>
    <Button :disabled="page >= pages - 1" aria-label="Last page" @click="gotoPage(pages - 1)">&gt;&gt;</Button>
  </div>
</template>
