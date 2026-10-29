<script lang="ts" setup>
const props = withDefaults(
  defineProps<{
    title: string;
    label: string;
    submit: (msg: string) => Promise<void> | undefined;
    requireInput?: boolean;
    submitTone?: "primary" | "danger";
    submitLabel?: string;
  }>(),
  { submitTone: "primary", submitLabel: undefined }
);

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
    <template #default>
      <InputTextarea v-model.trim="message" :label="label" :rows="2" @keydown.enter.prevent="" />
    </template>
    <template #activator="{ on }">
      <slot name="activator" :on="on" />
    </template>
    <template #footer="{ on }">
      <Button variant="ghost" tone="neutral" v-on="on">{{ i18n.t("general.cancel") }}</Button>
      <Button :tone="submitTone" :loading="loading" :disabled="requireInput && message.length === 0" @click="_submit(on.click)">
        {{ submitLabel || i18n.t("general.submit") }}
      </Button>
    </template>
  </Modal>
</template>
