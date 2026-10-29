<script lang="ts" setup>
const props = defineProps<{
  tagline?: string;
  action: string;
}>();

const newTagline = ref(props.tagline);

const router = useRouter();
const i18n = useI18n();
const loading = ref(false);

async function save() {
  loading.value = true;
  try {
    await useInternalApi(props.action, "post", {
      content: newTagline.value,
    });
    router.go(0);
  } catch (err) {
    handleRequestError(err);
  }
  loading.value = false;
}
</script>

<template>
  <Modal :title="i18n.t('author.editTagline')" window-classes="w-200">
    <template #activator="{ on }">
      <slot name="activator" :on="on">
        <Button variant="ghost" tone="neutral" size="sm" icon-only class="ml-2" :title="i18n.t('general.edit')" :aria-label="i18n.t('general.edit')" v-on="on">
          <IconMdiPencil />
        </Button>
      </slot>
    </template>
    <InputText v-model.trim="newTagline" :label="i18n.t('author.taglineLabel')" counter :maxlength="useBackendData.validations.userTagline.max" />
    <template #footer="{ on }">
      <Button variant="ghost" tone="neutral" v-on="on">{{ i18n.t("general.cancel") }}</Button>
      <Button @click="save">{{ i18n.t("general.change") }}</Button>
    </template>
  </Modal>
</template>
