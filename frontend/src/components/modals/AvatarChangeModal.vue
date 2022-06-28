<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import Button from "~/lib/components/design/Button.vue";
import Modal from "~/components/modals/Modal.vue";
import { useInternalApi } from "~/composables/useApi";
import { handleRequestError } from "~/composables/useErrorHandling";
import { AxiosError } from "axios";
import Tooltip from "~/lib/components/design/Tooltip.vue";
import { useContext } from "vite-ssr/vue";
import InputFile from "~/lib/components/ui/InputFile.vue";
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
    <InputFile v-model="file"></InputFile>

    <Button class="mt-3" @click="changeOrgAvatar()">{{ i18n.t("general.submit") }}</Button>
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
