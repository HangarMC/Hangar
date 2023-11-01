<script lang="ts" setup>
import type { TranslateResult } from "vue-i18n";
import Markdown from "~/components/Markdown.vue";
import Modal from "~/components/modals/Modal.vue";

const props = defineProps<{
  title: string | TranslateResult;
  markdownInput: string;
}>();
</script>

<template>
  <Modal :title="title" window-classes="w-250">
    <Markdown :raw="markdownInput" />
    <template #activator="{ on }">
      <slot name="activator" :on="on"></slot>
    </template>
  </Modal>
</template>
