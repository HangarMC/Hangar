<script lang="ts" setup>
const props = defineProps<{
  modelValue: { [key: string]: boolean };
}>();

const i18n = useI18n();
const notification = useNotificationStore();

const emit = defineEmits<{
  (e: "update:modelValue", value: { [key: string]: boolean }): void;
}>();

const loading = ref(false);
const draftVisibility = ref<Record<string, boolean>>({});

const internalVisibility = computed({
  get: () => props.modelValue,
  set: (v) => emit("update:modelValue", v),
});

const orgs = computed(() => Object.keys(internalVisibility.value));
const hasChanges = computed(() => orgs.value.some((org) => draftVisibility.value[org] !== internalVisibility.value[org]));

function resetForm() {
  draftVisibility.value = { ...internalVisibility.value };
}

async function save(close: () => void) {
  if (loading.value || !hasChanges.value) {
    return;
  }

  loading.value = true;
  try {
    const changedOrgs = orgs.value.filter((org) => draftVisibility.value[org] !== internalVisibility.value[org]);
    await Promise.all(
      changedOrgs.map((org) => useInternalApi(`organizations/${org}/userOrganizationsVisibility?hidden=${draftVisibility.value[org]}`, "post"))
    );
    internalVisibility.value = { ...draftVisibility.value };
    notification.success("Saved!");
    close();
  } catch (err) {
    handleRequestError(err);
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <Modal
    :title="i18n.t('author.editOrgVisibility')"
    window-classes="w-full max-w-xl !rounded-xl border border-gray-200 dark:border-gray-800 shadow-lg !bg-white dark:!bg-charcoal-900"
    close-button-right
    @open="resetForm"
    @close="resetForm"
  >
    <template #default="{ on }">
      <p class="mb-4 text-sm leading-relaxed text-gray-600 dark:text-gray-400">
        {{ i18n.t("author.orgVisibilityModal") }}
      </p>

      <ul class="flex flex-col gap-2">
        <li v-for="org in orgs" :key="org">
          <InputCheckbox v-model="draftVisibility[org]" :label="org" :disabled="loading" />
        </li>
      </ul>

      <div class="mt-2.5 flex justify-end gap-2 pt-2">
        <Button button-type="secondary" size="small" :disabled="loading" @click="on.click">
          {{ i18n.t("general.close") }}
        </Button>
        <Button size="small" :disabled="!hasChanges" :loading="loading" @click="save(on.click)">
          <IconMdiContentSaveOutline class="mr-1" />
          {{ i18n.t("general.save") }}
        </Button>
      </div>
    </template>
    <template #activator="{ on }">
      <Button button-type="borderless" class="!h-8 !w-8 !p-0 text-sm" aria-label="Edit organization visibility" v-on="on">
        <IconMdiPencil />
      </Button>
    </template>
  </Modal>
</template>
