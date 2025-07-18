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
const isOpen = ref(props.open);
if (props.alwaysOpen) {
  isOpen.value = true;
}

function click() {
  if (props.alwaysOpen) return;
  isOpen.value = !isOpen.value;
}
</script>

<template>
  <div class="spoiler-details">
    <component :is="alwaysOpen ? 'div' : 'button'" class="spoiler-details__title" @click="click">
      <slot name="title">
        {{ title }}
      </slot>
    </component>
    <div v-show="isOpen" :aria-hidden="isOpen">
      <hr v-if="withLine" class="mt-0.4rem py-1" />
      <slot name="content" />
    </div>
  </div>
</template>

<style lang="scss">
.spoiler-details {
  border: 1px solid #bbb;
  border-radius: 5px;
  padding: 0.4rem;
  max-width: 50%;
  min-width: 300px;

  &__title {
    width: 100%;
    text-align: start;
  }

  &:has(.error) {
    border-color: rgba(248, 113, 113); /* red-400 */
  }
}
</style>
