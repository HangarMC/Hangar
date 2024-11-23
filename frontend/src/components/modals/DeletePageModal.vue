<script lang="ts" setup>
const emit = defineEmits<{
  (e: "delete"): void;
}>();

const i18n = useI18n();
</script>

<template>
  <Modal :title="i18n.t('page.delete.title')">
    <p>{{ i18n.t("page.delete.text") }}</p>
    <Button class="mt-3" @click="emit('delete')">{{ i18n.t("general.delete") }}</Button>
    <template #activator="{ on }">
      <slot name="activator" :on="on" />
    </template>
  </Modal>
</template>
