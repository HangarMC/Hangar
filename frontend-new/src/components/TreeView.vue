<script lang="ts" setup>
import { ref, watch } from "vue";

const props = defineProps<{
  items: Record<string, unknown>;
  itemKey: string;
  clazz?: string;
  open: string[];
}>();
const expanded = ref<Record<string, boolean>>({});
watch(
  props.open,
  (val) => {
    if (val) {
      for (let item of val) {
        expanded.value[item] = true;
      }
    }
  },
  { immediate: true }
);
</script>

<template>
  <div v-for="item in items" :key="item[itemKey]" :class="props.clazz">
    <IconMdiMenuDown
      v-if="item.children && item.children.length > 0"
      :class="'cursor-pointer transform transition-transform ' + (expanded[item[itemKey]] ? 'rotate-0' : '-rotate-90')"
      @click="expanded[item[itemKey]] = !expanded[item[itemKey]]"
    />
    <span v-else class="pl-4" />
    <slot name="item" :item="item"></slot>
    <TreeView
      v-if="expanded[item[itemKey]] && item.children && item.children.length > 0"
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
