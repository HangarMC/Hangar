<script lang="ts" setup>
import type { ApiKey, SettingsResponse } from "#shared/types/backend";
import { useApiKeys, usePossiblePerms } from "~/composables/useData";

defineProps<{
  settings?: SettingsResponse;
}>();

const i18n = useI18n();
const notification = useNotificationStore();
const v = useVuelidate();
const auth = useAuthStore();

const { apiKeys } = useApiKeys(() => auth.user!.name);
const { possiblePerms } = usePossiblePerms(() => auth.user!.name);

const name = ref("");
const loadingCreate = ref(false);
const loadingDelete = reactive<Record<string, boolean>>({});
const selectedPerms = ref([]);
const createdKey = ref<string | undefined>();

async function create() {
  if (!(await v.value.$validate())) return;
  loadingCreate.value = true;
  const key = await useInternalApi<string>(`api-keys/create-key/${auth.user?.name}`, "post", {
    name: name.value,
    permissions: selectedPerms.value,
  }).catch((err) => handleRequestError(err));
  if (key) {
    createdKey.value = key;
    if (!apiKeys.value) {
      apiKeys.value = [];
    }
    apiKeys.value.unshift({
      tokenIdentifier: key.slice(0, Math.max(0, key.indexOf("."))),
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
  apiKeys.value = apiKeys.value?.filter((k) => k.name !== key.name);
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
  <div v-if="auth.user" class="min-w-0">
    <Card v-if="createdKey" class="mb-4 !p-0 overflow-hidden border-primary-500/60">
      <div class="flex items-start gap-3 border-b px-4 py-3 dark:border-gray-800">
        <span class="inline-flex h-10 w-10 flex-shrink-0 items-center justify-center rounded-md bg-primary-500/15 color-primary">
          <IconMdiKeyVariant class="text-xl" />
        </span>
        <div class="min-w-0 flex-grow">
          <div class="flex flex-wrap items-center gap-2">
            <h2 class="text-lg font-bold">API key created</h2>
            <span class="rounded-md border border-amber-500/60 bg-amber-500/10 px-2 py-0.5 text-xs font-semibold text-amber-500">Shown once</span>
          </div>
          <p class="mt-1 text-sm text-gray">{{ i18n.t("apiKeys.created") }}</p>
        </div>
        <button
          class="inline-flex h-8 w-8 flex-shrink-0 items-center justify-center rounded-md border border-transparent text-gray transition-colors hover:border-gray-300 hover:bg-gray-100 dark:hover:border-gray-700 dark:hover:bg-gray-800"
          title="Dismiss"
          @click="createdKey = undefined"
        >
          <IconMdiClose />
        </button>
      </div>
      <div class="p-4">
        <div class="flex flex-col gap-2 sm:flex-row sm:items-stretch">
          <code
            class="min-w-0 flex-grow overflow-x-auto whitespace-nowrap rounded-lg border border-gray-200 bg-gray-100 px-3 py-2.5 text-sm dark:border-gray-800 dark:bg-charcoal-500"
          >
            {{ createdKey }}
          </code>
          <Tooltip :hover="false" :show="copied">
            <template #content>{{ i18n.t("apiKeys.copied") }}</template>
            <Button button-type="secondary" size="medium" @click="copy">
              <IconMdiContentCopy class="mr-1" />
              {{ copied ? i18n.t("apiKeys.copied") : i18n.t("apiKeys.copy") }}
            </Button>
          </Tooltip>
        </div>
        <div
          class="mt-3 flex items-start gap-2 rounded-lg border border-gray-200 bg-gray-100/60 px-3 py-2 text-sm dark:border-gray-800 dark:bg-charcoal-500/60"
        >
          <IconMdiInformationOutline class="mt-0.5 flex-shrink-0 text-gray" />
          <span>Store this key somewhere secure before leaving or refreshing this page. Do not share it with anyone.</span>
        </div>
      </div>
    </Card>

    <div class="grid grid-cols-1 items-start gap-4 xl:grid-cols-[minmax(320px,0.8fr)_minmax(0,1.2fr)]">
      <Card>
        <h2 class="text-xl font-bold">{{ i18n.t("apiKeys.createNew") }}</h2>
        <p class="mt-1 text-sm text-gray">Give the key a recognizable name and only grant the permissions it needs.</p>

        <div class="mt-4">
          <label class="mb-1.5 block text-sm font-semibold" for="api-key-name">{{ i18n.t("apiKeys.name") }}</label>
          <div class="relative flex h-10.5 rounded-md transition-all duration-200">
            <input
              id="api-key-name"
              v-model="name"
              class="min-w-0 flex-grow truncate rounded-lg border border-transparent bg-gray-100 px-3 py-2 outline-none transition-colors hover:border-gray-300 focus:border-gray-400 dark:bg-gray-800 dark:hover:border-gray-700 dark:focus:border-gray-600"
              maxlength="36"
              placeholder="Integration name"
              type="text"
            />
          </div>
        </div>

        <div class="mt-4">
          <div class="flex items-center justify-between gap-2">
            <h3 class="font-semibold">{{ i18n.t("apiKeys.permissions") }}</h3>
            <span class="text-xs text-gray">{{ selectedPerms.length }} selected</span>
          </div>
          <div class="mt-2 grid max-h-72 grid-cols-1 gap-1 overflow-y-auto rounded-lg border p-2 dark:border-gray-800 sm:grid-cols-2">
            <InputCheckbox v-for="perm in possiblePerms" :key="perm" v-model="selectedPerms" :label="perm" :value="perm" />
          </div>
          <p v-if="selectedPerms.length === 0" class="mt-2 text-sm text-red">{{ i18n.t("apiKeys.permissionRequired") }}</p>
        </div>

        <div class="mt-4 flex justify-end border-t pt-4 dark:border-gray-800">
          <Button
            size="medium"
            :disabled="name.length < 5 || name.length > 36 || loadingCreate || selectedPerms.length === 0"
            :loading="loadingCreate"
            @click="create"
          >
            <IconMdiKeyPlus class="mr-1" />
            {{ i18n.t("apiKeys.createKey") }}
          </Button>
        </div>
      </Card>

      <Card class="!p-0 overflow-hidden">
        <div class="flex items-center gap-2 border-b px-4 py-3 dark:border-gray-800">
          <h2 class="flex-grow text-xl font-bold">{{ i18n.t("apiKeys.existing") }}</h2>
          <span class="rounded-full bg-gray-100 px-2 py-0.5 text-xs text-gray dark:bg-charcoal-500">{{ apiKeys?.length || 0 }}</span>
        </div>
        <div v-if="apiKeys?.length" class="space-y-2 p-3">
          <div
            v-for="key in apiKeys"
            :key="key.name"
            class="flex flex-col gap-3 rounded-lg border border-gray-200 bg-gray-100/60 p-3 dark:border-gray-800 dark:bg-charcoal-500/60 sm:flex-row sm:items-center"
          >
            <span class="inline-flex h-10 w-10 flex-shrink-0 items-center justify-center rounded-md bg-gray-200 text-xl dark:bg-charcoal-400">
              <IconMdiKeyVariant />
            </span>
            <div class="min-w-0 flex-grow">
              <div class="flex flex-wrap items-center gap-2">
                <h3 class="truncate font-semibold">{{ key.name }}</h3>
                <code class="rounded bg-gray-200 px-1.5 py-0.5 text-xs dark:bg-charcoal-400">{{ key.tokenIdentifier }}</code>
              </div>
              <div class="mt-1 flex flex-wrap gap-1">
                <span
                  v-for="permission in key.permissions"
                  :key="permission"
                  class="rounded-md border border-gray-300 px-1.5 py-0.5 text-[0.65rem] text-gray dark:border-gray-700"
                >
                  {{ permission }}
                </span>
              </div>
              <p class="mt-1 text-xs text-gray">
                {{ i18n.t("apiKeys.lastUsed") }}:
                <PrettyTime v-if="key.lastUsed" :time="key.lastUsed" long />
                <span v-else>Never</span>
              </p>
            </div>
            <button
              class="inline-flex h-9 w-9 flex-shrink-0 items-center justify-center self-end rounded-md border border-transparent transition-all hover:border-red-600 hover:bg-red-900/50 sm:self-center"
              :disabled="loadingDelete[key.name]"
              :title="`Delete ${key.name}`"
              @click="deleteKey(key)"
            >
              <IconMdiDelete />
            </button>
          </div>
        </div>
        <p v-else class="p-6 text-center text-sm text-gray">{{ i18n.t("apiKeys.noKeys") }}</p>
      </Card>
    </div>
  </div>
</template>
