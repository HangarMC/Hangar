<script lang="ts" setup>
import { TranslateResult, useI18n } from "vue-i18n";
import Markdown from "~/components/Markdown.vue";
import Button from "~/components/design/Button.vue";
import Modal from "~/components/modals/Modal.vue";

const props = defineProps<{
  title: string | TranslateResult;
  markdown: string;
}>();

const i18n = useI18n();
</script>

<template>
  <Modal :title="props.title">
    <template #default="{ on }">
      <Markdown :raw="markdown"></Markdown>

      <Button type="gray" class="mt-2" v-on="on">{{ i18n.t("general.close") }}</Button>
    </template>
    <template #activator="{ on }">
      <slot name="activator" :on="on"></slot>
    </template>
  </Modal>
</template>
