<script lang="ts" setup>
import { hasSlotContent } from "~/composables/useSlot";
import Table from "~/components/design/Table.vue";
import { computed, reactive, ref } from "vue";

export interface Header {
  name: string;
  title: string;
  sortable?: boolean;
  width?: string;
}

const props = defineProps<{
  headers: Header[];
  items: any[];
  expandable?: boolean;
}>();

const expanded = ref<Record<number, boolean>>({});
const sorter = reactive<Record<string, number>>({});
const sorted = ref<any[]>(props.items);

function sort() {
  sorted.value = [...props.items].sort((a, b) => {
    for (let field of Object.keys(sorter)) {
      if (sorter[field] === 0) continue;
      if (a[field] > b[field]) return sorter[field];
      if (a[field] < b[field]) return -sorter[field];
    }
    return 0;
  });
}

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
  }
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
      <template v-for="(item, idx) in sorted" :key="idx">
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
      <tr v-if="!items || items.length === 0">
        <td :colspan="headers.length">
          <slot name="empty"></slot>
        </td>
      </tr>
    </tbody>
  </Table>
</template>
