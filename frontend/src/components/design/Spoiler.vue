<script setup lang="ts">
import { ref } from "#imports";

const props = withDefaults(
  defineProps<{
    withLine?: boolean;
    title?: string;
    open?: boolean;
  }>(),
  {
    withLine: true,
    title: undefined,
    open: false,
  }
);
const open = ref(props.open);
</script>

<template>
  <details :open="open" class="spoiler-details">
    <summary>
      <slot name="title">
        {{ title }}
      </slot>
    </summary>
    <hr v-if="withLine" class="mt-0.4rem py-1" />
    <slot name="content" />
  </details>
</template>

<style>
.spoiler-details {
  border: 1px solid #bbb;
  border-radius: 5px;
  padding: 0.4rem 0.4rem 0.4rem;
  max-width: 50%;
  min-width: 300px;
}

.spoiler-details:has(.error) {
  border-color: rgba(248, 113, 113); /* red-400 */
}

.spoiler-details summary {
  cursor: pointer;
  list-style: none;
  padding: 0.2rem;
}

.spoiler-details summary::-webkit-details-marker {
  display: none;
}
</style>
