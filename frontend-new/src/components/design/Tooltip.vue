<script lang="ts" setup>
import Popper from "vue3-popper";
import { defineComponent, onMounted, ref } from "vue";

const ServerOnly = defineComponent({
  name: "ServerOnly",
  setup(_, { slots }) {
    const show = ref(true);
    onMounted(() => {
      show.value = false;
    });
    return () => (show.value && slots.default ? slots.default() : null);
  },
});
</script>

<template>
  <ClientOnly>
    <Popper v-bind="$attrs" hover open-delay="200" close-delay="100">
      <slot />
      <template #content="props">
        <slot name="content" v-bind="props" />
      </template>
    </Popper>
  </ClientOnly>
  <ServerOnly>
    <slot />
  </ServerOnly>
</template>

<style scoped>
:deep(.popper) {
  background: #464646;
  padding: 0.5rem;
  border-radius: 0.375rem;
  color: #fff;
}
</style>
