<script lang="ts" setup>
import { ref } from "vue";
import { Dialog, DialogOverlay } from "@headlessui/vue";
import { TranslateResult } from "vue-i18n";

const props = withDefaults(
  defineProps<{
    title: string | TranslateResult;
    small?: boolean;
    big?: boolean;
  }>(),
  {
    small: true,
    big: false,
  }
);

const isOpen = ref<boolean>(false);

function open() {
  isOpen.value = true;
}

function close() {
  isOpen.value = false;
}

defineExpose({
  open: open,
  close: close,
  isOpen: isOpen,
});
</script>

<template>
  <client-only>
    <Dialog :open="isOpen" class="fixed inset-0 z-10 overflow-y-auto" @close="close">
      <div class="flex items-center justify-center min-h-screen">
        <DialogOverlay class="fixed inset-0 bg-black opacity-30" />

        <div class="relative mx-auto background-default rounded p-4" :class="big ? 'max-w-80vw' : small ? 'max-w-sm' : 'max-w-lg'">
          <h2 class="font-bold text-xl mb-2">{{ props.title }}</h2>
          <slot :on="{ click: close }"></slot>
        </div>
      </div>
    </Dialog>
  </client-only>
  <slot name="activator" :on="{ click: open }"></slot>
</template>
