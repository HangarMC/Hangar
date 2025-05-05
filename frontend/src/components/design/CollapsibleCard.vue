<script lang="ts" setup>
import { ref } from 'vue';
import Card from './Card.vue';

// Props passthrough
const props = withDefaults(
  defineProps<{
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
      <div class="flex items-center justify-between">
        <slot name="title" />
        <span
          class="cursor-pointer flex items-center justify-between"
          @click="toggle"
        >
        <span class="ml-1 text-sm text-gray-400 hover:bg-gray-700 p-0.5 rounded-full">
          <IconMdiChevronDown v-if="isOpen" class="text-xl text-white"/>
          <IconMdiChevronUp v-else class="text-xl text-white"/>
        </span>
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
