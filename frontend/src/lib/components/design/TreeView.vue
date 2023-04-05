<script lang="ts" setup>
import { ref, useSlots, watch } from "vue";

type Item = Record<Exclude<string, "children">, string> & { children?: Record<string, string>[] };

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

// hack to fix vue-tsc from complaining
const itemSlot = useSlots().item;
function FakeSlot(props: { item: Item }) {
  return itemSlot?.({ item: props.item });
}
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
      <FakeSlot :item="item"></FakeSlot>
    </span>
    <TreeView
      v-if="expanded[item[itemKey]] && 'children' in item && item.children?.length"
      :key="item[itemKey]"
      :items="item.children"
      :item-key="itemKey"
      :open="open"
      clazz="pl-2"
    >
      <template #item="slotProp">
        <FakeSlot :item="slotProp.item"></FakeSlot>
      </template>
    </TreeView>
    <!-- to trick IJ that the slot exists -->
    <slot v-if="false" name="item" :item="item"></slot>
  </div>
</template>
