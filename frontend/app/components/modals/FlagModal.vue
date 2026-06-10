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

const flagReason = ref<string>();
const flagComment = ref<string>();
const loading = ref(false);

function reasonLabel(reason: string) {
  return {
    INAPPROPRIATE_CONTENT: "Inappropriate Content",
    IMPERSONATION: "Impersonation or Deception",
    SPAM: "Spam",
    MAL_INTENT: "Malicious Intent",
    OTHER: "Other",
  }[reason];
}

function resetForm() {
  flagReason.value = undefined;
  flagComment.value = undefined;
  loading.value = false;
}

async function submit(close: () => void) {
  loading.value = true;
  try {
    await useInternalApi("flags/", "POST", {
      projectId: props.project.id,
      reason: flagReason.value,
      comment: flagComment.value,
    });
    emit("reported");
    close();
    useNotificationStore().success("Your report has been submitted");
  } catch (err) {
    handleRequestError(err);
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <Modal
    :title="`Report ${project.name}?`"
    window-classes="w-full max-w-3xl !rounded-xl border border-gray-200 dark:border-gray-800 shadow-lg !bg-white dark:!bg-charcoal-900"
    close-button-right
    @close="resetForm"
  >
    <template #default="{ on }">
      <p class="mb-3 text-sm text-gray-600 dark:text-gray-400">Select the reason that best describes why you are reporting this project.</p>

      <div class="flex flex-col gap-1">
        <InputRadio
          v-for="reason in useBackendData.flagReasons"
          :key="reason.type"
          :model-value="flagReason"
          :value="reason.type"
          @update:model-value="flagReason = $event"
        >
          <IconMdiAlertOutline v-if="reason.type === 'INAPPROPRIATE_CONTENT'" class="ml-4 text-lg text-gray-400" />
          <IconMdiAccountAlertOutline v-else-if="reason.type === 'IMPERSONATION'" class="ml-4 text-lg text-gray-400" />
          <IconMdiEmailAlertOutline v-else-if="reason.type === 'SPAM'" class="ml-4 text-lg text-gray-400" />
          <IconMdiShieldAlertOutline v-else-if="reason.type === 'MAL_INTENT'" class="ml-4 text-lg text-gray-400" />
          <IconMdiHelpCircleOutline v-else class="ml-4 text-lg text-gray-400" />
          <span class="ml-2">{{ reasonLabel(reason.type) }}</span>
        </InputRadio>
      </div>

      <textarea
        v-model.trim="flagComment"
        class="mt-4 min-h-24 w-full rounded-lg border border-transparent bg-gray-100 px-3 py-2 outline-none transition-all duration-250 hover:border-gray-300 focus:border-gray-400 dark:bg-gray-800 dark:hover:border-gray-700 dark:focus:border-gray-700"
        placeholder="Comment"
        rows="3"
      />

      <div class="mt-4 flex justify-end">
        <Button button-type="secondary" size="medium" :loading="loading" :disabled="!flagReason || !flagComment" @click="submit(on.click)"> Submit </Button>
      </div>
    </template>
    <template #activator="{ on }">
      <Tooltip>
        <template #content>
          <span v-if="openReport"> You still have an unresolved report on this resource </span>
          <span v-else> Report </span>
        </template>
        <Button button-type="secondary" size="medium" :disabled="openReport || disabled" v-on="on">
          <IconMdiFlag />
          <span class="w-0 overflow-hidden !m-0">0</span>
        </Button>
      </Tooltip>
    </template>
  </Modal>
</template>
