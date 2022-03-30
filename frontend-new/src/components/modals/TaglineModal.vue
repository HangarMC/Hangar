<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import Button from "~/components/design/Button.vue";
import Modal from "~/components/modals/Modal.vue";
import { ref } from "vue";
import { useNotificationStore } from "~/store/notification";
import InputText from "~/components/ui/InputText.vue";
import { useInternalApi } from "~/composables/useApi";
import { handleRequestError } from "~/composables/useErrorHandling";
import { useContext } from "vite-ssr/vue";
import { useRouter } from "vue-router";

const props = defineProps<{
  tagline: string;
  action: string;
}>();

const newTagline = ref(props.tagline);

const ctx = useContext();
const router = useRouter();
const i18n = useI18n();
const notification = useNotificationStore();

async function save() {
  try {
    await useInternalApi(props.action, true, "post", {
      content: newTagline.value,
    });
    router.go(0);
  } catch (e) {
    handleRequestError(e, ctx, i18n);
  }
}
</script>

<template>
  <Modal :title="i18n.t('author.editTagline')">
    <template #default="{ on }">
      <!-- todo count exiting chars -->
      <InputText v-model.trim="newTagline" :label="i18n.t('author.taglineLabel')" />

      <Button size="medium" class="mt-2" v-on="on">{{ i18n.t("general.close") }}</Button>
      <Button size="medium" class="mt-2 ml-2" @click="newTagline = tagline">{{ i18n.t("general.reset") }}</Button>
      <Button size="medium" class="mt-2 ml-2" @click="save">{{ i18n.t("general.change") }}</Button>
    </template>
    <template #activator="{ on }">
      <Button size="small" class="ml-2 inline-flex" v-on="on">
        <IconMdiPencil />
      </Button>
    </template>
  </Modal>
</template>
