<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import { reactive, ref } from "vue";
import type { ApiKey } from "hangar-api";
import { useVuelidate } from "@vuelidate/core";
import PageTitle from "~/components/design/PageTitle.vue";
import InputText from "~/components/ui/InputText.vue";
import Button from "~/components/design/Button.vue";
import { useInternalApi } from "~/composables/useApi";
import { handleRequestError } from "~/composables/useErrorHandling";
import InputCheckbox from "~/components/ui/InputCheckbox.vue";
import Table from "~/components/design/Table.vue";
import Alert from "~/components/design/Alert.vue";
import { useNotificationStore } from "~/store/notification";
import { maxLength, minLength, required } from "~/composables/useValidationHelpers";
import { validApiKeyName } from "~/composables/useHangarValidations";
import InputGroup from "~/components/ui/InputGroup.vue";
import type { NamedPermission } from "~/types/enums";
import Tooltip from "~/components/design/Tooltip.vue";
import { useAuthStore } from "~/store/auth";
import PrettyTime from "~/components/design/PrettyTime.vue";

const i18n = useI18n();
const notification = useNotificationStore();
const v = useVuelidate();
const auth = useAuthStore();

const results = await Promise.all([
  useInternalApi<ApiKey[]>("api-keys/existing-keys/" + auth.user?.name),
  useInternalApi<NamedPermission[]>("api-keys/possible-perms/" + auth.user?.name),
]);
const apiKeys = ref(results[0]);
const possiblePerms = results[1];

const name = ref("");
const loadingCreate = ref(false);
const loadingDelete = reactive<Record<string, boolean>>({});
const selectedPerms = ref([]);
const createdKey = ref<string | null>(null);

async function create() {
  if (!(await v.value.$validate())) return;
  loadingCreate.value = true;
  const key = await useInternalApi<string>(`api-keys/create-key/${auth.user?.name}`, "post", {
    name: name.value,
    permissions: selectedPerms.value,
  }).catch((err) => handleRequestError(err));
  if (key) {
    createdKey.value = key;
    apiKeys.value.unshift({
      tokenIdentifier: key.substring(0, key.indexOf(".")),
      name: name.value,
      permissions: selectedPerms.value,
      createdAt: new Date().toISOString(),
    });
    const val = name.value;
    name.value = "";
    selectedPerms.value = [];
    v.value.$reset();
    notification.success(i18n.t("apiKeys.success.create", [val]));
  }
  loadingCreate.value = false;
}

async function deleteKey(key: ApiKey) {
  loadingDelete[key.name] = true;
  await useInternalApi(`api-keys/delete-key/${auth.user?.name}`, "post", {
    content: key.name,
  }).catch((err) => handleRequestError(err));
  apiKeys.value = apiKeys.value.filter((k) => k.name !== key.name);
  notification.success(i18n.t("apiKeys.success.delete", [key.name]));
  loadingDelete[key.name] = false;
}

const copied = ref(false);
function copy(event: any) {
  const clipboardData = event.clipboardData || event.originalEvent?.clipboardData || navigator.clipboard;
  clipboardData.writeText(createdKey.value as string);
  copied.value = true;
  setTimeout(() => (copied.value = false), 2000);
}
</script>

<template>
  <div v-if="auth.user" class="space-y-2">
    <Alert v-if="createdKey" type="info" class="flex">
      {{ i18n.t("apiKeys.created") }}
      <br />
      {{ createdKey }}
      <span class="flex-grow" />
      <Tooltip :hover="false" :show="copied">
        <template #content>
          {{ i18n.t("apiKeys.copied") }}
        </template>
        <Button button-type="secondary" size="large" @click="copy">
          {{ i18n.t("apiKeys.copy") }}
          <IconMdiContentCopy class="ml-1" />
        </Button>
      </Tooltip>
    </Alert>
    <PageTitle>{{ i18n.t("auth.settings.apiKeys.header") }}</PageTitle>
    <div>
      <h3 class="text-lg font-bold mb-2">{{ i18n.t("apiKeys.createNew") }}</h3>
      <div class="flex items-center">
        <div class="flex-grow mr-2">
          <InputText v-model="name" :label="i18n.t('apiKeys.name')" :rules="[required(), minLength()(5), maxLength()(36), validApiKeyName()(auth.user.name)]" />
        </div>
        <Button size="medium" class="w-max" :disabled="v.$invalid || loadingCreate || v.$pending || selectedPerms.length === 0" @click="create">
          {{ i18n.t("apiKeys.createKey") }}
        </Button>
      </div>
      <InputGroup v-model="selectedPerms" :label="i18n.t('apiKeys.permissions')" class="mt-4 text-lg font-bold" full-width>
        <div class="grid autofix mt-2">
          <InputCheckbox v-for="perm in possiblePerms" :key="perm" v-model="selectedPerms" :label="perm" :value="perm" />
        </div>
      </InputGroup>
      <div v-if="selectedPerms.length === 0" class="text-red font-bold mt-1">{{ i18n.t("apiKeys.permissionRequired") }}</div>
    </div>
    <div>
      <h3 class="text-lg font-bold mb-2">{{ i18n.t("apiKeys.existing") }}</h3>
      <Table>
        <thead>
          <tr>
            <th>
              {{ i18n.t("apiKeys.name") }}
            </th>
            <th>
              {{ i18n.t("apiKeys.keyIdentifier") }}
            </th>
            <th>
              {{ i18n.t("apiKeys.permissions") }}
            </th>
            <th class="min-w-100px">
              {{ i18n.t("apiKeys.lastUsed") }}
            </th>
            <th />
          </tr>
        </thead>
        <tbody>
          <tr v-for="key in apiKeys" :key="key.name">
            <td>{{ key.name }}</td>
            <td>{{ key.tokenIdentifier }}</td>
            <td>{{ key.permissions.join(", ") }}</td>
            <td><PrettyTime v-if="key.lastUsed" :time="key.lastUsed" long /></td>
            <td>
              <Button button-type="red" :loading="loadingDelete[key.name]" @click="deleteKey(key)"><IconMdiDelete /></Button>
            </td>
          </tr>
          <tr v-if="apiKeys.length === 0">
            <td colspan="5">
              {{ i18n.t("apiKeys.noKeys") }}
            </td>
          </tr>
        </tbody>
      </Table>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.autofix {
  grid-template-columns: repeat(auto-fit, 250px);
}
</style>
