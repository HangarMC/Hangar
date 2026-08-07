<script lang="ts" setup>
const i18n = useI18n();

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
const dialog = useTemplateRef("dialog");

function open() {
  isOpen.value = true;
  dialog.value?.showModal();
}

function close() {
  isOpen.value = false;
  dialog.value?.close();
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
    class="max-w-[calc(100vw-2rem)] background-default rounded-md border border-gray-300 p-0 shadow-xl dark:border-gray-700 >md:max-w-250"
    :class="windowClasses"
    :data-title="title"
    @close="close"
  >
    <div class="flex flex-shrink-0 items-center gap-3 border-b border-gray-300 px-5 py-3 dark:border-gray-700">
      <h2 class="min-w-0 flex-1 truncate text-lg font-bold">{{ props.title }}</h2>
      <Button
        data-close
        variant="ghost"
        tone="neutral"
        size="sm"
        icon-only
        :title="i18n.t('general.close')"
        :aria-label="i18n.t('general.close')"
        @click="close"
      >
        <IconMdiClose />
      </Button>
    </div>

    <div class="max-h-[70vh] overflow-auto px-5 py-4">
      <slot :on="{ click: close }" />
    </div>

    <div
      v-if="hasSlotContent($slots.footer, { on: { click: close }, close })"
      class="flex flex-shrink-0 flex-wrap justify-end gap-2 border-t border-gray-300 px-5 py-3 dark:border-gray-700"
    >
      <slot name="footer" :on="{ click: close }" :close="close" />
    </div>
  </dialog>
  <slot name="activator" :on="{ click: open }" />
</template>

<style lang="scss" scoped>
dialog {
  color: inherit;
}

dialog[open] {
  display: flex;
  flex-direction: column;
}

dialog::backdrop {
  @apply bg-black/60;
  backdrop-filter: blur(2px);
}
</style>
