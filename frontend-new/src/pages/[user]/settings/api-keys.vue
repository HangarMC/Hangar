<script lang="ts" setup>
import PageTitle from "~/components/design/PageTitle.vue";
import { useI18n } from "vue-i18n";
import InputText from "~/components/ui/InputText.vue";
import Button from "~/components/design/Button.vue";
import { ref, watch } from "vue";
import { useContext } from "vite-ssr/vue";
import { useInternalApi } from "~/composables/useApi";
import { ApiKey, HangarApiException, IPermission, User } from "hangar-api";
import { useRoute } from "vue-router";
import { AxiosError } from "axios";
import { handleRequestError } from "~/composables/useErrorHandling";
import InputCheckbox from "~/components/ui/InputCheckbox.vue";
import Table from "~/components/design/Table.vue";
import Alert from "~/components/design/Alert.vue";
import Card from "~/components/design/Card.vue";
import { useSeo } from "~/composables/useSeo";
import { avatarUrl } from "~/composables/useUrlHelper";
import { useHead } from "@vueuse/head";

const ctx = useContext();
const i18n = useI18n();
const route = useRoute();

const props = defineProps<{
  user: User;
}>();

const apiKeys = ref(await useInternalApi<ApiKey[]>("api-keys/existing-keys/" + route.params.user));
const possiblePerms = await useInternalApi<IPermission[]>("api-keys/possible-perms/" + route.params.user);

// TODO validation, loading animations, validation loading
const name = ref("");
const loading = ref(false);
const selectedPerms = ref([]);
const validateLoading = ref(false);
const nameErrorMessages = ref<string[]>([]);

useHead(useSeo(i18n.t("apiKeys.title") + " | " + props.user.name, null, route, avatarUrl(props.user.name)));

watch(name, async (val: string) => {
  if (!val) return;
  validateLoading.value = true;
  nameErrorMessages.value = [];
  await useInternalApi(`api-keys/check-key/${route.params.user}`, true, "get", {
    name: name.value,
  }).catch((err: AxiosError<HangarApiException>) => {
    if (!err.response?.data.isHangarApiException) {
      return handleRequestError(err, ctx, i18n);
    }
    nameErrorMessages.value.push(i18n.t(err.response.data.message || ""));
  });
  validateLoading.value = false;
});

async function create() {
  loading.value = true;
  const key = await useInternalApi<string>(`api-keys/create-key/${route.params.user}`, true, "post", {
    name: name.value,
    permissions: selectedPerms.value,
  }).catch((err) => handleRequestError(err, ctx, i18n));
  if (key) {
    apiKeys.value.unshift({
      token: key,
      name: name.value,
      permissions: selectedPerms.value,
      createdAt: new Date().toISOString(),
    });
    // TODO success notification
    // this.$util.success(i18n.$t('apiKeys.success.create', [name.value]));
  }
  loading.value = true;
}

async function deleteKey(key: ApiKey) {
  loading.value = false;
  await useInternalApi(`api-keys/delete-key/${route.params.user}`, true, "post", {
    content: key.name,
  }).catch((err) => handleRequestError(err, ctx, i18n));
  apiKeys.value = apiKeys.value.filter((k) => k.name !== key.name);
  // TODO success notification
  // this.$util.success(i18n.t('apiKeys.success.delete', [key.name]));
  loading.value = true;
}
</script>

<template>
  <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
    <Card>
      <template #header>
        <PageTitle>{{ i18n.t("apiKeys.createNew") }}</PageTitle>
      </template>
      <InputText v-model="name" :label="i18n.t('apiKeys.name')"></InputText>
      <Button class="ml-2" @click="create">{{ i18n.t("apiKeys.createKey") }}</Button>
      <div class="grid autofix mt-4">
        <InputCheckbox v-for="perm in possiblePerms" :key="perm" v-model="selectedPerms" :label="perm" :value="perm" />
      </div>
    </Card>
    <Card>
      <template #header>
        <PageTitle>{{ i18n.t("apiKeys.existing") }}</PageTitle>
      </template>
      <Table>
        <thead>
          <tr>
            <th>
              {{ i18n.t("apiKeys.name") }}
            </th>
            <th>
              {{ i18n.t("apiKeys.key") }}
            </th>
            <th>
              {{ i18n.t("apiKeys.keyIdentifier") }}
            </th>
            <th>
              {{ i18n.t("apiKeys.permissions") }}
            </th>
            <th>
              {{ i18n.t("apiKeys.delete") }}
            </th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="key in apiKeys" :key="key.name">
            <td>{{ key.name }}</td>
            <td>{{ key.token }}</td>
            <td>{{ key.tokenIdentifier }}</td>
            <td>{{ key.permissions.join(", ") }}</td>
            <td>
              <Button @click="deleteKey(key)">{{ i18n.t("apiKeys.deleteKey") }}</Button>
            </td>
          </tr>
          <tr v-if="apiKeys.length === 0">
            <td colspan="5">
              <Alert class="mt-4">
                {{ i18n.t("apiKeys.noKeys") }}
              </Alert>
            </td>
          </tr>
        </tbody>
      </Table>
    </Card>
  </div>
</template>

<route lang="yaml">
meta:
  requireCurrentUser: true
  requireGlobalPerm: ["EDIT_API_KEYS"]
</route>

<style lang="scss" scoped>
.autofix {
  grid-template-columns: repeat(auto-fit, 250px);
}
</style>
