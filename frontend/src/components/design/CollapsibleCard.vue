<script lang="ts" setup>
import { ref } from 'vue';
import Card from './Card.vue';
import { Tag } from "~/types/backend";

// Props passthrough
const props = withDefaults(
  defineProps<{
    title: string;
    accent?: boolean;
    alternateBackground?: boolean;
    initiallyOpen?: boolean;
  }>(),
  {
    accent: false,
    alternateBackground: false,
    initiallyOpen: true,
  }
);

const isOpen = ref(props.initiallyOpen);

// Toggle handler
const toggle = () => {
  isOpen.value = !isOpen.value;
};

const hasSlotContent = (slot: any) => !!slot && slot().length > 0;
</script>

<template>
  <Card :accent="props.accent" :alternate-background="props.alternateBackground">
    <!-- Header Slot -->
    <template #header>
      <div
        class="cursor-pointer flex items-center justify-between"
        @click="toggle"
      >
        <slot name="header" /> {{ props.title }}
        <span class="ml-2 text-sm text-gray-400">
          <IconMdiChevronDown v-if="isOpen" class="text-xl text-white"/>
          <IconMdiChevronUp v-else class="text-xl text-white"/>
        </span>
      </div>
    </template>

    <!-- Default Content -->
    <template #default>
      <Transition name="collapse">
        <div v-show="isOpen">
          <slot />
        </div>
      </Transition>
    </template>

    <!-- Footer -->
    <template v-if="isOpen && hasSlotContent($slots.footer)" #footer>
      <slot name="footer" />
    </template>
  </Card>
</template>
