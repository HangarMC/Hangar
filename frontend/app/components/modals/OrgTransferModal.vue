<script lang="ts" setup>
import type { PaginatedResultUser } from "#shared/types/backend";

const props = defineProps<{
  organization: string;
}>();

const i18n = useI18n();
const notificationStore = useNotificationStore();

const search = ref<string>("");
const result = ref<string[]>([]);
const loading = ref<boolean>(false);
async function doSearch(val?: string) {
  result.value = [];
  const users = await useApi<PaginatedResultUser>("users", "get", {
    query: val,
    limit: 25,
    offset: 0,
  });
  result.value = users.result?.filter((u) => !u.isOrganization).map((u) => u.name);
}

async function transfer(close: () => void) {
  if (loading.value || search.value.length === 0) {
    return;
  }

  loading.value = true;
  try {
    await useInternalApi<string>(`organizations/org/${props.organization}/transfer`, "post", {
      content: search.value,
    });
    notificationStore.success(i18n.t("organization.settings.success.transferRequest", [search.value]));
    close();
  } catch (err) {
    handleRequestError(err);
  } finally {
    loading.value = false;
  }
}

function resetForm() {
  search.value = "";
  result.value = [];
}
</script>

<template>
  <Modal
    :title="i18n.t('organization.settings.transferModal.title', [organization])"
    window-classes="w-full max-w-xl !rounded-xl border border-gray-200 dark:border-gray-800 shadow-lg !bg-white dark:!bg-charcoal-900"
    close-button-right
    @close="resetForm"
  >
    <template #default="{ on }">
      <p class="mb-4 text-sm leading-relaxed text-gray-600 dark:text-gray-400">
        {{ i18n.t("organization.settings.transferModal.description", [organization]) }}
      </p>
      <InputAutocomplete
        id="org-transfer"
        v-model="search"
        :values="result"
        :label="i18n.t('organization.settings.transferModal.transferTo')"
        @search="doSearch"
      />
      <div class="mt-5 flex justify-end gap-2">
        <Button button-type="secondary" size="medium" :disabled="loading" @click="on.click">
          {{ i18n.t("general.close") }}
        </Button>
        <Button size="medium" :disabled="search.length === 0" :loading="loading" @click="transfer(on.click)">
          <IconMdiRenameBox class="mr-2" />
          {{ i18n.t("project.settings.transfer") }}
        </Button>
      </div>
    </template>
    <template #activator="{ on }">
      <Button class="w-full" button-type="secondary" size="medium" v-on="on">
        <IconMdiCogTransfer class="mr-1" />
        Transfer
      </Button>
    </template>
  </Modal>
</template>
