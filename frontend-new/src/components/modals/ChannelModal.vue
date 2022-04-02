<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import Button from "~/components/design/Button.vue";
import Modal from "~/components/modals/Modal.vue";
import { ProjectChannel } from "hangar-internal";

const props = defineProps<{
  projectId: number;
  edit?: boolean;
  channel?: ProjectChannel;
}>();
const emit = defineEmits<{
  (e: "create", channel: ProjectChannel): void;
}>();

const i18n = useI18n();
</script>

<template>
  <Modal :title="edit ? i18n.t('channel.modal.titleEdit') : i18n.t('channel.modal.titleNew')">
    <template #default="{ on }">
      <!-- todo implement channel modal -->

      <Button type="gray" class="mt-2" v-on="on">{{ i18n.t("general.close") }}</Button>
    </template>
    <template #activator="{ on }">
      <slot name="activator" :on="on"></slot>
    </template>
  </Modal>
</template>
