<script lang="ts" setup>
const props = defineProps<{
  title: string;
  label: string;
  submit: (msg: string) => Promise<void> | undefined;
  requireInput?: boolean;
  description?: string;
  confirmationText?: string;
  submitLabel?: string;
  destructive?: boolean;
}>();

const message = ref("");
const confirmation = ref("");
const loading = ref(false);

const i18n = useI18n();
const canSubmit = computed(
  () => !loading.value && (!props.requireInput || message.value.length > 0) && (!props.confirmationText || confirmation.value === props.confirmationText)
);

async function _submit(close: () => void) {
  if (!canSubmit.value) return;
  loading.value = true;
  await props?.submit(message.value);
  loading.value = false;
  close();
}

function reset() {
  message.value = "";
  confirmation.value = "";
  loading.value = false;
}
</script>

<template>
  <Modal
    :title="props.title"
    window-classes="w-full max-w-xl !rounded-xl border border-gray-200 dark:border-gray-800 shadow-lg !bg-white dark:!bg-charcoal-900"
    close-button-right
    @close="reset"
  >
    <template #default="{ on }">
      <div v-if="description" class="mb-4 flex items-start gap-3 rounded-lg border border-red-600/60 bg-red-900/20 p-3 text-sm">
        <IconMdiAlertOutline class="mt-0.5 flex-shrink-0 text-lg text-red-400" />
        <p class="leading-relaxed">{{ description }}</p>
      </div>

      <div class="space-y-4">
        <InputTextarea v-model.trim="message" :label="label" :min-rows="3" @keydown.enter.prevent="" />

        <div v-if="confirmationText">
          <p class="mb-2 text-sm text-gray">
            Type <strong class="text-black dark:text-white">{{ confirmationText }}</strong> exactly to continue.
          </p>
          <InputText v-model="confirmation" :placeholder="confirmationText" autocomplete="off" />
        </div>
      </div>

      <div class="mt-5 flex justify-end gap-2 pt-4">
        <Button button-type="secondary" size="medium" :disabled="loading" @click="on.click">Cancel</Button>
        <Button
          :button-type="destructive ? 'secondary' : 'primary'"
          size="medium"
          :class="{ '!border-red-600 !bg-red-900/40 text-white hover:!bg-red-900/60 disabled:!border-gray-700 disabled:!bg-transparent': destructive }"
          :loading="loading"
          :disabled="!canSubmit"
          @click="_submit(on.click)"
        >
          {{ submitLabel || i18n.t("general.submit") }}
        </Button>
      </div>
    </template>
    <template #activator="{ on }">
      <slot name="activator" :on="on" />
    </template>
  </Modal>
</template>
