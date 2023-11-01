<script lang="ts" setup generic="T extends Record<string, any>">
import { reactive, ref, watch } from "vue";
import type { Pagination } from "hangar-api";
import { hasSlotContent } from "~/composables/useSlot";
import Table from "~/components/design/Table.vue";
import PaginationButtons from "~/components/design/PaginationButtons.vue";
import PaginationComponent from "~/components/design/Pagination.vue";
import type { Header } from "~/types/components/SortableTable";

type T = Record<string, any>; // remove when https://github.com/vuejs/rfcs/discussions/436 lands or when using volar

const props = withDefaults(
  defineProps<{
    headers: Header[];
    items: T[];
    expandable?: boolean;
    serverPagination?: Pagination;
    initialSorter?: Record<string, number>;
    maxSorters?: number;
  }>(),
  {
    maxSorters: 1,
    serverPagination: undefined,
    initialSorter: undefined,
  }
);

const expanded = ref<Record<number, boolean>>({});
const sorter = reactive<Record<string, number>>(props.initialSorter || {});
const sorted = ref<T[]>(props.items);

const page = ref(0);

function sort() {
  if (props.serverPagination) {
    // if we use server fetched data, we don't want to sort on the client, ever
    sorted.value = props.items;
    return;
  }
  sorted.value = [...props.items].sort((a, b) => {
    for (const field of Object.keys(sorter)) {
      if (sorter[field] === 0) continue;
      if (a[field] > b[field]) return sorter[field];
      if (a[field] < b[field]) return -sorter[field];
    }
    return 0;
  });
}

watch(() => props.items, sort);

function checkReset() {
  const keys = Object.keys(sorter);
  if (keys.length >= props.maxSorters) {
    for (const k of keys) {
      delete sorter[k];
    }
  }
}

function click(header: Header) {
  if (header.sortable) {
    if (sorter[header.name] === 1) {
      checkReset();
      sorter[header.name] = -1;
    } else if (sorter[header.name] === -1) {
      checkReset();
      sorter[header.name] = 0;
    } else {
      checkReset();
      sorter[header.name] = 1;
    }
    sort();
    emit("update:sort", header.name, sorter);
  }
}

const emit = defineEmits<{
  (e: "update:page", value: number): void;
  (e: "update:sort", col: string, sorter: Record<string, number>): void;
}>();
function updatePage(newPage: number) {
  emit("update:page", newPage);
}
</script>

<template>
  <Table class="w-full">
    <thead>
      <tr>
        <th v-for="header in headers" :key="header.name" :style="header.width ? 'width: ' + header.width : ''" @click="click(header)">
          <div class="items-center inline-flex" :cursor="header.sortable ? 'pointer' : 'auto'">
            <span class="mr-2"> {{ header.title }}</span>
            <IconMdiSortAscending v-if="sorter[header.name] === 1" />
            <IconMdiSortDescending v-else-if="sorter[header.name] === -1" />
            <IconMdiSort v-else-if="header.sortable" class="text-gray-400" />
          </div>
        </th>
      </tr>
    </thead>
    <tbody>
      <PaginationComponent v-if="sorted?.length !== 0" :items="sorted" :server-pagination="serverPagination" @update:page="updatePage">
        <template #default="{ item, idx }">
          <tr>
            <td v-for="header in headers" :key="header.name" :style="header.width ? 'width: ' + header.width : ''" @click="expanded[idx] = !expanded[idx]">
              <template v-if="hasSlotContent($slots['item_' + header.name], { item: item })">
                <slot :name="'item_' + header.name" :item="item"></slot>
              </template>
              <template v-else>
                {{ item[header.name] }}
              </template>
            </td>
          </tr>

          <tr v-if="expandable && expanded[idx]" class="!border-dashed">
            <slot name="expanded-item" :item="item" :headers="headers"></slot>
          </tr>
        </template>
        <template #pagination="slotProps">
          <tr>
            <td :colspan="headers.length">
              <PaginationButtons :page="slotProps.page" :pages="slotProps.pages" @update:page="slotProps.updatePage" />
            </td>
          </tr>
        </template>
      </PaginationComponent>
      <tr v-if="!items || items?.length === 0">
        <td :colspan="headers.length">
          <slot name="empty"></slot>
        </td>
      </tr>
    </tbody>
  </Table>
</template>
