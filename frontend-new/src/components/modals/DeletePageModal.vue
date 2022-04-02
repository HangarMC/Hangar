<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import Button from "~/components/design/Button.vue";
import Modal from "~/components/modals/Modal.vue";

const emit = defineEmits<{
  (e: "delete"): void;
}>();

const i18n = useI18n();
</script>

<template>
  <Modal :title="i18n.t('page.delete.title')">
    <template #default="{ on }">
      <p>{{ i18n.t("page.delete.text") }}</p>
      <Button class="mt-2" v-on="on">{{ i18n.t("general.close") }}</Button>
      <Button class="mt-2 ml-2" @click="emit('delete')">{{ i18n.t("general.delete") }}</Button>
    </template>
    <template #activator="{ on }">
      <slot name="activator" :on="on"></slot>
    </template>
  </Modal>
</template>
