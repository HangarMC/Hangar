<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import { reactive, ref } from "vue";
import { ApiKey, User } from "hangar-api";
import { useRoute } from "vue-router";
import { useHead } from "@vueuse/head";
import { useVuelidate } from "@vuelidate/core";
import PageTitle from "~/lib/components/design/PageTitle.vue";
import InputText from "~/lib/components/ui/InputText.vue";
import Button from "~/lib/components/design/Button.vue";
import { useInternalApi } from "~/composables/useApi";
import { handleRequestError } from "~/composables/useErrorHandling";
import InputCheckbox from "~/lib/components/ui/InputCheckbox.vue";
import Table from "~/lib/components/design/Table.vue";
import Alert from "~/lib/components/design/Alert.vue";
import Card from "~/lib/components/design/Card.vue";
import { useSeo } from "~/composables/useSeo";
import { useNotificationStore } from "~/lib/store/notification";
import { maxLength, minLength, required } from "~/lib/composables/useValidationHelpers";
import { validApiKeyName } from "~/composables/useHangarValidations";
import InputGroup from "~/lib/components/ui/InputGroup.vue";
import { NamedPermission } from "~/types/enums";
import { definePageMeta } from "#imports";

definePageMeta({
  currentUserRequired: true,
  globalPermsRequired: ["EDIT_API_KEYS"],
});

const i18n = useI18n();
const route = useRoute();
const notification = useNotificationStore();
const v = useVuelidate();

const props = defineProps<{
  user: User;
}>();

const apiKeys = ref(await useInternalApi<ApiKey[]>("api-keys/existing-keys/" + route.params.user));
const possiblePerms = await useInternalApi<NamedPermission[]>("api-keys/possible-perms/" + route.params.user);

const name = ref("");
const loadingCreate = ref(false);
const loadingDelete = reactive<Record<string, boolean>>({});
const selectedPerms = ref([]);
const createdKey = ref<string | null>(null);

useHead(useSeo(i18n.t("apiKeys.title") + " | " + props.user.name, null, route, props.user.avatarUrl));

async function create() {
  if (!(await v.value.$validate())) return;
  loadingCreate.value = true;
  const key = await useInternalApi<string>(`api-keys/create-key/${route.params.user}`, "post", {
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
  await useInternalApi(`api-keys/delete-key/${route.params.user}`, "post", {
    content: key.name,
  }).catch((err) => handleRequestError(err));
  apiKeys.value = apiKeys.value.filter((k) => k.name !== key.name);
  notification.success(i18n.t("apiKeys.success.delete", [key.name]));
  loadingDelete[key.name] = false;
}

function copy(event: any) {
  const clipboardData = event.clipboardData || event.originalEvent?.clipboardData || navigator.clipboard;
  clipboardData.writeText(createdKey.value as string);
}
</script>

<template>
  <div class="space-y-2">
    <Alert v-if="createdKey" type="info" class="flex">
      {{ i18n.t("apiKeys.created") }}
      <br />
      {{ createdKey }}
      <span class="flex-grow" />
      <!-- todo: show tooltip/change button text when copied -->
      <Button button-type="secondary" size="large" @click="copy">{{ i18n.t("apiKeys.copy") }}</Button>
    </Alert>
    <Card>
      <template #header>
        <PageTitle>{{ i18n.t("apiKeys.createNew") }}</PageTitle>
      </template>
      <div class="flex items-center">
        <div class="flex-grow mr-2">
          <InputText v-model="name" :label="i18n.t('apiKeys.name')" :rules="[required(), minLength()(5), maxLength()(255), validApiKeyName()(user.name)]" />
        </div>
        <Button size="medium" class="w-max" :disabled="v.$invalid || loadingCreate || v.$pending || selectedPerms.length === 0" @click="create">
          {{ i18n.t("apiKeys.createKey") }}
        </Button>
      </div>
      <InputGroup v-model="selectedPerms" :label="i18n.t('apiKeys.permissions')" class="mt-4 text-xl font-bold" full-width>
        <div class="grid autofix mt-2">
          <InputCheckbox v-for="perm in possiblePerms" :key="perm" v-model="selectedPerms" :label="perm" :value="perm" />
        </div>
      </InputGroup>
      <div class="mt-2">{{ i18n.t("apiKeys.permissionRequired") }}</div>
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
              {{ i18n.t("apiKeys.keyIdentifier") }}
            </th>
            <th>
              {{ i18n.t("apiKeys.permissions") }}
            </th>
            <th />
          </tr>
        </thead>
        <tbody>
          <tr v-for="key in apiKeys" :key="key.name">
            <td>{{ key.name }}</td>
            <td>{{ key.tokenIdentifier }}</td>
            <td>{{ key.permissions.join(", ") }}</td>
            <td>
              <Button button-type="red" :loading="loadingDelete[key.name]" @click="deleteKey(key)"><IconMdiDelete /></Button>
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

<style lang="scss" scoped>
.autofix {
  grid-template-columns: repeat(auto-fit, 250px);
}
</style>
