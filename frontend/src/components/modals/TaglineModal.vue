<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import Button from "~/lib/components/design/Button.vue";
import Modal from "~/lib/components/modals/Modal.vue";
import { ref } from "vue";
import { useNotificationStore } from "~/store/notification";
import InputText from "~/lib/components/ui/InputText.vue";
import { useInternalApi } from "~/composables/useApi";
import { handleRequestError } from "~/composables/useErrorHandling";
import { useContext } from "vite-ssr/vue";
import { useRouter } from "vue-router";
import { useBackendDataStore } from "~/store/backendData";

const props = defineProps<{
  tagline?: string;
  action: string;
}>();

const newTagline = ref(props.tagline);

const ctx = useContext();
const router = useRouter();
const i18n = useI18n();
const backendData = useBackendDataStore();
const loading = ref(false);

async function save() {
  loading.value = true;
  try {
    await useInternalApi(props.action, true, "post", {
      content: newTagline.value,
    });
    router.go(0);
  } catch (e) {
    handleRequestError(e, ctx, i18n);
  }
  loading.value = false;
}
</script>

<template>
  <Modal :title="i18n.t('author.editTagline')" window-classes="w-200">
    <InputText v-model.trim="newTagline" :label="i18n.t('author.taglineLabel')" counter :maxlength="backendData.validations.userTagline.max" />
    <Button class="mt-3" @click="save">{{ i18n.t("general.change") }}</Button>
    <template #activator="{ on }">
      <Button size="small" class="ml-2 inline-flex" v-on="on">
        <IconMdiPencil />
      </Button>
    </template>
  </Modal>
</template>
