<template>
  <span class="coming-soon" title="Coming soon">
    <render />
  </span>
</template>

<script lang="ts" setup>
import type { VNode } from "vue";
import { computed, useSlots } from "vue";

const props = defineProps<{
  short?: boolean;
}>();

const slot = useSlots();

const content = computed(() => {
  const slotContent = slot?.default?.();
  if (!slotContent) return;
  for (const vnode of slotContent) {
    replace(vnode);
  }
  return slotContent;
});

function replace(vnode: VNode) {
  if (vnode.type.toString() === "Symbol(Text)" || vnode.type.toString() === "Symbol()") {
    vnode.children = props.short ? "[R]" : "[REDACTED]";
  }
}

const render = () => {
  return content.value;
};
</script>

<style lang="scss">
.coming-soon {
  --blur-color: #6695f2;
  color: transparent;
  display: inherit;
  text-shadow: 0 0 12px var(--blur-color);

  svg {
    color: var(--blur-color);
    filter: blur(4px);
  }
}

.text-right .coming-soon {
  float: right;
}
</style>
