<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import { ref } from "vue";
import Button from "~/components/design/Button.vue";
import Modal from "~/components/modals/Modal.vue";
import InputTextarea from "~/components/ui/InputTextarea.vue";

const props = defineProps<{
  title: string;
  label: string;
  submit: (msg: string) => Promise<void> | undefined;
  requireInput?: boolean;
}>();

const message = ref("");
const loading = ref(false);

const i18n = useI18n();

async function _submit(close: () => void) {
  loading.value = true;
  await props?.submit(message.value);
  loading.value = false;
  close();
}
</script>

<template>
  <Modal :title="props.title" window-classes="w-150">
    <template #default="{ on }">
      <InputTextarea v-model.trim="message" :label="label" :rows="2" @keydown.enter.prevent="" />
      <Button class="mt-3" :disabled="loading || (requireInput && message.length === 0)" @click="_submit(on.click)">{{ i18n.t("general.submit") }}</Button>
    </template>
    <template #activator="{ on }">
      <slot name="activator" :on="on"></slot>
    </template>
  </Modal>
</template>
