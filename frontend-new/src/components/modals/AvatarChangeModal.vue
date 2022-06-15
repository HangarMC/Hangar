<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import Button from "~/components/design/Button.vue";
import Modal from "~/components/modals/Modal.vue";
import { useInternalApi } from "~/composables/useApi";
import { handleRequestError } from "~/composables/useErrorHandling";
import { AxiosError } from "axios";
import Tooltip from "~/components/design/Tooltip.vue";
import { useContext } from "vite-ssr/vue";
import InputFile from "~/components/ui/InputFile.vue";
import { User } from "hangar-api";
import { ref } from "vue";
import { useRouter } from "vue-router";

const props = defineProps<{
  user: User;
}>();

const i18n = useI18n();
const ctx = useContext();
const router = useRouter();

const file = ref();

async function changeOrgAvatar() {
  if (!props.user.isOrganization) {
    return;
  }

  try {
    await useInternalApi(
      `organizations/org/${props.user.name}/settings/avatar`,
      true,
      "POST",
      { avatar: file.value },
      { "Content-Type": "multipart/form-data" }
    );
    router.go(0);
  } catch (e) {
    handleRequestError(e as AxiosError, ctx, i18n);
  }
}
</script>

<template>
  <Modal :title="i18n.t('author.org.editAvatar')">
    <template #default="{ on }">
      <InputFile v-model="file"></InputFile>

      <Button button-type="secondary" class="mt-2" @click="on.click">{{ i18n.t("general.close") }}</Button>
      <Button button-type="primary" class="mt-2 ml-2" @click="changeOrgAvatar()">{{ i18n.t("general.submit") }}</Button>
    </template>
    <template #activator="{ on }">
      <Tooltip class="absolute -bottom-3 -right-3">
        <template #content>
          {{ i18n.t("author.org.editAvatar") }}
        </template>
        <Button v-on="on"><IconMdiPencil /></Button>
      </Tooltip>
    </template>
  </Modal>
</template>
