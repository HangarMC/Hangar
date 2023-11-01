<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import { ref } from "vue";
import { useRouter } from "vue-router";
import type { Flag } from "hangar-internal";
import Button from "~/components/design/Button.vue";
import Modal from "~/components/modals/Modal.vue";
import InputTextarea from "~/components/ui/InputTextarea.vue";
import { useInternalApi } from "~/composables/useApi";
import { handleRequestError } from "~/composables/useErrorHandling";
import { useNotificationStore } from "~/store/notification";
import InputCheckbox from "~/components/ui/InputCheckbox.vue";

const props = defineProps<{
  flag: Flag;
  sendToReporter: boolean;
}>();

const i18n = useI18n();
const notification = useNotificationStore();
const router = useRouter();

const content = ref<string>("");
const warning = ref<boolean>(false);

async function submit() {
  await useInternalApi(`flags/${props.flag.id}/notify`, "post", {
    warning: warning.value,
    toReporter: props.sendToReporter,
    content: content.value,
  }).catch((e) => handleRequestError(e));
  content.value = "";
  router.go(0);
}
</script>

<template>
  <Modal
    :title="
      sendToReporter
        ? i18n.t('flagReview.notification.reporterTitle', [flag.reportedByName])
        : i18n.t('flagReview.notification.projectTitle', [flag.projectNamespace.slug])
    "
    window-classes="w-150"
  >
    <template #default="{ on }">
      <span v-if="!sendToReporter">Note that changing the visibility already sends a notification to the project's members.</span>
      <InputTextarea v-model.trim="content" rows="2" :label="i18n.t('flagReview.notification.prompt')" class="pb-2" />
      <InputCheckbox v-model="warning" :label="i18n.t('flagReview.notification.asWarning')" />
      <Button class="mt-3" :disabled="content.length === 0" @click="submit(on.click)">{{ i18n.t("general.submit") }}</Button>
    </template>
    <template #activator="{ on }">
      <Button v-bind="$attrs" class="mr-1" v-on="on">
        <IconMdiComment class="mr-1" />
        {{ i18n.t(sendToReporter ? "flagReview.notification.reporterButton" : "flagReview.notification.button") }}
      </Button>
    </template>
  </Modal>
</template>
