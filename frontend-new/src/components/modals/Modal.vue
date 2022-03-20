<script lang="ts" setup>
import { ref } from "vue";
import { Dialog, DialogOverlay } from "@headlessui/vue";
import { TranslateResult } from "vue-i18n";

const props = defineProps<{
  title: string | TranslateResult;
}>();

const isOpen = ref<boolean>(false);

function open() {
  isOpen.value = true;
}

function close() {
  isOpen.value = false;
}
</script>

<template>
  <client-only>
    <Dialog :open="isOpen" class="fixed inset-0 z-10 overflow-y-auto" @close="close">
      <div class="flex items-center justify-center min-h-screen">
        <DialogOverlay class="fixed inset-0 bg-black opacity-30" />

        <div class="relative max-w-sm mx-auto bg-white rounded p-4">
          <h2 class="font-bold text-xl mb-2">{{ props.title }}</h2>
          <slot :on="{ click: close }"></slot>
        </div>
      </div>
    </Dialog>
  </client-only>
  <slot name="activator" :on="{ click: open }"></slot>
</template>
