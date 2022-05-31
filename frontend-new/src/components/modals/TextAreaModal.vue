<script lang="ts" setup>
import { TranslateResult, useI18n } from "vue-i18n";
import Button from "~/components/design/Button.vue";
import Modal from "~/components/modals/Modal.vue";
import { ref } from "vue";
import InputTextarea from "~/components/ui/InputTextarea.vue";

const props = defineProps<{
  title: string | TranslateResult;
  label: string;
  submit: (msg: string) => Promise<void>;
}>();

const message = ref("");
const loading = ref(false);

const i18n = useI18n();

async function _submit(close: () => void) {
  loading.value = true;
  await props.submit(message.value);
  loading.value = false;
  close();
}
</script>

<template>
  <Modal :title="props.title">
    <template #default="{ on }">
      <InputTextarea v-model.trim="message" :label="label" :rows="2" @keydown.enter.prevent="" />

      <Button class="mt-2" :disabled="loading" @click="_submit(on.click)">{{ i18n.t("general.submit") }}</Button>
      <Button button-type="secondary" class="mt-2 ml-2" v-on="on">{{ i18n.t("general.close") }}</Button>
    </template>
    <template #activator="{ on }">
      <slot name="activator" :on="on"></slot>
    </template>
  </Modal>
</template>
