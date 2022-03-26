<script lang="ts" setup>
const props = defineProps<{
  items: Record<string, unknown>;
  itemKey: string;
  clazz?: string;
}>();

// TODO make collapsable
</script>

<template>
  <div v-for="item in items" :key="item[itemKey]" :class="props.clazz">
    <slot name="item" :item="item"></slot>
    <TreeView v-if="item.children && item.children.length > 0" :key="item[itemKey]" :items="item.children" :item-key="itemKey" clazz="pl-2">
      <template #item="inner">
        <slot name="item" :item="inner.item"></slot>
      </template>
    </TreeView>
  </div>
</template>
