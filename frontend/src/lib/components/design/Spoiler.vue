<script setup lang="ts">
const props = withDefaults(
  defineProps<{
    withLine?: boolean;
  }>(),
  {
    withLine: true,
  }
);
</script>

<template>
  <details open class="spoiler-details">
    <summary>
      <slot name="title" />
    </summary>
    <hr v-if="withLine" class="py-1" />
    <slot name="content" />
  </details>
</template>

<style>
.spoiler-details {
  border: 1px solid #bbb;
  border-radius: 5px;
  padding: 0.6rem 0.6rem 0.4rem;
  max-width: 50%;
  min-width: 300px;
}

.spoiler-details summary {
  cursor: pointer;
  list-style: none;
  padding: 0.25rem;
}
</style>
