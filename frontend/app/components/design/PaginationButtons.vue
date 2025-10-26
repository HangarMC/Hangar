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
  <div class="flex gap-1.5 justify-center rounded-md h-10">
    <Button button-type="transparent" class="bg-charcoal-500 py-2 w-10 h-full rounded-full" :disabled="page <= 0" aria-label="First page" @click="gotoPage(0)">
      <IconMdiChevronDoubleLeft />
    </Button>
    <Button button-type="transparent" class="bg-charcoal-500 py-2 w-10 h-full rounded-full" :disabled="page <= 0" aria-label="Prev page" @click="gotoPage(page - 1)">
      <IconMdiChevronLeft />
    </Button>
    <Button
      v-for="index in options"
      :key="index"
      :disabled="page === index - 1"
      :aria-label="'Page ' + index"
      class="bg-transparent w-10 h-full rounded-full"
      button-type="transparent"
      :style="page === index - 1 ? {
        backgroundColor: 'color-mix(in srgb, var(--primary-500) 25%, transparent)',
        borderColor: 'var(--primary-500)'
      } : {}"
      @click="gotoPage(index - 1)"
    >
      <span class="text-white">
        {{ index }}
      </span>
    </Button>
    <Button
      class="bg-charcoal-500 w-10 py-2 h-full rounded-full"
      :disabled="page >= pages - 1"
      aria-label="Next page"
      button-type="transparent"
      @click="gotoPage(page + 1)"
    >
      <IconMdiChevronRight />
    </Button>
    <Button
      class="bg-charcoal-500 w-10 py-2 h-full rounded-full"
      :disabled="page >= pages - 1"
      aria-label="Last page"
      button-type="transparent"
      @click="gotoPage(pages - 1)"
    >
      <IconMdiChevronDoubleRight />
    </Button>
  </div>
</template>
