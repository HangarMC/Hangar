<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import Button from "~/lib/components/design/Button.vue";
import Modal from "~/lib/components/modals/Modal.vue";
import { useContext } from "vite-ssr/vue";
import { useRouter } from "vue-router";
import { ref } from "vue";
import InputTextarea from "~/lib/components/ui/InputTextarea.vue";
import { useInternalApi } from "~/composables/useApi";
import { handleRequestError } from "~/composables/useErrorHandling";

const props = defineProps<{
  organization: string;
}>();

const i18n = useI18n();
const ctx = useContext();
const router = useRouter();

const comment = ref<string>("");
const loading = ref<boolean>(false);

async function deleteOrg() {
  loading.value = true;
  await useInternalApi(`organizations/org/${props.organization}/delete`, true, "post", {
    content: comment.value,
  }).catch((e) => handleRequestError(e, ctx, i18n));
  await router.push("/");
  loading.value = false;
}
</script>

<template>
  <Modal :title="i18n.t('organization.settings.deleteModal.title', [organization])" window-classes="w-150">
    <template #default>
      <p class="mb-2">{{ i18n.t("organization.settings.deleteModal.description", [organization]) }}</p>
      <InputTextarea v-model.trim="comment" rows="2" :label="i18n.t('general.comment')" />
      <Button button-type="red" class="mt-3" :disabled="loading || comment.length < 7" @click="deleteOrg()">{{ i18n.t("general.delete") }}</Button>
    </template>
    <template #activator="{ on }">
      <Button button-type="red" size="small" class="mr-1" v-on="on"><IconMdiDeleteAlert /></Button>
    </template>
  </Modal>
</template>
