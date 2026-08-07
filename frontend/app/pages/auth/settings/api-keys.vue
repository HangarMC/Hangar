<script lang="ts" setup>
import type { ApiKey, NamedPermission, SettingsResponse } from "#shared/types/backend";
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
const selectedPerms = ref<NamedPermission[]>([]);
const createdKey = ref<string | undefined>();
const keyRevealed = ref(false);

const permLabel = (perm: string) => perm.replaceAll("_", " ").replace(/^./, (c) => c.toUpperCase());
const allSelected = computed(() => (possiblePerms.value?.length ?? 0) > 0 && selectedPerms.value.length === possiblePerms.value?.length);

function toggleAll() {
  selectedPerms.value = allSelected.value ? [] : [...(possiblePerms.value ?? [])];
}

async function create() {
  if (!(await v.value.$validate())) return;
  loadingCreate.value = true;
  const key = await useInternalApi<string>(`api-keys/create-key/${auth.user?.name}`, "post", {
    name: name.value,
    permissions: selectedPerms.value,
  }).catch((err) => handleRequestError(err));
  if (key) {
    createdKey.value = key;
    keyRevealed.value = false;
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
  <div v-if="auth.user">
    <PageTitle>{{ i18n.t("auth.settings.apiKeys.header") }}</PageTitle>

    <div v-if="createdKey" class="mb-5 rounded-lg background-card p-4">
      <div class="flex items-start gap-3">
        <div class="h-9 w-9 flex flex-shrink-0 items-center justify-center rounded-lg bg-lime-500/15 text-lg text-lime-500">
          <IconMdiKeyVariant />
        </div>
        <div class="min-w-0 flex-1">
          <h2 class="font-bold">{{ i18n.t("apiKeys.createdTitle") }}</h2>
          <p class="mt-0.5 text-sm text-gray-secondary">{{ i18n.t("apiKeys.created") }}</p>
          <div class="mt-2 flex flex-wrap items-center gap-2">
            <code class="min-w-0 flex-1 truncate rounded background-card px-2 py-1.5 text-sm">
              {{ keyRevealed ? createdKey : "•".repeat(48) }}
            </code>
            <Button variant="outline" tone="neutral" size="sm" @click="keyRevealed = !keyRevealed">
              <IconMdiEyeOff v-if="keyRevealed" />
              <IconMdiEye v-else />
              {{ i18n.t(keyRevealed ? "apiKeys.hide" : "apiKeys.reveal") }}
            </Button>
            <Tooltip :hover="false" :show="copied">
              <template #content>{{ i18n.t("apiKeys.copied") }}</template>
              <Button variant="outline" tone="neutral" size="sm" @click="copy">
                <IconMdiContentCopy />
                {{ i18n.t("apiKeys.copy") }}
              </Button>
            </Tooltip>
          </div>
        </div>
        <Button
          variant="ghost"
          tone="neutral"
          size="sm"
          icon-only
          :title="i18n.t('general.close')"
          :aria-label="i18n.t('general.close')"
          @click="createdKey = undefined"
        >
          <IconMdiClose />
        </Button>
      </div>
    </div>

    <section class="mt-5">
      <h3 class="text-lg font-bold">{{ i18n.t("apiKeys.createNew") }}</h3>

      <div class="mt-3 max-w-md">
        <InputText v-model="name" :label="i18n.t('apiKeys.name')" :rules="[required(), minLength()(5), maxLength()(36), validApiKeyName()(auth.user.name)]" />
      </div>

      <div class="mt-4 flex flex-wrap items-center gap-2">
        <h3 class="flex-grow font-semibold">{{ i18n.t("apiKeys.permissions") }}</h3>
        <span class="text-sm text-gray-secondary tabular-nums">{{ selectedPerms.length }}/{{ possiblePerms?.length ?? 0 }}</span>
        <Button variant="ghost" tone="neutral" size="sm" @click="toggleAll">
          {{ i18n.t(allSelected ? "apiKeys.clearAll" : "apiKeys.selectAll") }}
        </Button>
      </div>

      <InputGroup v-model="selectedPerms" full-width>
        <div class="mt-2 grid gap-x-4 gap-y-1 sm:grid-cols-2 lg:grid-cols-3">
          <InputCheckbox v-for="perm in possiblePerms" :key="perm" v-model="selectedPerms" :label="permLabel(perm)" :value="perm" />
        </div>
      </InputGroup>

      <div class="mt-4 flex flex-wrap items-center gap-3 border-t border-gray-300 pt-3 dark:border-gray-700">
        <p class="flex-grow text-sm text-gray-secondary">
          {{ selectedPerms.length === 0 ? i18n.t("apiKeys.permissionRequired") : i18n.t("apiKeys.createHint") }}
        </p>
        <Button :disabled="v.$invalid || loadingCreate || v.$pending || selectedPerms.length === 0" :loading="loadingCreate" @click="create">
          <IconMdiPlus />
          {{ i18n.t("apiKeys.createKey") }}
        </Button>
      </div>
    </section>

    <section class="mt-8">
      <div class="flex items-center gap-2 border-b border-gray-300 pb-2 dark:border-gray-700">
        <h3 class="flex-grow text-lg font-bold">{{ i18n.t("apiKeys.existing") }}</h3>
        <span class="text-sm text-gray-secondary tabular-nums">{{ apiKeys?.length ?? 0 }}</span>
      </div>

      <ul v-if="apiKeys?.length" class="divide-y divide-gray-300 dark:divide-gray-700">
        <li v-for="key in apiKeys" :key="key.name" class="flex flex-wrap items-start gap-3 py-3">
          <div class="min-w-0 flex-1">
            <div class="flex flex-wrap items-baseline gap-x-2">
              <span class="font-semibold">{{ key.name }}</span>
              <code class="text-xs text-gray-secondary">{{ key.tokenIdentifier }}</code>
            </div>
            <div class="mt-1 flex flex-wrap gap-1">
              <Chip v-for="perm in key.permissions" :key="perm">{{ permLabel(perm) }}</Chip>
            </div>
            <div class="mt-1.5 text-xs text-gray-secondary">
              <template v-if="key.lastUsed">{{ i18n.t("apiKeys.lastUsed") }}: <PrettyTime :time="key.lastUsed" long /></template>
              <template v-else>{{ i18n.t("apiKeys.neverUsed") }}</template>
            </div>
          </div>
          <Button
            variant="outline"
            tone="danger"
            size="sm"
            :loading="loadingDelete[key.name]"
            :title="i18n.t('general.delete')"
            @click="deleteKey(key)"
          >
            <IconMdiDelete />
            {{ i18n.t("general.delete") }}
          </Button>
        </li>
      </ul>
      <div v-else class="flex flex-col items-center py-10 text-center">
        <div class="mb-3 h-12 w-12 flex items-center justify-center rounded-full background-card text-xl text-gray-secondary">
          <IconMdiKeyVariant />
        </div>
        <p class="text-gray-secondary">{{ i18n.t("apiKeys.noKeys") }}</p>
      </div>
    </section>
  </div>
</template>
