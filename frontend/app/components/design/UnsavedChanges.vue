<script lang="ts" setup>
withDefaults(defineProps<{ show: boolean; loading?: boolean; disabled?: boolean }>(), {
  loading: false,
  disabled: false,
});

defineEmits<{
  (e: "save"): void;
  (e: "discard"): void;
}>();

const i18n = useI18n();
</script>

<template>
  <Transition name="unsaved">
    <div v-if="show" class="pointer-events-none fixed inset-x-0 bottom-0 z-30 flex justify-center p-4">
      <div class="unsaved-bar pointer-events-auto max-w-3xl w-full flex flex-wrap items-center gap-x-4 gap-y-2 rounded-lg px-4 py-2.5">
        <span class="flex items-center gap-2 text-sm font-medium">
          <span v-if="loading" class="h-4.5 w-4.5 flex-shrink-0"><Spinner class="stroke-gray-400" /></span>
          <IconMdiCircleMedium v-else class="flex-shrink-0 color-primary" />
          {{ i18n.t(loading ? "general.saving" : "general.unsavedChanges") }}
        </span>
        <div class="ml-auto flex flex-shrink-0 gap-2">
          <Button variant="ghost" tone="neutral" size="sm" :disabled="loading" @click="$emit('discard')">
            {{ i18n.t("general.discard") }}
          </Button>
          <Button size="sm" :disabled="disabled" :loading="loading" @click="$emit('save')">
            <IconMdiCheck />
            {{ i18n.t("general.save") }}
          </Button>
        </div>
      </div>
    </div>
  </Transition>
</template>

<style scoped>
.unsaved-bar {
  border: 1px solid var(--gray-300);
  background-color: #f9fafb;
  box-shadow:
    0 2px 4px rgb(0 0 0 / 0.06),
    0 12px 28px -8px rgb(0 0 0 / 0.18);
}

.dark .unsaved-bar {
  border-color: var(--gray-700);
  background-color: var(--gray-800);
  box-shadow:
    0 2px 4px rgb(0 0 0 / 0.4),
    0 12px 28px -8px rgb(0 0 0 / 0.6);
}

.unsaved-enter-active,
.unsaved-leave-active {
  transition:
    opacity 0.18s ease,
    transform 0.18s ease;
}

.unsaved-enter-from,
.unsaved-leave-to {
  opacity: 0;
  transform: translateY(0.75rem);
}
</style>
