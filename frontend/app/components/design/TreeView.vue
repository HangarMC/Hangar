<script lang="ts" setup generic="T extends Record<string, any>">
const props = defineProps<{
  items?: T[];
  itemKey: string;
  clazz?: string;
  open: string[];
  nested?: boolean;
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

function hasChildren(item: T) {
  return "children" in item && item.children?.length;
}

const showCaretGutter = computed(() => props.nested || props.items?.some((item) => hasChildren(item)));

defineSlots<{
  item?: (props: { item: T }) => any;
}>();
</script>

<template>
  <div v-for="item in items" :key="item[itemKey]" :class="props.clazz">
    <div class="flex items-center gap-0.5">
      <button
        v-if="hasChildren(item)"
        type="button"
        class="flex-shrink-0 rounded p-0.5 text-gray-secondary transition-colors hover:(background-card text-black dark:text-white) focus-visible:(outline-2 outline-primary-500)"
        :aria-expanded="Boolean(expanded[item[itemKey]])"
        @click="expanded[item[itemKey]] = !expanded[item[itemKey]]"
      >
        <IconMdiChevronRight class="transition-transform" :class="expanded[item[itemKey]] ? 'rotate-90' : ''" />
      </button>
      <span v-else-if="showCaretGutter" class="w-5 flex-shrink-0" />
      <slot name="item" :item="item" />
    </div>
    <TreeView
      v-if="expanded[item[itemKey]] && hasChildren(item)"
      :key="item[itemKey]"
      :items="item.children"
      :item-key="itemKey"
      :open="open"
      nested
      clazz="ml-[10px] border-l border-gray-300 pl-[5px] dark:border-gray-700"
    >
      <template #item="slotProp">
        <slot name="item" :item="slotProp.item as T" />
      </template>
    </TreeView>
  </div>
</template>
