<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import Button from "~/lib/components/design/Button.vue";
import Modal from "~/lib/components/modals/Modal.vue";
import Tooltip from "~/lib/components/design/Tooltip.vue";
import { useContext } from "vite-ssr/vue";
import { useRouter } from "vue-router";
import InputRadio from "~/lib/components/ui/InputRadio.vue";
import { useBackendDataStore } from "~/store/backendData";
import { ref } from "vue";
import InputTextarea from "~/lib/components/ui/InputTextarea.vue";
import { required } from "~/lib/composables/useValidationHelpers";
import { useInternalApi } from "~/composables/useApi";
import { HangarProject } from "hangar-internal";
import { handleRequestError } from "~/composables/useErrorHandling";
import { AxiosError } from "axios";
import { useNotificationStore } from "~/store/notification";

const props = defineProps<{
  project: HangarProject;
  disabled?: boolean;
}>();

const i18n = useI18n();
const ctx = useContext();
const router = useRouter();
const backendData = useBackendDataStore();

const flagReason = ref<string>();
const flagComment = ref<string>();

async function submit(close: () => void) {
  try {
    await useInternalApi("flags/", true, "POST", {
      projectId: props.project.id,
      reason: flagReason.value,
      comment: flagComment.value,
    });
    close();
    useNotificationStore().success(i18n.t("project.flag.flagSend"));
    await router.go(0);
  } catch (e) {
    handleRequestError(e as AxiosError, ctx, i18n);
  }
}
</script>

<template>
  <Modal :title="i18n.t('project.flag.flagProject', [project.name])" window-classes="w-150">
    <template #default="{ on }">
      <InputRadio v-for="(reason, index) in backendData.flagReasons" :key="index" v-model="flagReason" :label="i18n.t(reason.title)" :value="reason.type" />
      <div class="py-2"></div>
      <InputTextarea v-model.trim="flagComment" rows="3" :rules="[required()]" :label="i18n.t('general.comment')" />

      <Button class="mt-3" @click="submit(on.click)">{{ i18n.t("general.submit") }}</Button>
    </template>
    <template #activator="{ on }">
      <Tooltip>
        <template #content> {{ i18n.t("project.actions.flag") }} </template>
        <Button button-type="secondary" size="small" :disabled="project.userActions.flagged || disabled" v-on="on">
          <IconMdiFlag />
          <span class="w-0 overflow-hidden !m-0">0</span>
        </Button>
      </Tooltip>
    </template>
  </Modal>
</template>
