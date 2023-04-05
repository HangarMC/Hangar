<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import { computed, ref } from "vue";
import { useRouter } from "vue-router";
import Button from "~/components/design/Button.vue";
import Modal from "~/components/modals/Modal.vue";
import { Visibility } from "~/types/enums";
import InputRadio from "~/components/ui/InputRadio.vue";
import { useBackendData } from "~/store/backendData";
import InputTextarea from "~/components/ui/InputTextarea.vue";
import { useInternalApi } from "~/composables/useApi";
import { handleRequestError } from "~/composables/useErrorHandling";
import { useNotificationStore } from "~/store/notification";

const props = defineProps<{
  type: "project" | "version";
  propVisibility: Visibility;
  postUrl: string;
}>();

const i18n = useI18n();
const notification = useNotificationStore();
const router = useRouter();

const visibilities = computed(() => useBackendData.visibilities.filter((v) => v.canChangeTo));
const visibility = ref<Visibility>();
const reason = ref<string>("");

const showTextarea = computed(() => setVisibility.value?.showModal && props.propVisibility !== visibility.value);
const setVisibility = computed(() => useBackendData.visibilities.find((v) => v.name === visibility.value));
const currentVisibility = computed(() => useBackendData.visibilities.find((v) => v.name === props.propVisibility));

async function submit(): Promise<void> {
  await useInternalApi(props.postUrl, "post", {
    visibility: visibility.value,
    comment: setVisibility.value?.showModal ? reason.value : null,
  }).catch((e) => handleRequestError(e));
  reason.value = "";
  if (setVisibility.value) {
    notification.success(i18n.t("visibility.modal.success", [props.type, i18n.t(setVisibility.value?.title)]));
  }

  if (visibility.value === Visibility.SOFT_DELETE) {
    await router.push("/");
  } else {
    router.go(0);
  }
}
</script>

<template>
  <Modal :title="i18n.t('visibility.modal.title', [type])" window-classes="w-150">
    <template #default="{ on }">
      Currently: {{ i18n.t(currentVisibility.title) }}
      <InputRadio v-for="vis in visibilities" :key="vis.name" v-model="visibility" :value="vis.name" :label="i18n.t(vis.title)" class="block" />

      <div v-if="showTextarea">
        <InputTextarea v-model.trim="reason" rows="2" :label="i18n.t('visibility.modal.reason')" class="pt-3" />
        <span class="text-sm">This will send the project owner a notification with the provided reason!</span>
        <br />
      </div>

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
