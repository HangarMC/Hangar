<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import { useRouter } from "vue-router";
import { ref } from "vue";
import type { HangarProject } from "hangar-internal";
import Button from "~/components/design/Button.vue";
import Modal from "~/components/modals/Modal.vue";
import Tooltip from "~/components/design/Tooltip.vue";
import InputRadio from "~/components/ui/InputRadio.vue";
import { useBackendData } from "~/store/backendData";
import InputTextarea from "~/components/ui/InputTextarea.vue";
import { required } from "~/composables/useValidationHelpers";
import { useInternalApi } from "~/composables/useApi";
import { handleRequestError } from "~/composables/useErrorHandling";
import { useNotificationStore } from "~/store/notification";

const props = defineProps<{
  project: HangarProject;
  openReport: boolean;
  disabled?: boolean;
}>();

const emit = defineEmits<{
  (e: "reported"): void;
}>();

const i18n = useI18n();
const router = useRouter();

const flagReason = ref<string>();
const flagComment = ref<string>();

async function submit(close: () => void) {
  try {
    await useInternalApi("flags/", "POST", {
      projectId: props.project.id,
      reason: flagReason.value,
      comment: flagComment.value,
    });
    emit("reported");
    close();
    useNotificationStore().success(i18n.t("project.flag.flagSend"));
  } catch (e) {
    handleRequestError(e);
  }
}
</script>

<template>
  <Modal :title="i18n.t('project.flag.flagProject', [project.name])" window-classes="w-150">
    <template #default="{ on }">
      <InputRadio v-for="(reason, index) in useBackendData.flagReasons" :key="index" v-model="flagReason" :label="i18n.t(reason.title)" :value="reason.type" />
      <div class="py-2" />
      <InputTextarea v-model.trim="flagComment" rows="3" :rules="[required()]" :label="i18n.t('general.comment')" />

      <Button class="mt-3" :disabled="!flagReason || !flagComment" @click="submit(on.click)">{{ i18n.t("general.submit") }}</Button>
    </template>
    <template #activator="{ on }">
      <Tooltip>
        <template #content>
          <span v-if="openReport">
            {{ i18n.t("project.actions.openReport") }}
          </span>
          <span v-else>
            {{ i18n.t("project.actions.flag") }}
          </span>
        </template>
        <Button button-type="secondary" size="small" :disabled="openReport || disabled" v-on="on">
          <IconMdiFlag />
          <span class="w-0 overflow-hidden !m-0">0</span>
        </Button>
      </Tooltip>
    </template>
  </Modal>
</template>
