<script lang="ts" setup>
import { computed, ref, watch } from "vue";
import PaginationButtons from "~/lib/components/design/PaginationButtons.vue";
import { Pagination } from "hangar-api";

const props = withDefaults(
  defineProps<{
    items: Array<unknown>;
    itemsPerPage?: number;
    serverPagination: Pagination;
  }>(),
  {
    itemsPerPage: 10,
  }
);

const page = ref(props.serverPagination ? Math.ceil(props.serverPagination.offset / props.serverPagination.limit) : 0);

const slicedItems = computed(() => {
  if (props.serverPagination) {
    return props.items;
  }
  return props.items.slice(page.value * props.itemsPerPage, (page.value + 1) * props.itemsPerPage);
});
const pageCount = computed(() =>
  Math.ceil(
    (props.serverPagination ? props.serverPagination.count : props.items.length) / (props.serverPagination ? props.serverPagination.limit : props.itemsPerPage)
  )
);

function recalcPage() {
  const minPage = 0;
  const maxPage = pageCount.value - 1;

  if (page.value > maxPage) {
    page.value = maxPage;
  }
  if (page.value < minPage) {
    page.value = minPage;
  }
}

watch(
  () => props.items,
  () => {
    recalcPage();
  }
);

defineExpose({ recalcPage, page });

const emit = defineEmits<{
  (e: "update:page", value: number): void;
}>();
function updatePage(newPage: number) {
  page.value = newPage;
  emit("update:page", newPage);
}
</script>

<template>
  <slot v-for="(item, idx) in slicedItems" :key="idx" :item="item" :idx="idx" />
  <slot name="pagination" :page="page" :update-page="(p) => (page = p)" :pages="pageCount">
    <PaginationButtons :page="page" :pages="pageCount" @update:page="updatePage" />
  </slot>
</template>
