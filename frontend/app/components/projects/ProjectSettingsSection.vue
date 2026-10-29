<script lang="ts" setup>
const props = withDefaults(
  defineProps<{
    title?: string;
    description?: string;
    optional?: boolean;
  }>(),
  {
    title: "",
    description: "",
    optional: false,
  }
);
const slots = useSlots();
const i18n = useI18n();
</script>

<template>
  <section class="settings-section">
    <div v-if="props.title || hasSlotContent(slots.header)" class="mb-3">
      <slot name="header">
        <h2 class="text-lg font-semibold">
          {{ i18n.t(props.title) }}
          <small v-if="props.optional" class="ml-1 text-sm font-normal text-gray-secondary">{{ i18n.t("project.settings.optional") }}</small>
        </h2>
        <p v-if="props.description" class="mt-0.5 text-sm text-gray-secondary">{{ i18n.t(props.description) }}</p>
      </slot>
    </div>
    <slot />
  </section>
</template>

<style scoped>
.settings-section:not(:last-child) {
  margin-bottom: 1.5rem;
  padding-bottom: 1.5rem;
  border-bottom: 1px solid var(--gray-300);
}

.dark .settings-section:not(:last-child) {
  border-bottom-color: var(--gray-700);
}
</style>
