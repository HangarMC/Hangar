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
    <InputText v-model.trim="newTagline" :label="i18n.t('author.taglineLabel')" counter :maxlength="useBackendData.validations.userTagline.max" />
    <Button class="mt-3" @click="save">{{ i18n.t("general.change") }}</Button>
    <template #activator="{ on }">
      <Button size="small" class="ml-2 inline-flex" v-on="on">
        <IconMdiPencil />
      </Button>
    </template>
  </Modal>
</template>
