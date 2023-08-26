<script lang="ts" setup>
import { computed } from "vue";
import Button from "~/components/design/Button.vue";

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

function visibleCss(value: boolean) {
  return `visibility: ${value ? "visible" : "hidden"}`;
}
</script>

<template>
  <div class="flex gap-1.5 rounded-md justify-center">
    <Button class="bg-slate-400 dark:bg-slate-700 px-3" :style="visibleCss(page > 0)" :disabled="page <= 0" aria-label="First page" @click="gotoPage(0)">
      <span class="nav-btn">«</span>
    </Button>
    <Button class="bg-slate-400 dark:bg-slate-700 px-3" :style="visibleCss(page > 0)" :disabled="page <= 0" aria-label="Prev page" @click="gotoPage(page - 1)">
      <span class="nav-btn">‹</span>
    </Button>
    <Button
      v-for="index in options"
      :key="index"
      :disabled="page === index - 1"
      :aria-label="'Page ' + index"
      class="bg-slate-400 dark:bg-slate-700 px-3"
      :class="{ 'disabled:bg-slate-500 disabled:dark:bg-slate-500': page === index - 1 }"
      @click="gotoPage(index - 1)"
    >
      <span class="text-white">
        {{ index }}
      </span>
    </Button>
    <Button
      class="bg-slate-400 dark:bg-slate-700 px-3"
      :style="visibleCss(page < pages - 1)"
      :disabled="page >= pages - 1"
      aria-label="Next page"
      @click="gotoPage(page + 1)"
    >
      <span class="nav-btn">›</span>
    </Button>
    <Button
      class="bg-slate-400 dark:bg-slate-700 px-3"
      :style="visibleCss(page < pages - 1)"
      :disabled="page >= pages - 1"
      aria-label="Last page"
      @click="gotoPage(pages - 1)"
    >
      <span class="nav-btn">»</span>
    </Button>
  </div>
</template>
<style scoped>
.nav-btn {
  position: relative;
  top: -2px;
}
</style>
