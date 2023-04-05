<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import { useSlots } from "vue";
import { hasSlotContent } from "~/composables/useSlot";

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
  <section class="not-last:mb-3">
    <div v-if="props.title || hasSlotContent(slots.header)" class="mb-2">
      <slot name="header">
        <h2 class="text-lg font-semibold">
          {{ i18n.t(props.title) }} <small v-if="props.optional">{{ i18n.t("project.settings.optional") }}</small>
        </h2>
        <p v-if="props.description">{{ i18n.t(props.description) }}</p>
      </slot>
    </div>
    <slot></slot>
  </section>
</template>
