<script lang="ts" setup generic="T extends Record<string, any>">
import { reactive, ref, watch } from "vue";
import { Pagination } from "hangar-api";
import { hasSlotContent } from "~/lib/composables/useSlot";
import Table from "~/lib/components/design/Table.vue";
import PaginationButtons from "~/lib/components/design/PaginationButtons.vue";
import PaginationComponent from "~/lib/components/design/Pagination.vue";

type T = Record<string, any>; // remove when https://github.com/vuejs/rfcs/discussions/436 lands or when using volar

export interface Header {
  name: string;
  title: string;
  sortable?: boolean;
  width?: string;
}

const props = defineProps<{
  headers: Header[];
  items: T[];
  expandable?: boolean;
  serverPagination?: Pagination;
}>();

const expanded = ref<Record<number, boolean>>({});
const sorter = reactive<Record<string, number>>({});
const sorted = ref<T[]>(props.items);

const page = ref(0);

function sort() {
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

function click(header: Header) {
  if (header.sortable) {
    if (sorter[header.name] === 1) {
      sorter[header.name] = -1;
    } else if (sorter[header.name] === -1) {
      sorter[header.name] = 0;
    } else {
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
