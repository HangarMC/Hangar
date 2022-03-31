<script lang="ts" setup>
import { hasSlotContent } from "~/composables/useSlot";
import Table from "~/components/design/Table.vue";
import Button from "~/components/design/Button.vue";
import { ref } from "vue";

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
// TODO actually implement sorting
</script>

<template>
  <Table class="w-full">
    <thead>
      <tr>
        <th v-for="header in headers" :key="header.name" :style="header.width ? 'width: ' + header.width : ''">
          {{ header.title }}
        </th>
      </tr>
    </thead>
    <tbody>
      <template v-for="(item, idx) in items" :key="idx">
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

        <tr v-if="expanded[idx]" class="!border-dashed">
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
