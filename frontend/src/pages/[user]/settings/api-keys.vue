<script lang="ts" setup>
import PageTitle from "~/lib/components/design/PageTitle.vue";
import { useI18n } from "vue-i18n";
import InputText from "~/lib/components/ui/InputText.vue";
import Button from "~/lib/components/design/Button.vue";
import { reactive, ref } from "vue";
import { useContext } from "vite-ssr/vue";
import { useInternalApi } from "~/composables/useApi";
import { ApiKey, IPermission, User } from "hangar-api";
import { useRoute } from "vue-router";
import { handleRequestError } from "~/composables/useErrorHandling";
import InputCheckbox from "~/lib/components/ui/InputCheckbox.vue";
import Table from "~/lib/components/design/Table.vue";
import Alert from "~/lib/components/design/Alert.vue";
import Card from "~/lib/components/design/Card.vue";
import { useSeo } from "~/composables/useSeo";
import { avatarUrl } from "~/composables/useUrlHelper";
import { useHead } from "@vueuse/head";
import { useNotificationStore } from "~/store/notification";
import { maxLength, minLength, required } from "~/lib/composables/useValidationHelpers";
import { validApiKeyName } from "~/composables/useHangarValidations";
import { useVuelidate } from "@vuelidate/core";
import InputGroup from "~/lib/components/ui/InputGroup.vue";

const ctx = useContext();
const i18n = useI18n();
const route = useRoute();
const notification = useNotificationStore();
const v = useVuelidate();

const props = defineProps<{
  user: User;
}>();

const apiKeys = ref(await useInternalApi<ApiKey[]>("api-keys/existing-keys/" + route.params.user));
const possiblePerms = await useInternalApi<IPermission[]>("api-keys/possible-perms/" + route.params.user);

const name = ref("");
const loadingCreate = ref(false);
const loadingDelete = reactive<Record<string, boolean>>({});
const selectedPerms = ref([]);

useHead(useSeo(i18n.t("apiKeys.title") + " | " + props.user.name, null, route, avatarUrl(props.user.name)));

async function create() {
  if (!(await v.value.$validate())) return;
  loadingCreate.value = true;
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
    name.value = "";
    selectedPerms.value = [];
    v.value.$reset();
    notification.success(i18n.t("apiKeys.success.create", [name.value]));
  }
  loadingCreate.value = false;
}

async function deleteKey(key: ApiKey) {
  loadingDelete[key.name] = true;
  await useInternalApi(`api-keys/delete-key/${route.params.user}`, true, "post", {
    content: key.name,
  }).catch((err) => handleRequestError(err, ctx, i18n));
  apiKeys.value = apiKeys.value.filter((k) => k.name !== key.name);
  notification.success(i18n.t("apiKeys.success.delete", [key.name]));
  loadingDelete[key.name] = false;
}
</script>

<template>
  <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
    <Card>
      <template #header>
        <PageTitle>{{ i18n.t("apiKeys.createNew") }}</PageTitle>
      </template>
      <div class="flex">
        <div class="flex-grow">
          <InputText v-model="name" :label="i18n.t('apiKeys.name')" :rules="[required(), minLength()(5), maxLength()(255), validApiKeyName()(user.name)]" />
        </div>
        <Button class="ml-2 w-max" :loading="loadingCreate || v.$pending" :disabled="v.$invalid" @click="create">{{ i18n.t("apiKeys.createKey") }}</Button>
      </div>
      <InputGroup v-model="selectedPerms" :label="i18n.t('apiKeys.permissions')" :rules="[required(), minLength()(1)]" class="w-full mt-2">
        <div class="grid autofix mt-2">
          <InputCheckbox v-for="perm in possiblePerms" :key="perm" v-model="selectedPerms" :label="perm" :value="perm" />
        </div>
      </InputGroup>
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
              <Button :loading="loadingDelete[key.name]" @click="deleteKey(key)">{{ i18n.t("apiKeys.deleteKey") }}</Button>
            </td>
          </tr>
          <tr v-if="apiKeys.length === 0">
            <td colspan="5">
              <Alert class="mt-4" type="warning">
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
