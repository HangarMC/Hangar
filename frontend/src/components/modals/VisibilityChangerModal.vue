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

const showTextarea = computed(() => setVisibility.value?.showModal && props.propVisibility !== visibility.value);
const setVisibility = computed(() => backendData.visibilities.find((v) => v.name === visibility.value));
const currentVisibility = computed(() => backendData.visibilities.find((v) => v.name === props.propVisibility));

async function submit(closeModal: () => void): Promise<void> {
  const result = await useInternalApi(props.postUrl, true, "post", {
    visibility: visibility.value,
    comment: setVisibility.value?.showModal ? reason.value : null,
  }).catch((e) => handleRequestError(e, ctx, i18n));
  reason.value = "";
  if (setVisibility.value && result) {
    notification.success(i18n.t("visibility.modal.success", [props.type, i18n.t(setVisibility.value?.title)]));
  }
  closeModal();
}
</script>

<template>
  <Modal :title="i18n.t('visibility.modal.title', [type])">
    <template #default="{ on }">
      Currently: {{ i18n.t(currentVisibility.title) }}
      <InputRadio v-for="vis in backendData.visibilities" :key="vis.name" v-model="visibility" :value="vis.name" :label="i18n.t(vis.title)" class="block" />

      <InputTextarea v-if="showTextarea" v-model.trim="reason" rows="2" :label="i18n.t('visibility.modal.reason')" class="pt-3" />

      <Button class="mt-3" @click="submit(on.click)">{{ i18n.t("general.submit") }}</Button>
    </template>
    <template #activator="{ on }">
      <Button v-bind="$attrs" class="mr-1" v-on="on">
        <IconMdiEye class="mr-1" />
        {{ i18n.t("visibility.modal.activatorBtn") }}
      </Button>
    </template>
  </Modal>
</template>
