<script lang="ts" setup generic="T extends Record<string, any>, H extends string">
import type { Ref } from "vue";
import type { Header } from "#shared/types/components/SortableTable";
import type { Pagination } from "#shared/types/backend";

const props = withDefaults(
  defineProps<{
    headers: Header<H>[];
    items: T[];
    expandable?: boolean;
    serverPagination?: Pagination;
    initialSorter?: Partial<Record<H, number>>;
    maxSorters?: number;
    hidePagination?: boolean;
  }>(),
  {
    maxSorters: 1,
    serverPagination: undefined,
    initialSorter: undefined,
    hidePagination: false,
  }
);

const expanded = ref<Record<number, boolean>>({});
const sorter = reactive({ ...props.initialSorter } as Record<H, number>) as Record<H, number>;
const sorted = ref<T[]>(props.items) as Ref<T[]>; // idk why I need a cast here...
const page = ref(props.serverPagination ? Math.floor(props.serverPagination.offset / props.serverPagination.limit) : 0);
const itemsPerPage = 10;
const pageCount = computed(() =>
  Math.ceil((props.serverPagination ? props.serverPagination.count : sorted.value.length) / (props.serverPagination?.limit || itemsPerPage))
);
const displayedItems = computed(() => {
  if (props.serverPagination) {
    return sorted.value;
  }
  return sorted.value.slice(page.value * itemsPerPage, (page.value + 1) * itemsPerPage);
});

function sort() {
  if (props.serverPagination) {
    // if we use server fetched data, we don't want to sort on the client, ever
    sorted.value = props.items;
    return;
  }
  sorted.value = props.items.toSorted((a, b) => {
    for (const field of Object.keys(sorter) as Array<keyof typeof sorter>) {
      if (sorter[field] === 0) continue;
      if (a[field] > b[field]) return sorter[field];
      if (a[field] < b[field]) return -sorter[field];
    }
    return 0;
  });
}

watch(
  () => props.items,
  () => {
    sort();
    page.value = props.serverPagination ? Math.floor(props.serverPagination.offset / props.serverPagination.limit) : Math.min(page.value, pageCount.value - 1);
  }
);

function checkReset() {
  const keys = Object.keys(sorter) as H[];
  if (keys.length >= props.maxSorters) {
    for (const k of keys) {
      delete sorter[k];
    }
  }
}

function click(header: Header<H>) {
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
  (e: "update:sort", col: keyof T, sorter: Record<string, number>): void;
}>();
function updatePage(newPage: number) {
  page.value = newPage;
  emit("update:page", newPage);
}

const slots = defineSlots<
  {
    [A in H]?: (_: { item: T }) => any;
  } & {
    empty(): any;
    "expanded-item"(props: { item: T; headers: Header<H>[] }): any;
    pagination(props: { page: number; pages: number; updatePage: (newPage: number) => void }): any;
  }
>();
</script>

<template>
  <Table class="w-full">
    <thead class="text-xs font-semibold text-gray">
      <tr>
        <th v-for="header in headers" :key="header.name" :style="header.width ? 'width: ' + header.width : ''" @click="click(header)">
          <div class="items-center inline-flex" :cursor="header.sortable ? 'pointer' : 'auto'">
            <span class="mr-2"> {{ header.title }}</span>
            <IconMdiSortAscending v-if="(sorter as Record<H, number>)[header.name] === 1" class="hidden md:block" />
            <IconMdiSortDescending v-else-if="(sorter as Record<H, number>)[header.name] === -1" class="hidden md:block" />
            <IconMdiSort v-else-if="header.sortable" class="text-gray-400 hidden md:block" />
          </div>
        </th>
      </tr>
    </thead>
    <tbody>
      <template v-for="(item, idx) in displayedItems" :key="idx">
        <tr>
          <td v-for="header in headers" :key="header.name" :style="header.width ? 'width: ' + header.width : ''" @click="expanded[idx] = !expanded[idx]">
            <template v-if="hasSlotContent(slots[header.name], { item })">
              <slot :name="header.name" :item="item" />
            </template>
            <template v-else>
              {{ item[header.name] }}
            </template>
          </td>
        </tr>

        <tr v-if="expandable && expanded[idx]" class="!border-dashed">
          <slot name="expanded-item" :item="item" :headers="headers" />
        </tr>
      </template>
      <tr v-if="!items || items?.length === 0">
        <td :colspan="headers.length">
          <slot name="empty" />
        </td>
      </tr>
    </tbody>
  </Table>
  <slot v-if="pageCount > 1 && !hidePagination" name="pagination" :page="page" :pages="pageCount" :update-page="updatePage">
    <div class="border-t border-gray-200 px-3 py-3 dark:border-gray-800">
      <PaginationButtons :page="page" :pages="pageCount" @update:page="updatePage" />
    </div>
  </slot>
</template>
