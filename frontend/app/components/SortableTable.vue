<script lang="ts" setup generic="T extends Record<string, any>, H extends string">
import type { Ref } from "vue";
import type { Header } from "#shared/types/components/SortableTable";
import { Pagination as PaginationComponent } from "#components";
import type { Pagination } from "#shared/types/backend";

const props = withDefaults(
  defineProps<{
    headers: Header<H>[];
    items: T[];
    expandable?: boolean;
    serverPagination?: Pagination;
    initialSorter?: Partial<Record<H, number>>;
    maxSorters?: number;
  }>(),
  {
    maxSorters: 1,
    serverPagination: undefined,
    initialSorter: undefined,
  }
);

const expanded = ref<Record<number, boolean>>({});
const sorter = reactive({ ...props.initialSorter } as Record<H, number>) as Record<H, number>;
const sorted = ref<T[]>(props.items) as Ref<T[]>; // idk why I need a cast here...

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

watch(() => props.items, sort);

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
  emit("update:page", newPage);
}

const slots = defineSlots<
  {
    [A in H]?: (_: { item: T }) => any;
  } & {
    empty(): any;
    "expanded-item"(props: { item: T; headers: Header<H>[] }): any;
  }
>();
</script>

<template>
  <Table class="w-full">
    <thead>
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
      <PaginationComponent v-if="sorted?.length !== 0" :items="sorted" :server-pagination="serverPagination" @update:page="updatePage">
        <template #default="{ item, idx }">
          <tr>
            <td v-for="header in headers" :key="header.name" :style="header.width ? 'width: ' + header.width : ''" @click="expanded[idx] = !expanded[idx]">
              <template v-if="hasSlotContent(slots[header.name], { item: item })">
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
          <slot name="empty" />
        </td>
      </tr>
    </tbody>
  </Table>
</template>
