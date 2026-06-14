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
      <p class="text-sm leading-relaxed text-gray-600 dark:text-gray-400">
        {{ i18n.t("author.orgVisibilityModal") }}
      </p>

      <div class="mt-4 max-h-72 overflow-y-auto">
        <table class="w-full table-fixed border-collapse">
          <thead class="text-left text-xs font-semibold text-gray">
            <tr class="border-b border-gray-200 dark:border-gray-800">
              <th class="px-3 py-2.5">Organization</th>
              <th class="w-20 px-3 py-2.5 text-center">Hidden</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="org in orgs" :key="org" class="border-b border-gray-200 last:border-b-0 dark:border-gray-800">
              <td class="truncate px-3 py-2.5 font-semibold">{{ org }}</td>
              <td class="px-3 py-2.5 text-center">
                <label class="inline-flex cursor-pointer items-center justify-center">
                  <input v-model="draftVisibility[org]" type="checkbox" class="peer sr-only" :disabled="loading" />
                  <span
                    class="inline-flex h-5 w-5 items-center justify-center rounded border border-gray-400 bg-gray-200 text-white transition-colors peer-checked:border-primary-500 peer-checked:bg-primary-500 peer-focus-visible:ring-2 peer-focus-visible:ring-primary-500/50 peer-disabled:cursor-not-allowed peer-disabled:opacity-50 dark:border-gray-600 dark:bg-gray-700"
                  >
                    <IconMdiCheck v-if="draftVisibility[org]" class="text-sm" />
                  </span>
                </label>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="mt-5 flex items-center justify-end gap-2">
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
