<script lang="ts" setup generic="T extends Record<string, any>">
import type { Pagination } from "#shared/types/backend";

const props = withDefaults(
  defineProps<{
    items: T[];
    itemsPerPage?: number;
    serverPagination?: Pagination;
    alwaysShow?: boolean;
    resetAnchor?: HTMLElement | null;
  }>(),
  {
    itemsPerPage: 10,
    serverPagination: undefined,
    alwaysShow: false,
    resetAnchor: undefined,
  }
);

const page = ref(props.serverPagination ? Math.ceil(props.serverPagination.offset / props.serverPagination.limit) : 0);

const slicedItems = computed(() => {
  if (props.serverPagination) {
    return props.items;
  }
  return props.items?.slice(page.value * props.itemsPerPage, (page.value + 1) * props.itemsPerPage);
});
const pageCount = computed(() =>
  Math.ceil(
    (props.serverPagination ? props.serverPagination.count : props.items?.length) / (props.serverPagination ? props.serverPagination.limit : props.itemsPerPage)
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

defineExpose({ recalcPage, page, updatePage });

const emit = defineEmits<{
  (e: "update:page", value: number): void;
}>();
function updatePage(newPage: number) {
  if (newPage === page.value) {
    return;
  }

  page.value = newPage;
  if (import.meta.client && props.resetAnchor) {
    window.scrollBy(0, props.resetAnchor.getBoundingClientRect().y);
  }
  emit("update:page", newPage);
}

defineSlots<{
  pagination(props: { page: number; updatePage: (newPage: number) => void; pages: number }): any;
  default(props: { item: T; idx: number; key: number }): any;
}>();
</script>

<template>
  <slot v-for="(item, idx) in slicedItems" :key="idx" :item="item" :idx="idx" />
  <slot v-if="alwaysShow || pageCount > 1" name="pagination" :page="page" :update-page="updatePage" :pages="pageCount">
    <PaginationButtons :page="page" :pages="pageCount" @update:page="updatePage" />
  </slot>
</template>
