<script lang="ts" setup>
import { ref, watch } from "vue";
import { Dialog, DialogOverlay } from "@headlessui/vue";

const props = withDefaults(
  defineProps<{
    title: string;
    windowClasses?: string;
  }>(),
  {
    windowClasses: "",
  }
);

const isOpen = ref<boolean>(false);

function open() {
  isOpen.value = true;
}

function close() {
  isOpen.value = false;
}

const emit = defineEmits<{
  (e: "open"): void;
  (e: "close"): void;
}>();

watch(isOpen, (newVal) => {
  if (newVal) {
    emit("open");
  } else {
    emit("close");
  }
});

defineExpose({
  open,
  close,
  isOpen,
});
</script>

<template>
  <client-only>
    <Dialog :open="isOpen" class="fixed inset-0 z-10 overflow-y-auto" @close="close">
      <div class="flex items-center justify-center min-h-screen">
        <DialogOverlay class="fixed inset-0 bg-black opacity-60" />

        <div class="relative mx-auto background-default rounded max-w-10/12 >md:max-w-250 py-6 px-5" :class="windowClasses">
          <div class="inline-flex items-center w-full pb-4 pr-1 text-xl">
            <IconMdiClose class="cursor-pointer mr-1" @click="close" />
            <h2 class="font-bold">{{ props.title }}</h2>
          </div>
          <slot :on="{ click: close }"></slot>
        </div>
      </div>
    </Dialog>
  </client-only>
  <slot name="activator" :on="{ click: open }"></slot>
</template>
