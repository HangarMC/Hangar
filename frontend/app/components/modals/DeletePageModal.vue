<script lang="ts" setup>
const emit = defineEmits<{
  (e: "delete"): void;
}>();

const i18n = useI18n();
</script>

<template>
  <Modal :title="i18n.t('page.delete.title')">
    <p>{{ i18n.t("page.delete.text") }}</p>
    <template #activator="{ on }">
      <slot name="activator" :on="on" />
    </template>
    <template #footer="{ on }">
      <Button variant="ghost" tone="neutral" v-on="on">{{ i18n.t("general.cancel") }}</Button>
      <Button tone="danger" @click="emit('delete')">{{ i18n.t("general.delete") }}</Button>
    </template>
  </Modal>
</template>
