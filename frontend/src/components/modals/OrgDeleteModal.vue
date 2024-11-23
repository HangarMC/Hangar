<script lang="ts" setup>
const props = defineProps<{
  organization: string;
}>();

const i18n = useI18n();
const router = useRouter();

const comment = ref<string>("");
const loading = ref<boolean>(false);

async function deleteOrg() {
  loading.value = true;
  await useInternalApi(`organizations/org/${props.organization}/delete`, "post", {
    content: comment.value,
  }).catch((err) => handleRequestError(err));
  await router.push("/");
  loading.value = false;
}
</script>

<template>
  <Modal :title="i18n.t('organization.settings.deleteModal.title', [organization])" window-classes="w-150">
    <template #default>
      <p class="mb-2">{{ i18n.t("organization.settings.deleteModal.description", [organization]) }}</p>
      <InputTextarea v-model.trim="comment" rows="2" :label="i18n.t('general.comment')" />
      <Button button-type="red" class="mt-3" :disabled="loading" @click="deleteOrg()">{{ i18n.t("general.delete") }}</Button>
    </template>
    <template #activator="{ on }">
      <Button button-type="red" size="small" class="mr-1" v-on="on"><IconMdiDeleteAlert /></Button>
    </template>
  </Modal>
</template>
