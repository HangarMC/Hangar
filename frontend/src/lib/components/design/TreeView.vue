<script lang="ts" setup>
import { ref, watch } from "vue";

type Item = Record<Exclude<string, "children">, string> & { children?: Item[] };

const props = defineProps<{
  items?: Item[];
  itemKey: string;
  clazz?: string;
  open: string[];
}>();
const expanded = ref<Record<string, boolean>>({});
watch(
  props.open,
  (val) => {
    if (val) {
      for (const item of val) {
        expanded.value[item] = true;
      }
    }
  },
  { immediate: true }
);
</script>

<template>
  <div v-for="item in items" :key="item[itemKey]" :class="props.clazz">
    <span class="flex">
      <IconMdiMenuDown
        v-if="'children' in item && item.children?.length"
        :class="'cursor-pointer transform transition-transform ' + (expanded[item[itemKey]] ? 'rotate-0' : '-rotate-90')"
        @click="expanded[item[itemKey]] = !expanded[item[itemKey]]"
      />
      <span v-else class="pl-4" />
      <slot name="item" :item="item"></slot>
    </span>
    <TreeView
      v-if="expanded[item[itemKey]] && 'children' in item && item.children?.length"
      :key="item[itemKey]"
      :items="item.children"
      :item-key="itemKey"
      :open="open"
      clazz="pl-2"
    >
      <template #item="inner">
        <slot name="item" :item="inner.item"></slot>
      </template>
    </TreeView>
  </div>
</template>
