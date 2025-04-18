<script lang="ts" setup>
defineProps<{
  title: string;
  markdownInput: string;
}>();
</script>

<template>
  <Modal :title="title" window-classes="w-250">
    <Markdown :raw="markdownInput" />
    <template #activator="{ on }">
      <slot name="activator" :on="on" />
    </template>
  </Modal>
</template>
