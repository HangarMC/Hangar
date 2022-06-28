<script lang="ts" setup>
import { computed, ref } from "vue";
import PaginationButtons from "~/lib/components/design/PaginationButtons.vue";

const props = withDefaults(
  defineProps<{
    items: [];
    itemsPerPage?: number;
  }>(),
  {
    itemsPerPage: 10,
  }
);

const page = ref(0);

const slicedItems = computed(() => props.items.slice(page.value * props.itemsPerPage, (page.value + 1) * props.itemsPerPage));
</script>

<template>
  <slot v-for="(item, idx) in slicedItems" :key="idx" :item="item" :idx="idx" />
  <slot name="pagination" :page="page" :update-page="(p) => (page = p)" :pages="Math.ceil(items.length / itemsPerPage)">
    <PaginationButtons :page="page" :pages="Math.ceil(items.length / itemsPerPage)" @update:page="(p) => (page = p)" />
  </slot>
</template>
