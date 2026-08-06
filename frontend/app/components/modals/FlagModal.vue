<script lang="ts" setup>
import type { HangarProject } from "#shared/types/backend";

const props = defineProps<{
  project: HangarProject;
  openReport?: boolean;
  disabled?: boolean;
}>();

const emit = defineEmits<{
  (e: "reported"): void;
}>();

const i18n = useI18n();

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
  } catch (err) {
    handleRequestError(err);
  }
}
</script>

<template>
  <Modal :title="i18n.t('project.flag.flagProject', [project.name])" window-classes="w-150">
    <template #default>
      <InputRadio v-for="(reason, index) in useBackendData.flagReasons" :key="index" v-model="flagReason" :label="i18n.t(reason.title)" :value="reason.type" />
      <div class="py-2" />
      <InputTextarea v-model.trim="flagComment" rows="3" :rules="[required()]" :label="i18n.t('general.comment')" />
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
        <Button
          variant="outline"
          tone="neutral"
          size="sm"
          icon-only
          :disabled="openReport || disabled"
          :aria-label="i18n.t(openReport ? 'project.actions.openReport' : 'project.actions.flag')"
          v-on="on"
        >
          <IconMdiFlag />
        </Button>
      </Tooltip>
    </template>
    <template #footer="{ on }">
      <Button variant="ghost" tone="neutral" v-on="on">{{ i18n.t("general.cancel") }}</Button>
      <Button :disabled="!flagReason || !flagComment" @click="submit(on.click)">{{ i18n.t("general.submit") }}</Button>
    </template>
  </Modal>
</template>
