<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import Button from "~/components/design/Button.vue";
import Modal from "~/components/modals/Modal.vue";
import { Visibility } from "~/types/enums";
import InputRadio from "~/components/ui/InputRadio.vue";
import { useBackendDataStore } from "~/store/backendData";
import { computed, ref } from "vue";
import InputTextarea from "~/components/ui/InputTextarea.vue";
import { useInternalApi } from "~/composables/useApi";
import { handleRequestError } from "~/composables/useErrorHandling";
import { useContext } from "vite-ssr/vue";
import { useNotificationStore } from "~/store/notification";

const props = defineProps<{
  type: "project" | "version";
  propVisibility: Visibility;
  postUrl: string;
}>();

const i18n = useI18n();
const ctx = useContext();
const backendData = useBackendDataStore();
const notification = useNotificationStore();

const visibility = ref<Visibility>();
const reason = ref<string>("");

const showTextarea = computed(() => currentIVis.value?.showModal && props.propVisibility !== visibility.value);
const currentIVis = computed(() => backendData.visibilities.find((v) => v.name === visibility.value));

async function submit(closeModal: () => void): Promise<void> {
  const result = await useInternalApi(props.postUrl, true, "post", {
    visibility: visibility.value,
    comment: currentIVis.value?.showModal ? reason.value : null,
  }).catch((e) => handleRequestError(e, ctx, i18n));
  reason.value = "";
  if (currentIVis.value && result) {
    notification.success(i18n.t("visibility.modal.success", [props.type, i18n.t(currentIVis.value?.title)]));
  }
  closeModal();
}
</script>

<template>
  <Modal :title="i18n.t('visibility.modal.title', [type])">
    <template #default="{ on }">
      Currently {{ propVisibility }}
      <InputRadio v-for="vis in backendData.visibilities" :key="vis.name" v-model="visibility" :value="vis.name" :label="i18n.t(vis.title)" class="block" />

      <InputTextarea v-if="showTextarea" v-model.trim="reason" rows="2" :label="i18n.t('visibility.modal.reason')" />

      <Button class="mt-2" @click="submit(on.click)">{{ i18n.t("general.submit") }}</Button>
      <Button button-type="secondary" class="mt-2 ml-2" v-on="on">{{ i18n.t("general.close") }}</Button>
    </template>
    <template #activator="{ on }">
      <Button v-bind="$attrs" class="mr-1" v-on="on">
        <IconMdiEye class="mr-1" />
        {{ i18n.t("visibility.modal.activatorBtn") }}
      </Button>
    </template>
  </Modal>
</template>