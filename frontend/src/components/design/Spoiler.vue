<script setup lang="ts">
const props = withDefaults(
  defineProps<{
    withLine?: boolean;
    title?: string;
    open?: boolean;
    alwaysOpen?: boolean;
  }>(),
  {
    withLine: true,
    title: undefined,
    open: false,
    alwaysOpen: false,
  }
);
const el = ref<HTMLDetailsElement>();
onMounted(() => {
  if (!el.value) return;
  if (props.alwaysOpen) {
    el.value.open = true;
  }
  el.value?.addEventListener("toggle", (event) => {
    if (!el.value) return;
    if (props.alwaysOpen) {
      el.value.open = true;
      event.preventDefault();
    } else {
      el.value.open = !el.value.open;
    }
  });
});
</script>

<template>
  <details ref="el" :open="open" class="spoiler-details">
    <summary :class="alwaysOpen && 'always-open'">
      <slot name="title">
        {{ title }}
      </slot>
    </summary>
    <hr v-if="withLine" class="mt-0.4rem py-1" />
    <slot name="content" />
  </details>
</template>

<style lang="scss">
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

  &.always-open {
    cursor: default;
  }
}

.spoiler-details summary::-webkit-details-marker {
  display: none;
}
</style>
