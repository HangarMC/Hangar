<script lang="ts" setup>
import { PropType } from "vue";
import { hasSlotContent } from "~/composables/useSlot";
import Table from "~/components/design/Table.vue";

export interface Header {
  name: string;
  title: string;
  sortable?: boolean;
}

const props = defineProps({
  headers: {
    type: Array as PropType<Header[]>,
    required: true,
  },
  items: {
    type: Array,
    required: true,
  },
});
// TODO actually implement sorting
</script>

<template>
  <Table class="w-full">
    <thead>
      <tr>
        <th v-for="header in headers" :key="header.name">{{ header.title }}</th>
      </tr>
    </thead>
    <tbody>
      <tr v-for="item in items" :key="item">
        <td v-for="header in headers" :key="header.name">
          <template v-if="hasSlotContent($slots['item_' + header.name], { item: item })">
            <slot :name="'item_' + header.name" :item="item"></slot>
          </template>
          <template v-else>
            {{ item[header.name] }}
          </template>
        </td>
      </tr>
    </tbody>
  </Table>
</template>
