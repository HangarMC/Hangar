<script lang="ts" setup generic="T extends Record<string, any>">
const props = defineProps<{
  items?: T[];
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
defineSlots<{
  item?: (props: { item: T }) => any;
}>();
</script>

<template>
  <div v-for="item in items" :key="item[itemKey]" :class="props.clazz">
    <div class="flex items-center">
      <div v-if="'children' in item && item.children?.length" class="inline-flex items-center">
        <IconMdiMenuDown
          :class="'absolute cursor-pointer transform transition-transform ' + (expanded[item[itemKey]] ? 'rotate-0' : '-rotate-90')"
          @click="expanded[item[itemKey]] = !expanded[item[itemKey]]"
        />
        <span class="pl-5" />
      </div>
      <span v-else class="pl-5" />
      <slot name="item" :item="item" />
    </div>
    <TreeView
      v-if="expanded[item[itemKey]] && 'children' in item && item.children?.length"
      :key="item[itemKey]"
      :items="item.children"
      :item-key="itemKey"
      :open="open"
      clazz="pl-4"
    >
      <template #item="slotProp">
        <slot name="item" :item="slotProp.item as T" />
      </template>
    </TreeView>
  </div>
</template>
