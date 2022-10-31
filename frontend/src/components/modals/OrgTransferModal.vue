<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import Button from "~/lib/components/design/Button.vue";
import Modal from "~/lib/components/modals/Modal.vue";
import { useContext } from "vite-ssr/vue";
import { useRouter } from "vue-router";
import { useApi, useInternalApi } from "~/composables/useApi";
import { handleRequestError } from "~/composables/useErrorHandling";
import InputAutocomplete from "~/lib/components/ui/InputAutocomplete.vue";
import { ref } from "vue";
import { PaginatedResult, User } from "hangar-api";
import { useNotificationStore } from "~/lib/store/notification";

const props = defineProps<{
  organization: string;
}>();

const i18n = useI18n();
const ctx = useContext();
const router = useRouter();
const notificationStore = useNotificationStore();

const search = ref<string>("");
const result = ref<string[]>([]);
const loading = ref<boolean>(false);
async function doSearch(val: string) {
  result.value = [];
  const users = await useApi<PaginatedResult<User>>("users", false, "get", {
    query: val,
    limit: 25,
    offset: 0,
  });
  result.value = users.result.filter((u) => !u.isOrganization).map((u) => u.name);
}

async function transfer() {
  loading.value = true;
  await useInternalApi<string>(`organizations/org/${props.organization}/transfer`, true, "post", {
    content: search.value,
  }).catch((e) => handleRequestError(e, ctx, i18n));
  notificationStore.success(i18n.t("organization.settings.success.transferRequest", [search.value]));
  loading.value = false;
}
</script>

<template>
  <Modal :title="i18n.t('organization.settings.transferModal.title', [organization])" window-classes="w-150">
    <template #default>
      <p class="mb-2">{{ i18n.t("organization.settings.transferModal.description", [organization]) }}</p>
      <div class="flex items-center">
        <InputAutocomplete v-model="search" :values="result" :label="i18n.t('organization.settings.transferModal.transferTo')" @search="doSearch" />
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
