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

async function transfer() {
  loading.value = true;
  try {
    await useInternalApi<string>(`organizations/org/${props.organization}/transfer`, "post", {
      content: search.value,
    });
    notificationStore.success(i18n.t("organization.settings.success.transferRequest", [search.value]));
  } catch (err) {
    handleRequestError(err);
  }
  loading.value = false;
}
</script>

<template>
  <Modal :title="i18n.t('organization.settings.transferModal.title', [organization])" window-classes="w-150">
    <template #default>
      <p class="mb-2">{{ i18n.t("organization.settings.transferModal.description", [organization]) }}</p>
      <div class="flex items-center">
        <InputAutocomplete
          id="org-transfer"
          v-model="search"
          :values="result"
          :label="i18n.t('organization.settings.transferModal.transferTo')"
          @search="doSearch"
        />
        <Button :disabled="search.length === 0" :loading="loading" class="ml-2" @click="transfer">
          <IconMdiRenameBox class="mr-2" />
          {{ i18n.t("project.settings.transfer") }}
        </Button>
      </div>
    </template>
    <template #activator="{ on }">
      <Button button-type="red" size="small" class="mr-1" v-on="on"><IconMdiCogTransfer /></Button>
    </template>
  </Modal>
</template>
