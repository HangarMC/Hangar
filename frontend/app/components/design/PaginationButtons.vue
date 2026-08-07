<script lang="ts" setup>
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
  <div class="flex gap-1.5 justify-center">
    <Button variant="outline" tone="neutral" size="sm" icon-only :disabled="page <= 0" aria-label="First page" @click="gotoPage(0)">
      <IconMdiChevronDoubleLeft />
    </Button>
    <Button variant="outline" tone="neutral" size="sm" icon-only :disabled="page <= 0" aria-label="Prev page" @click="gotoPage(page - 1)">
      <IconMdiChevronLeft />
    </Button>
    <Button
      v-for="index in options"
      :key="index"
      :variant="page === index - 1 ? 'solid' : 'outline'"
      :tone="page === index - 1 ? 'primary' : 'neutral'"
      size="sm"
      :aria-label="'Page ' + index"
      :aria-current="page === index - 1 ? 'page' : undefined"
      class="min-w-8 !px-2 tabular-nums"
      @click="gotoPage(index - 1)"
    >
      {{ index }}
    </Button>
    <Button variant="outline" tone="neutral" size="sm" icon-only :disabled="page >= pages - 1" aria-label="Next page" @click="gotoPage(page + 1)">
      <IconMdiChevronRight />
    </Button>
    <Button variant="outline" tone="neutral" size="sm" icon-only :disabled="page >= pages - 1" aria-label="Last page" @click="gotoPage(pages - 1)">
      <IconMdiChevronDoubleRight />
    </Button>
  </div>
</template>
