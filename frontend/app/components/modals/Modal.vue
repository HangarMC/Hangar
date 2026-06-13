<script lang="ts" setup>
const props = withDefaults(
  defineProps<{
    title: string;
    windowClasses?: string;
    closeButtonRight?: boolean;
  }>(),
  {
    windowClasses: "",
    closeButtonRight: false,
  }
);

const isOpen = ref<boolean>(false);
const dialog = useTemplateRef("dialog");

function open() {
  isOpen.value = true;
  dialog.value?.showModal();
}

function close() {
  isOpen.value = false;
  dialog.value?.close();
}

function closeFromBackdrop(event: MouseEvent) {
  if (event.target === dialog.value) {
    close();
  }
}

const emit = defineEmits<{
  (e: "open"): void;
  (e: "close"): void;
}>();

watch(isOpen, (newVal) => {
  if (newVal) {
    if (!dialog.value?.open) {
      dialog.value?.showModal();
    }
    emit("open");
  } else {
    if (dialog.value?.open) {
      dialog.value?.close();
    }
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
  <dialog
    ref="dialog"
    class="background-default rounded max-w-10/12 >md:max-w-250 overflow-visible py-6 px-5 text-[#262626] dark:text-[#E0E6f0]"
    :class="windowClasses"
    :data-title="title"
    @cancel="close"
    @click="closeFromBackdrop"
    @close="close"
  >
    <div class="flex items-center w-full pb-4 text-xl">
      <div class="font-bold">{{ props.title }}</div>
      <Button
        data-close
        button-type="borderless"
        class="!h-8 !w-8 !p-0"
        :class="closeButtonRight ? 'ml-auto' : 'order-first mr-1'"
        aria-label="Close modal"
        @click="close"
      >
        <IconMdiClose />
      </Button>
    </div>
    <slot :on="{ click: close }" />
  </dialog>
  <slot name="activator" :on="{ click: open }" />
</template>

<style lang="scss" scoped>
dialog::backdrop {
  background: rgb(0 0 0 / 70%);
}
</style>
