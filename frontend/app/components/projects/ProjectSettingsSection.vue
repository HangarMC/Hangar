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
  <Card class="not-last:mb-4">
    <div v-if="props.title || hasSlotContent(slots.header)" class="mb-3">
      <slot name="header">
        <h2 class="text-xl font-bold">
          {{ i18n.t(props.title) }} <small v-if="props.optional">{{ i18n.t("project.settings.optional") }}</small>
        </h2>
        <p v-if="props.description" class="mt-1 text-sm text-gray">{{ i18n.t(props.description) }}</p>
      </slot>
    </div>
    <slot />
  </Card>
</template>
